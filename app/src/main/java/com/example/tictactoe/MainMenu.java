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

public class MainMenu extends AppCompatActivity {
    Button btn_1p, btn_2p;

    MediaPlayer bgm, sfx_menuClick;
    HomeWatcher mHomeWatcher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);

        doBindService();
        Intent music = new Intent();
        music.setClass(MainMenu.this, MusicService.class);
        startService(music);

        mHomeWatcher = new HomeWatcher(MainMenu.this);
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

        btn_1p = findViewById(R.id.btn_1p);
        btn_2p = findViewById(R.id.btn_2p);
        getSFX();


        btn_1p.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sfx_menuClick.start();
                startActivity(new Intent(MainMenu.this, Difficulty_screen.class));
            }
        });

        btn_2p.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sfx_menuClick.start();
                startActivity(new Intent(MainMenu.this, MainActivity.class));
            }
        });
    }

    public void getSFX() {
        sfx_menuClick = MediaPlayer.create(this,R.raw.sfx_button_menu);
    }

    private boolean mIsBound = false;
    private MusicService mServ;
    private ServiceConnection Scon =new ServiceConnection(){

        public void onServiceConnected(ComponentName name, IBinder
                binder) {
            mServ = ((MusicService.ServiceBinder)binder).getService();
        }

        public void onServiceDisconnected(ComponentName name) {
            mServ = null;
        }
    };

    void doBindService(){
        bindService(new Intent(this,MusicService.class),
                Scon, Context.BIND_AUTO_CREATE);
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
        music.setClass(this,MusicService.class);
        stopService(music);
        mHomeWatcher.stopWatch();

    }

}
