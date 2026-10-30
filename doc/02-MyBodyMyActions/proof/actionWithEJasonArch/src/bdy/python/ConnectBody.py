import queue, select, socket, threading, time, serial
HOST, PORT = 'localhost', 6969
SERIAL_PORT, BAUD = '/dev/ttyEmulatedPort0', 9600
TICK, POLL_INTERVAL = 0.05, 1.0
POLL_TIMEOUT, ACTION_TIMEOUT = 3.0, 8.0
ACTIONS = {'keepWalking': 'keepWalkingReply', 'standUp': 'standUpReply'}

def log(msg):
    print('[PythonBridgeBody]', msg, flush=True)

def name(literal):
    return literal.split('(', 1)[0]

def request(arduino, command, timeout):
    percept = command == 'getPercepts'
    log('Perceiving...' if percept else f'Action... {command}')
    arduino.reset_input_buffer()
    arduino.write(f'{command}\n'.encode())
    arduino.flush()
    arduino.timeout = timeout
    line = arduino.readline().decode().strip()
    log(('Perceived: ' if percept else 'Acted: ') + line)
    return line.split(';') if line else []

def serial_worker(arduino, jobs, events):
    while True:
        try:
            action, reply = jobs.get(timeout=POLL_INTERVAL)
        except queue.Empty:
            action = reply = None
        literals = request(arduino, action or 'getPercepts',ACTION_TIMEOUT if action else POLL_TIMEOUT)
        if action is None:
            events.put((literals, None))
        else:
            events.put((literals, '!' + action))

jobs, events = queue.Queue(), queue.Queue()
arduino = serial.Serial(SERIAL_PORT, BAUD)
time.sleep(2)
arduino.reset_input_buffer()
threading.Thread(target=serial_worker, args=(arduino, jobs, events), daemon=True).start()
server = socket.create_server((HOST, PORT))
log(f'Waiting for EJasonArch at {HOST}:{PORT}')
client, addr = server.accept()
log(f'Connected: {addr}')
pending, agent = b'', None
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
            elif line.startswith('!'):
                action = line[1:]
                jobs.put((action, ACTIONS[name(action)]))
    while agent is not None and not events.empty():
        literals, ack = events.get_nowait()
        if literals:
            client.sendall((';'.join(literals) + '\n').encode())
        if ack:
            client.sendall((ack + '\n').encode())
client.close()
server.close()
arduino.close()