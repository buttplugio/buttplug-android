package org.metafetish.buttplug.core;

import android.support.annotation.NonNull;

import org.metafetish.buttplug.core.Messages.Error;
import org.metafetish.buttplug.core.Messages.Log;

import java.util.Arrays;

//import android.util.Log;

class ButtplugLog implements IButtplugLog {
    @NonNull
    private final String TAG;

    private ButtplugEventHandler logMessageReceived = new ButtplugEventHandler();

    @NonNull
    public ButtplugEventHandler getLogMessageReceived() {
        return this.logMessageReceived;
    }

    public ButtplugLog(@NonNull Class classDefinition) {
        TAG = classDefinition.getSimpleName();
    }

    public void trace(String msg) {
        this.trace(msg, false);
    }

    public void trace(String msg, boolean localOnly) {
        android.util.Log.v(TAG, msg);
        if (!localOnly) {
            logMessageReceived.invoke(new ButtplugEvent(new Log(ButtplugLogLevel.TRACE, msg)));
        }
    }

    public void debug(String msg) {
        this.debug(msg, false);
    }

    public void debug(String msg, boolean localOnly) {
        android.util.Log.d(TAG, msg);
        if (!localOnly) {
            logMessageReceived.invoke(new ButtplugEvent(new Log(ButtplugLogLevel.DEBUG, msg)));
        }
    }

    public void info(String msg) {
        this.info(msg, false);
    }

    public void info(String msg, boolean localOnly) {
        android.util.Log.i(TAG, msg);
        if (!localOnly) {
            logMessageReceived.invoke(new ButtplugEvent(new Log(ButtplugLogLevel.INFO, msg)));
        }
    }

    public void warn(String msg) {
        this.warn(msg, false);
    }

    public void warn(String msg, boolean localOnly) {
        android.util.Log.w(TAG, msg);
        if (!localOnly) {
            logMessageReceived.invoke(new ButtplugEvent(new Log(ButtplugLogLevel.WARN, msg)));
        }
    }

    public void error(String msg) {
        this.error(msg, false);
    }

    public void error(String msg, boolean localOnly) {
        android.util.Log.e(TAG, msg);
        if (!localOnly) {
            logMessageReceived.invoke(new ButtplugEvent(new Log(ButtplugLogLevel.ERROR, msg)));
        }
    }

    public void fatal(String msg) {
        this.fatal(msg, false);
    }

    public void fatal(String msg, boolean localOnly) {
        android.util.Log.wtf(TAG, msg);
        if (!localOnly) {
            logMessageReceived.invoke(new ButtplugEvent(new Log(ButtplugLogLevel.FATAL, msg)));
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
            errorMsg = ex.getClass().getSimpleName() + ": " + (msg != null ? msg : ex
                    .getMessage()) + "\n" + Arrays.toString(ex.getStackTrace());
        } else {
            errorMsg = "Unknown Exception" + (msg != null ? ": " + msg : "");
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
