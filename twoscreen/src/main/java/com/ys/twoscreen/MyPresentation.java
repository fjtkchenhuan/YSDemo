package com.ys.twoscreen;

import android.app.Presentation;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.SurfaceView;

/**
 * Created by RYX on 2016/6/23.
 */
public class MyPresentation extends Presentation {
    private SurfaceView presentSurface;

    public MyPresentation(Context outerContext, Display display) {
        super(outerContext, display);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.presentation_layout);
        presentSurface = (SurfaceView) findViewById(R.id.present_surface);
    }

    public SurfaceView getSurface() {
        return presentSurface;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        Log.d("5345345345", "567567");
        return super.onTouchEvent(event);
    }
}
