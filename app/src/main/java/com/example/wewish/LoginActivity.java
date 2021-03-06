package com.example.wewish;

import android.app.DatePickerDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;


import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Calendar;



public class LoginActivity extends AppCompatActivity {



    private String TAG = "LoginActivity";
    private EditText Email, passWord,username;
    private TextView date,text;
    private Button loginButton,createButton,doneButton;
    private boolean dataServiceBound = false;
    private DataService dataService;
    private DatePickerDialog.OnDateSetListener dateSetListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        Email = findViewById(R.id.email);
        passWord = findViewById(R.id.password);
        username=findViewById(R.id.username);
        loginButton=findViewById(R.id.login);
        createButton=findViewById(R.id.create);
        doneButton=findViewById(R.id.done);
        date=findViewById(R.id.birthdate);
        text=findViewById(R.id.textview1);
        date.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Ref:https://www.youtube.com/watch?v=hwe1abDO2Ag
                Calendar calender = Calendar.getInstance();
                int year=calender.get(Calendar.YEAR);
                int month= calender.get(Calendar.MONTH);
                int day=calender.get(Calendar.DAY_OF_MONTH);
                DatePickerDialog dialog = new DatePickerDialog(LoginActivity.this,
                        R.style.DatePickerTheme,dateSetListener,
                        year,month,day);
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                dialog.show();
            }
        });
        dateSetListener=new DatePickerDialog.OnDateSetListener(){
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                date.setText(dayOfMonth+"-"+month+"-"+year);
            }
        };





        createButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                username.setVisibility(View.VISIBLE);
                date.setVisibility(View.VISIBLE);
                loginButton.setVisibility(View.INVISIBLE);
                doneButton.setVisibility(View.VISIBLE);
                createButton.setVisibility(View.INVISIBLE);
                text.setVisibility(View.VISIBLE);



            }
        });
        doneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = Email.getText().toString();
                String password = passWord.getText().toString().trim();

                if (TextUtils.isEmpty(email)){
                    Toast.makeText(getApplicationContext(), getString(R.string.errorEmail), Toast.LENGTH_SHORT);
                    return;
                }
                if (TextUtils.isEmpty(password)){
                    Toast.makeText(getApplicationContext(),getString(R.string.errorPassword),Toast.LENGTH_SHORT);
                    return;
                }

                dataService.createUser(email,password,username.getText().toString(),date.getText().toString());

            }
        });

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = Email.getText().toString();
                String password = passWord.getText().toString();

                if(TextUtils.isEmpty(email)) {
                    Toast.makeText(getApplicationContext(), getString(R.string.errorEmail), Toast.LENGTH_SHORT);
                    return;
                }
                if (TextUtils.isEmpty(password)){
                    Toast.makeText(getApplicationContext(),getString(R.string.errorPassword),Toast.LENGTH_SHORT);
                    return;
                }

                dataService.signIn(email,password);
            }
        });

    }


    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter dataFilter = new IntentFilter();
        dataFilter.addAction("wishActivity");
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
                case "wishActivity":
                    Intent intent1= new Intent(LoginActivity.this,WishActivity.class);
                    startActivity(intent1);
            }
        }
    };


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
            //dataService.checkIfUserisLoggedIn();
            dataServiceBound = true;
            Log.d(TAG,"Connected to DataService");
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            dataServiceBound = false;
        }
    };


}

