
#!/bin/bash

JACAMO="/usr/bin/jacamo"
SIMULIDE="/usr/bin/chonos-simulide"
ARDUINO_CLI="/usr/bin/arduino-cli"

PYTHON_DEPS=true
dpkg -s python3-venv >/dev/null 2>&1 || PYTHON_DEPS=false
dpkg -s python3-pip  >/dev/null 2>&1 || PYTHON_DEPS=false

HERE="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$HERE" || exit 1

SIMULATION_NAME="EJasonArch"
BAUDRATE=9600
SIMULATOR_DIR="$HERE/../simulation"
FIRMWARE_DIR="$HERE/src/bdy/firmware"
FIRMWARE_BUILD="$FIRMWARE_DIR/build"
FIRMWARE_HEX="$FIRMWARE_BUILD/firmware.ino.hex"
JCM_FILE="desireWithEJasonArch.jcm"

clear

############# DEPENDENCIES

if [[ ! -f "$JACAMO" || ! -f "$SIMULIDE" ||
      ! -f "$ARDUINO_CLI" || "$PYTHON_DEPS" == false ]]; then

    echo "The computer hasn't all required dependencies!"
    echo "Installing dependencies..."

    echo "deb [trusted=yes] http://packages.chon.group/ chonos main" |
        sudo tee /etc/apt/sources.list.d/chonos.list

    sudo apt update
    sudo apt install -y "linux-headers-$(uname -r)"
    sudo apt install -y chonos-serial-port-emulator
    sudo apt install -y jacamo-cli chonos-simulide arduino-cli
    sudo apt install -y python3-venv python3-pip
else
    echo "The computer has all required dependencies"
fi

############# PYTHON ENVIRONMENT

echo "Preparing Python environment..."

cd "$HERE/src/bdy" || exit 1
./connectBody.sh --prepare || exit 1
cd "$HERE" || exit 1

############# FIRMWARE LAYER

# Remove previous firmware from simulator
rm -f "$SIMULATOR_DIR/firmware.hex"

# Check if firmware already exists
if [[ ! -f "$FIRMWARE_HEX" ]]; then
    COMPILE_FIRMWARE=true
else
    echo "Do you want to recompile firmware.hex? [y/N]"
    read -r ANSWER

    if [[ "$ANSWER" =~ ^[yYsS]$ ]]; then
        COMPILE_FIRMWARE=true
    else
        COMPILE_FIRMWARE=false
    fi
fi

# Compile firmware if necessary
if [[ "$COMPILE_FIRMWARE" == true ]]; then

    echo "Install required Arduino libraries"
    arduino-cli lib install "LiquidCrystal" || exit 1

    echo "Compiling firmware..."
    rm -rf "$FIRMWARE_BUILD"

    arduino-cli compile \
        --fqbn arduino:avr:uno \
        --output-dir "$FIRMWARE_BUILD" \
        "$FIRMWARE_DIR" || exit 1

    echo "Firmware compiled successfully!"
fi

############# SIMULATOR

echo "Copy firmware to simulator"
cp "$FIRMWARE_HEX" "$SIMULATOR_DIR/firmware.hex" -v || exit 1

echo "Configuring serial port baudrate..."
sed -i \
    '/CircId="SerialPort-2"/ s/Baudrate="[^"]*"/Baudrate="'"$BAUDRATE"' _Bd"/' \
    "$SIMULATOR_DIR/rosie.sim1"

echo "Configuring experiment name..."
sed -i \
    '/CircId="TextComponent-66"/ s/Text="[^"]*"/Text="'"$SIMULATION_NAME"'"/' \
    "$SIMULATOR_DIR/rosie.sim1"

############# EXECUTIONS

echo "How many executions? [1]"
read -r EXECUTIONS

EXECUTIONS=${EXECUTIONS:-1}

if ! [[ "$EXECUTIONS" =~ ^[1-9][0-9]*$ ]]; then
    echo "Invalid number of executions."
    exit 1
fi

for ((i=1; i<=EXECUTIONS; i++)); do

    echo "========================================"
    echo "Execution $i of $EXECUTIONS"
    echo "========================================"

    echo "Starting virtual serial ports..."

    sudo /opt/group.chon/lib/SerialPortEmulator/virtualPortEmulator stop
    sudo /opt/group.chon/lib/SerialPortEmulator/virtualPortEmulator start

    echo "Starting SimulIDE..."

    cd "$SIMULATOR_DIR" || exit 1

    "$SIMULIDE" rosie.sim1 &
    SIMULATOR_PID=$!

    cd "$HERE" || exit 1

    echo ""
    read -r -p "PLEASE, start the execution in SimulIDE. Press ENTER to continue..."

    ############# PYTHON BRIDGE

    echo "Connecting the simulated body..."

    cd "$HERE/src/bdy" || exit 1
    ./connectBody.sh --connect &
    BODY_PID=$!
    cd "$HERE" || exit 1

    echo "Waiting for Rosie's body..."

    BODY_READY=false

    for ((t=1; t<=60; t++)); do

        if ! kill -0 "$BODY_PID" 2>/dev/null; then
            echo "ERROR: Rosie's body could not be started."
            kill "$SIMULATOR_PID" 2>/dev/null
            exit 1
        fi

        if ss -ltn | grep -q ':6969 '; then
            BODY_READY=true
            echo "Rosie's body is ready!"
            break
        fi

        sleep 1
    done

    if [[ "$BODY_READY" != true ]]; then
        echo "ERROR: Timeout waiting for Rosie's body."
        kill "$BODY_PID" 2>/dev/null
        kill "$SIMULATOR_PID" 2>/dev/null
        exit 1
    fi

    ############# MAS

    echo "Starting the MAS!"

    jacamo "$JCM_FILE"

    echo "MAS Stopped!"

    ############# CLEANUP

    echo "Stopping Python (PID: $BODY_PID)..."
    kill "$BODY_PID" 2>/dev/null
    wait "$BODY_PID" 2>/dev/null

    echo "Stopping SimulIDE (PID: $SIMULATOR_PID)..."
    kill "$SIMULATOR_PID" 2>/dev/null
    wait "$SIMULATOR_PID" 2>/dev/null

    echo ""
done

echo "All $EXECUTIONS executions completed!"
