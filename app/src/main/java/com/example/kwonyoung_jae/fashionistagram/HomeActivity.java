package com.example.kwonyoung_jae.fashionistagram;

import android.Manifest;
import android.app.Fragment;

import android.app.FragmentTransaction;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.Image;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

import com.google.firebase.auth.FirebaseAuth;

public class HomeActivity extends AppCompatActivity implements BottomNavigationView.OnNavigationItemSelectedListener {
    ProgressBar progbar;
    BottomNavigationView nav;
    android.support.v7.widget.Toolbar toolbar;
    ImageView toolbarImage, backbutton;
    TextView toolbarname;
    String uid;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        nav = findViewById(R.id.main_navigation);
        nav.setOnNavigationItemSelectedListener(this);
        nav.setSelectedItemId(R.id.action_home);
        toolbar = findViewById(R.id.my_toolbar);
        toolbarImage = findViewById(R.id.toolbar_title_image);
        backbutton = findViewById(R.id.toolbar_btn_back);
        ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},1);
        toolbarname = findViewById(R.id.toolbar_username);
    }
    public void setToolbarDefault(){
        toolbarImage.setVisibility(View.VISIBLE);
        backbutton.setVisibility(View.GONE);
        toolbarname.setVisibility(View.GONE);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        //setToolbarDefault();
        switch (item.getItemId()){
            case R.id.action_home:
                android.support.v4.app.Fragment fragment =new DetailedFragment();
                loadFragment(fragment);
                return true;
            case R.id.action_search:
                android.support.v4.app.Fragment fragment1 = new GridFragment();
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
            case R.id.action_favourite_alarm:
                android.support.v4.app.Fragment fragment2 = new AlarmFragment();
                loadFragment(fragment2);
                return true;
            case R.id.action_account:
                uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
                android.support.v4.app.Fragment fragment3 = new UserFragment();
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
}




