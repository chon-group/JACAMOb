// Agent bob in project minimumJaCaMoProject

/* Initial beliefs and rules */

/* Initial goals */

!start.
!attach1.
//!attach2.

/* Plans */

+!start : true
    <- .print("hello world.");
       .date(Y,M,D); .time(H,Min,Sec,MilSec); // get current date & time
       +started(Y,M,D,H,Min,Sec).            // add a new belief

+!attach1 <-
    .myBody.neckAttach("/dev/ttyUSB0");
        //.myBody.act(turnOnLamp);
        //.myBody.act(changeLED(13,true));
.


+!attach2 <-
     .myBody.neckAttach("/dev/ttyUSB1");
.

+myBody::lampStatus(VALUE)[source(TYPE,APPARATUS)]
: VALUE = enable
<-
    .wait(1000);
    .print("desligando ",APPARATUS);
    .myBody.act(changeLED(13,false),APPARATUS);
.

+myBody::lampStatus(VALUE)[source(TYPE,APPARATUS)]
: VALUE = disable
<-
    .wait(1000);
    .print("ligando ",APPARATUS);
    .myBody.act(turnOnLamp);
.

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