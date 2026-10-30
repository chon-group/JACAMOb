
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

// ======= CONFIGURAÇÃO =======
//#define INTERVAL_MS 5000
#define FREE_MS  7555
#define STUCK_MS 2444

// ======= LCD =======
LiquidCrystal lcd(PinLCDRS, PinLCDEN,
                  PinLCDCtrl01, PinLCDCtrl02,
                  PinLCDCtrl03, PinLCDCtrl04);

// ======= CONTROLE =======
bool stuck = false;
unsigned long lastCycle = 0;
int jumpCount = 0;

void setup() {
  pinMode(PinLCDLight, OUTPUT);
  pinMode(PinRELAY, OUTPUT);
  pinMode(PinJammerControl, INPUT);

  digitalWrite(PinLCDLight, HIGH);
  digitalWrite(PinRELAY, LOW);

  lcd.begin(16, 2);

  randomSeed(analogRead(A0));

  lastCycle = millis();
  updateDisplay();
}

// void loop(){

// }
void loop() {
  // Jammer bloqueado: mantém o estado atual
  if (digitalRead(PinJammerControl) == LOW) {
    return;
  }

  unsigned long now = millis();

  if (!stuck && (now - lastCycle >= FREE_MS)) {
    stuck = true;
    lastCycle = now;

    digitalWrite(PinRELAY, HIGH);
    jumpCount++;
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
  lcd.print("Hindrances: ");
  lcd.print(jumpCount);
}


// #include <LiquidCrystal.h>

// // ======= PINAGEM =======
// #define PinLCDCtrl01 5
// #define PinLCDCtrl02 4
// #define PinLCDCtrl03 3
// #define PinLCDCtrl04 2
// #define PinLCDRS     11
// #define PinLCDEN     10
// #define PinLCDLight  12
// #define PinRELAY      8

// // ======= CONFIGURAÇÃO =======
// #define INTERVAL_MS 5000
// #define FREE_MS  7500
// #define STUCK_MS 3200

// // ======= LCD =======
// LiquidCrystal lcd(PinLCDRS, PinLCDEN,
//                   PinLCDCtrl01, PinLCDCtrl02,
//                   PinLCDCtrl03, PinLCDCtrl04);

// // ======= CONTROLE =======
// bool stuck = false;
// unsigned long lastCycle = 0;
// int jumpCount = 0;

// void setup() {
//   pinMode(PinLCDLight, OUTPUT);
//   pinMode(PinRELAY, OUTPUT);

//   digitalWrite(PinLCDLight, HIGH);
//   digitalWrite(PinRELAY, LOW);

//   lcd.begin(16, 2);

//   randomSeed(analogRead(A0));

//   lastCycle = millis();
//   updateDisplay();

// }


// void loop() {
//   unsigned long now = millis();

//   if (!stuck && (now - lastCycle >= FREE_MS)) {
//     stuck = true;
//     lastCycle = now;

//     digitalWrite(PinRELAY, HIGH);
//     jumpCount++;
//     updateDisplay();

//   } else if (stuck && (now - lastCycle >= STUCK_MS)) {
//     stuck = false;
//     lastCycle = now;

//     digitalWrite(PinRELAY, LOW);
//     updateDisplay();
//   }
// }


// void updateDisplay() {
//   lcd.clear();

//   lcd.setCursor(0, 0);
//   if (stuck) {
//     lcd.print("State: STUCK");
//   } else {
//     lcd.print("State: FREE");
//   }

//   lcd.setCursor(0, 1);
//   lcd.print("Hindrances: ");
//   lcd.print(jumpCount);
// }
