package com.ys.muiltcamerademo;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.SurfaceTexture;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Surface;
import android.view.TextureView;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

public class MainActivity extends Activity {
    List<TextureView> surfaceViews;
    List<CameraSurface> surfaces;
    Semaphore mCameraOpenCloseLock = new Semaphore(1);

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        surfaceViews = new ArrayList<>();
        surfaces = new ArrayList<>();
        surfaceViews.add((TextureView) findViewById(R.id.camera1));
        surfaceViews.add((TextureView) findViewById(R.id.camera2));
        surfaceViews.add((TextureView) findViewById(R.id.camera3));
        surfaceViews.add((TextureView) findViewById(R.id.camera4));
        surfaceViews.add((TextureView) findViewById(R.id.camera5));
        surfaceViews.add((TextureView) findViewById(R.id.camera6));
        surfaceViews.add((TextureView) findViewById(R.id.camera7));
        surfaceViews.add((TextureView) findViewById(R.id.camera8));
        surfaceViews.add((TextureView) findViewById(R.id.camera9));
        surfaceViews.add((TextureView) findViewById(R.id.camera10));
        if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.CAMERA}, 300);
        } else {
            openCamera();
        }

    }

    private void openCamera() {
        String[] cameraIdList = MyCamera.getCameraIdList(this);
        if (cameraIdList != null) {
            for (int i = 0; i < cameraIdList.length; i++) {
                Log.e("heef", "camera Id:" + cameraIdList[i]);
                CameraSurface cameraSurface = new CameraSurface(this, cameraIdList[i]);
                surfaceViews.get(i).setSurfaceTextureListener(cameraSurface);
                surfaces.add(cameraSurface);
            }
        }
//        CameraSurface cameraSurface1 = new CameraSurface(this, "0");
//        surfaceViews.get(0).setSurfaceTextureListener(cameraSurface1);
//        surfaces.add(cameraSurface1);
//        CameraSurface cameraSurface2 = new CameraSurface(this, "1");
//        surfaceViews.get(1).setSurfaceTextureListener(cameraSurface2);
//        surfaces.add(cameraSurface2);
//        CameraSurface cameraSurface3 = new CameraSurface(this, "2");
//        surfaceViews.get(2).setSurfaceTextureListener(cameraSurface3);
//        surfaces.add(cameraSurface3);
//        CameraSurface cameraSurface4 = new CameraSurface(this, "3");
//        surfaceViews.get(3).setSurfaceTextureListener(cameraSurface4);
//        surfaces.add(cameraSurface4);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 300) {
            openCamera();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        for (int i = 0; i < surfaces.size(); i++) {
            surfaces.get(i).release();
        }
    }


    class CameraSurface implements TextureView.SurfaceTextureListener {
        MyCamera mMyCamera;
        String cameraId;

        public CameraSurface(Context context, String cameraId) {
            mMyCamera = new MyCamera(context, mCameraOpenCloseLock);
            this.cameraId = cameraId;
        }

        public void release() {
            if (mMyCamera != null)
                mMyCamera.release();
        }

        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture texture
                , int width, int height) {
            // 当TextureView可用时，打开摄像头
            if (mMyCamera != null)
                mMyCamera.openCamera(cameraId, new Surface(texture));
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture texture
                , int width, int height) {
        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture texture) {

            return true;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture texture) {
        }
    }
}
