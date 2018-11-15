package com.example.wewish;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FirebaseFirestore;

public class DataService extends Service {
    public DataService() {
    }
    String TAG = "Dataservice";
    private FirebaseDatabase database;
    private DatabaseReference reference;
    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "onBind");
        return binder;
    }
    public class DataServiceBinder extends Binder {
        DataService getService() {
            return DataService.this;
        }
    }
    private IBinder binder = new DataServiceBinder();


    FirebaseFirestore db;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        db= FirebaseFirestore.getInstance();

        //database=FirebaseDatabase.getInstance();
        //reference=database.getReference().child("WishLists");

        return START_STICKY;

    }

    public void sendBroadcast(){
        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction("startIntent");
        LocalBroadcastManager.getInstance(DataService.this).sendBroadcast(broadcastIntent);
        Log.d(TAG,"Broadcast sent");
    }


    public void saveNewUser(String name){

        final String newname=name;

        Query query = reference.child(name);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    Toast.makeText(getApplicationContext(), R.string.alreadyExist, Toast.LENGTH_LONG).show();
                }
                else{
                    reference.child(newname).setValue("");
                    sendBroadcast();
                    }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }


    public void login(String name){
        final String newname=name;
        Query query = reference.child(name);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    sendBroadcast();
                }
                else{
                    Toast.makeText(getApplicationContext(), R.string.wrongUsername, Toast.LENGTH_LONG).show();
                    }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }


}
