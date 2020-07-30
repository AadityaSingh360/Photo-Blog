package com.example.photoblog;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import id.zelory.compressor.Compressor;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.style.MetricAffectingSpan;
import android.text.style.TypefaceSpan;
import android.util.Log;
import android.util.LruCache;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Random;
import java.util.UUID;

public class PostBlogActivity extends AppCompatActivity {
    private ImageView postImage;
    private Button post_button;
    private EditText post_desc;
    private ProgressBar postUpload;
    private Uri postImageuri;
    private FirebaseFirestore firestore;
    private StorageReference storageReference;
    private FirebaseAuth mAuth;
    private String getDownloadUri;
    private String userId;
    private String randName;
    Bitmap compressedImageFile;
    private static final int MAX_LENGTH = 100;
    private String downloadthumbUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_blog);
        Toolbar posttoolbar = findViewById(R.id.post_toolbar);
        setSupportActionBar(posttoolbar);
        SpannableString s = new SpannableString("Photo Blog");
        getSupportActionBar().setTitle(s);

        postImage = (ImageView) findViewById(R.id.post);
        post_button = (Button) findViewById(R.id.post_button);
        post_desc = (EditText) findViewById(R.id.post_desc);
        postUpload = (ProgressBar) findViewById(R.id.postUpload);
        mAuth = FirebaseAuth.getInstance();

        userId = mAuth.getCurrentUser().getUid();

        firestore = FirebaseFirestore.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference();

        postImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CropImage.activity()
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .setMinCropResultSize(512, 512)
                        .setAspectRatio(3, 2)
                        .start(PostBlogActivity.this);
            }
        });

        post_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String desc = post_desc.getText().toString();

                if (!TextUtils.isEmpty(desc) && postImageuri != null) {
                    postUpload.setVisibility(View.VISIBLE);
                    randName = UUID.randomUUID().toString();
                    File newThumbFile = new File(postImageuri.getPath());
                    try {

                        compressedImageFile = new Compressor(PostBlogActivity.this)
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
                    final StorageReference filePath = storageReference.child("Posts").child(randName + ".jpg");
                    UploadTask uploadTask = storageReference.child("Posts/thumbs")
                            .child(randName + ".jpg").putBytes(thumbData);
                    uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(final UploadTask.TaskSnapshot taskSnapshot) {
                            downloadthumbUri = taskSnapshot.getMetadata().getReference().getDownloadUrl().toString();
                            filePath.putFile(postImageuri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                                    if (task.isSuccessful()) {
                                        filePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                            @Override
                                            public void onSuccess(Uri uri) {
                                                getDownloadUri = uri.toString();
                                                HashMap<String, Object> usermap = new HashMap<>();
                                                usermap.put("image_url", getDownloadUri);
                                                usermap.put("thumbnail", downloadthumbUri);
                                                usermap.put("description", desc);
                                                usermap.put("userId", userId);
                                                usermap.put("timestamp", FieldValue.serverTimestamp());
                                                firestore.collection("Posts").add(usermap).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<DocumentReference> task) {
                                                        if (task.isSuccessful()) {
                                                            Intent intent = new Intent(PostBlogActivity.this, MainActivity.class);
                                                            startActivity(intent);
                                                            finish();
                                                        } else {
                                                            String error = task.getException().getMessage();
                                                            Toast.makeText(PostBlogActivity.this, "Error: " + error, Toast.LENGTH_SHORT).show();
                                                        }
                                                    }
                                                });
                                            }
                                        });
                                    } else {
                                        String error = task.getException().getMessage();
                                        Toast.makeText(PostBlogActivity.this, "Error: " + error, Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });

                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            String error = e.getMessage();
                            Toast.makeText(PostBlogActivity.this, "Error: " + error, Toast.LENGTH_SHORT).show();
                        }
                    });

                }
            }
        });

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                postImageuri = result.getUri();
                postImage.setImageURI(postImageuri);
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }

    public static String random() {
        Random generator = new Random();
        StringBuilder randomStringBuilder = new StringBuilder();
        int randomLength = generator.nextInt(MAX_LENGTH);
        char tempChar;
        for (int i = 0; i < randomLength; i++) {
            tempChar = (char) (generator.nextInt(96) + 32);
            randomStringBuilder.append(tempChar);
        }
        return randomStringBuilder.toString();
    }
    public static class TypefaceSpan extends MetricAffectingSpan {
        /** An <code>LruCache</code> for previously loaded typefaces. */
        private static LruCache<String, Typeface> sTypefaceCache =
                new LruCache<String, Typeface>(12);

        private Typeface mTypeface;

        /**
         * Load the {@link Typeface} and apply to a {@link Spannable}.
         */
//        public TypefaceSpan(Context context, String typefaceName) {
//            mTypeface = sTypefaceCache.get(typefaceName);
//
//            if (mTypeface == null) {
//                mTypeface = Typeface.createFromAsset(context.getApplicationContext()
//                        .getAssets(), String.format("fonts/%s", typefaceName));
//
//                // Cache the loaded Typeface
//                sTypefaceCache.put(typefaceName, mTypeface);
//            }
//        }

        @Override
        public void updateMeasureState(TextPaint p) {
            p.setTypeface(mTypeface);

            // Note: This flag is required for proper typeface rendering
            p.setFlags(p.getFlags() | Paint.SUBPIXEL_TEXT_FLAG);
        }

        @Override
        public void updateDrawState(TextPaint tp) {
            tp.setTypeface(mTypeface);

            // Note: This flag is required for proper typeface rendering
            tp.setFlags(tp.getFlags() | Paint.SUBPIXEL_TEXT_FLAG);
        }
    }

}
