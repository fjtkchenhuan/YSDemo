package com.ys.ysdemo;

import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

public class AlertDialog {
    private static AlertDialog alertDialog = null;
    private Context context;
    private Dialog dialog;
    private TextView txt_title;
    private TextView txt_msg;
    private Button btn_neg;

    public static AlertDialog getInstance(Context context){
        if (alertDialog==null){
            synchronized (AlertDialog.class) {
                if (alertDialog == null) {
                    alertDialog = new AlertDialog(context).builder();
                }
            }
        }
        return alertDialog;
    }

    public AlertDialog(Context context) {
        this.context = context;
    }

    public AlertDialog builder() {
        View view = LayoutInflater.from(context).inflate(R.layout.view_alertdialog, null);
        txt_title = (TextView) view.findViewById(R.id.txt_title);
        txt_msg = (TextView) view.findViewById(R.id.txt_msg);
        btn_neg = (Button) view.findViewById(R.id.btn_neg);

        dialog = new Dialog(context);
        dialog.setContentView(view);
        dialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);

        return this;
    }

    public void setTitle(String title) {
            txt_title.setText(title);
    }

    public void setMsg(String msg) {
            txt_msg.setText(msg);
    }

    public void setPositiveButton(String text,
                                         final View.OnClickListener listener) {
        btn_neg.setText(text);
        btn_neg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(listener!=null) {
                    listener.onClick(v);
                }
                dialog.dismiss();
            }
        });
    }

    public void show() {
        dialog.show();
    }

    public void dismiss() {
        dialog.dismiss();
    }
}



