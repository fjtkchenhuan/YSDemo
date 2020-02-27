package com.ys.twoscreen;

import android.content.Context;
import android.util.AttributeSet;
import android.view.SurfaceView;

/**
 * Created by Administrator on 2018/6/7.
 */

public class PreSurfaceView extends SurfaceView {
    public PreSurfaceView(Context context) {
        super(context);
    }

    public PreSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public PreSurfaceView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

//        int width = measureDimension(1920, widthMeasureSpec);
//        int height = measureDimension(1080, heightMeasureSpec);
//        setMeasuredDimension(1080, 1920);
    }

//    @Override
//    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
//        super.onLayout(changed, left, top, right, bottom);
//
//        setTop(-1920);
//
//    }

}
