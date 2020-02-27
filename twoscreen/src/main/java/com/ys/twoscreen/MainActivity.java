package com.ys.twoscreen;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.hardware.display.DisplayManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.util.Log;
import android.util.SparseArray;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.SeekBar;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

// 目前想在一个surfaceView里面实现图片和视频操作起来比较困难。
// 要分别用两个控件来，一个显示视频，一个显示图片，然后适时隐藏。
// 视频用surfaceView. 图片还是用viewPager来.

public class MainActivity extends Activity {

    // 主屏播放视频的路径。
    public final static String LVDS_PATH = Environment.getExternalStorageDirectory().getPath() + "/lvds";
    //Environment.getExternalStorageDirectory().getPath() + "/Download/lvds" ;

    static final String[] PERMISSION_LIST = new String[]{
            Manifest.permission.WRITE_SETTINGS, Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.CALL_PHONE, Manifest.permission.ACCESS_COARSE_LOCATION};

    private static final String TAG = "---MainActivity---";

    private List<String> lvdsVideoPath = new ArrayList<>();
    private int nowLvdsPosition;
    private List<String> hdmiVideoPath = new ArrayList<>();
    private int nowHdmiPosition;

    // 两个player
    private MediaPlayer screen1Player;
    private MediaPlayer screen2Player;

    private DisplayManager mDisplayManager;

    private SurfaceView mainSurface;
    private SurfaceView hdmiSurface;

    private MyPresentation myPresentation;

    private PopupWindow videoControlPop;

    private boolean isVideoControlShowing = false;
    private SeekBar videoProgress;

    private final static int HIDE_POP = 0x111;
    private final static int HIDE_POP_TIME = 10 * 1000;
    private final static int CHANGE_CONTROL_PIC_1 = 0x112;
    private final static int CHANGE_CONTROL_PIC_2 = 0x113;
    private final static int CHANGE_PLAY_ICON = 0x114;
    private final static int CHANGE_PAUSE_ICON = 0x115;

    private final static int WRITE_1_TO_STATE = 0x126;
    private boolean isAirplay = true;
    private boolean isPause = false;
    private boolean isControlFirstScreen = true;

    private ImageView airPlay, playPre, playNext, playOrPause, controlSwitch;

    private int currentVideoTime;


    private Timer timer;

    boolean isFirstAirPlay = true;

    private final static String SERVICE_ACTION = "com.ryx.twovideos.actions";

    // List of all currently visible presentations indexed by display id.
    private final SparseArray<MyPresentation> mActivePresentations = new SparseArray<MyPresentation>();

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case HIDE_POP:
                    if (isVideoControlShowing) {
                        videoControlPop.dismiss();
                        videoControlPop = null;
                        // 停止刷新seekbar计时。
                        timer.cancel();
                    }
                    break;

                case CHANGE_CONTROL_PIC_1:
                    controlSwitch.setImageDrawable(getResources().getDrawable(R.drawable.control_1));
                    break;
                case CHANGE_CONTROL_PIC_2:
                    controlSwitch.setImageDrawable(getResources().getDrawable(R.drawable.control_2));
                    break;

                case CHANGE_PLAY_ICON:
                    playOrPause.setImageDrawable(getResources().getDrawable(R.drawable.play));
                    break;

                case CHANGE_PAUSE_ICON:
                    playOrPause.setImageDrawable(getResources().getDrawable(R.drawable.pause));
                    break;
                default:
                    break;

            }
        }
    };
    private SharedPreferences sharedPreferences;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//        checkPermission();

        if (!new File(LVDS_PATH).exists()) {
            Toast.makeText(this, "请在内置存储根目录放置lvds文件夹和视频", Toast.LENGTH_LONG).show();
            finish();
        }
        lvdsVideoPath = FileUtils.getPaths(LVDS_PATH);

        mDisplayManager = (DisplayManager) getSystemService(Context.DISPLAY_SERVICE);
//        mDisplayManager.registerDisplayListener(mDisplayListener, null);
        initView();
        sharedPreferences = getSharedPreferences("isAirPlay", 0);
        isAirplay = sharedPreferences.getBoolean("isAirPlay", true);
        if (isAirplay && Build.MODEL.contains("rk3288")) {
            Intent i = new Intent(MainActivity.this, BackgroundPlayService.class);
            startService(i);
//                screen1Player.setVolume(0, 0);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
            if (isAirplay) {
                Intent i = new Intent(MainActivity.this, BackgroundPlayService.class);
                startService(i);
//                screen1Player.setVolume(0, 0);
            }
        }
    }

    void initView() {
        mainSurface = (SurfaceView) findViewById(R.id.main_surface);
        mainSurface.getHolder().addCallback(new MySurfaceCallBack());

        mainSurface.setZOrderOnTop(false);
        mainSurface.setFocusable(false);
        screen1Player = new MediaPlayer();
        playVideo(screen1Player, nowLvdsPosition);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            if (videoControlPop == null) showBottomPop();
        }
        return super.onTouchEvent(event);
    }

    // surface 的回调接口。
    class MySurfaceCallBack implements SurfaceHolder.Callback {

        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            screen1Player.setDisplay(holder);
        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {

        }
    }

    // 播放结束监听。
    class MyVideoFinishListener implements MediaPlayer.OnCompletionListener {

        @Override
        public void onCompletion(MediaPlayer mp) {
            if (mp == screen1Player) {
                nowLvdsPosition++;
                if (nowLvdsPosition >= lvdsVideoPath.size()) nowLvdsPosition = 0;
                playVideo(mp, nowLvdsPosition);
            }

            if (mp == screen2Player) {
                nowHdmiPosition++;
                if (nowHdmiPosition >= hdmiVideoPath.size()) nowHdmiPosition = 0;
                screen2Player.reset();
                try {
                    screen2Player.setDataSource(hdmiVideoPath.get(nowHdmiPosition));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    // 显示Pop
    private void showBottomPop() {
        mHandler.sendEmptyMessageDelayed(HIDE_POP, HIDE_POP_TIME);
        isVideoControlShowing = true;
        mainSurface.setZOrderOnTop(false);
        LayoutInflater inflater = getLayoutInflater();
        View bottomPopView = inflater.inflate(R.layout.video_control_layout, null);
        videoProgress = (SeekBar) bottomPopView.findViewById(R.id.video_progress);
        videoProgress.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                screen1Player.seekTo(seekBar.getProgress() * screen1Player.getDuration() / 100);
            }
        });

        airPlay = (ImageView) bottomPopView.findViewById(R.id.air_play);
        airPlay.setOnClickListener(new MyViewOnClickListener());

        playPre = (ImageView) bottomPopView.findViewById(R.id.play_pre);
        playPre.setOnClickListener(new MyViewOnClickListener());

        playNext = (ImageView) bottomPopView.findViewById(R.id.play_next);
        playNext.setOnClickListener(new MyViewOnClickListener());

        playOrPause = (ImageView) bottomPopView.findViewById(R.id.play_or_pause);
        playOrPause.setOnClickListener(new MyViewOnClickListener());

        controlSwitch = (ImageView) bottomPopView.findViewById(R.id.control_switch);
        if (isControlFirstScreen) {
            controlSwitch.setImageDrawable(getResources().getDrawable(R.drawable.control_1));
        } else {
            controlSwitch.setImageDrawable(getResources().getDrawable(R.drawable.control_2));
        }
        controlSwitch.setOnClickListener(new MyViewOnClickListener());

        videoControlPop = new PopupWindow(bottomPopView, WindowManager.LayoutParams.MATCH_PARENT,
                android.view.WindowManager.LayoutParams.WRAP_CONTENT);
        //  popupWindow.setAnimationStyle(R.style.popwin_anim_style);
        videoControlPop.showAtLocation(bottomPopView, Gravity.BOTTOM, 0, 0);
        videoControlPop.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                isVideoControlShowing = false;
            }
        });

        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                videoProgress.setProgress(screen1Player.getCurrentPosition() * 100 / screen1Player.getDuration());
                currentVideoTime = screen1Player.getCurrentPosition();
            }
        }, 0, 1000);
    }

    /**
     * Shows a {@link } on the specified display.
     */

    // 点击监听。
    class MyViewOnClickListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {

            if (v == airPlay) {
                Log.d(TAG, "airplay.........");
                if (!isAirplay) {

                    Intent i = new Intent(MainActivity.this, BackgroundPlayService.class);
                    startService(i);
                    isAirplay = true;
                    sharedPreferences.edit().putBoolean("isAirPlay", isAirplay).apply();
//                    screen1Player.setVolume(0,0);
                    isFirstAirPlay = false;
                    Log.d(TAG, "BackgroundPlayService.class");
                } else {
                    Log.d(TAG, "isAirplay = true");
                    Intent i = new Intent(SERVICE_ACTION);
                    i.putExtra("receiver_key", 0);
                    sendBroadcast(i);
                    isAirplay = false;
                    sharedPreferences.edit().putBoolean("isAirPlay", isAirplay).apply();
                    mHandler.sendEmptyMessage(CHANGE_CONTROL_PIC_1);
                    isControlFirstScreen = true;
                    screen1Player.setVolume(0.5f, 0.5f);
                }
            }

            if (v == playPre) {
                if (isControlFirstScreen) {
                    nowLvdsPosition--;
                    if (nowLvdsPosition < 0) {
                        nowLvdsPosition = lvdsVideoPath.size() - 1;
                    }
                    playVideo(screen1Player, nowLvdsPosition);
                } else if (isAirplay) {
                    Intent i = new Intent(SERVICE_ACTION);
                    i.putExtra("receiver_key", 1);
                    sendBroadcast(i);
                }
            }

            if (v == playNext) {
                if (isControlFirstScreen) {
                    nowLvdsPosition++;
                    if (nowLvdsPosition >= lvdsVideoPath.size()) {
                        nowLvdsPosition = 0;
                    }
                    playVideo(screen1Player, nowLvdsPosition);
                } else if (isAirplay) {
                    Intent i = new Intent(SERVICE_ACTION);
                    i.putExtra("receiver_key", 2);
                    sendBroadcast(i);
                }
            }

            if (v == playOrPause) {
                if (isControlFirstScreen) {
                    if (!isPause) {
                        screen1Player.pause();
                        isPause = true;
                        mHandler.sendEmptyMessage(CHANGE_PLAY_ICON);
                    } else {
                        screen1Player.start();
                        isPause = false;
                        mHandler.sendEmptyMessage(CHANGE_PAUSE_ICON);
                    }
                } else if (isAirplay) {
                    Intent i = new Intent(SERVICE_ACTION);
                    i.putExtra("receiver_key", 3);
                    sendBroadcast(i);
                }

            }

            if (v == controlSwitch && isAirplay) {
                Log.d(TAG, "v == controlSwitch && isAirplay");
                if (isControlFirstScreen) {
                    isControlFirstScreen = false;
                    mHandler.sendEmptyMessage(CHANGE_CONTROL_PIC_2);
                } else {
                    isControlFirstScreen = true;
                    mHandler.sendEmptyMessage(CHANGE_CONTROL_PIC_1);
                }
            }
        }
    }

    // 统一的播放界面。
    private void playVideo(MediaPlayer mediaPlayer, int pathIndex) {
        mediaPlayer.reset();
        mediaPlayer.setOnCompletionListener(new MyVideoFinishListener());
        try {
            if (lvdsVideoPath.size() > 0)
                mediaPlayer.setDataSource(lvdsVideoPath.get(pathIndex));
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            mediaPlayer.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            //  拔hdmi会出现异常。
        }
        mediaPlayer.start();
    }

    /**
     * Listens for displays to be added, changed or removed. We use it to update
     * the list and show a new {@link } when a display is connected.
     * <p/>
     * Note that we don't bother dismissing the {@link } when a
     * display is removed, although we could. The presentation API takes care of
     * doing that automatically for us.
     * <p/>
     * 显示设备接入监听。
     */
    private final DisplayManager.DisplayListener mDisplayListener = new DisplayManager.DisplayListener() {
        public void onDisplayAdded(int displayId) {
            // mHasTwoDispalys = true;
            // updateContents();
            Log.d(TAG, "onDisplayAdded");

        }

        public void onDisplayChanged(int displayId) {
//            updateContents();
            Log.d(TAG, "onDisplayChanged");
        }

        public void onDisplayRemoved(int displayId) {
            // mHasTwoDispalys = true;
            Log.d(TAG, "onDisplayRemoved");
            // updateContents();
        }
    };

    @Override
    protected void onStop() {
        super.onStop();
        // 停止时做一些资源的回收释放工作。
        if (mainSurface != null) mainSurface = null;

        if (timer != null) timer.cancel();

        if (videoControlPop != null && videoControlPop.isShowing()) {
            videoControlPop.dismiss();
            videoControlPop = null;
        }

        if (screen1Player != null) screen1Player.release();

        this.finish();
    }

    @Override
    protected void onDestroy() {
        Intent i = new Intent(SERVICE_ACTION);
        i.putExtra("receiver_key", 0);
        sendBroadcast(i);
        super.onDestroy();
    }
}
