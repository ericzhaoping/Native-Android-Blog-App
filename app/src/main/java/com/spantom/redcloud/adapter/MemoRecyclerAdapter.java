package com.spantom.redcloud.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Point;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateFormat;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.spantom.redcloud.MainActivity;
import com.spantom.redcloud.R;
import com.spantom.redcloud.activities.MemoDetailsActivity;
import com.spantom.redcloud.model.Memo;

import org.w3c.dom.Text;

import java.lang.reflect.Field;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class MemoRecyclerAdapter extends RecyclerView.Adapter<MemoRecyclerAdapter.ViewHolder> {

    public List<Memo> memo_list;
    public Context context;

    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth firebaseAuth;

    public MemoRecyclerAdapter(List<Memo> memo_list){

        this.memo_list = memo_list;

    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.memo_list_item, parent, false);
        context = parent.getContext();
        firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {

        holder.setIsRecyclable(false);

        final String memoId = memo_list.get(position).MemoId;
        final String currentUserId = firebaseAuth.getCurrentUser().getUid();

        final String title_data = memo_list.get(position).getTitle();
        //holder.setTitleText(title_data);

        final String tagline_data = memo_list.get(position).getTagline();
        //holder.setTaglineText(tagline_data);

        final String description_data = memo_list.get(position).getDescription();
        holder.setDescriptionText(description_data);

        final String location_data = memo_list.get(position).getLocation();
        //holder.setLocationText(location_data);

        final String image_url = memo_list.get(position).getImage_url();
        final String thumbUri = memo_list.get(position).getImage_thumb();
        holder.setMemoImage(image_url, thumbUri);

        String user_id = memo_list.get(position).getUser_id();
        //User Data will be retrieved here...
        firebaseFirestore.collection("Users").document(user_id).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                if(task.isSuccessful()){

                    String userUserName = task.getResult().getString("user_name");
                    String userFullName = task.getResult().getString("full_name");
                    String userImage = task.getResult().getString("image");

                    //holder.setUserData(userName, userImage);


                } else {

                    //Firebase Exception

                }

            }
        });

        try {
            long millisecond = memo_list.get(position).getTimestamp().getTime();
            String dateString = DateFormat.format("MM/dd/yyyy", new Date(millisecond)).toString();
            //holder.setTime(dateString);
        } catch (Exception e) {

            Toast.makeText(context, "Exception : " + e.getMessage(), Toast.LENGTH_SHORT).show();

        }

        //Get Likes Count
        firebaseFirestore.collection("Memos/" + memoId + "/Likes").addSnapshotListener(((MainActivity) context), new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {

                if(!documentSnapshots.isEmpty()){

                    int count = documentSnapshots.size();

                    holder.updateLikesCount(count);

                } else {

                    holder.updateLikesCount(0);

                }

            }
        });


        //Get Likes
        firebaseFirestore.collection("Memos/" + memoId + "/Likes").document(currentUserId).addSnapshotListener(((MainActivity) context), new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {

                if(documentSnapshot.exists()){

                    holder.memoLikeBtn.setImageResource(R.mipmap.action_like_accent);

                } else {

                    holder.memoLikeBtn.setImageResource(R.mipmap.action_like_gray);

                }

            }
        });

        //Likes Feature
        holder.memoLikeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                firebaseFirestore.collection("Memos/" + memoId + "/Likes").document(currentUserId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                        if(!task.getResult().exists()){

                            Map<String, Object> likesMap = new HashMap<>();
                            likesMap.put("timestamp", FieldValue.serverTimestamp());

                            firebaseFirestore.collection("Memos/" + memoId + "/Likes").document(currentUserId).set(likesMap);

                        } else {

                            firebaseFirestore.collection("Memos/" + memoId + "/Likes").document(currentUserId).delete();

                        }

                    }
                });
            }
        });

        holder.memoImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, MemoDetailsActivity.class);
                intent.putExtra("Title", title_data);
                intent.putExtra("Tagline", tagline_data);
                intent.putExtra("Description", description_data);
                intent.putExtra("Location", location_data);
                intent.putExtra("Thumbnail", image_url);
                context.startActivity(intent);
            }
        });

    }


    @Override
    public int getItemCount() {
        return memo_list.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private View mView;

        private TextView titleView, taglineView, descriptionView, locationView;
        private ImageView memoImageView;

        private ImageView memoLikeBtn;
        private TextView memoLikeCount;
        private CardView cardView;



        public ViewHolder(View itemView) {
            super(itemView);
            mView = itemView;

            memoLikeBtn = mView.findViewById(R.id.memo_like_btn);
            cardView = mView.findViewById(R.id.main_memo_post);

        }


        public void setDescriptionText(String descText){

            descriptionView = mView.findViewById(R.id.memo_description);
            descriptionView.setText(descText);

        }


        public void setMemoImage(String downloadUri, String thumbUri){

            memoImageView = mView.findViewById(R.id.memo_image);

            RequestOptions requestOptions = new RequestOptions();
            requestOptions.placeholder(R.drawable.image_placeholder);

            Glide.with(context).applyDefaultRequestOptions(requestOptions).load(downloadUri).thumbnail(
                    Glide.with(context).load(thumbUri)
            ).into(memoImageView);

        }

        public void updateLikesCount(int count){

            memoLikeCount = mView.findViewById(R.id.memo_like_count);
            memoLikeCount.setText(count + " Likes");

        }

    }

}
