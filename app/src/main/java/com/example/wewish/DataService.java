package com.example.wewish;

import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;


import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class DataService extends Service {

    String TAG = "Dataservice";
    AsyncTaskStart BirthdayTask;
    Boolean running;
    CurrentUser currentUser;
    public DataService() {
    }
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
        running=true;
        BirthdayTask = new AsyncTaskStart();
        BirthdayTask.execute();

        return START_STICKY;

    }

    public class AsyncTaskStart extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... voids) {
            while (running) {
                try {
                    Log.d(TAG, "BirthdayTask started");
                    checkForBirthday();
                    //Thread sleeps for 1 day
                    Thread.sleep(86400000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }
    }

    public void sendBroadcast(){
        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction("startIntent");
        LocalBroadcastManager.getInstance(DataService.this).sendBroadcast(broadcastIntent);
        Log.d(TAG,"Broadcast sent");
    }


    public void saveNewUser(final String email,final String username, final String birthdate){
    Map<String,Object> user = new HashMap<>();
    user.put("email",email);
    user.put("username",username);
    user.put("birthdate",birthdate);

        db.collection("users").document(email).set(user)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void avoid) {
                        Log.d(TAG,"User added");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error adding document", e);
                    }
                });

    }


    public void login(String email){
        DocumentReference docRef = db.collection("users").document(email);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        Log.d(TAG, "DocumentSnapshot data: " + document.getData());
                    } else {
                        Log.d(TAG, "No such document");
                    }
                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                }
            }
        });
    }

    public void checkForBirthday(){
//        currentUser=new CurrentUser();
//        if(currentUser!=null){
//        String user = currentUser.getCurrentUser().getEmail();}
        //DocumentReference docRef = db.collection("users").document(user);
        /*docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        Log.d(TAG, "DocumentSnapshot data: " + document.getData());
                    } else {
                        Log.d(TAG, "No such document");
                    }
                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                }
            }
        });*/

    }





}
