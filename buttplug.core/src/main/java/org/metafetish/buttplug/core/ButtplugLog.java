package org.metafetish.buttplug.core;

import android.support.annotation.NonNull;

import org.metafetish.buttplug.core.Messages.Error;
import org.metafetish.buttplug.core.Messages.Log;

import java.util.Arrays;

class ButtplugLog implements IButtplugLog {
    @NonNull
    private final String TAG;

    private ButtplugEventHandler logMessageReceived = new ButtplugEventHandler();

    @NonNull
    public ButtplugEventHandler getLogMessageReceived() {
        return this.logMessageReceived;
    }

    public ButtplugLog(@NonNull String className) {
        TAG = className;
    }

    public void trace(String msg) {
        this.trace(msg, false);
    }

    public void trace(String msg, boolean localOnly) {
        android.util.Log.v(TAG, msg);
        if (!localOnly) {
            this.logMessageReceived.invoke(new ButtplugEvent(new Log(ButtplugLogLevel.TRACE, msg, TAG)));
        }
    }

    public void debug(String msg) {
        this.debug(msg, false);
    }

    public void debug(String msg, boolean localOnly) {
        android.util.Log.d(TAG, msg);
        if (!localOnly) {
            this.logMessageReceived.invoke(new ButtplugEvent(new Log(ButtplugLogLevel.DEBUG, msg, TAG)));
        }
    }

    public void info(String msg) {
        this.info(msg, false);
    }

    public void info(String msg, boolean localOnly) {
        android.util.Log.i(TAG, msg);
        if (!localOnly) {
            this.logMessageReceived.invoke(new ButtplugEvent(new Log(ButtplugLogLevel.INFO, msg, TAG)));
        }
    }

    public void warn(String msg) {
        this.warn(msg, false);
    }

    public void warn(String msg, boolean localOnly) {
        android.util.Log.w(TAG, msg);
        if (!localOnly) {
            this.logMessageReceived.invoke(new ButtplugEvent(new Log(ButtplugLogLevel.WARN, msg, TAG)));
        }
    }

    public void error(String msg) {
        this.error(msg, false);
    }

    public void error(String msg, boolean localOnly) {
        android.util.Log.e(TAG, msg);
        if (!localOnly) {
            this.logMessageReceived.invoke(new ButtplugEvent(new Log(ButtplugLogLevel.ERROR, msg, TAG)));
        }
    }

    public void fatal(String msg) {
        this.fatal(msg, false);
    }

    public void fatal(String msg, boolean localOnly) {
        android.util.Log.wtf(TAG, msg);
        if (!localOnly) {
            this.logMessageReceived.invoke(new ButtplugEvent(new Log(ButtplugLogLevel.FATAL, msg, TAG)));
        }
    }

    private ButtplugEventHandler onLogException = new ButtplugEventHandler();

    @NonNull
    public ButtplugEventHandler getOnLogException() {
        return onLogException;
    }

    public void logException(Exception ex) {
        this.logException(ex, true, null);
    }

    public void logException(Exception ex, boolean localOnly) {
        this.logException(ex, localOnly, null);
    }

    public void logException(Exception ex, String msg) {
        this.logException(ex, true, msg);
    }

    public void logException(Exception ex, boolean localOnly, String msg) {
        String errorMsg;
        if (ex != null) {
            errorMsg = String.format("%s: %s\n%s",
                    ex.getClass().getSimpleName(),
                    (msg != null ? msg : ex.getMessage()),
                    Arrays.toString(ex.getStackTrace()));
            ex.printStackTrace();
        } else {
            errorMsg = String.format("Unknown Exception%s",
                    (msg != null ? String.format(": %s", msg) : ""));
        }
        this.error(errorMsg, localOnly);
        this.onLogException.invoke(new ButtplugEvent(new Error(errorMsg, Error.ErrorClass
                .ERROR_UNKNOWN, ButtplugConsts.DefaultMsgId)));
    }

    public void logErrorMsg(Error error) {
        this.error(error.errorMessage, false);
    }

    @NonNull
    public Error logErrorMsg(long id, Error.ErrorClass code, String msg) {
        this.error(msg, false);
        return new Error(msg, code, id);
    }

    public void logWarnMsg(Error warning) {
        this.warn(warning.errorMessage, false);
    }

    @NonNull
    public Error logWarnMsg(long id, Error.ErrorClass code, String msg) {
        this.warn(msg, false);
        return new Error(msg, code, id);
    }

}
