!attach1.
//!andar1.

+!attach1 <-
   .myBody.neckAttach("/dev/ttyUSB0");
   // .myBody.neckAttach("/dev/ttyUSB1");
    //.myBody.neckAttach(lampBoy);
    //.myBody.neckAttach("/dev/ttyUSB0");
    //!andar1[source(lampBoy)]
  //  .myBody.neckAttach(ap1,"/dev/ttyEmulatedPort0",Reply3);
.

+myBody::blink(false)[source(interoception,noName)] <-
   // .wait(500);
    .myBody.act(blinkOn,Reply);
    .print("ON ------> ",Reply).

+myBody::blink(true)[source(interoception,noName)] <-
    .myBody.act(blinkOff,Reply);
    .print("OFF -----> ",Reply).


+myBody::lampStatus(VALUE)[source(TYPE,APPARATUS)]
: VALUE = enable
<-
    //.print("detaching ",APPARATUS);
    .wait(1000);
    .myBody.act(changeLED(13,false),APPARATUS);
   //.myBody.neckDetach(APPARATUS);
.

+myBody::lampStatus(VALUE)[source(TYPE,APPARATUS)]
: VALUE = disable
<-
    .wait(1000);
   // .print("ligando ",APPARATUS);
    .myBody.act(turnOnLamp,APPARATUS);
.

+myBody::led(STATUS)[source(TYPE,APPARATUS)] <- .wait(250); .myBody.act(toggleLED).

/*+myBody::lampStatus(V)[SOURCE]
: SOURCE=source(T,S) & T=proprioception
<- .print("IN lampStatus ->",V,"  ",T).
-myBody::lampStatus(V)[SOURCE]
: SOURCE=source(T,S) & T=proprioception
<- .print("OUT lampStatus ->",V,"  ",T).
*/
//+myBody::light(V) <- .print("Light ",V).

//+body::outraCoisa(S) <-  .print(S).

{ include("/home/nilson/chonGroup/JACAMOb/src/main/resources/templates/common-cartago.asl")}
{ include("/home/nilson/chonGroup/JACAMOb/src/main/resources/templates/common-moise.asl")}
//{ include("$jacamo/templates/common-cartago.asl") }
//{ include("$jacamo/templates/common-moise.asl") }

// uncomment the include below to have an agent compliant with its organisation
//{ include("$moise/asl/org-obedient.asl") }


+myBody::port(off,PORT,_,_)[source(interoception,APPARATUS)] <-
    .myBody.neckDetach(APPARATUS);
    !!tryAttach(APPARATUS,PORT);
.

+!tryAttach(APPARATUS,PORT) <-
     .random(R);
     .wait(15000*R);
     .myBody.neckAttach(APPARATUS,PORT);
.

-!tryAttach(APPARATUS,PORT) <- !!tryAttach(APPARATUS,PORT); .


+!meuDesejo(T) <- .print(T).
