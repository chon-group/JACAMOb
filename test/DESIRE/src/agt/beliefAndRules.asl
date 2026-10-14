// Agent Rosie in project testTrieb
{ include("$jacamo/templates/common-cartago.asl") }
{ include("common/aamas.asl") }
{ include("common/bathroom.asl") }

need        :- myBody::bladderLevel(L) & L >= 0.75 & L < 0.975.
urgency     :- myBody::bladderLevel(L) & L >= 0.975.

+!confBody <- .myBody.act(disableTrieb); .myBody.act(enableBladderLevelPercepts).

+myBody::bladderLevel(L)[element(urinarySystem,bladder),source(interoception)]: .number(L) & not deciding <- +deciding; !!decide(L).

+!decide(L): need & busy <- 
    .print("Bodily demand detected (bladderLevel=", L, "), but I am busy...");
    .wait(1000); 
    -deciding.

+!decide(L): need & not busy & not going <- 
    .print("Bodily demand detected (bladderLevel=", L, ") and I am not busy..."); 
    !goToBathroom;
    -deciding.

+!decide(L): urgency & not going <-
    .print("Bodily demand is urgent (bladderLevel=", L, "). I need to go out..."); 
    !goToBathroom;
    -deciding.

+!decide(L): going <- -deciding.

-!decide.