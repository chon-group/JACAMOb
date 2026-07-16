package neck.util;

import cartago.Artifact;
import cartago.UnknownArtifactTemplateException;

import java.util.logging.Level;
import java.util.logging.Logger;

public class CompileArtifact {
    Logger logger = Logger.getLogger("CHON");
    // helper: converte primitivo -> wrapper para comparação de tipos
    private static Class<?> wrap(Class<?> t) {
        if (!t.isPrimitive()) return t;
        if (t == int.class) return Integer.class;
        if (t == long.class) return Long.class;
        if (t == double.class) return Double.class;
        if (t == float.class) return Float.class;
        if (t == boolean.class) return Boolean.class;
        if (t == char.class) return Character.class;
        if (t == byte.class) return Byte.class;
        if (t == short.class) return Short.class;
        return t;
    }

    // parseia "example.Counter(3, \"x\")" -> [className, args...]
    private static String classNameOf(String template) {
        int p = template.indexOf('(');
        return (p >= 0 ? template.substring(0, p) : template).trim();
    }

    private static Object[] parseArgs(String template) {
        int p = template.indexOf('(');
        if (p < 0 || !template.endsWith(")")) return new Object[0];
        String inside = template.substring(p + 1, template.length() - 1).trim();
        if (inside.isEmpty()) return new Object[0];

        // split simples por vírgula (suficiente para casos básicos)
        String[] toks = inside.split("\\s*,\\s*");
        Object[] out = new Object[toks.length];
        for (int i = 0; i < toks.length; i++) {
            String t = toks[i];
            if (t.equalsIgnoreCase("true") || t.equalsIgnoreCase("false")) {
                out[i] = Boolean.valueOf(t);
            } else if (t.matches("-?\\d+")) {
                out[i] = Integer.valueOf(t);
            } else if (t.matches("-?\\d+[lL]")) {
                out[i] = Long.valueOf(t.substring(0, t.length() - 1));
            } else if (t.matches("-?\\d*\\.\\d+")) {
                out[i] = Double.valueOf(t);
            } else if ((t.startsWith("\"") && t.endsWith("\"")) || (t.startsWith("'") && t.endsWith("'"))) {
                out[i] = t.substring(1, t.length() - 1);
            } else {
                // fallback: string literal sem aspas
                out[i] = t;
            }
        }
        return out;
    }

    private static java.lang.reflect.Method pickInit(Class<?> cls, Object[] args) throws NoSuchMethodException {
        // tenta casar por aridade e tipos atribuíveis
        for (var m : cls.getMethods()) {
            if (!m.getName().equals("init")) continue;
            var ptypes = m.getParameterTypes();
            if (ptypes.length != args.length) continue;
            boolean ok = true;
            for (int i = 0; i < ptypes.length; i++) {
                if (!wrap(ptypes[i]).isInstance(args[i])) { ok = false; break; }
            }
            if (ok) return m;
        }
        // se não achou e não há args, permite init() ausente
        if (args.length == 0) throw new NoSuchMethodException("init sem parâmetros não encontrado");
        throw new NoSuchMethodException("init com aridade/tipos compatíveis não encontrado");
    }

    public Artifact makeArtifactCompiling(String template) throws UnknownArtifactTemplateException {
        try {
            //CompilerLite.ensureEnvClassLoaderInstalled("src"+System.getProperty("file.separator")+"env");
            CompilerLite.ensureProjectClassLoaderInstalled();
            String clsName = classNameOf(template);
            Object[] args = parseArgs(template);

            ClassLoader cl = Thread.currentThread().getContextClassLoader();
            Class<?> klass = Class.forName(clsName, true, cl);

            if (!cartago.Artifact.class.isAssignableFrom(klass)) {
                throw new UnknownArtifactTemplateException("Classe " + clsName + " não estende cartago.Artifact");
            }else{
                logger.log(Level.FINE, "Loading Class: " + klass.getName());
            }

            var ctor = klass.getDeclaredConstructor();
            ctor.setAccessible(true);
            cartago.Artifact art = (cartago.Artifact) ctor.newInstance();

            // chamar init(...) se existir
            try {
                if (args.length == 0) {
                    // tenta init() vazio; se não existir, tudo bem
                    var m = klass.getMethod("init");
                    m.setAccessible(true);
                    m.invoke(art);
                } else {
                    var m = pickInit(klass, args);
                    m.setAccessible(true);
                    m.invoke(art, args);
                }
            } catch (NoSuchMethodException ignore) {
                // sem init compatível — alguns artifacts não precisam
            }

            return art;
        } catch (UnknownArtifactTemplateException e) {
            throw e;
        } catch (Throwable t) {
            throw new UnknownArtifactTemplateException("template: "+template);
        }
    }
}
