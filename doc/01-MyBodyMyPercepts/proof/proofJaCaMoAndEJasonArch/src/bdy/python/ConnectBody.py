import socket
import serial
import time


HOST = "localhost"
PORT = 6969

DUSTER_PORT = "/dev/ttyEmulatedPort0"
LOCOMOTION_PORT = "/dev/ttyEmulatedPort1"

BAUD_RATE = 9600


def connect_apparatus(name, port):

    print(f"[Body] Connecting {name} at {port}...")

    comm = serial.Serial(
        port=port,
        baudrate=BAUD_RATE,
        timeout=0.5
    )

    time.sleep(2)

    comm.reset_input_buffer()
    comm.reset_output_buffer()

    print(f"[Body] {name} connected.")

    return comm


def send_message(comm, message):

    comm.write((message + "\n").encode())
    comm.flush()


def receive_message(comm):

    message = comm.readline().decode().strip()

    if message:
        return message

    return None


# ---------------------------------------------------------
# BODY
# ---------------------------------------------------------

print("[Body] Starting Rosie's body...")


# Physical components

locomotion = connect_apparatus(
    "locomotion",
    LOCOMOTION_PORT
)

duster = connect_apparatus(
    "duster",
    DUSTER_PORT
)


# ---------------------------------------------------------
# Interface expected by EJasonArch
# ---------------------------------------------------------

server = socket.socket(
    socket.AF_INET,
    socket.SOCK_STREAM
)

server.setsockopt(
    socket.SOL_SOCKET,
    socket.SO_REUSEADDR,
    1
)

server.bind((HOST, PORT))
server.listen(1)


print(
    f"[Body] Waiting for EJasonArch at {HOST}:{PORT}..."
)


client, address = server.accept()

print(
    f"[Body] EJasonArch connected from {address}"
)


# EJasonArch sends the agent name immediately
agent_name = client.recv(1024).decode().strip()

print(f"[Body] Agent: {agent_name}")
print("[Body] Ready.")

client.setblocking(False)


try:

    while True:

        # -------------------------------------------------
        # Receive actions from EJasonArch
        # -------------------------------------------------

        try:

            message = client.recv(1024).decode().strip()

            if message:

                if message.startswith("!"):

                    action = message[1:]

                    send_message(
                        duster,
                        action
                    )

                    client.sendall(
                        (message + "\n").encode()
                    )

        except BlockingIOError:
            pass


        # -------------------------------------------------
        # Locomotion perceptions
        # -------------------------------------------------

        send_message(
            locomotion,
            "getPercepts"
        )

        percepts = receive_message(locomotion)

        if percepts:

            client.sendall(
                (percepts + "\n").encode()
            )


        # -------------------------------------------------
        # Duster perceptions
        # -------------------------------------------------

        send_message(
            duster,
            "getPercepts"
        )

        percepts = receive_message(duster)

        if percepts:

            client.sendall(
                (percepts + "\n").encode()
            )


        time.sleep(1)


except KeyboardInterrupt:

    print("\n[Body] Stopping...")


finally:

    locomotion.close()
    duster.close()

    client.close()
    server.close()