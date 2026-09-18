#! /bin/bash
JACAMO="/usr/bin/jacamo"
SIMULIDE="/usr/bin/chonos-simulide"
HERE="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
cd $HERE
clear

if [[ ! -f "$JACAMO" || ! -f "$SIMULIDE" ]]
then
    echo "The computer hasn't all required dependencies!"
    echo "Installing dependencies..."
    sleep 3
    echo "deb [trusted=yes] http://packages.chon.group/ chonos main" | sudo tee /etc/apt/sources.list.d/chonos.list
    sudo apt update
    sudo apt install linux-headers-`uname -r` -y
    sudo apt install chonos-serial-port-emulator -y
    sudo apt install -y jacamo-cli chonos-simulide -y
else
    echo "The computer has all required dependencies"
fi


clear
echo "Starting the Simulation!"
cd src/bdy/simulation/
chonos-simulide rosie.sim1 &
SIMULATOR_PID=$!
cd ../../../
sleep 3
clear
until read -r -t 2 -p "PLEASE, start the execution in simulIDE. After, press ENTER to continue"
do
    echo "" 
done


echo "Starting the MAS!"
jacamo *.jcm
echo "MAS Stopped!"


echo "Stopping the Simulation (PID: $SIMULATOR_PID)..."
kill -9 "$SIMULATOR_PID" 2>/dev/null
