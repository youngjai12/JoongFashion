package com.example.kwonyoung_jae.fashionistagram;

import android.app.DownloadManager;
import android.content.Context;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;

import android.app.FragmentTransaction;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;

import android.app.FragmentTransaction;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import org.w3c.dom.Text;

import java.util.ArrayList;

public class AlarmFragment extends Fragment {
    public AlarmFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_alarm, container, false);
        RecyclerView recyclerView = view.findViewById(R.id.alarm_recyclerview);
        recyclerView.setAdapter(new AlarmRecyclerViewAdapter());
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        return view;
    }

    class AlarmRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        private ArrayList<AlarmDTO> alarmDTOList;
        public AlarmRecyclerViewAdapter(){
            String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
            FirebaseFirestore.getInstance().collection("alarms").whereEqualTo("destinationUid",uid)
                    .orderBy("timestamp").addSnapshotListener(new EventListener<QuerySnapshot>() {
                @Override
                public void onEvent(QuerySnapshot queryDocumentSnapshots, FirebaseFirestoreException e) {
                    alarmDTOList = new ArrayList<>();
                    if(queryDocumentSnapshots==null){
                        return;
                    }else{
                        for(QueryDocumentSnapshot doc : queryDocumentSnapshots){
                            alarmDTOList.add(doc.toObject(AlarmDTO.class));
                        }
                        notifyDataSetChanged();
                    }
                }
            });
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_comment,parent,false);
            return new CustomViewHolder(view);
        }

        private class CustomViewHolder extends RecyclerView.ViewHolder {
            ImageView profileImage;
            TextView profileText;
            CustomViewHolder(View itemview){
                super(itemview);
                profileImage = itemview.findViewById(R.id.comment_image);
                profileText = itemview.findViewById(R.id.comment_profile);
            }
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, final int position) {
            final ImageView profileImage = ((CustomViewHolder)holder).profileImage;
            TextView commentText = ((CustomViewHolder)holder).profileText;

            FirebaseFirestore.getInstance().collection("profileImages").document(alarmDTOList.get(position).uid)
                    .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if(task.isSuccessful()){
                        String url = task.getResult().getData().toString();
                        Glide.with(getActivity()).load(url).apply(new RequestOptions().circleCrop())
                                .into(profileImage);
                    }
                }
            });

            switch (alarmDTOList.get(position).kind){
                case 0:
                    String str_0 = alarmDTOList.get(position).userId + getString(R.string.alarm_favorite);
                    commentText.setText(str_0);
                    break;
                case 1:
                    String str_1 = alarmDTOList.get(position).userId + getString(R.string.alarm_who)
                            +alarmDTOList.get(position).message + getString(R.string.alarm_comment);
                    commentText.setText(str_1);
                    break;
                case 2:
                    String str_2 = alarmDTOList.get(position).userId + getString(R.string.alarm_follow);
                    commentText.setText(str_2);
                    break;
            }
        }

        @Override
        public int getItemCount() {
            return null!=alarmDTOList?alarmDTOList.size():0;
        }
    }
}
