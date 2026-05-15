possoRecarregar.
!born.

+!born <- .myBody.neckAttach("/dev/ttyUSB0").


+myBody::port(off,PORT,_,_)[source(interoception,APPARATUS)] <-
    .print("porta off",PORT);
    .myBody.neckDetach(APPARATUS);
    !!tryAttach(APPARATUS);
.

+!tryAttach(APPARATUS) <-
     .random(R);
     .wait(5000*R);
     .myBody.neckAttach(APPARATUS);
.

-!tryAttach(APPARATUS) <- !!tryAttach(APPARATUS); .

