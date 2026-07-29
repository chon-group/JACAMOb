import socket
import javino
import time

HOST = "localhost"
PORT = 6969

DUSTER_PORT = "/dev/ttyEmulatedPort0"
LOCOMOTION_PORT = "/dev/ttyEmulatedPort1"



def connect_apparatus(name, port):
    print(f"[Body] Connecting {name} at {port}...")

    comm = javino.start(port)

    if not comm:
        raise RuntimeError(
            f"Could not connect apparatus '{name}' at {port}"
        )

    print(f"[Body] {name} connected.")
    return comm


# ---------------------------------------------------------
# BODY
# ---------------------------------------------------------

print("[Body] Starting Rosie's body...")

# Physical components
locomotion = connect_apparatus("locomotion", LOCOMOTION_PORT)
duster = connect_apparatus("duster", DUSTER_PORT)

# Interface expected by JasonBulb
server = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
server.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
server.bind((HOST, PORT))
server.listen(1)

print(f"[Body] Waiting for EJasonArch at {HOST}:{PORT}...")

client, address = server.accept()

print(f"[Body] EJasonArch connected from {address}")

# JasonBulb sends the agent name immediately after connecting
agent_name = client.recv(1024).decode().strip()

print(f"[Body] Agent: {agent_name}")
print("[Body] Ready.")

try:
    while True:
        javino.sendMsg(locomotion, "getPercepts")
        javino.sendMsg(duster, "getPercepts")

        time.sleep(1)

except KeyboardInterrupt:
    print("\n[Body] Stopping...")

finally:
    javino.disconnect(locomotion)
    javino.disconnect(duster)

    client.close()
    server.close()