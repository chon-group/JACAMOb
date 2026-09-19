#! /bin/bash
JACAMO="/usr/bin/jacamo"
SIMULIDE="/usr/bin/chonos-simulide"

PYTHON_DEPS=true
dpkg -s python3-venv >/dev/null 2>&1 || PYTHON_DEPS=false
dpkg -s python3-pip  >/dev/null 2>&1 || PYTHON_DEPS=false
command -v wget      >/dev/null 2>&1 || PYTHON_DEPS=false



HERE="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
cd $HERE
clear

if [[ ! -f "$JACAMO" || ! -f "$SIMULIDE" || "$PYTHON_DEPS" == false ]]
then
    echo "The computer hasn't all required dependencies!"
    echo "Installing dependencies..."
    sleep 3
    echo "deb [trusted=yes] http://packages.chon.group/ chonos main" | sudo tee /etc/apt/sources.list.d/chonos.list
    sudo apt update
    sudo apt install linux-headers-`uname -r` -y
    sudo apt install chonos-serial-port-emulator -y
    sudo apt install -y jacamo-cli chonos-simulide python3-venv python3-pip wget
else
    echo "The computer has all required dependencies"
fi

clear

echo "Python ENV"
cd src/bdy/
./connectBody.sh --prepare
cd "$HERE"



################# SIMULIDE #######################

echo "Starting the Simulation!"
cd src/bdy/simulation/
chonos-simulide rosie.sim1 &
SIMULATOR_PID=$!
cd $HERE
sleep 3

until read -r -t 2 -p "PLEASE, start the execution in simulIDE. After, press ENTER to continue"
do
    echo "" 
done

sleep 1

######################### BRIDGE PYTHON #####################
echo "Connecting the simulated body"

cd src/bdy/
./connectBody.sh --connect &
BODY_PID=$!
cd "$HERE"

echo "Waiting for Rosie's body..."

for i in {1..60}
do
    # Body process died
    if ! kill -0 "$BODY_PID" 2>/dev/null
    then
        echo "ERROR: Rosie's body could not be started."
        exit 1
    fi

    # Body is listening for JasonBulb
    if ss -ltn | grep -q ':6969 '
    then
        echo "Rosie's body is ready!"
        break
    fi

    sleep 1
done

echo "Starting the MAS!"
jacamo *.jcm
echo "MAS Stopped!"


echo "Stopping the Python (PID: $BODY_PID)..."
kill -9 "$BODY_PID" 2>/dev/null
echo "Stopping the Simulation (PID: $SIMULATOR_PID)..."
kill -9 "$SIMULATOR_PID" 2>/dev/null
