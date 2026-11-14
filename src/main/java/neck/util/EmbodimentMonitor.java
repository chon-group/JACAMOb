package neck.util;

import jacamo.infra.JaCaMoLauncher;

import jason.asSemantics.Message;
import jason.asSyntax.ASSyntax;
import jason.asSyntax.Literal;
import jason.infra.local.LocalAgArch;
import jason.runtime.RuntimeServices;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public class EmbodimentMonitor implements Runnable {

    private static final Logger logger =
            Logger.getLogger("NECK");

    private volatile boolean running = false;
    private volatile Process process;
    private Thread thread;

    private final Set<String> temporaryAgents =
            ConcurrentHashMap.newKeySet();

    private final ScheduledExecutorService scheduler =
            Executors.newSingleThreadScheduledExecutor(r -> {
                Thread t = new Thread(r, "NECK-EventScheduler");
                t.setDaemon(true);
                return t;
            });

    public synchronized void start() {
        if (running) return;

        running = true;

        thread = new Thread(this, "NECK-EmbodimentMonitor");
        thread.setDaemon(true);
        thread.start();
    }

    private String createNeckAgent() throws Exception {

        JaCaMoLauncher launcher = JaCaMoLauncher.getJaCaMoRunner();

        if (launcher == null) {
            throw new IllegalStateException(
                    "JaCaMoLauncher is not available."
            );
        }

        RuntimeServices rt = launcher.getRuntimeServices();

        String agentName = rt.createAgent(
                "neck",
                null,
                null,
                List.of(),
                null,
                null,
                null
        );

        if ("system.not.running".equals(agentName)) {
            throw new IllegalStateException("MAS is not running.");
        }

        temporaryAgents.add(agentName);

        try {
            rt.startAgent(agentName);
        } catch (Exception e) {
            stopNeckAgent(agentName);
            throw e;
        }

        logger.fine("Agent started: "+ agentName);

        return agentName;
    }

    @Override
    public void run() {
        try {
            ProcessBuilder pb = new ProcessBuilder(
                    "udevadm",
                    "monitor",
                    "--udev",
                    "--subsystem-match=tty",
                    "--property"
            );

            pb.redirectErrorStream(true);
            process = pb.start();

            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()))) {

                String line;
                Map<String, String> event = new HashMap<>();

                while (running && (line = reader.readLine()) != null) {

                    line = line.trim();

                    if (line.startsWith("UDEV")) {
                        processEvent(event);
                        event.clear();

                    } else if (line.isEmpty()) {
                        processEvent(event);
                        event.clear();

                    } else {
                        int separator = line.indexOf('=');

                        if (separator > 0) {
                            String key = line.substring(0, separator);
                            String value = line.substring(separator + 1);

                            event.put(key, value);
                        }
                    }
                }

                if (running) {
                    processEvent(event);
                }
            }

        } catch (Exception e) {
            if (running) {
                logger.log(
                        Level.SEVERE,
                        "EmbodimentMonitor error",
                        e
                );
            }
        } finally {
            stop();
        }
    }

    private void processEvent(Map<String, String> event) {

        if (!running) return;

        if (!"tty".equals(event.get("SUBSYSTEM"))) return;

        // Processa somente conexões USB.
        if (!"add".equals(event.get("ACTION"))) return;

        String port = event.get("DEVNAME");

        if (port == null ||
                (!port.startsWith("/dev/ttyUSB")
                        && !port.startsWith("/dev/ttyACM"))) {
            return;
        }

        logger.info("Serial connected: "+ port);

        notifyAgents(port);
    }

    private void notifyAgents(String port) {

        Literal content = ASSyntax.createLiteral(
                "serialEvent",
                ASSyntax.createAtom("add"),
                ASSyntax.createString(port)
        );

        String agentName = null;

        try {
            agentName = createNeckAgent();

            broadcast(agentName, "tell", content);

            final String sender = agentName;

            scheduler.schedule(() -> {
                try {
                    broadcast(sender, "untell", content);
                } catch (Exception e) {
                    logger.log(
                            Level.SEVERE,
                            "Untell failed",
                            e
                    );
                } finally {
                    stopNeckAgent(sender);
                }
            }, 1, TimeUnit.SECONDS);

        } catch (Exception e) {
            logger.log(
                    Level.SEVERE,
                    "Broadcast failed",
                    e
            );

            if (agentName != null) {
                stopNeckAgent(agentName);
            }
        }
    }

    private void broadcast(
            String agentName,
            String performative,
            Literal content
    ) throws Exception {

        JaCaMoLauncher launcher = JaCaMoLauncher.getJaCaMoRunner();

        if (launcher == null) {
            throw new IllegalStateException(
                    "JaCaMoLauncher is not available."
            );
        }

        LocalAgArch sender = launcher.getAgs().get(agentName);

        if (sender == null) {
            throw new IllegalStateException(
                    "Agent "+ agentName + "is not available."
            );
        }

        Message message = new Message(
                performative,
                agentName,
                null,
                content
        );

        logger.fine("Broadcasting: "+ message);

        sender.broadcast(message);
    }

    private void stopNeckAgent(String agentName) {

        if (agentName == null) return;

        if (!temporaryAgents.remove(agentName)) return;

        try {
            JaCaMoLauncher launcher = JaCaMoLauncher.getJaCaMoRunner();

            if (launcher != null) {
                RuntimeServices rt = launcher.getRuntimeServices();

                boolean killed = rt.killAgent(
                        agentName,
                        null,
                        0
                );

                if (killed) {
                    logger.fine(
                            "Agent stopped: "+ agentName
                    );
                } else {
                    logger.warning(
                            "Could not stop agent: "+ agentName
                    );
                }
            }

        } catch (Exception e) {
            logger.log(
                    Level.SEVERE,
                    "Agent termination failed: "+ agentName,
                    e
            );
        }
    }

    public synchronized void stop() {

        running = false;

        if (process != null) {
            process.destroy();
        }

        if (thread != null && thread != Thread.currentThread()) {
            thread.interrupt();
        }

        scheduler.shutdownNow();

        for (String agentName : List.copyOf(temporaryAgents)) {
            stopNeckAgent(agentName);
        }
    }

    public static void main(String[] args) throws Exception {

        EmbodimentMonitor monitor = new EmbodimentMonitor();
        monitor.start();

        Runtime.getRuntime().addShutdownHook(
                new Thread(monitor::stop)
        );

        Thread.currentThread().join();
    }
}