#! /bin/bash
SIMULIDE="/opt/group.chon/simulide/simulide"
SERIALPORT="/dev/ttyEmulatedPort0"

clear
if [[ ! -f "$SIMULIDE" ]] || [[ ! -e "$SERIALPORT" ]]
then
    echo "The computer hasn't Simualtor!"
    echo "Installing dependencies..."
    sleep 3
    echo "deb [trusted=yes] http://packages.chon.group/ chonos main" | sudo tee /etc/apt/sources.list.d/chonos.list
    sudo apt update
    sudo apt install linux-headers-`uname -r` chonos-serial-port-emulator -y    # https://github.com/chon-group/dpkg-virtualport-driver
    sudo apt install chonos-simulide                                            # https://github.com/chon-group/dpkg-simulide
    
else
    echo "The computer has SimulIDE and SerialPort Emulator"
fi

echo "Starting SimulIDE..."

chonos-simulide Body/walkerWithJACAMOb.sim1 &
SIMULIDE_PID=$!

sleep 3
clear

until read -r -t 2 -p "PLEASE, start the execution in simulIDE. After, press ENTER to continue"
do
    echo "" 
done

echo "Starting the MAS!"
cd MAS

mvn -f ../../../pom.xml clean compile exec:java \
  -Dexec.mainClass="jacamo.infra.JaCaMoLauncher" \
  -Dexec.args="minimumJaCaMoProject.jcm"

cd ..
kill -9 $SIMULIDE_PID
echo "FINISH!"
