package com.example.wewish;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.IBinder;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ProgressBar;

import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.List;

import static com.example.wewish.ConstantValues.MODE_SMARTPHONE;
import static com.example.wewish.ConstantValues.MODE_TABLET;

public class WishActivity extends AppCompatActivity implements
        OverviewFragment.OnOverviewFragmentInteractionListener,
        DetailsFragment.OnDetailsFragmentInteractionListener{

    private String TAG ="WishActivity";
    private static FragmentManager fragmentManager;
    private FrameLayout container;
    private DataService dataService;
    private boolean dataServiceBound;
    private ProgressBar progressBar;
    private OverviewFragment overviewFragment;
    private DetailsFragment detailsFragment;

    private ArrayList<User> users;
    private int mode;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wish);

        fragmentManager = getSupportFragmentManager();
        container = findViewById(R.id.container);
        progressBar = findViewById(R.id.progressbar);


        users = new ArrayList<>();

        //OverviewFragment created and put on top on back stack.
        //On orientation change fragments are reloaded from preexisting versions saved in onSaveInstanceState().
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

        //inspiration from https://forum.unity.com/threads/reliable-way-to-detect-tablet-on-android.127184/

        //Calculating screen size in inches.
        DisplayMetrics dm = getResources().getDisplayMetrics();
        float screenWidth  = dm.widthPixels / dm.xdpi;
        float screenHeight = dm.heightPixels / dm.ydpi;
        double inches = Math.sqrt(Math.pow(screenWidth, 2) +
                Math.pow(screenHeight, 2));

        //Set mode to tablet or smartphone depending on screensize.
        if (inches > 6){
            mode = MODE_TABLET;
        }else{
            mode = MODE_SMARTPHONE;
        }

        //In tablet mode DetailsFragment is created and shown next to OverviewFragment
        if(mode == MODE_TABLET){
            detailsFragment = DetailsFragment.newInstance(null, 0);
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.container2,detailsFragment,"detail");
            fragmentTransaction.addToBackStack(null);
            fragmentTransaction.commit();
        }

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

        //Saves current states of fragments
        fragmentManager.putFragment(outState, "overviewfragment", overviewFragment);
        if(detailsFragment!=null) {
            if(detailsFragment.isAdded())
                fragmentManager.putFragment(outState, "detailsfragment", detailsFragment);
        }
    }

    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG,"Broadcast received: " + intent.getAction());
            switch (intent.getAction()){
                case "newdata":
                    //Data received from firebase first time.
                    updateUserList();
                    break;

                case "subscriberdata":
                    //Data updated (wish list or wish added or deleted by current user)
                    overviewFragment.updateList(dataService.getUserList());
                    break;
            }
        }
    };

    @Override
    public void onOverviewFragmentInteraction(Wish wish,int groupPosition) {
        detailsFragment = DetailsFragment.newInstance(wish, groupPosition);

        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        if(mode == MODE_TABLET) {
            fragmentTransaction.replace(R.id.container2, detailsFragment, "details");
        }else fragmentTransaction.replace(R.id.container, detailsFragment, "details");
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();

    }

    @Override

    //Method called from DetailsFragment through interface.
    public void previousFragment() {
        fragmentManager.popBackStack();
    }


    //Closes app if back is pressed from OverviewFragment instead of just removing fragment.
    @Override
    public void onBackPressed() {
        if(fragmentManager.getBackStackEntryCount()>1) {
            fragmentManager.popBackStack();
        }else {
            finishAffinity();
        }
    }

    public void signout(View v){
        dataService.signOut();
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

        //Method collects wishes from all current user's subscribers through methods
        // in dataService.

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

            Log.d(TAG,"Connected to DataService");
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            dataServiceBound = false;
        }
    };


    //FOLLOWING METHODS ARE CALLED FROM FRAGMENTS THROUGH INTERFACES.

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

}
