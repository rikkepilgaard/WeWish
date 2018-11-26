package com.example.wewish;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class WishActivity extends AppCompatActivity implements
        OverviewFragment.OnOverviewFragmentInteractionListener,
        DetailsFragment.OnDetailsFragmentInteractionListener{


    private String TAG ="WishActivity";
    public static FragmentManager fragmentManager;
    private FragmentTransaction fragmentTransaction;
    private FrameLayout container;
    private Button signoutButton;
    private DataService dataService;
    private boolean dataServiceBound;

    private OverviewFragment overviewFragment;

    private ArrayList<User> users;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wish);

        fragmentManager = getSupportFragmentManager();
        container = findViewById(R.id.container);
        signoutButton=findViewById(R.id.signout);

        users = new ArrayList<>();

        if(container!=null){
            if(savedInstanceState!=null){
                return;
            }
            overviewFragment = new OverviewFragment();
            fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.container,overviewFragment,"replace");
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
        dataFilter.addAction("wishActivity");
        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver,dataFilter);
    }

    @Override
    protected void onStop() {
        super.onStop();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiver);
        unbindServices();
    }

    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG,"Broadcast received: " + intent.getAction());
            switch (intent.getAction()){
                case "newdata":
                    updateUserList();
            }
        }
    };

    @Override
    public void onOverviewFragmentInteraction(Wish wish) {
        Fragment fragment = fragmentManager.findFragmentById(R.id.container);
        if(fragment instanceof OverviewFragment){
            fragment = DetailsFragment.newInstance(wish);
        }
        else {
            fragment = new OverviewFragment();
        }
        fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.container,fragment,"replace");
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
                dataService.addNewWishList(subscriber);

            }
        }
        overviewFragment.initData(dataService.getUserList());
    }


    private ServiceConnection dataConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            DataService.DataServiceBinder dataBinder = (DataService.DataServiceBinder) service;
            dataService = dataBinder.getService();
            dataServiceBound = true;

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

}
