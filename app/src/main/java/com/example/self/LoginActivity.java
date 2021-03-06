package com.example.self;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import util.JournalApi;

public class LoginActivity extends AppCompatActivity {
    private Button loginButton;
    private Button createButton;
    private AutoCompleteTextView emailAddress;
    private EditText passwordEditText;

    // firebase auth
    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener authStateListener;
    private FirebaseUser currentUser;

    // firestore connection
    private FirebaseFirestore db=FirebaseFirestore.getInstance();
    private CollectionReference collectionReference=db.collection("Users");





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        firebaseAuth=FirebaseAuth.getInstance();

        loginButton=findViewById(R.id.login_button);
        createButton=findViewById(R.id.create_acct_button);
        emailAddress=findViewById(R.id.email);
        passwordEditText=findViewById(R.id.password);

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginEmailPasswordUser(emailAddress.getText().toString().trim(),passwordEditText.getText().toString().trim());

            }
        });

        createButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this,CreateAccountActivity.class));
            }
        });
    }

    private void loginEmailPasswordUser(String email, String pwd) {
        if(!TextUtils.isEmpty(email) && !TextUtils.isEmpty(pwd)){
            firebaseAuth.signInWithEmailAndPassword(email,pwd)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(task.isSuccessful()){
                            FirebaseUser user=firebaseAuth.getCurrentUser();
                            assert user != null;
                            String currentUserId=user.getUid();
                            // need to loop through the firestore and match the user id
                            collectionReference.whereEqualTo("userId",currentUserId)
                                    .addSnapshotListener(new EventListener<QuerySnapshot>() {
                                        @Override
                                        public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                                            if(error!=null){

                                            }
                                            assert value != null;
                                            if(!value.isEmpty()){
                                                for(QueryDocumentSnapshot snapshot: value){
                                                    JournalApi journalApi=JournalApi.getInstance();
                                                    journalApi.setUsername(snapshot.getString("username"));
                                                    journalApi.setUserId(snapshot.getString("userId"));
                                                    startActivity(new Intent(LoginActivity.this,PostJournalActivity.class));
                                                }
                                            }

                                        }
                                    });
                        }}
                    }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {

                }
            });
        }else{
            Toast.makeText(LoginActivity.this, "Enter email and password!", Toast.LENGTH_LONG).show();
        }

    }
}