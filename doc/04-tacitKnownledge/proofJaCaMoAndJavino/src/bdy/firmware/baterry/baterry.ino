#include <Javino.h>
#include <LiquidCrystal.h>

// Choose exactly one pin configuration.
#define SIMULIDE

#ifdef SIMULIDE
  // SimulIDE LCD Keypad Shield
  #define PinLCDCtrl01 5
  #define PinLCDCtrl02 4
  #define PinLCDCtrl03 3
  #define PinLCDCtrl04 2
  #define PinLCDRS     11
  #define PinLCDEN     10
  #define PinLCDLight  12
  #define LedRed       7
  #define LedYellow    8
  #define LedGreen     9
#else
  // Physical Arduino with LCD Keypad Shield
  #define PinLCDCtrl01 4
  #define PinLCDCtrl02 5
  #define PinLCDCtrl03 6
  #define PinLCDCtrl04 7
  #define PinLCDRS     8
  #define PinLCDEN     9
  #define PinLCDLight  10
  #define LedRed       11
  #define LedYellow    12
  #define LedGreen     13
#endif

Javino javino;
LiquidCrystal lcd(PinLCDRS, PinLCDEN, PinLCDCtrl01, PinLCDCtrl02, PinLCDCtrl03, PinLCDCtrl04);

int batteryLevel = 100;
unsigned long lastClick = 0;
const unsigned long DEBOUNCE_MS = 250;
String lastButton = "none";

void serialEvent() {
  javino.readSerial();
}

String getBtnPressed() {
  int input = analogRead(A0);
  if (input < 50)  return "right";
  if (input < 150) return "up";
  if (input < 350) return "down";
  if (input < 550) return "left";
  if (input < 750) return "select";
  return "none";
}

void updateLeds() {
  digitalWrite(LedGreen,  batteryLevel > 15 ? HIGH : LOW);
  digitalWrite(LedYellow, batteryLevel > 5 && batteryLevel <= 15 ? HIGH : LOW);
  digitalWrite(LedRed,    batteryLevel <= 5 ? HIGH : LOW);
}

void updateLCD() {
  lcd.clear();
  lcd.setCursor(0, 0);
  lcd.print("Rosie Battery");
  lcd.setCursor(0, 1);
  lcd.print(batteryLevel);
  lcd.print("% ");
  if (batteryLevel <= 5) lcd.print("CRITICAL");
  else if (batteryLevel <= 15) lcd.print("LOW");
  else lcd.print("NORMAL");
  updateLeds();
}

void readLCDKeyboard() {
  String button = getBtnPressed();
  unsigned long now = millis();
  // One action per press; release before the next action.
  if (button != "none" && lastButton == "none" && now - lastClick >= DEBOUNCE_MS) {
    lastClick = now;
    if (button == "right")       batteryLevel += 1;
    else if (button == "left")   batteryLevel -= 1;
    else if (button == "up")     batteryLevel += 5;
    else if (button == "down")   batteryLevel -= 5;
    else if (button == "select") batteryLevel = 100;
    batteryLevel = constrain(batteryLevel, 0, 100);
    updateLCD();
  }
  lastButton = button;
}

void setup() {
  pinMode(LedRed, OUTPUT);
  pinMode(LedYellow, OUTPUT);
  pinMode(LedGreen, OUTPUT);
  pinMode(PinLCDLight, OUTPUT);
  digitalWrite(PinLCDLight, HIGH);
  lcd.begin(16, 2);
  updateLCD();
  javino.start(9600);
}

void loop() {
  readLCDKeyboard();
  if (javino.availableMsg()) {
    String msg = javino.getMsg();
    if (msg == "BATTERYINFO") {
      javino.sendMsg(String(batteryLevel));
    }
  }
}