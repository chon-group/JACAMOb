{ include("$jacamo/templates/common-cartago.asl") }
{ include("$jacamo/templates/common-moise.asl") }
{ include("$moise/asl/org-obedient.asl") }


/*
!tryActionInContext.

+!tryActionInContext <- .wait(10000); !tryUseActionInContext.
+!tryUseActionInContext: legState("ok") & walk  <- .print("Funciona!"); .stopMAS.
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

+!step: needSteps(S) & S > 0 & destination(D) & avoid(A) & legState("stuck") <-
    -+avoid(A+1);
    .wait(1000);
    !!step.

+!step: needSteps(S) & S > 0 & destination(D) & legState("ok")<-
    walk;
    -+needSteps(S-1);
    .print("Steps so far: ", S-1);
    !!step.

-!step[error(no_applicable)] <- .wait(1000); !!step.
-!step[error(action_failed), error_msg("unable")] <- .wait(1000); ?stuck(S); -+stuck(S+1); !!tryStandUp.
-!step[error(action_failed), error_msg("fall")] <- .wait(1000); .print("I fell"); ?fall(Q); -+fall(Q+1); !!tryStandUp.

+!tryStandUp <- standUp(Reply); !!tryStandUp(Reply).
+!tryStandUp(R): R=="ok" <- .wait(1000); .print("StandUp again"); !!step.
+!tryStandUp(R): R=="stuck" <- .wait(1000); !!tryStandUp.