package org.metafetish.buttplug.server.util;

public class FleshlightHelper {
    /**
     * Returns the distance (in percent) moved given speed (in percent) in the given duration
     * (milliseconds).
     * Thanks to @funjack - https://github
     * .com/funjack/launchcontrol/blob/master/protocol/funscript/functions.go
     *
     * @param duration duration (milliseconds)
     * @param speed    speed (in percent)
     * @return distance (in percent)
     */
    public static double getDistance(long duration, double speed) {
        if (speed <= 0) {
            return 0;
        } else if (speed > 1) {
            speed = 1;
        }

        double mil = Math.pow(speed / 250, -0.95);
        double diff = mil - duration;
        return Math.abs(diff) < 0.001 ? 0 : Math.max(Math.min((90 - (diff / mil * 90)) / 100, 1),
                0);
    }

    /**
     * Returns the speed (in percent) to move the given distance (in percent) in the given
     * duration (milliseconds).
     * Thanks to @funjack - https://github
     * .com/funjack/launchcontrol/blob/master/protocol/funscript/functions.go
     *
     * @param distance distance (in percent)
     * @param duration duration (milliseconds)
     * @return speed (in percent)
     */
    public static double getSpeed(double distance, long duration) {
        if (distance <= 0) {
            return 0;
        } else if (distance > 1) {
            distance = 1;
        }
        return 250 * Math.pow((duration * 90) / (distance * 100), -1.05);
    }

    /**
     * Returns the time it will take to move the given distance (in percent) at the given
     * speed (in percent).
     *
     * @param distance distance (in percent)
     * @param speed    speed (in percent)
     * @return time
     */
    public static double getDuration(double distance, double speed) {
        if (distance <= 0) {
            return 0;
        } else if (distance > 1) {
            distance = 1;
        }

        if (speed <= 0) {
            return 0;
        } else if (speed > 1) {
            speed = 1;
        }

        double mil = Math.pow(speed / 250, -0.95);
        return mil / (90 / (distance * 100));
    }
}
