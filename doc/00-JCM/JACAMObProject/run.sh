#! /bin/bash
MVN="/usr/bin/mvn"

#clear
if [[ ! -f "$MVN" ]] 
then
    echo "The computer hasn't JaCaMo-CLI!"
    echo "Installing dependencies..."
    sleep 3
    sudo apt update
    sudo apt install maven
else
    echo "The computer has Maven"
fi
echo "Compiling JACAMOb"

HERE=`pwd`
rm -rf JACAMOb.jar
rm -rf target
cd ../../../
mvn clean package
mv target/*-all.jar $HERE/JACAMOb.jar -v
cd $HERE
java -jar JACAMOb.jar *.jcm
#echo "Starting the MAS!"
#mvn -f ../../../pom.xml clean compile exec:java \
#  -Dexec.mainClass="jacamo.infra.JaCaMoLauncher" \
#  -Dexec.args="jacamoProject.jcm"
echo "FINISH!"
