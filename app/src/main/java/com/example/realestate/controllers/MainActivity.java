package com.example.realestate.controllers;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import com.example.realestate.R;
import com.example.realestate.databinding.ActivityMainBinding;
import com.example.realestate.fragments.ChatListFragment;
import com.example.realestate.fragments.HomeFragment;
import com.example.realestate.fragments.MyAdsFragment;
import com.example.realestate.fragments.ProfileFragment;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity {
   // view binding
    private ActivityMainBinding binding;

    private FirebaseAuth firebaseAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        firebaseAuth = FirebaseAuth.getInstance();

        if (firebaseAuth.getCurrentUser() == null){
            startLoginOptionsActivity();
        }
        // by default
        showHomeFragment();
        //showChatListFragment();
        binding.bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();

                if (itemId == R.id.item_home){
                    showHomeFragment();
                }else if (itemId == R.id.item_chats){
                    showChatListFragment();
                }else if (itemId == R.id.item_fav){
                    showFavoriteListFragment();
                }else if (itemId == R.id.item_profile){
                    showProfileFragment();
                }
                return false;
            }
        });

        binding.sellFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //startActivity(new Intent(MainActivity.this, AdCreateActivity.class));

                Intent intent = new Intent(MainActivity.this, AdCreateActivity.class);
                intent.putExtra("isEditMode", true);
                startActivity(intent);
            }
        });
    }



    @SuppressLint("SetTextI18n")
    private void showHomeFragment(){
        binding.toolbarTitleTv.setText("Home");

        HomeFragment homeFragment = new HomeFragment();
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(binding.fragmentsFl.getId(), homeFragment, "HomeFragment");
        fragmentTransaction.commit();
    }

    @SuppressLint("SetTextI18n")
    private void showChatListFragment(){
        binding.toolbarTitleTv.setText("Chats");

        ChatListFragment chatListFragment = new ChatListFragment();
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(binding.fragmentsFl.getId(), chatListFragment, "ChatListFragment");
        fragmentTransaction.commit();
    }

    @SuppressLint("SetTextI18n")
    private void showFavoriteListFragment(){
        binding.toolbarTitleTv.setText("Favorites");

        MyAdsFragment myAdsFragment = new MyAdsFragment();
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(binding.fragmentsFl.getId(), myAdsFragment, "MyAdsFragment");
        fragmentTransaction.commit();
    }
    @SuppressLint("SetTextI18n")
    private void showProfileFragment(){
        binding.toolbarTitleTv.setText("Profile");

        ProfileFragment profileFragment = new ProfileFragment();
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(binding.fragmentsFl.getId(), profileFragment, "ProfileFragment");
        fragmentTransaction.commit();
    }

    private void startLoginOptionsActivity() {
        startActivity(new Intent(this, LoginOptionsActivity.class));
    }
}