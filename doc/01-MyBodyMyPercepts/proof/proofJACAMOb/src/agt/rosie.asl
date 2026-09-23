// Agent rosie in project proofJaCaMoAndJavino
{ include("$jacamo/templates/common-cartago.asl") }
{ include("$jacamo/templates/common-moise.asl") }
{ include("$moise/asl/org-obedient.asl") }


/* Plans */
+!cleanLivingRoom[scheme(cleaning)] <- 
    .print("Starting to clean the living room..."); 
    !startWork.

+!startWork <- 
    !togglePower; 
    !!monitorBodyTemp.

+!monitorBodyTemp <- 
    !decide; 
    .wait(500); 
    !monitorBodyTemp.

-!decide.

+status("burnt") <- .print("The robot burned out! temperature > 100ºC -- FAIL!!!"); .stopMAS.

/* new code */
+!togglePower <- .myBody.act(tooglePower).
  
+!decide: myBody::temperature(T)[source(interoception)] & T > 70 & myBody::powerStatus(P)[source(proprioception)] & P=="On" <- 
    .print("[ALERT] The robot is overheated (",T,") Status is... ",P); .myBody.act(tooglePower).

+!decide: myBody::temperature(T)[source(interoception)] & T > 60 & myBody::powerStatus(P)[source(proprioception)] & P=="Off" <- 
    .print("Waiting for decreasing temperature... SUCCESS!"); .stopMAS.

+!decide: myBody::temperature(T)[source(interoception)] & myBody::powerStatus(P)[source(proprioception)] <- 
    .print("[INFO] Temperature is ",T," powerStatus is... ",P).

+myBody::temperature(T)[source(interoception)]: T>100 <- +status("burnt").