import socket
import time

HOST, PORT = "localhost", 6969

server = socket.create_server((HOST, PORT))

print("[Body A] Waiting for EJasonArch...")

client, addr = server.accept()

print("[BodyV2] Connected:", addr)

agent = client.recv(1024).decode().strip()

print("[BodyV2] Agent:", agent)

try:
    while True:
        client.sendall(b'bodyType("A")\n')
        print('[BodyV2] Sending bodyType("A")')
        time.sleep(1)

except KeyboardInterrupt:
    print("\n[BodyV2] Stopping...")

except OSError as e:
    print("[BodyV2] Connection error:", e)

finally:
    client.close()
    server.close()