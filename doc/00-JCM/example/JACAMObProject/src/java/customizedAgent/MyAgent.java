package customizedAgent;

public class MyAgent extends jason.asSemantics.Agent {

    @Override
    public void initAg() {
        super.initAg();
        System.out.println("[" +getTS().getAgArch().getAgName() +"] Custom agent using: "+getClass().getName());
    }

}