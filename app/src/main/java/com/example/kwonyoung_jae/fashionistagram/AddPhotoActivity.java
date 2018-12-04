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
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;


public class AddPhotoActivity extends AppCompatActivity  {

    private static int PICK_iMAGE_FROM_ALBUM=0;
    FirebaseFirestore firestore;
    FirebaseStorage storage;
    private FirebaseAuth mAuth;
    EditText photo_explain;
    ImageView addphoto;
    Uri photoUri;
    Button submit_btn;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_photo);
        photo_explain = findViewById(R.id.photo_context);
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
                //startActivitForResult( 그 액티비티를 호출하고, 그 때의 요청코드임)
            }
        });
        /*
        //앨범에서 사진 가져오는 권한 허용문제는 main에서 하는게 한번만 하면 되니깐 .
        //만약 여기 activity에 들어있으면 이 탭들어갈 때마다 해나까. 이상해진다.
        Intent photopicker = new Intent(Intent.ACTION_PICK);
        //일단 창을 열어주고, 여기다가 사진을 고르는 의미이다.
        photopicker.setType("image/*");
        startActivityForResult(photopicker,PICK_iMAGE_FROM_ALBUM);
        */
    }
    //activity reuslt -> 이름처럼 모든 결과들이 집중되는 함수이다.

    //지금 내 앱에서 잠깐 내 갤러리 앱으로가서, 갤러리 앱에서 사진을 고르는 처리를 하고, 그다음에 다시 내 앱으로 되돌아온다.
    // 그렇기 때문에 onActivityResult를 통해서 그 결과를 수신대기하는 느낌인 것임.
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
                photoUri = data.getData(); //내가 고른 사진의 Uri를 담아준다. 그래야 그 uri를 DB에 넘길수 있다. 말이 uri이지, 그냥 사진 데이터를 말하는것 같다.
                addphoto.setImageURI(data.getData()); //이렇게 골랐으면 일단 그 사진을 URI에 뿌려주는 것이다.
            }
            else if(resultCode == Activity.RESULT_CANCELED){
                Toast.makeText(this,"사진을 고르지 않으셨습니다.",Toast.LENGTH_LONG).show();
                finish();
            }
        }
    }

    public void contentUpload(){
        final String time = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        Toast.makeText(this, time, Toast.LENGTH_SHORT).show();
        final String filename = time+".png";
        //중복되지 않는 파일명을 주기 위함.


        final StorageReference storageReference = storage.getReference().child("images").child(filename);
        //images라는 폴더에다가 세부 경로로 filename을 지정해줬고, 이는 중복되지 않게 하려고 올린시각을 기준으로 해준 것이다.
        storageReference.putFile(photoUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
           //그리고 그렇게 storage reference로 올릴 위치를 지정해준다음에, 아까 사진을 담고 있는 변수였던, photoUri를 담겨준다.
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                //이렇게 storage에다가 올려주면서 ....
                Toast.makeText(getApplicationContext(),"업로드 작업 완료됨",Toast.LENGTH_LONG).show();
                final ContentDTO contentDTO = new ContentDTO();
                //storage에 올려주면서 DTO를 만들어준다. 이때는 구체정인 내용을 같이... 누가 올렸는지, 이런것들을 같이..
                Uri uri = taskSnapshot.getDownloadUrl();
                contentDTO.imageUrl=uri.toString();
                String uuid = mAuth.getCurrentUser().getUid();
                contentDTO.uid = uuid;
                //contentDTO.photoid = filename+uuid;
                contentDTO.explain = photo_explain.getText().toString();
                contentDTO.userId = mAuth.getCurrentUser().getEmail();

                //Date date = new Date();
                //SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd:mm:ss");
                //contentDTO.timestamp = simpleDateFormat.format(date);
                contentDTO.timestamp = System.currentTimeMillis();
                firestore.collection("images").document(contentDTO.photoid).set(contentDTO);
                //contentDTO 형식으로 넣어주는 것을 말함.

                setResult(Activity.RESULT_OK);

                finish();

            }
        });

        }

}

        //여기서 부터 firebase storage 연결.



