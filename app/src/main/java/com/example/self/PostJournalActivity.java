package com.example.self;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.Date;

import model.Journel;
import util.JournalApi;

public class PostJournalActivity extends AppCompatActivity implements View.OnClickListener {
    public static final int GALLERY_CODE=1;
    private Uri imageUri;
    private Button saveButton;
    private ProgressBar progressBar;
    private EditText titleEditText;
    private EditText thoughtEditText;
    private ImageView addImageButton;
    private TextView usernameTextView;
    private TextView dateTextView;
    private ImageView postImageview;


    private String currentUsername;
    private String currenUserId;

    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener authStateListener;
    private FirebaseUser user;

    // connection to firestore
    private FirebaseFirestore db=FirebaseFirestore.getInstance();
    private StorageReference storageReference; // this reference is for image that is gonna be stored in
    //firebase cloud storage

    private CollectionReference collectionReference=db.collection("Journal");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_journal);
        storageReference= FirebaseStorage.getInstance().getReference();

        firebaseAuth=FirebaseAuth.getInstance();

        saveButton=findViewById(R.id.post_save_button);
        saveButton.setOnClickListener(this);

        addImageButton=findViewById(R.id.postCameraButton);
        addImageButton.setOnClickListener(this);

        postImageview=findViewById(R.id.imageView3);
        progressBar=findViewById(R.id.post_progressBar);
        titleEditText=findViewById(R.id.post_title_et);
        thoughtEditText=findViewById(R.id.post_description_et);
        usernameTextView=findViewById(R.id.post_username_textview);
        dateTextView=findViewById(R.id.post_date_textview);
        progressBar.setVisibility(View.INVISIBLE);

        if(JournalApi.getInstance()!=null){
            currentUsername=JournalApi.getInstance().getUsername();
            currenUserId=JournalApi.getInstance().getUserId();

            usernameTextView.setText(currentUsername);
        }
        authStateListener=new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                user=firebaseAuth.getCurrentUser();
                if(user!=null){

                }else{

                }
            }
        };
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.post_save_button:
                //save Journal
                saveJournal();
                break;
            case R.id.postCameraButton:
                //get image from gallery
                Intent galleryIntent=new Intent(Intent.ACTION_GET_CONTENT);// we need something from the device
                galleryIntent.setType("image/*");// we need image of any type


                startActivityForResult(galleryIntent,GALLERY_CODE);

        }
    }

    private void saveJournal() {
        // retrieve the editText text
        String title=titleEditText.getText().toString().trim();
        String thoughts=thoughtEditText.getText().toString().trim();

        progressBar.setVisibility(View.VISIBLE);

        if (!TextUtils.isEmpty(title) && !TextUtils.isEmpty(thoughts) && imageUri != null) {

            //image has to go to firebase storage as uri

            StorageReference filepath=storageReference.child("journal_images")
                    .child("my_image_"+ Timestamp.now().getSeconds());
            // journal image is the folder name
            //each image must have a unique id so timestamp is added.

            // put imageUri to the filepath
            filepath.putFile(imageUri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                            // we need imageUrl because we need to store that to fireStore
                            filepath.getDownloadUrl()
                                    .addOnSuccessListener(new OnSuccessListener<Uri>() {
                                        @Override
                                        public void onSuccess(Uri uri) {
                                            String imageUrl=uri.toString();  // get thr url
                                            Journel journel=new Journel();
                                            journel.setTitle(title);
                                            journel.setThought(thoughts);
                                            journel.setImageUrl(imageUrl);
                                            journel.setTimeAdded(new Timestamp(new Date()));
                                            journel.setUsername(currentUsername);
                                            journel.setUserId(currenUserId);

                                            collectionReference.add(journel).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                                @Override
                                                public void onSuccess(DocumentReference documentReference) {
                                                    progressBar.setVisibility(View.INVISIBLE);
                                                    startActivity(new Intent(PostJournalActivity.this,JournelListActivity.class));
                                                    finish();
                                                }
                                            }).addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {

                                                }
                                            });
                                        }

                                    });


                        }
                    }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    progressBar.setVisibility(View.INVISIBLE);
                }
            });


        }else{

        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==GALLERY_CODE && resultCode==RESULT_OK){
            if(data!=null){
                imageUri=data.getData();
                postImageview.setImageURI(imageUri); // show image

            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        user=firebaseAuth.getCurrentUser();
        firebaseAuth.addAuthStateListener(authStateListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(firebaseAuth!=null){
            firebaseAuth.removeAuthStateListener(authStateListener);
        }
    }
}