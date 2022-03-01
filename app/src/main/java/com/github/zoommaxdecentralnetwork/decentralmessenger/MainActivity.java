package com.github.zoommaxdecentralnetwork.decentralmessenger;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.ListView;

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

import ru.zoommax.hul.HexUtils;

public class MainActivity extends AppCompatActivity {
    protected SQLiteDatabase db;
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
        db.execSQL("CREATE TABLE IF NOT EXISTS names(publickey TEXT NOT NULL, name TEXT)");
        db.execSQL("CREATE TABLE IF NOT EXISTS servers(ip TEXT NOT NULL, alive TEXT NOT NULL)");
        if (!mSettings.contains(APP_PREFERENCES_ip)){
            DialogFragmentStart dialogFragmentStart = new DialogFragmentStart(db, mSettings);
            dialogFragmentStart.show(getSupportFragmentManager(), "start");
        }

        Cursor cursor = db.rawQuery("SELECT * FROM servers", null);
        String tmpServers = "";
        while (cursor.moveToNext()){

            tmpServers += cursor.getString(1)+";";
        }
        String[] servers = tmpServers.split(";");

        //ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.contactlist);


        iam.setOnClickListener(view -> {
            String key = mSettings.getString(APP_PREFERENCES_KEYpub, "error");
            DialogFragmentKey dialogFragmentKey = new DialogFragmentKey("My Account Public Key", key);
            dialogFragmentKey.show(getSupportFragmentManager(), "key");
        });

        addPerson.setOnClickListener(view -> {

        });

    }
}