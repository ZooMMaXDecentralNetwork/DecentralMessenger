package com.github.zoommaxdecentralnetwork.decentralmessenger;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class GetMesseges extends TimerTask {
    SQLiteDatabase db;
    String myKey;
    public GetMesseges(SQLiteDatabase db, String myKey){
        this.db = db;
        this.myKey = myKey;
    }
    @Override
    public void run() {
            List<String> servers = new ArrayList<>();
            Cursor cursor = db.rawQuery("SELECT * FROM servers WHERE alive like '1'", null);
            while (cursor.moveToNext()){
                servers.add(cursor.getString(0));
            }
            ExecutorService executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
            for (String ip: servers){
                Runnable worker = () -> {
                    try {
                        JSONObject jsonObject = new JSONObject(new WEB().get("http://"+ip+":3000/api/v1/syncmessageclient?"+myKey));
                        if (jsonObject.has("response")){
                            JSONArray jsonArray = jsonObject.getJSONArray("response");
                            for (int x = 0; x < jsonArray.length(); x++){
                                JSONObject jObj = jsonArray.getJSONObject(x);
                                String sender = jObj.getString("sender");
                                String receiver = jObj.getString("receiver");
                                String data = jObj.getString("data");
                                String ts = jObj.getString("ts");
                                String hash = jObj.getString("hash");
                                Cursor cur = db.rawQuery("SELECT * FROM messeges WHERE hash like '"+hash+"'", null);
                                boolean needSaveMsg = true;
                                while (cur.moveToNext()){
                                    if (cur.getString(4).equals(hash)){
                                        needSaveMsg = false;
                                    }
                                }
                                if (needSaveMsg){
                                    db.execSQL("INSERT INTO messeges(sender, receiver, data, ts, hash) VALUES('"+sender+"','"+receiver+"','"+data+"','"+ts+"','"+hash+"')");
                                }

                                cur = db.rawQuery("SELECT * FROM names WHERE publickey like '"+sender+"'", null);
                                boolean needSaveSender = true;
                                while (cur.moveToNext()){
                                    if (cur.getString(0).equals(sender)){
                                        needSaveSender = false;
                                    }
                                }
                                if (needSaveSender){
                                    db.execSQL("INSERT INTO names(publickey, name, newmsg) VALUES('"+sender+"','"+sender+"','1')");
                                }else {
                                    db.execSQL("UPDATE names SET newmsg = '1' WHERE publickey like '"+sender+"'");
                                }
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                };
                executorService.execute(worker);
            }
            executorService.shutdown();
            while (!executorService.isTerminated()){
            }
    }
}
