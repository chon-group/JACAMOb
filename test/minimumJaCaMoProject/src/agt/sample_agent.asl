{ include("/home/nilson/chonGroup/JACAMOb/src/main/resources/templates/common-cartago.asl")}
{ include("/home/nilson/chonGroup/JACAMOb/src/main/resources/templates/common-moise.asl")}

//!attach1.
//!andar1.

+!attach1 <-
   .myBody.neckAttach("/dev/ttyEmulatedPort0");
   // .myBody.neckAttach("/dev/ttyUSB1");
    //.myBody.neckAttach(lampBoy);
    //.myBody.neckAttach("/dev/ttyUSB0");
    //!andar1[source(lampBoy)]
  //  .myBody.neckAttach(ap1,"/dev/ttyEmulatedPort0",Reply3);
.

+!atuar <-
    .myBody.act(changeLED(13,true).


+myBody::ledStatus(false) <- .print("LED: false"); .myBody.act(toggleLED).
+myBody::ledStatus(true) <- .print("LED: on"); .myBody.act(toggleLED).



+myBody::port(off,PORT,_,_)[source(interoception,APPARATUS)] <-
    .myBody.neckDetach(APPARATUS);
    !!tryAttach(APPARATUS,PORT);
.

+!tryAttach(APPARATUS,PORT) <-
     .random(R);
     .wait(5000*R);
     .myBody.neckAttach(APPARATUS,PORT);
.

-!tryAttach(APPARATUS,PORT) <- !!tryAttach(APPARATUS,PORT); .

