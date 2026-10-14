// Agent Rosie in project testTrieb
{ include("$jacamo/templates/common-cartago.asl") }
{ include("common/aamas.asl") }
{ include("common/bathroom.asl") }

+!confBody <- .myBody.act(enableTrieb); .myBody.act(disableBladderLevelPercepts).

+!myBody::fullBladder(Drang)[element(urinarySystem,bladder)]: Drang < 0.9 & busy <- 
    .print("Bodily demand detected (fullBladder=", Drang, "), but I am busy..."); 
    .wait(1000).

+!myBody::fullBladder(Drang)[element(urinarySystem,bladder)]: Drang < 0.9 & not busy <- 
    .print("Bodily demand detected (fullBladder=", Drang, ") and I am not busy..."); 
    !goToBathroom.

+!myBody::fullBladder(Drang)[element(urinarySystem,bladder)]: Drang >= 0.9 <- 
    .print("Bodily demand is urgent (fullBladder=", Drang, "). I need to go out..."); 
    !goToBathroom.