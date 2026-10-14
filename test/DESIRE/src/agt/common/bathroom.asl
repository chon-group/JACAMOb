!embody.
+!embody <- .myBody.neckAttach(urinarySystem); !confBody.

+!goToBathroom: not going <-
    +going;
    .print("GOING TO BATH");
    .intention(ThisID,_,_,current);
    .findall(Intention, .intend(Intention, IntentionID) & IntentionID \== ThisID & Intention \== decide(L)[source(self)], OtherIntentions);
    for (.member(I, OtherIntentions)) {
        .print("Suspending intention: ",I);
        .suspend(I);
    }
    !empty;
    for (.member(I, OtherIntentions)) {
        .print("Resuming intention: ",I);
        .resume(I);
    } 
    -going;
.

+!goToBathroom: going.

+!empty <- .myBody.act(empty, Reply); !handleEmpty(Reply).
+!handleEmpty(executed) <- !empty.
+!handleEmpty(unable) <- .print("Bodily demand satisfied.").