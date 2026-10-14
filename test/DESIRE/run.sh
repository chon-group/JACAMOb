#! /bin/bash
MVN="/usr/bin/mvn"
DIALOG="/usr/bin/dialog"
SIMULIDE="/usr/bin/chonos-simulide"
HERE="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
cd $HERE
clear

if [[ ! -f "$MVN" || ! -f "$DIALOG" ]]
then
    echo "The computer hasn't all required dependencies!"
    echo "Installing dependencies..."
    sleep 3
    echo "deb [trusted=yes] http://packages.chon.group/ chonos main" | sudo tee /etc/apt/sources.list.d/chonos.list
    sudo apt update
    sudo apt install -y maven dialog -y
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
    cd ../../
    mvn clean package --batch-mode
    mv target/*-all.jar $HERE/JACAMOb.jar -v
    cd $HERE
    echo ""
fi


dialog --defaultno \
        --title "Arduino" \
        --yesno "Do you want to deploy the Controlling Layer?" 7 50

if [[ $? -eq 0 ]]
then
    COMPILE=true
else
    COMPILE=false
fi
clear
if [[ "$COMPILE" == true ]]
then
    clear
    echo "Deploying Controlling Layer"
    ./src/bdy/deployBody.sh
fi



#echo "Starting the MAS!"
#java -jar JACAMOb.jar *.jcm
#echo "MAS Stopped!"

RUNS=$(dialog --stdout \
              --title "JACAMOb" \
              --inputbox "How many times do you want to run the MAS?" 8 50 1)
clear

# Enter direto devolve "1"; cancelar ou digitar algo inválido também cai em 1
if ! [[ "$RUNS" =~ ^[0-9]+$ ]] || (( 10#$RUNS < 1 ))
then
    RUNS=1
fi

for ((i = 1; i <= RUNS; i++))
do
    echo "Starting the MAS! (run $i of $RUNS)"
    java -jar JACAMOb.jar *.jcm
    echo "MAS Stopped!"
    if (( i < RUNS ))
    then
        echo "Waiting 10 seconds before the next run..."
        sleep 10
    fi
done