#!/usr/bin/env python3
"""Bridge EJasonArch <-> Arduino. Sem cache de percepções nem TTL de replies.

Cada leitura serial produz uma amostra completa do corpo. Uma resposta de ação
aparece apenas na amostra gerada pela própria ação; a leitura seguinte a remove.
"""
import queue
import re
import select
import socket
import threading
import time
from dataclasses import dataclass

import serial

HOST, PORT, BAUD = 'localhost', 6969, 9600
APPARATUS = {'locomotion': {'port': '/dev/ttyEmulatedPort0', 'poll': 1.0}}
POLL_TIMEOUT = 3.0
ACTION_TIMEOUT = 8.0
TICK = 0.05
LITERAL = re.compile(r'^[a-z][A-Za-z0-9_]*(\(.*\))?$')
ACTIONS = {'keepWalking': ('locomotion', 'keepWalkingReply'),
           'standUp': ('locomotion', 'standUpReply')}


def log(message):
    print('[PythonBridgeBody] ' + message, flush=True)


def parse(line):
    values = []
    for item in line.split(';'):
        item = item.strip()
        if item:
            if not LITERAL.fullmatch(item):
                log('WARNING: invalid literal: ' + repr(item))
            else:
                values.append(item)
    return values


def name(literal):
    return literal.split('(', 1)[0]


@dataclass(frozen=True)
class Job:
    action: str
    command: str
    expected_reply: str


class Apparatus(threading.Thread):
    def __init__(self, apparatus_name, cfg, events):
        super().__init__(name=apparatus_name, daemon=True)
        self.apparatus_name = apparatus_name
        self.poll = cfg['poll']
        self.events = events
        self.jobs = queue.Queue()
        self.running = True
        self.serial = serial.Serial(cfg['port'], BAUD, timeout=0.1)

    def read_line(self, timeout):
        deadline = time.monotonic() + timeout
        data = bytearray()
        while self.running and time.monotonic() < deadline:
            ch = self.serial.read(1)
            if ch == b'\n':
                return data.decode(errors='replace').strip()
            if ch:
                data.extend(ch)
        return None

    def request(self, command, timeout):
        # Somente esta thread acessa a serial deste apparatus.
        self.serial.reset_input_buffer()
        if command == 'getPercepts':
            log('Perceiving...')
        else:
            log('Action... ' + command)
        self.serial.write((command + '\n').encode())
        self.serial.flush()
        line = self.read_line(timeout)
        if command == 'getPercepts':
            log('Perceived: ' + line if line is not None else 'Perception timeout')
        else:
            log('Acted: ' + line if line is not None else 'Action timeout: ' + command)
        return line

    def run(self):
        next_poll = time.monotonic()
        try:
            while self.running:
                try:
                    job = self.jobs.get(timeout=max(0, next_poll - time.monotonic()))
                except queue.Empty:
                    job = None
                if job is None:
                    line = self.request('getPercepts', POLL_TIMEOUT)
                    if line is not None:
                        self.events.put(('sample', self.apparatus_name, parse(line)))
                    else:
                        log('WARNING: getPercepts timeout')
                else:
                    line = self.request(job.command, ACTION_TIMEOUT)
                    literals = parse(line) if line is not None else []
                    if any(name(l) == job.expected_reply for l in literals):
                        # Amostra do momento da ação, com reply somente nesta amostra.
                        self.events.put(('sample', self.apparatus_name, literals))
                        self.events.put(('ack', '!' + job.action))
                    else:
                        log('WARNING: missing reply for ' + job.action)
                        self.events.put(('ack', '@!' + job.action))
                next_poll = time.monotonic() + self.poll
        except serial.SerialException as exc:
            log('ERROR: serial: ' + str(exc))
        finally:
            self.running = False
            self.serial.close()


def main():
    events = queue.Queue()
    devices = {key: Apparatus(key, cfg, events) for key, cfg in APPARATUS.items()}
    time.sleep(2)  # reset Arduino ao abrir serial
    for device in devices.values():
        device.serial.reset_input_buffer()
        device.start()

    server = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    server.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
    server.bind((HOST, PORT))
    server.listen(1)
    log(f'Waiting for EJasonArch at {HOST}:{PORT}')
    client, addr = server.accept()
    log('Connected: ' + str(addr))
    pending = b''
    agent = None
    # Snapshot completo atual: não há cache histórico de respostas.
    # O último estado de cada aparelho é necessário apenas se houver vários aparelhos.
    current = {key: [] for key in devices}
    try:
        while True:
            ready, _, _ = select.select([client], [], [], TICK)
            if ready:
                data = client.recv(4096)
                if not data:
                    break
                pending += data
                while b'\n' in pending:
                    raw, pending = pending.split(b'\n', 1)
                    line = raw.decode(errors='replace').strip()
                    if not line:
                        continue
                    if agent is None:
                        agent = line
                        log('Agent: ' + agent)
                    elif line.startswith('!'):
                        action = line[1:]
                        action_name = action.split('(', 1)[0]
                        spec = ACTIONS.get(action_name)
                        if spec and devices[spec[0]].running:
                            devices[spec[0]].jobs.put(Job(action, action, spec[1]))
                        else:
                            client.sendall(('@!' + action + '\n').encode())
            if agent is None:
                continue
            while True:
                try:
                    event = events.get_nowait()
                except queue.Empty:
                    break
                if event[0] == 'ack':
                    client.sendall((event[1] + '\n').encode())
                else:
                    _, device_name, literals = event
                    # Nenhuma resposta anterior é incorporada à amostra nova.
                    current[device_name] = literals
                    snapshot = ';'.join(l for key in devices for l in current[key])
                    if snapshot:
                        client.sendall((snapshot + '\n').encode())
                    # Não reter replies entre amostras de aparelhos distintos.
                    current[device_name] = [l for l in literals if name(l) not in
                                            {reply for _, reply in ACTIONS.values()}]
    except KeyboardInterrupt:
        pass
    finally:
        for device in devices.values():
            device.running = False
        client.close()
        server.close()


if __name__ == '__main__':
    main()
