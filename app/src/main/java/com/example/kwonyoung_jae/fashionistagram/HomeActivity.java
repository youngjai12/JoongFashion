package com.example.kwonyoung_jae.fashionistagram;

import android.Manifest;
import android.app.Activity;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;
import java.util.Map;

public class HomeActivity extends AppCompatActivity implements BottomNavigationView.OnNavigationItemSelectedListener {
    ProgressBar progbar;
    BottomNavigationView nav;
    android.support.v7.widget.Toolbar toolbar;
    ImageView toolbarImage, backbutton;
    TextView toolbarname;
    String uid;
    int PICK_PROFILE_FROM_ALBUM=10;
    String FRAGMENT_ARG = "ARG_NO";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        toolbar = findViewById(R.id.my_toolbar);
        toolbarImage = findViewById(R.id.toolbar_title_image);
         ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},1);

        nav = findViewById(R.id.main_navigation);
        nav.setOnNavigationItemSelectedListener(this);
        nav.setSelectedItemId(R.id.action_home);

    }
    public void setToolbarDefault(){
        if(toolbarImage==null){
            Log.d("tag","이거 지금 toolbarimage null이라는데?");
        }
        toolbarImage = findViewById(R.id.toolbar_title_image);
        backbutton = findViewById(R.id.toolbar_btn_back);
        toolbarname = findViewById(R.id.toolbar_username);

        toolbarImage.setVisibility(View.VISIBLE);
        backbutton.setVisibility(View.GONE);
        toolbarname.setVisibility(View.GONE);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        setToolbarDefault();
        switch (item.getItemId()){
            case R.id.action_home:
                android.support.v4.app.Fragment fragment =new DetailedFragment();
                Bundle bundle0 = new Bundle();
                bundle0.putInt(FRAGMENT_ARG,0);
                loadFragment(fragment);
                return true;
            case R.id.action_search:
                android.support.v4.app.Fragment fragment1 = new GridFragment();
                Bundle bundle1 = new Bundle();
                bundle1.putInt(FRAGMENT_ARG,1);
                loadFragment(fragment1);
                return true;
            case R.id.action_add_photo:
                //manifest를 통해서 권한을 받아오는 부분을 설정해놨다. 그래서 그 권한을 받아오지 못하면 실행이 안되는 것임.
                if(ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                        == PackageManager.PERMISSION_GRANTED){
                    Intent intent = new Intent(this,AddPhotoActivity.class);
                    startActivity(intent);
                }else{
                    Toast.makeText(this,"스토리지 권한이 없다.",Toast.LENGTH_LONG).show();
                }
                return true;
            case R.id.action_favourite_alarm:;
                android.support.v4.app.Fragment fragment2 = new AlarmFragment();
                Bundle bundle3 = new Bundle();
                bundle3.putInt(FRAGMENT_ARG,3);
                loadFragment(fragment2);
                return true;
            case R.id.action_account:
                uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
                android.support.v4.app.Fragment fragment3 = new UserFragment();
                Bundle bundle =new Bundle();
                bundle.putString("destinationUid",uid);
                fragment3.setArguments(bundle);
                loadFragment(fragment3);
                return true;
            default:
                return false;
        }
    }
    public void loadFragment(android.support.v4.app.Fragment fragment){
        getSupportFragmentManager().beginTransaction().replace(R.id.main_frame,fragment)
                .commit();

    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }
    @Override // actvity에 소속되어 있는게 fragmentview임 그래서 그 고른 사진이 fragment가 아니라, 속한 activity로 넘어가기 때문에 이렇게 지정해두는 것이다.
    public void onActivityResult(int requestCode, int resultCode, Intent data){
        //메인 액티비티에서 간단한 작업을 하고 넘겨준다.
        //이 때 단순히 startActivity(intent) 를 한 것이 아니라, 결과 값을 리턴받기 위해서 startActivityForResult() 를 호출한다.
        super.onActivityResult(requestCode,resultCode,data);
        if(requestCode == PICK_PROFILE_FROM_ALBUM && resultCode == RESULT_OK){
            Uri imageUri = data.getData();
            Log.d("tag","image uri : "+imageUri);
            //이미지를 담아오기 위해서 data. getdata()하는 것임. data에는 image가 담겨있다.
            uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
            FirebaseStorage.getInstance().getReference().child("userProfileImages").child(uid)
                    //그냥 프로필 이미지니깐 그 유저의 고유번호인 uid로 만들어준다.
                    .putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Log.d("tag","storage에 올리는건 성공");
                    String url = taskSnapshot.getDownloadUrl().toString();
                    Map<String, Object> map = new HashMap<>();
                    map.put(uid,url);
                    //어디 계정에 어떤 이미지가 들어갔는지를 해주는 작업. 즉 데이터베이스에 넣어주는 것임.
                    FirebaseFirestore.getInstance().collection("profileImages").document(uid).set(map);
                    //이 코드까지 치고 시현해보면... crush가 일어나는데 왜??  만약에 제대로 작동이 된다면
                    //그 user fragment쪽가서 그 부분 클릭햇을 때 앨범이 떠야한다.
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.d("tag","업로드만 안되는듯?");
                }
            });


            nav.setSelectedItemId(R.id.action_account);
        }else{
            Log.d("tag","뭔가 이상한듯... error가 자꾸남...");
        }
    }

}




