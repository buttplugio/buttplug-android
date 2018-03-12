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

    public ButtplugLog(@NonNull Class aClass) {
        TAG = aClass.getSimpleName();
    }

    public void trace(String aMsg) {
        this.trace(aMsg, false);
    }

    public void trace(String aMsg, boolean aLocalOnly) {
        android.util.Log.v(TAG, aMsg);
        if (!aLocalOnly) {
            logMessageReceived.invoke(new ButtplugEvent(new Log(ButtplugLogLevel.TRACE, aMsg)));
        }
    }

    public void debug(String aMsg) {
        this.debug(aMsg, false);
    }

    public void debug(String aMsg, boolean aLocalOnly) {
        android.util.Log.d(TAG, aMsg);
        if (!aLocalOnly) {
            logMessageReceived.invoke(new ButtplugEvent(new Log(ButtplugLogLevel.DEBUG, aMsg)));
        }
    }

    public void info(String aMsg) {
        this.info(aMsg, false);
    }

    public void info(String aMsg, boolean aLocalOnly) {
        android.util.Log.i(TAG, aMsg);
        if (!aLocalOnly) {
            logMessageReceived.invoke(new ButtplugEvent(new Log(ButtplugLogLevel.INFO, aMsg)));
        }
    }

    public void warn(String aMsg) {
        this.warn(aMsg, false);
    }

    public void warn(String aMsg, boolean aLocalOnly) {
        android.util.Log.w(TAG, aMsg);
        if (!aLocalOnly) {
            logMessageReceived.invoke(new ButtplugEvent(new Log(ButtplugLogLevel.WARN, aMsg)));
        }
    }

    public void error(String aMsg) {
        this.error(aMsg, false);
    }

    public void error(String aMsg, boolean aLocalOnly) {
        android.util.Log.e(TAG, aMsg);
        if (!aLocalOnly) {
            logMessageReceived.invoke(new ButtplugEvent(new Log(ButtplugLogLevel.ERROR, aMsg)));
        }
    }

    public void fatal(String aMsg) {
        this.fatal(aMsg, false);
    }

    public void fatal(String aMsg, boolean aLocalOnly) {
        android.util.Log.wtf(TAG, aMsg);
        if (!aLocalOnly) {
            logMessageReceived.invoke(new ButtplugEvent(new Log(ButtplugLogLevel.FATAL, aMsg)));
        }
    }

    private ButtplugEventHandler onLogException = new ButtplugEventHandler();
    @NonNull
    public ButtplugEventHandler getOnLogException() {
        return  onLogException;
    }

    public void logException(Exception aEx) {
        this.logException(aEx, true, null);
    }

    public void logException(Exception aEx, boolean aLocalOnly) {
        this.logException(aEx, aLocalOnly, null);
    }

    public void logException(Exception aEx, String aMsg) {
        this.logException(aEx, true, aMsg);
    }

    public void logException(Exception aEx, boolean aLocalOnly, String aMsg) {
        String errorMsg;
        if (aEx != null) {
            errorMsg = aEx.getClass().getSimpleName() + ": " + (aMsg != null ? aMsg : aEx
                    .getMessage()) + "\n" + Arrays.toString(aEx.getStackTrace());
        } else {
            errorMsg = "Unknown Exception" + (aMsg != null ? ": " + aMsg : "");
        }
        this.error(errorMsg, aLocalOnly);
        this.onLogException.invoke(new ButtplugEvent(new Error(errorMsg, Error.ErrorClass
                .ERROR_UNKNOWN, ButtplugConsts.DefaultMsgId)));
    }

    public void logErrorMsg(Error error) {
        this.error(error.errorMessage, false);
    }

    @NonNull
    public Error logErrorMsg(long aId, Error.ErrorClass aCode, String aMsg) {
        this.error(aMsg, false);
        return new Error(aMsg, aCode, aId);
    }

    public void logWarnMsg(Error warning) {
        this.warn(warning.errorMessage, false);
    }

    @NonNull
    public Error logWarnMsg(long aId, Error.ErrorClass aCode, String aMsg) {
        this.warn(aMsg, false);
        return new Error(aMsg, aCode, aId);
    }

}
