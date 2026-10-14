package aamas;
import cartago.*;

import java.util.logging.Logger;

public class Program extends Artifact {

    private static final Logger LOG = Logger.getLogger("AAMAS 2027");

    static private int hour = 7;
    static private int minute = 0;

    public static int getHour() {
        return hour;
    }

    public static int getMinute() {
        return minute;
    }


    void init() {
        defineObsProperty("currentActivity", "none");
        defineObsProperty("localActivity", "none");
        defineObsProperty("time", hour, minute);
        execInternalOp("clock");
    }

    @INTERNAL_OPERATION
    void clock() {
        await_time(3000);
        say("Event venue opened.");

        while (hour < 19) {
            await_time(200); // 200 miliseconds = 1 AAMAS minute

            minute++;

            if (minute == 60) {
                minute = 0;
                hour++;
            }

            getObsProperty("time").updateValues(hour, minute);

            String activity = getActivity(hour, minute);
            ObsProperty currentActivity = getObsProperty("currentActivity");

            if (!currentActivity.stringValue().equals(activity)) {
                currentActivity.updateValue(activity);
                ObsProperty localActivity = getObsProperty("localActivity");
                localActivity.updateValue(getLocalActivity(activity));
                say("New activity: " + activity + " at " + getLocalActivity(activity));
            }
        }
        say("Event venue closed.");

        System.exit(0);
    }

    /* helpers */
    private void say(String msg) {
        LOG.info("[" + getTime() + "] " + msg);
    }

    private String getTime() {
        return String.format("%02d:%02d", hour, minute);
    }

    private String getActivity(int hour, int minute) {
        int time = hour * 60 + minute;
        if (time >= 7*60+30 && time < 8*60+30)  return "registration";
        if (time >= 8*60+30 && time < 9*60)     return "openingSession";
        if (time >= 9*60 && time < 10*60)       return "keynote";
        if (time >= 10*60 && time < 10*60+45)   return "coffeeBreak";
        if (time >= 10*60+45 && time < 12*60+30)return "session1";
        if (time >= 12*60+30 && time < 14*60)   return "lunch";
        if (time >= 14*60 && time < 15*60+45)   return "session2";
        if (time >= 15*60+45 && time < 16*60+30)return "coffeeBreak";
        if (time >= 16*60+30 && time < 17*60+30)return "awardTalk";
        if (time >= 17*60+30 && time < 19*60+30)return "silverJubileePanel";
        return "none";
    }

    private String getLocalActivity(String activity) {
        switch (activity) {
            case "registration":
                return "registrationArea";

            case "openingSession":
            case "keynote":
            case "awardTalk":
            case "silverJubileePanel":
                return "mainHall";

            case "coffeeBreak":
                return "coffeeArea";

            case "session1":
            case "session2":
                return "sessionRoom";

            case "lunch":
                return "restaurant";

            default:
                return "none";
        }
    }
}