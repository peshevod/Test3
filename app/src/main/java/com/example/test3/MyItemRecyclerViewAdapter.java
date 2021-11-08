package com.example.test3;

import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.res.ColorStateList;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
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

    private final List<MyDevice> mValues;
    Context ctx;

    public MyItemRecyclerViewAdapter(List<MyDevice> items) {
        mValues = items;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        ctx=parent.getContext();
        return new ViewHolder(FragmentDevicesBinding.inflate(LayoutInflater.from(ctx), parent, false));

    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.mItem = mValues.get(position);
        holder.mIdView.setText(mValues.get(position).devName);
        holder.mContentView.setText(mValues.get(position).devEui);
        holder.mVersion.setText(" 1."+mValues.get(position).version);
        Log.i("TLS13 MyItemRecView", "position="+position+" id="+holder.mIdView.getText()+" content="+holder.mContentView.getText());
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        final TextView mIdView;
        final ChipGroup mChipGroup;
        public final Chip mContentView = new Chip(ctx);
        public final Chip mVersion = new Chip(ctx);
        public MyDevice mItem;

        public ViewHolder(FragmentDevicesBinding binding) {
            super(binding.getRoot());
//            mIdView = binding.itemNumber;
            mIdView=binding.tv;
            mChipGroup=binding.cg;
            mContentView.setChipIconResource(R.drawable.alarm_on_icon);
            mVersion.setChipIconResource(R.drawable.alarm_off_icon);
            mChipGroup.addView(mContentView);
            mChipGroup.addView(mVersion);

//            mIdView.setChipBackgroundColorResource(R.color.purple_200);
//            mIdView.setChipIconResource(R.drawable.alarm_off_icon);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + mContentView.getText() + "'";
        }
    }
}