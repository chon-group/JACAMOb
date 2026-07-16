package neck.util;

import cartago.Workspace;
import javax.tools.*;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public final class CompilerLite {
    static Logger logger = Logger.getLogger("CHON");
    static final String TARGET_PATH = "target"+System.getProperty("file.separator")+"classes";
    //static volatile boolean ENV_CL_INSTALLED = false;
    static final java.util.Set<String> INSTALLED_DIRS =
            java.util.Collections.synchronizedSet(new java.util.HashSet<>());

        // SEM JAR
//    public static void compileAndInstall(String path) {
//        try {
//            Path src = Paths.get(path);
//            if (!Files.isDirectory(src)) return;
//
//            List<File> sources;
//            try (var s = Files.walk(src)) {
//                sources = s.filter(p -> p.toString().endsWith(".java"))
//                        .map(Path::toFile).collect(Collectors.toList());
//            }
//            if (sources.isEmpty()){
//                return;
//            }else{
//                for (File source : sources) {
//                    logger.log(Level.FINE,"Reading File: " +path+System.getProperty("file.separator")+source.getName());
//                }
//            }
//
//            Path out = Paths.get(TARGET_PATH+System.getProperty("file.separator")+path);
//            Files.createDirectories(out);
//
//            JavaCompiler jc = ToolProvider.getSystemJavaCompiler();
//            if (jc == null){
//                logger.log(Level.SEVERE, "No Java System Java Compiler");
//                throw new IllegalStateException("Rode com um JDK (javac indisponível).");
//            }else{
//                logger.log(Level.FINE,"Compiling Java System Java Compiler");
//            }
//
//            try (var fm = jc.getStandardFileManager(null, null, StandardCharsets.UTF_8)) {
//                fm.setLocation(StandardLocation.CLASS_OUTPUT, List.of(out.toFile()));
//                var opts = List.of("-classpath", System.getProperty("java.class.path"));
//                var units = fm.getJavaFileObjectsFromFiles(sources);
//                boolean ok = jc.getTask(null, fm, null, opts, null, units).call();
//                if (!ok) throw new IllegalStateException("Falha ao compilar fontes de src/env");
//            } catch (Exception e) {
//                throw new RuntimeException(e);
//            }
//
//            URLClassLoader envCl = new URLClassLoader(new URL[]{ out.toUri().toURL() },
//                    ClassLoader.getSystemClassLoader());
//            Thread.currentThread().setContextClassLoader(envCl);
//        } catch (Exception e) {
//            throw new RuntimeException("Erro preparando ambiente: " + e.getMessage(), e);
//        }
//    }

        // SEM SRC/BODY...
//    public static void ensureEnvClassLoaderInstalled(String sourceDir) {
//        if (ENV_CL_INSTALLED) return;
//        synchronized (Workspace.class) {
//            if (ENV_CL_INSTALLED) return;
//            try {
//                compileAndInstall(sourceDir);
//                var out = java.nio.file.Paths.get(TARGET_PATH+System.getProperty("file.separator")+sourceDir);
//                if (java.nio.file.Files.isDirectory(out)) {
//                    var url = out.toUri().toURL();
//                    // parent = *o mesmo loader da app* (para ver JaCaMo/Cartago)
//                    ClassLoader parent = ClassLoader.getSystemClassLoader();
//                    var envCl = new java.net.URLClassLoader(new java.net.URL[]{url}, parent);
//                    Thread.currentThread().setContextClassLoader(envCl);
//                    ENV_CL_INSTALLED = true;
//                } else {
//                    logger.log(Level.SEVERE,TARGET_PATH+System.getProperty("file.separator")+sourceDir+" not exists.");
//                }
//            } catch (Exception e) {
//                throw new RuntimeException("EnvClassLoader Fail", e);
//            }
//        }
//    }

    // AINDA SEM JAR DA LIB...
//    public static void ensureEnvClassLoaderInstalled(String sourceDir) {
//        //System.out.println("Processando "+sourceDir);
//        if (INSTALLED_DIRS.contains(sourceDir)) return;
//        synchronized (Workspace.class) {
//            if (INSTALLED_DIRS.contains(sourceDir)) return;
//            try {
//                compileAndInstall(sourceDir);
//
//                // Confirma que o diretório de saída existe:
//                Path out = Paths.get(TARGET_PATH + System.getProperty("file.separator") + sourceDir);
//                if (!Files.isDirectory(out)) {
//                    //logger.log(Level.SEVERE, TARGET_PATH + System.getProperty("file.separator") + sourceDir + " not exists.");
//                    return;
//                }
//
//                // Marca este dir como instalado
//                INSTALLED_DIRS.add(sourceDir);
//
//                // (N O V O) Recria o TCCL com TODOS os dirs já instalados
//                java.util.List<java.net.URL> urls = new java.util.ArrayList<>();
//                for (String dir : INSTALLED_DIRS) {
//                    Path p = Paths.get(TARGET_PATH + System.getProperty("file.separator") + dir);
//                    if (Files.isDirectory(p)) {
//                        urls.add(p.toUri().toURL());
//                    }
//                }
//
//                ClassLoader parent = ClassLoader.getSystemClassLoader();
//                java.net.URLClassLoader aggCl = new java.net.URLClassLoader(urls.toArray(new java.net.URL[0]), parent);
//                Thread.currentThread().setContextClassLoader(aggCl);
//
//            } catch (Exception e) {
//                throw new RuntimeException("EnvClassLoader Fail", e);
//            }
//        }
//    }

    public static void ensureProjectClassLoaderInstalled() {
        ensureEnvClassLoaderInstalled("src");
    }
    public static void ensureEnvClassLoaderInstalled(String sourceDir) {
        if (INSTALLED_DIRS.contains(sourceDir)) return;
        synchronized (Workspace.class) {
            if (INSTALLED_DIRS.contains(sourceDir)) return;
            try {
                compileAndInstall(sourceDir);

                //Path out = Paths.get(TARGET_PATH + File.separator + sourceDir);
                Path out = Paths.get(TARGET_PATH);
                if (!Files.isDirectory(out)) return;

                INSTALLED_DIRS.add(sourceDir);

                java.util.List<URL> urls = new java.util.ArrayList<>();

//                // (a) diretórios já instalados (compilados)
//                for (String dir : INSTALLED_DIRS) {
//                    Path p = Paths.get(TARGET_PATH + File.separator + dir);
//                    if (Files.isDirectory(p)) urls.add(p.toUri().toURL());
//                }

                // classes compiladas
                Path classesDir = Paths.get(TARGET_PATH);
                if (Files.isDirectory(classesDir)) {
                    urls.add(classesDir.toUri().toURL());
                }

                // (b) JARs da pasta lib/
                urls.addAll(jarURLsIn("lib"));

                ClassLoader parent = ClassLoader.getSystemClassLoader();
                java.net.URLClassLoader aggCl =
                        new java.net.URLClassLoader(urls.toArray(new URL[0]), parent);

                Thread.currentThread().setContextClassLoader(aggCl);

            } catch (Exception e) {
                throw new RuntimeException("EnvClassLoader Fail", e);
            }
        }
    }


    private static java.util.List<Path> jarFilesIn(String libDir) throws java.io.IOException {
        Path base = Paths.get(libDir);
        if (!Files.isDirectory(base)) return java.util.List.of();
        try (java.util.stream.Stream<Path> s = Files.walk(base)) {
            return s.filter(p -> p.toString().toLowerCase().endsWith(".jar"))
                    .collect(java.util.stream.Collectors.toList());
        }
    }

    private static java.util.List<URL> jarURLsIn(String libDir) throws java.io.IOException {
        java.util.List<URL> out = new java.util.ArrayList<>();
        for (Path p : jarFilesIn(libDir)) {
            out.add(p.toUri().toURL());
        }
        return out;
    }

    private static void compileAndInstall(String path) {
        try {
            Path src = Paths.get(path);
            if (!Files.isDirectory(src)) return;

            List<File> sources;
            try (var s = Files.walk(src)) {
                sources = s.filter(p -> p.toString().endsWith(".java"))
                        .map(Path::toFile)
                        .collect(Collectors.toList());
            }
            if (sources.isEmpty()) {
                return;
            } else {
                for (File source : sources) {
                    logger.log(Level.FINE, "Reading File: " + path + File.separator + source.getName());
                }
            }

            //Path out = Paths.get(TARGET_PATH + File.separator + path);
            Path out = Paths.get(TARGET_PATH);
            Files.createDirectories(out);

            JavaCompiler jc = ToolProvider.getSystemJavaCompiler();
            if (jc == null) {
                logger.log(Level.SEVERE, "No Java System Java Compiler");
                throw new IllegalStateException("Rode com um JDK (javac indisponível).");
            } else {
                logger.log(Level.FINE, "Compiling Java System Java Compiler");
            }

            // >>> MONTA O CLASSPATH DE COMPILAÇÃO (inclui lib/*.jar e outputs existentes)
            String compileCP = buildCompileClasspath(path);

            try (var fm = jc.getStandardFileManager(null, null, StandardCharsets.UTF_8)) {
                fm.setLocation(StandardLocation.CLASS_OUTPUT, List.of(out.toFile()));

                // opções do javac, agora com o classpath completo
                List<String> opts = new ArrayList<>();
                opts.add("-classpath");
                opts.add(compileCP);

                var units = fm.getJavaFileObjectsFromFiles(sources);
                boolean ok = jc.getTask(null, fm, null, opts, null, units).call();
                if (!ok) throw new IllegalStateException("Falha ao compilar fontes de " + path);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

            // (opcional) isto não é mais necessário se o TCCL é montado em ensureEnvClassLoaderInstalled
            URLClassLoader envCl = new URLClassLoader(new URL[]{ out.toUri().toURL() },
                    ClassLoader.getSystemClassLoader());
            Thread.currentThread().setContextClassLoader(envCl);

        } catch (Exception e) {
            throw new RuntimeException("Erro preparando ambiente: " + e.getMessage(), e);
        }
    }

    /** Monta o -classpath para o javac: java.class.path + TARGET_PATH/<dirs instalados> + out atual + lib/*.jar */
    private static String buildCompileClasspath(String sourceDir) throws IOException {
        String sep = File.pathSeparator;
        StringBuilder cp = new StringBuilder();

        // classpath atual da JVM
        cp.append(System.getProperty("java.class.path", ""));

        // outputs já instalados (na ordem que você registrou)
        synchronized (Workspace.class) {
            for (String dir : INSTALLED_DIRS) {
                Path p = Paths.get(TARGET_PATH + File.separator + dir);
                if (Files.isDirectory(p)) {
                    cp.append(sep).append(p.toAbsolutePath());
                }
            }
        }

        // output deste sourceDir
        Path thisOut = Paths.get(TARGET_PATH + File.separator + sourceDir);
        cp.append(sep).append(thisOut.toAbsolutePath());

        // JARs em lib/
        for (Path jar : jarFilesIn("lib")) {           // <- usa o helper que você já criou no passo 1
            cp.append(sep).append(jar.toAbsolutePath());
        }

        return cp.toString();
    }



}
