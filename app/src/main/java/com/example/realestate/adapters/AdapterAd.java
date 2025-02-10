package com.example.realestate.adapters;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.realestate.FilterAd;
import com.example.realestate.MyUtils;
import com.example.realestate.R;
import com.example.realestate.controllers.AdDetailsActivity;
import com.example.realestate.databinding.RowAdBinding;
import com.example.realestate.models.ModelAd;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class AdapterAd extends RecyclerView.Adapter<AdapterAd.HolderAd> implements Filterable {
    private RowAdBinding binding;
    private static final String TAG = "ADAPTER_AD_TAG";
    private FirebaseAuth firebaseAuth;
    private Context context;
    public ArrayList<ModelAd> adArrayList;
    private ArrayList<ModelAd> filterList;
    private FilterAd filter;

    public AdapterAd(Context context, ArrayList<ModelAd> adArrayList) {
        this.context = context;
        this.adArrayList = adArrayList;
        this.filterList = adArrayList;

        firebaseAuth = FirebaseAuth.getInstance();
    }

    @NonNull
    @Override
    public HolderAd onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        binding = RowAdBinding.inflate(LayoutInflater.from(context), parent, false);
        return new HolderAd(binding.getRoot());
    }

    @Override
    public void onBindViewHolder(@NonNull HolderAd holder, int position) {
        ModelAd modelAd = adArrayList.get(position);

        String estate = modelAd.getEstate();
        String description = modelAd.getDescription();
        String address = modelAd.getAddress();
        String price = modelAd.getPrice();
        String size_per_square_meter = modelAd.getSize();
        long timeStamp = modelAd.getTimestamp();
        String formattedDate = MyUtils.formatTimeStampDate(timeStamp);

        loadAdFirstImage(modelAd, holder);

        if (firebaseAuth.getCurrentUser() != null) {
            checkIsFavorite(modelAd, holder);
        }

        holder.estateTv.setText(estate);
        holder.descriptionTv.setText(description);
        holder.addressTv.setText(address);
        holder.priceTv.setText(price);
        holder.sizeTv.setText(size_per_square_meter);
        holder.dateTv.setText(formattedDate);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, AdDetailsActivity.class);
                intent.putExtra("adId", modelAd.getId());
                context.startActivity(intent);
            }
        });

        holder.favBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean favorite = modelAd.isFavorite();
                if (favorite) {
                    MyUtils.removeFromFavorite(context, modelAd.getId());
                }
            }
        });
    }

    private void checkIsFavorite(ModelAd modelAd, HolderAd holder) {

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(firebaseAuth.getUid()).child("Favorites").child(modelAd.getId())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        boolean favorite = snapshot.exists();

                        if (favorite) {
                            holder.favBtn.setImageResource(R.drawable.fav_yes);
                        } else {
                            holder.favBtn.setImageResource(R.drawable.fave_no);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void loadAdFirstImage(ModelAd modelAd, HolderAd holder) {
        Log.d(TAG, "loadAdFirstImage: ");

        String adId = modelAd.getId();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Ads");
        reference.child(adId).child("Images")
        //reference.child("Images").limitToFirst(1)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        for (DataSnapshot ds: snapshot.getChildren()){
                            String imageUrl = "" + ds.child("imageUrl").getValue();
                            Log.d(TAG, "onDataChange: imageUrl: " + imageUrl);

                            try {
                                Glide.with(context)
                                        .load(imageUrl)
                                        .placeholder(R.drawable.image_grey)
                                        .into(holder.imageIv);
                            } catch (Exception e){
                                Log.e(TAG, "onDataChange: ", e);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    @Override
    public int getItemCount() {
        return adArrayList.size();
    }

    @Override
    public Filter getFilter() {
        if (filter == null){
            filter = new FilterAd(this, filterList);
        }
        return filter;
    }

    class HolderAd extends RecyclerView.ViewHolder{

        ShapeableImageView imageIv;
        TextView estateTv;
        TextView descriptionTv;
        ImageButton favBtn;
        TextView addressTv;
        TextView sizeTv;
        TextView priceTv;
        TextView dateTv;
        public HolderAd(@NonNull View itemView) {
            super(itemView);

            imageIv = binding.imageIv;
            estateTv = binding.estateTv;
            descriptionTv = binding.descriptionTv;
            favBtn = binding.favBtn;
            addressTv = binding.addressTv;
            sizeTv = binding.sizeTv;
            priceTv = binding.priceTv;
            dateTv = binding.dateTv;
        }
    }
}
