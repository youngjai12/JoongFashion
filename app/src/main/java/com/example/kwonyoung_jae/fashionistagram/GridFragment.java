package com.example.kwonyoung_jae.fashionistagram;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;

import android.app.FragmentTransaction;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;



public class GridFragment extends Fragment {
    FirebaseFirestore firestore;
    FirebaseAuth mAuth;
    View mainview;
    RecyclerView recyclerView;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        mainview = inflater.inflate(R.layout.fragment_grid, container, false);
        recyclerView = (RecyclerView) mainview.findViewById(R.id.wrapper);
        return mainview;
    }
    @Override
    public void onResume(){
        super.onResume();
        recyclerView.setAdapter(new GridFragmentRecyclerViewAdatper());
        recyclerView.setLayoutManager(new GridLayoutManager(getActivity(),3));
    }

    class GridFragmentRecyclerViewAdatper extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        private ArrayList<ContentDTO> contentDTOs;
        public GridFragmentRecyclerViewAdatper() {
            contentDTOs = new ArrayList<>();
            firestore.getInstance().collection("style").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    if (task.isSuccessful()) {
                        contentDTOs = new ArrayList<ContentDTO>();
                        for (DocumentSnapshot doc : task.getResult()) {
                            Log.d("tag", "oh maybe success in accessing firebase?");
                            ContentDTO item = doc.toObject(ContentDTO.class);
                            contentDTOs.add(item);
                        }
                        notifyDataSetChanged();
                    } else {
                        Log.d("TAG", "error getting documents....shiba", task.getException());
                    }
                }
            });
        }


        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            //현재 사이즈 뷰 화면 크기의 가로 크기의 1/3값을 가지고 오기

            int width = getResources().getDisplayMetrics().widthPixels / 3;
            ImageView imageView = new ImageView(parent.getContext());
            imageView.setLayoutParams(new LinearLayoutCompat.LayoutParams(width, width));

            return new CustomViewHolder(imageView);
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, final int position) {
            mAuth = FirebaseAuth.getInstance();
            Glide.with(holder.itemView.getContext())

                    .load(contentDTOs.get(position).imageUrl).apply(new RequestOptions().centerCrop())

                    .into(((CustomViewHolder) holder).imageView);
            ((CustomViewHolder)holder).imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(v.getContext(),AddPhotoActivity.class);
                    intent.putExtra("selector",mAuth.getCurrentUser().getUid());
                    Log.d("이 사진을 선택한 "," 사람은 바로 : "+mAuth.getCurrentUser().getUid());
                    Log.d("finalposition","number is "+position);
                    intent.putExtra("imageUid",contentDTOs.get(position).photoid);
                    startActivity(intent);

                }
            });
        }

        @Override
        public int getItemCount() {
            return contentDTOs.size();
        }

        private class CustomViewHolder extends RecyclerView.ViewHolder{
            public ImageView imageView;

            public CustomViewHolder(ImageView imageView){
            super(imageView);
            this.imageView = imageView;
        }
        }
    }



}
