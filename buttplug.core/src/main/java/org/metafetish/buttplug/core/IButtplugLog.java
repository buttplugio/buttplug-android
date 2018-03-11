package org.metafetish.buttplug.core;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.metafetish.buttplug.core.Messages.Error;

public interface IButtplugLog {
    @Nullable
    ButtplugEventHandler logMessageReceived = null;

    void trace(String aMsg);

    void trace(String aMsg, boolean aLocalOnly);

    void debug(String aMsg);

    void debug(String aMsg, boolean aLocalOnly);

    void info(String aMsg);

    void info(String aMsg, boolean aLocalOnly);

    void warn(String aMsg);

    void warn(String aMsg, boolean aLocalOnly);

    void error(String aMsg);

    void error(String aMsg, boolean aLocalOnly);

    // Fatal is kept here for completeness, even if it is not yet used.
    void fatal(String aMsg);

    void fatal(String aMsg, boolean aLocalOnly);

    void logException(Exception aEx);

    void logException(Exception aEx, boolean aLocalOnly);

    void logException(Exception aEx, String aMsg);

    void logException(Exception aEx, boolean aLocalOnly, String aMsg);

    @Nullable
    ButtplugEventHandler onLogException = null;

    void logErrorMsg(Error error);

    @NonNull
    Error logErrorMsg(long aId, Error.ErrorClass aCode, String aMsg);

    void logWarnMsg(Error warning);

    @NonNull
    Error logWarnMsg(long aId, Error.ErrorClass aCode, String aMsg);
}
