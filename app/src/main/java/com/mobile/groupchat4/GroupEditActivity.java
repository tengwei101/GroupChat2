package com.mobile.groupchat4;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;

public class GroupEditActivity extends AppCompatActivity {
    //permission constants
    private static final int CAMERA_REQUEST_CODE = 100;
    private static final int STORAGE_REQUEST_CODE = 200;

    //image pick constant
    private static final int IMAGE_PICK_CAMERA_CODE = 300;
    private static final int IMAGE_PICK_GALLERY_CODE = 400;

    //permission arrays
    private String[] cameraPermissions;
    private String[] storagePermissions;

    //picked image uri
    private Uri image_uri = null;

    private ActionBar actionBar;

    private FirebaseAuth firebaseAuth;

    //UI VIews
    private ImageView groupIconIv;
    private EditText groupTitleEt, groupDescriptionEt;
    private FloatingActionButton updateGroupBtn;

    private ProgressDialog progressDialog;

    private String groupId;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_edit);

        actionBar = getSupportActionBar();
        actionBar.setTitle("Edit Group");
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);

        groupId = getIntent().getStringExtra("groupId");

        //init UI Views
        groupIconIv = findViewById(R.id.groupIconIv);
        groupTitleEt = findViewById(R.id.groupTitleEt);
        groupDescriptionEt = findViewById(R.id.groupDescriptionEt);
        updateGroupBtn = findViewById(R.id.updateGroupBtn);

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please wait...");
        progressDialog.setCanceledOnTouchOutside(false);

        //init permission arrays
        cameraPermissions = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};


        firebaseAuth = FirebaseAuth.getInstance();

        checkUser();

        loadGroupInfo();

        //pick image
        groupIconIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showImagePickDialog();
            }
        });

        // handle click event
        updateGroupBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startUpdatingGroup();
            }
        });
    }

    private void startUpdatingGroup() {
        String groupTitle = groupTitleEt.getText().toString().trim();
        String groupDescription = groupDescriptionEt.getText().toString().trim();
        
        //validate data
        if(TextUtils.isEmpty(groupTitle)){
            Toast.makeText(this, "Group Title is required...", Toast.LENGTH_SHORT).show();
            return;
        }

        progressDialog.setMessage("Updating Group Info");
        progressDialog.show();

        if(image_uri == null){
            //update group without icon

            HashMap<String, Object> hashMap = new HashMap<>();
            hashMap.put("groupTitle", ""+groupTitle);
            hashMap.put("groupDescription", ""+groupDescription);

            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups");
            ref.child(groupId).updateChildren(hashMap)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            //updated...
                            progressDialog.dismiss();
                            Toast.makeText(GroupEditActivity.this, "Group Info updated successfully...", Toast.LENGTH_SHORT).show();

                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressDialog.dismiss();
                            Toast.makeText(GroupEditActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();

                        }
                    });

        }
        else{
            //update group with icon

            String timestamp = ""+System.currentTimeMillis();

            String filePathAndName = "Group_Imgs/"+"image"+"_"+timestamp;

            //uplod image to firebase storage
            StorageReference storageReference = FirebaseStorage.getInstance().getReference(filePathAndName);
            storageReference.putFile(image_uri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            //image uploaded
                            //get url
                            Task<Uri> p_uriTask = taskSnapshot.getStorage().getDownloadUrl();
                            while(!p_uriTask.isSuccessful());

                            Uri p_downloadUri = p_uriTask.getResult();
                            if(p_uriTask.isSuccessful()){

                                HashMap<String, Object> hashMap = new HashMap<>();
                                hashMap.put("groupTitle", ""+groupTitle);
                                hashMap.put("groupDescription", ""+groupDescription);
                                hashMap.put("groupIcon", ""+p_downloadUri);

                                DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups");
                                ref.child(groupId).updateChildren(hashMap)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void unused) {
                                                //updated...
                                                progressDialog.dismiss();
                                                Toast.makeText(GroupEditActivity.this, "Group Info updated successfully...", Toast.LENGTH_SHORT).show();

                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                progressDialog.dismiss();
                                                Toast.makeText(GroupEditActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();

                                            }
                                        });

                            }
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {

                            //failed image upload
                            progressDialog.dismiss();
                            Toast.makeText(GroupEditActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });

        }
    }

    private void loadGroupInfo() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups");
        ref.orderByChild("groupId").equalTo(groupId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot ds:dataSnapshot.getChildren()){

                    //get group info
                    String groupId = ""+ds.child("groupId").getValue();
                    String groupTitle = ""+ds.child("groupTitle").getValue();
                    String groupDescription = ""+ds.child("groupDescription").getValue();
                    String groupIcon = ""+ds.child("groupIcon").getValue();
                    String createdBy = ""+ds.child("createdBy").getValue();
                    String timestamp = ""+ds.child("timestamp").getValue();

                    //convert time stamp to dd/mm/yyyy hh:mm am/pm
                    Calendar cal = Calendar.getInstance(Locale.ENGLISH);
                    cal.setTimeInMillis(Long.parseLong(timestamp));
                    String dateTime = DateFormat.format("dd/MM/yyyy hh:mm aa", cal).toString();

                    //set group info
                    groupTitleEt.setText(groupTitle);
                    groupDescriptionEt.setText(groupDescription);

                    try{

                        Picasso.get().load(groupIcon).placeholder(R.drawable.ic_group_cyan).into(groupIconIv);
                    }catch (Exception e){
                        groupIconIv.setImageResource(R.drawable.ic_group_cyan);
                    }

                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void showImagePickDialog() {
        //options to pick image from
        String[] options = {"Camera", "Gallery"};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Pick Image")
                .setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int which) {
                        if (which == 0) {
                            //camera clicked
                            if(!checkCameraPermission()){
                                requestCameraPermission();
                            }
                            else{
                                pickFromCamera();
                            }
                        }
                        else{
                            //galery clicked
                            if(!checkStoragePermission()){
                                requestStoragePermission();
                            }
                            else{
                                pickFromGallery();
                            }
                        }
                    }
                }).show();

    }

    private void pickFromGallery(){
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, IMAGE_PICK_GALLERY_CODE);
    }

    private void pickFromCamera(){
        ContentValues cv = new ContentValues();
        cv.put(MediaStore.Images.Media.TITLE, "Group Image Icon Title");
        cv.put(MediaStore.Images.Media.DESCRIPTION, "Group Image Icon Description");
        image_uri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, cv);

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, image_uri);
        startActivityForResult(intent, IMAGE_PICK_CAMERA_CODE);
    }

    private boolean checkStoragePermission(){
        boolean result = ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) == (PackageManager.PERMISSION_GRANTED);

        return result;
    }

    private void requestStoragePermission(){
        ActivityCompat.requestPermissions(this, storagePermissions, STORAGE_REQUEST_CODE);
    }

    private boolean checkCameraPermission(){
        boolean result = ContextCompat.checkSelfPermission(this,
                Manifest.permission.CAMERA) == (PackageManager.PERMISSION_GRANTED);
        boolean result1 = ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) == (PackageManager.PERMISSION_GRANTED);
        return result && result1;
    }

    private void requestCameraPermission(){
        ActivityCompat.requestPermissions(this, cameraPermissions, CAMERA_REQUEST_CODE);
    }

    private void checkUser() {

        FirebaseUser user = firebaseAuth.getCurrentUser();
        if(user != null){
            actionBar.setSubtitle(user.getEmail());
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case CAMERA_REQUEST_CODE:{
                if(grantResults.length >0){
                    boolean cameraAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean storageAccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED;

                    if(cameraAccepted && storageAccepted){
                        pickFromCamera();
                    }
                    else{
                        //both or one is denied
                        Toast.makeText(this, "Camera & Storage permission are required", Toast.LENGTH_SHORT).show();
                    }
                }
            }
            break;

            case STORAGE_REQUEST_CODE:{
                if(grantResults.length >0){
                    boolean storageAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    if(storageAccepted){
                        //permission allowed
                        pickFromGallery();
                    }
                    else{
                        //permission denied
                        Toast.makeText(this, "Storage permission are required", Toast.LENGTH_SHORT).show();

                    }
                }
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        //handle image pick result
        if(resultCode == RESULT_OK){
            if(requestCode == IMAGE_PICK_GALLERY_CODE){
                //was picked form gallery
                image_uri = data.getData();

                //set to imageView
                groupIconIv.setImageURI(image_uri);
            }
            else if(requestCode == IMAGE_PICK_CAMERA_CODE){
                //was picked form camera


                //set to imageView
                groupIconIv.setImageURI(image_uri);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }
}