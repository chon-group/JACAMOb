#include <NECK.hpp>   /* https://github.com/chon-group/neck2arduino */
#define THRESHOLD 0.75

float bladderLevel = 0.0f;
unsigned long lastKidneyUpdate = 0;
boolean isEnabledTrieb  = false;
boolean isEnabledBladderLevelPercepts = false;

Apparatus(urinarySystem) {
  Element(bladder);
  Element(kidney);
}

Behaving(kidney) {
  if (millis() - lastKidneyUpdate >= 500) {
    lastKidneyUpdate = millis();
    bladderLevel += 1.0f / 60.0f;
    if (bladderLevel > 1.0f) bladderLevel = 1.0f;
  }
}

/* --------------------------------------------------------------------------
   Percepts
   -------------------------------------------------------------------------- */

Percept(bladder, bladderLevel, INTEROCEPTION) {

  if ((isEnabledTrieb) && (bladderLevel >= THRESHOLD)) {
    trieb("fullBladder", ((bladderLevel - THRESHOLD) / (1.0 - THRESHOLD)));
  } 
  
  if(isEnabledBladderLevelPercepts) return bladderLevel;
  else return "disabled";
}

/* --------------------------------------------------------------------------
   Acts
   -------------------------------------------------------------------------- */

Act(bladder, enableTrieb) {
    isEnabledTrieb = true;
    return EXECUTED;    
}

Act(bladder, disableTrieb) {
    isEnabledTrieb = false;
    return EXECUTED;    
}

Act(bladder, disableBladderLevelPercepts) {
    isEnabledBladderLevelPercepts = false;
    return EXECUTED;    
}

Act(bladder, enableBladderLevelPercepts) {
    isEnabledBladderLevelPercepts = true;
    return EXECUTED;    
}

Act(bladder, empty) {
  
  if (bladderLevel < 0.15f){
    return UNABLE;
  }else{
    bladderLevel -= 0.15f;
    return EXECUTED;    
  }
}
