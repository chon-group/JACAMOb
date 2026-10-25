#include "common.h"

void setup() {
	configPinMode();
	attachServos();
	Serial.begin(9600);
}

void loop() {
  static String msg = "";
  while (Serial.available()) {
    char c = Serial.read();
    if (c == '\n') {processMsg(msg); msg = "";}
    else if (c != '\r') {msg += c;}
  }
}

void processMsg(String cmd) {
    cmd.trim();
    if (cmd == "getPercepts") sendPercepts();
    else if (cmd == "keepWalking") keepWalkingAction();
    else if (cmd == "standUp") standUpAction();
}

/* Protocolo com o Python: cada resposta é UMA linha (termina com println),
   percepções separadas por ';' e strings entre aspas, como no duster. */

const char* stateName() {
    if (state == STUCK) return "stuck";
    if (state == FALL)  return "fall";
    return "ok";
}

void printLegState() {
    Serial.print("legState(\"");
    Serial.print(stateName());
    Serial.print("\");");
}

void keepWalkingAction() {
    const char* reply = "ok";
    if (!walk()) {              // bloqueia ~1-2 s: só responde depois de andar
        isStuck();
        reply = stateName();
    }
    Serial.print("keepWalkingReply(\"");
    Serial.print(reply);
    Serial.print("\");");
    printLegState();
    Serial.println();
}

void standUpAction() {
    standUp();
    isStuck();
    Serial.print("standUpReply(\"");
    Serial.print(stateName());
    Serial.print("\");");
    printLegState();
    Serial.println();
}

void sendPercepts() {
    if (state != FALL) isStuck();
    printLegState();
    Serial.println();
}