package com.example.radiuschat;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import com.example.radiuschat.utils.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class RegisterNumber extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_number);
        setTitle("Register your mobile number here");
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public void registerUser(View v){
        String phoneNumber = ((EditText)findViewById(R.id.editTextPhone)).getText().toString();
        String name = ((EditText)findViewById(R.id.editTextTextPersonName)).getText().toString();

        User user = new User(phoneNumber,name);

        //create firebase reference...
        FirebaseDatabase db = FirebaseDatabase.getInstance();
        DatabaseReference userRef = db.getReference("users");

        //query and check if user already exists...
        Query checkUser = userRef.orderByChild("phoneNumber").equalTo(phoneNumber);
        checkUser.get().addOnCompleteListener(task -> {
            String userId;

            if(task.isSuccessful()){
                Map<String,User> result = (Map<String,User>) task.getResult().getValue();

                if(result != null){
                    //if user exists, get userId
                    userId = result.keySet().stream().findFirst().get();
                }else{
                    //if user does not exist, create the user
                    userId = userRef.push().getKey();
                    userRef.child(userId).setValue(user);
                }
                Log.d("userId",userId);
                loginUser(userId);
            }else{
                Log.e("firebase_error","Task failed successfully",task.getException());
            }
        });
    }


    private void loginUser(String userId){
        //writing user to shared preference .....
        SharedPreferences pref = getSharedPreferences("com.example.radiuschat.users", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString("userId",userId);
        editor.apply();

        //redirect permanently to main activity
        startActivity(new Intent(this,MainActivity.class));
        finish();
    }
}