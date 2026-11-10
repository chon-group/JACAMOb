
#include "common.h"

void setup() {
    configBody();
    Serial.begin(9600);
}

void loop() {
    updateBattery();

    static String msg = "";

    while (Serial.available()) {
        char c = Serial.read();

        if (c == '\n') {
            processMsg(msg);
            msg = "";
        }
        else if (c != '\r') {
            msg += c;
        }
    }
}

void processMsg(String cmd) {
    cmd.trim();

    if (cmd == "getPercepts") {
        sendPercepts();
    }
}

void sendPercepts() {
    Serial.print("batteryLevel(");
    Serial.print(getBatteryLevel(), 3);
    Serial.println(")");
}
