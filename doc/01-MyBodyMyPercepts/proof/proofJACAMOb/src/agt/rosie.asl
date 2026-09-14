// Agent rosie in project proofJACAMOb
{ include("$jacamo/templates/common-cartago.asl") }
{ include("$jacamo/templates/common-moise.asl") }
{ include("$moise/asl/org-obedient.asl") }


/* Plans */
+!helloWorld <- .print("hello world.").

+!greetPeople[scheme(greetPeople)] : humans("no") <- .print("Nobody is in the living room."); !stopMAS.
+!greetPeople[scheme(greetPeople)] : humans("yes") & .date(Y,M,D) & .time(H,Min,S) <-
    if (H < 12) {.print("Good morning!");} 
    elif (H < 18) {.print("Good afternoon!");} 
    else {.print("Good evening!");};
    !startWork;
.

+!startWork <- .myBody.act(dusterOn); .wait(3000); .stopMAS; !!monitorTemp.

//+!stopMAS <-     .random(R); .wait(5000*R); .stopMAS.

+constitutive_rule(rosie, robotic_worker, true, true) <-
    .print(">>> INSTITUTION: The constitutive rule recognizes Rosie as a robotic worker.").

+!monitorTemp <-
   !decide;
   .wait(100);
   !monitorTemp;
.

-!monitorTemp <- .print("ERRO"); .stopMAS.


+!decide: myBody::temperature(T)[source(interoception)] & T > 60 <-
    .print("The robot burned out!");
//    .stopMAS
.
    
+!decide: myBody::temperature(T)[source(interoception)] & T > 50 & myBody::powerStatus(P)[source(proprioception)] & P=="on" <- 
    .print("[ALERT] The robot is overheated (",T,") Status is... ",P);
    .myBody.act(powerOff);
  .

+!decide: myBody::temperature(T)[source(proprioception,duster)] & T <=50 & myBody::powerStatus(P)[source(proprioception,duster)] & P=="on" <-
    .print("[INFO] Temperatura is ",T," Status is... ",P).

//+myBody::powerStatus(off)[source(proprioception,duster)] <- .print("FUNCIONANDOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOO").
+myBody::powerStatus(off)[source(proprioception)] <- .print("FUNCIONANDOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOO").

/* myBody::
port(on,"/dev/ttyEmulatedPort0",duster,778922378)[source(interoception,duster)]
powerStatus(off)[source(proprioception,duster)]
temperature(20)[source(interoception,duster)]
*/