/* Dependences */
/* https://github.com/frankjoshua/rosserial_arduino_lib */
/* https://github.com/bblanchon/ArduinoJson.git */
/* https://github.com/embedded-mas/embedded-mas/tree/master/src/arduino/Embedded_Protocol_2 */
#include<Embedded_Protocol_2.h>
#include<ArduinoJson.h>
#include <Servo.h>    /* https://docs.arduino.cc/libraries/servo/ */

#define PinLed     13 // Led 
#define PinGreen   10 // Alert
#define PinYellow  11 // Alert
#define PinRed     12 // Alert
#define PinSensor  A0   // Temp sensor
#define servoPin   3    // Servo Motor

Communication communication;
Servo servoMotor;       
float temperature = 0;
bool cleanning = false; 

const unsigned long intervaloMovimento = 200;
unsigned long ultimoMovimento = 0;
bool movimentoEsquerda = true;
bool limpando = false; 
const int anguloEsq = 40;   // ajuste conforme posição esquerda
const int anguloDir = 180;  // ajuste conforme posição direita
const int anguloDescanso = 90;
const int delayMovimento = 200; // tempo entre os movimentos (ms)

void setup() {
  pinMode(PinLed, OUTPUT);
  pinMode(PinGreen, OUTPUT);
  pinMode(PinYellow, OUTPUT);
  pinMode(PinRed, OUTPUT);
  pinMode(servoPin,  OUTPUT);      
  servoMotor.attach(servoPin);
  servoMotor.write(anguloDescanso);  
  Serial.begin(9600);
  delay(5000);
}

void loop() {
  while(Serial.available() > 0){ 
    String command = Serial.readString();
    if (command == "TOGGLEPOWER") {
    	if(digitalRead(PinLed)){digitalWrite(PinLed, LOW); cleanning = false;}
		else{digitalWrite(PinLed, HIGH); cleanning = true;}   
    }
  }
  every();
  delay(500);  
}

void sendPercepts(){

  communication.startBelief("temperature");
  communication.beliefAdd(temperature);
  communication.endBelief();
  
  communication.startBelief("powerStatus");
  if(digitalRead(PinLed)){communication.beliefAdd("On");}
  else{communication.beliefAdd("Off");}
  communication.endBelief();

  if(temperature > 100){  
  	communication.startBelief("status");
  	communication.beliefAdd("burnt");
  	communication.endBelief();
  }

  communication.sendMessage();  
}

void every(){
    /* every cycle */
  temperature = 20.0 + (analogRead(PinSensor) / 1023.0) * 100.0;
  
   if(temperature > 100 & !digitalRead(PinRed)){digitalWrite(PinGreen,LOW); digitalWrite(PinYellow,LOW); digitalWrite(PinRed,HIGH);}
   else if(temperature > 70 & !digitalRead(PinYellow)){digitalWrite(PinGreen,LOW); digitalWrite(PinYellow,HIGH); digitalWrite(PinRed,LOW);}
   else if(temperature <= 70 & !digitalRead(PinGreen)){digitalWrite(PinGreen,HIGH); digitalWrite(PinYellow,LOW); digitalWrite(PinRed,LOW);}
  
  
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

  sendPercepts();
}
