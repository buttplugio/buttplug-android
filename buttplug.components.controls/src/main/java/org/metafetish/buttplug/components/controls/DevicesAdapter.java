package org.metafetish.buttplug.components.controls;

import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.util.LongSparseArray;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.metafetish.buttplug.core.Messages.DeviceMessageInfo;


public class DevicesAdapter extends RecyclerView.Adapter<DevicesAdapter.ViewHolder> {
    private Handler handler;
    private LongSparseArray<DeviceMessageInfo> dataSet;

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView lineMessage;
        ViewHolder(View view) {
            super(view);
            this.lineMessage = view.findViewById(R.id.line_message);
        }
    }

    public DevicesAdapter(Handler handler) {
        this(handler, new LongSparseArray<DeviceMessageInfo>());
    }

    public DevicesAdapter(Handler handler, LongSparseArray<DeviceMessageInfo> dataSet) {
        this.handler = handler;
        this.dataSet = dataSet;
    }

    public void add(final DeviceMessageInfo dataItem) {
        this.handler.post(new Runnable() {
            @Override
            public void run() {
                DevicesAdapter.this.dataSet.put(dataItem.deviceIndex, dataItem);
                DevicesAdapter.this.notifyItemInserted(DevicesAdapter.this.dataSet.size() - 1);
            }
        });
    }

    public void remove(final long deviceIndex) {
        this.handler.post(new Runnable() {
            @Override
            public void run() {
                int position = DevicesAdapter.this.dataSet.indexOfKey(deviceIndex);
                DevicesAdapter.this.dataSet.remove(deviceIndex);
                DevicesAdapter.this.notifyItemRemoved(position);
            }
        });
    }

    public void clear() {
        this.handler.post(new Runnable() {
            @Override
            public void run() {
                DevicesAdapter.this.dataSet.clear();
                DevicesAdapter.this.notifyDataSetChanged();
            }
        });
    }

    private String getItemText(DeviceMessageInfo dataItem) {
        return String.format("%s: %s", dataItem.deviceIndex, dataItem.deviceName);
    }

    @NonNull
    @Override
    public DevicesAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_item, parent, false);
        return new DevicesAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DevicesAdapter.ViewHolder holder, int position) {
        DeviceMessageInfo dataItem = this.dataSet.valueAt(position);
        holder.lineMessage.setText(this.getItemText(dataItem));
    }

    @Override
    public int getItemCount() {
        return this.dataSet.size();
    }
}
