package com.example.qkx.speechtotext.utils;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by qkx on 16/10/28.
 */

public class FileUtil {

    public static String getCurrentDate() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd-HH:mm:ss");
        String date = dateFormat.format(new Date());
        return date;
    }

    public static void addStringToFile(String data, String fileName) {
        if (data == null || data.length() == 0) return;

        if (fileName == null) return;

        PrintWriter pw = null;
        try {
            pw = new PrintWriter(new FileWriter(fileName, true));
            pw.print(data);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (pw != null) {
                pw.close();
            }
        }
    }
}
