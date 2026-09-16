#include <NECK.hpp>   /* https://github.com/chon-group/neck2arduino */
unsigned long lastCicle = 0;

Apparatus(locomotion){
	Element(led);
}

unsigned long previousBlink = 0;
void setup() {pinMode(13, OUTPUT);}
void loop() {locomotion.embody();}

Behaving(led) {
  if (millis() - previousBlink >= 250) {
      previousBlink = millis();
      digitalWrite(13, !digitalRead(13));
  }
}