package com.example.realestate.fragments;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.realestate.adapters.AdapterAd;
import com.example.realestate.databinding.FragmentMyAdsFavBinding;
import com.example.realestate.models.ModelAd;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;


public class MyAdsFavFragment extends Fragment {

    private FragmentMyAdsFavBinding binding;
    private static final String TAG = "FAV_TAG";
    private Context mContext;
    private FirebaseAuth firebaseAuth;
    private ArrayList<ModelAd> adArrayList;
    private AdapterAd adapterAd;

    public MyAdsFavFragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(@NonNull Context context) {
        mContext = context;
        super.onAttach(context);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentMyAdsFavBinding.inflate(inflater,container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        firebaseAuth = FirebaseAuth.getInstance();
        loadAds();

        binding.searchEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                try {
                    String query = s.toString();
                    adapterAd.getFilter().filter(query);
                } catch (Exception e) {
                    Log.e(TAG, "onTextChanged: ", e);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    private void loadAds(){
        Log.d(TAG, "loadAds: ");

        adArrayList = new ArrayList<>();

        DatabaseReference favRef = FirebaseDatabase.getInstance().getReference("Ads");
        favRef.child(firebaseAuth.getUid()).child("Favorites")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        adArrayList.clear();

                        for (DataSnapshot ds: snapshot.getChildren()){
                            String adId = "" + ds.child("adId").getValue();
                            Log.d(TAG, "onDataChange: adId: " + adId);

                            DatabaseReference adRef = FirebaseDatabase.getInstance().getReference("Ads");
                            adRef.child(adId)
                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot) {

                                            try {
                                                ModelAd modelAd = snapshot.getValue(ModelAd.class);
                                                adArrayList.add(modelAd);
                                            } catch (Exception e){
                                                Log.e(TAG, "onDataChange: ", e);
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {

                                        }
                                    });

                        }

                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                adapterAd = new AdapterAd(mContext, adArrayList);
                                binding.adsRv.setAdapter(adapterAd);
                            }
                        }, 500);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }
}