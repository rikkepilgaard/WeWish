package com.example.wewish;

import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;


import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class DataService extends Service {

    String TAG = "Dataservice";
    AsyncTaskStart BirthdayTask;
    Boolean running;
    private FirebaseAuth mAuth;
    CurrentUser currentUser;
    ArrayList<User> othersUsers;

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
        mAuth=FirebaseAuth.getInstance();
        running=true;
        BirthdayTask = new AsyncTaskStart();
        BirthdayTask.execute();

        othersUsers = new ArrayList<>();

        getCurrentUserFromFirebase();

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

    public void sendBroadcast(String action){
        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction(action);
        LocalBroadcastManager.getInstance(DataService.this).sendBroadcast(broadcastIntent);
        Log.d(TAG,"Broadcast sent");
    }

    public void createUser(final String email, String password, final String username, final String date){

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "createUserWithEmail:success");
                            saveNewUser(email,username,date);
                            sendBroadcast("wishActivity");
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "createUserWithEmail:failure", task.getException());
                            Toast.makeText(DataService.this, "Creating user failed.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });

    }

    public void saveNewUser(final String email,final String username, final String birthdate){
        User user = new User();
        user.setEmail(email);
        user.setUserName(username);
        user.setBirthDate(birthdate);

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

    public void signIn(final String email, final String password){
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithEmail:success");
                            sendBroadcast("wishActivity");
                            login(email);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithEmail:failure", task.getException());
                            Toast.makeText(DataService.this, "Sign in failed.",
                                    Toast.LENGTH_SHORT).show();

                        }

                    }
                });
    }

    public void checkIfUserisLoggedIn(){
        if(mAuth.getCurrentUser()!=null){
            login(mAuth.getCurrentUser().getEmail());
            sendBroadcast("wishActivity");
        }
    }

    public void login(final String email){
        db.collection("users").document(email).get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (!documentSnapshot.exists()) {
                            Toast.makeText(DataService.this, "Email does not exist", Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    public void getCurrentUserFromFirebase(){
        String email = mAuth.getCurrentUser().getEmail();
        db.collection("users").document(email).get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if(documentSnapshot.exists()) {
                            User user = documentSnapshot.toObject(User.class);
                            getWishes(user);
                            //getSubscribers(user);
                            if(othersUsers.size()==0) {
                                othersUsers.add(user);
                            }
                            //sendBroadcast("newdata");
                        }
                    }
                });

    }

    public void getWishes(final User user){
        db.collection("users").document(user.getEmail()).collection("wishes").get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            ArrayList<Wish> wishList = new ArrayList<>();
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Log.d(TAG, document.getId() + " => " + document.getData());
                                Wish wish = document.toObject(Wish.class);
                                wishList.add(wish);
                            }
                            user.setWishList(wishList);
                            if(user.getEmail().equals(mAuth.getCurrentUser().getEmail())){
                            getSubscribers(user);}
                            else{sendBroadcast("subscriberdata");}
                        } else {
                            Log.d(TAG, "Error getting documents: ", task.getException());
                        }
                    }
                });

    }

    public void addNewWishList(final String email){

        db.collection("users").document(email).get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if(task.getResult().exists()) {
                            User user = task.getResult().toObject(User.class);
                            getWishes(user);
                            othersUsers.add(user);
                            addSubscriber(email);
                        }
                    }
                });

    }


    public void getSubscriberWishList(final String email){

        db.collection("users").document(email).get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if(task.getResult().exists()) {
                            User user = task.getResult().toObject(User.class);
                            getWishes(user);
                            othersUsers.add(user);
                        }
                    }
                });

    }

    public void getSubscribers(final User currentUser){
        String myEmail = currentUser.getEmail();
        db.collection("users").document(myEmail).collection("subscribers").get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        ArrayList<String> subscriberList = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {

                            String subscriber = document.getString("email");
                            subscriberList.add(subscriber);
                        }
                        currentUser.setSubscriberList(subscriberList);
                        sendBroadcast("newdata");
                    }
                });
    }

    public void  addSubscriber(String email){
        String myEmail= mAuth.getCurrentUser().getEmail();
        HashMap<String, String> emailHash = new HashMap<>();
        emailHash.put("email",email);
        db.collection("users").document(myEmail).collection("subscribers").document(email).set(emailHash)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void avoid) {
                        Log.d(TAG,"Subscriber added");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error adding document", e);
                    }
                });

    }


    public void deleteSubscriber(final String email) {
        String myEmail= mAuth.getCurrentUser().getEmail();
        db.collection("users").document(myEmail).collection("subscribers").document(email)
                .delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        for(User u: othersUsers){
                            if(email.equals(u.getEmail())){
                                othersUsers.remove(u);

                            }
                        }
                        sendBroadcast("subscriberdata");
                        Log.d(TAG, "DocumentSnapshot successfully deleted!");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error deleting document", e);
                    }
                });
    }

    public void addWish(Wish wish){
        String email=mAuth.getCurrentUser().getEmail();
        db.collection("users").document(email).collection("wishes").document(wish.getWishName()).set(wish)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void avoid) {
                        Log.d(TAG,"Wish added");
                        //Her skal der opdateres i expandable list
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error adding document", e);
                    }
                });

    }

    public void deleteWish(Wish wish){

        String myEmail= mAuth.getCurrentUser().getEmail();
        db.collection("users").document(myEmail).collection("wishes").document(wish.getWishName())
                .delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "DocumentSnapshot successfully deleted!");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error deleting document", e);
                    }
                });
    }

    public ArrayList<User> getUserList(){
        return othersUsers;
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
