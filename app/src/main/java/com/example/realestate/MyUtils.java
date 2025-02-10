package com.example.realestate;

import android.content.Intent;
import android.net.Uri;
import android.widget.Toast;

import android.content.Context;
import android.text.format.DateFormat;

//import java.text.DateFormat;
import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Date;

public class MyUtils {

    public static final String MESSAGE_TYPE_TEXT = "TEXT";
    public static final String MESSAGE_TYPE_IMAGE = "IMAGE";
    public static final String AD_STATUS_AVAILABLE = "AVAILABLE";
    public static final String AD_STATUS_TAKEN = "TAKEN";
    public static final String[] categories = {
            "Residential",
            "Commercial",
            "Industrial",
            "Raw Land",
            "Special Use"

    };

    public static final int[] categoryIcons = {
            R.drawable.category_residential,
            R.drawable.category_commercial,
            R.drawable.category_industrial,
            R.drawable.category_row_land,
            R.drawable.category_special_use
    };
    public  static final String USER_TYPE_GOOGLE = "Google";

    public  static final String USER_TYPE_EMAIL = "Email";

    public  static final String USER_TYPE_PHONE = "Phone";


    /**
     * A function to show Toast
     * @param context the context activity
     * @param message the message to be shown in the toast
     * */
    public static void toast(Context context, String message){
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }

    public static long timestamp(){
        return System.currentTimeMillis();
    }

    public static String formatTimeStampDate(Long timestamp){
        Calendar calendar = Calendar.getInstance(Locale.ENGLISH);
        calendar.setTimeInMillis(timestamp);

        String date = DateFormat.format("dd/MM/yyyy", calendar).toString();

        return date;
    }

    public static String formatTimeStampDateTime(Long timestamp){
        Calendar calendar = Calendar.getInstance(Locale.ENGLISH);
        calendar.setTimeInMillis(timestamp);

        String date = DateFormat.format("dd/MM/yyyy hh:mm:a", calendar).toString();

        return date;
    }

    public static void addToFavorite(Context context, String adId){

        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        if (firebaseAuth.getCurrentUser() == null){
            MyUtils.toast(context, "You're Not logged In!");
        } else {
            long timestamp = MyUtils.timestamp();

            HashMap<String, Object> hashMap = new HashMap<>();
            hashMap.put("adId", adId);
            hashMap.put("timestamp", timestamp);

            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
            ref.child(firebaseAuth.getUid()).child("Favorites").child(adId)
                    .setValue(hashMap)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {

                            MyUtils.toast(context, "Added to favorite...");
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            MyUtils.toast(context, "Failed to add to favorite due to " + e.getMessage());
                        }
                    });
        }
    }

    public static void removeFromFavorite(Context context, String adId){

        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        if (firebaseAuth.getCurrentUser() == null){

            MyUtils.toast(context, "You're not logged in!");
        } else {

            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
            ref.child(firebaseAuth.getUid()).child("Favorites").child(adId)
                    .removeValue()
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            MyUtils.toast(context, "Removed from favorites");
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            MyUtils.toast(context, "Failed to remove due to " + e.getMessage());
                        }
                    });
        }
    }



    public static String chatPath(String receiptUid, String yourUid){
        String[] arrayUids = new String[]{receiptUid, yourUid};

        Arrays.sort(arrayUids);

        String chatPath = arrayUids[0] + "_" + arrayUids[1];

        return chatPath;
    }
    public static void callIntent(Context context, String phone){
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("tel:" + Uri.encode(phone)));
        context.startActivity(intent);
    }
}
