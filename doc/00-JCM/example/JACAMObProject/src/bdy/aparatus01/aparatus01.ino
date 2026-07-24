#include <NECK.hpp>   /*  https://github.com/chon-group/neck2arduino */
Apparatus(myApparatus); 
Element(myApparatus,led2);
Element(myApparatus,led); 

boolean loadDesire = true;
boolean blinkStatus = false;
int ledPin = 13;
void setup() {pinMode(ledPin, OUTPUT); pinMode(12, OUTPUT);}

void loop() {
  myApparatus.embody();

  if(blinkStatus){
    digitalWrite(ledPin,!digitalRead(ledPin));
    delay(250);
  }
}  


Action (led2,blinkOperation){
  if(!ActionArgs.isBool(0)) return INVALID; 
  if(blinkStatus == ActionArgs.asBool(0)) return ALREADY;

  if (loadDesire){
  NECKArgs out;
  out.add("desirEEEEEEEEEEEEE");
  out.add(0.877665);
  led2.trieb("dentro_da_blinkOperation",out,0.99);  
  }
  blinkStatus = ActionArgs.asBool(0);
  return EXECUTED;  
}

Perception (led2,ledStatus, PROPRIOCEPTION){
  if (loadDesire){
    led2.trieb("esteEh1Desejo",0.99);  
    loadDesire = false;
  }


  if(digitalRead(ledPin)) return true;
  else return false;
}

TacitKnowledge(myApparatus, nomequalquer1, "gasdfdsafsadfsadfsadfsdafdsafdsafasd");
TacitKnowledge(myApparatus, segundoNome, "contextoXXXX", "qwerwqerewqrwqerwqerwerweqrwe");

/* helpers */
bool algumaFuncao(){return true;}
bool funcaRetornaTrueouFalse(){return false;}
bool testeQualquer(){return false;}
