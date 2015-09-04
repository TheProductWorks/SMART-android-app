package ie.teamchile.smartapp.util;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;

import ie.teamchile.smartapp.R;

/**
 * Created by user on 7/16/15.
 */
public class CustomDialogs {
    public CustomDialogs() {}

    public void showErrorDialog(Context context, String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage(message)
                .setTitle("Error")
                .setIcon(R.drawable.ic_alert_triangle_red)
                .setCancelable(false)
                .setPositiveButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        //do things
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }

    public void showWarningDialog(Context context, String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage(message)
                .setTitle("Warning")
                .setIcon(R.drawable.ic_alert_triangle_yellow)
                .setCancelable(false)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        //do things
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        //do things
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }

    public ProgressDialog showProgressDialog(Context context, String message) {
        ProgressDialog pd;

        pd = new ProgressDialog(context);
        pd.setMessage(message);
        pd.setCanceledOnTouchOutside(false);
        pd.setCancelable(false);
        pd.show();

        return pd;
    }
}
