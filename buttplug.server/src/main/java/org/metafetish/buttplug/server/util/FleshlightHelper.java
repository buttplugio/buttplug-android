package org.metafetish.buttplug.server.util;

public class FleshlightHelper {
    /**
     * Returns the distance (in percent) moved given speed (in percent) in the given duration
     * (milliseconds).
     * Thanks to @funjack - https://github
     * .com/funjack/launchcontrol/blob/master/protocol/funscript/functions.go
     *
     * @param aDuration duration (milliseconds)
     * @param aSpeed    speed (in percent)
     * @return distance (in percent)
     */
    public static double getDistance(long aDuration, double aSpeed) {
        if (aSpeed <= 0) {
            return 0;
        } else if (aSpeed > 1) {
            aSpeed = 1;
        }

        double mil = Math.pow(aSpeed / 250, -0.95);
        double diff = mil - aDuration;
        return Math.abs(diff) < 0.001 ? 0 : Math.max(Math.min((90 - (diff / mil * 90)) / 100, 1),
                0);
    }

    /**
     * Returns the speed (in percent) to move the given distance (in percent) in the given
     * duration (milliseconds).
     * Thanks to @funjack - https://github
     * .com/funjack/launchcontrol/blob/master/protocol/funscript/functions.go
     *
     * @param aDistance distance (in percent)
     * @param aDuration duration (milliseconds)
     * @return speed (in percent)
     */
    public static double GetSpeed(double aDistance, long aDuration) {
        if (aDistance <= 0) {
            return 0;
        } else if (aDistance > 1) {
            aDistance = 1;
        }
        return 250 * Math.pow((aDuration * 90) / (aDistance * 100), -1.05);
    }

    /**
     * Returns the time it will take to move the given distance (in percent) at the given
     * speed (in percent).
     *
     * @param aDistance distance (in percent)
     * @param aSpeed    speed (in percent)
     * @return time
     */
    public static double GetDuration(double aDistance, double aSpeed) {
        if (aDistance <= 0) {
            return 0;
        } else if (aDistance > 1) {
            aDistance = 1;
        }

        if (aSpeed <= 0) {
            return 0;
        } else if (aSpeed > 1) {
            aSpeed = 1;
        }

        double mil = Math.pow(aSpeed / 250, -0.95);
        return mil / (90 / (aDistance * 100));
    }
}
