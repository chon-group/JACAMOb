#include <Javino.h>    /* https://github.com/chon-group/javino2arduino/releases/latest */
#include "common.h"
Javino javino;

void serialEvent() { javino.readSerial(); }

void setup() {
  configPinMode();
  attachServos();

  /* Javino Acts and Percept description */
  javino.start(9600);
}

void loop() {
  if (javino.availableMsg()) {
    String msg = javino.getMsg();

    if (msg == "LEGINFO") {
      javino.sendMsg(getLEGINFO());
      return;
    }

    if (msg == "KEEPWALKING") {
      walk();
      javino.sendMsg(getLEGINFO());
      return;
    }

    if (msg == "STANDUP") {
      standUp();
      javino.sendMsg(getLEGINFO());
      return;
    }
  }
}

String getLEGINFO() {
  if (state == FALL){return "fall";}
  if (isStuck()) return "stuck";
  else return "ok";
}