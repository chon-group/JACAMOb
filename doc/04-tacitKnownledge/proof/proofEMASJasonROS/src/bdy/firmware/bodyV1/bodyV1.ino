#include <Embedded_Protocol_2.h>
Communication body;

void setup() {
  Serial.begin(9600);
}

void loop() {
  body.startBelief("bodyType");
  body.beliefAdd("A");
  body.endBelief();

  body.sendMessage();
  delay(1000);
}
