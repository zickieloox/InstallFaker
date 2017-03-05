package com.zic.installfaker.utils;

import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

public class ExeUtils {

    private static final String TAG = "ExeUtils";
    private static String exeOutput, exeError;

    public static Boolean run(String cmd, boolean asRoot) {
        StringBuilder out = new StringBuilder();
        StringBuilder err = new StringBuilder();
        try {
            String line;

            Process process;

            if (asRoot) {
                process = Runtime.getRuntime().exec("su");
            } else {
                process = Runtime.getRuntime().exec(cmd);
            }

            OutputStream stdin = process.getOutputStream();
            InputStream stderr = process.getErrorStream();
            InputStream stdout = process.getInputStream();

            if (asRoot) {
                stdin.write((cmd + "\n").getBytes());
            }
            stdin.write("exit\n".getBytes());
            stdin.flush();
            stdin.close();

            BufferedReader br = new BufferedReader(new InputStreamReader(stdout));
            while ((line = br.readLine()) != null) {
                out.append(line).append("\n");
            }
            br.close();

            br = new BufferedReader(new InputStreamReader(stderr));
            while ((line = br.readLine()) != null) {
                err.append(line).append("\n");
            }
            br.close();

            process.waitFor();
            process.destroy();
        } catch (Exception e) {
            Log.e(TAG, e.toString());
            exeError = "Exception";
            return false;
        }
        exeOutput = out.toString().trim(); //Trim to remove \n
        exeError = err.toString().trim();

        // Commands is executable or not
        return exeError.length() == 0;
    }

    public static String getExeOutput() {
        return exeOutput;
    }

    public static String getExeError() {
        return exeError;
    }
}