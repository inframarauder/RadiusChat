package com.example.radiuschat;

import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.radiuschat.utils.User;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.CancellationTokenSource;
import com.google.android.gms.tasks.OnSuccessListener;

public class MainActivity extends AppCompatActivity {

    private FusedLocationProviderClient client;
    private User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setTitle("Welcome To Radius Chat!");

        //access registered user from shared pref:
        user = getRegisteredUser();
        if(user != null){
            //stay on this activity and work with location
            sendLocationToFirebase();
        }else{
            //send to registration activity
            startActivity(new Intent(this,RegisterNumber.class));
            finish();
        }

    }

    private User getRegisteredUser(){
        SharedPreferences pref = getSharedPreferences("com.example.radiuschat.users", Context.MODE_PRIVATE);
        String phoneNumber = pref.getString("phoneNumber","");

        return phoneNumber.length() == 10?new User(phoneNumber) : null;
    }

    private void sendLocationToFirebase(){
        //create fused location client
        client = LocationServices.getFusedLocationProviderClient(this);

        //fetch last location using the client and write to firebase
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (getApplicationContext().checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                client.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if (location != null) {
                            double lat = location.getLatitude();
                            double lon = location.getLongitude();

                            user.setLat(lat);
                            user.setLon(lon);

                            Log.d("user",user.getPhoneNumber() + "," + user.getLat()+","+user.getLon());

                            //update location of user in firebase here...
                        }
                    }
                });
            } else {
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            }
        }
    }

    public void logoutUser(View v){
        SharedPreferences pref = getSharedPreferences("com.example.radiuschat.users", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.clear();
        editor.apply();

        startActivity(new Intent(this,RegisterNumber.class));
        finish();
    }

}