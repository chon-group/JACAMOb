#include <Javino.h>   /* https://github.com/chon-group/javino2arduino */
#include <Servo.h>    /* https://docs.arduino.cc/libraries/servo/ */
#define PinLed 	   13	// Led 
#define PinGreen   10	// Alert
#define PinYellow  11	// Alert
#define PinRed     12	// Alert
#define PinSensor  A0   // Temp sensor
#define servoPin   3		// Servo Motor

Javino javino;
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

void serialEvent(){javino.readSerial();}

void setup() {
  pinMode(PinLed, OUTPUT);
  pinMode(PinGreen, OUTPUT);
  pinMode(PinYellow, OUTPUT);
  pinMode(PinRed, OUTPUT);
  pinMode(servoPin,  OUTPUT);      
  servoMotor.attach(servoPin);
  servoMotor.write(anguloDescanso);  
  javino.act["togglePower"]  = togglePower;
  javino.perceive(getExogenousPerceptions);
  javino.start(9600);
}

void togglePower(){
	if(digitalRead(PinLed)){
		digitalWrite(PinLed, LOW);    
		cleanning = false;
		servoMotor.write(anguloDescanso);
	}else{
		digitalWrite(PinLed, HIGH);
		cleanning = true;
		servoMotor.write(anguloEsq);
	}		
}

void getExogenousPerceptions(){ 
	if(temperature > 100){javino.addPercept("status(\"burnt\")");}
	
    javino.addPercept("temperature("+String(temperature)+")");

	if(digitalRead(PinLed)){javino.addPercept("powerStatus(\"On\")");}
	else{javino.addPercept("powerStatus(\"Off\")");}
}

void loop() {
	javino.run();

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
}