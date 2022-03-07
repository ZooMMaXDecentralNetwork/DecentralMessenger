package com.github.zoommaxdecentralnetwork.decentralmessenger;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Typeface;
import android.location.GnssAntennaInfo;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class DialogFragmentStart extends DialogFragment implements DialogInterface.OnClickListener {
    protected EditText editText;
    public static final String APP_PREFERENCES_ip = "ip";
    SQLiteDatabase db;
    SharedPreferences mSettings;
    public DialogFragmentStart(SQLiteDatabase db, SharedPreferences mSttings){
        this.db = db;
        this.mSettings = mSttings;
    }
    @NonNull
    public Dialog onCreateDialog(Bundle savedInstanceState){

        TextView textTitle = new TextView(getActivity());
        textTitle.setText("Set start point. Type ip and press OK");
        textTitle.setTextSize(18.0F);
        textTitle.setTypeface(null, Typeface.BOLD);
        textTitle.setGravity(Gravity.CENTER);



        editText = new EditText(getActivity());
        editText.setGravity(Gravity.CENTER);



        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        return builder
                .setCustomTitle(textTitle)
                .setView(editText)
                .setPositiveButton("OK", this::onClick)
                .create();
    }

    @Override
    public void onClick(DialogInterface dialogInterface, int i){
        String servers = new WEB().get("http://"+editText.getText()+":3000/api/v1/getservers");
        SharedPreferences.Editor editor = mSettings.edit();
        editor.putString(APP_PREFERENCES_ip, editText.getText().toString());
        editor.apply();
        JSONObject jObj = null;
        try {
            jObj = new JSONObject(servers);
            if (jObj.has("servers")) {
                JSONArray jArr = jObj.getJSONArray("servers");
                for (int x = 0; x < jArr.length(); x++) {
                    db.execSQL("INSERT INTO servers(ip, alive) VALUES('" + jArr.getString(x) + "', '1')");
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
