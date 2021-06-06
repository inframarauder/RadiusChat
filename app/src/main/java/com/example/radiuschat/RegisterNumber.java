package com.example.radiuschat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import com.example.radiuschat.utils.User;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

public class RegisterNumber extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_number);
        setTitle("Register your mobile number here");
    }

    public void handleRegistrationClick(View v){
        String phoneNumber =((EditText)findViewById(R.id.editTextPhone)).getText().toString();

        //create firebase reference...
        FirebaseDatabase db = FirebaseDatabase.getInstance();
        DatabaseReference userRef = db.getReference("users");

        //query and check if user already exists...
        Query checkUser = userRef.orderByChild("phoneNumber").equalTo(phoneNumber);
        checkUser.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @org.jetbrains.annotations.NotNull DataSnapshot snapshot) {
                if(!snapshot.exists()){
                    //if user does not exist, create and insert new user record...
                    userRef.push().setValue(new User(phoneNumber));
                }
                //login user by writing user data to shared preference...
                loginUser(phoneNumber);
            }

            @Override
            public void onCancelled(@NonNull @org.jetbrains.annotations.NotNull DatabaseError error) {
                Log.d("onCancelled",error.getDetails());
            }
        });
    }


    private void loginUser(String phoneNumber){
        //writing user to shared preference .....
        SharedPreferences pref = getSharedPreferences("com.example.radiuschat.users", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString("phoneNumber",phoneNumber);
        editor.apply();

        //redirect permanently to main activity
        startActivity(new Intent(this,MainActivity.class));
        finish();
    }
}