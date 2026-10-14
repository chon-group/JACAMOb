// Agent Rosie in project testTrieb
{ include("$jacamo/templates/common-cartago.asl") }
{ include("common/aamas.asl") }
{ include("common/bathroom.asl") }

+!confBody <- .myBody.act(enableTrieb); .myBody.act(enableBladderLevelPercepts).

+!myBody::fullBladder(Drang)[element(urinarySystem,bladder)]: Drang < 0.9 & busy & myBody::bladderLevel(L) <- 
    .print("Bodily demand detected (bladderLevel=", L, "), but I am busy..."); 
    .wait(1000).

+!myBody::fullBladder(Drang)[element(urinarySystem,bladder)]: Drang < 0.9 & not busy & myBody::bladderLevel(L)<- 
    .print("Bodily demand detected (bladderLevel=", L, ") and I am not busy..."); 
    !goToBathroom.

+!myBody::fullBladder(Drang)[element(urinarySystem,bladder)]: Drang >= 0.9 & myBody::bladderLevel(L) <- 
    .print("Bodily demand is urgent (bladderLevel=", L, "). I need to go out..."); 
    !goToBathroom.