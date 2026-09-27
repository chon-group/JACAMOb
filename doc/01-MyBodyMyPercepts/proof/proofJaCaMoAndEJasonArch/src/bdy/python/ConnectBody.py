import socket
import serial
import time

HOST = "localhost"
PORT = 6969
DUSTER_PORT = "/dev/ttyEmulatedPort0"
LOCOMOTION_PORT = "/dev/ttyEmulatedPort1"
BAUD_RATE = 9600

def connect_apparatus(name, port):
    print(f"[PythonBridgeBody] Connecting {name} at {port}...")
    comm = serial.Serial(port=port, baudrate=BAUD_RATE, timeout=0.5)
    time.sleep(2)
    comm.reset_input_buffer()
    comm.reset_output_buffer()
    print(f"[PythonBridgeBody] {name} connected.")
    return comm

def send_message(comm, message):
    comm.write((message + "\n").encode())
    comm.flush()

def receive_message(comm):
    message = comm.readline().decode().strip()
    return message if message else None

# Conecta aos apparatus simulados
print("[PythonBridgeBody] Starting Rosie's body...")
locomotion = connect_apparatus("locomotion", LOCOMOTION_PORT)
duster = connect_apparatus("duster", DUSTER_PORT)

# Disponibiliza a interface para o EJasonArch
server = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
server.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
server.bind((HOST, PORT))
server.listen(1)

print(f"[PythonBridgeBody] Waiting for EJasonArch at {HOST}:{PORT}...")
client, address = server.accept()
print(f"[PythonBridgeBody] EJasonArch connected from {address}")

agent_name = client.recv(1024).decode().strip()
print(f"[PythonBridgeBody] Agent: {agent_name}")
print("[PythonBridgeBody] Ready.")
client.setblocking(False)

try:
    while True:
        # Encaminha ações para o corpo
        try:
            message = client.recv(1024).decode().strip()
            print(f"[PythonBridgeBody] Mensagem recebida: {message}")
            if message and message.startswith("!"):
                send_message(duster, message[1:])
                client.sendall((message + "\n").encode())
        except BlockingIOError:
            pass

        # Encaminha percepções para o agente
        send_message(locomotion, "getPercepts")
        percepts = receive_message(locomotion)
        if percepts:
            client.sendall((percepts + "\n").encode())
        send_message(duster, "getPercepts")
        percepts = receive_message(duster)
        if percepts:
            client.sendall((percepts + "\n").encode())
        time.sleep(1)

except KeyboardInterrupt:
    print("\n[PythonBridgeBody] Stopping...")

finally:
    locomotion.close()
    duster.close()
    client.close()
    server.close()
