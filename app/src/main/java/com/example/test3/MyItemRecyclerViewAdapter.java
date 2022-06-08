package com.example.test3;

import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.test3.placeholder.PlaceholderContent.PlaceholderItem;
import com.example.test3.databinding.FragmentDevicesBinding;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.List;

/**
 * {@link RecyclerView.Adapter} that can display a {@link PlaceholderItem}.
 * TODO: Replace the implementation with code for your data type.
 */
public class MyItemRecyclerViewAdapter extends RecyclerView.Adapter<MyItemRecyclerViewAdapter.myViewHolder> {

    public static final int VIEW_MODE_SHORT=1;
    public static final int VIEW_MODE_MEDIUM=2;
    public static final int VIEW_MODE_LARGE=3;
    public List<MySession> mySessions;
    private int viewMode;

    Context ctx;

    public MyItemRecyclerViewAdapter(List<MySession>  mySessions) {
        this.mySessions =mySessions;
        viewMode=MyItemRecyclerViewAdapter.VIEW_MODE_MEDIUM;
    }

    public void setItems(List<MySession>  mySessions)
    {
        this.mySessions =mySessions;
    }

    public void setViewMode(int viewMode)
    {
        this.viewMode=viewMode;
    }

    @Override
    public myViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        ctx=parent.getContext();
        return new myViewHolder(FragmentDevicesBinding.inflate(LayoutInflater.from(ctx), parent, false));

    }

    @Override
    public void onBindViewHolder(final myViewHolder holder, int devNumber) {
        if(mySessions !=null) {
            holder.mSesItem = mySessions.get(devNumber);
            holder.deviceNameText.setText(mySessions.get(devNumber).devName);
            holder.sensor1.setText(mySessions.get(devNumber).sensor1);
            holder.sensor2.setText(mySessions.get(devNumber).sensor2);
            holder.time.setText(mySessions.get(devNumber).getTime());
            holder.packetNumber.setText("Packet number " + mySessions.get(devNumber).devNonce +"."+ mySessions.get(devNumber).fcntUp);
            holder.temperature.setText(""+ mySessions.get(devNumber).temperature/10.0+" C");
            holder.battery.setText(""+ mySessions.get(devNumber).battery *100/7+"%");
            holder.remoteRSSI.setText("DEV RSSI "+ mySessions.get(devNumber).remoteRSSI +" dbm");
            holder.remoteSNR.setText("DEV SNR "+ mySessions.get(devNumber).remoteSNR);
            holder.localRSSI.setText("GW RSSI "+ mySessions.get(devNumber).localRssi +" dbm");
            holder.localSNR.setText("GW SNR "+ mySessions.get(devNumber).localSnr);
            holder.remotePower.setText("DEV Power "+ mySessions.get(devNumber).remotePower +" dbm");
            holder.localPower.setText("GW Power "+ mySessions.get(devNumber).localPower +" dbm");
            if((mySessions.get(devNumber).values & 0x00000001) ==1)
            {
                holder.sensor1.setChipBackgroundColorResource(R.color.alert);
                holder.sensor1.setChipIconResource(R.drawable.alert);
            }
            else
            {
                holder.sensor1.setChipBackgroundColorResource(R.color.good);
                holder.sensor1.setChipIconResource(R.drawable.good);
            }
            if((mySessions.get(devNumber).values & 0x00000002) ==1)
            {
                holder.sensor2.setChipBackgroundColorResource(R.color.alert);
                holder.sensor2.setChipIconResource(R.drawable.alert);
            }
            else
            {
                holder.sensor2.setChipBackgroundColorResource(R.color.good);
                holder.sensor2.setChipIconResource(R.drawable.good);
            }
        }
        Log.i("TLS13 MyItemRecView", "position="+devNumber+" id="+holder.deviceNameText.getText()+" content="+holder.time.getText());
    }

    @Override
    public int getItemCount() {
        return mySessions.size();
    }

    public class myViewHolder extends RecyclerView.ViewHolder {
        final TextView deviceNameText;
        final ChipGroup deviceData;
        public final Chip sensor1;
        public final Chip sensor2;
        public final Chip time = new Chip(ctx);
        public final Chip temperature = new Chip(ctx);
        public final Chip battery = new Chip(ctx);
        public final Chip packetNumber = new Chip(ctx);
        public final Chip remoteRSSI = new Chip(ctx);
        public final Chip remoteSNR = new Chip(ctx);
        public final Chip localRSSI = new Chip(ctx);
        public final Chip localSNR = new Chip(ctx);
        public final Chip remotePower = new Chip(ctx);
        public final Chip localPower = new Chip(ctx);
//        public MyDevice mItem;
        public MySession mSesItem;

        public myViewHolder(FragmentDevicesBinding binding) {
            super(binding.getRoot());
//            deviceNameText = binding.itemNumber;

            deviceNameText =binding.devicename;
            sensor1=binding.sensor1;
            sensor2=binding.sensor2;
            deviceData =binding.devicedata;
            time.setChipIconResource(R.drawable.clock);
            time.setChipBackgroundColorResource(R.color.teal_200);
            packetNumber.setChipBackgroundColorResource(R.color.teal_700);
            temperature.setChipIconResource(R.drawable.thermo);
            temperature.setChipBackgroundColorResource(R.color.purple_700);
            temperature.setTextColor(0xFFFFFFFF);
            battery.setChipIconResource(R.drawable.bat);
            battery.setChipBackgroundColorResource(R.color.batlevel);
            remoteRSSI.setChipBackgroundColorResource(R.color.teal_200);
            remoteSNR.setChipBackgroundColorResource(R.color.teal_200);
            localRSSI.setChipBackgroundColorResource(R.color.teal_200);
            localSNR.setChipBackgroundColorResource(R.color.teal_200);
            remotePower.setChipBackgroundColorResource(R.color.teal_200);
            localPower.setChipBackgroundColorResource(R.color.teal_200);
            if(viewMode!=MyItemRecyclerViewAdapter.VIEW_MODE_SHORT)
            {
                deviceData.addView(time);
                deviceData.addView(temperature);
                deviceData.addView(battery);
            }
            if(viewMode==MyItemRecyclerViewAdapter.VIEW_MODE_LARGE)
            {
                deviceData.addView(packetNumber);
                deviceData.addView(remoteRSSI);
                deviceData.addView(remoteSNR);
                deviceData.addView(localRSSI);
                deviceData.addView(localSNR);
                deviceData.addView(remotePower);
                deviceData.addView(localPower);
            }

        }

        @Override
        public String toString() {
            return super.toString() + " '" + time.getText() + "'";
        }
    }
}