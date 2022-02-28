package com.github.zoommaxdecentralnetwork.decentralmessenger;

import android.app.AlertDialog;
import android.app.Dialog;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import java.io.File;

public class DialogFragmentKey extends DialogFragment {
    String title;
    String message;
    public DialogFragmentKey(String title, String message){
        this.title = title;
        this.message = message;
    }

    @NonNull
    public Dialog onCreateDialog(Bundle savedInstanceState){
        TextView textTitle = new TextView(getActivity());
            textTitle.setText(title);
            textTitle.setTextSize(18.0F);
            textTitle.setTypeface(null, Typeface.BOLD);
            textTitle.setGravity(Gravity.CENTER);

        TextView textMsg = new TextView(getActivity());
        textMsg.setText(message);
        textMsg.setTextSize(14.0F);
        textMsg.setTypeface(null, Typeface.NORMAL);
        textMsg.setGravity(Gravity.CENTER);
        textMsg.setTextIsSelectable(true);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        return builder
                .setCustomTitle(textTitle)
                .setView(textMsg)
                .setPositiveButton("OK", null)
                .create();
    }
}
