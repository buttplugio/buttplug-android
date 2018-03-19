package org.metafetish.buttplug.core;

import android.support.annotation.NonNull;

import org.metafetish.buttplug.core.Messages.Error;

public interface IButtplugLog {
    @NonNull
    ButtplugEventHandler getLogMessageReceived();

    void trace(String msg);

    void trace(String msg, boolean localOnly);

    void debug(String msg);

    void debug(String msg, boolean localOnly);

    void info(String msg);

    void info(String msg, boolean localOnly);

    void warn(String msg);

    void warn(String msg, boolean localOnly);

    void error(String msg);

    void error(String msg, boolean localOnly);

    // Fatal is kept here for completeness, even if it is not yet used.
    void fatal(String msg);

    void fatal(String msg, boolean localOnly);

    void logException(Exception ex);

    void logException(Exception ex, boolean localOnly);

    void logException(Exception ex, String msg);

    void logException(Exception ex, boolean localOnly, String msg);

    @NonNull
    ButtplugEventHandler getOnLogException();

    void logErrorMsg(Error error);

    @NonNull
    Error logErrorMsg(long id, Error.ErrorClass code, String msg);

    void logWarnMsg(Error warning);

    @NonNull
    Error logWarnMsg(long id, Error.ErrorClass code, String msg);
}
