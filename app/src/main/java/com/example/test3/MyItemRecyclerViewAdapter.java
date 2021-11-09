package com.example.test3;

import androidx.annotation.ColorRes;
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
public class MyItemRecyclerViewAdapter extends RecyclerView.Adapter<MyItemRecyclerViewAdapter.ViewHolder> {

    public final List<MyDevice> mValues;
    public final List<MySession> mSessions;

    Context ctx;

    public MyItemRecyclerViewAdapter(List<MySession>  items, List<MyDevice> devs) {
        mSessions=items;
        mValues=devs;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        ctx=parent.getContext();
        return new ViewHolder(FragmentDevicesBinding.inflate(LayoutInflater.from(ctx), parent, false));

    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        if(mValues!=null) holder.mItem = mValues.get(position);
        if(mSessions!=null) {
            holder.mSesItem = mSessions.get(position);
            holder.mIdView.setText(mSessions.get(position).devName);
            holder.mContentView.setText(mSessions.get(position).getTime());
            holder.mVersion.setText("Packet number " + mSessions.get(position).devnonce+"."+mSessions.get(position).fcntup);
            holder.mtemp.setText(""+mSessions.get(position).temperature/10.0+" C");
            holder.mbat.setText(""+mSessions.get(position).batlevel*100/7+"%");
            holder.mrssi.setText("DEV RSSI "+mSessions.get(position).rssi+" dbm");
            holder.msnr.setText("DEV SNR "+mSessions.get(position).snr);
            holder.mlrssi.setText("GW RSSI "+mSessions.get(position).local_rssi+" dbm");
            holder.mlsnr.setText("GW SNR "+mSessions.get(position).local_snr);
            holder.mpower.setText("DEV Power "+mSessions.get(position).power+" dbm");
            holder.mlpower.setText("GW Power "+mSessions.get(position).local_power+" dbm");
            if((mSessions.get(position).values & 0x00000001) ==1)
            {
                holder.mSensor1.setChipBackgroundColorResource(R.color.alert);
                holder.mSensor1.setChipIconResource(R.drawable.alert);
            }
            else
            {
                holder.mSensor1.setChipBackgroundColorResource(R.color.good);
                holder.mSensor1.setChipIconResource(R.drawable.good);
            }
            if((mSessions.get(position).values & 0x00000002) ==1)
            {
                holder.mSensor2.setChipBackgroundColorResource(R.color.alert);
                holder.mSensor2.setChipIconResource(R.drawable.alert);
            }
            else
            {
                holder.mSensor2.setChipBackgroundColorResource(R.color.good);
                holder.mSensor2.setChipIconResource(R.drawable.good);
            }
        }
        Log.i("TLS13 MyItemRecView", "position="+position+" id="+holder.mIdView.getText()+" content="+holder.mContentView.getText());
    }

    @Override
    public int getItemCount() {
        return mSessions.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        final TextView mIdView;
        final ChipGroup mChipGroup;
        public final Chip mSensor1;
        public final Chip mSensor2;
        public final Chip mContentView = new Chip(ctx);
        public final Chip mtemp = new Chip(ctx);
        public final Chip mbat = new Chip(ctx);
        public final Chip mVersion = new Chip(ctx);
        public final Chip mrssi = new Chip(ctx);
        public final Chip msnr = new Chip(ctx);
        public final Chip mlrssi = new Chip(ctx);
        public final Chip mlsnr = new Chip(ctx);
        public final Chip mpower= new Chip(ctx);
        public final Chip mlpower = new Chip(ctx);
        public MyDevice mItem;
        public MySession mSesItem;

        public ViewHolder(FragmentDevicesBinding binding) {
            super(binding.getRoot());
//            mIdView = binding.itemNumber;
            mIdView=binding.tv;
            mSensor1=binding.chip4;
            mSensor2=binding.chip5;
            mChipGroup=binding.cg;
            mContentView.setChipIconResource(R.drawable.clock);
            mContentView.setChipBackgroundColorResource(R.color.teal_200);
//            mVersion.setChipIconResource(R.drawable.n10k);
            mVersion.setChipBackgroundColorResource(R.color.teal_700);
            mtemp.setChipIconResource(R.drawable.thermo);
            mtemp.setChipBackgroundColorResource(R.color.purple_700);
            mtemp.setTextColor(0xFFFFFFFF);
            mbat.setChipIconResource(R.drawable.bat);
            mbat.setChipBackgroundColorResource(R.color.batlevel);
            mrssi.setChipBackgroundColorResource(R.color.teal_200);
            msnr.setChipBackgroundColorResource(R.color.teal_200);
            mlrssi.setChipBackgroundColorResource(R.color.teal_200);
            mlsnr.setChipBackgroundColorResource(R.color.teal_200);
            mpower.setChipBackgroundColorResource(R.color.teal_200);
            mlpower.setChipBackgroundColorResource(R.color.teal_200);
            mChipGroup.addView(mContentView);
            mChipGroup.addView(mtemp);
            mChipGroup.addView(mbat);
            mChipGroup.addView(mVersion);
            mChipGroup.addView(mrssi);
            mChipGroup.addView(msnr);
            mChipGroup.addView(mlrssi);
            mChipGroup.addView(mlsnr);
            mChipGroup.addView(mpower);
            mChipGroup.addView(mlpower);

//            mIdView.setChipBackgroundColorResource(R.color.purple_200);
//            mIdView.setChipIconResource(R.drawable.alarm_off_icon);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + mContentView.getText() + "'";
        }
    }
}