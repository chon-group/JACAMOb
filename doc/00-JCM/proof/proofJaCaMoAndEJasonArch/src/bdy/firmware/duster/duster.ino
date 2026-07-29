#include <Javino.h>               //Available at: https://github.com/chon-group/javino2arduino
Javino duster;

void serialEvent(){duster.readSerial();}

void setup() {
  pinMode(13,  OUTPUT);  
  duster.start(9600);
}


void loop() {
 if(duster.availableMsg()){
  if(duster.requestPercepts()){
    digitalWrite(13,!digitalRead(13));
  }
 }
}
