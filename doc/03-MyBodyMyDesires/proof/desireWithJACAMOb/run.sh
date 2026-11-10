#!/bin/bash

MVN="/usr/bin/mvn"
SIMULIDE="/usr/bin/chonos-simulide"
ARDUINO_CLI="/usr/bin/arduino-cli"

HERE="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$HERE" || exit 1

SIMULATION_NAME="JACAMOb"
BAUDRATE=115200
SIMULATOR_DIR="$HERE/../simulation"
FIRMWARE_DIR="$HERE/src/bdy/firmware"
FIRMWARE_BUILD="$FIRMWARE_DIR/build"
JCM_FILE="desireWithJACAMOb.jcm"

clear

############# DEPENDENCIES

if [[ ! -f "$MVN" || ! -f "$SIMULIDE" || ! -f "$ARDUINO_CLI" ]]; then
    echo "The computer hasn't all required dependencies!"
    echo "Installing dependencies..."

    echo "deb [trusted=yes] http://packages.chon.group/ chonos main" |
        sudo tee /etc/apt/sources.list.d/chonos.list

    sudo apt update
    sudo apt install -y "linux-headers-$(uname -r)"
    sudo apt install -y chonos-serial-port-emulator
    sudo apt install -y maven chonos-simulide arduino-cli
else
    echo "The computer has all required dependencies"
fi

############# JACAMOB

COMPILE=false

if [[ ! -f "$HERE/JACAMOb.jar" ]]; then
    COMPILE=true
else
    echo "Do you want to recompile JACAMOb.jar? [y/N]"
    read -r ANSWER

    if [[ "$ANSWER" =~ ^[yYsS]$ ]]; then
        COMPILE=true
    fi
fi

if [[ "$COMPILE" == true ]]; then
    echo "Compiling JACAMOb..."

    rm -f "$HERE/JACAMOb.jar"
    rm -rf "$HERE/target"

    cd "$HERE/../../../../" || exit 1

    "$MVN" clean package --batch-mode || exit 1

    JAR_FILE=$(find target -maxdepth 1 -name '*-all.jar' -print -quit)

    if [[ -z "$JAR_FILE" ]]; then
        echo "ERROR: JACAMOb JAR not found!"
        exit 1
    fi

    cp "$JAR_FILE" "$HERE/JACAMOb.jar" || exit 1

    cd "$HERE" || exit 1
fi



############## FIRMWARE LAYER

FIRMWARE_DIR="$HERE/src/bdy/firmware"
FIRMWARE_HEX="$FIRMWARE_DIR/build/firmware.ino.hex"

# Remove previous firmware from simulator
rm -f "$SIMULATOR_DIR/firmware.hex"

# Check if firmware already exists
if [[ ! -f "$FIRMWARE_HEX" ]]
then
    COMPILE_FIRMWARE=true
else
    echo "Do you want to recompile firmware.hex? [y/N]"
    read -r ANSWER

    if [[ "$ANSWER" =~ ^[yYsS]$ ]]
    then
        COMPILE_FIRMWARE=true
    else
        COMPILE_FIRMWARE=false
    fi
fi

# Compile firmware if necessary
if [[ "$COMPILE_FIRMWARE" == true ]]
then
    echo "Install required Arduino libraries"
    arduino-cli lib install "LiquidCrystal" || exit 1

    echo "Compiling firmware..."
    rm -rf "$FIRMWARE_DIR/build"

    arduino-cli compile \
        --fqbn arduino:avr:uno \
        --output-dir "$FIRMWARE_DIR/build" \
        "$FIRMWARE_DIR" || exit 1

    echo "Firmware compiled successfully!"
fi

################ SIMULATOR

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

    echo "Starting the MAS!"

    java -jar JACAMOb.jar "$JCM_FILE"

    echo "MAS Stopped!"

    echo "Stopping SimulIDE (PID: $SIMULATOR_PID)..."

    kill "$SIMULATOR_PID" 2>/dev/null
    wait "$SIMULATOR_PID" 2>/dev/null

    echo ""
done

echo "All $EXECUTIONS executions completed!"