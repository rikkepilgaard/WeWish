package com.example.wewish;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ExpandableListView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class OverviewFragment extends Fragment {

    private OnOverviewFragmentInteractionListener mListener;
    private String TAG ="OverviewFragment";
    private ExpandableListView listView;
    private ExpandableListAdapter adapter;
    private Button btnAddWish;
    private Button btnAddWishList;
    FirebaseFirestore db;
    private FirebaseAuth mAuth;

    public OverviewFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        db=FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        View view = inflater.inflate(R.layout.fragment_overview, container, false);
        listView = view.findViewById(R.id.listViewExpandable);
        initData();

        listView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {

                if (mListener != null) {
                    Wish wish = (Wish)adapter.getChild(groupPosition,childPosition);
                    mListener.onOverviewFragmentInteraction(wish);
                }

                return true;
            }
        });

        btnAddWish = view.findViewById(R.id.btnAddWish);
        btnAddWish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openNewWishAlert();
            }
        });

        btnAddWishList = view.findViewById(R.id.btnAddWishList);
        btnAddWishList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openNewWishListAlert();
            }
        });

        return view;
    }

    private void openNewWishListAlert() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                getActivity());

        // set title
        alertDialogBuilder.setTitle(getString(R.string.add_wish_list_title));
        LayoutInflater inflater = getLayoutInflater();
        final View v = inflater.inflate(R.layout.alert_new_wishlist, null);
        alertDialogBuilder.setView(v);

        // set dialog message
        alertDialogBuilder
                .setMessage(getString(R.string.add_wish_list_description))
                .setCancelable(false)
                .setPositiveButton(getString(R.string.add),new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,int id) {
                        EditText edtEmail = v.findViewById(R.id.edtEmail);
                        String email = edtEmail.getText().toString();
                        getWishListFromFirebase(email);
                    }
                })
                .setNegativeButton(getString(R.string.cancel),new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,int id) {
                        dialog.cancel();
                    }
                });

        // create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();

        // show it
        alertDialog.show();
    }

    private void openNewWishAlert() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                getActivity());

        alertDialogBuilder.setTitle(getString(R.string.add_wish_title));
        LayoutInflater inflater = getLayoutInflater();
        final View v = inflater.inflate(R.layout.alert_new_wish, null);
        alertDialogBuilder.setView(v);

        // set dialog message
        alertDialogBuilder
                .setMessage(getString(R.string.add_wish_list_description))
                .setCancelable(false)
                .setPositiveButton(getString(R.string.add),new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,int id) {
                        EditText comments=v.findViewById(R.id.editCommentsAlert);
                        EditText wishName=v.findViewById(R.id.editWishNameAlert);
                        EditText price = v.findViewById(R.id.editPriceAlert);
                        EditText url=v.findViewById(R.id.editUrlAlert);
                        EditText priority =v.findViewById(R.id.editPriorityAlert);

                        Wish wish = new Wish(wishName.getText().toString(),
                                priority.getText().toString(),
                                comments.getText().toString(),
                                url.getText().toString(),
                                price.getText().toString());

                        addWishToWishlist(wish);
                    }
                })
                .setNegativeButton(getString(R.string.cancel),new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,int id) {
                        dialog.cancel();
                    }
                });

        // create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();

        // show it
        alertDialog.show();

    }

    private void initData() {

        Wish wish = new Wish("Bil1","Høj","Den vil jeg gerne ha","Her.dk","300 kr");
        Wish wish1 = new Wish("Bil2","Høj","Den vil rigtig jeg gerne ha","Her.dk","300 kr");
        Wish wish2 = new Wish("Bil3","Høj","Den vil jeg utrolig gerne ha","Her.dk","300 kr");
        Wish wish3 = new Wish("Bil4","Høj","Den vil jeg særligt gerne ha","Her.dk","300 kr");
        Wish wish4 = new Wish("Bil5","Høj","Den vil jeg vildt gerne ha","Her.dk","300 kr");

        ArrayList<Wish> wishes1 = new ArrayList<>();
        ArrayList<Wish> wishes2 = new ArrayList<>();

        wishes1.add(wish);
        wishes1.add(wish1);
        wishes1.add(wish2);
        wishes2.add(wish3);
        wishes2.add(wish4);

        User user1 = new User("katten",wishes1);
        User user2 = new User("hello",wishes2);
        User user3 = new User("rikkehdaahudhua",wishes1);

        ArrayList users = new ArrayList();
        users.add(user1);
        users.add(user2);
        users.add(user3);

        adapter = new ExpandableListAdapter(getContext(),users);
        listView.setAdapter(adapter);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnOverviewFragmentInteractionListener) {
            mListener = (OnOverviewFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }


    public interface OnOverviewFragmentInteractionListener {

        void onOverviewFragmentInteraction(Wish wish);
    }

    public void getWishListFromFirebase(String email){
        db.collection("users").document(email).collection("wishes").get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        Log.d(TAG, document.getId() + " => " + document.getData());
                        //Her skal der laves en liste af ønsker til den pågælende person
                    }
                } else {
                    Log.d(TAG, "Error getting documents: ", task.getException());
                }

            }

        });

    }

    public void addWishToWishlist(Wish wish){
        String email=mAuth.getCurrentUser().getEmail();
        db.collection("users").document(email).collection("wishes").document().set(wish)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void avoid) {
                        Log.d(TAG,"Wish added");
                        //Her skal der opdateres i expandable list
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error adding document", e);
                    }
                });

    }



}
