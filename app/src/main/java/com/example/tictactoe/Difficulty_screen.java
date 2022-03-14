package com.example.tictactoe;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.IBinder;
import android.os.PowerManager;
import android.view.View;
import android.widget.Button;

public class Difficulty_screen extends AppCompatActivity implements View.OnClickListener {
    Button btn_easy, btn_medium, btn_hard;
    MediaPlayer bgm, sfx_menuClick;

    public final static String lvl_difficulty = "com.example.tictactoe.tictactoe.lvl_difficulty";
    String level;

    HomeWatcher mHomeWatcher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_difficulty_screen);
        level = "";

        doBindService();
        Intent music = new Intent();
        music.setClass(Difficulty_screen.this, MusicService.class);
        startService(music);


        mHomeWatcher = new HomeWatcher(Difficulty_screen.this);
        mHomeWatcher.setOnHomePressedListener(new HomeWatcher.OnHomePressedListener() {
            @Override
            public void onHomePressed() {
                if (mServ != null) {
                    mServ.pauseMusic();
                }
            }
            @Override
            public void onHomeLongPressed() {
                if (mServ != null) {
                    mServ.pauseMusic();
                }
            }
        });
        mHomeWatcher.startWatch();

        getSFX();

        btn_easy = findViewById(R.id.btn_easy);
        btn_medium = findViewById(R.id.btn_medium);
        btn_hard = findViewById(R.id.btn_hard);

        btn_easy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                level = "easy";
                setDifficulty();
            }
        });

        btn_medium.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                level = "medium";
                setDifficulty();
            }
        });

        btn_hard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                level = "hard";
                setDifficulty();
            }
        });

    }

    private boolean mIsBound = false;
    private MusicService mServ;
    private ServiceConnection Scon = new ServiceConnection(){

        public void onServiceConnected(ComponentName name, IBinder
                binder) {
            mServ = ((MusicService.ServiceBinder)binder).getService();
        }

        public void onServiceDisconnected(ComponentName name) {
            mServ = null;
        }
    };

    void doBindService(){
        bindService(new Intent(Difficulty_screen.this,MusicService.class),
                Scon,Context.BIND_AUTO_CREATE);
        mIsBound = true;
    }

    void doUnbindService()
    {
        if(mIsBound)
        {
            unbindService(Scon);
            mIsBound = false;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (mServ != null) {
            mServ.resumeMusic();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        PowerManager pm = (PowerManager)
                getSystemService(Context.POWER_SERVICE);
        boolean isScreenOn = false;
        if (pm != null) {
            isScreenOn = pm.isInteractive();
        }

        if (!isScreenOn) {
            if (mServ != null) {
                mServ.pauseMusic();
            }
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();

        doUnbindService();
        Intent music = new Intent();
        music.setClass(Difficulty_screen.this,MusicService.class);
        stopService(music);
        mHomeWatcher.stopWatch();

    }



    public void setDifficulty() {

        Intent intent = new Intent(Difficulty_screen.this, Main2Activity.class);
        intent.putExtra(lvl_difficulty, level);
        startActivity(intent);

    }

    public void getSFX() {
        sfx_menuClick = MediaPlayer.create(Difficulty_screen.this,R.raw.sfx_button_menu);
    }


    @Override
    public void onClick(View v) {
        sfx_menuClick.start();
    }
}
