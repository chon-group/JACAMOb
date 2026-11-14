#include <NECK.hpp>
#include <DHT.h>

// Multi-purpose Shield V1
#define pinDHT 4
#define BlueLED 13
#define RedLED 12

DHT dht(pinDHT, DHT11);

bool cleaning = false;

float temperature = 0.0;
bool dhtValid = false;

unsigned long lastDHTRead = 0;
const unsigned long DHT_INTERVAL = 2000;

/* --------------------------------------------------------------------------
   Apparatus / Elements
   -------------------------------------------------------------------------- */

Apparatus(duster) {
    Element(broom);
    Element(temperatureSensor);
}

/* --------------------------------------------------------------------------
   Preparation
   -------------------------------------------------------------------------- */

Preparation {
    pinMode(BlueLED, OUTPUT);
    pinMode(RedLED, OUTPUT);

    digitalWrite(BlueLED, LOW);
    digitalWrite(RedLED, LOW);

    dht.begin();
}

/* --------------------------------------------------------------------------
   Sensing / Behaving
   -------------------------------------------------------------------------- */

Sensing(temperatureSensor) {
    unsigned long now = millis();

    if (now - lastDHTRead < DHT_INTERVAL) return;

    lastDHTRead = now;

    float temp = dht.readTemperature();
    float humi = dht.readHumidity();

    if (isnan(temp) || isnan(humi)) return;

    temperature = temp;
    dhtValid = true;
}

/* --------------------------------------------------------------------------
   Percepts
   -------------------------------------------------------------------------- */

Percept(broom, powerStatus, PROPRIOCEPTION) {return cleaning ? "on" : "off";}

Percept(temperatureSensor, temperature, INTEROCEPTION) {
    if (!dhtValid) return UNAVAILABLE;
    return temperature;
}

/* --------------------------------------------------------------------------
   Acts
   -------------------------------------------------------------------------- */

Act(broom, togglePower) {
    cleaning = !cleaning;

    digitalWrite(
        BlueLED,
        cleaning ? HIGH : LOW
    );

    return EXECUTED;
}

/* --------------------------------------------------------------------------
   Tacit Knowledge
   -------------------------------------------------------------------------- */

TacitKnowledge(sweep,
    "not clean",
    ".print(sweeping); .myBody.act(togglePower); .wait(1000); !sweep.");
