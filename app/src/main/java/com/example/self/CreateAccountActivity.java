package com.example.self;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import org.w3c.dom.Text;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import util.JournalApi;

public class CreateAccountActivity extends AppCompatActivity {
    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener authStateListener;
    private FirebaseUser currentUser; // to fetch the user who has been currently logged in

    // Firestore connection
    private FirebaseFirestore db=FirebaseFirestore.getInstance();

    private Button createButton;
    private EditText usernameEditText;
    private EditText passwordEditText;
    private EditText emailEditText;
    private ProgressBar progressBar;

    private CollectionReference collectionReference=db.collection("Users");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_account);

        firebaseAuth=FirebaseAuth.getInstance();

        progressBar= findViewById(R.id.create_acc);

        createButton=findViewById(R.id.create_acct_button);
        usernameEditText=findViewById(R.id.username_acct);
        passwordEditText=findViewById(R.id.password_acct);
        emailEditText=findViewById(R.id.email_acct);



        authStateListener= new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {    // this will listen to the frebase changes
                currentUser=firebaseAuth.getCurrentUser();
                if(currentUser!=null){
                    //user is logged in
                }else{
                    // no user yet!
                }
            }
        };

        createButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!TextUtils.isEmpty(usernameEditText.getText().toString()) &&
                        !TextUtils.isEmpty(emailEditText.getText().toString()) && !TextUtils.isEmpty(passwordEditText.getText().toString())){
                    String email=emailEditText.getText().toString().trim();
                    String password=passwordEditText.getText().toString().trim();
                    String username=usernameEditText.getText().toString().trim();
                    createUserEmailAccount(email,password,username);

                }else{
                    Toast.makeText(CreateAccountActivity.this, "Empty Fields are not allowed!",
                            Toast.LENGTH_LONG).show();
                }


            }
        });

    }
    private void createUserEmailAccount(String email,String password,String username){
        if(!TextUtils.isEmpty(email) && !TextUtils.isEmpty(password) && !TextUtils.isEmpty(username)){
            progressBar.setVisibility(View.VISIBLE);

            firebaseAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            // task will have the current user
                            if(task.isSuccessful()){
                                Log.d("hello", "onComplete: "+ "task successful");
                                // take user to addjournal
                                // when we create account we must Authenticate them using firebase plus add to firestore
                                currentUser=firebaseAuth.getCurrentUser();
                                assert currentUser != null;
                                String currentUserId=currentUser.getUid();
                                // map is for userid and username to put in firestore
                                Map<String,String> userObj=new HashMap<>();
                                userObj.put("userId",currentUserId);
                                userObj.put("username",username);

                                // save to firestore

                                collectionReference.add(userObj)
                                        .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                            @Override
                                            public void onSuccess(DocumentReference documentReference) {
                                                documentReference.get()
                                                        .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                                if(Objects.requireNonNull(task.getResult()).exists()){
                                                                    progressBar.setVisibility(View.INVISIBLE);
                                                                    String name=task.getResult().getString("username");
                                                                    // we will use the global class that we created to store username and userid so that it
                                                                    // can be used anywhere in the application
                                                                    JournalApi journalApi=JournalApi.getInstance();
                                                                    journalApi.setUserId(currentUserId);
                                                                    journalApi.setUsername(name);

                                                                    // below intent can also work by sending


                                                                    Intent intent=new Intent(CreateAccountActivity.this,PostJournalActivity.class);
                                                                    intent.putExtra("username",name);
                                                                    intent.putExtra("userId",currentUserId);
                                                                    startActivity(intent);
                                                                }else{
                                                                    progressBar.setVisibility(View.INVISIBLE);
                                                                }

                                                            }
                                                        });

                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {

                                            }
                                        });


                            }else{

                            }
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {

                        }
                    });
        }else{

        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        currentUser=firebaseAuth.getCurrentUser();
        firebaseAuth.addAuthStateListener(authStateListener); // listen to the changes that happen in our firebase
    }
}