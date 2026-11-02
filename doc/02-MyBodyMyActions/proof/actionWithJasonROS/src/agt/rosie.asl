{ include("$jacamo/templates/common-cartago.asl") }
{ include("$jacamo/templates/common-moise.asl") }
{ include("$moise/asl/org-obedient.asl") }

/*!tryActionInContext.

+!tryActionInContext <- .wait(10000); !tryUseActionInContext.
+!tryUseActionInContext: legState("ok") & embedded.mas.bridges.jacamo.defaultEmbeddedInternalAction("arduino1","keepWalking",[]) <-
    .print("Funciona!"); .stopMAS.
+!tryUseActionInContext: legState("stuck") <- .print("Espera"); .wait(1000); !tryUseActionInContext.
-!tryUseActionInContext[error(E), error_msg(M)] <- .print(E," ",M); .stopMAS.
*/


!cleanElroyBedrooom.

+!cleanElroyBedrooom <- 
    +needSteps(10); +fall(0); +stuck(0); +avoid(0); +destination(elroyBedroom); 
    .time(H,M,S); +started(H,M,S); !!step.

+!startClean <- 
    .time(NH,NM,NS); 
    ?started(SH,SM,SS); 
    .print("Started at:",SH,":",SM,":",SS," Finished at:",NH,":",NM,":",NS);
    .stopMAS.

+!step: needSteps(S) & S <= 0 & destination(D) & fall(F) & avoid(A) & stuck(T)<-
    -destination(D);
    .print("I arrived at my destination: ",D," stuck=",T," falls=",F," avoid=",A);
    !!startClean.

+!step: needSteps(S) & S > 0 & destination(D) & avoid(A) & legState("stuck")<-
    -+avoid(A+1);
    .wait(1000);
    !!step.

+!step: needSteps(S) & S > 0 & destination(D) & legState("ok") <-
    embedded.mas.bridges.jacamo.defaultEmbeddedInternalAction("arduino1","keepWalking",[]);
    .print("sent keepWalking cmd");
    .wait(keepWalkingReply(R),10000);
    !handle(keepWalking,R).

-!step[error(no_applicable)]<- .wait(1000); !!step.
-!step[error(wait_timeout)] <- .print("TIMEOUT: keepWalkingReply nao chegou"); .wait(1000); !step.
+legState("fall")   <- .wait(1000); .print("I fell"); ?fall(Q); -+fall(Q+1).

+!step: legState("fall") & needSteps(S) & S > 0 <- !tryStandUp.  

+!tryStandUp <- 
    embedded.mas.bridges.jacamo.defaultEmbeddedInternalAction("arduino1","standUp",[]);
    .print("sent standUp cmd");
    .wait(standUpReply(R),10000);
    !handle(standUp,R).

-!tryStandUp[error(wait_timeout)] <- .print("TIMEOUT: standUpReply nao chegou"); .wait(1000); !tryStandUp.

+!handle(A,R): A == keepWalking & R == "ok" & needSteps(S) <- -+needSteps(S-1); .print("Steps so far: ", S-1); !step.
+!handle(A,R): A == keepWalking & R == "stuck" <- .wait(1000); ?stuck(S); -+stuck(S+1); !tryStandUp.
+!handle(A,R): A == keepWalking & R == "fall" <- !tryStandUp.
+!handle(A,R): A == standUp & R == "ok"     <- .wait(1000); .print("StandUp again"); !step.
+!handle(A,R): A == standUp & R == "stuck"  <- .wait(1000); !tryStandUp.


