package com.example.photoblog;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import de.hdodenhof.circleimageview.CircleImageView;

public class BlogRecyclerAdapter extends RecyclerView.Adapter<BlogRecyclerAdapter.ViewHolder> {

    public List<BlogPost>blog_post;
    Context context;
    private FirebaseFirestore firestore;

    BlogRecyclerAdapter(List<BlogPost>blog_post){
        this.blog_post=blog_post;
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        firestore=FirebaseFirestore.getInstance();
        context=parent.getContext();
        view= LayoutInflater.from(parent.getContext()).inflate(R.layout.blog_list_item,parent,false);
        return new ViewHolder(view);
    }
    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
        String descText=blog_post.get(position).getDescription();
        holder.setDescText(descText);
        String downloadUri=blog_post.get(position).getImage_url();
        holder.setImage(downloadUri);
        String userId=blog_post.get(position).getUserId();
        try {
            long milliseconds = blog_post.get(position).getTimestamp().getTime();
            SimpleDateFormat dateFormatNew = new SimpleDateFormat("dd/MM/yyyy");
            String date = dateFormatNew.format(milliseconds);
            holder.setDate(date);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        //date = DateFormat.format("MM/dd/yyyy",new Date(milliseconds).toString());
        firestore.collection("Users").document(userId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful())
                {
                    if(task.getResult().exists())
                    {
                        String username=task.getResult().getString("name");
                        holder.setUsername(username);
                        String userProfile=task.getResult().getString("image");
                        holder.setProfileImage(userProfile);
                    }
                }
            }
        });
    }
    @Override
    public int getItemCount() {
        return blog_post.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder
    {
        private TextView descView,usernameView,dateView;
        private CircleImageView profileImage;
        private ImageView blogImage;
        private View mview;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            mview=itemView;
        }

        public void setDescText(String descText){
            descView=mview.findViewById(R.id.blogDesc);
            descView.setText(descText);
    }
    public void setImage(String downloadUri){
            blogImage=mview.findViewById(R.id.blogImage);
            RequestOptions requestOptions=new RequestOptions();
            requestOptions.placeholder(R.drawable.image_placeholder);
        Glide.with(context).load(downloadUri).apply(requestOptions).into(blogImage);
    }
    public void setUsername(String username)
    {
        usernameView=mview.findViewById(R.id.blogUsername);
        usernameView.setText(username);
    }
    public void setProfileImage(String userProfile)
    {
        profileImage=mview.findViewById(R.id.userImage);
        RequestOptions requestOptions= new RequestOptions();
        requestOptions.placeholder(R.drawable.profile_placeholder);
        Glide.with(context).load(userProfile).apply(requestOptions).into(profileImage);
    }
    public void setDate(String date)
    {
        dateView=mview.findViewById(R.id.blogDate);
        dateView.setText(date);
    }
    }
}
