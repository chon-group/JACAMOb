#include <Servo.h> // https://docs.arduino.cc/libraries/servo/ 
const byte Led = 13, Green = 10, Yellow = 11, Red = 12; 
const byte TempSensor = A0, servoPin = 3; 

Servo servoMotor;
const int leftAngle = 20, rightAngle = 220, restAngle = 90;

const int movementInterval = 200;
unsigned long lastMovement = 0;
float temp = 0;
bool cleaning = false, movingLeft = true;

// TogglePower
void togglePower(){
    cleaning = !cleaning;
    digitalWrite(Led, cleaning);
    servoMotor.write(cleaning ? leftAngle : restAngle);
}

// Temperature normalized between 20 and 120 degrees
void getTemp(){
    temp = 20.0 + (analogRead(TempSensor) / 1023.0) * 100.0;
}
// Broom-arm movement
void broomArmMovement(){
    if (!cleaning || temp >= 110) return;

    unsigned long now = millis();
    if ((now - lastMovement) < movementInterval) return;

    servoMotor.write(movingLeft ? leftAngle : rightAngle);
    lastMovement = now;
    movingLeft = !movingLeft;
}

// Temperature indicator LEDs
void manageLeds(){
 if (temp > 100 && !digitalRead(Red)){setLeds(LOW, LOW, HIGH);}
 else if (temp > 70 && !digitalRead(Yellow)){setLeds(LOW, HIGH, LOW);}
 else if (temp <= 70 && !digitalRead(Green)){setLeds(HIGH, LOW, LOW);}
}
void setLeds(bool greenValue, bool yellowValue, bool redValue){
    digitalWrite(Green, greenValue);
    digitalWrite(Yellow, yellowValue);
    digitalWrite(Red, redValue);
}

/*new*/
#include <NECK.hpp>   /* https://github.com/chon-group/neck2arduino */

Apparatus(duster){
	Element(broom);
	Element(tempSensor);
	Element(alertLeds);
}

Preparation{
  pinMode(Led, OUTPUT);
  pinMode(Green, OUTPUT);
  pinMode(Yellow, OUTPUT);
  pinMode(Red, OUTPUT);
  pinMode(servoPin,  OUTPUT);      
  servoMotor.attach(servoPin);
  servoMotor.write(restAngle);  
}

Sensing(tempSensor){getTemp();}
Percept(tempSensor,temperature, INTEROCEPTION) {return temp;}

Percept(broom, powerStatus, PROPRIOCEPTION){return digitalRead(Led) ? "On" : "Off";}
Act (broom,tooglePower){
  togglePower();
  return EXECUTED;  
}

Behaving(broom){broomArmMovement();}

Behaving(alertLeds){manageLeds();}
