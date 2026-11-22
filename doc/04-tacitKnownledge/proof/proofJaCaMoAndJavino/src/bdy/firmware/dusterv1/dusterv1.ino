#include "armModelA.h"
#include <Javino.h>
Javino javino;

void serialEvent() {javino.readSerial();}

void setup() {
    setupArm();
    javino.start(9600);
}

void loop() {
  if (javino.availableMsg()) {
    String msg = javino.getMsg();
    if (msg == "TOGGLEPOWER") {
      togglePower();
    }else if (msg == "getPercepts"){
      javino.sendMsg("deviceName(armModelA)");
    }
  }
}
