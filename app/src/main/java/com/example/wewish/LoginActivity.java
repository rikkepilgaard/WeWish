package com.example.wewish;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class LoginActivity extends AppCompatActivity {
    private String TAG = "LoginActivity";
    EditText userName;
    Button loginButton,createButton;
    private boolean dataServiceBound = false;
    private DataService dataService;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        userName = findViewById(R.id.username);
        loginButton=findViewById(R.id.login);
        createButton=findViewById(R.id.create);
    }
    public void loginToAccount(View v){
        dataService.login(userName.getText().toString());
    }

    public void createAccount(View v){
        dataService.saveNewUser(userName.getText().toString());
    }

    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter dataFilter = new IntentFilter();
        dataFilter.addAction("startIntent");
        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver,dataFilter);
    }

    @Override
    protected void onStart() {
        super.onStart();
        startServices();
        bindToServices();
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
                case "startIntent": startWishActivity();
            }
        }
    };
    public void startWishActivity(){
        Intent intent= new Intent(LoginActivity.this,WishActivity.class);
        startActivity(intent);
    }

    private void startServices(){
        Intent dataIntent = new Intent(LoginActivity.this,DataService.class);
        startService(dataIntent);

    }

    private void bindToServices(){
        Intent dataIntent = new Intent(LoginActivity.this,DataService.class);
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
}
