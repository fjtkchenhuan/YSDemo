package com.ys.adjustvolume;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Service;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ToggleButton;

public class MainActivity extends AppCompatActivity {

    private Button btnPlay = null, btnUpper = null, btnLower = null;
    private ToggleButton tbMute = null;
    private MediaPlayer mediaPlayer = null; //声频
    private AudioManager audioManager = null; //音频

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        audioManager = (AudioManager) getSystemService(Service.AUDIO_SERVICE);
        btnPlay = (Button) findViewById(R.id.btnPlay);
        btnUpper = (Button) findViewById(R.id.btnUpper);
        btnLower = (Button) findViewById(R.id.btnLower);
        btnPlay.setOnClickListener(listener);
        btnUpper.setOnClickListener(listener);
        btnLower.setOnClickListener(listener);
        tbMute = (ToggleButton) findViewById(R.id.tbMute);
        tbMute.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                audioManager.setStreamMute(AudioManager.STREAM_MUSIC, !isChecked); //设置是否静音
            }
        });
    }

    View.OnClickListener listener = new View.OnClickListener() {
        public void onClick(View v) {
            @SuppressWarnings("unused")
            Button btn = (Button) v;
            switch (v.getId()) {
                case R.id.btnPlay://播放音乐
                    mediaPlayer = MediaPlayer.create(MainActivity.this, R.raw.ap);
                    mediaPlayer.setLooping(true);//设置循环播放
                    mediaPlayer.start();//播放声音
                    break;
                case R.id.btnUpper:
                    //adjustStreamVolume: 调整指定声音类型的音量
                    audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC,
                            AudioManager.ADJUST_RAISE,
                            AudioManager.FLAG_VIBRATE);
                    break;
                case R.id.btnLower:
                    //第一个参数：声音类型
                    //第二个参数：调整音量的方向
                    //第三个参数：可选的标志位
                    audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC,
                            AudioManager.ADJUST_LOWER,
                            AudioManager.FLAG_VIBRATE);
                    break;
            }
        }
    };
}
