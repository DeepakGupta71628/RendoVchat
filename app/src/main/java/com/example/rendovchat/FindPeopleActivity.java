package com.example.rendovchat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

public class FindPeopleActivity extends AppCompatActivity {
private RecyclerView findFriendsList;
EditText searchET;
private String str="";
private DatabaseReference usreRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        usreRef= FirebaseDatabase.getInstance().getReference().child("User");
        setContentView(R.layout.activity_find_people);
        searchET=findViewById(R.id.serach_user_text);
        findFriendsList=findViewById(R.id.find_friends_list);

        findFriendsList.setLayoutManager(new LinearLayoutManager(getApplicationContext()));

        searchET.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                if(searchET.getText().toString().equals("")){
                    Toast.makeText(FindPeopleActivity.this, "Please Write a name to search", Toast.LENGTH_SHORT).show();
                }
                else {
                  str=charSequence.toString();

                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                onStart();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseRecyclerOptions<Contacts> options=null;

        if (str.equals("")) {

            options=new FirebaseRecyclerOptions.Builder<Contacts>().setQuery(usreRef,Contacts.class).build();

        }
        else{
            options=new FirebaseRecyclerOptions.Builder<Contacts>()
                    .setQuery(usreRef
                            .orderByChild("name")
                                 .startAt(str)
                                 .endAt(str+"\uf8ff")
                            ,Contacts.class).build();
        }

       FirebaseRecyclerAdapter<Contacts,FindFriendViewHolder> firebaseRecyclerAdapter=new FirebaseRecyclerAdapter<Contacts, FindFriendViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull FindFriendViewHolder holder, final int position, @NonNull final Contacts model) {
                holder.userNameTxt.setText(model.getName());
               //Picasso.get().load(model.getImage()).into(holder.profileImageView1);

               // Picasso.get().load(model.getImage()).into(holder.profileImageView1);


                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String visit_user_id=getRef(position).getKey();
                        Intent intent=new Intent(FindPeopleActivity.this,ProfileActivity.class);
                        intent.putExtra("visit_user_id",visit_user_id);
                        intent.putExtra("profile_image",model.getImage());
                        intent.putExtra("Profile_name",model.getName());

                        startActivity(intent);
                    }
                });
            }

            @NonNull
            @Override
            public FindFriendViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
               View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.contact_desigh,parent,false);
               FindFriendViewHolder viewHolder=new FindFriendViewHolder(view);
               return viewHolder;
            }
        };
        findFriendsList.setAdapter(firebaseRecyclerAdapter);
        firebaseRecyclerAdapter.startListening();
    }

    public  static class FindFriendViewHolder extends RecyclerView.ViewHolder {
        TextView userNameTxt;
        Button  videoCallBtn;
        ImageView profileImageView1;
        RelativeLayout cardView;

        public FindFriendViewHolder(@NonNull View itemView) {

            super(itemView);

            userNameTxt=itemView.findViewById(R.id.name_contact);
            videoCallBtn=itemView.findViewById(R.id.call_btn);
            cardView=itemView.findViewById(R.id.card_view1);
            profileImageView1=itemView.findViewWithTag(R.id.image_contact);

            videoCallBtn.setVisibility(View.GONE);
        }
    }
}
