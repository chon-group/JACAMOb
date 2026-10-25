
#include <NECK.hpp>   /*  https://github.com/chon-group/neck2arduino */
#include "common.h"

Apparatus(walker){
  Element(legs);
}

Preparation{
  configPinMode(); 
  attachServos();
}


Act(legs, keepWalking) {
  if (state == FALL) return REJECTED;

  if (walk()) return EXECUTED;

  return UNABLE;
}


Act(legs, standUp){

  if (state == OK) {
    return ALREADY;
  }

  standUp();

  if (state == OK) {
    return EXECUTED;
  } else {
    return UNABLE;
  }
}

Percept(legs, legState, PROPRIOCEPTION){
  if (state == FALL){return "fall";}
  if (isStuck()) return "stuck";
  else return "ok";
}


// Behaving(legs){
// 	if(millis()-walker.getLastPresence()>1000){
// 	digitalWrite(13, !digitalRead(13));
// 	delay(200);
// 	}
// }
