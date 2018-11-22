package com.example.wewish;

import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.FrameLayout;

public class WishActivity extends AppCompatActivity {

    public static FragmentManager fragmentManager;
    private FrameLayout container;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wish);

        fragmentManager = getSupportFragmentManager();

        container = findViewById(R.id.container);

        if(container!=null){
            if(savedInstanceState!=null){
                return;
            }
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            OverviewFragment overviewFragment = new OverviewFragment();
            fragmentTransaction.add(R.id.container,overviewFragment,null);
            fragmentTransaction.commit();
        }
    }
}
