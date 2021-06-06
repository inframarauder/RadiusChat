package com.example.radiuschat;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class RegisterNumber extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_number);
        setTitle("Register your mobile number here");
    }

    public void handleRegistrationClick(View v){
        String phoneNumber =((EditText)findViewById(R.id.editTextPhone)).getText().toString();

        //save to firebase...
        FirebaseDatabase db = FirebaseDatabase.getInstance();
        DatabaseReference root = db.getReference();
        root.setValue(phoneNumber);


        //after success, save to shared preference:
        SharedPreferences pref = getSharedPreferences("com.example.radiuschat.users", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString("phoneNumber",phoneNumber);
        editor.apply();

        //redirect permanently to main activity
        startActivity(new Intent(this,MainActivity.class));
        finish();
    }
}