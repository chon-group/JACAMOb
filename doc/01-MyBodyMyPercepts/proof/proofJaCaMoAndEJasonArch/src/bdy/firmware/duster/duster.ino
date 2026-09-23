#include <Servo.h>    /* https://docs.arduino.cc/libraries/servo/ */
const byte Led = 13, Green = 10, Yellow = 11, Red = 12, TempSensor = A0, servoPin = 3; //pinout
Servo servoMotor; String msg = ""; float temp = 0; bool cleanning = false; 
bool movimentoEsquerda = true; const int anguloEsq = 20; const int anguloDir = 220; const int anguloDescanso = 90;
const unsigned long intervaloMovimento = 200; unsigned long ultimoMovimento = 0;

void setup() {
 pinMode(Led,OUTPUT); pinMode(Green,OUTPUT); pinMode(Yellow,OUTPUT); pinMode(Red,OUTPUT); pinMode(servoPin,OUTPUT);
 servoMotor.attach(servoPin); servoMotor.write(anguloDescanso); Serial.begin(9600);
}

void loop() {
 readSerialPort(); 
 
 temp = 20.0 + (analogRead(TempSensor) / 1023.0) * 100.0;
 if(temp > 100 && !digitalRead(Red)){digitalWrite(Green, LOW); digitalWrite(Yellow, LOW);digitalWrite(Red, HIGH);}
 else if(temp > 70 && !digitalRead(Yellow)){digitalWrite(Green,LOW); digitalWrite(Yellow,HIGH); digitalWrite(Red,LOW);}
 else if(temp <= 70 && !digitalRead(Green)){digitalWrite(Green,HIGH); digitalWrite(Yellow,LOW); digitalWrite(Red,LOW);}

 if (cleanning && temp < 110) {
  unsigned long agora = millis();
  if (agora - ultimoMovimento >= intervaloMovimento) {
   ultimoMovimento = agora;
   if (movimentoEsquerda) {servoMotor.write(anguloEsq);} 
   else {servoMotor.write(anguloDir);}
   movimentoEsquerda = !movimentoEsquerda;
  }
 }
}

void processmsg(String cmd) {
  cmd.trim();
  if (cmd == "getPercepts") {getPercepts();} 
  else if (cmd == "togglePower") {togglePower();}
}

void togglePower() {
 if (digitalRead(Led)) {digitalWrite(Led, LOW); cleanning = false; servoMotor.write(anguloDescanso);}
 else {digitalWrite(Led, HIGH); cleanning = true; servoMotor.write(anguloEsq);}
}

void getPercepts() {
  if (temp > 100) {Serial.print("status(\"burnt\");");}
  else{Serial.print("temperature(");Serial.print(temp);Serial.print(");");}
  
  if (digitalRead(Led)) {Serial.println("powerStatus(\"On\")");} 
  else {Serial.println("powerStatus(\"Off\")");}
}

void readSerialPort() {
  while (Serial.available()) {
    char c = Serial.read();
    if (c == '\n') {processmsg(msg); msg = "";} 
    else if (c != '\r') {msg += c;}
  }
}
