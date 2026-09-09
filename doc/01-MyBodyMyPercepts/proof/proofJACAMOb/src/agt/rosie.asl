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

+!startWork <- .myBody.act(dusterOn).

//+!stopMAS <-     .random(R); .wait(5000*R); .stopMAS.

+constitutive_rule(rosie, robotic_worker, true, true) <-
    .print(">>> INSTITUTION: The constitutive rule recognizes Rosie as a robotic worker.").
