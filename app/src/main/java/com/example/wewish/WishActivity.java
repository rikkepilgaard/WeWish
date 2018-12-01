package com.example.wewish;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.res.Configuration;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ProgressBar;

import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.List;

public class WishActivity extends AppCompatActivity implements
        OverviewFragment.OnOverviewFragmentInteractionListener,
        DetailsFragment.OnDetailsFragmentInteractionListener{


    private String TAG ="WishActivity";
    public static FragmentManager fragmentManager;
   //private FragmentTransaction fragmentTransaction;
    private FrameLayout container;
    private Button signoutButton;
    private DataService dataService;
    private boolean dataServiceBound;
    ProgressBar progressBar;
    int orientation;

    private OverviewFragment overviewFragment;
    private DetailsFragment detailsFragment;

    private ArrayList<User> users;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wish);

        fragmentManager = getSupportFragmentManager();
        container = findViewById(R.id.container);
        signoutButton=findViewById(R.id.signout);
        progressBar = findViewById(R.id.progressbar);


        users = new ArrayList<>();

        if(container!=null){
            if(savedInstanceState!=null){
                overviewFragment = (OverviewFragment)fragmentManager.getFragment(savedInstanceState,"overviewfragment");
                detailsFragment = (DetailsFragment)fragmentManager.getFragment(savedInstanceState,"detailsfragment");
            } else {
                progressBar.setVisibility(View.VISIBLE);
                progressBar.bringToFront();
                overviewFragment = new OverviewFragment();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.replace(R.id.container, overviewFragment, "replace");
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.commit();
            }
        }

      /*  orientation = this.getResources().getConfiguration().orientation;

        if(orientation == Configuration.ORIENTATION_LANDSCAPE){
            detailsFragment = DetailsFragment.newInstance(null, 0);
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.container2,detailsFragment,"detail");
            fragmentTransaction.addToBackStack(null);
            fragmentTransaction.commit();
        }*/

    }


    @Override
    protected void onStart() {
        bindToService();
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter dataFilter = new IntentFilter();
        dataFilter.addAction("newdata");
        dataFilter.addAction("subscriberdata");
        dataFilter.addAction("wishActivity");
        dataFilter.addAction("notification");

        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver,dataFilter);
    }

    @Override
    protected void onStop() {
        super.onStop();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiver);
        unbindServices();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        fragmentManager.putFragment(outState, "overviewfragment", overviewFragment);
        if(detailsFragment.isAdded()) {
            fragmentManager.putFragment(outState, "detailsfragment", detailsFragment);
        }
    }

    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG,"Broadcast received: " + intent.getAction());
            switch (intent.getAction()){
                case "newdata":
                    updateUserList();
                    break;
                case "subscriberdata":
                    overviewFragment.updateList(dataService.getUserList());
                    break;
                case "notification":

                    showNotification(intent.getStringExtra("name"));
            }
        }
    };

    @Override
    public void onOverviewFragmentInteraction(Wish wish,int groupPosition) {
        detailsFragment = DetailsFragment.newInstance(wish, groupPosition);

        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.container, detailsFragment, "details");
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();

    }

    @Override
    public void previousFragment() {
        fragmentManager.popBackStack();
    }

    @Override
    public void onBackPressed() {
        if(fragmentManager.getBackStackEntryCount()>1) {
            fragmentManager.popBackStack();
        }else {
            finishAffinity();
        }
    }

    public void signout(View v){
        FirebaseAuth.getInstance().signOut();
        finish();
    }

    private void bindToService(){
        Intent dataIntent = new Intent(WishActivity.this,DataService.class);
        bindService(dataIntent,dataConnection,Context.BIND_AUTO_CREATE);

    }

    private void unbindServices(){
        if(dataServiceBound){
            unbindService(dataConnection);
        }
    }

    private void updateUserList(){
        users = dataService.getUserList();

        User currentUser = users.get(0);
        List<String> subscribers = currentUser.getSubscriberList();
        if(subscribers!=null) {
            for (String subscriber : subscribers) {
                dataService.getSubscriberWishList(subscriber);

            }

        }
        progressBar.setVisibility(View.INVISIBLE);
        overviewFragment.initData(dataService.getUserList());
    }


    private ServiceConnection dataConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            DataService.DataServiceBinder dataBinder = (DataService.DataServiceBinder) service;
            dataService = dataBinder.getService();
            dataServiceBound = true;

            if(dataService.getUserList()==null){
                dataService.getCurrentUserFromFirebase();
            }
            else {
                if(overviewFragment.isVisible())
                    overviewFragment.initData(dataService.getUserList());
            }

            //updateUserList();

            Log.d(TAG,"Connected to DataService");
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            dataServiceBound = false;
        }
    };
    public void getWishListFromFirebase(String email){
       dataService.addNewWishList(email);
    }

    public void addWishToWishList(Wish wish){
        dataService.addWish(wish);

    }

    public void deleteWish(Wish wish){
        dataService.deleteWish(wish);
    }

    public void deleteWishList(String email){
        dataService.deleteSubscriber(email);

    }

    @Override
    public ArrayList<User> getUserList() {
        if(dataServiceBound) {
            return dataService.getUserList();
        }else return null;
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
                .setSmallIcon(R.drawable.ic_launcher_background)
                .setContentTitle(getString(R.string.alert_time_title))
                .setContentText(name+" "+getString(R.string.birthdatetext));


        if (notificationManager != null) {
            notificationManager.notify(1,notificationBuilder.build());
        }

    }

}
