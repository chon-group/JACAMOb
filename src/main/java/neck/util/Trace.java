//package neck.util;
//
//import java.io.IOException;
//import java.nio.channels.FileChannel;
//import java.nio.channels.FileLock;
//import java.nio.file.Path;
//import java.nio.file.StandardOpenOption;
//import java.util.concurrent.locks.ReentrantLock;
//
//public final class Trace {
//
//    /** Ativa/desativa todos os traces. */
//    public static boolean ENABLED = true;
//
//    private static final boolean CAT1 = true;  /* JCM COMPILER */
//    private static final boolean CAT2 = true;  /* LOAD */
//    private static final boolean CAT3 = true;  /* START */
//
//    /** Arquivo usado como trava entre processos diferentes. */
//    private static final Path LOCK_FILE = Path.of("/tmp/trace.lock");
//
//    /**
//     * Trava usada entre threads da mesma JVM.
//     *
//     * Ela também evita OverlappingFileLockException quando duas threads
//     * tentam adquirir simultaneamente o mesmo FileLock.
//     */
//    private static final ReentrantLock JVM_LOCK = new ReentrantLock(true);
//
//    private Trace() {
//        // Classe utilitária
//    }
//
//    /**
//     * Exibe uma mensagem de trace juntamente com a classe,
//     * método, arquivo e linha de onde foi chamada.
//     */
//    public static void log(String message) {
//        register(message);
//    }
//
//    private static void register(String message) {
//        if (!ENABLED) {
//            return;
//        }
//
//        String linha = "unknown";
//        String strSource = "unknown";
//
//        try {
//            StackTraceElement source =
//                    Thread.currentThread().getStackTrace()[4];
//
//            strSource =
//                    source.getClassName()
//                            + "."
//                            + source.getMethodName()
//                            + "["
//                            + source.getLineNumber()
//                            + "]";
//
//        } catch (Exception ignored) {
//            strSource = "unknown";
//        }
//
//        String strHere = "unknown";
//
//        try {
//            StackTraceElement here =
//                    Thread.currentThread().getStackTrace()[3];
//
//            strHere =
//                    here.getClassName()
//                            + "."
//                            + here.getMethodName()
//                            + "["
//                            + here.getLineNumber()
//                            + "]";
//
//            linha =
//                    "("
//                            + here.getFileName()
//                            + ":"
//                            + here.getLineNumber()
//                            + ")";
//
//        } catch (Exception ignored) {
//            strHere = "unknown";
//        }
//
//        printLocked(
//                "[TRACE] "
//                        + linha
//                        + " "
//                        + strSource
//                        + " -> "
//                        + strHere
//                        + "["
//                        + message
//                        + "] "
//                        + System.lineSeparator()
//        );
//    }
//
//    public static void logCAT1() {
//        if (!CAT1) {
//            return;
//        }
//
//        register("JCM PARSE");
//    }
//
//    public static void logCAT2() {
//        if (!CAT2) {
//            return;
//        }
//
//        register("JCM LOADER");
//    }
//
//    public static void logCAT3() {
//        if (!CAT3) {
//            return;
//        }
//
//        register("JCM STARTER");
//    }
//
//    public static void importantPoint() {
//        if (!ENABLED) {
//            return;
//        }
//
//        printLocked(
//                "---------------------------------- PONTO --------------------------------------"
//                        + System.lineSeparator()
//        );
//    }
//
//    public static void logSuper(String message) {
//        registerSuper(message);
//    }
//
//    private static void registerSuper(String message) {
//        if (!ENABLED) {
//            return;
//        }
//
//        String linha = "unknown";
//        String strSource = "unknown";
//
//        try {
//            StackTraceElement source =
//                    Thread.currentThread().getStackTrace()[4];
//
//            strSource =
//                    source.getClassName()
//                            + "."
//                            + source.getMethodName()
//                            + "["
//                            + source.getLineNumber()
//                            + "]";
//
//            linha =
//                    "("
//                            + source.getFileName()
//                            + ":"
//                            + source.getLineNumber()
//                            + ") ";
//
//        } catch (Exception ignored) {
//            strSource = "unknown";
//        }
//
//        String strHere = "unknown";
//
//        try {
//            StackTraceElement here =
//                    Thread.currentThread().getStackTrace()[3];
//
//            strHere =
//                    here.getClassName()
//                            + "."
//                            + here.getMethodName()
//                            + "["
//                            + here.getLineNumber()
//                            + "]";
//
//        } catch (Exception ignored) {
//            strHere = "unknown";
//        }
//
//        printLocked(
//                "-- [TRACE] "
//                        + linha
//                        + strSource
//                        + " -> "
//                        + strHere
//                        + "["
//                        + message
//                        + "] "
//                        + System.lineSeparator()
//        );
//    }
//
//    public static void stack(String message) {
//        if (!ENABLED) {
//            return;
//        }
//
//        StackTraceElement[] stack =
//                Thread.currentThread().getStackTrace();
//
//        StringBuilder output = new StringBuilder();
//
//        output.append(System.lineSeparator());
//        output.append("======================================================");
//        output.append(System.lineSeparator());
//
//        output.append("[TRACE STACK] ");
//        output.append(message);
//        output.append(System.lineSeparator());
//
//        output.append("======================================================");
//        output.append(System.lineSeparator());
//
//        int level = 0;
//
//        // Percorre a pilha de trás para frente
//        for (int i = stack.length - 1; i >= 2; i--) {
//
//            StackTraceElement element = stack[i];
//
//            // Indentação
//            if (level > 0) {
//                output.append("    ".repeat(level - 1));
//                output.append("└── ");
//            }
//
//            output.append(element.getClassName());
//            output.append(".");
//            output.append(element.getMethodName());
//            output.append(" (");
//            output.append(element.getFileName());
//            output.append(":");
//            output.append(element.getLineNumber());
//            output.append(")");
//            output.append(System.lineSeparator());
//
//            level++;
//        }
//
//        output.append("======================================================");
//        output.append(System.lineSeparator());
//
//        /*
//         * A stack inteira é impressa dentro da mesma trava.
//         * Assim outro agente não consegue imprimir no meio dela.
//         */
//        printLocked(output.toString());
//    }
//
//    /**
//     * Imprime um bloco completo protegendo a saída contra:
//     *
//     * 1. outras threads desta mesma JVM;
//     * 2. outros processos Java que utilizem /tmp/trace.lock.
//     */
//    private static void printLocked(String text) {
//
//        JVM_LOCK.lock();
//
//        try (
//                FileChannel channel = FileChannel.open(
//                        LOCK_FILE,
//                        StandardOpenOption.CREATE,
//                        StandardOpenOption.WRITE
//                );
//
//                FileLock ignored = channel.lock()
//        ) {
//
//            System.out.print(text);
//            System.out.flush();
//
//        } catch (IOException e) {
//
//            /*
//             * Caso a trava de arquivo falhe, ainda realiza a impressão.
//             * A JVM_LOCK continua protegendo contra outras threads
//             * desta mesma JVM.
//             */
//            System.err.println(
//                    "[TRACE] Não foi possível bloquear "
//                            + LOCK_FILE
//                            + ": "
//                            + e.getMessage()
//            );
//
//            System.out.print(text);
//            System.out.flush();
//
//        } finally {
//            JVM_LOCK.unlock();
//        }
//    }
//}
package neck.util;

import java.security.PublicKey;

public final class Trace {

    /** Ativa/desativa todos os traces. */
    public static boolean ENABLED = true;
    private static final boolean CAT1 = true;  /* JCM COMPILER */
    private static final boolean CAT2 = true;  /* LOAD */
    private static final boolean CAT3 = true;  /* START */

    private Trace() {
        // Classe utilitária
    }

    /**
     * Exibe uma mensagem de trace juntamente com a classe,
     * método, arquivo e linha de onde foi chamada.
     */
    public static void log(String message) {
       register(message);
    }

    private static void register(String message) {
        if (!ENABLED) {
            return;
        }

        String linha = "unknown";
        String strSource = "unknown";
        try{
            StackTraceElement source = Thread.currentThread().getStackTrace()[4];
            strSource = source.getClassName()+"."+source.getMethodName()+"["+source.getLineNumber()+"]";
        } catch (Exception ignored) {
            strSource = "unknown";
        }


        String strHere = "unknown";
        try{
            StackTraceElement here = Thread.currentThread().getStackTrace()[3];
            strHere = here.getClassName()+"."+here.getMethodName()+"["+here.getLineNumber()+"]";
            linha = "("+here.getFileName()+":"+here.getLineNumber()+")";
        }catch (Exception ignored){
            strHere = "unknown";
        }

        System.out.println("[TRACE] "+linha+" "+strSource+" -> "+strHere+"["+message+"] ");
    }

    public static void logCAT1(){if (!CAT1){return;} register("JCM PARSE");}
    public static void logCAT2(){if (!CAT2){return;} register("JCM LOADER");}
    public static void logCAT3(){if (!CAT3){return;} register("JCM STARTER");}

    public static void importantPoint(){
        if (!ENABLED) {
            return;
        }
        System.out.println("---------------------------------- PONTO --------------------------------------");
    }


    public static void logSuper(String message) {
        registerSuper(message);
    }

    private static void registerSuper(String message) {
        if (!ENABLED) {
            return;
        }

        String linha = "unknown";
        String strSource = "unknown";
        try{
            StackTraceElement source = Thread.currentThread().getStackTrace()[4];
            strSource = source.getClassName()+"."+source.getMethodName()+"["+source.getLineNumber()+"]";
            linha = "("+source.getFileName()+":"+source.getLineNumber()+") ";
        } catch (Exception ignored) {
            strSource = "unknown";
        }


        String strHere = "unknown";
        try{
            StackTraceElement here = Thread.currentThread().getStackTrace()[3];
            strHere = here.getClassName()+"."+here.getMethodName()+"["+here.getLineNumber()+"]";
        }catch (Exception ignored){
            strHere = "unknown";
        }

        System.out.println("-- [TRACE] "+linha+" "+strSource+" -> "+strHere+"["+message+"] ");
    }

    public static void stack(String message) {
        if (!ENABLED) {
            return;
        }

        StackTraceElement[] stack = Thread.currentThread().getStackTrace();

        System.out.println();
        System.out.println("======================================================");
        System.out.println("[TRACE STACK] " + message);
        System.out.println("======================================================");

        int level = 0;

        // Percorre a pilha de trás para frente
        for (int i = stack.length - 1; i >= 2; i--) {

            StackTraceElement element = stack[i];

            // Indentação
            if (level > 0) {
                System.out.print("    ".repeat(level - 1));
                System.out.print("└── ");
            }

            System.out.println(
                    element.getClassName()
                            + "."
                            + element.getMethodName()
                            + " ("
                            + element.getFileName()
                            + ":"
                            + element.getLineNumber()
                            + ")"
            );

            level++;
        }

        System.out.println("======================================================");
    }
}