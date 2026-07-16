package customizedAgent;

import java.util.Collection;

import jason.architecture.AgArch;
import jason.asSyntax.Literal;

public class MyAgArch extends AgArch {
    private boolean first = true;

    @Override
    public Collection<Literal> perceive() {

        if (first) {
            first = false;
            System.out.println("[" + getAgName() + "] Custom ARCH using: " +getClass().getName());
        }

        return super.perceive();
    }

}