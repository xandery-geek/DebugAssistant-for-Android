package com.example.yuanxu.debugassistant;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;
import java.util.Objects;

public class ListAdapter extends ArrayAdapter<BluetoothDevice> {

    private int id;

    ListAdapter(Context context, int textViewResourceId, List<BluetoothDevice> objects) {
        super(context, textViewResourceId, objects);

        this.id = textViewResourceId;
    }

    @NonNull
    @SuppressLint("SetTextI18n")
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {

        View view;
        ViewHolder viewHolder;
        if(convertView == null)
        {
            view = LayoutInflater.from(getContext()).inflate(id, null);
            viewHolder = new ViewHolder();
            viewHolder.name_text = view.findViewById(R.id.device_name);
            viewHolder.address_text = view.findViewById(R.id.device_address);
            view.setTag(viewHolder);
        }
        else {
            view = convertView;
            viewHolder = (ViewHolder)view.getTag();
        }

        BluetoothDevice device = getItem(position);

        if(Objects.requireNonNull(device).getName() == null)
        {
            viewHolder.name_text.setText("未知设备");
        }
        else
        {
            viewHolder.name_text.setText(device.getName());
        }

        if (device.getAddress() == null)
        {
            viewHolder.address_text.setText("未知地址");
        }
        else
        {
            viewHolder.address_text.setText(device.getAddress());
        }
        return view;
    }

    static class ViewHolder{
        TextView name_text;
        TextView address_text;
    }
}
