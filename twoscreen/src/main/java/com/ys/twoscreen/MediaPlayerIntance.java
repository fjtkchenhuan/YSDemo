package com.ys.twoscreen;

import android.media.MediaPlayer;

/**
 * Created by Administrator on 2018/6/8.
 */

public class MediaPlayerIntance {
    private static MediaPlayer singleton;

    public synchronized static MediaPlayer newInstance() {
        if (singleton== null) {
            singleton= new MediaPlayer();
        }
        return singleton;
    }


}
