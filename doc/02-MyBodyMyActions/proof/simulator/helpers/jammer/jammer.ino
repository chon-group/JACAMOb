#include <LiquidCrystal.h>

// ======= PINAGEM =======
#define PinLCDCtrl01 5
#define PinLCDCtrl02 4
#define PinLCDCtrl03 3
#define PinLCDCtrl04 2
#define PinLCDRS     11
#define PinLCDEN     10
#define PinLCDLight  12
#define PinRELAY      8
#define PinJammerControl 7
#define PinStepCounter   6

// ======= CONFIGURAÇÃO =======
#define FREE_MS  5555
#define STUCK_MS 1444

// ======= LCD =======
LiquidCrystal lcd(PinLCDRS, PinLCDEN,
                  PinLCDCtrl01, PinLCDCtrl02,
                  PinLCDCtrl03, PinLCDCtrl04);

// ======= CONTROLE =======
bool stuck = false;
unsigned long lastCycle = 0;
unsigned long stepCount = 0;
bool lastStepSignal = LOW;

void setup() {
  pinMode(PinLCDLight, OUTPUT);
  pinMode(PinRELAY, OUTPUT);
  pinMode(PinJammerControl, INPUT);
  pinMode(PinStepCounter, INPUT);

  digitalWrite(PinLCDLight, HIGH);
  digitalWrite(PinRELAY, LOW);

  lcd.begin(16, 2);

  randomSeed(analogRead(A0));

  lastStepSignal = digitalRead(PinStepCounter);
  lastCycle = millis();
  updateDisplay();
}

void loop() {
  // ======= CONTADOR DE PASSOS =======
  bool stepSignal = digitalRead(PinStepCounter);

  // Conta somente na borda de subida
  if (stepSignal == HIGH && lastStepSignal == LOW) {
    stepCount++;
    updateDisplay();
  }

  lastStepSignal = stepSignal;

  // ======= CONTROLE DO JAMMER =======
  // Jammer bloqueado: mantém o estado atual
  if (digitalRead(PinJammerControl) == LOW) {
    return;
  }

  unsigned long now = millis();

  if (!stuck && (now - lastCycle >= FREE_MS)) {
    stuck = true;
    lastCycle = now;

    digitalWrite(PinRELAY, HIGH);
    updateDisplay();

  } else if (stuck && (now - lastCycle >= STUCK_MS)) {
    stuck = false;
    lastCycle = now;

    digitalWrite(PinRELAY, LOW);
    updateDisplay();
  }
}

void updateDisplay() {
  lcd.clear();

  lcd.setCursor(0, 0);
  if (stuck) {
    lcd.print("State: STUCK");
  } else {
    lcd.print("State: FREE");
  }

  lcd.setCursor(0, 1);
  lcd.print("Steps: ");
  lcd.print(stepCount);
}