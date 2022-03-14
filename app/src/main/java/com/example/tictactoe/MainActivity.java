package com.example.tictactoe;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.IBinder;
import android.os.PowerManager;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private Button[][] board = new Button[3][3];
    TextView player1, player2, round;
    Button btn_reset;
    Boolean p1turn, p2turn, p1win, p2win, gameOver;
    int count;
    int x_color, o_color, x_tint, o_tint, win_tint;
    int markerColor, turnColor;
    String activeMarker, winMarker, nextTurn;
    String brd[][] = new String[3][3];
    String winBlocks[][] = new String[3][3];

    Button btn_next;
    TextView scr1, scr2;
    int score1, score2;
    int roundCount;
    String firstTurn;

    String p1_turn, p2_turn, drawMsg, p1WinMsg, p2WinMsg;

    MediaPlayer sfx_p1move, bgm, sfx_menuClick, sfx_next, sfx_reset;
    MediaPlayer sfx_win, sfx_lose, sfx_p2move;
    HomeWatcher mHomeWatcher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        count = 1;
        player1 = findViewById(R.id.txt_p1);
        player2 = findViewById(R.id.txt_p2);
        round = findViewById(R.id.roundDet);
        x_color = getResources().getColor(R.color.blue_bright);
        o_color = getResources().getColor(R.color.red_dark);
        x_tint = getResources().getColor(R.color.blue_light);
        o_tint = getResources().getColor(R.color.red_light);

        p1turn = p2turn = p1win = p2win = false;
        gameOver = false;
        activeMarker = winMarker = nextTurn = "";

        btn_next = findViewById(R.id.btn_next);
        scr1 = findViewById(R.id.txt_scoreP1);
        scr2 = findViewById(R.id.txt_scoreP2);

        score1 = score2 = 0;
        roundCount = 1;
        firstTurn = p1_turn;

        getStrings();
        getSFX();

        doBindService();
        Intent music = new Intent();
        music.setClass(this, MusicService.class);
        startService(music);

        mHomeWatcher = new HomeWatcher(this);
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


        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                winBlocks[i][j] = "";
            }
        }


        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                String btnId = "btn_" + i + j;
                int rId = getResources().getIdentifier(btnId, "id", getPackageName());
                board[i][j] = findViewById(rId);
                board[i][j].setOnClickListener(this);
            }
        }

        btn_reset = findViewById(R.id.btn_reset);
        btn_reset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sfx_reset.start();
                btn_reset.setText("Reset");
                if (btn_next.getVisibility() == View.VISIBLE) {
                    roundCount = 1;
                    resetAll();
                    btn_reset.setText("Reset");
                }
                else {
                    reset();
                }
                btn_next.setVisibility(View.INVISIBLE);

            }
        });

        btn_next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               sfx_next.start();
               roundCount++;
               reset();
               btn_next.setVisibility(View.INVISIBLE);
               btn_reset.setText("Reset");
            }
        });

    }

    @Override
    public void onClick(View v) {

        Button btnClicked = ((Button)v);
        String btnIn = (btnClicked.getText().toString());

        if (!btnIn.equals("") || gameOver) {
            return;
        } else {
            checkTurn();
            count++;

            btnClicked.setTextColor(markerColor);
            btnClicked.setText(activeMarker);
            round.setText(nextTurn);

            checkGameState();
            if (gameOver) {
                declareWin();
            }
        }
    }

    public void getSFX() {
        sfx_menuClick = MediaPlayer.create(MainActivity.this,R.raw.sfx_button_menu);
        sfx_next = MediaPlayer.create(MainActivity.this,R.raw.sfx_button_next);
        sfx_reset = MediaPlayer.create(MainActivity.this,R.raw.sfx_button_reset);
        sfx_p1move = MediaPlayer.create(MainActivity.this,R.raw.sfx_button_click);
        sfx_p2move = MediaPlayer.create(MainActivity.this,R.raw.sfx_cpu_move);

        sfx_win = MediaPlayer.create(MainActivity.this,R.raw.sfx_win2);
        sfx_lose = MediaPlayer.create(MainActivity.this,R.raw.sfx_lose);
    }

    public void getStrings() {
        p1_turn = getString(R.string.p1_turn);
        p2_turn = getString(R.string.p2_turn);
        drawMsg = getString(R.string.drawMsg);
        p1WinMsg = getString(R.string.p1WinMsg);
        p2WinMsg = getString(R.string.p2WinMsg);
    }

    public void reset() {

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                board[i][j].setText("");
            }
        }

        gameOver = false;
        getFirstMove();
        round.setText(firstTurn);
        checkBoard();
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                winBlocks[i][j] = "";
            }
        }
    }

    public void resetAll() {
        reset();
        scr1.setText("0");
        scr2.setText("0");
    }

    public void declareWin() {
        if (winMarker.equals("x")) {
            player1Win();
        } else if(winMarker.equals("o")) {
            player2Win();
        } else {
            draw();
        }
        changeTint();

        btn_next.setVisibility(View.VISIBLE);
        btn_reset.setText("Reset All");
    }

    public void player1Win() {
        sfx_win.start();
        round.setText(p1WinMsg);
        win_tint = x_tint;
        score1 = Integer.parseInt(scr1.getText().toString());
        score1++;
        scr1.setText(String.valueOf(score1));
    }

    public void player2Win() {
        sfx_win.start();
        round.setText(p2WinMsg);
        win_tint = x_tint;
        score2 = Integer.parseInt(scr2.getText().toString());
        score2++;
        scr2.setText(String.valueOf(score2));
    }

    public void draw() {

        round.setText(drawMsg);
    }

    public void changeTint() {
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if (winBlocks.equals("tint")) {
                    board[i][j].setBackgroundColor(win_tint);
                }
            }
        }
    }

    public void player1() {
        markerColor = x_color;
        activeMarker = "x";
        nextTurn = p2_turn;
    }

    public void player2() {
        markerColor = o_color;
        activeMarker = "o";
        nextTurn = p1_turn;
    }

    public void getFirstMove() {
        if (roundCount%2 == 1) {
            count = 1;
            firstTurn = p1_turn;
        } else {
            count = 0;
            firstTurn = p2_turn;
        }
    }

    public void checkTurn() {
        if (count%2 == 1) {
            sfx_p1move.start();
            player1();
        }
        else {
            sfx_p2move.start();
            player2();
        }

    }

    public void checkBoard() {
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                brd[i][j] = board[i][j].getText().toString();
            }
        }

    }

    public void checkGameState() {
        Boolean win = false, draw = false;
        checkBoard();

        for (int i = 0; i < 3; i++) {
            if (brd[i][0].equals(brd[i][1]) && brd[i][1].equals(brd[i][2])
                    && !brd[i][0].equals("")) {
                win = true;
                winMarker = brd[i][0];
                winBlocks[i][0] = "tint";
                winBlocks[i][1] = "tint";
                winBlocks[i][2] = "tint";
                break;
            }
        }

        for (int j = 0; j < 3; j++) {
            if (brd[0][j].equals(brd[1][j]) && brd[1][j].equals(brd[2][j])
                    && !brd[0][j].equals("")) {
                win = true;
                winMarker = brd[0][j];
                winBlocks[0][j] = "tint";
                winBlocks[1][j] = "tint";
                winBlocks[2][j] = "tint";
                break;
            }
        }

        if (brd[0][0].equals(brd[1][1]) && brd[1][1].equals(brd[2][2])
                && !brd[0][0].equals("")) {
            win = true;
            winMarker = brd[0][0];
            winBlocks[0][0] = "tint";
            winBlocks[1][1] = "tint";
            winBlocks[2][2] = "tint";
        }

        if (brd[0][2].equals(brd[1][1]) && brd[1][1].equals(brd[2][0])
                && !brd[0][2].equals("")) {
            win = true;
            winMarker = brd[0][2];
            winBlocks[0][2] = "tint";
            winBlocks[1][1] = "tint";
            winBlocks[2][0] = "tint";
        }

        if (count == 10 && !gameOver && !win) {
            draw = true;
        }


        if (win || draw) {
            gameOver = true;
            if (draw) {
                winMarker = "d";
            }
        }
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
        music.setClass(this,MusicService.class);
        stopService(music);
        mHomeWatcher.stopWatch();

    }
}
