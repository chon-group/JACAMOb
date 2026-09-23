#include <NECK.hpp>   /* https://github.com/chon-group/neck2arduino */
unsigned long lastCicle = 0;

Apparatus(duster){
	Element(led);
}

Preparation{
  pinMode(13, OUTPUT);
  lastCicle = millis();
}

Behaving(led){
  if (duster.getLastPresence() > lastCicle) {
    digitalWrite(13, !digitalRead(13));
    lastCicle = millis();
  }
}