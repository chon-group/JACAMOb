#include <Servo.h>

// ---------- Pinos ----------
#define PIN_CMD_IN     5   // Entrada: comando PWM do Arduino externo (tipo servo)
#define PIN_CMD_OUT    6   // Saída: replica o comando recebido (pass-through)

#define PIN_SENSE_IN   3   // Entrada: sense PWM (depois do botão/corte)
#define PIN_FB_OUT     11   // Saída: "analógico fake" (PWM->RC->A0 do Arduino externo)

// ---------- Parâmetros ----------
#define CMD_TIMEOUT_US      30000UL   // timeout do pulseIn no comando (30ms)
#define SENSE_TIMEOUT_MS    60UL      // se não chega pulso sense recente, falha
//#define SENSE_TIMEOUT_MS 120UL
#define FB_INVALID_PWM      0         // valor PWM do feedback quando sense falha (0V após RC)

// >>> Ajuste aqui (faixa real típica do Servo.h: ~544..2400us)
#define SERVO_MIN_US        500
#define SERVO_MAX_US        2500

//#define SERVO_MIN_US 544
//#define SERVO_MAX_US 2400



// ---------- Servo pass-through ----------
Servo servoOut; // vai gerar pulsos de servo no PIN_CMD_OUT

// ---------- Medição do SENSE (no PIN 3 via interrupção CHANGE) ----------
volatile unsigned long senseRiseUs = 0;
volatile unsigned int  senseWidthUs = 0;     // largura HIGH medida (us)
volatile unsigned long senseLastPulseMs = 0; // timestamp do último pulso válido
volatile bool senseHigh = false;

void isrSense() {
  bool level = digitalRead(PIN_SENSE_IN);
  unsigned long nowUs = micros();

  if (level) { // RISING
    senseRiseUs = nowUs;
    senseHigh = true;
  } else {     // FALLING
    if (senseHigh) {
      unsigned long w = nowUs - senseRiseUs;
      if (w <= 3000) {                 // sanidade
        senseWidthUs = (unsigned int)w;
        senseLastPulseMs = millis();
      }
      senseHigh = false;
    }
  }
}

// ---------- Conversões ----------
int usToDeg(int us) {
  if (us < 500) return -1;     // inválido (sem pulso / ruído)
  if (us < 544)  us = 544;
  if (us > 2400) us = 2400;

  long deg = (long)(us - 544) * 180L / (2400L - 544L);
  return (int)deg;             // 0..180
}


bool senseOk(unsigned int us, unsigned long lastMs) {
  if (millis() - lastMs > SENSE_TIMEOUT_MS) return false;
  // >>> Ajuste aqui (antes era 700..2300, o que mata 0° e 180°)
  return (us >= SERVO_MIN_US && us <= SERVO_MAX_US);
}

// Lê o ângulo "executado" a partir do sense (ou -1 se falhou)
int readExecDegFromSense() {
  unsigned int us;
  unsigned long t;
  noInterrupts();
  us = senseWidthUs;
  t  = senseLastPulseMs;
  interrupts();

  if (!senseOk(us, t)) return -1;
  return usToDeg((int)us);
}

// Escreve feedback no PIN_FB_OUT como PWM (0..255) para RC->A0
void writeFeedbackDeg(int deg) {
  if (deg < 0) {
    analogWrite(PIN_FB_OUT, FB_INVALID_PWM);
    return;
  }
  if (deg > 180) deg = 180;
  int pwm = map(deg, 0, 180, 32, 224);   /* diminui o expectro, pois o simulIDE às vezes envia 1024 quando deveria enviar 0 */
  analogWrite(PIN_FB_OUT, pwm);
}

void setup() {
 // Serial.begin(9600);

  pinMode(PIN_CMD_IN, INPUT);
  pinMode(PIN_SENSE_IN, INPUT);

  pinMode(PIN_FB_OUT, OUTPUT);
  analogWrite(PIN_FB_OUT, 0);

  // Pass-through: gera sinal servo no pino 6
  servoOut.attach(PIN_CMD_OUT);
  servoOut.writeMicroseconds(1500); // neutro inicial

  // Sense no pino 3 (INT1): mede largura de pulso com CHANGE
  attachInterrupt(digitalPinToInterrupt(PIN_SENSE_IN), isrSense, CHANGE);

 // Serial.println("Servo-proxy com feedback iniciado.");
 // Serial.println("CMD_IN=D5, CMD_OUT=D6, SENSE_IN=D3, FB_OUT=D9 (PWM->RC->A0 externo)");
}

void loop() {
  // 1) Lê comando PWM no D5 (bloqueia até um pulso ou timeout)
  unsigned long cmdUs = pulseIn(PIN_CMD_IN, HIGH, CMD_TIMEOUT_US);

  // 2) Replica no D6 (pass-through)
  // >>> Ajuste aqui (antes era 700..2300)
  if (cmdUs >= SERVO_MIN_US && cmdUs <= SERVO_MAX_US) {
    servoOut.writeMicroseconds((int)cmdUs);
  }

  // 3) Lê "execução" pelo SENSE (D3), converte em grau
  int execDeg = readExecDegFromSense();

  // 4) Exporta como "feedback analógico fake" no D9 (PWM -> RC -> A0 externo)
  writeFeedbackDeg(execDeg);

  // Debug leve
/*  static unsigned long lastLog = 0;
  if (millis() - lastLog > 200) {
    lastLog = millis();

 //   Serial.print("cmdUs=");
//    Serial.print((int)cmdUs);

  //  Serial.print(" | execDeg=");
  //  Serial.print(execDeg);

/*    unsigned int sUs;
    unsigned long sMs;
    noInterrupts();
    sUs = senseWidthUs;
    sMs = senseLastPulseMs;
    interrupts();

 //   Serial.print(" | senseUs=");
 //   Serial.print(sUs);

  //  Serial.print(" | ageMs=");
   // Serial.println(millis() - sMs);
  }
*/
}
