package com.example.realestate.controllers;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.realestate.MyUtils;
import com.example.realestate.R;
import com.example.realestate.adapters.AdapterImageSlider;
import com.example.realestate.databinding.ActivityAdDetailsBinding;
import com.example.realestate.models.ModelAd;
import com.example.realestate.models.ModelImageSlider;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;

public class AdDetailsActivity extends AppCompatActivity {

    private ActivityAdDetailsBinding binding;
    private static final String TAG = "AD_DETAILS_TAG";

    private FirebaseAuth firebaseAuth;
    private String adId = "";
    private double adlatitude = 0;
    private double adLongitude = 0;
    private String ownerUid = null;
    private String ownerPhone = "";
    private boolean favorite = false;

    private ArrayList<ModelImageSlider> imageSliderArrayList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityAdDetailsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.toolbarEditBtn.setVisibility(View.GONE);
        binding.toolbarDeleteBtn.setVisibility(View.GONE);
        binding.chatBtn.setVisibility(View.GONE);
        binding.callBtn.setVisibility(View.GONE);

        adId = getIntent().getStringExtra("adId");
        Log.d(TAG, "onCreate: adId: " + adId);

        firebaseAuth = FirebaseAuth.getInstance();

        if (firebaseAuth.getCurrentUser() != null){
            checkIsFavorite();
        }

        loadAdDetails();
        loadAdImages();

        binding.toolbarBackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        binding.toolbarDeleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                MaterialAlertDialogBuilder materialAlertDialogBuilder = new MaterialAlertDialogBuilder(AdDetailsActivity.this);
                materialAlertDialogBuilder.setTitle("Delete Ad")
                        .setMessage("Are you sure you want to delete this Ad?")
                        .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                deleteAd();
                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        })
                        .show();
            }
        });

        binding.toolbarEditBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editOptions();
            }
        });

        binding.toolbarFavBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (favorite){
                    MyUtils.removeFromFavorite(AdDetailsActivity.this, adId);
                } else {
                    MyUtils.addToFavorite(AdDetailsActivity.this, adId);
                }
            }
        });

        binding.ownerProfileCv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        binding.chatBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AdDetailsActivity.this, ChatActivity.class);
                intent.putExtra("receiptUid", ownerUid);
                startActivity(intent);
            }
        });

        binding.callBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MyUtils.callIntent(AdDetailsActivity.this, ownerPhone);
            }
        });
    }

    private void editOptions(){
        Log.d(TAG, "editOptions: ");

        PopupMenu popupMenu = new PopupMenu(this, binding.toolbarEditBtn);

        popupMenu.getMenu().add(Menu.NONE, 0, 0, "Edit");
        popupMenu.getMenu().add(Menu.NONE, 1, 1, "Mark As Taken");

        popupMenu.show();

        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int itemId = item.getItemId();

                if (itemId == 0) {
                    Intent intent = new Intent(AdDetailsActivity.this, AdCreateActivity.class);
                    intent.putExtra("isEditMode", true);
                    intent.putExtra("", adId);
                    startActivity(intent);
                } else if(itemId == 1){
                    showMarkAsSoldDialog();
                }

                return true;
            }
        });
    }

    private void showMarkAsSoldDialog(){

        MaterialAlertDialogBuilder alertDialogBuilder = new MaterialAlertDialogBuilder(this);
        alertDialogBuilder.setTitle("Mark as Taken")
                .setMessage("Are you sure you want to mark this Ad as taken?")
                .setPositiveButton("TAKEN", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Log.d(TAG, "onClick: Taken Clicked...");

                        HashMap<String, Object> hashMap = new HashMap<>();
                        hashMap.put("status", "" + MyUtils.AD_STATUS_TAKEN);

                        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Ads");
                        ref.child(adId)
                                .updateChildren(hashMap)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void unused) {
                                        Log.d(TAG, "onSuccess: Marked as Taken");
                                        MyUtils.toast(AdDetailsActivity.this, "Marked as taken");
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.e(TAG, "onFailure: ", e);
                                        Toast.makeText(AdDetailsActivity.this, "Failed to mark as taken due to " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });
                    }
                })
                .setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Log.d(TAG, "onClick: Cancel Clicked...");
                        dialog.dismiss();
                    }
                })
                .show();
    }

    private void loadAdDetails(){
        Log.d(TAG, "loadAdDetails: ");

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Ads");
        ref.child(adId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        try {
                            ModelAd modelAd = snapshot.getValue(ModelAd.class);

                            ownerUid = modelAd.getUid();
                            String estate = modelAd.getEstate();
                            String category = modelAd.getCategory();
                            String description = modelAd.getDescription();
                            String address = modelAd.getAddress();
                            String price = modelAd.getPrice();
                            String size = modelAd.getSize();
                            adlatitude = modelAd.getLatitude();
                            adLongitude = modelAd.getLongitude();
                            long timestamp = modelAd.getTimestamp();
                            String formattedDate = MyUtils.formatTimeStampDate(timestamp);

                            if (ownerUid.equals(firebaseAuth.getUid())){

                                binding.toolbarEditBtn.setVisibility(View.VISIBLE);
                                binding.toolbarDeleteBtn.setVisibility(View.VISIBLE);

                                binding.chatBtn.setVisibility(View.GONE);
                                binding.callBtn.setVisibility(View.GONE);
                            } else {

                                binding.toolbarEditBtn.setVisibility(View.GONE);
                                binding.toolbarDeleteBtn.setVisibility(View.GONE);

                                binding.chatBtn.setVisibility(View.VISIBLE);
                                binding.callBtn.setVisibility(View.VISIBLE);
                            }

                            binding.estateTv.setText(estate);
                            binding.descriptionTv.setText(description);
                            binding.addressTv.setText(address);
                            binding.categoryTv.setText(category);
                            binding.priceTv.setText(price);
                            binding.dateTv.setText(formattedDate);

                            loadOwnerDetails();
                        } catch (Exception e) {
                            Log.e(TAG, "onDataChange: ", e);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void loadOwnerDetails(){
        Log.d(TAG, "loadOwnerDetails: ");

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(ownerUid)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String phoneCode = "" + snapshot.child("phoneCode").getValue();
                        String phoneNumber = "" + snapshot.child("phoneNumber").getValue();
                        String name = "" + snapshot.child("name").getValue();
                        String profileImageUrl = "" + snapshot.child("profileImageUrl").getValue();
                        long timestamp = (Long) snapshot.child("timestamp").getValue();

                        String formattedDate = MyUtils.formatTimeStampDate(timestamp);

                        ownerPhone = phoneCode + "" + phoneNumber;

                        binding.ownerNameTv.setText(name);
                        binding.memberSinceTv.setText(formattedDate);

                        try {
                            Glide.with(AdDetailsActivity.this)
                                    .load(profileImageUrl)
                                    .placeholder(R.drawable.black_profile)
                                    .into(binding.ownerProfileIv);
                        } catch (Exception e) {
                            Log.e(TAG, "onDataChange: ", e);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void checkIsFavorite(){
        Log.d(TAG, "checkIsFavorite: ");

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(firebaseAuth.getUid()).child("Favorites").child(adId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        favorite = snapshot.exists();

                        Log.d(TAG, "onDataChange: favorite: " + favorite);

                        if (favorite){

                            binding.toolbarFavBtn.setImageResource(R.drawable.fav_yes);
                        } else {

                            binding.toolbarFavBtn.setImageResource(R.drawable.fave_no);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void loadAdImages(){
        Log.d(TAG, "loadAdImages: ");
        imageSliderArrayList = new ArrayList<>();

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Ads");
        ref.child(adId).child("Images")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        imageSliderArrayList.clear();

                        for (DataSnapshot ds: snapshot.getChildren()){
                            ModelImageSlider modelImageSlider = ds.getValue(ModelImageSlider.class);
                            imageSliderArrayList.add(modelImageSlider);
                        }

                        AdapterImageSlider adapterImageSlider = new AdapterImageSlider(AdDetailsActivity.this, imageSliderArrayList);
                        binding.imageSliderVp.setAdapter(adapterImageSlider);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void deleteAd(){
        Log.d(TAG, "deleteAd: ");

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Ads");
        ref.child(adId)
                .removeValue()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Log.d(TAG, "onSuccess: Deleted");
                        MyUtils.toast(AdDetailsActivity.this, "Deleted");
                        finish();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                        Log.e(TAG, "onFailure: ", e);
                        MyUtils.toast(AdDetailsActivity.this, "Failed to delete the Ad due to " + e.getMessage());
                    }
                });
    }
}