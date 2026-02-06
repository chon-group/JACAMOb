package neck.util;

import neck.Apparatus;
import neck.model.*;
import com.fazecast.jSerialComm.SerialPort;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.logging.Logger;

public class SerialComm {
    // JSON-seq over SLIP (igual ao JSON_SLP.h)
    private static final int BAUD_RATE      = 115200;
    private static final int TRANSMISSION   = 0xC0; // END (SLIP)
    private static final int JSONSTART      = 0x1E; // RS  (JSON-seq)
    private static final int JSONEND        = 0x0A; // LF  (JSON-seq)
    private static final int TIMEOUTms      = 4000;
    private Logger logger;
    private SerialPortStatus portStatus     = SerialPortStatus.UNKNOWN;
    private String portAddress;
    private SerialPort port;
    private InputStream in;
    private OutputStream out;

    public SerialComm(String portName) {
        this.logger = Logger.getLogger("NECK");
        this.portAddress = neck.util.Util.getFormatedPortName(portName);
    }

    public void openConnection() {
        try{
            this.port = SerialPort.getCommPort(portAddress);
            this.port.setBaudRate(BAUD_RATE);
            this.port.setNumDataBits(8);
            this.port.setNumStopBits(SerialPort.ONE_STOP_BIT);
            this.port.setParity(SerialPort.NO_PARITY);
            this.port.setComPortTimeouts(SerialPort.TIMEOUT_READ_BLOCKING, TIMEOUTms, 0); // Leitura bloqueante com timeout (evita travar pra sempre)
            if(this.port.openPort()){
                logger.fine("Opening SerialComm at "+getPortAddress());
            }else{
                logger.severe("Port already in use or cannot open: " + getPortAddress());
                return;
            }
        }catch (Exception ex){
            logger.severe("ERROR to connect at "+getPortAddress());
            setPortStatus(SerialPortStatus.OFF);
            return;
        }

        try {
            Thread.sleep(TIMEOUTms);          // janela de boot
            drainInput(TIMEOUTms/4, TIMEOUTms);
            in = port.getInputStream();
            out = port.getOutputStream();
            setPortStatus(SerialPortStatus.ON);
        } catch (InterruptedException e) {
            setPortStatus(SerialPortStatus.OFF);
        }
    }

    public void closeConnection() {
        try { if (this.in != null) this.in.close(); } catch (Exception ignored) {}
        try { if (this.out != null) this.out.close(); } catch (Exception ignored) {}
        if (this.port != null) this.port.closePort();
        this.port = null;
        portStatus = SerialPortStatus.OFF;
    }

    public JSONObject sendMsg(String strMessage) {return sendMsg(strMessage, (Object[]) null);}

    public JSONObject sendMsg(String strMessage, Object[] args) {

        //enviando mensagem...
        drainInput(25, 50);         // limpando o canal
        if (!(getPortStatus() == SerialPortStatus.OFF)) sendJsonSlp(prepareJSON(strMessage,args));

        // Preparando para receber resposta.
        JSONObject  meta        = new JSONObject();             // bodyResponse
        JSONArray   desires  = new JSONArray();              // DESEJOS DO CORPO...
        JSONArray   actions     = new JSONArray();              // CAPACIDADES DO CORPO? AÇÕES SUPORTADAS
        JSONArray   skills      = new JSONArray();              // PLANOS/CAPACIDADE/INSTINTO? DO CORPO
        JSONObject  percepts    = new JSONObject();             // PERCEPÇÕES DO CORPO
            percepts.put("interoception", new JSONArray());
            percepts.put("proprioception", new JSONArray());
            percepts.put("exteroception", new JSONArray());
        boolean hasAnyPercept = false;

        if (!(getPortStatus() == SerialPortStatus.OFF)) {
            readUntilByte(TRANSMISSION);                                // 1) aguarda início da transmissão (0xC0)
            while (getPortStatus() == SerialPortStatus.ON) {            // 2) lê records até fechar transmissão (próximo 0xC0)
                int b = readByte();
                if (b < 0) throw new RuntimeException("Serial fechou durante a leitura.");
                if (b == TRANSMISSION) break;           // fim da transmissão
                if (b != JSONSTART) continue;           // ignora qualquer lixo/ruído entre bytes

                String jsonText = readUntilJsonEnd();   // lê o JSON textual até JSONEND (0x0A)
                if (jsonText != null && !jsonText.isBlank()) {
                    JSONObject obj;
                    try {
                        obj = new JSONObject(jsonText);
                    } catch (Exception e) {
                        continue;   // se vier um fragmento inválido, ignora
                    }
                    //System.out.println(obj.toString());
                    // METADADOS
                    if (obj.has("apparatus")) meta.put("apparatus", obj.get("apparatus"));
                    if (obj.has("apparatusID")) meta.put("apparatusID", obj.get("apparatusID"));
                    if (obj.has("bodyResponse")) meta.put("bodyResponse", obj.get("bodyResponse"));

                    // INTENTIONS (um grupo só)
                    if (obj.has("desire")) {
                        JSONObject desire = new JSONObject();
                        desire.put("desire", obj.get("desire"));
                        if (obj.has("args")) desire.put("args", obj.get("args"));
                        desires.put(desire);
                    }

                    // ACTIONS
                    if (obj.has("actionName")) {
                        JSONObject a = new JSONObject();
                        a.put("actionName", obj.get("actionName"));
                        if (obj.has("args")) a.put("args", obj.get("args")); // geralmente JSONArray
                        actions.put(a);
                    }

                    // SKILLS
                    if (obj.has("skill")) {
                        JSONObject s = new JSONObject();
                        s.put("skill", obj.get("skill"));
                        if (obj.has("context")) s.put("context", obj.get("context"));
                        if (obj.has("plans")) s.put("plans", obj.get("plans"));
                        skills.put(s);
                    }

                    // PERCEPTS agrupados por type (NOVO PADRÃO: args[])
                    if (obj.has("percept") && obj.has("type")) {
                        String type = obj.optString("type", "");
                        if (percepts.has(type)) {
                            JSONObject p = new JSONObject();
                            p.put("percept", obj.get("percept"));

                            // args é opcional (crença sem argumentos)
                            if (obj.has("args") && !obj.isNull("args")) {
                                p.put("args", obj.get("args")); // normalmente JSONArray
                            }

                            percepts.getJSONArray(type).put(p);
                            hasAnyPercept = true;
                        }
                    }

                }
            }
        }
        meta.put("port", getPortStatus().name().toLowerCase());

        // Monta JSON final
        JSONObject result = new JSONObject(meta.toMap());
        if (desires.length() > 0) result.put("desires", desires);
        if (actions.length() > 0) result.put("actions", actions);
        if (skills.length() > 0) result.put("skills", skills);
        if (hasAnyPercept) result.put("percepts", percepts);

        return result;
    }

    public SerialPortStatus getPortStatus(){
        return this.portStatus;
    }

    public String getPortAddress(){
        return this.portAddress;
    }

    // ---------- Privados ----------
    private void setPortStatus(SerialPortStatus status){
        logger.fine("Serial port ["+getPortAddress()+"] is "+status);
        this.portStatus = status;
    }

    private JSONObject prepareJSON(String strMessage, Object[] args){
        JSONObject req = new JSONObject();
        req.put("msg", strMessage);
        if (args != null && args.length > 0) {
            JSONArray arr = new JSONArray();
            for (Object a : args) {
                if (a == null) {
                    arr.put(JSONObject.NULL);
                } else if (
                        a instanceof String ||
                                a instanceof Boolean ||
                                a instanceof Integer ||
                                a instanceof Long ||
                                a instanceof Float ||
                                a instanceof Double ||
                                a instanceof JSONObject ||
                                a instanceof JSONArray
                ) {
                    arr.put(a);
                } else {
                    throw new IllegalArgumentException(
                            "Tipo de argumento não suportado: " + a.getClass()
                    );
                }
            }
            req.put("args", arr);
        }
        return req;
    }

    private void sendJsonSlp(JSONObject doc) {
        try {
            out.write(TRANSMISSION);        // startTransmission()
            out.write(JSONSTART);           // transmit()
            out.write(doc.toString().getBytes(StandardCharsets.UTF_8));
            out.write(JSONEND);             // transmit()
            out.write(TRANSMISSION);        // endTransmission()
            out.flush();
            //System.out.println(doc.toString());
        } catch (Exception e) {
            logger.severe("ERROR with communication at "+getPortAddress());
            setPortStatus(SerialPortStatus.OFF);
        }
    }

    private void readUntilByte(int target) {
        while (true) {
            if (!(getPortStatus() == SerialPortStatus.OFF)){
                int b = readByte();
                if (b == target) {
                    if(getPortStatus()==SerialPortStatus.TIMEOUT) setPortStatus(SerialPortStatus.ON);
                    return;
                }
                else if (b < 0) {
                    logger.severe("Serial port is OFF...");
                    setPortStatus(SerialPortStatus.OFF);
                    return;
                }else if (b == 0){
                    if(getPortStatus()==SerialPortStatus.ON) setPortStatus(SerialPortStatus.TIMEOUT);
                    return;
                }else{
                    System.out.print(".");
                }
            }else{
                logger.severe("Serial port is OFF...");
                return;
            }
        }
    }

    private String readUntilJsonEnd() {
        StringBuilder sb = new StringBuilder();
        while (true) {
            int b = readByte();
            if (b < 0) return null;
            if (b == JSONEND) break;
            sb.append((char) b);
        }
        return sb.toString().trim();
    }

    private int readByte() {
        try {
            int incoming = in.read();
            if (incoming < 0) setPortStatus(SerialPortStatus.OFF);
            return incoming;
        } catch (Exception e) {
            setPortStatus(SerialPortStatus.TIMEOUT);
            return 0;
        }
    }

    private void drainInput(long quietWindowMs, long maxDrainMs) {
        long start = System.currentTimeMillis();
        long lastRead = start;

        byte[] buf = new byte[256];

        while (true) {
            int avail = port.bytesAvailable();
            if (avail > 0) {
                int n = port.readBytes(buf, Math.min(avail, buf.length));
                if (n > 0) lastRead = System.currentTimeMillis();
            } else {
                // se ficou "quieto" por X ms, consideramos drenado
                if (System.currentTimeMillis() - lastRead >= quietWindowMs) break;
                // evita busy loop
                try { Thread.sleep(10); } catch (InterruptedException ignored) {}
            }

            // não deixa drenar pra sempre
            if (System.currentTimeMillis() - start >= maxDrainMs) break;
        }
    }

}
