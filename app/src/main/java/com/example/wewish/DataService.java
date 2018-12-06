package com.example.wewish;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
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
    private AsyncTaskStart birthdayTask;
    private Boolean running;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    private ArrayList<User> othersUsers;
    private String currentUserEmail;

    public DataService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "onBind");
        return binder;
    }

    public void signOut() {
        mAuth.getInstance().signOut();
    }


    public class DataServiceBinder extends Binder {
        DataService getService() {
            return DataService.this;
        }
    }


    private IBinder binder = new DataServiceBinder();


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        db= FirebaseFirestore.getInstance();
        mAuth=FirebaseAuth.getInstance();
        running=true;
        birthdayTask = new AsyncTaskStart();
        birthdayTask.execute();

        othersUsers = new ArrayList<>();

        //Login directly if user is logged in in Authentication.
        if(mAuth.getCurrentUser()!=null){
            login(mAuth.getCurrentUser().getEmail());
            currentUserEmail=mAuth.getCurrentUser().getEmail();
            sendBroadcast("wishActivity","");
        }
        else{

        }

        return START_STICKY;

    }

    //Async task class calls method checkForBirthday() every day
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

    //Create new user in Authentication.
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

    //Create new user in firebase firestore.
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

    //Sign in through firebase authentication
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


    //Gives a toast if entered email does not exist in database. If email exists, method
    //getCurrentUserFromFirebase() is called.
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

    //Collects details about user from firebase (username,email,birthdate) and calls method
    //getWishes() with current user as input parameter.
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

    //Collects each wish from given user in firebase firestore. Calls method getSubscriber() if
    //user is currentUser. Otherwise subscriberlist is redundant.
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

    //Method is called, when current user wants to add new wishlist to overview. Email is added
    //to current user's subscriberlist and wishes for new user is collected through getWishes().
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
                        else{
                            Toast.makeText(DataService.this, R.string.wrongUsername, Toast.LENGTH_SHORT).show();}
                    }
                });

    }


    //Gets wishlist from subscriber
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

    //Gets current user's subsriberlist. Method is called when information about current user is collected.
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

    //Adds a user to current user's subscriber list.
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


    //Removes a subscriber from current user's subscriberlist.
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


    //Adds new wish to current user's wish list.
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

    //deletes a wish from current user's wish list.
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


    //Calls method checkSubscriberBirthdate when information about current user.
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

    //Methods collects current user's list of subscribers and calls method birthdayInTwoWeeks()
    //for each subscriber.
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

    //Sends a notification if user has birthday in two weeks.
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
            showNotification(name);
        }

    }

    public void showNotification(String name){
        //https://developer.android.com/training/notify-user/build-notification

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            NotificationChannel channel = new NotificationChannel("101",
                    "notiname",NotificationManager.IMPORTANCE_DEFAULT);

            channel.canShowBadge();
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this,"101")
                .setSmallIcon(R.drawable.ic_noti_icon)
                .setContentTitle(getString(R.string.alert_time_title))
                .setContentText(name+" "+getString(R.string.birthdatetext));


        if (notificationManager != null) {
            notificationManager.notify(1,notificationBuilder.build());
        }

    }


}
