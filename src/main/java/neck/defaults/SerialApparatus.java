package neck.defaults;

import jason.asSyntax.Term;
import neck.Apparatus;
import neck.model.BodyResponse;
import neck.model.SerialCommStatus;
import neck.util.SerialComm;
import neck.util.Util;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;


/**
 * Implementação padrão de Apparatus baseada em comunicação serial.
 *
 * Esta classe adapta o protocolo utilizado pelos dispositivos
 * físicos conectados através da interface serial para a representação
 * esperada por Apparatus.
 */
public class SerialApparatus extends Apparatus {

    private final SerialComm serialComm;


    public SerialApparatus(String address) {

        this.serialComm = new SerialComm(
                Util.getFormatedPortName(address)
        );

        super.init();
    }


    /* ============================================================
       CONNECTION
       ============================================================ */

    @Override
    public void attach() {

        if (serialComm.getPortStatus()
                == SerialCommStatus.ON) {

            serialComm.closeConnection();
        }

        serialComm.openConnection();
    }


    @Override
    public void detach() {
        serialComm.closeConnection();
    }


    @Override
    public boolean getStatus() {

        return serialComm.getPortStatus()
                == SerialCommStatus.ON;
    }


    @Override
    public String getConnectionStatus() {
        return serialComm.getPortStatus().toString();
    }


    @Override
    public String getAddress() {
        return serialComm.getPortAddress();
    }


    /* ============================================================
       ACTIONS
       ============================================================ */

    @Override
    protected JSONObject getActions() {
        return request("getActions", null);
    }


    @Override
    public BodyResponse act(Term actionTerm) {

        JSONObject response = request(
                Util.getFunctor(actionTerm),
                Util.argsOfTermToObjects(actionTerm)
        );

        return BodyResponse.jsonObjectToBodyResponse(
                response
        );
    }


    /* ============================================================
       KNOW-HOW
       ============================================================ */

    @Override
    protected JSONObject getKnowHow() {
        return request("getKnowHow", null);
    }


    /* ============================================================
       PERCEPTION
       ============================================================ */

    @Override
    public JSONObject perceive() {
        return request("getPercepts", null);
    }


    /* ============================================================
       EMBODIMENT
       ============================================================ */

    @Override
    public JSONObject embody() {

        /*
         * Atualmente o protocolo serial não exige uma
         * operação específica durante a incorporação.
         */
        return null;
    }


    /* ============================================================
       SERIAL APPARATUS PROTOCOL
       ============================================================ */

    /**
     * Cria uma requisição segundo o protocolo do apparatus,
     * envia através da comunicação serial e adapta os registros
     * JSON recebidos para a representação esperada por Apparatus.
     */
    private JSONObject request(
            String message,
            Object[] args
    ) {

        JSONObject request =
                prepareRequest(message, args);

        List<JSONObject> records =
                serialComm.transact(request);

        return parseResponse(records);
    }


    /**
     * Monta a mensagem utilizada pelo protocolo dos dispositivos
     * seriais suportados pelo NECK.
     */
    private JSONObject prepareRequest(
            String message,
            Object[] args
    ) {

        JSONObject request = new JSONObject();

        request.put("msg", message);

        if (args == null || args.length == 0) {
            return request;
        }

        JSONArray jsonArgs = new JSONArray();

        for (Object arg : args) {

            if (arg == null) {

                jsonArgs.put(JSONObject.NULL);

            } else if (
                    arg instanceof String
                            || arg instanceof Boolean
                            || arg instanceof Integer
                            || arg instanceof Long
                            || arg instanceof Float
                            || arg instanceof Double
                            || arg instanceof JSONObject
                            || arg instanceof JSONArray
            ) {

                jsonArgs.put(arg);

            } else {

                throw new IllegalArgumentException(
                        "Unsupported argument type: "
                                + arg.getClass()
                );
            }
        }

        request.put("args", jsonArgs);

        return request;
    }


    /**
     * Interpreta os registros recebidos do recurso físico e
     * constrói a representação utilizada por Apparatus.
     */
    private JSONObject parseResponse(
            List<JSONObject> records
    ) {

        JSONObject meta = new JSONObject();

        JSONArray triebs = new JSONArray();
        JSONArray actions = new JSONArray();
        JSONArray knowHow = new JSONArray();

        JSONObject percepts = new JSONObject();

        percepts.put(
                "interoception",
                new JSONArray()
        );

        percepts.put(
                "proprioception",
                new JSONArray()
        );

        percepts.put(
                "exteroception",
                new JSONArray()
        );

        boolean hasAnyPercept = false;


        for (JSONObject record : records) {

            /* ----------------------------------------------------
               METADATA
               ---------------------------------------------------- */

            if (record.has("apparatus")) {

                meta.put(
                        "apparatus",
                        record.get("apparatus")
                );
            }

            if (record.has("apparatusID")) {

                meta.put(
                        "apparatusID",
                        record.get("apparatusID")
                );
            }

            if (record.has("bodyResponse")) {

                meta.put(
                        "bodyResponse",
                        record.get("bodyResponse")
                );
            }

            if (record.has("request")) {

                meta.put(
                        "request",
                        record.get("request")
                );
            }


            /* ----------------------------------------------------
               TRIEBS
               ---------------------------------------------------- */

            if (record.has("trieb")) {

                JSONObject trieb =
                        new JSONObject();

                trieb.put(
                        "trieb",
                        record.get("trieb")
                );

                if (record.has("args")) {

                    trieb.put(
                            "args",
                            record.get("args")
                    );
                }

                if (record.has("drang")) {

                    trieb.put(
                            "drang",
                            record.get("drang")
                    );
                }

                if (record.has("element")) {

                    trieb.put(
                            "element",
                            record.get("element")
                    );
                }

                triebs.put(trieb);
            }


            /* ----------------------------------------------------
               SUPPORTED ACTIONS
               ---------------------------------------------------- */

            if (record.has("action")) {

                JSONObject action =
                        new JSONObject();

                action.put(
                        "action",
                        record.get("action")
                );

                if (record.has("args")) {

                    action.put(
                            "args",
                            record.get("args")
                    );
                }

                actions.put(action);
            }


            /* ----------------------------------------------------
               KNOW-HOW
               ---------------------------------------------------- */

            if (record.has("skill")) {

                JSONObject skill =
                        new JSONObject();

                skill.put(
                        "skill",
                        record.get("skill")
                );

                if (record.has("context")) {

                    skill.put(
                            "context",
                            record.get("context")
                    );
                }

                if (record.has("plan")) {

                    skill.put(
                            "plan",
                            record.get("plan")
                    );
                }

                knowHow.put(skill);
            }


            /* ----------------------------------------------------
               PERCEPTIONS
               ---------------------------------------------------- */

            if (record.has("percept")
                    && record.has("type")) {

                String type =
                        record.optString(
                                "type",
                                ""
                        );

                if (!percepts.has(type)) {
                    continue;
                }

                JSONObject percept =
                        new JSONObject();

                percept.put(
                        "percept",
                        record.get("percept")
                );

                if (record.has("args")
                        && !record.isNull("args")) {

                    percept.put(
                            "args",
                            record.get("args")
                    );
                }

                if (record.has("element")) {

                    percept.put(
                            "element",
                            record.get("element")
                    );
                }

                if (record.has("status")) {

                    percept.put(
                            "status",
                            record.get("status")
                    );
                }

                percepts
                        .getJSONArray(type)
                        .put(percept);

                hasAnyPercept = true;
            }
        }


        /* --------------------------------------------------------
           CONNECTION STATUS
           -------------------------------------------------------- */

        meta.put(
                "port",
                serialComm
                        .getPortStatus()
                        .name()
                        .toLowerCase()
        );


        /* --------------------------------------------------------
           NORMALIZED APPARATUS RESPONSE
           -------------------------------------------------------- */

        JSONObject result =
                new JSONObject(meta.toMap());

        if (triebs.length() > 0) {
            result.put("triebs", triebs);
        }

        if (actions.length() > 0) {
            result.put("actions", actions);
        }

        if (knowHow.length() > 0) {
            result.put("knowHow", knowHow);
        }

        if (hasAnyPercept) {
            result.put("percepts", percepts);
        }

        return result;
    }
}