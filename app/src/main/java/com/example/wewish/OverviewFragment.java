package com.example.wewish;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.Toast;

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
    private static final String TAG ="OverviewFragment";
    private ArrayList<User> userList;
    private OnOverviewFragmentInteractionListener mListener;

    private ExpandableListView listView;
    private ExpandableListAdapter adapter;
    private Button btnAddWish;
    private Button btnAddWishList;

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

        View view = inflater.inflate(R.layout.fragment_overview, container, false);
        listView = view.findViewById(R.id.listViewExpandable);
        //initData();

        listView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {

                if (mListener != null) {
                    Wish wish = (Wish)adapter.getChild(groupPosition,childPosition);
                    mListener.onOverviewFragmentInteraction(wish,groupPosition);
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

        userList = mListener.getUserList();
        if(userList!=null){
            initData(userList);
        }

        return view;
    }

    public void updateList(ArrayList<User> users){
        userList=users;
        adapter.updateList(users);
    }


    //Creates an alert with custom layout.
    private void openNewWishListAlert() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                getActivity());

        alertDialogBuilder.setTitle(getString(R.string.add_wish_list_title));
        LayoutInflater inflater = getLayoutInflater();
        final View v = inflater.inflate(R.layout.alert_new_wishlist, null);
        alertDialogBuilder.setView(v);

        alertDialogBuilder
                .setMessage(getString(R.string.add_wish_list_description))
                .setCancelable(false)
                .setPositiveButton(getString(R.string.add),new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,int id) {

                        EditText edtEmail = v.findViewById(R.id.edtEmail);
                        String email = edtEmail.getText().toString();
                        if(email.equals("")){
                            Toast.makeText(getContext(),R.string.blankwishlist, Toast.LENGTH_LONG).show();}
                            else{
                        mListener.getWishListFromFirebase(email);}
                    }
                })
                .setNegativeButton(getString(R.string.cancel),new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,int id) {
                        dialog.cancel();
                    }
                });

        AlertDialog alertDialog = alertDialogBuilder.create();

        alertDialog.show();
    }

    //Creates an alert with custom layout.
    private void openNewWishAlert() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                getActivity());

        alertDialogBuilder.setTitle(getString(R.string.add_wish_title));
        LayoutInflater inflater = getLayoutInflater();
        final View v = inflater.inflate(R.layout.alert_new_wish, null);
        alertDialogBuilder.setView(v);

        alertDialogBuilder
                .setMessage(null)
                .setCancelable(false)
                .setPositiveButton(getString(R.string.add),new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,int id) {
                        EditText comments=v.findViewById(R.id.editCommentsAlert);
                        EditText wishName=v.findViewById(R.id.editWishNameAlert);
                        EditText price = v.findViewById(R.id.editPriceAlert);
                        EditText url=v.findViewById(R.id.editUrlAlert);
                        EditText priority =v.findViewById(R.id.editPriorityAlert);
                        if(wishName.getText().toString().equals("")){
                            Toast.makeText(getContext(), R.string.blankwishname, Toast.LENGTH_SHORT).show(); }
                        else{
                        Wish wish = new Wish(wishName.getText().toString(),
                                priority.getText().toString(),
                                comments.getText().toString(),
                                url.getText().toString(),
                                price.getText().toString());

                        mListener.addWishToWishList(wish);}
                    }
                })
                .setNegativeButton(getString(R.string.cancel),new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,int id) {
                        dialog.cancel();
                    }
                });

        AlertDialog alertDialog = alertDialogBuilder.create();

        alertDialog.show();

    }

    public void initData(ArrayList<User> users) {
        adapter = new ExpandableListAdapter(getContext(),users,this);
        listView.setAdapter(adapter);
    }

    //Make sure activity implements interface. Creates instance of interface (mListener)
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

    public void deleteSubscriber(String email){
        mListener.deleteWishList(email);
    }



    public interface OnOverviewFragmentInteractionListener {
        void onOverviewFragmentInteraction(Wish wish, int groupPosition);
        void getWishListFromFirebase(String email);
        void addWishToWishList(Wish wish);
        void deleteWishList(String email);
        ArrayList<User> getUserList();
    }



}
