package com.example.moengageapp.ui;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckedTextView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.moengageapp.R;

import java.util.ArrayList;
import java.util.List;

/*
dialog to filter from list of authors.
 */

public class AuthorFilterDialogFragment extends DialogFragment {
    public final static String TAG = "SwapTilesDialogFragment";
    private final static String ARG = "authors";

    private RecyclerView mRecyclerView;
    private ArrayList<String> authors;
    private List<String> checkedAuthors = new ArrayList<>();

    public interface MyListener {
        void doPositiveClick(List<String> checkedAuthors);
        void doNegativeClick();
    }

    private MyListener mListener;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        if (context instanceof MyListener) {
            mListener = (MyListener) context;
        } else {
            throw new ClassCastException(context.toString() +
                    " must implement " + TAG + ".MyListener");
        }
    }

    public static AuthorFilterDialogFragment newInstance(ArrayList<String> authors) {
        AuthorFilterDialogFragment dialogFragment = new AuthorFilterDialogFragment();
        Bundle args = new Bundle();
        args.putStringArrayList(ARG, authors);
        dialogFragment.setArguments(args);
        return dialogFragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        authors = getArguments().getStringArrayList(ARG);

        mRecyclerView = new RecyclerView(getContext());
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mRecyclerView.setAdapter(new RecyclerView.Adapter<MyViewHolder>() {

            @NonNull
            @Override
            public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                View v = LayoutInflater.from(parent.getContext()).inflate(
                        android.R.layout.simple_list_item_multiple_choice,
                        parent,
                        false);
                return new MyViewHolder(v);
            }

            @Override
            public void onBindViewHolder(@NonNull MyViewHolder vh, int position) {
                TextView tv = (TextView) vh.itemView;
                tv.setText(authors.get(position));
            }

            @Override
            public int getItemCount() {
                return authors.size();
            }
        });

        return new AlertDialog.Builder(getActivity())
                .setIcon(R.mipmap.ic_launcher)
                .setTitle(R.string.label_select_author)
                .setView(mRecyclerView)
                .setPositiveButton(R.string.label_apply,
                        (dialog, whichButton) -> mListener.doPositiveClick(checkedAuthors)
                )
                .setNegativeButton(R.string.label_cancel,
                        (dialog, whichButton) -> dismiss()
                )
                .create();
    }

    private class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private CheckedTextView checkedTextView;

        MyViewHolder(View v) {
            super(v);
            checkedTextView = (CheckedTextView) v;
            v.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if(checkedTextView.isChecked())
                checkedAuthors.remove(authors.get(getAdapterPosition()));
            else
                checkedAuthors.add(authors.get(getAdapterPosition()));

            checkedTextView.setChecked(!checkedTextView.isChecked());
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }
}
