package neck.util;

import com.fazecast.jSerialComm.SerialPort;
import neck.model.SerialCommStatus;
import org.json.JSONObject;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;


/**
 * Comunicação serial utilizando JSON-seq sobre SLIP.
 *
 * Esta classe é responsável exclusivamente pelo transporte:
 * abertura e fechamento da porta serial, enquadramento SLIP,
 * transmissão de documentos JSON e recepção dos registros JSON.
 *
 * A interpretação semântica das mensagens recebidas é
 * responsabilidade da implementação concreta de Apparatus.
 */
public class SerialComm {

    private static final int BAUD_RATE = 115200;

    // SLIP
    private static final int TRANSMISSION = 0xC0;

    // JSON Text Sequences
    private static final int JSONSTART = 0x1E;
    private static final int JSONEND = 0x0A;

    private static final int TIMEOUTms = 4000;

    private final Logger logger;

    private SerialCommStatus portStatus = SerialCommStatus.UNKNOWN;
    private final String portAddress;

    private SerialPort port;
    private InputStream in;
    private OutputStream out;


    public SerialComm(String portName) {
        this.logger = Logger.getLogger("NECK");
        this.portAddress = Util.getFormatedPortName(portName);
    }


    /* ============================================================
       CONNECTION
       ============================================================ */

    public void openConnection() {

        try {

            this.port = SerialPort.getCommPort(portAddress);

            this.port.setBaudRate(BAUD_RATE);
            this.port.setNumDataBits(8);
            this.port.setNumStopBits(SerialPort.ONE_STOP_BIT);
            this.port.setParity(SerialPort.NO_PARITY);

            this.port.setComPortTimeouts(
                    SerialPort.TIMEOUT_READ_BLOCKING,
                    TIMEOUTms,
                    0
            );

            if (this.port.openPort()) {

                logger.fine(
                        "Opening SerialComm at "
                                + getPortAddress()
                );

            } else {

                logger.severe(
                        "Port already in use or cannot open: "
                                + getPortAddress()
                );

                setPortStatus(SerialCommStatus.OFF);
                return;
            }

        } catch (Exception ex) {

            logger.severe(
                    "ERROR to connect at "
                            + getPortAddress()
            );

            setPortStatus(SerialCommStatus.OFF);
            return;
        }


        try {

            /*
             * Janela de boot do dispositivo físico.
             */
            Thread.sleep(TIMEOUTms);

            /*
             * Descarta mensagens produzidas durante o boot.
             */
            drainInput(
                    TIMEOUTms / 4,
                    TIMEOUTms
            );

            in = port.getInputStream();
            out = port.getOutputStream();

            setPortStatus(SerialCommStatus.ON);

        } catch (InterruptedException ex) {

            Thread.currentThread().interrupt();
            setPortStatus(SerialCommStatus.OFF);
        }
    }


    public void closeConnection() {

        try {
            if (in != null) {
                in.close();
            }
        } catch (Exception ignored) {
        }

        try {
            if (out != null) {
                out.close();
            }
        } catch (Exception ignored) {
        }

        if (port != null) {
            port.closePort();
        }

        in = null;
        out = null;
        port = null;

        setPortStatus(SerialCommStatus.OFF);
    }


    public SerialCommStatus getPortStatus() {
        return portStatus;
    }


    public String getPortAddress() {
        return portAddress;
    }


    /* ============================================================
       JSON-SEQUENCE OVER SLIP
       ============================================================ */

    /**
     * Envia um documento JSON e retorna os documentos JSON
     * recebidos como resposta.
     *
     * Nenhuma interpretação sobre o conteúdo dos documentos é
     * realizada nesta classe.
     */
    public List<JSONObject> transact(JSONObject request) {

        List<JSONObject> response = new ArrayList<>();

        if (getPortStatus() == SerialCommStatus.OFF) {
            return response;
        }

        /*
         * Descarta resíduos de transmissões anteriores.
         */
        drainInput(25, 50);

        sendJsonSlp(request);

        if (getPortStatus() == SerialCommStatus.OFF) {
            return response;
        }

        /*
         * Aguarda o início da transmissão SLIP.
         */
        if (!readUntilTransmission()) {
            return response;
        }

        /*
         * Lê os registros JSON-seq até o fechamento
         * da transmissão SLIP.
         */
        while (getPortStatus() == SerialCommStatus.ON) {

            int b = readByte();

            if (b < 0) {
                setPortStatus(SerialCommStatus.OFF);
                break;
            }

            /*
             * Segundo 0xC0 encerra a transmissão.
             */
            if (b == TRANSMISSION) {
                break;
            }

            /*
             * Bytes que não iniciam um registro JSON-seq
             * são ignorados.
             */
            if (b != JSONSTART) {
                continue;
            }

            String jsonText = readUntilJsonEnd();
            //Log..
            //System.out.println("SERIAL RAW >>> " + jsonText);

            if (jsonText == null || jsonText.isBlank()) {
                continue;
            }

            try {

                response.add(
                        new JSONObject(jsonText)
                );

            } catch (Exception ex) {

                /*
                 * Registro JSON inválido.
                 * Ignora o fragmento e continua a transmissão.
                 */
                logger.warning(
                        "Invalid JSON received from "
                                + getPortAddress()
                                + ": "
                                + jsonText
                );
            }
        }

        return response;
    }


    /* ============================================================
       SLIP / JSON-SEQUENCE
       ============================================================ */

    private void sendJsonSlp(JSONObject document) {

        if (out == null) {
            setPortStatus(SerialCommStatus.OFF);
            return;
        }

        try {

            /*
             * SLIP transmission start.
             */
            out.write(TRANSMISSION);

            /*
             * JSON-seq record start.
             */
            out.write(JSONSTART);

            out.write(
                    document
                            .toString()
                            .getBytes(StandardCharsets.UTF_8)
            );

            /*
             * JSON-seq record end.
             */
            out.write(JSONEND);

            /*
             * SLIP transmission end.
             */
            out.write(TRANSMISSION);

            out.flush();

        } catch (Exception ex) {

            logger.severe(
                    "ERROR with communication at "
                            + getPortAddress()
            );

            setPortStatus(SerialCommStatus.OFF);
        }
    }


    /**
     * Aguarda o byte delimitador de transmissão SLIP.
     */
    private boolean readUntilTransmission() {

        while (getPortStatus() != SerialCommStatus.OFF) {

            int b = readByte();

            if (b == TRANSMISSION) {

                if (getPortStatus() == SerialCommStatus.TIMEOUT) {
                    setPortStatus(SerialCommStatus.ON);
                }

                return true;
            }

            if (b < 0) {

                setPortStatus(SerialCommStatus.OFF);
                return false;
            }

            if (getPortStatus() == SerialCommStatus.TIMEOUT) {
                return false;
            }
        }

        return false;
    }


    /**
     * Lê um registro JSON-seq até LF (0x0A).
     */
    private String readUntilJsonEnd() {

        StringBuilder sb = new StringBuilder();

        while (true) {

            int b = readByte();

            if (b < 0) {
                return null;
            }

            if (getPortStatus() == SerialCommStatus.TIMEOUT) {
                return null;
            }

            if (b == JSONEND) {
                break;
            }

            sb.append((char) b);
        }

        return sb.toString().trim();
    }


    private int readByte() {

        if (in == null) {
            setPortStatus(SerialCommStatus.OFF);
            return -1;
        }

        try {

            int incoming = in.read();

            if (incoming < 0) {
                setPortStatus(SerialCommStatus.OFF);
            }

            return incoming;

        } catch (Exception ex) {

            setPortStatus(SerialCommStatus.TIMEOUT);
            return -1;
        }
    }


    /* ============================================================
       INPUT CLEANUP
       ============================================================ */

    private void drainInput(
            long quietWindowMs,
            long maxDrainMs
    ) {

        if (port == null || !port.isOpen()) {
            return;
        }

        long start = System.currentTimeMillis();
        long lastRead = start;

        byte[] buffer = new byte[256];

        while (true) {

            int available = port.bytesAvailable();

            if (available > 0) {

                int n = port.readBytes(
                        buffer,
                        Math.min(
                                available,
                                buffer.length
                        )
                );

                if (n > 0) {
                    lastRead = System.currentTimeMillis();
                }

            } else {

                if (System.currentTimeMillis() - lastRead
                        >= quietWindowMs) {
                    break;
                }

                try {

                    Thread.sleep(10);

                } catch (InterruptedException ex) {

                    Thread.currentThread().interrupt();
                    break;
                }
            }

            if (System.currentTimeMillis() - start
                    >= maxDrainMs) {
                break;
            }
        }
    }


    /* ============================================================
       STATUS
       ============================================================ */

    private void setPortStatus(SerialCommStatus status) {

        logger.fine(
                "Serial communication ["
                        + getPortAddress()
                        + "] is "
                        + status
        );

        this.portStatus = status;
    }
}