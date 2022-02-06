package com.example.test3;

import androidx.annotation.ColorRes;
import androidx.navigation.Navigation;
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

    public static final int VIEW_MODE_SHORT=1;
    public static final int VIEW_MODE_MEDIUM=2;
    public static final int VIEW_MODE_LARGE=3;
    public List<MySession> mSessions;
    private int viewMode;

    Context ctx;

    public MyItemRecyclerViewAdapter(List<MySession>  items) {
        mSessions=items;
        viewMode=MyItemRecyclerViewAdapter.VIEW_MODE_MEDIUM;
    }

    public void setItems(List<MySession>  items)
    {
        mSessions=items;
    }

    public void setViewMode(int viewMode)
    {
        this.viewMode=viewMode;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        ctx=parent.getContext();
        return new ViewHolder(FragmentDevicesBinding.inflate(LayoutInflater.from(ctx), parent, false));

    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        if(mSessions!=null) {
            holder.mSesItem = mSessions.get(position);
            holder.mIdView.setText(mSessions.get(position).devName);
            holder.mSensor1.setText(mSessions.get(position).Sensor1);
            holder.mSensor2.setText(mSessions.get(position).Sensor2);
            holder.mTime.setText(mSessions.get(position).getTime());
            holder.mPacket.setText("Packet number " + mSessions.get(position).devnonce+"."+mSessions.get(position).fcntup);
            holder.mTemp.setText(""+mSessions.get(position).temperature/10.0+" C");
            holder.mBat.setText(""+mSessions.get(position).batlevel*100/7+"%");
            holder.mRssi.setText("DEV RSSI "+mSessions.get(position).rssi+" dbm");
            holder.mSnr.setText("DEV SNR "+mSessions.get(position).snr);
            holder.mLrssi.setText("GW RSSI "+mSessions.get(position).local_rssi+" dbm");
            holder.mLsnr.setText("GW SNR "+mSessions.get(position).local_snr);
            holder.mPower.setText("DEV Power "+mSessions.get(position).power+" dbm");
            holder.mLpower.setText("GW Power "+mSessions.get(position).local_power+" dbm");
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
        Log.i("TLS13 MyItemRecView", "position="+position+" id="+holder.mIdView.getText()+" content="+holder.mTime.getText());
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
        public final Chip mTime = new Chip(ctx);
        public final Chip mTemp = new Chip(ctx);
        public final Chip mBat = new Chip(ctx);
        public final Chip mPacket = new Chip(ctx);
        public final Chip mRssi = new Chip(ctx);
        public final Chip mSnr = new Chip(ctx);
        public final Chip mLrssi = new Chip(ctx);
        public final Chip mLsnr = new Chip(ctx);
        public final Chip mPower= new Chip(ctx);
        public final Chip mLpower = new Chip(ctx);
        public MyDevice mItem;
        public MySession mSesItem;

        public ViewHolder(FragmentDevicesBinding binding) {
            super(binding.getRoot());
//            mIdView = binding.itemNumber;

            mIdView=binding.tv;
            mSensor1=binding.chip4;
            mSensor2=binding.chip5;
            mChipGroup=binding.cg;
            mTime.setChipIconResource(R.drawable.clock);
            mTime.setChipBackgroundColorResource(R.color.teal_200);
            mPacket.setChipBackgroundColorResource(R.color.teal_700);
            mTemp.setChipIconResource(R.drawable.thermo);
            mTemp.setChipBackgroundColorResource(R.color.purple_700);
            mTemp.setTextColor(0xFFFFFFFF);
            mBat.setChipIconResource(R.drawable.bat);
            mBat.setChipBackgroundColorResource(R.color.batlevel);
            mRssi.setChipBackgroundColorResource(R.color.teal_200);
            mSnr.setChipBackgroundColorResource(R.color.teal_200);
            mLrssi.setChipBackgroundColorResource(R.color.teal_200);
            mLsnr.setChipBackgroundColorResource(R.color.teal_200);
            mPower.setChipBackgroundColorResource(R.color.teal_200);
            mLpower.setChipBackgroundColorResource(R.color.teal_200);
            if(viewMode!=MyItemRecyclerViewAdapter.VIEW_MODE_SHORT)
            {
                mChipGroup.addView(mTime);
                mChipGroup.addView(mTemp);
                mChipGroup.addView(mBat);
            }
            if(viewMode==MyItemRecyclerViewAdapter.VIEW_MODE_LARGE)
            {
                mChipGroup.addView(mPacket);
                mChipGroup.addView(mRssi);
                mChipGroup.addView(mSnr);
                mChipGroup.addView(mLrssi);
                mChipGroup.addView(mLsnr);
                mChipGroup.addView(mPower);
                mChipGroup.addView(mLpower);
            }

        }

        @Override
        public String toString() {
            return super.toString() + " '" + mTime.getText() + "'";
        }
    }
}