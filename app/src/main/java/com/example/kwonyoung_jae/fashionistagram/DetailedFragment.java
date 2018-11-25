package com.example.kwonyoung_jae.fashionistagram;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;

import android.app.FragmentTransaction;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.text.Layout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import org.w3c.dom.Text;

import java.util.ArrayList;


public class DetailedFragment extends Fragment {
    FirebaseFirestore firestore;
    FirebaseAuth mAuth;
    FirebaseUser user;
    private FirebaseDatabase FDB;
    public DetailedFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mAuth=FirebaseAuth.getInstance();

        View view = inflater.inflate(R.layout.fragment_detailed, container, false);
        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.detailview_recyle); // 전체적인 reclyeview 의 xml을 말한다.
        //현재 창에다가 recyclerview를 띄우라는 소리인듯.
        recyclerView.setAdapter(new DetailRecyclerViewAdapter());
        //DetailRecyclerViewAdapter라는 것은 파이어베이스 firestore에서 여러 정보들을 가지고 온 것들을 포함하고 있는 class이다.
        //그래서 new DetailRecyclerViewAdapter라는 것은 그러한 정보들을 담고 있는 새로운 객체를 만든 것이다. 그게 recyclerview에 연결을 해주라라는 말이다.
        recyclerView.setLayoutManager(new LinearLayoutManager(view.getContext()));

        return view ;
    }


    private class DetailRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        private ArrayList<ContentDTO> contentDTOs;
        private ArrayList<String> contentUidList;
        //좋아요를 누른 회원들이 누구인지를 파악하기 위해서 Uid들만 받으려고하는것.

        DetailRecyclerViewAdapter() {
            firestore= FirebaseFirestore.getInstance();
            String uid = mAuth.getCurrentUser().getUid();
            firestore.collection("images").orderBy("timestamp").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    if(task.isSuccessful()){
                        contentDTOs = new ArrayList<ContentDTO>();
                        contentUidList = new ArrayList<String>();

                    for(DocumentSnapshot doc : task.getResult()) {
                        Log.d("tag","oh maybe success in accessing firebase?");
                        ContentDTO item = doc.toObject(ContentDTO.class);
                        contentDTOs.add(item);
                        }
                        notifyDataSetChanged();
                    }else{
                        Log.d("TAG","error getting documents....shiba",task.getException());
                    }
                }
            });
        }
        @Nullable
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_detail,parent,false);

            return new CustomViewHolder(view);
        }

        @Override //그 뷰에 나올 내용들을 정리하는 것.
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            final int finalPosition = position;

            ((CustomViewHolder)holder).name.setText(contentDTOs.get(position).userId);
            Glide.with(holder.itemView.getContext()).load(contentDTOs.get(position).imageUrl)
                    .into(((CustomViewHolder)holder).content);



            ((CustomViewHolder)holder).comment_text.setText(contentDTOs.get(position).explain);

            ((CustomViewHolder)holder).favorite.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    favoriteEvent(finalPosition);
                }
            });
            if(contentDTOs.get(position).favorites.containsKey(mAuth.getCurrentUser().getUid())){
                ((CustomViewHolder)holder).favorite.setImageResource(R.drawable.ic_favorite);
            }else{
                ((CustomViewHolder)holder).favorite.setImageResource(R.drawable.ic_favorite_border);
            }
            ((CustomViewHolder)holder).favoritecount.setText("좋아요 "+ contentDTOs.get(position).favoriteCount+" 개");

            ((CustomViewHolder)holder).comment.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(v.getContext(),CommentActivity.class);
                    intent.putExtra("imageUid",contentUidList.get(finalPosition));
                    intent.putExtra("detinationUid",contentDTOs.get(finalPosition).uid);
                    Log.d("DetailViewFragment",contentUidList.get(finalPosition)==null ? "NULL":
                    contentUidList.get(finalPosition));
                    startActivity(intent);
                }
            });

        }

        @Override
        public int getItemCount(){
            //return contentDTOs.size();
            return null!=contentDTOs?contentDTOs.size():0;
            //return 4;
        }

        private void favoriteEvent(int position){
            final int finalPosition = position;
            FDB.getReference("images").child(contentUidList.get(position)).runTransaction(
                    new Transaction.Handler() {
                        @NonNull
                        @Override
                        public Transaction.Result doTransaction(@NonNull MutableData mutableData) {
                            ContentDTO contentDTO = mutableData.getValue(ContentDTO.class);
                            String uid = mAuth.getCurrentUser().getUid();
                            if(contentDTO==null){
                                return Transaction.success(mutableData);
                            }
                            if(contentDTO.favorites.containsKey(uid)){
                                contentDTO.favoriteCount = contentDTO.favoriteCount -1;
                                contentDTO.favorites.remove(uid);
                            }else{
                                contentDTO.favoriteCount = contentDTO.favoriteCount +1;
                                contentDTO.favorites.put(uid,true);
                                favoriteAlarm(contentDTOs.get(finalPosition).uid);
                            }
                            mutableData.setValue(contentDTO);
                            return Transaction.success(mutableData);
                        }

                        @Override
                        public void onComplete(@android.support.annotation.Nullable
                                                       DatabaseError databaseError, boolean b,
                                               @android.support.annotation.Nullable DataSnapshot dataSnapshot) {

                        }
                    }
            );
        }

        public void favoriteAlarm (String destinationUid){
            AlarmDTO alarmDTO = new AlarmDTO();
            alarmDTO.destinationUid = destinationUid;
            alarmDTO.userId = user.getEmail();
            alarmDTO.uid = user.getUid();
            alarmDTO.kind = 0;
            FDB.getReference().child("alarms").push().setValue(alarmDTO);
        }
        private class CustomViewHolder extends RecyclerView.ViewHolder {
            public ImageView comment, favorite, content,profile;
            public TextView name,comment_text,favoritecount;
            public CustomViewHolder(View view) {
                super(view);
                comment = view.findViewById(R.id.detailview_commentimage);
                favorite = view.findViewById(R.id.detailview_favoriteimage);
                content = view.findViewById(R.id.detailview_contentimage);
                profile = view.findViewById(R.id.detailview_profileimage);
                name = view.findViewById(R.id.detailview_profilename);
                comment_text = view.findViewById(R.id.detailview_commenttext);
                favoritecount = view.findViewById(R.id.detailview_favoritecount);
            }
        }
    }
}
