package com.initpointdk.android.potatocam;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;


public class ModesFragment extends Fragment {
    private  ArrayList<ModeItem> modeArray;
    private RecyclerView modesRecyclerView;

    private static final String ARG_FRAG_PLACEHOLDER = "param1";
    private int fragmentPlaceholder;

    public static ModesFragment newInstance(int fragmentPlaceholder) {
        ModesFragment fragment = new ModesFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_FRAG_PLACEHOLDER, fragmentPlaceholder);
        fragment.setArguments(args);
        return fragment;
    }
    public ModesFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            fragmentPlaceholder = getArguments().getInt(ARG_FRAG_PLACEHOLDER);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_modes, container, false);
        modeArray = new ArrayList<ModeItem>();
        modeArray.add(new ModeItem("Blur", R.drawable.ic_blur_low_black_48dp));
        modeArray.add(new ModeItem("Sharpen", R.drawable.ic_sharpen_black_48dp));
        modeArray.add(new ModeItem("Brightness", R.drawable.ic_brightness_low_black_48dp));
        modeArray.add(new ModeItem("Contrast", R.drawable.ic_contrast_black_48dp));
        modesRecyclerView = (RecyclerView) v.findViewById(R.id.modesRecyclerView);
        ModesRecycleViewAdapter modesAdapter = new ModesRecycleViewAdapter(modeArray);
        RecyclerView.LayoutManager modesListLayoutManager = new LinearLayoutManager(getActivity(),
                LinearLayoutManager.HORIZONTAL, false);
        modesRecyclerView.setLayoutManager(modesListLayoutManager);
        modesRecyclerView.setAdapter(modesAdapter);
        return v;
    }


    private class ModesRecycleViewAdapter extends RecyclerView.Adapter<ModesFragment.ModesRecycleViewAdapter.ViewHolder> {
        List<ModeItem> modeList;

        @Override
        public ModesFragment.ModesRecycleViewAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View mintListCardView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.modes_layout, parent, false);
            return new ModesFragment.ModesRecycleViewAdapter.ViewHolder(mintListCardView);
        }

        @SuppressLint("SetTextI18n")
        @Override
        public void onBindViewHolder(ModesFragment.ModesRecycleViewAdapter.ViewHolder holder, final int position) {
            ModeItem m = modeList.get(position);
            holder.modeImage.setImageResource(m.getModeRes());
            holder.modeText.setText(m.getModeName());
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
//                    if(position==0)
//                    getActivity().getSupportFragmentManager().beginTransaction().replace(fragmentPlaceholder, new PortraitFragment()).addToBackStack("sp_mode").commit();
//                    else if(position == 1)
//                        getActivity().getSupportFragmentManager().beginTransaction().replace(fragmentPlaceholder, new SharpenFragment()).addToBackStack("sp_mode").commit();
//                    else if(position == 2)
//                        getActivity().getSupportFragmentManager().beginTransaction().replace(fragmentPlaceholder, new BrightnessFragment()).addToBackStack("sp_mode").commit();
//                    else if(position == 3)
//                        getActivity().getSupportFragmentManager().beginTransaction().replace(fragmentPlaceholder, new ContrastFragment()).addToBackStack("sp_mode").commit();
                    if(position==0)
                        replaceFragmentWithAnimation(new PortraitFragment(),"sp_mode");
                    else if(position == 1)
                        replaceFragmentWithAnimation(new SharpenFragment(),"sp_mode");
                    else if(position == 2)
                        replaceFragmentWithAnimation(new BrightnessFragment(),"sp_mode");
                    else if(position == 3)
                        replaceFragmentWithAnimation(new ContrastFragment(),"sp_mode");

                }
            });
        }
        public void replaceFragmentWithAnimation(android.support.v4.app.Fragment fragment, String tag){
            FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
            transaction.setCustomAnimations(R.anim.enter_from_left, R.anim.exit_to_right, R.anim.enter_from_right, R.anim.exit_to_left);
            transaction.replace(fragmentPlaceholder, fragment);
            transaction.addToBackStack(tag);
            transaction.commit();
        }
        @Override
        public int getItemCount() {
            return modeList.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            ImageView modeImage;
            TextView modeText;

            public ViewHolder(View view) {
                super(view);
                modeImage = view.findViewById(R.id.modeImage);
                modeText = view.findViewById(R.id.modeText);
            }
        }

        public ModesRecycleViewAdapter(List<ModeItem> modeList) {
            this.modeList = modeList;
        }
    }

}
