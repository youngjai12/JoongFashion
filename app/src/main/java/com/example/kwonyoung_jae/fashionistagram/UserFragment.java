package com.example.kwonyoung_jae.fashionistagram;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.Transaction;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.UploadTask;

import java.net.NoRouteToHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class UserFragment extends Fragment {
    Context mainact = null;
    ListenerRegistration follow_LR , following_LR , image_profileListenerRegistration, recycle_LR;
    View fragView;
    FirebaseAuth mAuth;
    DatabaseReference dbref;
    FirebaseFirestore firestore;
    String pagevector;
    FirebaseStorage storage;
    String uid, currentUid , photoUri;
    Button follow_btn ;
    ImageView profile_image;
    TextView postCount;
    RecyclerView recyclerView;
    Uri profile_uri;
    int PICK_PROFILE_FROM_ALBUM=10;
    TextView following_count , follower_count;
    private HomeActivity activity;
    LinearLayout friends;
    public UserFragment() {
        // Required empty public constructor
    }
    @Override
    public void onAttach(Context context){
        super.onAttach(context);
        if(context instanceof Activity){
            activity = (HomeActivity) context;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        fragView = inflater.inflate(R.layout.fragment_user, container, false);
        mAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        currentUid = mAuth.getCurrentUser().getUid();
        postCount = fragView.findViewById(R.id.post_count);
        follow_btn = fragView.findViewById(R.id.account_followbtn);
        profile_image = fragView.findViewById(R.id.account_profile);
        following_count = fragView.findViewById(R.id.following_count);
        friends = fragView.findViewById(R.id.friends_list);
        friends.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent1 = new Intent(activity,SimilarityActivity.class);
                intent1.putExtra("destinationUID",uid);
                startActivity(intent1);

            }
        });
        follower_count = fragView.findViewById(R.id.follower_count);

        profile_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("tag","profile image area is clicked");
                if(ContextCompat.checkSelfPermission(activity, Manifest.permission.READ_EXTERNAL_STORAGE) ==
                        PackageManager.PERMISSION_GRANTED){
                    Intent photo_picker = new Intent(Intent.ACTION_PICK);
                    photo_picker.setType("image/*");
                    getActivity().startActivityForResult(photo_picker,PICK_PROFILE_FROM_ALBUM);
                    //이게 넘어간다. 그리고 activityResult 함수를 여기서 쓰는 것이 아니다.. 그걸 fragment가 속하는 곳으로 간다.
                    //fragment에는 그런 것을 할수가 없다. 그래서 userFragment에 하는게 아니라 넘어가서 HomeActivity에 하는 것임.
                }

            }
        });

        recyclerView = fragView.findViewById(R.id.account_recycler);
        recyclerView.setAdapter(new UserFragmentRecyclerViewAdatper());
        recyclerView.setLayoutManager(new GridLayoutManager(getActivity(),3));



        if(getArguments()!=null){
            uid = getArguments().getString("destinationUid");
            if(uid!=null && uid.equals(currentUid)){ // 본인계정인 경우 로그아웃, toolbar 기본으로 설정.
                follow_btn.setText("로그아웃");
                follow_btn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        getActivity().finish();
                        Intent intent = new Intent(getActivity(), MainActivity.class);
                        getActivity().startActivity(intent);
                        mAuth.signOut();
                    }
                });
            }else{
                follow_btn.setText("팔로우");
                activity.toolbarImage.setVisibility(View.GONE);
                activity.backbutton.setVisibility(View.VISIBLE);
                activity.toolbarname.setVisibility(View.VISIBLE);
                activity.toolbarname.setText(getArguments().getString("userID"));
                // getArgument??
                activity.backbutton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        activity.nav.setSelectedItemId(R.id.action_home);
                    }
                });
                follow_btn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        requestFollow();
                    }
                });
            }
        }


        getFollowing();
        getFollower();

        return fragView;
    }
    @Override
    public void onResume(){
        super.onResume();
        getProfileImage();
    }

    public void getProfileImage(){
        image_profileListenerRegistration = firestore.collection("profileImages").document(uid)
                .addSnapshotListener(new EventListener<DocumentSnapshot>() {
                    @Override
                    public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {
                        if(documentSnapshot.exists()){
                            Map<String,Object> item = documentSnapshot.getData();
                            String profile_url = item.get(uid).toString();
                            Glide.with(activity).load(profile_url).apply(new RequestOptions().circleCrop())
                                    .into(profile_image);
                        }else{
                            Toast.makeText(getContext(),"document가 없엉",Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    public void getFollower(){
        follow_LR = firestore.collection("users").document(uid).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            FollowDTO followDTO = new FollowDTO();

            @Override
            public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {
                followDTO = documentSnapshot.toObject(FollowDTO.class);
                if(followDTO==null){
                    Log.d("#### 과연 ","followDTO가 null");
                    return;
                }
                follower_count.setText(String.valueOf(followDTO.followerCount));

                if(followDTO.followers.containsKey(currentUid)){
                    follow_btn.setText("팔로우 취소");
                    follow_btn.getBackground().setColorFilter(ContextCompat.getColor(activity,R.color.colorLightGrey), PorterDuff.Mode.MULTIPLY);
                }else{
                    if(!uid.equals(currentUid)){
                        follow_btn.setText("팔로우");
                        follow_btn.getBackground().setColorFilter(null);
                    }
                }
            }
        });
    }
    public void getFollowing(){
        following_LR = firestore.collection("users").document(uid).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            FollowDTO followDTO = new FollowDTO();
            @Override
            public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {
                followDTO = documentSnapshot.toObject(FollowDTO.class);
                if(followDTO==null){
                    return;
                }else {
                    following_count.setText(String.valueOf(followDTO.followingCount));
                }
            }
        });
    }


    class UserFragmentRecyclerViewAdatper extends RecyclerView.Adapter<RecyclerView.ViewHolder>{
        private ArrayList<ContentDTO> contentDTOs1;

        UserFragmentRecyclerViewAdatper(){
            //여기서 내가 올린 이미지들만 불러와야한다. where equls to
            uid = getArguments().getString("destinationUid");
            Toast.makeText(getContext(),"uid = "+uid,Toast.LENGTH_LONG).show();

            firestore.collection("userClothes").whereEqualTo("uid",uid)

            .addSnapshotListener(new EventListener<QuerySnapshot>() {
                @Override
                public void onEvent(QuerySnapshot queryDocumentSnapshots, FirebaseFirestoreException e) {
                    contentDTOs1= new ArrayList<>();
                   Log.d("###### 과연 userfrag : ","이건 addsnapshot 안에 있는거");
                    if(queryDocumentSnapshots==null){

                    }else{

                        for(QueryDocumentSnapshot doc : queryDocumentSnapshots){
                            ContentDTO item = doc.toObject(ContentDTO.class);
                            contentDTOs1.add(item);
                        }
                        postCount.setText(String.valueOf(contentDTOs1.size()));
                        notifyDataSetChanged();
                        getVector(contentDTOs1);

                    }
                }
            });
            Log.d("###### userfrag : ","이건 addsnapshot 밖에 있는거");




        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            Log.d("### 과연 ","oncreate view holder 안에서 실행되는 것이다.");
            int width = getResources().getDisplayMetrics().widthPixels / 3;
            ImageView imageView = new ImageView(parent.getContext());
            imageView.setLayoutParams(new LinearLayoutCompat.LayoutParams(width, width));
            return new CustomViewHolder(imageView);
        }
        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, final int position) {
            Log.d("###### userfrag : ","이건 OnBindViewHodler에 있는거임.  밖에 있는거");

            Glide.with(holder.itemView.getContext())

                    .load(contentDTOs1.get(position).imageUrl).apply(new RequestOptions().centerCrop())

                    .into(((CustomViewHolder) holder).user_image);

            ((CustomViewHolder)holder).user_image.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    CharSequence colors[] = new CharSequence[]{"Yes", "NO"};
                    AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                    builder.setTitle("취향 목록에서 제거하시겠습니까?");
                    builder.setItems(colors, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (which == 0) { // 제거하겠다.
                                firestore.collection("userClothes").document(contentDTOs1.get(position).photoid).delete()
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                Toast.makeText(getContext(),"성공적으로 지워졌습니다",Toast.LENGTH_LONG).show();
                                            }
                                        });
                            } else {
                                Log.d("############","취소 버튼 눌러버렸는데??");
                            }
                        }
                    });
                    builder.show();

                }
            });



        }

        @Override
        public int getItemCount() {
            return null!=contentDTOs1?contentDTOs1.size():0;

        }

        private class CustomViewHolder extends RecyclerView.ViewHolder {
            public ImageView user_image;
            public CustomViewHolder(ImageView imageView) {
                super(imageView);
                this.user_image = imageView;
            }
        }
    }



    public void requestFollow(){
        //현재 firestore에는 users라는 것이 일단 존재하진 않음.없는이름을 collection해서 있는 것 처럼 하면
        //알아서 자동으로 생긴다.
        final DocumentReference docref = firestore.collection("users").document(currentUid);

        final String myvector, friendvector;
        firestore.runTransaction(new Transaction.Function<Void>() {
            @Nullable
            @Override
            public Void apply(@NonNull Transaction transaction) throws FirebaseFirestoreException {

                FollowDTO followDTO = transaction.get(docref).toObject(FollowDTO.class);
                if (followDTO == null) {//following 같은걸 하지 않아서 아무 데이터가 없을 때 ...
                    followDTO = new FollowDTO();
                    followDTO.followingCount = 1;
                    followDTO.followings.put(uid, true); // following은 내가 제 3자를 따라가는 경우를 말한다.
                    //코드 상 보면 currentUser가 아니라 uid라는 것은 제 3자를 말하는 것이다.
                    transaction.set(docref, followDTO);
                    return null;
                }
                if (followDTO.followings.containsKey(uid)) {
                    followDTO.followingCount = followDTO.followingCount - 1;
                    followDTO.followings.remove(uid);
                } else {
                    followDTO.followingCount = followDTO.followingCount + 1;
                    followDTO.followings.put(uid, true);
                }
                transaction.set(docref, followDTO);
                return null;
            }

        });
        final DocumentReference follower_ref = firestore.collection("users").document(uid);
        firestore.runTransaction(new Transaction.Function<Void>() {
            @Nullable
            @Override
            public Void apply(@NonNull Transaction transaction) throws FirebaseFirestoreException {
                FollowDTO followDTO = transaction.get(follower_ref).toObject(FollowDTO.class);
                if(followDTO==null){
                    followDTO = new FollowDTO();
                    followDTO.followerCount =1;
                    followDTO.followers.put(currentUid,true);
                    transaction.set(follower_ref,followDTO);
                    return null;
                    //return @runTransaction이라는 것은 현재 어디를 종료 시켜야할지가 불분명할 때
                    //어디의 함수를 return 한다는 것을 명시해주기 위해서 return @transaction이라고 하는 것이다.

                }
                //제 3의 유저를 내가 팔로잉하고 있을 때
                if(followDTO.followers.containsKey(currentUid)){
                    followDTO.followerCount = followDTO.followerCount -1;
                    followDTO.followers.remove(currentUid);
                }else{
                    followDTO.followerCount = followDTO.followerCount +1;
                    followDTO.followers.put(currentUid,true);
                }
                transaction.set(follower_ref,followDTO);
                return null;
            }
        });



    }

    public String getVector(ArrayList<ContentDTO> arr){
        Log.d("#####몇","개나 거칠까??####"+ arr.size());
        int[] index ={25,26,27,28,29,30,31,32,33,34,35,36,37,38,39,40,41,42,43,44,45,46};
        int[] feature_vector = new int[index.length];
        for(int i = 0; i< arr.size(); i++){
            Log.d("##### 무슨사진?","##### 사진은 바로 #####"+ arr.get(i).photoid);
            for(int j=0;j<index.length;j++){
                //Log.d("##### j " ," index는 무엇이 될까 ?"+j);
                feature_vector[j] =feature_vector[j]+ arr.get(i).feature.charAt(index[j])-'0';
            }
        }
        String str_feature =" ";
        for(int i=0;i<index.length;i++){
            str_feature = str_feature + (char)(feature_vector[i]+48);
        }
        Log.d("##### 과연 ##","############# 사용자의 vector는 ###### "+str_feature );
        firestore.collection("users").document(uid).update("vector",str_feature);
        firestore.collection("users").document(uid).update("uid",uid);
        return str_feature;

    }

    @Override
    public void onStop(){
        super.onStop();
        follow_LR.remove();
        following_LR.remove();
        //image_profileListenerRegistration.remove();
        //recycle_LR.remove();
    }




}