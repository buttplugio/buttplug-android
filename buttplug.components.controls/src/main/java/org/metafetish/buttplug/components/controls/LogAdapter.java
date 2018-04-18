package org.metafetish.buttplug.components.controls;

import android.annotation.SuppressLint;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.util.Pair;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.metafetish.buttplug.core.ButtplugLogLevel;
import org.metafetish.buttplug.core.Messages.Log;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

public class LogAdapter extends RecyclerView.Adapter<LogAdapter.ViewHolder> {
    private Handler handler;
    private List<Pair<Date, Log>> dataSet;
    private ButtplugLogLevel logLevel;
    private List<Pair<Date, Log>> filteredDataSet;
    private DateFormat dateFormat;
    private View.OnLongClickListener onLongClickListener;

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView lineMessage;
        ViewHolder(View view) {
            super(view);
            this.lineMessage = view.findViewById(R.id.line_message);
        }
    }

    public LogAdapter(Handler handler, ButtplugLogLevel logLevel, View.OnLongClickListener onLongClickListener) {
        this(handler, new ArrayList<Pair<Date, Log>>(), logLevel, onLongClickListener);
    }

    @SuppressLint("SimpleDateFormat")
    public LogAdapter(Handler handler, List<Pair<Date, Log>> dataSet, ButtplugLogLevel logLevel, View.OnLongClickListener onLongClickListener) {
        this.handler = handler;
        this.dataSet = new ArrayList<>(dataSet);
        this.logLevel = ButtplugLogLevel.OFF;
        this.filteredDataSet = new ArrayList<>();
        this.dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        this.dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        this.onLongClickListener = onLongClickListener;
        this.setLogLevel(logLevel);
    }

    public void setLogLevel(final ButtplugLogLevel logLevel) {
        this.handler.post(new Runnable() {
            @Override
            public void run() {
                if (logLevel.ordinal() != LogAdapter.this.logLevel.ordinal()) {
                    if (logLevel.ordinal() >= LogAdapter.this.logLevel.ordinal()) {
                        LogAdapter.this.filteredDataSet = new ArrayList<>();
                        for (Pair<Date, Log> dataItem : LogAdapter.this.dataSet) {
                            if (dataItem.second != null && dataItem.second.logLevel.ordinal() <= logLevel.ordinal()) {
                                LogAdapter.this.filteredDataSet.add(dataItem);
                            }
                        }
                    } else {
                        List<Pair<Date, Log>> replacementDataSet = new ArrayList<>();
                        for (Pair<Date, Log> dataItem : LogAdapter.this.filteredDataSet) {
                            if (dataItem.second != null && dataItem.second.logLevel.ordinal() <= logLevel.ordinal()) {
                                replacementDataSet.add(dataItem);
                            }
                        }
                        LogAdapter.this.filteredDataSet = replacementDataSet;
                    }
                    LogAdapter.this.logLevel = logLevel;
                    LogAdapter.this.notifyDataSetChanged();
                }
            }
        });
    }

    public void add(Log msg) {
        final Pair<Date, Log> dataItem = new Pair<>(new Date(), msg);
        this.dataSet.add(dataItem);
        if (msg.logLevel.ordinal() <= this.logLevel.ordinal()) {
            this.handler.post(new Runnable() {
                @Override
                public void run() {
                    LogAdapter.this.filteredDataSet.add(dataItem);
                    LogAdapter.this.notifyItemInserted(LogAdapter.this.filteredDataSet.size() - 1);
                }
            });
        }
    }

    public void clear() {
        this.dataSet.clear();
        this.handler.post(new Runnable() {
            @Override
            public void run() {
                LogAdapter.this.filteredDataSet.clear();
                LogAdapter.this.notifyDataSetChanged();
            }
        });
    }

    public String getLogMessage(Pair<Date, Log> dataItem) {
        if (dataItem.second != null) {
            return String.format("[%s][%s][%s] %s",
                    this.dateFormat.format(dataItem.first),
                    dataItem.second.logLevel.toString().toUpperCase(),
                    dataItem.second.tag,
                    dataItem.second.logMessage);
        } else {
            return null;
        }
    }

    public List<String> getLogMessages() {
        List<String> logMessages = new ArrayList<>();
        for (Pair<Date, Log> dataItem : this.filteredDataSet) {
            String logMessage = this.getLogMessage(dataItem);
            if (logMessage != null) {
                logMessages.add(logMessage);
            }
        }
        return logMessages;
    }

    @NonNull
    @Override
    public LogAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Pair<Date, Log> dataItem = this.filteredDataSet.get(position);
        String logMessage = this.getLogMessage(dataItem);
        if (logMessage != null) {
            holder.lineMessage.setText(logMessage);
            holder.lineMessage.setOnLongClickListener(this.onLongClickListener);
        }
    }

    @Override
    public int getItemCount() {
        return this.filteredDataSet.size();
    }
}
