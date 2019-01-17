package com.mafdy.onthemove.utils;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

import com.mafdy.onthemove.MainActivity;

/**
 * Created by SBP on 7/18/2018.
 */

public class Utils {

    public static void displayAlertDialog1(Context c, String title, String message)
    {
        AlertDialog.Builder build = new AlertDialog.Builder(c);
        build.setTitle(title);
        build.setMessage(message);
        build.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        build.show();
    }
}
