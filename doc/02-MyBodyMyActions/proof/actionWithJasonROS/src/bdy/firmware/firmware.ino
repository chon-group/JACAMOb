#include "common.h"
#include<Embedded_Protocol_2.h>
#include<ArduinoJson.h>
Communication communication;

void setup() {
	configPinMode();
	attachServos();
	Serial.begin(9600);
}

void loop() {
	while(Serial.available() > 0){ 
		String s = Serial.readString();
		if(s.equals("KEEPWALKING")) keepWalkingAction();
		if(s.equals("STANDUP")) standUpAction();
	}  

	if (state != FALL) isStuck();	

	communication.startBelief("legState");
	
	if(state==STUCK) communication.beliefAdd("stuck");
	else if(state==FALL) 	communication.beliefAdd("fall");
	else communication.beliefAdd("ok");
	
	communication.endBelief();
	communication.sendMessage();    
	
	delay(100);
	
}

void keepWalkingAction(){
	communication.startBelief("keepWalkingReply");
	if(walk()){
		communication.beliefAdd("ok");
	}else{
		isStuck();
		if(state==STUCK) communication.beliefAdd("stuck");
		else if(state==OK) communication.beliefAdd("ok");
		else communication.beliefAdd("fall");
	}
	communication.endBelief();

	communication.startBelief("legState");
	
	if(state==STUCK) communication.beliefAdd("stuck");
	else if(state==FALL) 	communication.beliefAdd("fall");
	else communication.beliefAdd("ok");
	
	communication.endBelief();
	communication.sendMessage();
}

void standUpAction(){
 	standUp();
	isStuck();
	communication.startBelief("standUpReply");
	
	if(state==STUCK) communication.beliefAdd("stuck");
	else if(state==OK) communication.beliefAdd("ok");
	else communication.beliefAdd("fall");
	
	communication.endBelief();

	communication.startBelief("legState");
	
	if(state==STUCK) communication.beliefAdd("stuck");
	else if(state==FALL) 	communication.beliefAdd("fall");
	else communication.beliefAdd("ok");
	
	communication.endBelief();
	communication.sendMessage(); 
}