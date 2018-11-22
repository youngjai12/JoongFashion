package com.example.kwonyoung_jae.fashionistagram;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;

import java.text.SimpleDateFormat;
import java.util.Date;

import io.grpc.Context;

public class AddPhotoActivity extends AppCompatActivity {

    private static int PICK_iMAGE_FROM_ALBUM=0;
    private static final String[] photoURI = null;
    FirebaseFirestore firestore;
    FirebaseStorage storage;
    private FirebaseAuth mAuth;
    ImageView addphoto;
    public Uri photoUri;
    Button submit_btn;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_photo);

        storage = FirebaseStorage.getInstance();
        firestore = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        addphoto = findViewById(R.id.target_photo);
        submit_btn = findViewById(R.id.photo_submit);
        submit_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                contentUpload();
            }
        });

        addphoto.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                Intent photopicker = new Intent(Intent.ACTION_PICK);
                photopicker.setType("image/*");
                startActivityForResult(photopicker,PICK_iMAGE_FROM_ALBUM);
            }
        });
        //앨범에서 사진 가져오는 권한 허용문제는 main에서 하는게 한번만 하면 되니깐 .
        //만약 여기 activity에 들어있으면 이 탭들어갈 때마다 해나까. 이상해진다.
        Intent photopicker = new Intent(Intent.ACTION_PICK);
        //일단 창을 열어주고, 여기다가 사진을 고르는 의미이다.
        photopicker.setType("image/*");
        startActivityForResult(photopicker,PICK_iMAGE_FROM_ALBUM);

    }
    //activity reuslt -> 이름처럼 모든 결과들이 집중되는 함수이다.
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //사진은 data라는 자리에 타서 넘어온다.

        if(requestCode == PICK_iMAGE_FROM_ALBUM ){
            //이건 데이터를 요청했을 때 그 데이터가 사진임을 알려주는 것이다. 왜냐면 그게 사진은
            // 그 고유의 TAG 값을 PICK_IMAGE_FROM_ALBUM이라고 해서 달아놨음
            // Activity Result_OK라는 것은 사진을 진짜 선택했을 경우를 말함. 그리고 resultcode라는  것은
            //실제로 동작이 일어났는지의 진위를 보는 것임.
            if(resultCode == Activity.RESULT_OK) {
                photoUri = data.getData(); //내가 고른 사진의 Uri를 담아준다. 그래야 그 uri를 DB에 넘길수 있다.
                addphoto.setImageURI(data.getData());
            }
            else if(resultCode == Activity.RESULT_CANCELED){
                Toast.makeText(this,"사진을 고르지 않으셨습니다.",Toast.LENGTH_LONG).show();
                finish();
            }
        }
    }
    public void contentUpload(){
        String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        Toast.makeText(this, timestamp, Toast.LENGTH_SHORT).show();
        String filename = timestamp+".png";
        //중복되지 않는 파일명을 주기 위함.
        final StorageReference storageReference = storage.getReference().child("images").child(filename);
        storageReference.putFile(photoUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Toast.makeText(getApplicationContext(),"업로드 성공!! 흐흫",Toast.LENGTH_LONG).show();
                Log.d("photo_upload successful","good");
                //Uri uri = taskSnapshot.getResult();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getApplicationContext(),"업로드에 실패하였다.", Toast.LENGTH_SHORT).show();

            }
        });
        //여기서 부터 firebase storage 연결.

    }

}
