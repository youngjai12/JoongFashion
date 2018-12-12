package com.example.kwonyoung_jae.fashionistagram;

import android.content.Intent;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.os.Handler;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
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
    ArrayList<FollowDTO> ids = new ArrayList<>();
    String destid,curid;
    FirebaseAuth mAuth;
    String target_vector;
    ArrayList<String>UidList = new ArrayList<>();



    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_similarity);

        destid = getIntent().getStringExtra("destinationUID");
        curid = getIntent().getStringExtra("currentUID");
        friendRecycle = findViewById(R.id.friend_recyclerview);
        //아마 이 페이지의 holder 인 host의 uid도 필요하긴 할듯.

        FirebaseFirestore.getInstance().collection("users").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                final ArrayList<FollowDTO> friendlist = new ArrayList<>();
                if(task.isSuccessful()){
                    for(QueryDocumentSnapshot doc : task.getResult()){
                        FollowDTO item = doc.toObject(FollowDTO.class);
                        if(item.uid.equals(destid)){
                            target_vector = item.vector;
                        }else{
                            friendlist.add(item);
                        }
                    }
                   Map<String,Double> simmap = new HashMap<>();
                    for(int i=0;i<friendlist.size();i++){
                        UidList.add(friendlist.get(i).uid);
                        simmap.put(friendlist.get(i).username,getSimilarity(target_vector,friendlist.get(i).vector));
                    }
                    // 여기까지 완료하면 friendlist에 그 사람들이 들어가있음. 자기와는 다른...
                    friendRecycle.setAdapter(new FriendRecyclerView(simmap));


                }
            }
        });
        friendRecycle.setLayoutManager(new LinearLayoutManager(this));


    }

    private class FriendRecyclerView extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        ArrayList<String> friend_ids;
        ArrayList<Double> sim;
        FriendRecyclerView(Map<String, Double> class_sim){
            Iterator<String> it = class_sim.keySet().iterator();
            friend_ids = new ArrayList<>();
            sim = new ArrayList<>();
            while(it.hasNext()){
                String name = it.next();
                if(class_sim.get(name)>80){
                    friend_ids.add(name);
                    sim.add(class_sim.get(name));
                }
            }
            //여기까지 하고 나면 각각의 friends 랑 sim다 나온다.
        }


        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.friendlist_item,parent,false);
            return new CustomViewHolder(view) ;
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, final int position) {
            Log.d("#### 과연 ","음... friendList에는 어떤게?"+friend_ids.get(position));
            ((CustomViewHolder)holder).friendsID.setText(String.valueOf(friend_ids.get(position)));
            Log.d("### 과연 ","vector는 똑바로 들어가나? "+sim.get(position));

            ((CustomViewHolder) holder).similairty.setText(String.valueOf(sim.get(position)));
            ((CustomViewHolder) holder).similairty.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Fragment fragment = new UserFragment();
                    Bundle bundle = new Bundle();
                    bundle.putString("destinationUid",UidList.get(position));
                    bundle.putString("userID",curid);
                    fragment.setArguments(bundle);
                    getSupportFragmentManager().beginTransaction().replace(R.id.main_frame,fragment)
                            .commit();
                    finish();

                }
            });


        }

        @Override
        public int getItemCount() {
            return null!=friend_ids?friend_ids.size():0;
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
