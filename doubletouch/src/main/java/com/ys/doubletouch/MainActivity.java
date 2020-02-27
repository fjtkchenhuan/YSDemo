package com.ys.doubletouch;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Presentation;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.display.DisplayManager;
import android.media.MediaRouter;
import android.os.Build;
import android.provider.Settings;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    private MyPresentation myPresentation;
    private Display display;
    private TextView textView;
    static final String[] PERMISSION_LIST = new String[]{
            Manifest.permission.WRITE_SETTINGS, Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.SYSTEM_ALERT_WINDOW};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        checkPermission();

        findViewById(R.id.btn1).setOnClickListener(this);
        findViewById(R.id.btn2).setOnClickListener(this);
        findViewById(R.id.btn3).setOnClickListener(this);
        findViewById(R.id.btn4).setOnClickListener(this);

    }

    private void checkPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            boolean hasUnCkeck = false;
            for (int i = 0; i < PERMISSION_LIST.length; i++) {
                if (checkSelfPermission(PERMISSION_LIST[i]) != PackageManager.PERMISSION_GRANTED) {
                    hasUnCkeck = true;
                }
            }
            if (hasUnCkeck) {
                requestPermissions(PERMISSION_LIST, 300);
            }


            if (!Settings.canDrawOverlays(this)) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivityForResult(intent, 1);
            } else {
                //TODO do something you need
            }
        }
    }

    int index = 0;
    int[] texts = {R.string.first,R.string.second,R.string.third,R.string.forth};
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn1:
                MediaRouter.RouteInfo localRouteInfo = ((MediaRouter)getSystemService("media_router")).getSelectedRoute(2);
                display = localRouteInfo.getPresentationDisplay();
                showPresentation(display);
                break;
            case R.id.btn2:
                Display[] arrayOfDisplay = ((DisplayManager)getSystemService("display"))
                        .getDisplays("android.hardware.display.category.PRESENTATION");
                if (arrayOfDisplay.length != 0)
                    showPresentation(arrayOfDisplay[0]);
                break;
            case R.id.btn3:
                if (myPresentation != null) {
                    if (index > 3)
                        index = 0;
                    textView.setText(texts[index]);
                    Log.d("chenhuan","setBackgroundColor with index " + index);
                    index ++;
                }
                break;
            case R.id.btn4:
                if (myPresentation != null) {
                    myPresentation.dismiss();
                    myPresentation = null;
                }
                break;
        }
    }


    private void showPresentation(Display display) {
        if (myPresentation == null) {
            myPresentation = new MyPresentation(this, display);
        }

        myPresentation.getWindow().setType(WindowManager.LayoutParams.TYPE_PHONE);
        myPresentation.show();
    }

    private class MyPresentation extends Presentation {

        public MyPresentation(Context outerContext, Display display) {
            super(outerContext, display);
        }

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.presentation_layout);

            textView = (TextView)findViewById(R.id.text);
            findViewById(R.id.click).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    new AlertDialog.Builder(MainActivity.this)
                            .setTitle("Dialog")
                            .setMessage("Prsentation Click Test")
                            .setCancelable(false)
                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
//                                    dismiss();

                                }
                            }).create().show();
                }
            });

            findViewById(R.id.dismiss).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dismiss();
                }
            });
        }
    }
}
