package com.github.zoommaxdecentralnetwork.decentralmessenger;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.security.InvalidAlgorithmParameterException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.spec.ECGenParameterSpec;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import ru.zoommax.hul.HexUtils;

public class MainActivity extends AppCompatActivity {
    protected SQLiteDatabase db;
    protected Cursor cursor;
    public static final String APP_PREFERENCES = "mysettings";
    public static final String APP_PREFERENCES_KEYpub = "keyPub";
    public static final String APP_PREFERENCES_KEYpriv = "keyPriv";
    public static final String APP_PREFERENCES_ip = "ip";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        db = getBaseContext().openOrCreateDatabase("decmes.db", MODE_PRIVATE, null);
        FloatingActionButton addPerson = (FloatingActionButton) findViewById(R.id.personAdd);
        FloatingActionButton iam = (FloatingActionButton) findViewById(R.id.iam);
        ListView list = (ListView)findViewById(R.id.list);
        SharedPreferences mSettings = getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);
        if (!mSettings.contains(APP_PREFERENCES_KEYpub)) {
            try {
                ECGenParameterSpec ecSpec = new ECGenParameterSpec("secp256r1");
                KeyPairGenerator g = KeyPairGenerator.getInstance("EC");
                g.initialize(ecSpec, new SecureRandom());
                KeyPair keypair = g.generateKeyPair();
                PublicKey publicKey = keypair.getPublic();
                PrivateKey privateKey = keypair.getPrivate();
                String pubKey = HexUtils.toString(publicKey.getEncoded());
                String privKey = HexUtils.toString(privateKey.getEncoded());
                SharedPreferences.Editor editor = mSettings.edit();
                editor.putString(APP_PREFERENCES_KEYpub, pubKey);
                editor.putString(APP_PREFERENCES_KEYpriv, privKey);
                editor.apply();
            } catch (NoSuchAlgorithmException | InvalidAlgorithmParameterException e) {
                e.printStackTrace();
            }
        }
        db.execSQL("CREATE TABLE IF NOT EXISTS messeges(sender TEXT NOT NULL, receiver TEXT NOT NULL, data TEXT NOT NULL, ts TEXT NOT NULL, hash TEXT NOT NULL)");
        db.execSQL("CREATE TABLE IF NOT EXISTS names(publickey TEXT NOT NULL, name TEXT NOT NULL, newmsg TEXT NOT NULL)");
        db.execSQL("CREATE TABLE IF NOT EXISTS servers(ip TEXT NOT NULL, alive TEXT NOT NULL)");
        if (!mSettings.contains(APP_PREFERENCES_ip)){
            DialogFragmentStart dialogFragmentStart = new DialogFragmentStart(db, mSettings);
            dialogFragmentStart.show(getSupportFragmentManager(), "start");
        }
        cursor = db.rawQuery("SELECT * FROM servers", null);
        while (cursor.moveToNext()) {
            String ip = cursor.getString(0);
            String alive = new WEB().get("http://" + ip + ":3000/api/v1/pingclient");
            if (alive != null) {
                if (alive.equals("alive")) {
                    db.execSQL("UPDATE servers SET alive = '1' WHERE ip like '" + ip + "'");
                }
            } else {
                db.execSQL("UPDATE servers SET alive = '0' WHERE ip like '" + ip + "'");
            }
        }
        new Timer().schedule(new GetMesseges(db, mSettings.getString(APP_PREFERENCES_KEYpub, "null")), 0, 1000);
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                cursor = db.rawQuery("SELECT * FROM names", null);
                List<Contacts> t = new ArrayList<Contacts>();
                while (cursor.moveToNext()){
                    t.add(new Contacts(cursor.getString(0), cursor.getString(1), cursor.getString(2)));
                }
                runOnUiThread(() -> {
                    t.sort(new Comparator<Contacts>() {
                        @Override
                        public int compare(Contacts t0, Contacts t1) {
                            return t0.newMsg - t1.newMsg;
                        }
                    });
                    ArrayAdapter<Contacts> adapter = new ContactsAdapter(MainActivity.this, R.layout.contactlist, t);
                    list.setAdapter(adapter);
                });
            }
        }, 0, 1000);
        list.setOnItemClickListener((adapterView, view, i, l) -> {

        });

        list.setOnItemLongClickListener((adapterView, view, i, l) -> {
            TextView txt = (TextView) view.findViewById(R.id.key);
            String key = txt.getText().toString();
            DialogFragmentNewContact dialogFragmentNewContact = new DialogFragmentNewContact(db, true, key);
            dialogFragmentNewContact.show(getSupportFragmentManager(), "ncontlst");
            return false;
        });

        iam.setOnClickListener(view -> {
            String key = mSettings.getString(APP_PREFERENCES_KEYpub, "error");
            DialogFragmentKey dialogFragmentKey = new DialogFragmentKey("My Account Public Key", key);
            dialogFragmentKey.show(getSupportFragmentManager(), "key");
        });

        addPerson.setOnClickListener(view -> {
            DialogFragmentNewContact dialogFragmentNewContact = new DialogFragmentNewContact(db, false);
            dialogFragmentNewContact.show(getSupportFragmentManager(), "ncont");
        });

    }
}