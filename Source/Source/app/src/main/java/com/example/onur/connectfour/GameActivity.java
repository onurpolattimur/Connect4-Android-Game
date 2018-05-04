package com.example.onur.connectfour;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Date;
import java.util.Random;
import java.util.Vector;



/**
 * Created by Onur on 6.02.2018.
 */

public class GameActivity extends AppCompatActivity {

    enum _cellType {
        empty, user1, user2, computer, used1, used2, pressed
    }

    //Gui Variables
    TextView textTurn1;
    TextView textTurn2;
    ImageView imgTurn1;
    ImageView imgTurn2;
    TextView timeText;
    GridLayout cellGrid;
    Context context;
    private int imageSize;
    boolean onAnim = false;
    LinearLayout wait;

    //Game Members
    private Vector<Integer> undoMoves;
    private CountDownTimer timer;
    private int assignTimer;
    private int time;
    private int activeTime;
    private int scoreForP1;
    private int scoreForP2;
    private int turn;
    private int gameMode = 1;
    private int size = 6;
    private int gameEnd = 0;
    Cell[][] cells;
    private Board board;
    protected int score = 100000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        //Tam ekran yapmak için gerekli işlemler.
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.game_activity);

        imageSize = 0;
        turn = 0;
        gameMode = 2;
        size = 6;
        gameEnd = 0;
        scoreForP1 = 0;
        scoreForP2 = 0;
        onAnim = false;
        time = 6;

        Intent intent = getIntent();
        size = intent.getIntExtra("Size", 6);
        time = intent.getIntExtra("Time", 6);
        gameMode = intent.getIntExtra("GameMode", 1);

        context = this;
        cellGrid = findViewById(R.id.cellGrid);
        textTurn1 = findViewById(R.id.textTurn1);
        textTurn2 = findViewById(R.id.textTurn2);
        imgTurn1 = findViewById(R.id.headerTurn1);
        imgTurn2 = findViewById(R.id.headerTurn2);
        timeText = findViewById(R.id.timeText);

        init();

    }

    /** MainActivityden gelen oyun bilgilerine göre board'ın oluşturulması gibi işlemleri gerçekler.
     *
     */
    @SuppressLint("ClickableViewAccessibility")
    private void init() {

        if (time > 0)
            timeText.setText(String.valueOf(time));

        //Size geldi, boardı ayarla.
        cells = new Cell[size][size];
        cellGrid.setRowCount(size);
        cellGrid.setColumnCount(size);
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                cells[i][j] = new Cell(i, j, _cellType.empty);
                TouchHandle touchHandle = new TouchHandle();
                cells[i][j].cellImage.setOnTouchListener(touchHandle);
                cellGrid.addView(cells[i][j].cellImage);
                //cells[i][j].updateCellImg();
            }
        }

        //Timer ayarla
        if (time > 0) {
            activeTime = time;
            timer = new CountDownTimer((time + 2) * 1000, 1000) {
                @Override
                public void onTick(long l) {
                    if (activeTime == 0) {
                        timer.onFinish();
                    }
                    timeText.setText(String.valueOf(activeTime));
                    activeTime--;

                }

                @Override
                public void onFinish() {
                    Toast.makeText(context, "Time Expired!", Toast.LENGTH_SHORT).show();
                    int col;
                    do {
                        Random rand = new Random();
                        col = rand.nextInt(size);
                    } while (!isLegal(col));
                    play(col);
                }
            };
        }

        undoMoves = new Vector<>();
        board = new Board(this, cells, 0);
        wait = findViewById(R.id.wait);

    }

    /**
     * Pause butonuna tıklandığında customdialog çıkmasını sağlar ve oyunu durdurur.
     */
    public void pauseClick(View view) {
        if (gameMode == 2 || (gameMode == 1 && turn == 0)) {
            if (time > 0 && isGameEnd() == 0) {
                if (assignTimer == 1) timer.cancel();
            }
        }
        PauseDialog cdd = new PauseDialog(GameActivity.this);
        cdd.show();
    }

    /**
     * Yapılan hareketleri geri alır, eğer geri alınan hareket bilgisayara ait ise yeni bir hamle üretilir.
     * @param view
     */
    public void undoClick(View view) {
        if (isGameEnd() == 0 && wait.getVisibility()!=View.VISIBLE) {
            if (undoMoves.size() > 0) {
                if (time > 0 && assignTimer == 1) {
                    timer.cancel();
                    activeTime = time;
                    timer.start();
                }

                setTurn();
                int col = undoMoves.remove(undoMoves.size() - 1);
                _cellType myType = turn == 0 ? _cellType.user1 : _cellType.user2;
                for (int i = 0; i < size; i++) {
                    if (cells[i][col].getType() == myType) {
                        cells[i][col].setType(_cellType.empty);
                        cells[i][col].updateCellImg();
                        break;
                    }
                }
                if (gameMode == 1 && turn == 1 && isGameEnd() == 0) {
                    for(int i=0;i<getSize();i++){
                        for(int j=0;j<getSize();j++){
                            if(cells[i][j].getType()== _cellType.empty)
                                board.getField()[i][j] = -1;
                            else if(cells[i][j].getType()== _cellType.user1)
                                board.getField()[i][j] = 0;
                            else if(cells[i][j].getType()== _cellType.user2)
                                board.getField()[i][j] = 1;
                        }
                    }
                    board.player = board.player == 0 ? 1: 0;
                    wait.setVisibility(View.VISIBLE);
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            playComputer();
                        }
                    }, 400);

                }
            }
        }
    }

    /**
     * Board size'ı return eder.
     */
    public int getSize() {
        return size;
    }

    /**
     * Skor labellerini günceller.
     */
    private void updateScore() {
        textTurn1.setText(String.format("%s", scoreForP1));
        textTurn2.setText(String.format("%s", scoreForP2));

    }

    /**
     * Header kısmında bulunan görselleri oyuncu sırası kimdeyse ona uygun olarak günceller.
     */
    private void setTurn() {
        if (turn == 0) {
            turn = 1;
            imgTurn2.setImageResource(R.drawable.turn2);
            imgTurn1.setImageResource(R.drawable.p1header);
        } else {
            turn = 0;
            imgTurn1.setImageResource(R.drawable.turn1);
            imgTurn2.setImageResource(R.drawable.p2header);
        }
    }

    /**
     * Bu fonksiyon bilgisayarın akıllı hamle yapabilmesi için kullanıldı.
     * Algoritma olarak Alpha Beta Pruning içerir.
     * Board sınıfı yalnızca board'ı data olarak tutar, görsel olarak herhangi bir işlem gerçeklemez.
     * minimizePlay fonksiyonu ile birbirlerini çağırırlar.
     * Temel mantık, her bir kolonu oyun sırasına göre doldurur ve kazanan bir hamle bulunmayanı seçmeye çalışılır.
     *
     * @param board
     * @param depth Akıllı hamle aranırken kaç satır boyunca arama yapılacağını belirtir. (Derinlik)
     * @param alpha Maksimum skor.
     * @param beta  Minimum skor.
     * @return akıllı olan hamlenin skorunu ve kolon numarasını bir dizi olarak döndürür.
     */
    public int[] maximizePlay(Board board, int depth, int alpha, int beta) {
        int score = board.score();

        if (board.isFinished(depth, score)) {
            int retVal[] = new int[2];
            retVal[0] = -1;
            retVal[1] = score;
            return retVal;
        }

        int max[] = new int[2];
        max[0] = -1;
        max[1] = -99999;

        for (int column = 0; column < size; column++) {
            Board new_board = board.copy(); // Create new board
            if (new_board.place(column)) {

                int[] next_move = minimizePlay(new_board, depth - 1, alpha, beta);

                if (max[0] == -1 || next_move[1] > max[1]) {
                    max[0] = column;
                    max[1] = next_move[1];
                    alpha = next_move[1];
                }

                if (alpha >= beta) return max;
            }
        }

        return max;
    }

    public int[] minimizePlay(Board board, int depth, int alpha, int beta) {
        int score = board.score();

        if (board.isFinished(depth, score)) {
            int retVal[] = new int[2];
            retVal[0] = -1;
            retVal[1] = score;
            return retVal;
        }

        // Column, score
        int min[] = new int[2];
        min[0] = -1;
        min[1] = 99999;

        for (int column = 0; column < size; column++) {
            Board new_board = board.copy();

            if (new_board.place(column)) {
                int[] next_move = maximizePlay(new_board, depth - 1, alpha, beta);
                if (min[0] == -1 || next_move[1] < min[1]) {
                    min[0] = column;
                    min[1] = next_move[1];
                    beta = next_move[1];
                }

                if (alpha >= beta) return min;
            }
        }
        return min;
    }

    /**
     * Bilgisayar için hamle seçer ve oynar.
     */
    private void playComputer() {

        int col = selectForComputer();
        play(col);
    }

    /**
     * Bilgisayarın hamlesini seçmesi için kullanılır.
     * İlk önce bilgisayarın hamle yapınca kazanacağı yer var mı onu kontrol eder, eğer yoksa oyuncunun kazanacağı bir yer var mı onu kontrol eder.
     * Eğer bu iki koşulda da uygun bir hamle yoksa Alpha Beta Pruning algoritmasını kullanan maximizePlay fonksiyonunu çağırır.
     * Dikkat edilmesi gereken nokta şudur ki, bu algoritma büyük board size'a sahip oyunlarda hamle seçerken uzun süre geçmektedir.
     * @return seçilen hamle.
     */
    private int selectForComputer() {
        //Bilgisayar hamle yapınca kazanabileceği yer var mı kontrol et.
        int col=-1;
        for (int i = 0; i < getSize(); i++) {
            for (int j = getSize() - 1; j >= 0; j--) {
                if (cells[j][i].getType() == _cellType.empty) {
                    cells[j][i].setType(_cellType.user2);
                    if (checkWin(_cellType.user2, 1) == 1) {
                        col = i;
                    }
                    cells[j][i].setType(_cellType.empty);
                    break;
                }
            }
        }
        if (col != -1) return col;
        /*
        Oyuncu hamle yapınca kazanabileceği yer var mı kontrol et.
        */
        for (int i = 0; i < getSize(); i++) {
            for (int j = getSize() - 1; j >= 0; j--) {
                if (cells[j][i].getType() == _cellType.empty) {
                    cells[j][i].setType(_cellType.user1);
                    if (checkWin(_cellType.user1, 1) == 1) {
                        col = i;
                    }
                    cells[j][i].setType(_cellType.empty);
                    break;
                }
            }
        }
        if (col != -1) return col;
        //Kasmaya sebep olacağından dolayı sınırlandırılmıştır.
        if(getSize()<10) {
            int colNegamax[] = maximizePlay(board, 4, Integer.MIN_VALUE, Integer.MAX_VALUE);
            col = colNegamax[0];
        }
        if(col!=-1) return col;
        //Eğer -1 dönerse tüm bunlara rağmen, random bir şeyler gönder.
        do {
            Random rand = new Random();
            col = rand.nextInt(size);
        } while (!isLegal(col));
        return col;

    }

    /**
     * Board üzerinde değişiklikleri yapar ve animasyonu başlatır.
     * Animasyon bittiği zaman bir kazanan olup olmadığını kontrol eder ve gösterir.
     * Eğer bilgisayar ile oynanıyorsa bilgisayar için hamle seçer ve oynar.
     * @param colNum oynanacak kolon numarasını belirtir.
     */
    private void play(int colNum) {
        /*Bu fonksiyon hamleyi yapar ve  oyunun bitip bitmediğini kontrol eder.*/
        if (time > 0) timer.cancel();
        final ImageView tempImage;
        for (int i = size - 1; i >= 0; i--) {
            if (cells[i][colNum].getType() == _cellType.empty) {
                if (gameMode == 1) {
                    if (!board.place(colNum)) {
                        throw new RuntimeException("Kritik bir hata oluştu");
                    }
                }
                cells[i][colNum].setType(turn == 0 ? _cellType.user1 : _cellType.user2);
                //Animasyon için geçici ImageView'in oluşturulması.
                tempImage = new ImageView(context);
                GridLayout.LayoutParams param = new GridLayout.LayoutParams();
                param.height = GridLayout.LayoutParams.WRAP_CONTENT;
                param.width = GridLayout.LayoutParams.WRAP_CONTENT;
                tempImage.setLayoutParams(param);
                tempImage.setImageResource(turn == 0 ? R.drawable.user1 : R.drawable.user2);

                cellGrid.addView(tempImage, new GridLayout.LayoutParams(GridLayout.spec(0, GridLayout.CENTER), GridLayout.spec(colNum, GridLayout.CENTER)));
                tempImage.setAdjustViewBounds(true);
                tempImage.getLayoutParams().width = imageSize;

                int toY = cells[i][colNum].cellImage.getTop();
                int fromY = cells[0][colNum].cellImage.getTop();
                TranslateAnimation moveAnim = new TranslateAnimation(0, 0, 0, toY - fromY);
                moveAnim.setDuration(250);
                moveAnim.setFillAfter(true);

                final int row = i;
                final int col = colNum;
                final _cellType animTurn = turn == 0 ? _cellType.user1 : _cellType.user2;
                moveAnim.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {
                        onAnim = true;

                    }

                    @Override
                    public void onAnimationEnd(Animation arg0) {
                        if(wait.getVisibility()== View.VISIBLE) wait.setVisibility(View.GONE);
                        Date date = new Date();
                        boolean boardFull = isBoardFull();
                        onAnim = false;
                        cells[row][col].setImg(animTurn);
                        cellGrid.removeViewInLayout(tempImage);
                        undoMoves.add(col);
                        _cellType moveType = turn == 1 ? _cellType.user1 : _cellType.user2;
                        if (checkWin(moveType, 0) == 1) {
                            setTurn();
                            gameEnd = 1;
                            String won = "";
                            if (gameMode == 1 && turn == 1) won = "Computer";
                            if (gameMode == 2 && turn == 1) won = "Player 2";
                            if (turn == 0) won = "Player 1";
                            if (turn == 0) scoreForP1++;
                            else scoreForP2++;
                            updateScore();
                            gameEnd = 1;
                            String winText = won + " won! \nThe game will be restarted in 3 seconds.";
                            Toast winMessage = Toast.makeText(context, winText, Toast.LENGTH_LONG);
                            LinearLayout layout = (LinearLayout) winMessage.getView();
                            if (layout.getChildCount() > 0) {
                                TextView tv = (TextView) layout.getChildAt(0);
                                tv.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL);
                            }
                            winMessage.setGravity(Gravity.CENTER, 0, 0);
                            winMessage.show();

                            new CountDownTimer(3 * 1000, 1000) {
                                @Override
                                public void onTick(long l) {

                                }

                                @Override
                                public void onFinish() {
                                    gameReset();
                                }
                            }.start();
                        } else {

                            if (gameMode == 1 && turn == 1 && isGameEnd() == 0 && !boardFull) {
                                final waitDialogClass cdd = new waitDialogClass(GameActivity.this);
                                wait.setVisibility(View.VISIBLE);
                                new Handler().postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        playComputer();
                                    }
                                }, 200);
                            }
                            if (gameMode == 2 || (gameMode == 1 && turn == 0)) {
                                if (time > 0 && isGameEnd() == 0) {
                                    if (assignTimer == 1) {
                                        timer.cancel();
                                    } else {
                                        assignTimer = 1;
                                    }
                                    activeTime = time;
                                    timer.start();
                                }
                            }

                        }
                        //Eğer board dolduysa oyunu sıfırla başa al.
                        if (boardFull) {
                            Toast.makeText(context, "Board was restarted due to not exist empty cell.", Toast.LENGTH_SHORT).show();
                            gameReset();
                        }

                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {

                    }
                });
                tempImage.startAnimation(moveAnim);
                setTurn();

                break;
            }
        }


    }

    /**
     * Board'ın dolup dolmadığını kontrol eder
     */
    public boolean isBoardFull() {
        int i, j;
        for (i = 0; i < size; i++) {
            for (j = 0; j < size; j++) {
                if (cells[i][j].getType() == _cellType.empty) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Bir kazanan olduktan sonra oyunun hücrelerini, gameEnd ve turn değişkenlerini sıfırlar.
     */
    private void gameReset() {
        if (time > 0 && assignTimer == 1) {
            timer.cancel();
            activeTime = time;
            timeText.setText(String.valueOf(time));
        }
        gameEnd = 0;
        turn = 0;
        imgTurn1.setImageResource(R.drawable.turn1);
        imgTurn2.setImageResource(R.drawable.p2header);

        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                cells[i][j].setType(_cellType.empty);
                cells[i][j].updateCellImg();
            }
        }
        undoMoves.removeAllElements();

        board = new Board(this, cells, 0);

    }

    /**
     * Gelen yapılan hareket tipine göre bir kazanan olup  olmadığını kontrol eder.
     * Bu fonksiyon aynı zaman forSelect parametresi ile selectForComputer fonksiyonu için de kullanılmaktadır.
     */
    private int checkWin(_cellType moveType, int forSelect) {
        // Yatay Kontrol
        for (int j = 0; j < getSize() - 3; j++) {
            for (int i = 0; i < getSize(); i++) {
                int thereis4 = 1;
                for (int k = 0; k < 4; k++) {
                    if (cells[i][j + k].getType() != moveType) {
                        thereis4 = 0;
                        break;
                    }
                }
                if (forSelect == 1 && thereis4 == 1) return 1;
                if (thereis4 == 1) {
                    for (int k = 0; k < 4; k++) {
                        cells[i][j + k].setType(moveType == _cellType.user1 ? _cellType.used1 : _cellType.used2);
                        cells[i][j + k].updateCellImg();
                    }
                    return 1;
                }
            }
        }
        // Dikey Kontrol
        for (int i = 0; i < getSize() - 3; i++) {
            for (int j = 0; j < getSize(); j++) {
                int thereis4 = 1;
                for (int k = 0; k < 4; k++) {
                    if (cells[i + k][j].getType() != moveType) {
                        thereis4 = 0;
                        break;
                    }
                }
                if (forSelect == 1 && thereis4 == 1) return 1;
                if (thereis4 == 1) {
                    for (int k = 0; k < 4; k++) {
                        cells[i + k][j].setType(moveType == _cellType.user1 ? _cellType.used1 : _cellType.used2);
                        cells[i + k][j].updateCellImg();
                    }
                    return 1;
                }
            }
        }
        // Sağ çapraz Kontrol
        for (int i = 3; i < getSize(); i++) {
            for (int j = 0; j < getSize() - 3; j++) {
                int thereis4 = 1;
                for (int k = 0; k < 4; k++) {
                    if (cells[i - k][j + k].getType() != moveType) {
                        thereis4 = 0;
                        break;
                    }
                }
                if (forSelect == 1 && thereis4 == 1) return 1;
                if (thereis4 == 1) {
                    for (int k = 0; k < 4; k++) {
                        cells[i - k][j + k].setType(moveType == _cellType.user1 ? _cellType.used1 : _cellType.used2);
                        cells[i - k][j + k].updateCellImg();
                    }
                    return 1;
                }
            }
        }
        // Sol çapraz Kontrol
        for (int i = 3; i < getSize(); i++) {
            for (int j = 3; j < getSize(); j++) {
                int thereis4 = 1;
                for (int k = 0; k < 4; k++) {
                    if (cells[i - k][j - k].getType() != moveType) {
                        thereis4 = 0;
                        break;
                    }
                }
                if (forSelect == 1 && thereis4 == 1) return 1;
                if (thereis4 == 1) {
                    for (int k = 0; k < 4; k++) {
                        cells[i - k][j - k].setType(moveType == _cellType.user1 ? _cellType.used1 : _cellType.used2);
                        cells[i - k][j - k].updateCellImg();
                    }
                    return 1;
                }
            }
        }
        return 0;
    }

    /**
     * Gelen kolon numarasına göre hamlenin olup olamayacağını kontrol eder.
     */
    private boolean isLegal(int col) {
        if (col < 0 || col >= size) {
            return false;
        }
        for (int i = size - 1; i >= 0; i--) {
            if (cells[i][col].getType() == _cellType.empty) {
                return true;
            }

        }
        return false;
    }

    /**
     * Oyunun bitip bitmediğini döndürür.
     */
    public int isGameEnd() {
        return gameEnd;
    }

    private class TouchHandle implements View.OnTouchListener {

        private int colNumber = -1;

        //Bir ImageVıew'e dokunma ile ilgili işlemler.
        @SuppressLint("ClickableViewAccessibility")
        @Override
        public boolean onTouch(View view, MotionEvent event) {
            if(wait.getVisibility()==View.VISIBLE) return false;
            //Eğer basılıyorsa, hangi kolon olduğunu gelen kaynağa göre bulur.
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                if (gameMode == 2 || (gameMode == 1 && turn == 0)) {
                    if (gameEnd == 0) {
                        for (int i = 0; i < size; i++) {
                            for (int j = 0; j < size; j++) {
                                if (view == cells[i][j].cellImage) {
                                    colNumber = j;
                                    j = size;
                                    i = size;

                                }
                            }
                        }
                        for (int i = 0; i < size; i++) {
                            if (cells[i][colNumber].getType() == _cellType.empty) {
                                cells[i][colNumber].setImg(_cellType.pressed);
                            }
                        }
                    }
                }
                return true;
            }
            //Eğer basılmış ve geri çekiliyorsa, animasyon işlemleri ve topu yerleştirme işlemleri gerçekleştirilir.
            else if (event.getAction() == MotionEvent.ACTION_UP) {
                if (gameMode == 2 || (gameMode == 1 && turn == 0)) {
                    if (colNumber != -1) {
                        for (int i = 0; i < size; i++) {
                            if (cells[i][colNumber].getType() == _cellType.empty) {
                                cells[i][colNumber].setImg(_cellType.empty);
                            }
                        }
                        if (onAnim == false && gameEnd == 0) play(colNumber);
                    }

                }
                return true;
            } else if (event.getAction() == MotionEvent.ACTION_CANCEL) {
                //Kaydırma işlemi için burayı kontrol et!
                if (colNumber != -1) {
                    for (int i = 0; i < size; i++) {
                        if (cells[i][colNumber].getType() == _cellType.empty) {
                            cells[i][colNumber].setImg(_cellType.empty);
                        }
                    }
                }
                return true;
            }

            return false;
        }
    }

    public class Cell {

        /**
         * Hucrenin tipi için gerekli olan tanımlama.
         */

        private int row;
        private int col;
        private _cellType cellType;
        private ImageView cellImage;


        /**
         * Default constructor hücreyi oluşturduktan sonra emptyIcon atamasını yapar.
         */
        public void setImg(final _cellType typeCell) {
            switch (typeCell) {
                case empty:
                    cellImage.setImageResource(R.drawable.emptycell);
                    break;
                case user1:
                    cellImage.setImageResource(R.drawable.user1);
                    break;
                case user2:
                    cellImage.setImageResource(R.drawable.user2);
                    break;
                case computer:
                    cellImage.setImageResource(R.drawable.computer);
                    break;
                case used1:
                    cellImage.setImageResource(R.drawable.used1);
                    break;
                case used2:
                    cellImage.setImageResource(R.drawable.used2);
                    break;
                case pressed:
                    cellImage.setImageResource(R.drawable.presscell);
                    break;
            }

            cellImage.post(new Runnable() {
                @Override
                public void run() {
                    if (typeCell == _cellType.empty && imageSize == 0) {
                        DisplayMetrics metrics = new DisplayMetrics();
                        getWindowManager().getDefaultDisplay().getMetrics(metrics);
                        int minVal = cellImage.getMeasuredWidth() * 7;
                        int forLayout = (metrics.widthPixels * 47) / 768;
                        if (minVal + forLayout > metrics.widthPixels) {
                            int imgSize = (metrics.widthPixels - forLayout) / 7;
                            cellImage.setAdjustViewBounds(true);
                            cellImage.getLayoutParams().width = imgSize;
                            imageSize = imgSize;
                        } else {
                            imageSize = cellImage.getMeasuredWidth();
                        }
                    }
                    cellImage.setAdjustViewBounds(true);
                    cellImage.getLayoutParams().width = imageSize;
                }
            });

        }

        public void updateCellImg() {
            setImg(cellType);
        }

        private void createViewImage() {
            cellImage = new ImageView(context);
            GridLayout.LayoutParams param = new GridLayout.LayoutParams();
            param.height = GridLayout.LayoutParams.WRAP_CONTENT;
            param.width = GridLayout.LayoutParams.WRAP_CONTENT;
            cellImage.setLayoutParams(param);
        }

        Cell() {
            row = 0;
            col = 0;
            cellType = _cellType.empty;
            createViewImage();
            setImg(cellType);

        }

        Cell(int rowVal, int colVal, _cellType typeVal) {
            row = rowVal;
            col = colVal;
            cellType = typeVal;
            createViewImage();
            setImg(cellType);
        }

        /**
         * Hücrenin tipini döndürür.
         */
        protected _cellType getType() {

            return cellType;
        }

        /**
         * Hücrenin tipini ayarlar.
         */
        protected void setType(_cellType typeVal) {

            cellType = typeVal;
        }

        /**
         * Hücrenin row değerini döndürür.
         */
        private int getRow() {
            return row;
        }

        /**
         * Hücrenin col değerini döndürür.
         */
        private int getCol() {
            return col;
        }

        /**
         * Hücrenin row değerini ayarlar.
         */
        private void setRow(int rowVal) {
            row = rowVal;
        }

        /**
         * Hücrenin col değerini ayarlar.
         */
        private void setCol(int colVal) {
            row = colVal;
        }

    }

    /**
     * Bilgisayar seçimini yaparken oyuncuyu bilgilendirme ekranı çıkması için gerekli olan custom dialog.
     */
    public class waitDialogClass extends  Dialog {
        public Activity c;
        public waitDialogClass(Activity a) {
            super(a);
            this.c = a;
        }
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            requestWindowFeature(Window.FEATURE_NO_TITLE);
            setContentView(R.layout.wait);
            setCancelable(false);
            getWindow().setBackgroundDrawableResource(R.color.transparan);

        }
    }
    public class PauseDialog extends Dialog implements android.view.View.OnClickListener {

        private Activity c;
        public Dialog d;
        private Button yes, no;

        private PauseDialog (Activity a) {
            super(a);
            this.c = a;
        }

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            requestWindowFeature(Window.FEATURE_NO_TITLE);
            setContentView(R.layout.dialog);
            yes = (Button) findViewById(R.id.btn_yes);
            no = (Button) findViewById(R.id.btn_no);
            yes.setOnClickListener(this);
            no.setOnClickListener(this);
            setCancelable(false);
            getWindow().setBackgroundDrawableResource(R.color.transparan);

        }

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.btn_yes:
                    dismiss();
                    if (gameMode == 2 || (gameMode == 1 && turn == 0)) {
                        if (time > 0 && isGameEnd() == 0) {
                            if (assignTimer == 1) timer.start();
                        }
                    }
                    break;
                case R.id.btn_no:
                    Intent intent = new Intent(context, MainActivity.class);
                    startActivity(intent);
                    dismiss();

                    break;
                default:
                    break;
            }
        }
    }
}



