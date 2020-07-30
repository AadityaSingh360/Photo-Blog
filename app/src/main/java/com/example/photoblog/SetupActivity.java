package com.example.photoblog;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import de.hdodenhof.circleimageview.CircleImageView;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;
import java.util.Map;

public class SetupActivity extends AppCompatActivity {

    private CircleImageView myImage;
    private Uri mainImage = null;
    private EditText imageName;
    private Button ImageButton;
    private StorageReference mStorageRef;
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firestore;
    private String userid;
    ProgressBar progressBar;
    String string_uri;
    boolean isChanged = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);

        Toolbar setuptoolbar = findViewById(R.id.account_setup);
        setSupportActionBar(setuptoolbar);
        getSupportActionBar().setTitle("Account Setup");

        myImage = findViewById(R.id.profile_image);
        imageName = (EditText) findViewById(R.id.image_name);
        ImageButton = (Button) findViewById(R.id.save_image);
        progressBar = (ProgressBar) findViewById(R.id.setup_progressBar);

        firebaseAuth = FirebaseAuth.getInstance();
        mStorageRef = FirebaseStorage.getInstance().getReference();
        firestore = FirebaseFirestore.getInstance();

        userid = firebaseAuth.getCurrentUser().getUid();

        progressBar.setVisibility(View.VISIBLE);
        ImageButton.setEnabled(false);
        imageName.setEnabled(false);

        firestore.collection("Users").document(userid).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                if (task.isSuccessful()) {
                    if (task.getResult().exists()) {
                        String dname = task.getResult().getString("name");
                        String image_url = task.getResult().getString("image");
                        mainImage = Uri.parse(image_url);
                        imageName.setText(dname);
                        RequestOptions placeholderrequest = new RequestOptions();
                        placeholderrequest.placeholder(R.drawable.default_image);
                        Glide.with(SetupActivity.this).setDefaultRequestOptions(placeholderrequest).load(image_url).into(myImage);
                    }
                } else {
                    String error = task.getException().getMessage().toString();
                    Toast.makeText(SetupActivity.this, "Retrieve error:" + error, Toast.LENGTH_SHORT).show();
                }

                progressBar.setVisibility(View.INVISIBLE);
                ImageButton.setEnabled(true);
                imageName.setEnabled(true);
            }
        });

        ImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String name = imageName.getText().toString();
                if (!TextUtils.isEmpty(name)) {
                    userid = firebaseAuth.getCurrentUser().getUid();
                    progressBar.setVisibility(View.VISIBLE);
                    final StorageReference image_path = mStorageRef.child("profile_images").child(userid + ".jpg");
                    image_path.putFile(mainImage).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                            if (task.isSuccessful()) {
                                    image_path.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                        @Override
                                        public void onSuccess(Uri uri) {
                                            string_uri = uri.toString();
                                            Map<String, String> userMap = new HashMap<>();
                                            userMap.put("name", name);
                                            userMap.put("image", string_uri);
                                            Log.i("url: ", string_uri);
                                            firestore.collection("Users").document(userid).set(userMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if (task.isSuccessful()) {
                                                        Intent intent = new Intent(SetupActivity.this, MainActivity.class);
                                                        startActivity(intent);
                                                        finish();
                                                        Toast.makeText(SetupActivity.this, "User Settings uploaded", Toast.LENGTH_SHORT).show();
                                                    } else {
                                                        Toast.makeText(SetupActivity.this, "Firestore upload error", Toast.LENGTH_SHORT).show();
                                                    }
                                                }
                                            });
                                        }
                                    });
                            }else {
                                String error = task.getException().getMessage();
                                Toast.makeText(SetupActivity.this, "Error: " + error, Toast.LENGTH_SHORT).show();
                            }
                            progressBar.setVisibility(View.INVISIBLE);
                        }
                    });
                }
            }
        });

        myImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (ContextCompat.checkSelfPermission(SetupActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

                        Toast.makeText(SetupActivity.this, "Permission Denied", Toast.LENGTH_SHORT).show();
                        ActivityCompat.requestPermissions(SetupActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
                    } else {

                        CropImage.activity()
                                .setGuidelines(CropImageView.Guidelines.ON)
                                .setAspectRatio(1, 1)
                                .start(SetupActivity.this);
                    }
                } else {
                    CropImage.activity()
                            .setGuidelines(CropImageView.Guidelines.ON)
                            .setAspectRatio(1, 1)
                            .start(SetupActivity.this);
                }

            }
        });
        isChanged = true;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                mainImage = result.getUri();
                myImage.setImageURI(mainImage);
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }
}
