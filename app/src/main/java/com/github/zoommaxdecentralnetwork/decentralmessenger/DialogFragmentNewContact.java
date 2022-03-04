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
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

public class DialogFragmentNewContact extends DialogFragment implements DialogInterface.OnClickListener {

    SQLiteDatabase db;
    String pubKey;
    boolean update;
    EditText editText, editText1;

    public DialogFragmentNewContact(SQLiteDatabase db, boolean update){
        this.db = db;
        this.update = update;
    }

    public DialogFragmentNewContact(SQLiteDatabase db, boolean update, String pubKey){
        this.db = db;
        this.pubKey = pubKey;
        this.update = update;
    }
    @NonNull
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        editText = new EditText(getActivity());
        editText.setGravity(Gravity.LEFT);
        editText.setHint("Key");
        if (pubKey != null){
            editText.setText(pubKey);
        }

        editText1 = new EditText(getActivity());
        editText1.setGravity(Gravity.LEFT);
        editText1.setHint("NickName");

        LinearLayout layout = new LinearLayout(getActivity());
        layout.setOrientation(LinearLayout.VERTICAL);
        if (!update) {
            layout.addView(editText);
        }
        layout.addView(editText1);

        TextView textTitle = new TextView(getActivity());
        if (update){
            textTitle.setText("Update contact. Type nickname and press OK");
        }else {
            textTitle.setText("New contact. Type key, nickname and press OK");
        }
        textTitle.setTextSize(18.0F);
        textTitle.setTypeface(null, Typeface.BOLD);
        textTitle.setGravity(Gravity.CENTER);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        return builder
                .setCustomTitle(textTitle)
                .setView(layout)
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
            if (cursor.getString(0).equals(key)){
                need = false;
            }
            System.out.println(need);
        }

        if (need){
            db.execSQL("INSERT INTO names(publickey, name, newmsg) VALUES('"+key+"','"+nickName+"','0')");
        }
        if (!need){
            db.execSQL("Update names SET name = '"+nickName+"' WHERE publickey like '"+key+"'");
        }
    }
}
