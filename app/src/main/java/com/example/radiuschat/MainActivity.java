package com.example.radiuschat;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.radiuschat.utils.User;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.CancellationTokenSource;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private FusedLocationProviderClient client;
    String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setTitle("Welcome To Radius Chat!");

        //access registered user from shared pref:
        userId = getRegisteredUserId();
        if(userId.length() > 0){
            /**
             * if user is registered, the following method retrieves current location,
             * writes it to firebase and queries for all nearby users
             */
            performLocationOps();
        }else{
            //send to registration activity
            startActivity(new Intent(this,RegisterNumber.class));
            finish();
        }

    }


    private String getRegisteredUserId(){
        SharedPreferences pref = getSharedPreferences("com.example.radiuschat.users", Context.MODE_PRIVATE);
        String userId = pref.getString("userId","");

        return userId;
    }

    private void performLocationOps(){
        //create fused location client
        client = LocationServices.getFusedLocationProviderClient(this);
        CancellationTokenSource cts = new CancellationTokenSource(); //cancellation token needed for getCurrentLocation method


        //fetch current location using the client and write to firebase
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            if (getApplicationContext().checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

                client.getCurrentLocation(LocationRequest.PRIORITY_HIGH_ACCURACY,cts.getToken()).addOnSuccessListener(location -> {
                    if (location != null) {
                        double lat = location.getLatitude();
                        double lon = location.getLongitude();

                        //write to firebase using GeoFire :
                        FirebaseDatabase db = FirebaseDatabase.getInstance();
                        DatabaseReference locationRef = db.getReference("locations");
                        GeoFire geoFire = new GeoFire(locationRef);

                        geoFire.setLocation(userId, new GeoLocation(lat, lon), (key, error) -> {
                            if (error != null) {
                                Log.d("error_geofire", error.getDetails());
                            } else {
                                //run geo query on current location
                                GeoLocation geoLocation = new GeoLocation(lat,lon);
                                double radius = 5.0;
                                runGeoQuery(geoFire,geoLocation,radius);
                            }
                        });
                    }else {
                        Log.d("coordinates", "location is null");
                    }
                });
            } else {
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            }
        }
    }

    private void runGeoQuery(GeoFire geoFire,GeoLocation geoLocation,double radius) {
        GeoQuery geoQuery = geoFire.queryAtLocation(geoLocation,radius);
        ArrayList<User> usersInRadius = new ArrayList<>();


        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onKeyEntered(String key, GeoLocation location) {
                //exclude currently logged in user:
                if(!userId.equalsIgnoreCase(key)){
                    //fetch user object from db and insert into array list:
                    DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users");
                    Query userQuery = userRef.orderByKey().equalTo(key);
                    userQuery.get().addOnCompleteListener(task -> {
                        if(task.isSuccessful()){
                            Map<String,Map> result = (Map<String,Map>) task.getResult().getValue();
                            Map<String,String> userMap = result.values().stream().findFirst().get();

                            usersInRadius.add(new User(userMap.get("phoneNumber"),userMap.get("name")));
                            renderUsersInRadius(usersInRadius);
                        }else{
                            Log.e("firebase_error","error in querying for user with given Id", task.getException());
                        }
                    });
                }
            }

            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onKeyExited(String key) {
                //fetch user object from db and remove from array list:
                DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users");
                Query userQuery = userRef.orderByKey().equalTo(key);
                userQuery.get().addOnCompleteListener(task -> {
                    if(task.isSuccessful()){
                        Map<String,Map> result = (Map<String,Map>) task.getResult().getValue();
                        Map<String,String> userMap = result.values().stream().findFirst().get();

                        usersInRadius.remove(new User(userMap.get("phoneNumber"),userMap.get("name")));
                        renderUsersInRadius(usersInRadius);
                    }else{
                        Log.e("firebase_error","error in querying for user with given Id", task.getException());
                    }
                });
            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {
                //do nothing
            }

            @Override
            public void onGeoQueryReady() {
                Log.d("geoQueryReady","GeoQuery is ready");
            }

            @Override
            public void onGeoQueryError(DatabaseError error) {
                Log.e("geoQueryError","There was an error in performing geo query", error.toException());
            }
        });
    }

    private void renderUsersInRadius(ArrayList<User> usersInRadius) {
        Log.d("usersInRadius", Arrays.toString(usersInRadius.toArray()));
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