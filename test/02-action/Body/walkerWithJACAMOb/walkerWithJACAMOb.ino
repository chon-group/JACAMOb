#define LeftLeg    10
#define LeftFoot   9
#define FB_LEFT    A1   // feedback analógico do pé esquerdo (LF)

#define RightLeg   5
#define RightFoot  6
#define FB_RIGHT   A0   // feedback analógico do pé direito (RF)

#define STEP_MS     1000
#define GREEN_PIN   13
#define YELLOW_PIN  12
#define RED_PIN     11
#define ALARM_PIN   8
#define LOCK        7    /* Para fins do experimento, o estado só pode mudar ANTES da atuação e ANTES da percepção */

#define NINETY_DEGREE 90
#define HUNDRED_EIGHTY_DEGREE 180
#define TICK_MS     10

enum STATUS : uint8_t { OK, STUCK, FALL };
STATUS state = OK;

#include <Servo.h>     /* https://docs.arduino.cc/libraries/servo/ */
#include <NECK.hpp>   /*  https://github.com/chon-group/neck2arduino */
Apparatus(walker); 

Servo sLL, sLF, sRL, sRF;

void setup() {configPinMode(); attachServos();}


void loop() {
	//isStuck();
	walker.embody();
}


/* Apparatus Description */
Element(walker, legs);

Action(legs, keepWalking){
	//if(isStuck()) return REJECTED;
  walk();
  isStuck();
	if(getState() == "ok"){
    return EXECUTED;
  }else{
    return UNABLE;
  }
}

Perception(legs, legState, PROPRIOCEPTION){
  if (state == FALL){return "fall";}
  unlock();
  lock();
  if (isStuck()) return "stuck";
  else return "ok";
}

/* HELPERS */

bool moveTo(int tLL, int tLF, int tRL, int tRF, unsigned long durMs) {
  int aLL0 = sLL.read();
  int aLF0 = sLF.read();
  int aRL0 = sRL.read();
  int aRF0 = sRF.read();

  unsigned long t0 = millis();

  /* movement effect... */
  while (millis() - t0 < durMs) {
    float p = (float)(millis() - t0) / (float)durMs;

    int newLL = aLL0 + (int)((tLL - aLL0) * p);
    int newLF = aLF0 + (int)((tLF - aLF0) * p);
    int newRL = aRL0 + (int)((tRL - aRL0) * p);
    int newRF = aRF0 + (int)((tRF - aRF0) * p);

    sLL.write(newLL);
    sLF.write(newLF);
    sRL.write(newRL);
    sRF.write(newRF);

    delay(TICK_MS);
  }

  // garante alvo exato no final
  sLL.write(tLL);
  sLF.write(tLF);
  sRL.write(tRL);
  sRF.write(tRF);

  // enroscou o pé ????...
  if ((tLF - getFeedbackDegree(FB_LEFT) > 90) || (tRF - getFeedbackDegree(FB_RIGHT) > 90)) {
    digitalWrite(ALARM_PIN, LOW);
    digitalWrite(RED_PIN, LOW);
    digitalWrite(YELLOW_PIN, HIGH);
    digitalWrite(GREEN_PIN, LOW);
    return false;
  }

  return true; // movimento executado com sucesso...
}

bool oneStep() {  
  unlock();
  lock(); 
  const unsigned long phase = STEP_MS / 5;
  int lift = 25, swing = 15, tilt = 10;

  if (!moveTo(NINETY_DEGREE,         NINETY_DEGREE + tilt, NINETY_DEGREE,                 NINETY_DEGREE - tilt, phase)) return false;
  if (!moveTo(NINETY_DEGREE,         NINETY_DEGREE + tilt, NINETY_DEGREE - lift,          NINETY_DEGREE - tilt, phase)) return false;
  if (!moveTo(NINETY_DEGREE,         NINETY_DEGREE + tilt, (NINETY_DEGREE - lift) + swing, NINETY_DEGREE - tilt, phase)) return false;
  if (!moveTo(NINETY_DEGREE,         NINETY_DEGREE - tilt, NINETY_DEGREE,                 NINETY_DEGREE + tilt, phase)) return false;
  if (!moveTo(NINETY_DEGREE - lift,  NINETY_DEGREE - tilt, NINETY_DEGREE,                 NINETY_DEGREE + tilt, phase)) return false; 
  if (!moveTo(NINETY_DEGREE, NINETY_DEGREE, NINETY_DEGREE, NINETY_DEGREE, 100)) return false;

  return !isStuck();
//  return true;
}

bool isStuck() {
  if (state == FALL){return true;}
  delay(50);
  if ((getFeedbackDegree(FB_LEFT) == -1) || (getFeedbackDegree(FB_RIGHT) == -1)) {
    setState(STUCK);
    return true;
  } else {
    setState(OK);
    return false;
  }
}

bool walk() {
  if (isStuck()) {fall(); return false;}
  return oneStep();
}

void attachServos() {
  sLL.attach(LeftLeg);
  sLF.attach(LeftFoot);
  sRL.attach(RightLeg);
  sRF.attach(RightFoot);

  delay(500);
  standUp();
  lock();
  isStuck();
}

void configPinMode() {
  pinMode(ALARM_PIN, OUTPUT);
  pinMode(RED_PIN, OUTPUT);
  pinMode(YELLOW_PIN, OUTPUT);
  pinMode(GREEN_PIN, OUTPUT);
  pinMode(FB_LEFT, INPUT);
  pinMode(FB_RIGHT, INPUT);
  pinMode(LOCK, OUTPUT);
}

void fall() {
  setState(FALL);
  sLL.write(HUNDRED_EIGHTY_DEGREE);
  sLF.write(HUNDRED_EIGHTY_DEGREE);
  sRL.write(HUNDRED_EIGHTY_DEGREE);
  sRF.write(HUNDRED_EIGHTY_DEGREE);
}

void standUp() {
  setState(OK);
  sLL.write(NINETY_DEGREE);
  sLF.write(NINETY_DEGREE);
  sRL.write(NINETY_DEGREE);
  sRF.write(NINETY_DEGREE);
}

int getFeedbackDegree(int feedbackPin) {
  int raw = analogRead(feedbackPin);

  if ((raw < 64) || (raw > 960)) return -1; /* falha no feedback = intervenção no experimento */

  int degree = map(raw, 128, 896, 0, 180);
  return degree;
}

void setState(STATUS newState) {
  state = newState;

  if (state == OK) {
    digitalWrite(ALARM_PIN, LOW);
    digitalWrite(RED_PIN, LOW);
    digitalWrite(YELLOW_PIN, LOW);
    digitalWrite(GREEN_PIN, HIGH);
  } else if (state == STUCK) {
    digitalWrite(ALARM_PIN, LOW);
    digitalWrite(RED_PIN, LOW);
    digitalWrite(YELLOW_PIN, HIGH);
    digitalWrite(GREEN_PIN, LOW);
  } else if (state == FALL) {
    digitalWrite(ALARM_PIN, HIGH);
    digitalWrite(RED_PIN, HIGH);
    digitalWrite(YELLOW_PIN, LOW);
    digitalWrite(GREEN_PIN, LOW);
  }
}


void lock(){digitalWrite(LOCK, HIGH); delay(50);}
void unlock(){digitalWrite(LOCK, LOW); delay(100);}

String getState() {
  switch (state) {
    case OK:
      return "ok";
    case STUCK:
      return "stuck";
    case FALL:
      return "fall";
    default:
      return "unknown";
  }
}

