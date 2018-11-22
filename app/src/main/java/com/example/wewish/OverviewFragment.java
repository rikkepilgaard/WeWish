package com.example.wewish;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;

import java.util.ArrayList;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link OverviewFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link OverviewFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class OverviewFragment extends Fragment {


    private ExpandableListView listView;
    private ExpandableListAdapter adapter;

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
        initData();

        listView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {



                return true;
            }
        });

        return view;
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
}
