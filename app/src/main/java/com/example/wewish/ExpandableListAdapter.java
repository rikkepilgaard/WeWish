package com.example.wewish;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class ExpandableListAdapter extends BaseExpandableListAdapter {

    private Context context;
    private ArrayList<User> userNameList;
    OverviewFragment fragment;
    public ExpandableListAdapter(Context context, ArrayList<User> userNameList, OverviewFragment fragment) {
        this.context = context;
        this.userNameList = userNameList;
        this.fragment=fragment;
    }
    void updateList(ArrayList<User> users)
    {
        this.userNameList = users;

        notifyDataSetChanged();
    }
    @Override
    public int getGroupCount() {
        return userNameList.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        if(userNameList.get(groupPosition).getWishList()==null){
            return 0;
        } else return userNameList.get(groupPosition).getWishList().size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return userNameList.get(groupPosition);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {

        User user = userNameList.get(groupPosition);
        Wish wish = user.getWishList().get(childPosition);

        return wish;

    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public View getGroupView(final int groupPosition, boolean isExpanded, View convertView, final ViewGroup parent) {

        final User user = userNameList.get(groupPosition);

        if(convertView == null) {
            LayoutInflater inflater = (LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.group_item, null);
        }

        convertView.setBackgroundColor(context.getResources().getColor(R.color.dark));


        TextView txtUser = convertView.findViewById(R.id.txtUserName);
        if(groupPosition==0){txtUser.setText(R.string.mywishes);}
        else{
        String text = user.getUserName() + "'s wishes";
        txtUser.setText(text);}

        TextView txtNumberWishes = convertView.findViewById(R.id.txtWishNumber);
        int number = 0;
        if(user.getWishList()!=null){
            number = user.getWishList().size();
        }
        txtNumberWishes.setText(String.valueOf(number));


        ImageButton btnDeleteList = convertView.findViewById(R.id.btnDeleteList);

        if(groupPosition==0){btnDeleteList.setVisibility(View.INVISIBLE);}
        else {
            btnDeleteList.setFocusable(false);
            btnDeleteList.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    fragment.deleteSubscriber(user.getEmail());

                }
            });
        }


        return convertView;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {

        Wish wish = (Wish)getChild(groupPosition,childPosition);

        if(convertView == null) {
            LayoutInflater inflater = (LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.child_item, null);
        }

        TextView txtWishName = convertView.findViewById(R.id.txtWishName);
        txtWishName.setText(wish.getWishName());
        TextView txtPriority = convertView.findViewById(R.id.txtWishPriority);
        txtPriority.setText(wish.getPriority());
        TextView txtPrice = convertView.findViewById(R.id.txtWishPrice);
        txtPrice.setText(wish.getPrice());

        return convertView;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }
}
