package com.example.wewish;

import android.support.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class CurrentUser {
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;

    public CurrentUser() {
        this.mAuth = FirebaseAuth.getInstance();
        this.currentUser = mAuth.getCurrentUser();
        mAuth.addAuthStateListener(new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                if(firebaseAuth.getCurrentUser()!=null){
                    currentUser = firebaseAuth.getCurrentUser();
                }else{
                    currentUser = null;
                }
            }
        });
    }

    public boolean isLoggedIn(){
        if(currentUser!=null){
            return true;
        }else {
            return false;
        }
    }

    public FirebaseUser getCurrentUser() {
        return currentUser;
    }
}
