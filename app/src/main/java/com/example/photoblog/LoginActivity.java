package com.example.photoblog;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.w3c.dom.Text;

public class LoginActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    EditText login_email,login_password;
    ProgressBar progressBar;
    Button login_btn,newUser_btn;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mAuth = FirebaseAuth.getInstance();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        login_email = (EditText) findViewById(R.id.loginemail);
        login_password = (EditText) findViewById(R.id.loginPassword);
        progressBar=(ProgressBar)findViewById(R.id.progressBar);
        progressBar.setIndeterminate(true);
        login_btn = (Button) findViewById(R.id.login);
        newUser_btn = (Button) findViewById(R.id.newUser);

        login_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String loginEmail=login_email.getText().toString();
                String loginPassword=login_password.getText().toString();

                if(!TextUtils.isEmpty(loginEmail)&&!TextUtils.isEmpty(loginPassword))
                {
                    progressBar.setVisibility(View.VISIBLE);

                    mAuth.signInWithEmailAndPassword(loginEmail,loginPassword).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {

                            if(task.isSuccessful())
                            {
                                Intent intent=new Intent(LoginActivity.this,MainActivity.class);
                                startActivity(intent);
                                finish();
                            }
                            else
                            {
                                String error=task.getException().getMessage();
                                Toast.makeText(LoginActivity.this,"Error: "+error,Toast.LENGTH_SHORT).show();

                            }

                            progressBar.setVisibility(View.INVISIBLE);
                        }
                    });

                }
            }
        });
        newUser_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(LoginActivity.this,RegisterActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }
    @Override
    public void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser!=null)
        {
            Intent intent=new Intent(LoginActivity.this,MainActivity.class);
            startActivity(intent);
            finish();
        }
    }
}
