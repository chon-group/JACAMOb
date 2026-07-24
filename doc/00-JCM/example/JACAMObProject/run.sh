#! /bin/bash
MVN="/usr/bin/mvn"
DIALOG="/usr/bin/dialog"

clear

if [[ ! -f "$MVN" || ! -f "$DIALOG" ]]
then
    echo "The computer hasn't all required dependencies!"
    echo "Installing dependencies..."
    sleep 3
    sudo apt update
    sudo apt install -y maven dialog
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
    HERE=`pwd`
    rm -rf JACAMOb.jar
    rm -rf target
    cd ../../../../
    mvn clean package --batch-mode
    mv target/*-all.jar $HERE/JACAMOb.jar -v
    cd $HERE
    echo ""
fi

echo "Starting the MAS!"
java -jar JACAMOb.jar *.jcm

echo "MAS Stopped!"
