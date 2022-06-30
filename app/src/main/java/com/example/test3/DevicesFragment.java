package com.example.test3;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.example.test3.databinding.FragmentDevicesBinding;
import com.example.test3.databinding.FragmentLoginBinding;

import java.util.List;


/**
 * A fragment representing a list of Items.
 */
public class DevicesFragment extends Fragment {
    MainActivity main;
    // TODO: Customize parameter argument names
    ProgressBar progressBar;
    RecyclerView recyclerView;
    MyItemRecyclerViewAdapter myItemRecyclerViewAdapter;
    private static final String ARG_COLUMN_COUNT = "column-count";
    // TODO: Customize parameters
    private int mColumnCount = 1;
    List<MyDevice> devices=null;
    private final static String TAG="TLS13 DevicesFragment";
    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public DevicesFragment() {
     }

    // TODO: Customize parameter initialization
    @SuppressWarnings("unused")
    public static DevicesFragment newInstance(int columnCount) {
        DevicesFragment fragment = new DevicesFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_COLUMN_COUNT, columnCount);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mColumnCount = getArguments().getInt(ARG_COLUMN_COUNT);
        }
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_devices_list, container, false);
        Toolbar toolbar = (Toolbar) getActivity().findViewById(R.id.toolbar);
        toolbar.setTitle("Devices");
        Log.i(TAG, "OnCreateView");
//        progressBar=(ProgressBar) view.findViewById(R.id.progressBar5);
//        progressBar.setVisibility(View.VISIBLE);
        // Set the adapter

        if (view instanceof RecyclerView) {
            Context context = view.getContext();
            recyclerView = (RecyclerView) view;
            if (mColumnCount <= 1) {
                recyclerView.setLayoutManager(new LinearLayoutManager(context));
            } else {
                recyclerView.setLayoutManager(new GridLayoutManager(context, mColumnCount));
            }
            myItemRecyclerViewAdapter=new MyItemRecyclerViewAdapter(main.shConnectionService.sessions);
            recyclerView.setAdapter(myItemRecyclerViewAdapter);
        }
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.i(TAG,"On ViewCreated");
//        progressBar.setVisibility(View.GONE);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        main=(MainActivity)context;
    }

    @Override
    public void onStart() {
        super.onStart();
    };

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getActivity().getMenuInflater().inflate(R.menu.device_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_refresh) {
            main.shConnectionService.getSessions();
            main.shConnectionService.requestCompleted.observeForever(new Observer<Boolean>() {
                @Override
                public void onChanged(@Nullable Boolean b) {
                    if(b)
                    {
                        Log.i(TAG,"Request completed");
                        main.shConnectionService.requestCompleted.removeObserver(this);
                        myItemRecyclerViewAdapter.setItems(main.shConnectionService.sessions);
                        myItemRecyclerViewAdapter.notifyDataSetChanged();
                    } else Log.i(TAG,"Request started");
                }
            });
        }
        else if(id==R.id.action_little)
        {
            myItemRecyclerViewAdapter.setViewMode(MyItemRecyclerViewAdapter.VIEW_MODE_SHORT);
        }
        else if(id==R.id.action_medium)
        {
            myItemRecyclerViewAdapter.setViewMode(MyItemRecyclerViewAdapter.VIEW_MODE_MEDIUM);
        }
        else if(id==R.id.action_large)
        {
            myItemRecyclerViewAdapter.setViewMode(MyItemRecyclerViewAdapter.VIEW_MODE_LARGE);
        }
        else return super.onOptionsItemSelected(item);
        recyclerView.setAdapter(null);
        recyclerView.setAdapter(myItemRecyclerViewAdapter);
        return true;
    }

    @Override
    public void onPause()
    {
        Toolbar toolbar = (Toolbar) getActivity().findViewById(R.id.toolbar);
        toolbar.getMenu().findItem(R.id.action_refresh).setEnabled(false);
        super.onPause();
    }
}