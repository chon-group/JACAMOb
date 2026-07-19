package jacamo.project;

import java.io.Serializable;
import java.util.*;

import jason.mas2j.AgentParameters;
import jason.mas2j.ClassParameters;


public class JaCaMoBodyParameters implements Serializable {

    private static final long serialVersionUID = 1L;

    protected JaCaMoProject project;
    protected String name;
    protected Map<String,ClassParameters> apparatus = new HashMap<>();
    protected boolean debug = false;

    public JaCaMoBodyParameters(JaCaMoProject project) {
        neck.util.Trace.logCAT1();
        this.project = project;
    }

    public void setName(String n) { name = n; }
    public String getName()       { return name; }

    public void addApparatus(String apparatusName, ClassParameters className) {
        neck.util.Trace.logCAT1();
        apparatus.put(apparatusName,className);
        //System.out.println("ATENCAO: addApparatus("+name+","+apparatusName+","+className+")");
    }

     public Integer getApparatusCount() {
        return apparatus.size();
    }

    public Set<Map.Entry<String, ClassParameters>> getApparatusEntries() {
        return apparatus.entrySet();
    }

    public Collection<ClassParameters> getApparatusValues() {
        return apparatus.values();
    }

    public void setDebug(boolean on) {
        //debug = on; TODO: it is not working
        System.err.println("debug option for workspace is not working and is disabled, try web inspector instead.");
    }
    public boolean hasDebug() {
        return debug;
    }
    

    @Override
    public String toString() {
        StringBuilder s = new StringBuilder("   body "+name+" {\n");
        for (String a: apparatus.keySet()) {
            s.append("      apparatus "+a+": "+apparatus.get(a)+"\n");
        }
        s.append("   }");
        return s.toString();
    }

//    @Override
//    public boolean equals(Object o) {
//        if (! (o instanceof JaCaMoBodyParameters)) return false;
//        return this.name.equals( ((JaCaMoBodyParameters)o).name );
//    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

}
