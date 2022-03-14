package com.example.tictactoe;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class Main2Activity extends AppCompatActivity implements View.OnClickListener {
    private Button[][] board = new Button[3][3];
    TextView player1, cpu, round;
    Button btn_reset;
    Boolean p1turn, cpu_turn, gameOver;
    int count;
    int winCol, winRow, winMajDia, winMinDia;
    int bestRow, bestCol;
    int randRow, randCol;
    String brd[][] = new String [3][3];
    String winMarker;
    int rRow, rCol;

    int x_color, o_color, x_tint, o_tint, win_tint;
    int markerColor, turnColor;
    String activeMarker, nextTurn;

    Button btn_next;
    TextView scr1, scr2;
    int score1, score2;

    String p1Marker, cpuMarker;

    Handler setDelay;
    Runnable startDelay;


    String playerTurn, cpuTurn, drawMsg, pWinMsg, cpuWinMsg;
    MediaPlayer sfx_click, bgm, sfx_menuClick, sfx_next, sfx_reset;
    MediaPlayer sfx_win, sfx_lose, sfx_cpuMove;
    String level;

    HomeWatcher mHomeWatcher;
    int roundCount;
    String firstTurn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        count = 1;
        player1 = findViewById(R.id.txt_p1);
        cpu = findViewById(R.id.txt_cpu);
        round = findViewById(R.id.roundDet);

        setDelay = new Handler();

        p1Marker = "x";
        cpuMarker = "o";

        p1turn = cpu_turn = false;
        winCol = winRow = winMajDia = winMinDia = 0;
        bestCol = bestRow = 0;
        gameOver = false;
        randRow = 0;
        winMarker = "";
        randCol = 0;
        rRow = rCol = 0;

        x_color = getResources().getColor(R.color.blue_bright);
        o_color = getResources().getColor(R.color.red_dark);
        x_tint = getResources().getColor(R.color.blue_light);
        o_tint = getResources().getColor(R.color.red_light);
        activeMarker = winMarker = nextTurn = "";

        btn_next = findViewById(R.id.btn_next);
        scr1 = findViewById(R.id.txt_scoreP1);
        scr2 = findViewById(R.id.txt_scoreP2);

        final Intent intent = getIntent();
        level = intent.getStringExtra(Difficulty_screen.lvl_difficulty);

        score1 = score2 = 0;
        roundCount = 1;
        firstTurn = "";
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
                String btnId = "btn_" + i + j;
                int rId = getResources().getIdentifier(btnId, "id", getPackageName());
                board[i][j] = findViewById(rId);
                board[i][j].setOnClickListener(this);
            }
        }
        checkBoard();

        btn_reset = findViewById(R.id.btn_reset);
        btn_reset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sfx_reset.start();
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

        checkTurn();

        if (!btnIn.equals("") || gameOver || !p1turn) {
            return;
        } else {
            checkTurn();
            sfx_click.start();

            btnClicked.setTextColor(markerColor);
            btnClicked.setText(activeMarker);
            round.setText(nextTurn);
            count++;
            checkGameState();

            if (!gameOver) {
                Runnable r = new Runnable() {
                    public void run() {
                        cpuMove();
                    }
                };
                setDelay.postDelayed(r, 800);
            } else {
                declareWin();
            }
        }
    }

    public void getFirstMove() {
        if (roundCount%2 == 1) {
            count = 1;
            firstTurn = playerTurn;
            round.setText(playerTurn);
        } else {
            count = 0;
            firstTurn = cpuTurn;
            round.setText(cpuTurn);
            Runnable r = new Runnable() {
                public void run() {
                    cpuMove();
                }
            };
            setDelay.postDelayed(r, 800);
        }
    }

    public void checkTurn() {
        if (count%2 == 1) {
            player1();
            p1turn = true;
        }
        else {
            cpu();
            p1turn = false;
        }
    }

    public void cpuMove() {
        if (level.equalsIgnoreCase("easy")) {
            cpuMoveEasy();
        }
        else if (level.equalsIgnoreCase("medium")) {
            cpuMoveMedium();
        }
        else if (level.equalsIgnoreCase("hard")) {
            cpuMoveHard();
        }


        checkGameState();
        if (gameOver) declareWin();
        else {
            round.setText(nextTurn);
            count++;
        }
    }

    public void getSFX() {
        sfx_click = MediaPlayer.create(Main2Activity.this,R.raw.sfx_button_click);
        sfx_menuClick = MediaPlayer.create(Main2Activity.this,R.raw.sfx_button_menu);
        sfx_next = MediaPlayer.create(Main2Activity.this,R.raw.sfx_button_next);
        sfx_reset = MediaPlayer.create(Main2Activity.this,R.raw.sfx_button_reset);
        sfx_win = MediaPlayer.create(Main2Activity.this,R.raw.sfx_win1);
        sfx_lose = MediaPlayer.create(Main2Activity.this,R.raw.sfx_lose);
        sfx_cpuMove = MediaPlayer.create(Main2Activity.this,R.raw.sfx_cpu_move);
    }

    public void getStrings() {

        playerTurn = getString(R.string.playerTurn);
        cpuTurn = getString(R.string.cpuTurn);
        drawMsg = getString(R.string.drawMsg);
        pWinMsg = getString(R.string.pWinMsg);
        cpuWinMsg = getString(R.string.cpuWinMsg);
    }

    public void reset() {
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                board[i][j].setText("");
            }
        }
        randRow = randCol = 0;
        winCol = winRow = 0;
        bestCol = bestRow = 0;
        gameOver = false;
        round.setText(firstTurn);
        winMarker = "";

        getFirstMove();
    }

    public void resetAll() {
        reset();
        scr1.setText("0");
        scr2.setText("0");
    }

    public void declareWin() {
        if (winMarker.equals("x")) {
            playerWin();
        } else if(winMarker.equals("o")) {
            cpuWin();
        } else {
            draw();
        }

        btn_next.setVisibility(View.VISIBLE);
        btn_reset.setText("Reset All");
    }

    public void playerWin() {
        sfx_win.start();
        round.setText(pWinMsg);

        score1 = Integer.parseInt(scr1.getText().toString());
        score1++;
        scr1.setText(String.valueOf(score1));
    }

    public void cpuWin() {
        sfx_lose.start();
        round.setText(cpuWinMsg);
        score2 = Integer.parseInt(scr2.getText().toString());
        score2++;
        scr2.setText(String.valueOf(score2));
    }

    public void draw() {
        round.setText(drawMsg);
    }


    public void player1() {
        markerColor = x_color;
        activeMarker = "x";
        nextTurn = cpuTurn;
    }

    public void cpu() {
        markerColor = o_color;
        activeMarker = "o";
        nextTurn = playerTurn;
    }



    public void cpuMoveEasy() {
        sfx_cpuMove.start();
        checkBoard();
        checkTurn();

        int markRow, markCol;

        findRandomMove();
        markRow = randRow;
        markCol = randCol;
        board[markRow][markCol].setTextColor(markerColor);
        board[markRow][markCol].setText(activeMarker);

    }

    public void cpuMoveMedium() {
        sfx_cpuMove.start();
        checkBoard();
        checkTurn();

        int markRow, markCol;

        if (canWin(cpuMarker)) {
            markRow = bestRow;
            markCol = bestCol;
        } else {
            findRandomMove();
            markRow = randRow;
            markCol = randCol;
        }

        board[markRow][markCol].setTextColor(markerColor);
        board[markRow][markCol].setText(activeMarker);


    }

    public void cpuMoveHard() {
        sfx_cpuMove.start();
        checkBoard();
        checkTurn();

        int markRow, markCol;

        if (canWin(p1Marker)) {
            markRow = bestRow;
            markCol = bestCol;
        } else if (canWin(cpuMarker)) {
            markRow = bestRow;
            markCol = bestCol;
        }
        else {
            findRandomMove();
            markRow = randRow;
            markCol = randCol;
        }

        board[markRow][markCol].setTextColor(markerColor);
        board[markRow][markCol].setText(activeMarker);

    }



    public boolean canWin(String marker) {
        checkBoard();
        int oCount = 0;
        boolean findWinMove = false, findCol = false, findRow = false, findMajDia = false, findMinDia=false;

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if (brd[i][j].equals(marker)) {
                    oCount++;
                }
            }
            if (oCount == 2) {
                winRow = i;
                findRow = true;
                findWinMove = true;
                break;
            }
            oCount = 0;
        }

        oCount = 0;

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if (brd[j][i].equals(marker)) {
                    oCount++;
                }
            }
            if (oCount == 2) {
                winCol = i;
                findCol = true;
                findWinMove = true;
                break;
            }
            oCount = 0;
        }

        oCount = 0;

        int length = brd.length;

        int x;
        for (x = 0; x < length; x++) {
            if (brd[x][x].equalsIgnoreCase(marker)) {
                oCount++;
            }
        }
        if (oCount == 2) {
            findMajDia = true;
            findWinMove = true;
        }
        oCount=0;

        int j = length-1;
        for (x = 0; x < length; x++) {
            if (brd[x][j].equalsIgnoreCase(marker)) {
                oCount++;
            }
            j--;
        }
        if (oCount == 2) {
            findMinDia = true;
            findWinMove = true;
        }



        boolean winPossible = false;
        if (findWinMove) {
            if (findRow) {
                checkBoard();
                for (int i = 0; i < 3; i++) {
                    if (brd[winRow][i].equals("")) {
                        bestRow = winRow;
                        bestCol = i;
                        winPossible = true;
                        break;
                    }
                }
            } else if (findCol) {
                checkBoard();
                for (int i = 0; i < 3; i++) {
                    if (brd[i][winCol].equals("")) {
                        bestRow = i;
                        bestCol = winCol;
                        winPossible = true;
                        break;
                    }
                }
            } else if (findMajDia) {
                checkBoard();
                for (int i = 0; i < length; i++) {
                    if (brd[i][i].equalsIgnoreCase("")) {
                        bestRow = bestCol = i;
                        winPossible = true;
                        break;
                    }
                }
            } else if (findMinDia) {
                checkBoard();
                int col = length-1;
                for (int i = 0; i < length; i++) {

                    if (brd[i][col].equalsIgnoreCase("")) {
                        bestRow = i;
                        bestCol = col;
                        winPossible = true;
                        break;
                    }
                    col--;
                }
            }
        } else {
            winPossible = false;
        }
        return winPossible;
    }



    public void findRandomMove() {
        checkBoard();
        rRow = (int) (Math.round(Math.random() * 2));
        rCol = (int) (Math.round(Math.random() * 2));



        if (brd[rRow][rCol].equalsIgnoreCase("")) {
            randRow = rRow;
            randCol = rCol;
        } else {
            findRandomMove();
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
                break;
            }
        }

        for (int j = 0; j < 3; j++) {
            if (brd[0][j].equals(brd[1][j]) && brd[1][j].equals(brd[2][j])
                    && !brd[0][j].equals("")) {
                win = true;
                winMarker = brd[0][j];
                break;
            }
        }

        if (brd[0][0].equals(brd[1][1]) && brd[1][1].equals(brd[2][2])
                && !brd[0][0].equals("")) {
            win = true;
            winMarker = brd[0][0];
        }

        if (brd[0][2].equals(brd[1][1]) && brd[1][1].equals(brd[2][0])
                && !brd[0][2].equals("")) {
            win = true;
            winMarker = brd[0][2];
        }


        boolean full = true;
        checkBoard();
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if(!brd[i][j].equals("")) {
                    full = true;
                } else {
                    full = false;
                    break;
                }
            }
            if (!full) break;
        }

        if (full && !gameOver && !win) {
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
