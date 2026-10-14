!goToAAMAS.
+! goToAAMAS <-
    joinWorkspace("/main/aamas2027",AAMASID);       
    lookupArtifact(program,ProgramID)[wid(AAMASID)];   
    focus(ProgramID)[wid(AAMASID)]; 
    !!participateInAAMAS.
    
// General intention: participate in AAMAS
+!participateInAAMAS: currentActivity(Activity) & Activity \== "none" <- !participate(Activity); .wait(1000); !!participateInAAMAS.
-!participateInAAMAS <- .print("No activity at the moment..."); .wait(1000); !!participateInAAMAS.

// activities
+!participate("registration"): not done("registration") <- .print("Registering..."); .wait(1000); +done("registration").
+!participate("coffeeBreak"): not mealLast("coffeeBreak") <- .print("Having coffee..."); .wait(1000); -+mealLast("coffeeBreak").
+!participate("lunch"): not mealLast("lunch") <- .print("Having lunch..."); .wait(1000); -+mealLast("lunch").
+!participate(Activity): currentActivity(Activity) & localActivity(Location) & Activity \== "registration" & Activity \== "coffeeBreak" & Activity \== "lunch" <-
    +busy;
    .print("Participating in ", Activity, " at ", Location);
    .wait(1000);
    !participate(Activity).
-!participate(Activity) <- -busy.
