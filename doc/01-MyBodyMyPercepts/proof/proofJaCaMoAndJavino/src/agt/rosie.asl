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

+!startWork <- togglePower; !!monitorTemp.

+!monitorTemp <- !decide; .wait(500); !monitorTemp.
   
+!decide: temperature(T)[source(percept)] & T > 70 & powerStatus(P)[source(percept)] & P=="On" <- 
    .print("My mind: temperature(",T,") + powerStatus(",P,")"); togglePower.

+!decide: temperature(T)[source(percept)] & T > 60 & powerStatus(P)[source(percept)] & P=="Off" <- 
    .print("Waiting for decreasing temperature... SUCCESS!"); .stopMAS.
    
+!decide: temperature(T)[source(percept)] & powerStatus(P)[source(percept)] <- 
    .print("My mind: temperature(",T,") + powerStatus(",P,")").

-!decide.
