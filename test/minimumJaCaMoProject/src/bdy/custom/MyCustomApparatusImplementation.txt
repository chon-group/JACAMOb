package custom;

import group.chon.javino.Javino;
import java.time.Duration;
import java.time.Instant;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import jason.asSyntax.ASSyntax;
import jason.asSyntax.Literal;

public class MyCustomApparatusImplementation extends neck.Apparatus {

    private Javino javino;
    private Pattern pattern;
    private Matcher matcher;
    private boolean atuando = false;
    private boolean percebendo = false;
    private int timeouts=0;
    private Instant lastTimeout = Instant.ofEpochSecond(0);


    public MyCustomApparatusImplementation(String address) {
        super(address);
        javino = new Javino();
    }

    @Override
    public void act(String CMD) {
        while(this.percebendo){
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        this.atuando = true;
        javino.sendCommand(super.getAddress(),CMD);
        try {
            Thread.sleep(250);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        this.atuando = false;
    }

    @Override
    public void perceive() {
        while(this.atuando){
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        this.percebendo = true;
        if(javino.requestData(super.getAddress(),"getPercepts")){
            String percepts = javino.getData();
            if(verifyPortStatus(percepts)){
                this.percebendo = false;
                processPerceptions(percepts);
                return;
            }
        }
        this.percebendo = false;
        //return null;
    }

    private boolean verifyPortStatus(String beliefString){
        String regex = "port\\(([^,]+),(.+?)\\);";

        pattern = Pattern.compile(regex);
        matcher = pattern.matcher(beliefString);

        if (matcher.find()) {
            String portName = matcher.group(1);
            String status   = matcher.group(2);
            if ("on".equals(status)) {
                return true;
            } else if ("timeout".equals(status)) {
                if(this.timeouts<4){
                    newTimeout();
                    return true;
                }else{
                    setStatus(false);
                    return false;
                }
            } else if ("off".equals(status)) {
                setStatus(false);
                return false;
            }
        }
        setStatus(false);
        return false;
    }

    private void newTimeout(){
        Instant now = Instant.now();
        Duration duration = Duration.between(this.lastTimeout,now);
        if(duration.toMinutes() >= 1){
            this.lastTimeout = now;
            this.timeouts = 0;
        }else{
            this.timeouts++;
        }
    }

    private void processPerceptions(String stream) {
        if (stream == null || stream.isBlank()) return;

        for (String token : splitSemicolonsOutsideQuotes(stream)) {
            String t = token.trim();
            if (t.isEmpty()) continue;

            String origin = null;
            if (t.endsWith("]")) {
                int lb = t.lastIndexOf('[');
                if (lb >= 0) {
                    origin = t.substring(lb + 1, t.length() - 1).trim();
                    t = t.substring(0, lb).trim();
                }
            }

            try {
                Literal lit = ASSyntax.parseLiteral(t);

                if (origin == null || origin.isEmpty()) {
                    super.addInteroception(lit);
                } else if (origin.equalsIgnoreCase("e")) {
                    super.addExteroception(lit);
                } else if (origin.equalsIgnoreCase("i")) {
                    super.addInteroception(lit);
                } else if (origin.equalsIgnoreCase("p")) {
                    super.addProprioception(lit);
                } else {
                    // origem desconhecida -> trata como interocepção
                    super.addInteroception(lit);
                }
            } catch (Exception ignore) {
                // literal inválido: ignora
            }
        }
    }

    private static java.util.List<String> splitSemicolonsOutsideQuotes(String s) {
        java.util.List<String> out = new java.util.ArrayList<>();
        StringBuilder cur = new StringBuilder();
        boolean inSingle = false, inDouble = false, escape = false;

        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);

            if (escape) { cur.append(c); escape = false; continue; }
            if (c == '\\') { cur.append(c); escape = true; continue; }

            if (c == '"' && !inSingle) { inDouble = !inDouble; cur.append(c); continue; }
            if (c == '\'' && !inDouble) { inSingle = !inSingle; cur.append(c); continue; }

            if (c == ';' && !inSingle && !inDouble) {
                out.add(cur.toString());
                cur.setLength(0);
                continue;
            }
            cur.append(c);
        }
        if (cur.length() > 0) out.add(cur.toString());
        return out;
    }


}
