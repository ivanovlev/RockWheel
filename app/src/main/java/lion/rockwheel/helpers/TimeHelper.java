package lion.rockwheel.helpers;

/**
 * Created by lion on 7/7/17.
 */

public class TimeHelper {
    public static String nanoToText(float nanoTime){
        double expire = toMin(nanoTime);
        int min = (int)expire;
        int sec = (int)((expire - min) * 60);

        if (min >= 60){
            int hour = min / 60;
            min = min - hour * 60;
            return String.format("%1$s:%2$02d:%3$02d", hour, min, sec);
        }

        return String.format("%1$s:%2$02d", min, sec);
    }

    public static double minutesToNano(float minutes){
        return minutes * 60 * 1E9;
    }

    private static double toSec(float nano){
        return nano / 1E9;
    }

    private static double toMin(float nano){
        return toSec(nano) / 60;
    }

    public static double toHour(float nano){
        return toMin(nano) / 60;
    }
}
