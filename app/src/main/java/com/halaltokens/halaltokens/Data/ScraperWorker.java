/*
 * Scrapes the data from the uRooms website using a thread pool
 */


package com.halaltokens.halaltokens.Data;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.halaltokens.halaltokens.Runnables.AgRunnable;
import com.halaltokens.halaltokens.Runnables.ArtsRunnable;
import com.halaltokens.halaltokens.Runnables.CompSciRunnable;
import com.halaltokens.halaltokens.Runnables.EngRunnable;
import com.halaltokens.halaltokens.Runnables.HealthScienceRunnable;
import com.halaltokens.halaltokens.Runnables.SciEastRunnable;
import com.halaltokens.halaltokens.Runnables.SciHubRunnable;
import com.halaltokens.halaltokens.Runnables.SciNorthRunnable;
import com.halaltokens.halaltokens.Runnables.SciSouthRunnable;
import com.halaltokens.halaltokens.Runnables.SciWestRunnable;

import org.jsoup.Connection;
import org.jsoup.Jsoup;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

import androidx.work.Worker;
import androidx.work.WorkerParameters;

public class ScraperWorker extends Worker {

    public static Connection.Response response = null;
    private String responseBody = null;

    public ScraperWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    //checks firebase to see if scraping has already been done, if not, start each runnable in a new thread to scrape the room info
    @Override
    public Worker.Result doWork() {
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference ref = database.getReference("");
        String day = "";

        try {
            //connects to firebase
            responseBody = Jsoup.connect("https://halaltokens.firebaseio.com/Times+on+phone.json?print=pretty").ignoreContentType(true).execute().body();
        } catch (IOException e) {
            e.printStackTrace();
        }

        JsonParser jsonParser = new JsonParser();
        JsonObject jsonObject = jsonParser.parse(responseBody).getAsJsonObject();

        for (Map.Entry<String, JsonElement> object : jsonObject.entrySet()) {
            day = jsonParser.parse(object.getValue().toString()).getAsJsonObject().get("dayOfWeek").getAsString();
        }

        if (!day.equals(LocalDateTime.now().getDayOfWeek().toString())) {

            ref.setValue(null);
            ref.child("Times on phone").push().setValue(LocalDateTime.now());

            try {

                Map<String, String> map = new HashMap<>();
                map.put("p_butn", "1");
                map.put("p_title", "Welcome+to+SISWeb");
                map.put("p_username", "15551647");
                map.put("p_password", "Destiny10");
                map.put("p_forward", "W_HU_MENU.P_DISPLAY_MENU¬p_menu=SI-HOME");
                map.put("p_lmet", "SISWEB");
                map.put("p_parameters", "");

                try {
                    Connection.Response loginForm = Jsoup.connect("https://sisweb.ucd.ie/usis/W_HU_MENU.P_DISPLAY_MENU?p_menu=SI-HOME")
                            .method(Connection.Method.GET)
                            .execute();
                    response = Jsoup.connect("https://sisweb.ucd.ie/usis/W_HU_LOGIN.P_PROC_LOGINBUT")
                            .method(Connection.Method.POST)
                            .data(map)
                            .cookies(loginForm.cookies())
                            .execute();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                ThreadPoolExecutor threadPoolExecutor = (ThreadPoolExecutor) Executors.newCachedThreadPool();
                //Comp Sci
                threadPoolExecutor.execute(new CompSciRunnable(getApplicationContext()));
                //AG
                threadPoolExecutor.execute(new AgRunnable(getApplicationContext()));
                //science east
                threadPoolExecutor.execute(new SciEastRunnable(getApplicationContext()));
                //science hub
                threadPoolExecutor.execute(new SciHubRunnable(getApplicationContext()));
                //science north
                threadPoolExecutor.execute(new SciNorthRunnable(getApplicationContext()));
                //science south
                threadPoolExecutor.execute(new SciSouthRunnable(getApplicationContext()));
                //science west
                threadPoolExecutor.execute(new SciWestRunnable(getApplicationContext()));
                //health science
                threadPoolExecutor.execute(new HealthScienceRunnable(getApplicationContext()));
                //eng
                threadPoolExecutor.execute(new EngRunnable(getApplicationContext()));
                //arts
                threadPoolExecutor.execute(new ArtsRunnable(getApplicationContext()));
                threadPoolExecutor.shutdown();

                return Result.SUCCESS;
            } catch (Exception e) {
                e.printStackTrace();
                return Result.FAILURE;
            }

        }

        return Result.SUCCESS;
    }

}

