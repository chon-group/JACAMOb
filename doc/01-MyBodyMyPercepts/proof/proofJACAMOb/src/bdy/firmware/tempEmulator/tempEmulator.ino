const int dusterPin = 2;
const int ledPin = 13;
const int tempOutPin = 11;

int heat = 0;

void setup() {
    pinMode(dusterPin, INPUT);
    pinMode(ledPin, OUTPUT);
    pinMode(tempOutPin, OUTPUT);
}

void loop() {

    if (digitalRead(dusterPin) == HIGH) {

        // Aquecendo
        if (heat < 100) {
            heat++;
		    delay(200);
        }

    } else {

        // Esfriando
        if (heat > 0) {
            heat--;
		    delay(350);
        }
    }

    // LED acende quando atingir o limite
    if (heat >= 100) {
        digitalWrite(ledPin, HIGH);
    } else {
        digitalWrite(ledPin, LOW);
    }

    // Gera a saída para o sensor
    int output = map(heat, 0, 100, 0, 255);
    analogWrite(tempOutPin, output);

    delay(100);
}