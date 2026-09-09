#include <NECK.hpp>   /* https://github.com/chon-group/neck2arduino */
unsigned long lastCicle = 0;

Apparatus(locomotion);

void setup() {
  pinMode(13, OUTPUT);
  lastCicle = millis();
}

void loop() {
  locomotion.sense();

  if (locomotion.getLastSense() > lastCicle) {
    digitalWrite(13, !digitalRead(13));
    lastCicle = millis();
  }
}
