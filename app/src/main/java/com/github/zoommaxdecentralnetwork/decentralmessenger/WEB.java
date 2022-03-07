package com.github.zoommaxdecentralnetwork.decentralmessenger;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;

public class WEB {
    public String get(String urls) {
        ExecutorService executor = Executors.newFixedThreadPool(1);
        Callable<String> callable = () -> {
            try {
                URL url = new URL(urls);
                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                con.setConnectTimeout(5000);
                if (con.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    //Если запрос выполнен удачно, читаем полученные данные и далее, делаем что-то
                    String res = new BufferedReader(new InputStreamReader(con.getInputStream())).readLine();
                    return res;
                } else {
                    //Если запрос выполнен не удачно, делаем что-то другое

                    return con.getResponseMessage();
                }
            } catch (IOException e) {
                e.printStackTrace();
                return "error;"+e.getMessage()+";";
            }
        };
        FutureTask future = (FutureTask)executor.submit(callable);
        try {
            return (String) future.get();
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
            return "error;"+e.getMessage()+";";
        }
    }

    public String post(String urls, String payload){
        ExecutorService executorService = Executors.newFixedThreadPool(1);
        Callable<String> callable = () -> {
            HttpURLConnection connection = null;
            try {
                //Create connection
                URL url = new URL(urls);
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Length", Integer.toString(payload.getBytes().length));
                connection.setUseCaches(false);
                connection.setDoOutput(true);
                connection.setConnectTimeout(5000);

                DataOutputStream wr = new DataOutputStream (connection.getOutputStream());
                wr.write(payload.getBytes(StandardCharsets.UTF_8));
                wr.close();
                if (connection.getResponseCode() == HttpURLConnection.HTTP_OK){
                    String res = new BufferedReader(new InputStreamReader(connection.getInputStream())).readLine();
                    return res;
                } else {
                    //Если запрос выполнен не удачно, делаем что-то другое

                    return connection.getResponseMessage();
                }
            } catch (IOException e) {
                e.printStackTrace();
                return "error;"+e.getMessage()+";";
            }
        };
        FutureTask future = (FutureTask)executorService.submit(callable);
        try {
            return (String) future.get();
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
            return "error;"+e.getMessage()+";";
        }
    }
}
