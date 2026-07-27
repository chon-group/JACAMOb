#include <NECK.hpp>   /* https://github.com/chon-group/neck2arduino */
unsigned long lastCicle = 0;

Apparatus(duster);

void setup() {
  pinMode(13, OUTPUT);
  lastCicle = millis();
}

void loop() {
  duster.sense();

  if (duster.getLastSense() > lastCicle) {
    digitalWrite(13, !digitalRead(13));
    lastCicle = millis();
  }
}
