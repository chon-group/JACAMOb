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

+constitutive_rule(rosie, robotic_worker, true, true) <- .print(">>> INSTITUTION: The constitutive rule recognizes Rosie as a robotic worker.").

+!startWork <- .myBody.act(dusterOn); !!monitorTemp.

+!monitorTemp <- !decide; .wait(500); !monitorTemp.

+!decide: myBody::temperature(T)[source(interoception)] & T > 100 <- 
    .print("The robot burned out! -- FAIL!!!"); .stopMAS.
    
+!decide: myBody::temperature(T)[source(interoception)] & T > 90 
& myBody::powerStatus(P)[source(proprioception)] & P=="On" <- 
    .print("[ALERT] The robot is overheated (",T,") Status is... ",P);
    .myBody.act(dusterOff);
    .print("SUCESS!!!"); .stopMAS.

+!decide: myBody::temperature(T)[source(interoception)] & myBody::powerStatus(P)[source(proprioception)] <- 
    .print("[INFO] Temperature is ",T," powerStatus is... ",P).

-!decide.
