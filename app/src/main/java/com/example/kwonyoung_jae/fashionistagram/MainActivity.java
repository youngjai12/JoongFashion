package com.example.kwonyoung_jae.fashionistagram;

import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
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
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageException;
import com.google.firebase.storage.StorageReference;


public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    FirebaseAuth mAuth;
    FirebaseFirestore DB;
    private FirebaseUser user;
    private FirebaseDatabase FDB;
    private Button mlogin;
    private Button mcreate;
    private EditText memail,mpassword;
    private ImageView testview;
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



        //crawl_update();
    }
    public void crawl_update(){
        final FirebaseFirestore firestore;
        firestore = FirebaseFirestore.getInstance();
        String filename = "11";
        final ContentDTO contentDTO = new ContentDTO();
        for(int i=10;i<100;i++) {
            final String ff = filename+i;
            Log.d("과연 "," 파일명은 제대로 들어갔을까?"+ff);
            StorageReference storageReference;
            try {
                storageReference = FirebaseStorage.getInstance().getReference().child("styles").child(ff + ".png");
            }
            catch (RuntimeException e){
                continue;
            }
            storageReference.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    String uri = task.getResult().toString();
                    contentDTO.imageUrl = uri;
                    contentDTO.photoid = ff+".png";
                    firestore.collection("style").document(contentDTO.photoid).set(contentDTO);

                }
            });


        }
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
