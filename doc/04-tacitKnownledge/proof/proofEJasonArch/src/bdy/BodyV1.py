import socket
import time

HOST, PORT = "localhost", 6969

server = socket.create_server((HOST, PORT))

print("[BodyV1] Waiting for EJasonArch...")

client, addr = server.accept()

print("[BodyV1] Connected:", addr)

agent = client.recv(1024).decode().strip()

print("[BodyV1] Agent:", agent)

try:
    while True:
        client.sendall(b'bodyType("A")\n')
        print('[BodyV1] Sending bodyType("A")')
        time.sleep(1)

except KeyboardInterrupt:
    print("\n[BodyV1] Stopping...")

except OSError as e:
    print("[BodyV1] Connection error:", e)

finally:
    client.close()
    server.close()