package com.github.zoommaxdecentralnetwork.decentralmessenger;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

import java.security.InvalidAlgorithmParameterException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Security;
import java.security.spec.ECGenParameterSpec;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import ru.zoommax.hul.HexUtils;

public class MainActivity extends AppCompatActivity {
    private static final int NOTIFY_ID = new Random().nextInt();
    private static String CHANNEL_ID = "DMch";
    protected SQLiteDatabase db;
    protected Cursor cursor;
    public static final String APP_PREFERENCES = "mysettings";
    public static final String APP_PREFERENCES_KEYpub = "keyPub";
    public static final String APP_PREFERENCES_KEYpriv = "keyPriv";
    public static final String APP_PREFERENCES_ip = "ip";

    static {
        Security.removeProvider("BC");
        // Confirm that positioning this provider at the end works for your needs!
        Security.addProvider(new BouncyCastleProvider());
    }

    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, CHANNEL_ID, importance);
            channel.setDescription("desc");
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        createNotificationChannel();
        db = getBaseContext().openOrCreateDatabase("decmes.db", MODE_PRIVATE, null);
        FloatingActionButton addPerson = (FloatingActionButton) findViewById(R.id.personAdd);
        FloatingActionButton iam = (FloatingActionButton) findViewById(R.id.iam);
        ListView list = (ListView)findViewById(R.id.list);
        SharedPreferences mSettings = getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);
        if (!mSettings.contains(APP_PREFERENCES_KEYpub)) {
            HashMap<String, String> keys = new Crypto().getKeys();
            if (keys.containsKey("error")){

            }else {
                SharedPreferences.Editor editor = mSettings.edit();
                editor.putString(APP_PREFERENCES_KEYpub, keys.get("pubkey"));
                editor.putString(APP_PREFERENCES_KEYpriv, keys.get("privkey"));
                editor.apply();
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
        new Timer().schedule(new GetMesseges(db, mSettings.getString(APP_PREFERENCES_KEYpub, "null"), CHANNEL_ID, NOTIFY_ID, MainActivity.this), 0, 1000);
        List<Contacts> t = new ArrayList<Contacts>();
        ArrayAdapter<Contacts> adapter = new ContactsAdapter(MainActivity.this, R.layout.contactlist, t);
        list.setAdapter(adapter);
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                cursor = db.rawQuery("SELECT * FROM names", null);
                t.clear();
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
                    int index = list.getFirstVisiblePosition();
                    View v = list.getChildAt(0);
                    int top = (v == null) ? 0 : v.getTop();
                    adapter.notifyDataSetChanged();
                    list.setSelectionFromTop(index, top);
                });
            }
        }, 0, 1000);

        list.setOnItemClickListener((adapterView, view, i, l) -> {
            TextView txtKey = (TextView) view.findViewById(R.id.key);
            String key = txtKey.getText().toString();
            TextView txtName = (TextView) view.findViewById(R.id.name);
            String name = txtName.getText().toString();
            Intent intent = new Intent(MainActivity.this, Dialog.class);
            intent.putExtra("name", name);
            intent.putExtra("key", key);
            startActivity(intent);
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