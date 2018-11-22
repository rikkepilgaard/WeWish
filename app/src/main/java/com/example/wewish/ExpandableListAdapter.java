package com.example.wewish;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class ExpandableListAdapter extends BaseExpandableListAdapter {

    private Context context;
    private ArrayList<User> userNameList;
    //private HashMap<User, ArrayList<Wish>> listHashMap;

    public ExpandableListAdapter(Context context, ArrayList<User> userNameList) {
        this.context = context;
        this.userNameList = userNameList;
        //this.listHashMap = listHashMap;
    }

    @Override
    public int getGroupCount() {
        return userNameList.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return userNameList.get(groupPosition).getWishList().size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return userNameList.get(groupPosition);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        //return listHashMap.get(userNameList.get(groupPosition)).get(childPosition);

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
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {

        User user = userNameList.get(groupPosition);

        if(convertView == null) {
            LayoutInflater inflater = (LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.group_item, null);
        }

        convertView.setBackgroundColor(context.getResources().getColor(R.color.dark));

        TextView txtUser = convertView.findViewById(R.id.txtUserName);
        String text = user.getUserName() + "'s wishes";
        txtUser.setText(text);

        TextView txtNumberWishes = convertView.findViewById(R.id.txtWishNumber);
        int number = 0;
        if(user.getWishList()!=null){
            number = user.getWishList().size();
        }
        txtNumberWishes.setText(String.valueOf(number));

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
