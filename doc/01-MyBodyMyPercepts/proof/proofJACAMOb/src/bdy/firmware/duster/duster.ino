#include <NECK.hpp>   /* https://github.com/chon-group/neck2arduino */
#include <Servo.h>    /* https://docs.arduino.cc/libraries/servo/ */
#define PinLed 	   13			   // Led 
#define PinGreen    10			   // Alert
#define PinYellow    11			   // Alert
#define PinRed    12			   // Alert
#define PinSensor  A0        // Temp sensor
#define servoPin    3			   // Servo Motor

Apparatus(duster);
Element(duster,broom);
Element(duster,tempMonitor);

Servo servoMotor; 
float temperature = 0;
int extraTemp = 0;
bool limpando = false; 

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

void loop() {
	duster.sense();
	if (limpando) {limparOn();}
	/* min= 20ºC - max=120ºC */
	temperature = 20.0 + (analogRead(PinSensor) / 1023.0) * 100.0;
	if(temperature < 70){
		digitalWrite(PinGreen,HIGH);
  		digitalWrite(PinYellow,LOW);
		digitalWrite(PinRed,LOW);
	}else if(temperature < 100){
		digitalWrite(PinGreen,LOW);
  		digitalWrite(PinYellow,HIGH);
		digitalWrite(PinRed,LOW);
	}else{
		digitalWrite(PinGreen,LOW);
  		digitalWrite(PinYellow,LOW);
		digitalWrite(PinRed,HIGH);
		limpando = false;
	}
}

/* ****************************************** */

Perception (broom,powerStatus, PROPRIOCEPTION){
  if(limpando) return true;
  else return false;
}

Perception (tempMonitor,temp, INTEROCEPTION) {return temperature;}

/* helpers */

Action (broom,dusterOn){
  digitalWrite(PinLed, HIGH);
  limpando = true;
  return EXECUTED;  
}

Action (broom,dusterOff){
  digitalWrite(PinLed, LOW);    
  limpando = false;
  return EXECUTED;  
}


// Para o limpador
void limparOff() {
  servoMotor.write(anguloDescanso);
}

// Inicia movimento automático
void iniciarLimpeza() {
  limpando = true;
  limparOn();
}

// Para movimento automático
void pararLimpeza() {
  limpando = false;
  limparOff();
}

void limparOn() {

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