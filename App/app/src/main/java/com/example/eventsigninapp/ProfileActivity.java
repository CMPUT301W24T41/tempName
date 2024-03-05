package com.example.eventsigninapp;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.content.Intent;
import androidx.annotation.Nullable;

import com.squareup.picasso.Picasso;


public class ProfileActivity extends AppCompatActivity implements EditProfileFragment.OnProfileUpdateListener{

    TextView firstName;
    TextView lastName;
    TextView contact;

    ImageView profPic;
    UserController userController = new UserController();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile_layout);


        getSupportFragmentManager().beginTransaction().add(R.id.toolbarFragmentContainer, new ToolbarFragment()).commit();

        Button editButton = findViewById(R.id.editButton);
        firstName = findViewById(R.id.user_first_name);
        lastName = findViewById(R.id.user_last_name);
        contact = findViewById(R.id.user_number);
        profPic = findViewById(R.id.profilePicture);



        firstName.setText(userController.getUser().getFirstName());
        lastName.setText(userController.getUser().getLastName());
        contact.setText(userController.getUser().getContact());
        Picasso.get().load(userController.getUser().getPicture()).into(profPic);

        profPic.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                UserController.selectImage(ProfileActivity.this);
            }


        });
        editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditProfileFragment editProfileFragment = new EditProfileFragment();
                editProfileFragment.setOnProfileUpdateListener(ProfileActivity.this);

                editProfileFragment.show(getSupportFragmentManager(), "profileEditDialog");

            }


        });

    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {

            Uri imageUri = data.getData();

            userController.uploadProfilePicture(imageUri);
            Picasso.get().load(imageUri).into(profPic);



        }
    }




    @Override
    public void onProfileUpdate(String newFirstName, String newLastName, String newContact, Uri newPicture) {
        firstName.setText(newFirstName);
        lastName.setText(newLastName);
        contact.setText(newContact);
        Picasso.get().load(newPicture).into(profPic);

    }
}

