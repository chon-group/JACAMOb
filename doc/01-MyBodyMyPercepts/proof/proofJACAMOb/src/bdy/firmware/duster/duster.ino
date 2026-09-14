#include <NECK.hpp>   /* https://github.com/chon-group/neck2arduino */
#include <Servo.h>    /* https://docs.arduino.cc/libraries/servo/ */
#define PinLed 	   13	// Led 
#define PinGreen   10	// Alert
#define PinYellow  11	// Alert
#define PinRed     12	// Alert
#define PinSensor  A0   // Temp sensor
#define servoPin   3		// Servo Motor

Apparatus(duster){
	Element(broom);
	Element(tempSensor);
	Element(anotherTempSensor);
	Element(alertLeds);
}

Servo servoMotor; 
float temperature = 0;
int extraTemp = 0;
bool cleanning = false; 

const int anguloEsq = 20;   // ajuste conforme posição esquerda
const int anguloDir = 220;  // ajuste conforme posição direita
const int anguloDescanso = 90;

const unsigned long intervaloMovimento = 200;
unsigned long ultimoMovimento = 0;
bool movimentoEsquerda = true;

void setup() {
  pinMode(PinLed, OUTPUT);
  pinMode(PinGreen, OUTPUT);
  pinMode(PinYellow, OUTPUT);
  pinMode(PinRed, OUTPUT);
  pinMode(servoPin,  OUTPUT);      
  servoMotor.attach(servoPin);
  servoMotor.write(anguloDescanso);  
}

void loop() {duster.embody();}

/* ****************************************** */
/* min= 20ºC - max=120ºC */
Sensing(tempSensor){temperature = 20.0 + (analogRead(PinSensor) / 1023.0) * 100.0;}
Percept(tempSensor,temperature, INTEROCEPTION) {return temperature;}
Percept(anotherTempSensor,temperature, INTEROCEPTION) {return 20;}


Behaving(alertLeds){
  if(temperature > 100 & digitalRead(PinRed)) return;
  else if(temperature > 70  & digitalRead(PinYellow)) return;
  else if(temperature <= 70  & digitalRead(PinGreen)) return;
  else{
    if(temperature > 100){digitalWrite(PinGreen,LOW); digitalWrite(PinYellow,LOW); digitalWrite(PinRed,HIGH);}
    else if(temperature > 70){digitalWrite(PinGreen,LOW); digitalWrite(PinYellow,HIGH); digitalWrite(PinRed,LOW);}
    else if(temperature <= 70){digitalWrite(PinGreen,HIGH); digitalWrite(PinYellow,LOW); digitalWrite(PinRed,LOW);}
  }
}


Percept(broom, powerStatus, PROPRIOCEPTION){return digitalRead(PinLed) ? "on" : "off";}
Act (broom,dusterOn){
  if(cleanning) return ALREADY;
  digitalWrite(PinLed, HIGH);
  cleanning = true;
  servoMotor.write(anguloEsq);
  return EXECUTED;  
}
Act (broom,dusterOff){
  if(!cleanning) return ALREADY;
  digitalWrite(PinLed, LOW);    
  cleanning = false;
  servoMotor.write(anguloDescanso);
  return EXECUTED;  
}

Behaving(broom){
  if(cleanning & temperature < 110){
    unsigned long agora = millis();
    if (agora - ultimoMovimento >= intervaloMovimento) {
      ultimoMovimento = agora;
      if (movimentoEsquerda) {
        servoMotor.write(anguloEsq);
      } else {
        servoMotor.write(anguloDir);
      }
      movimentoEsquerda = !movimentoEsquerda;
    }
  }
}
