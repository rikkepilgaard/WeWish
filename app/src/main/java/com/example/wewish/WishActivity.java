package com.example.wewish;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
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



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wish);

        fragmentManager = getSupportFragmentManager();
        container = findViewById(R.id.container);
        signoutButton=findViewById(R.id.signout);

        if(container!=null){
            if(savedInstanceState!=null){
                return;
            }
            OverviewFragment fragment = new OverviewFragment();
            fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.container,fragment,"replace");
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

    private ServiceConnection dataConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            DataService.DataServiceBinder dataBinder = (DataService.DataServiceBinder) service;
            dataService = dataBinder.getService();
            dataServiceBound = true;
            Log.d(TAG,"Connected to DataService");
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            dataServiceBound = false;
        }
    };
    public void getWishListFromFirebase(String email){
       dataService.getWishListFromFirebase(email);
    }

    public void addWishToWishList(Wish wish){
        dataService.addWishToWishList(wish);

    }



}
