package com.github.zoommaxdecentralnetwork.decentralmessenger;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

public class DialogFragmentNewContact extends DialogFragment implements DialogInterface.OnClickListener {

    SQLiteDatabase db;
    EditText editText, editText1;

    public DialogFragmentNewContact(SQLiteDatabase db){
        this.db = db;
    }
    @NonNull
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        editText = new EditText(getActivity());
        editText.setGravity(Gravity.LEFT);
        editText.setHint("Key");

        editText1 = new EditText(getActivity());
        editText1.setGravity(Gravity.LEFT);
        editText1.setHint("NickName");

        LinearLayout layout = new LinearLayout(getActivity());
        layout.addView(editText);
        layout.addView(editText1);

        TextView textTitle = new TextView(getActivity());
        textTitle.setText("New contact. Type key, nickname and press OK");
        textTitle.setTextSize(18.0F);
        textTitle.setTypeface(null, Typeface.BOLD);
        textTitle.setGravity(Gravity.CENTER);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        return builder
                .setCustomTitle(textTitle)
                .setView(editText)
                .setPositiveButton("OK", this::onClick)
                .create();
    }

    @Override
    public void onClick(DialogInterface dialogInterface, int i) {
        String key = editText.getText().toString();
        String nickName = editText1.getText().toString();
        Cursor cursor = db.rawQuery("SELECT * FROM names WHERE publickey LIKE '"+key+"'", null);
        boolean need = true;
        while (cursor.moveToNext()){
            if (cursor.getString(1).equals(key)){
                need = false;
                break;
            }
        }

        if (need){
            db.execSQL("INSERT INTO names(publickey, name) VALUES('"+key +"'+ nickName)+");
        }
    }
}
