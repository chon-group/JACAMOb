
+myBody::led(STATUS)[source(TYPE,APPARATUS)] <-
    .wait(250);
    .myBody.act(toggleLED,app2,BodyRESPONSE);
    .print("result is ",BodyRESPONSE);
.



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