package com.example.wewish;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;


public class DetailsFragment extends Fragment {


    private OnDetailsFragmentInteractionListener mListener;

    TextView txtWishName;
    TextView txtPrice;
    TextView txtPriority;
    TextView txtComments;
    TextView txtUrl;

    Button btnBack;
    Button btnDelete;
    ImageButton btnOpenUrl;

    public DetailsFragment() {
        // Required empty public constructor
    }


    public static DetailsFragment newInstance(Wish wish) {
        Bundle bundle = new Bundle();
        bundle.putSerializable("wish",wish);
        DetailsFragment fragment = new DetailsFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_details, container, false);

        assert getArguments() != null;
        final Wish wish = (Wish)getArguments().getSerializable("wish");

        txtWishName = view.findViewById(R.id.txtWishNameChange);
        assert wish != null;
        txtWishName.setText(wish.getWishName());
        txtPrice = view.findViewById(R.id.txtPriceChange);
        txtPrice.setText(wish.getPrice());
        txtPriority = view.findViewById(R.id.txtPriorityChange);
        txtPriority.setText(wish.getPriority());
        txtComments = view.findViewById(R.id.txtCommentsChange);
        txtComments.setText(wish.getComments());
        txtUrl = view.findViewById(R.id.txtUrlChange);
        txtUrl.setText(wish.getUrlName());

        btnBack = view.findViewById(R.id.btnBack);
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.previousFragment();
            }
        });

        btnDelete = view.findViewById(R.id.btnDelete);
        btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.deleteWish(wish);
            }
        });

        btnOpenUrl = view.findViewById(R.id.btnOpenUrl);
        btnOpenUrl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String url = wish.getUrlName();

                if (!url.startsWith("http://") && !url.startsWith("https://"))
                    url = "http://" + url;

                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                startActivity(browserIntent);
            }
        });

        return view;
    }



    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnDetailsFragmentInteractionListener) {
            mListener = (OnDetailsFragmentInteractionListener) context;
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


    public interface OnDetailsFragmentInteractionListener {

        void previousFragment();
        void deleteWish(Wish wish);

    }
}
