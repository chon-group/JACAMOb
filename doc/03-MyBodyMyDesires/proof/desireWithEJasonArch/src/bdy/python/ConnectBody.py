
import select
import socket
import time
import serial

HOST, PORT = 'localhost', 6969
SERIAL_PORT, BAUD = '/dev/ttyEmulatedPort0', 9600

TICK = 0.05
POLL_INTERVAL = 1.0
POLL_TIMEOUT = 3.0


def log(msg):
    print('[PythonBridgeBody]', msg, flush=True)


def request(arduino):
    log('Perceiving...')

    arduino.reset_input_buffer()
    arduino.write(b'getPercepts\n')
    arduino.flush()

    arduino.timeout = POLL_TIMEOUT

    line = arduino.readline().decode().strip()

    log('Perceived: ' + line)

    return line.split(';') if line else []


arduino = serial.Serial(SERIAL_PORT, BAUD)

time.sleep(2)
arduino.reset_input_buffer()

server = socket.create_server((HOST, PORT))

log(f'Waiting for EJasonArch at {HOST}:{PORT}')

client, addr = server.accept()

log(f'Connected: {addr}')

pending = b''
agent = None
last_poll = 0.0

try:
    while True:
        if select.select([client], [], [], TICK)[0]:
            data = client.recv(4096)

            if not data:
                break

            *lines, pending = (pending + data).split(b'\n')

            for raw in lines:
                line = raw.decode().strip()

                if not line:
                    continue

                if agent is None:
                    agent = line
                    log(f'Agent: {agent}')

        if agent is not None:
            now = time.monotonic()

            if now - last_poll >= POLL_INTERVAL:
                last_poll = now

                literals = request(arduino)

                if literals:
                    client.sendall(
                        (';'.join(literals) + '\n').encode()
                    )

finally:
    client.close()
    server.close()
    arduino.close()
