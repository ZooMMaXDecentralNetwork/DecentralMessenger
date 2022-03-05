package com.github.zoommaxdecentralnetwork.decentralmessenger;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.json.JSONException;
import org.json.JSONObject;

import java.sql.Time;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class Dialog extends AppCompatActivity {
    SQLiteDatabase db;
    public static final String APP_PREFERENCES = "mysettings";
    public static final String APP_PREFERENCES_KEYpub = "keyPub";
    public static final String APP_PREFERENCES_KEYpriv = "keyPriv";
    protected int tsize;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_layout);
        SharedPreferences mSettings = getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);
        Bundle arguments = getIntent().getExtras();
        String name = arguments.getString("name");
        String key = arguments.getString("key");
        String myPubKey = mSettings.getString(APP_PREFERENCES_KEYpub, "");
        String myPrivKey = mSettings.getString(APP_PREFERENCES_KEYpriv, "");
        db = getBaseContext().openOrCreateDatabase("decmes.db", MODE_PRIVATE, null);
        ListView listView = (ListView) findViewById(R.id.msgs);
        EditText editText = (EditText) findViewById(R.id.msg);
        FloatingActionButton send = (FloatingActionButton) findViewById(R.id.send);
        List<DialogGS> t = new ArrayList<DialogGS>();
        ArrayAdapter<DialogGS> adapter = new DialogAdapter(Dialog.this, R.layout.dialog_listview, t);
        listView.setAdapter(adapter);
        tsize = t.size();
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                db.execSQL("UPDATE names SET newmsg = '0' WHERE publickey LIKE '"+key+"'");
                t.clear();
                int lastPos = listView.getLastVisiblePosition();
                Cursor cursor = db.rawQuery("SELECT * FROM messeges WHERE sender like '"+key+"' OR receiver like '"+key+"'", null);
                while (cursor.moveToNext()){
                    String sender = cursor.getString(0);
                    String receiver = cursor.getString(1);
                    String data = cursor.getString(2);
                    String ts = cursor.getString(3);
                    if (sender.equals(key)) {
                        t.add(new DialogGS(name, data, ts));
                    }
                    if (receiver.equals(key)){
                        t.add(new DialogGS("Me", data, ts));
                    }
                }
                runOnUiThread(() -> {
                    t.sort(new Comparator<DialogGS>() {
                        @Override
                        public int compare(DialogGS t0, DialogGS t1) {
                            long a = Long.parseLong(t0.ts);
                            long b = Long.parseLong(t1.ts);
                            long res = a - b;
                            if (res > a){
                                return -1;
                            }
                            if (res < a){
                                return 1;
                            }
                            return 0;
                        }
                    });
                    int index = listView.getFirstVisiblePosition();
                    View v = listView.getChildAt(0);
                    int top = (v == null) ? 0 : v.getTop();
                    if (tsize != t.size()) {
                        adapter.notifyDataSetChanged();
                        listView.smoothScrollToPosition(listView.getLastVisiblePosition()+(t.size()-tsize));
                    }
                    tsize = t.size();
                    /*if (lastPos != tsize) {
                        listView.setSelectionFromTop(index, top);
                    }*/
                });
            }
        }, 0, 1000);

        send.setOnClickListener(view -> {
            String text = editText.getText().toString();
            if (!text.equals("")) {
                editText.setText("");
                JSONObject out = new JSONObject();
                try {
                    out.put("sender", mSettings.getString(APP_PREFERENCES_KEYpub, ""));
                    out.put("receiver", key);
                    out.put("data", text);
                    out.put("peertopeer", "0");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                new Thread(() -> {
                    Cursor cursor = db.rawQuery("SELECT * FROM servers WHERE alive like '1'", null);
                    boolean goodSend = false;
                    while (!goodSend) {
                        if (cursor.moveToNext()) {
                            String ip = cursor.getString(0);
                            String result = new WEB().post("http://"+ip+":3000/api/v1/inputmessage", out.toString());
                            if (!result.split(";")[0].equals("error")){
                                goodSend = true;
                            }
                        }
                    }
                }).start();
            }
        });

    }
}

class DialogGS{

    String name;
    String text;
    String ts;

    public DialogGS(String name, String text, String ts){
        this.text = text;
        this.name = name;
        this.ts = ts;
    }
}

class DialogAdapter extends ArrayAdapter<DialogGS>{

    public DialogAdapter(Context context, int layout, List<DialogGS> msgs) {
        super(context, layout, msgs);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        DialogGS dialogGS = getItem(position);
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext())
                    .inflate(R.layout.dialog_listview, null);
        }
        ((TextView) convertView.findViewById(R.id.user))
                .setText(dialogGS.name);
        ((TextView) convertView.findViewById(R.id.text))
                .setText(dialogGS.text);
        return convertView;
    }
}
