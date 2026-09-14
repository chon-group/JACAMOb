#include <NECK.hpp>   /* https://github.com/chon-group/neck2arduino */
unsigned long lastCicle = 0;

Apparatus(locomotion){
	Element(tempSensor);
	Element(anotherTempSensor);
}

void setup() {
  pinMode(13, OUTPUT);
  lastCicle = millis();
}

void loop() {
  locomotion.embody();

  if (locomotion.getLastPresence() > lastCicle) {
    digitalWrite(13, !digitalRead(13));
    lastCicle = millis();
  }
}

Percept(tempSensor,temperature,INTEROCEPTION){return 20;}
Percept(anotherTempSensor,temperature,EXTEROCEPTION){return 20;}
