#include <NECK.hpp>   /* https://github.com/chon-group/neck2arduino */
unsigned long lastCicle = 0;
unsigned long previousBlink = 0;

Apparatus(locomotion){
	Element(led);
}


Preparation{
	pinMode(13, OUTPUT);
}

Behaving(led) {
  if (millis() - previousBlink >= 250) {
      previousBlink = millis();
      digitalWrite(13, !digitalRead(13));
  }
}