
#include "common.h"
#include <Embedded_Protocol_2.h>
#include <ArduinoJson.h>

Communication communication;

void setup() {
    configBody();
    Serial.begin(9600);
}

void loop() {
    updateBattery();
    communication.startBelief("batteryLevel");
    communication.beliefAdd(getBatteryLevel());
    communication.endBelief();
    communication.sendMessage();
    delay(100);
}
