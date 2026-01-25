// Agent bob in project minimumJaCaMoProject

/* Initial beliefs and rules */

/* Initial goals */

!start.

/* Plans */

+!start : true
    <- .print("hello world.");
       .date(Y,M,D); .time(H,Min,Sec,MilSec); // get current date & time
       +started(Y,M,D,H,Min,Sec).            // add a new belief

+myBody::port(P,S) <- .print(P," ",S).


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
