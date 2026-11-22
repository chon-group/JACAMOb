#include "batteryModule.h"
#include <Javino.h>
Javino javino;

void serialEvent() {javino.readSerial();}

void setup() {
  PowerModule::setupBattery(); 
  javino.start(9600);
}

void loop() {
  PowerModule::updateBattery();
  if (javino.availableMsg()) {
    String msg = javino.getMsg();
    if (msg == "BATTERYINFO") {
      javino.sendMsg(String(PowerModule::getBatteryLevel()));
    }
  }
}
