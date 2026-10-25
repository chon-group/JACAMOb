#! /bin/bash
SIMULIDE="/usr/bin/chonos-simulide"
ARDUINO_CLI="/usr/bin/arduino-cli"
JACAMO="/usr/bin/jacamo"

HERE="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
cd $HERE

SIMULATION_NAME="JasonROS"
BAUDRATE=9600
SIMULATOR_DIR="$HERE/../simulator"

clear

############# DEPENDENCIAS
if [[ ! -f "$JACAMO" || ! -f "$SIMULIDE" || ! -f "$ARDUINO_CLI" ]]
then
    echo "The computer hasn't all required dependencies!"
    echo "Installing dependencies..."
    sleep 3
    echo "deb [trusted=yes] http://packages.chon.group/ chonos main" | sudo tee /etc/apt/sources.list.d/chonos.list
    sudo apt update
    sudo apt install linux-headers-`uname -r` -y
    sudo apt install chonos-serial-port-emulator -y
    sudo apt install -y dialog chonos-simulide arduino-cli jacamo-cli -y
else
    echo "The computer has all required dependencies"
fi


############## ARDUINO

# Remove previous firmware from simulator
rm -f "$SIMULATOR_DIR/firmware.hex"

# Check if firmware already exists
if [[ ! -f "$HERE/src/bdy/firmware/build/firmware.ino.hex" ]]
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
    clear
fi

# Compile firmware if necessary
if [[ "$COMPILE_FIRMWARE" == true ]]
then
    
    echo "Install required Arduino libraries"
    arduino-cli lib install "LiquidCrystal" || exit 1
    arduino-cli lib install "ArduinoJson" || exit 1
    arduino-cli lib install "Servo" || exit 1

    echo "Compiling firmware..."
    rm -rf "$HERE/src/bdy/firmware/build"
    rm -rf "$HERE/src/bdy/helpers/jammer/build"
    rm -rf "$HERE/src/bdy/helpers/proxyServoMotor/build"

    arduino-cli compile \
        --fqbn arduino:avr:uno \
        --output-dir "$HERE/src/bdy/firmware/build" \
        "$HERE/src/bdy/firmware" || exit 1

    arduino-cli compile \
        --fqbn arduino:avr:uno \
        --output-dir "$HERE/src/bdy/helpers/jammer/build" \
        "$HERE/src/bdy/helpers/jammer" || exit 1
    
    arduino-cli compile \
        --fqbn arduino:avr:uno \
        --output-dir "$HERE/src/bdy/helpers/proxyServoMotor/build" \
        "$HERE/src/bdy/helpers/proxyServoMotor" || exit 1
    
    echo "Firmware compiled successfully!"
fi

################ SIMULADOR
echo "Copy firmware to simulator"
cp "$HERE/src/bdy/firmware/build/firmware.ino.hex" "$SIMULATOR_DIR/firmware.hex" -v || exit 1
cp "$HERE/src/bdy/helpers/jammer/build/jammer.ino.hex" "$SIMULATOR_DIR/jammer.hex" -v || exit 1
cp "$HERE/src/bdy/helpers/proxyServoMotor/build/proxyServoMotor.ino.hex" "$SIMULATOR_DIR/proxyServoMotor.hex" -v || exit 1

echo "Configuring serial port baudrate..."
sed -i '/Port="\/dev\/ttyExogenous0"/ s/Baudrate="[^"]*"/Baudrate="'"$BAUDRATE"' _Bd"/' "$SIMULATOR_DIR/rosie.sim1"
sed -i '/CircId="TextComponent-481"/ s/Text="[^"]*"/Text=" '"$SIMULATION_NAME"'"/' "$SIMULATOR_DIR/rosie.sim1"

############################

echo "How many executions? [1]"
read -r EXECUTIONS
EXECUTIONS=${EXECUTIONS:-1}

# Exit if invalid
if ! [[ "$EXECUTIONS" =~ ^[1-9][0-9]*$ ]]; then
    echo "Invalid number of executions."
    exit 1
fi

for ((i=1; i<=EXECUTIONS; i++)); do

    echo "Starting the Simulation!"
    sudo /opt/group.chon/lib/SerialPortEmulator/virtualPortEmulator stop
    sudo /opt/group.chon/lib/SerialPortEmulator/virtualPortEmulator start

    cd "$HERE/../simulator"
    chonos-simulide rosie.sim1 &
    SIMULATOR_PID=$!

    cd "$HERE"
    clear
    

    until read -r -t 2 -p "PLEASE, start the execution in simulIDE. After, press ENTER to continue"
    do
        echo ""
    done

    echo "Execution $i of $EXECUTIONS"
    
    echo "Starting the MAS!"
    jacamo *.jcm
    echo "MAS Stopped!"

    echo "Stopping the Simulation (PID: $SIMULATOR_PID)..."
    kill -9 "$SIMULATOR_PID" 2>/dev/null


done

   
echo "All $EXECUTIONS executions completed!"