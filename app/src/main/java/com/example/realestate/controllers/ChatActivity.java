package com.example.realestate.controllers;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.PopupMenu;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.realestate.MyUtils;
import com.example.realestate.R;
import com.example.realestate.adapters.AdapterChat;
import com.example.realestate.databinding.ActivityChatBinding;
import com.example.realestate.models.ModelChat;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ChatActivity extends AppCompatActivity {

    private ActivityChatBinding binding;
    private static final String TAG = "CHAT_TAG";
    private ProgressDialog progressDialog;
    private FirebaseAuth firebaseAuth;
    private String receiptUid = "";
    private String myUid = "";
    private String chatPath = "";
    private Uri imageUri = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        firebaseAuth = FirebaseAuth.getInstance();

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please wait...");
        progressDialog.setCanceledOnTouchOutside(false);

        receiptUid = getIntent().getStringExtra("receiptUid");
        myUid = firebaseAuth.getUid();
        chatPath = MyUtils.chatPath(receiptUid, myUid);
        Log.d(TAG, "onCreate: receiptUid: " + receiptUid);
        Log.d(TAG, "onCreate: myUid: " + myUid);
        Log.d(TAG, "onCreate: chatPath: " + chatPath);

        loadReceiptDetails();
        loadMessages();

        binding.toolbarBackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        binding.attachFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imagePickDialog();
            }
        });

        binding.sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validateData();
            }
        });
    }

    private void loadReceiptDetails(){
        Log.d(TAG, "loadReceiptDetails: ");

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(receiptUid)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        try {
                            String name = "" + snapshot.child("name").getValue();
                            String profileImageUrl = "" + snapshot.child("profileImageUrl").getValue();

                            Log.d(TAG, "onDataChange: name: " + name);
                            Log.d(TAG, "onDataChange: profileImageUrl: " + profileImageUrl);

                            binding.toolbarTitleTv.setText(name);

                            try {
                                Glide.with(ChatActivity.this)
                                        .load(profileImageUrl)
                                        .placeholder(R.drawable.black_profile)
                                        .error(R.drawable.broken_image)
                                        .into(binding.toolbarProfileIv);
                            } catch (Exception e) {
                                Log.e(TAG, "onDataChange: ", e);
                            }
                        } catch (Exception e){
                            Log.e(TAG, "onDataChange: ", e);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void loadMessages(){
        Log.d(TAG, "loadMessages: ");

        ArrayList<ModelChat> chatArrayList = new ArrayList<>();

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Chats");
        ref.child(chatPath)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        chatArrayList.clear();

                        for (DataSnapshot ds: snapshot.getChildren()){
                            try {
                                ModelChat modelChat = ds.getValue(ModelChat.class);

                                chatArrayList.add(modelChat);
                            } catch (Exception e){
                                Log.e(TAG, "loadMessages:onDataChange: ", e);
                            }
                        }

                        AdapterChat adapterChat = new AdapterChat(ChatActivity.this, chatArrayList);
                        binding.chatRv.setAdapter(adapterChat);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void imagePickDialog(){

        PopupMenu popupMenu = new PopupMenu(this, binding.attachFab);

        popupMenu.getMenu().add(Menu.NONE, 1, 1, "Camera");
        popupMenu.getMenu().add(Menu.NONE, 2, 2, "Gallery");

        popupMenu.show();

        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int itemId = item.getItemId();
                if (itemId == 1){
                    Log.d(TAG, "onMenuItemClick: Camera click, check if camera permissions are granted or not");
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        requestCameraPermissions.launch(new String[]{Manifest.permission.CAMERA,});
                    } else {
                        requestCameraPermissions.launch(new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE});
                    }
                } else if (itemId == 2) {
                    Log.d(TAG, "onMenuItemClick: ");

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        pickImageGallery();
                    } else {
                        requestStoragePermission.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE);
                    }
                }
                return true;
            }
        });
    }

    private ActivityResultLauncher<String[]> requestCameraPermissions = registerForActivityResult(
            new ActivityResultContracts.RequestMultiplePermissions(),
            new ActivityResultCallback<Map<String, Boolean>>() {
                @Override
                public void onActivityResult(Map<String, Boolean> result) {
                    Log.d(TAG, "onActivityResult: " + result);

                    boolean areAllGranted = true;
                    for (Boolean isGranted: result.values()){
                        areAllGranted = areAllGranted && isGranted;
                    }

                    if (areAllGranted) {
                        Log.d(TAG, "onActivityResult: ");
                        pickImageCamera();
                    } else {
                        Log.d(TAG, "onActivityResult: Camera or Storage or Both Permissions denied...");
                        MyUtils.toast(ChatActivity.this, "Camera or Storage or Both Permissions denied...");
                    }
                }
            }
    );

    private ActivityResultLauncher<String> requestStoragePermission = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            new ActivityResultCallback<Boolean>() {
                @Override
                public void onActivityResult(Boolean isGranted) {
                    Log.d(TAG, "onActivityResult: isGranted: " + isGranted);

                    if (isGranted) {
                        pickImageGallery();
                    } else {
                        MyUtils.toast(ChatActivity.this, "Permission denied...");
                    }
                }
            }
    );

    private void pickImageCamera(){
        Log.d(TAG, "pickImageCamera: ");

        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.Images.Media.TITLE, "CHAT_IMAGE_TEMP");
        contentValues.put(MediaStore.Images.Media.TITLE, "CHAT_IMAGE_TEMP_DESCRIPTION");

        imageUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        cameraActivityResultLauncher.launch(intent);
    }

    private ActivityResultLauncher<Intent> cameraActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Log.d(TAG, "onActivityResult: imageUri: " + imageUri);
                        uploadToFirebaseStorage();
                    } else {
                        MyUtils.toast(ChatActivity.this, "Cancelled");
                    }
                }
            }
    );

    private void pickImageGallery(){
        Log.d(TAG, "pickImageGallery: ");

        Intent intent = new Intent(Intent.ACTION_PICK);

        intent.setType("image/*");
        galleryActivityResultLauncher.launch(intent);
    }

    private ActivityResultLauncher<Intent> galleryActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {

                    if (result.getResultCode() == Activity.RESULT_OK){
                        Intent data = result.getData();

                        imageUri = data.getData();
                        Log.d(TAG, "onActivityResult: imageUri" + imageUri);
                        uploadToFirebaseStorage();
                    } else {
                        MyUtils.toast(ChatActivity.this, "Cancelled");
                    }
                }
            }
    );

    private void uploadToFirebaseStorage(){
        Log.d(TAG, "uploadToFirebaseStorage: ");

        progressDialog.setMessage("Uploading image...");
        progressDialog.show();

        long timestamp = MyUtils.timestamp();
        String filePathAndName = "ChatImages/" + timestamp;

        StorageReference storageReference = FirebaseStorage.getInstance().getReference(filePathAndName);
        storageReference.putFile(imageUri)
                .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {
                        double progress = (100.0 * snapshot.getBytesTransferred()) / snapshot.getTotalByteCount();

                        progressDialog.setMessage("Uploading image. Progress: " + (int) progress + "%");
                    }
                })
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();

                        while (!uriTask.isSuccessful());
                        String imageUrl = uriTask.getResult().toString();

                        if (uriTask.isSuccessful()){
                            sendMessage(MyUtils.MESSAGE_TYPE_IMAGE, imageUrl, timestamp);
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "onFailure: ", e);
                        progressDialog.dismiss();
                        MyUtils.toast(ChatActivity.this, "Failed to upload the image due to " + e.getMessage());
                    }
                });
    }

    private void validateData(){
        Log.d(TAG, "validateData: ");

        String message = binding.messageEt.getText().toString().trim();
        long timestamp = MyUtils.timestamp();

        if (message.isEmpty()) {
            MyUtils.toast(this, "Enter message to send...");
        } else {
            sendMessage(MyUtils.MESSAGE_TYPE_TEXT, message, timestamp);
        }
    }

    private void sendMessage(String messageType, String message, long timestamp) {
        Log.d(TAG, "sendMessage: messageType: " + messageType);
        Log.d(TAG, "sendMessage: message: " + message);
        Log.d(TAG, "sendMessage: timestamp: " + timestamp);

        progressDialog.setMessage("Sending message...");
        progressDialog.show();

        DatabaseReference refChat = FirebaseDatabase.getInstance().getReference("Chats");

        String keyId = "" + refChat.push().getKey();

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("messageId", "" + keyId);
        hashMap.put("messageType", "" + messageType);
        hashMap.put("message", "" + message);
        hashMap.put("fromUid", "" + myUid);
        hashMap.put("toUid", "" + receiptUid);
        hashMap.put("timestamp", timestamp);

        refChat.child(chatPath)
                .child(keyId)
                .setValue(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {

                        binding.messageEt.setText("");
                        progressDialog.dismiss();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "onFailure: ", e);
                        progressDialog.dismiss();
                        MyUtils.toast(ChatActivity.this, "Failed to send due to " + e.getMessage());
                    }
                });
    }
}