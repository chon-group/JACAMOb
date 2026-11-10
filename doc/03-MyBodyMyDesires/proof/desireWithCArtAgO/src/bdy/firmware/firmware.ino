#include "common.h"
#include <Javino.h>

Javino javino;

void serialEvent() {javino.readSerial();}

void setup() {
    configBody();
    javino.start(9600);
}

void loop() {
    updateBattery();

    if (javino.availableMsg()) {
        String msg = javino.getMsg();

        if (msg == "BATTERY") {
            javino.sendMsg(String(getBatteryLevel(), 3));
            return;
        }
    }
}
