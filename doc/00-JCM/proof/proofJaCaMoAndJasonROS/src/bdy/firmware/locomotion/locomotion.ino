/* Dependences */
/* https://github.com/frankjoshua/rosserial_arduino_lib */
/* https://github.com/bblanchon/ArduinoJson.git */
/* https://github.com/embedded-mas/embedded-mas/tree/master/src/arduino/Embedded_Protocol_2 */


#include<Embedded_Protocol_2.h>
#include<ArduinoJson.h>

Communication locomotion;

void setup() {
  pinMode(13,OUTPUT);
  Serial.begin(9600);
}

void loop() {
	delay(500);
	if(digitalRead(13)){
		digitalWrite(13,LOW);
		locomotion.startBelief("locomotion");
		locomotion.beliefAdd(0);
		locomotion.endBelief();
		locomotion.sendMessage();
	}else{
		digitalWrite(13,HIGH);
		locomotion.startBelief("locomotion");
		locomotion.beliefAdd(1);
		locomotion.endBelief();
		locomotion.sendMessage();
	}    
}