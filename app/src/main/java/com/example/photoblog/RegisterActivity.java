package com.example.photoblog;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class RegisterActivity extends AppCompatActivity {

    EditText reg_email,reg_password,confirm_password;
    Button createAccount,goto_login;
    FirebaseAuth mAuth;
    ProgressBar reg_progress;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        reg_email=(EditText)findViewById(R.id.reg_email);
        reg_password=(EditText)findViewById(R.id.reg_Password);
        confirm_password=(EditText)findViewById(R.id.confirmation_pass);
        createAccount=(Button) findViewById(R.id.createAccount);
        goto_login=(Button) findViewById(R.id.oldUser);
        reg_progress=(ProgressBar)findViewById(R.id.progressBar);

        mAuth=FirebaseAuth.getInstance();

        createAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                reg_progress.setVisibility(View.VISIBLE);

                String email=reg_email.getText().toString();
                String pass=reg_password.getText().toString();
                String confirm=confirm_password.getText().toString();

                if(email.isEmpty())
                {
                    reg_email.setError("Email is required");
                    reg_email.requestFocus();
                    return;
                }

                if(!Patterns.EMAIL_ADDRESS.matcher(email).matches())
                {
                    reg_email.setError("Please enter valid email address");
                    reg_email.requestFocus();
                    return;
                }

                if(pass.isEmpty())
                {
                    reg_password.setError("Password is required");
                    reg_password.requestFocus();
                    return;
                }
                if(!confirm.equals(pass))
                {
                    confirm_password.setError("Password Does not match");
                    confirm_password.requestFocus();
                    return;
                }

                if(confirm.equals(pass))
                {

                    mAuth.createUserWithEmailAndPassword(email,pass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {

                            if(task.isSuccessful())
                            {
                                Toast.makeText(RegisterActivity.this,"User created",Toast.LENGTH_SHORT).show();
                                Intent intent=new Intent(RegisterActivity.this,SetupActivity.class);
                                startActivity(intent);
                                finish();
                            }
                            else
                            {
                                String error=task.getException().getMessage();
                                Toast.makeText(RegisterActivity.this,"Error: "+error,Toast.LENGTH_SHORT).show();
                            }
                            reg_progress.setVisibility(View.INVISIBLE);

                        }
                    });

                }

            }
        });

        goto_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(RegisterActivity.this,LoginActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser=mAuth.getCurrentUser();

        if(currentUser!=null)
        {
            Intent intent=new Intent(RegisterActivity.this,MainActivity.class);
            startActivity(intent);
            finish();
        }
    }
}
