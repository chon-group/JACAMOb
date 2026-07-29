/* Dependences */
/* https://github.com/frankjoshua/rosserial_arduino_lib */
/* https://github.com/bblanchon/ArduinoJson.git */
/* https://github.com/embedded-mas/embedded-mas/tree/master/src/arduino/Embedded_Protocol_2 */


#include<Embedded_Protocol_2.h>
#include<ArduinoJson.h>

Communication duster;

void setup() {
  pinMode(13,OUTPUT);
  Serial.begin(9600);
}

void loop() {
	delay(500);
	if(digitalRead(13)){
		digitalWrite(13,LOW);
		duster.startBelief("duster");
		duster.beliefAdd(0);
		duster.endBelief();
		duster.sendMessage();
	}else{
		digitalWrite(13,HIGH);
		duster.startBelief("duster");
		duster.beliefAdd(1);
		duster.endBelief();
		duster.sendMessage();
	}    
}