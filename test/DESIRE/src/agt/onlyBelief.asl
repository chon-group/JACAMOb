// Agent Rosie in project testTrieb
{ include("$jacamo/templates/common-cartago.asl") }
{ include("common/aamas.asl") }
{ include("common/bathroom.asl") }

+!confBody <- .myBody.act(disableTrieb); .myBody.act(enableBladderLevelPercepts).

+myBody::bladderLevel(L)[element(urinarySystem,bladder),source(interoception)]: L >= 0.75 & L < 0.975 & busy <- 
    .print("Bodily demand detected (bladderLevel=", L, "), but I am busy..."); 
    .wait(1000).

+myBody::bladderLevel(L)[element(urinarySystem,bladder),source(interoception)]: L > 0.75 & L < 0.975 & not busy <- 
    .print("Bodily demand detected (bladderLevel=", L, ") and I am not busy..."); 
    !goToBathroom.

+myBody::bladderLevel(L)[element(urinarySystem,bladder),source(interoception)]: L >= 0.975 <-
    .print("Bodily demand is urgent (bladderLevel=", L, "). I need to go out..."); 
    !goToBathroom.