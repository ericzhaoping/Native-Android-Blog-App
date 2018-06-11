package com.spantom.redcloud.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.spantom.redcloud.MainActivity;
import com.spantom.redcloud.R;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import id.zelory.compressor.Compressor;

public class NewMemoActivity extends AppCompatActivity {

    private Toolbar newProductToolbar;

    private ImageView newMemoImage;
    private EditText newMemoTitle, newMemoTagline, newMemoDescription, newMemoLocation;
    private Button newMemoBtn;

    private Uri memoImageUri = null;

    private ProgressBar newMemoProgress;

    private StorageReference storageReference;
    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth firebaseAuth;

    private String current_user_id;

    private Bitmap compressedImageFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_memo);

        storageReference = FirebaseStorage.getInstance().getReference();
        firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();

        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .build();
        firebaseFirestore.setFirestoreSettings(settings);

        current_user_id = firebaseAuth.getCurrentUser().getUid();

        getSupportActionBar().setTitle("Add New Memo");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        newMemoImage = findViewById(R.id.memo_image);
        newMemoTitle = findViewById(R.id.memo_title);
        newMemoTagline = findViewById(R.id.memo_tagline);
        newMemoDescription = findViewById(R.id.memo_description);
        newMemoLocation = findViewById(R.id.memo_location);
        newMemoBtn = findViewById(R.id.save_btn);
        newMemoProgress = findViewById(R.id.new_memo_progress);

        newMemoImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                CropImage.activity()
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .setMinCropResultSize(512, 512)
                        .setAspectRatio(1, 1)
                        .start(NewMemoActivity.this);

            }
        });

        newMemoBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                postMemo();
            }
        });


    }

    public void postMemo() {

        final String title = newMemoTitle.getText().toString();
        final String tagline = newMemoTagline.getText().toString();
        final String description = newMemoDescription.getText().toString();
        final String location = newMemoLocation.getText().toString();

        if(!TextUtils.isEmpty(title) && !TextUtils.isEmpty(tagline) && !TextUtils.isEmpty(description)
                && !TextUtils.isEmpty(location)) {

            newMemoProgress.setVisibility(View.VISIBLE);

            final String randomName = UUID.randomUUID().toString();

            // PHOTO UPLOAD
            File newImageFile = new File(memoImageUri.getPath());
            try {

                compressedImageFile = new Compressor(NewMemoActivity.this)
                        .setMaxHeight(720)
                        .setMaxWidth(720)
                        .setQuality(50)
                        .compressToBitmap(newImageFile);

            } catch (IOException e) {
                e.printStackTrace();
            }

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            compressedImageFile.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            byte[] imageData = baos.toByteArray();

            // PHOTO UPLOAD

            UploadTask filePath = storageReference.child("memo_images").child(randomName + ".jpg").putBytes(imageData);
            filePath.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull final Task<UploadTask.TaskSnapshot> task) {

                    final String downloadUri = task.getResult().getDownloadUrl().toString();

                    if(task.isSuccessful()){

                        File newThumbFile = new File(memoImageUri.getPath());
                        try {

                            compressedImageFile = new Compressor(NewMemoActivity.this)
                                    .setMaxHeight(100)
                                    .setMaxWidth(100)
                                    .setQuality(1)
                                    .compressToBitmap(newThumbFile);

                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        compressedImageFile.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                        byte[] thumbData = baos.toByteArray();

                        UploadTask uploadTask = storageReference.child("memo_images/thumbs")
                                .child(randomName + ".jpg").putBytes(thumbData);

                        uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                                String downloadthumbUri = taskSnapshot.getDownloadUrl().toString();

                                Map<String, Object> memoMap = new HashMap<>();
                                memoMap.put("image_url", downloadUri);
                                memoMap.put("image_thumb", downloadthumbUri);
                                memoMap.put("title", title);
                                memoMap.put("tagline", tagline);
                                memoMap.put("description", description);
                                memoMap.put("location", location);
                                memoMap.put("user_id", current_user_id);
                                memoMap.put("timestamp", FieldValue.serverTimestamp());

                                firebaseFirestore.collection("Memos").add(memoMap).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                                    @Override
                                    public void onComplete(@NonNull Task<DocumentReference> task) {

                                        if(task.isSuccessful()){

                                            Toast.makeText(NewMemoActivity.this, "Memo was added successfully", Toast.LENGTH_LONG).show();
                                            Intent mainIntent = new Intent(NewMemoActivity.this, MainActivity.class);
                                            startActivity(mainIntent);
                                            finish();

                                        } else {


                                        }

                                        newMemoProgress.setVisibility(View.INVISIBLE);

                                    }
                                });

                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {

                                //Error handling

                            }
                        });


                    } else {

                        newMemoProgress.setVisibility(View.INVISIBLE);

                    }

                }
            });


        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {

                memoImageUri = result.getUri();
                newMemoImage.setImageURI(memoImageUri);

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {

                Exception error = result.getError();

            }
        }

    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // If the nav drawer is open, hide action items related to the content view
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.menu_new_memo, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {

            case R.id.action_save_btn:

                postMemo();

                return true;


            default:
                return false;


        }

    }

}
