#include <LiquidCrystal.h>

// ======= PINAGEM =======
#define PinLCDCtrl01 5
#define PinLCDCtrl02 4
#define PinLCDCtrl03 3
#define PinLCDCtrl04 2
#define PinLCDRS     11
#define PinLCDEN     10
#define PinLCDLight  12
#define PinFAILURE    7
#define PinRELAY      8
#define agentCOMM     0
#define PinSYNC_IN    6

// ======= LCD =======
LiquidCrystal lcd(PinLCDRS, PinLCDEN,
                  PinLCDCtrl01, PinLCDCtrl02,
                  PinLCDCtrl03, PinLCDCtrl04);

// ======= CONTROLE =======
bool running = false;
bool failed = false;
unsigned long lastCycle = 0;
unsigned long relayStart = 0;

int jumpCount = 0;

enum State {IDLE, WAITING, JUMPING, FAILED};
State currentState = IDLE;

void setup() {
  pinMode(PinLCDLight, OUTPUT);
  pinMode(PinRELAY, OUTPUT);
  pinMode(PinFAILURE, INPUT);
  pinMode(agentCOMM, INPUT);
  pinMode(PinSYNC_IN, INPUT);

  digitalWrite(PinRELAY, LOW);
  digitalWrite(PinLCDLight, HIGH);

  lcd.begin(16, 2);
  lcd.print("Ready");

  updateDisplay();
}

void loop() {

  // ======= CHECA FALHA =======
  if (digitalRead(PinFAILURE) == HIGH) {
    currentState = FAILED;
    running = false;
	failed = true;
    digitalWrite(PinRELAY, LOW);
    updateDisplay();
  }


  if(!running && !failed){
	 if ((analogRead(A0) < 900) || ((digitalRead(agentCOMM) == LOW))){  // botão pressionado ou iniciou comunicação
	    running = true;
	    jumpCount = 0;
	    currentState = WAITING;
		randomSeed(millis());
	    lastCycle = millis();
	    lcd.clear();
	    updateDisplay();
	 }
  }else{
    unsigned long now = millis();

    switch (currentState) {
      case WAITING:
        if ((now - lastCycle >= 5000) && digitalRead(PinSYNC_IN) == LOW) {
			if(change()){
			  digitalWrite(PinRELAY, HIGH);  // abre relé
	          relayStart = now;
	          currentState = JUMPING;
			  jumpCount++;
			  updateDisplay();
			}
        }
        break;
      case JUMPING:
        if (now - relayStart >= 5000) {
		  if(change()){
			  digitalWrite(PinRELAY, LOW);   // fecha relé
	          lastCycle = now;
	          currentState = WAITING;
			  updateDisplay();		
		  }
        }
        break;
      default:
        break;
    }
  }
}

void updateDisplay() {

  lcd.setCursor(0, 0);

  switch (currentState) {
    case IDLE:
      lcd.print("State: IDLE...");
      break;
    case WAITING:
      lcd.print("State: FREE  ");
      break;
    case JUMPING:
      lcd.print("State: STUCK   ");
      break;
    case FAILED:
      lcd.print("State: FAIL   ");
      break;
  }

  lcd.setCursor(0, 1);
  lcd.print("Hindrances: ");
  lcd.print(jumpCount);
  lcd.print("    ");
}

bool change() {
  long n = random(0, 100);   // sorteia de 0 a 99
  if (n % 2 == 0) {
    return true;   // par
  } else {
    return false;  // ímpar
  }
}
