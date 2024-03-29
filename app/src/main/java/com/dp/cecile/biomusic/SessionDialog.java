package com.dp.cecile.biomusic;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

/**
 * Created by cecilerobertm on 2017-03-01.
 */

public class SessionDialog extends DialogFragment {

    CharSequence[] emotions = {"Anger", "Fear", "Disgust", "Enjoyment", "Sadness", "Other"};
    String selected = "Other";
    MainActivity mActivity;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        mActivity = (MainActivity) getActivity();
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Get the layout inflater
        final LayoutInflater inflater = getActivity().getLayoutInflater();
        final View view = inflater.inflate(R.layout.save_session_dialog, null);

        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        builder.setView(view)
                .setTitle("What emotion best describes how you felt during the recording?")
                .setSingleChoiceItems(emotions, 5, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int item) {
                        selected = emotions[item].toString();
                    }
                })
                // Add action buttons
                .setPositiveButton(R.string.save, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        // save to history
                        String comment = ((EditText)view.findViewById(R.id.notes)).getText().toString();
                        SessionModel session = new SessionModel();
                        session.setEmotion(selected);
                        session.setComment(comment);
                        mActivity.sqLiteHelper.insertRecord(session);

                    }
                })
                .setNegativeButton(R.string.discard, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        SessionDialog.this.getDialog().cancel();
                    }
                });
        return builder.create();
    }
}