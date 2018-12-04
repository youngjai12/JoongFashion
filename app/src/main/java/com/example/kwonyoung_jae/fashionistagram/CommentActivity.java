package com.example.kwonyoung_jae.fashionistagram;

import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

public class CommentActivity extends AppCompatActivity {

    String contentUid , destinationUid;
    FirebaseUser user;
    Button sendButton;
    EditText message;
    RecyclerView comment_recyclerView;
    //FcmPush fcmPush;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comment);
        contentUid = getIntent().getStringExtra("imageUid");
        user = FirebaseAuth.getInstance().getCurrentUser();
        destinationUid = getIntent().getStringExtra("destinationUid");
        message = findViewById(R.id.comment_edit_message);
        sendButton = findViewById(R.id.comment_btn_send);
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ContentDTO.Comment comment = new ContentDTO.Comment();
                comment.userId= FirebaseAuth.getInstance().getCurrentUser().getEmail();
                comment.comment = message.getText().toString();
                comment.uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
                comment.timestamp = System.currentTimeMillis();
                Log.d("무슨 아이디일까","과연 이게 content 맞는가?"+contentUid);
                FirebaseFirestore.getInstance().collection("images").document(contentUid).collection("comments")
                        .document().set(comment);
                commentAlarm(destinationUid,message.toString());
            }
        });
        comment_recyclerView = (RecyclerView) findViewById(R.id.comment_recyclerview);
        comment_recyclerView.setAdapter(new CommentRecyclerView());
        comment_recyclerView.setLayoutManager(new LinearLayoutManager(this));

    }

    private void commentAlarm(String destinationUid,String message){
        AlarmDTO alarmDTO = new AlarmDTO();
        alarmDTO.destinationUid = destinationUid;
        alarmDTO.userId = user.getEmail();
        alarmDTO.uid = user.getUid();
        alarmDTO.kind=1;
        alarmDTO.message = message;
        alarmDTO.timestamp = System.currentTimeMillis();
        FirebaseFirestore.getInstance().collection("alarms").document().set(alarmDTO);
    }

    class CommentRecyclerView extends RecyclerView.Adapter<RecyclerView.ViewHolder>{
        private ArrayList<ContentDTO.Comment> commentList;
        CommentRecyclerView(){
            FirebaseFirestore.getInstance().collection("images").document(contentUid).collection("comments")
                    .addSnapshotListener(new EventListener<QuerySnapshot>() {
                        @Override
                        public void onEvent(QuerySnapshot queryDocumentSnapshots, FirebaseFirestoreException e) {
                            commentList = new ArrayList<>();
                            Log.d("어떤 커멘트?","이게 사진 주소맞나?"+contentUid);
                            if(queryDocumentSnapshots==null){
                                return;
                            }
                            for(QueryDocumentSnapshot doc : queryDocumentSnapshots){
                                commentList.add(doc.toObject(ContentDTO.Comment.class));
                            }
                            notifyDataSetChanged();
                        }
                    });
            if(commentList==null){
                Log.d("tag","코멘트 리스트가 null인가보다.");
            }
        }


        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_comment,parent,false);
            return new CustomViewHolder(view) ;
        }

        private class CustomViewHolder extends RecyclerView.ViewHolder{
            public ImageView profileImage;
            public TextView profileTextView;
            public TextView commentTextView;

            public CustomViewHolder(View itemView){
                super(itemView);
                profileImage = (ImageView) itemView.findViewById(R.id.comment_image);
                profileTextView = (TextView) itemView.findViewById(R.id.comment_profile);
                commentTextView = itemView.findViewById(R.id.comment_comment);
            }
        }

        @Override
        public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder holder, final int position) {
            FirebaseFirestore.getInstance().collection("profileImages").document(commentList.get(position).uid)
                        .addSnapshotListener(new EventListener<DocumentSnapshot>() {
                            @Override
                            public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {
                                if (documentSnapshot.getData() != null) {
                                    String url = documentSnapshot.getData().toString();
                                    ImageView profileImageView = ((CustomViewHolder) holder).profileImage;
                                    Glide.with(holder.itemView.getContext()).load(url).apply(new RequestOptions().circleCrop()).into(profileImageView);
                                } else {
                                    Log.d("tag ", "완전 다 끝나버렸넹 ㅎㅎ");
                                }
                            }
                        });


                ((CustomViewHolder) holder).profileTextView.setText(commentList.get(position).userId);
                ((CustomViewHolder) holder).commentTextView.setText(commentList.get(position).comment);

        }

        @Override
        public int getItemCount() {
            return null!=commentList?commentList.size():0;
        }

    }
}
