#define OK    0
#define STUCK 1
#define FALL  2

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

#define NINETY_DEGREE 90
#define HUNDRED_EIGHTY_DEGREE 180
#define TICK_MS     10

#define JAMMER_CONTROL_PIN 7

#include <Servo.h>     /* https://docs.arduino.cc/libraries/servo/ */
Servo sLL, sLF, sRL, sRF;
uint8_t state = OK;

/* Function prototypes */
bool moveTo(int tLL, int tLF, int tRL, int tRF, unsigned long durMs);
bool oneStep();
bool isStuck();
bool verifyStuck(bool unlock);
bool walk();

void attachServos();
void configPinMode();
void fall();
void standUp();
void unlockJammer();
void lockJammer();

int getFeedbackDegree(int feedbackPin);
void setState(uint8_t newState);


bool moveTo(int tLL, int tLF, int tRL, int tRF, unsigned long durMs) {
  int aLL0 = sLL.read(); int aLF0 = sLF.read(); int aRL0 = sRL.read(); int aRF0 = sRF.read();
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
    return false;
  }

  return true; // movimento executado com sucesso...
}

bool oneStep() {  
  unlockJammer();
  const unsigned long phase = STEP_MS / 5;
  int lift = 25, swing = 15, tilt = 10;

  if (!moveTo(NINETY_DEGREE,         NINETY_DEGREE + tilt, NINETY_DEGREE,                 NINETY_DEGREE - tilt, phase)) return false;
  if (!moveTo(NINETY_DEGREE,         NINETY_DEGREE + tilt, NINETY_DEGREE - lift,          NINETY_DEGREE - tilt, phase)) return false;
  if (!moveTo(NINETY_DEGREE,         NINETY_DEGREE + tilt, (NINETY_DEGREE - lift) + swing, NINETY_DEGREE - tilt, phase)) return false;
  if (!moveTo(NINETY_DEGREE,         NINETY_DEGREE - tilt, NINETY_DEGREE,                 NINETY_DEGREE + tilt, phase)) return false;
  if (!moveTo(NINETY_DEGREE - lift,  NINETY_DEGREE - tilt, NINETY_DEGREE,                 NINETY_DEGREE + tilt, phase)) return false; 
  if (!moveTo(NINETY_DEGREE, NINETY_DEGREE, NINETY_DEGREE, NINETY_DEGREE, 100)) return false;

  return !verifyStuck(false);

}

bool isStuck() {
  return verifyStuck(true);
}

bool verifyStuck(bool unlock) {
  if (state == FALL) return true;

  if(unlock){
    unlockJammer();
    delay(500);    
  }
  lockJammer();

  if (getFeedbackDegree(FB_LEFT) == -1 ||
      getFeedbackDegree(FB_RIGHT) == -1) {
    setState(STUCK);
    return true;
  }
  setState(OK);
  return false;
}


bool walk() {
  bool result = false;
  if (verifyStuck(false)) {
    fall();
  }else{
    result = oneStep();
  }
  return result;
}

void attachServos() {
  sLL.attach(LeftLeg);
  sLF.attach(LeftFoot);
  sRL.attach(RightLeg);
  sRF.attach(RightFoot);
  delay(500);
  standUp();
  isStuck();
}

void configPinMode() {
  pinMode(ALARM_PIN, OUTPUT);
  pinMode(RED_PIN, OUTPUT);
  pinMode(YELLOW_PIN, OUTPUT);
  pinMode(GREEN_PIN, OUTPUT);
  pinMode(FB_LEFT, INPUT);
  pinMode(FB_RIGHT, INPUT);
  pinMode(JAMMER_CONTROL_PIN, OUTPUT);
  unlockJammer();
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

void setState(uint8_t newState) {
  state = newState;

  digitalWrite(ALARM_PIN, state == FALL ? HIGH : LOW);
  digitalWrite(RED_PIN, state == FALL ? HIGH : LOW);
  digitalWrite(YELLOW_PIN, state == STUCK ? HIGH : LOW);
  digitalWrite(GREEN_PIN, state == OK ? HIGH : LOW);
}


void unlockJammer() {
    digitalWrite(JAMMER_CONTROL_PIN, HIGH);
}

void lockJammer() {
    digitalWrite(JAMMER_CONTROL_PIN, LOW);
}
