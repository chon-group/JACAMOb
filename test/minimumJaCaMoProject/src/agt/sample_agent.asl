!attach1.

+!attach1 <-
    .myBody.neckAttach("/dev/ttyUSB0");
    .myBody.neckAttach(lampBoy);
    //.myBody.neckAttach(lampBoy);
    //.myBody.neckAttach("/dev/ttyUSB0");

  //  .myBody.neckAttach(ap1,"/dev/ttyEmulatedPort0",Reply3);
.


+myBody::lampStatus(VALUE)[source(TYPE,APPARATUS)]
: VALUE = enable
<-
   // .wait(1000);
    .print("desligando ",APPARATUS);
    .myBody.act(changeLED(13,false),APPARATUS);
.

+myBody::lampStatus(VALUE)[source(TYPE,APPARATUS)]
: VALUE = disable
<-
 //   .wait(1000);
    .print("ligando ",APPARATUS);
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

