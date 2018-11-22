package com.example.kwonyoung_jae.fashionistagram;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;


public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    FirebaseAuth mAuth;
    FirebaseFirestore DB;
    private FirebaseUser user;
    private FirebaseDatabase FDB;
    private Button mlogin;
    private Button mcreate;
    private EditText memail,mpassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();
        mcreate = findViewById(R.id.create);
        mlogin =findViewById(R.id.email_login_button);
        memail = findViewById(R.id.email_edittext);
        mpassword = findViewById(R.id.password_edittext);
        mlogin.setOnClickListener(this);
        mcreate.setOnClickListener(this);
    }
    public void login(){
        final String usermail = memail.getText().toString().trim();
        final String password = mpassword.getText().toString().trim();
        mAuth = FirebaseAuth.getInstance();
        if(usermail.isEmpty()){
            memail.setError("email is required");
            return;
        }
        if(password.isEmpty()){
            mpassword.setError("password is required");
            mpassword.requestFocus();
            return;
        }

        mAuth.signInWithEmailAndPassword(usermail,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                FirebaseUser user = mAuth.getCurrentUser();
                if(task.isSuccessful()){
                    Toast.makeText(getApplicationContext(),"이제 로그인할거임.",Toast.LENGTH_LONG).show();

                    Intent intent = new Intent(getApplicationContext(),HomeActivity.class);
                    startActivity(intent);
                    finish();
                }else{
                    if (user==null){
                        Toast.makeText(getApplicationContext(),"등록되지 않은 아이디입니다. 회원가입 해주세요",Toast.LENGTH_LONG).show();
                        return;
                    }else{
                        Toast.makeText(getApplicationContext(),"비밀번호가 잘못됬음",Toast.LENGTH_LONG).show();

                    }
                }
            }
        });
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.create:
                Intent intent = new Intent(getApplicationContext(),CreateProfileActivity.class);
                startActivity(intent);
                break;
            case R.id.email_login_button:
                login();
                break;
        }
    }
}
