package com.example.moengageapp.utils;

import android.content.Context;
import android.net.Uri;
import android.preference.PreferenceManager;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Utility {

    public static Uri writeToFile(String data) {
        File file = new File("htmlData.txt");
        try {
            file.createNewFile();
            FileOutputStream fOut = new FileOutputStream(file);
            OutputStreamWriter myOutWriter = new OutputStreamWriter(fOut);
            myOutWriter.append(data);

            myOutWriter.close();

            fOut.flush();
            fOut.close();
        } catch (IOException e) {
            return null;
        }
        return Uri.fromFile(file);
    }

    public static String readFromFile(Uri uri) {
        //Creating an InputStream object
        InputStream inputStream = null;
        try {
            inputStream = new FileInputStream(new File(uri.getPath()));
            //creating an InputStreamReader object
            InputStreamReader isReader = new InputStreamReader(inputStream);
            //Creating a BufferedReader object
            BufferedReader reader = new BufferedReader(isReader);
            StringBuilder sb = new StringBuilder();
            String str;
            while((str = reader.readLine())!= null){
                sb.append(str);
            }
            return sb.toString();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void saveHtmlPageInSharedPreferences(Context context, String htmlPage) {
        PreferenceManager.getDefaultSharedPreferences(context).edit().putString("HTML_PAGE", htmlPage).apply();
    }

    public static String getHtmlPageFromSharedPreferences(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getString("HTML_PAGE", "");
    }

    public static long convertDateStringToEpochMillis(String date) {
        long timeInMilliseconds = 0;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.ENGLISH);
        try {
            Date mDate = sdf.parse(date);
            timeInMilliseconds = mDate.getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return timeInMilliseconds;
    }

    public static String getPolishedDate(String date) {
        SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.ENGLISH);
        SimpleDateFormat outputFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.ENGLISH);
        Date mDate;
        try {
            mDate = inputFormat.parse(date);
            return outputFormat.format(mDate);
        } catch (ParseException e) {
            e.printStackTrace();
            return "";
        }
    }
}
