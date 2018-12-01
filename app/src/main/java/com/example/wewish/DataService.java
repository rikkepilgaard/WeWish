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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class DataService extends Service {

    private String TAG = "Dataservice";
    AsyncTaskStart BirthdayTask;
    private Boolean running;
    private FirebaseAuth mAuth;

    private ArrayList<User> othersUsers;
    private String currentUserEmail;

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

        //birthdateInTwoWeeks("13-12-1995","sofie");

        if(mAuth.getCurrentUser()!=null){
            login(mAuth.getCurrentUser().getEmail());
            currentUserEmail=mAuth.getCurrentUser().getEmail();
            sendBroadcast("wishActivity","");
        }
        else{

        }

        return START_STICKY;

    }

    public class AsyncTaskStart extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... voids) {
            while (running) {
                try {
                    Log.d(TAG, "BirthdayTask started");
                    Thread.sleep(10000);
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

    public void sendBroadcast(String action, String extra){
        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction(action);
        broadcastIntent.putExtra("name",extra);
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
                            sendBroadcast("wishActivity",null);
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
                        currentUserEmail=email;
                        getCurrentUserFromFirebase();
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
                            sendBroadcast("wishActivity",null);
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


    public void login(final String email){
        db.collection("users").document(email).get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (!documentSnapshot.exists()) {
                            Toast.makeText(DataService.this, "Email does not exist", Toast.LENGTH_LONG).show();
                        }
                        else{getCurrentUserFromFirebase();}
                    }
                });
    }

    public void getCurrentUserFromFirebase(){
        currentUserEmail= mAuth.getCurrentUser().getEmail();

            db.collection("users").document(currentUserEmail).get()
                    .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                        @Override
                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                            if (documentSnapshot.exists()) {
                                User user = documentSnapshot.toObject(User.class);
                                getWishes(user);
                                //getSubscribers(user);
                                if (othersUsers.size() == 0) {
                                    othersUsers.add(user);
                                }
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
                            else{sendBroadcast("subscriberdata",null);}
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
                        sendBroadcast("newdata",null);
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
        //String myEmail= mAuth.getCurrentUser().getEmail();
        db.collection("users").document(currentUserEmail).collection("subscribers").document(email)
                .delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        for(User u: othersUsers){
                            if(email.equals(u.getEmail())){
                                othersUsers.remove(u);

                            }
                        }
                        sendBroadcast("subscriberdata",null);
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

    public void addWish(final Wish wish){
        //String email=mAuth.getCurrentUser().getEmail();
        db.collection("users").document(currentUserEmail).collection("wishes").document(wish.getWishName()).set(wish)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void avoid) {
                        Log.d(TAG,"Wish added");
                        List<Wish> wishlist = othersUsers.get(0).getWishList();
                        wishlist.add(wish);
                        othersUsers.get(0).setWishList(wishlist);
                        sendBroadcast("subscriberdata",null);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error adding document", e);
                    }
                });

    }

    public void deleteWish(final Wish wish){

        //String myEmail= mAuth.getCurrentUser().getEmail();
        db.collection("users").document(currentUserEmail).collection("wishes").document(wish.getWishName())
                .delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        List<Wish> mywishes= othersUsers.get(0).getWishList();
                        for(Wish w: mywishes){
                            if(w.getWishName().equals(wish.getWishName())){
                                mywishes.remove(w);
                            }
                        }
                        othersUsers.get(0).setWishList(mywishes);
                        sendBroadcast("subscriberdata",null);
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
        if(mAuth.getCurrentUser()!=null){
        String user = mAuth.getCurrentUser().getEmail();
        db.collection("users").document(user).get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.exists()) {
                            User user = documentSnapshot.toObject(User.class);
                            if (othersUsers.size() == 0) {
                                othersUsers.add(user);
                            }
                            checkSubscriberBirthdate(user);
                        }
                    }
                });

    }
    }
    public void checkSubscriberBirthdate(final User currentUser){
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
                        for (String s : subscriberList) {
                            db.collection("users").document(s).get()
                                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                        @Override
                                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                            if (task.getResult().exists()) {
                                                User user = task.getResult().toObject(User.class);
                                                birthdateInTwoWeeks(user.getBirthDate(),user.getUserName());

                                            }
                                        }
                                    });
                        }
                    }
                });
    }

    private void birthdateInTwoWeeks(String birthdate, String name){
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR,2000);
        calendar.set(Calendar.MINUTE,0);
        calendar.set(Calendar.HOUR_OF_DAY,0);
        calendar.set(Calendar.SECOND,0);
        Date currentDate = calendar.getTime();


        SimpleDateFormat simpledateformat = new SimpleDateFormat("dd-MM-yyyy",Locale.getDefault());

        Date userdate= null;
        try {
            userdate = simpledateformat.parse(birthdate);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        calendar.setTime(userdate);
        calendar.set(Calendar.YEAR,2000);
        userdate = calendar.getTime();

        long diff = userdate.getTime()-currentDate.getTime();
        if(diff>1200960000 && diff<1209600100){
            sendBroadcast("notification",name);
        }

    }


}
