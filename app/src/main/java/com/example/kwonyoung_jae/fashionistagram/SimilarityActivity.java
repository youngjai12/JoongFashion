package com.example.kwonyoung_jae.fashionistagram;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.Transaction;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class SimilarityActivity extends AppCompatActivity {
    FirebaseFirestore firestore;
    RecyclerView friendRecycle;
    ArrayList<String> ids = new ArrayList<>();
    String destid;
    String target_vector;
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_similarity);

        destid = getIntent().getStringExtra("destinationUID");
        friendRecycle = findViewById(R.id.friend_recyclerview);
        friendRecycle.setAdapter(new FriendRecyclerView());
        friendRecycle.setLayoutManager(new LinearLayoutManager(this));

        //아마 이 페이지의 holder 인 host의 uid도 필요하긴 할듯.
    }


    private class FriendRecyclerView extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        ArrayList<FollowDTO> vector_list;
        FriendRecyclerView(){
        Log.d("###### 과연 ####","recyclerview adapter 내용이다.");
            FirebaseFirestore.getInstance().collection("users").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    vector_list = new ArrayList<>();
                    if(task.isSuccessful()){
                        Log.d("#### 과연 ###"," adapter 안에서 array를 읽어오는 이 작업은 언제 실행되나? ");
                        for(QueryDocumentSnapshot doc : task.getResult()){
                            Log.d("### 과연 ####","이 lopp을 통과하지 않는건가?");

                            FollowDTO item = doc.toObject(FollowDTO.class);
                            if(item.uid.equals(destid)){
                                target_vector = item.vector;
                                Log.d("###### 과연 ##"," 올바르게 vector를 가지고 오는가?"+target_vector);
                            }else{
                                vector_list.add(item);
                                Log.d("### 과연 ####","이 lopp에서는 여기만 들어오나?");
                            }
                        }
                        notifyDataSetChanged();
                    }
                }
            });


        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.friendlist_item,parent,false);
            return new CustomViewHolder(view) ;
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            Log.d("#### 과연 "," recyclerview 이건 언제 실행되는건가?");
            Log.d("#### 과연 ","음... vectorlist에는 뭐가 ?"+vector_list.get(position).vector);
            ((CustomViewHolder)holder).friendsID.setText(vector_list.get(position).uid);
            Log.d("### 과연 ","my vector 에는 과연 null 이 들어가는 것인가? "+target_vector);

            ((CustomViewHolder) holder).similairty.setText(String.valueOf(getSimilarity(vector_list.get(position).vector,target_vector)));

        }

        @Override
        public int getItemCount() {
            return null!=vector_list?vector_list.size():0;
        }

        private class CustomViewHolder extends RecyclerView.ViewHolder {
            public TextView similairty, friendsID;
            public CustomViewHolder(View view) {
                super(view);
                similairty = view.findViewById(R.id.similarity);
                friendsID = view.findViewById(R.id.friend_id);
            }
        }
    }
    public double getSimilarity(String vec,String myvector){
        int sumx=0;
        for(int i=0;i<vec.length();i++) {
            if(vec.charAt(i)!='0') {
                sumx=sumx+(vec.charAt(i)-'0')*(vec.charAt(i)-'0');
            }
        }
        int sumy =0;
        for(int i=0;i<myvector.length();i++) {
            if(myvector.charAt(i)!='0') {
                sumy = sumy+(myvector.charAt(i)-'0')*(myvector.charAt(i)-'0');
            }
        }
        int inner=0;
        for(int i=0;i<myvector.length();i++) {
            inner = inner + (vec.charAt(i)-'0')*(myvector.charAt(i)-'0');
        }
        double result = (inner /Math.sqrt(sumx * sumy))*100;
        return result;

    }


}
