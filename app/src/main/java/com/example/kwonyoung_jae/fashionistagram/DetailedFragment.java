package com.example.kwonyoung_jae.fashionistagram;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import org.w3c.dom.Document;

import java.io.BufferedReader;
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

           firestore.collection("userClothes").orderBy("timestamp", Query.Direction.DESCENDING).addSnapshotListener(new EventListener<QuerySnapshot>() {
               @Override
               public void onEvent(QuerySnapshot queryDocumentSnapshots, FirebaseFirestoreException e) {
                   Log.d("########## 왜작동?","Detailed 에서는 작동하는가?");

                   Log.d("#### Detailed Frag","addsnapshot 안에 잇는거");
                   contentDTOs = new ArrayList<>();
                   contentUidList = new ArrayList<>();
                   if(queryDocumentSnapshots == null){
                       Toast.makeText(getContext(),"nonono",Toast.LENGTH_LONG).show();
                   }else{
                       for(QueryDocumentSnapshot doc : queryDocumentSnapshots){
                           ContentDTO item = doc.toObject(ContentDTO.class);
                           contentDTOs.add(item);
                           contentUidList.add(item.uid);
                       }
                       notifyDataSetChanged();
                   }
               }
           });
           Log.d("#### Detailed Frag","addsnapshot 밖에 잇는거");

        }
        @Nullable
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_detail,parent,false);

            return new CustomViewHolder(view);
        }

        @Override //그 뷰에 나올 내용들을 정리하는 것.
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, final int position) {
            final int finalPosition = position;

            ((CustomViewHolder)holder).name.setText(contentDTOs.get(position).userId);
            ((CustomViewHolder)holder).name.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Fragment fragment = new UserFragment();
                    Bundle bundle = new Bundle();
                    bundle.putString("destinationUid",contentDTOs.get(position).uid);
                    bundle.putString("userID",contentDTOs.get(position).userId);
                    fragment.setArguments(bundle);
                    getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.main_frame,fragment)
                            .commit();
                }
            });
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
                //favories에는 없으면 0이지만, 원래는 uid가 들어간다. 그래야 누가 좋아요를 했는지 알 수 있다.
                //그래서 현재 user가 만약에 좋아요를 눌렀으면 거기에 favorites 에 자신의 uid가 들어가게 된다. (주민등록번호마냥)
                //그래서 faovirte를 열어보고 자신이 눌렀으면 눌렀다는 표시를 보여줘야 하므로 하트가 색칠되게 된다.
                //containsKey가 아무래도 그냥 그 있다 없다를 return 하는 그것인듯..
            }else{
                ((CustomViewHolder)holder).favorite.setImageResource(R.drawable.ic_favorite_border);
            }
            ((CustomViewHolder)holder).favoritecount.setText("좋아요 "+ contentDTOs.get(position).favoriteCount+" 개");

            ((CustomViewHolder)holder).comment.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(v.getContext(),CommentActivity.class);
                    intent.putExtra("destinationUid",contentDTOs.get(position).uid);
                    Log.d("finalposition","number is "+finalPosition);
                    intent.putExtra("imageUid",contentDTOs.get(finalPosition).photoid);
                    Log.d("DetailViewFragment",contentUidList.get(finalPosition)==null ? "NULL": contentUidList.get(finalPosition));
                    startActivity(intent);
                }
            });
        }
        private void favoriteEvent(final int position) {
            Log.d("tag"," position index : ."+contentUidList.get(position));
            //지금 contentUidList 에는 어느사진인지가 들어가 있다. 그래야 그 사진에 접근해서 좋아요를 누르니깐 ....
            final DocumentReference docref = firestore.collection("userClothes").document(contentDTOs.get(position).photoid);
            firestore.runTransaction(new com.google.firebase.firestore.Transaction.Function<Void>() {
                @Nullable
                @Override
                public Void apply(@NonNull com.google.firebase.firestore.Transaction transaction) throws FirebaseFirestoreException {
                    String uid = mAuth.getCurrentUser().getUid();
                    ContentDTO item  = transaction.get(docref).toObject(ContentDTO.class);
                    Log.d("tag","내가 고른 아이템의 photoid는 ...? "+item.photoid);
                    if(item.favorites.containsKey(uid)){
                        item.favoriteCount = item.favoriteCount-1;
                        item.favorites.remove(uid);
                    }else{
                        //좋아요를 누르지 않은 상태를 말함. -> 누르는 상황으로 변해가는 것임.
                        item.favoriteCount = item.favoriteCount+1;
                        item.favorites.put(uid,true);
                        favoriteAlarm(contentDTOs.get(position).uid);
                    }
                    transaction.set(docref,item);
                    return null;
                }
            });
        }

        @Override
        public int getItemCount(){
            //return contentDTOs.size();
            return null!=contentDTOs?contentDTOs.size():0;
            //return 4;
        }



        public void favoriteAlarm (String destinationUid){
            AlarmDTO alarmDTO = new AlarmDTO();
            alarmDTO.destinationUid = destinationUid;
            alarmDTO.userId = mAuth.getCurrentUser().getEmail();
            alarmDTO.uid = mAuth.getCurrentUser().getUid();
            alarmDTO.kind = 0;
            FirebaseFirestore.getInstance().collection("alarms").document().set(alarmDTO);

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
