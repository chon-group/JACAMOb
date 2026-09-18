#include <Javino.h>               //Available at: https://github.com/chon-group/javino2arduino
Javino locomotion;

void serialEvent(){locomotion.readSerial();}

void setup() {
  pinMode(13,  OUTPUT);  
  locomotion.start(9600);
}


void loop() {
 if(locomotion.availableMsg()){
  if(locomotion.requestPercepts()){
    digitalWrite(13,!digitalRead(13));
  }
 }
}
