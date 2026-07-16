#! /bin/bash
JACAMO="/usr/bin/jacamo"

clear
if [[ ! -f "$JACAMO" ]] 
then
    echo "The computer hasn't JaCaMo-CLI!"
    echo "Installing dependencies..."
    sleep 3
    echo "deb [trusted=yes] http://packages.chon.group/ chonos main" | sudo tee /etc/apt/sources.list.d/chonos.list
    sudo apt update
    sudo apt install jacamo-cli                                            # https://github.com/chon-group/dpkg-jacamo
else
    echo "The computer has JaCaMo"
fi

echo "Starting the MAS!"
jacamo jacamoProject.jcm
echo "FINISH!"
