#! /bin/bash
MVN="/usr/bin/mvn"
DIALOG="/usr/bin/dialog"
SIMULIDE="/usr/bin/chonos-simulide"
HERE="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
cd $HERE
clear

if [[ ! -f "$MVN" || ! -f "$DIALOG" || ! -f "$SIMULIDE" ]]
then
    echo "The computer hasn't all required dependencies!"
    echo "Installing dependencies..."
    sleep 3
    echo "deb [trusted=yes] http://packages.chon.group/ chonos main" | sudo tee /etc/apt/sources.list.d/chonos.list
    sudo apt update
    sudo apt install linux-headers-`uname -r` -y
    sudo apt install chonos-serial-port-emulator -y
    sudo apt install -y maven dialog chonos-simulide -y
else
    echo "The computer has all required dependencies"
fi


if [[ ! -f "JACAMOb.jar" ]]
then
    COMPILE=true
else
    dialog --defaultno \
           --title "JACAMOb" \
           --yesno "Do you want to recompile JACAMOb.jar?" 7 50

    if [[ $? -eq 0 ]]
    then
        COMPILE=true
    else
        COMPILE=false
    fi
    clear
fi


if [[ "$COMPILE" == true ]]
then
    echo "Compiling JACAMOb"
    rm -rf JACAMOb.jar
    rm -rf target
    cd ../../../../
    mvn clean package --batch-mode
    mv target/*-all.jar $HERE/JACAMOb.jar -v
    cd $HERE
    echo ""
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
java -jar JACAMOb.jar *.jcm
echo "MAS Stopped!"


echo "Stopping the Simulation (PID: $SIMULATOR_PID)..."
kill -9 "$SIMULATOR_PID" 2>/dev/null
