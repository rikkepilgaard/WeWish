package com.example.wewish;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;

import com.google.firebase.auth.FirebaseAuth;

public class WishActivity extends AppCompatActivity implements
        OverviewFragment.OnOverviewFragmentInteractionListener,
        DetailsFragment.OnDetailsFragmentInteractionListener{

    public static FragmentManager fragmentManager;
    private FragmentTransaction fragmentTransaction;
    private FrameLayout container;
    private Button signoutButton;



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
}
