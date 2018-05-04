package com.example.onur.connectfour;

/**
 * Created by Onur on 15.02.2018.
 * Bu sınıf yapılırken, http://blog.gamesolver.org/ ve https://www.gimu.org/connect-four-js/ adresleri referans olarak kullanılmıştır.
 */

class Board {


    private GameActivity game;
    private Integer [][] field;
    int player;

    /**
     * Game activityde bulunan bilgileri Board sınıfında ki memberlarla ilişkilendiren constructor.
     * @param game GameActivity sınıfı olarak gelen aktif oyun.
     * @param field GameActivity sınıfı içerisinde bulunan Cell bilgileri.
     * @param player Sıranın kimde olduğunu belirten değişken.
     */
    Board(GameActivity game, GameActivity.Cell[][] field, int player) {
        this.game = game;
        this.player = player;
        this.field = new Integer[game.getSize()][game.getSize()];
        for(int i=0;i<game.getSize();i++){
            for(int j=0;j<game.getSize();j++){
                if(field[i][j].getType()== GameActivity._cellType.empty)
                    this.field[i][j] = -1;
                else if(field[i][j].getType()== GameActivity._cellType.user1)
                    this.field[i][j] = 0;
                else if(field[i][j].getType()== GameActivity._cellType.user2)
                    this.field[i][j] = 1;
            }
        }

    }

    /**
     * Bu constructor ise kopyalama işlemi için kullanılıyor. Diğer constructordan farkı gelen cell bilgileri Integer türünde olması.
     */
    private Board(GameActivity game, Integer[][] field, int player) {
        this.game = game;
        this.player = player;
        this.field = new Integer[game.getSize()][game.getSize()];
        for(int i=0;i<game.getSize();i++){
            for(int j=0;j<game.getSize();j++){
                if(field[i][j]==-1)
                    this.field[i][j] = -1;
                else if(field[i][j]== 0)
                    this.field[i][j] = 0;
                else if(field[i][j]==1)
                    this.field[i][j] = 1;
            }
        }

    }

    public Integer[][] getField(){
        return field;
    }
    /**
     * Oyunun akıllı hamle için bitip bitmediğini kontrol eder.
     * @param depth derinlik
     * @param score skor eğer maksimum veya minimum skor olursa oyun bitmiş anlamına gelir.
     * @return eğer oyun bittiyse 1 döndürür.
     */
    protected boolean isFinished(int depth, int score) {
        if (depth == 0 || score == this.game.score || score == -this.game.score || this.isFull()) {
            return true;
        }
        return false;
    }

    /**
     * Integer dizisi olarak tutulan board içinde ki hücrelere gelen kolon numarasına göre yerleştirme yapar.
     * @param column Kolon numarası.
     * @return Başarılı ise 1 döner.
     */
    protected boolean place (int column) {
        // Check if column valid
        // 1. not empty 2. not exceeding the board size
        if (this.field[0][column] ==-1 && column >= 0 && column < this.game.getSize()) {
            // Bottom to top
            for (int y = this.game.getSize() - 1; y >= 0; y--) {
                if (this.field[y][column] == -1) {
                    this.field[y][column]=player;
                    //boardYaz(this.field);
                    break; // Break from loop after inserting
                }
            }
            this.player = this.player == 0 ? 1 : 0;
            return true;
        } else {
            return false;
        }
    }

    /**
     * Var olan board üzerinde ki belirtilen bir satır ve sütun alanında bulunan hücrenin skorunu, sağa sola aşağı ve yukarıya giderek hesaplar.
     * Eğer çevresinde ki hücreler dolu ise ve aynı ise skoru her defasında 1 arttırır.
     * @param row Bakılacak satır.
     * @param column Bakılacak sütun.
     * @param delta_y Satırlar arasından ne kadar farklıkla bakılacağı.
     * @param delta_x Sütunlara arasından ne kadar farklıkla bakılacağı.
     * @return skor değerini döndürür.
     */
    private int scorePosition(int row, int column, int delta_y, int delta_x) {
        int human_points = 0;
        int computer_points = 0;

        for (int i = 0; i < 4; i++) {
            if (this.field[row][column]==0) {
                human_points++;
            } else if (this.field[row][column]==1) {
                computer_points++;
            }
            row += delta_y;
            column += delta_x;
        }
        if (human_points == 4) {
            return -this.game.score;
        } else if (computer_points == 4) {
            return this.game.score;
        } else {
            return computer_points;
        }
    }

    /**
     * Bilgisayar için tek tek tüm skorları(Yatay - Dikey - Çapraz) hesaplar ve toplayıp döndürür.
     * @return toplam skor.
     */
    protected int score () {
        int points = 0;

        int vertical_points = 0;
        int horizontal_points = 0;
        int diagonal_points1 = 0;
        int diagonal_points2 = 0;


        for (int row = 0; row < this.game.getSize() - 3; row++) {
            for (int column = 0; column < this.game.getSize(); column++) {
                int score = this.scorePosition(row, column, 1, 0);
                if (score == this.game.score) return this.game.score;
                if (score == -this.game.score) return -this.game.score;
                vertical_points += score;
            }
        }

        for (int row = 0; row < this.game.getSize(); row++) {
            for (int column = 0; column < this.game.getSize() - 3; column++) {
                int score = this.scorePosition(row, column, 0, 1);
                if (score == this.game.score) return this.game.score;
                if (score == -this.game.score) return -this.game.score;
                horizontal_points += score;
            }
        }


        for (int row = 0; row < this.game.getSize() - 3; row++) {
            for (int column = 0; column < this.game.getSize() - 3; column++) {
                int score = this.scorePosition(row, column, 1, 1);
                if (score == this.game.score) return this.game.score;
                if (score == -this.game.score) return -this.game.score;
                diagonal_points1 += score;
            }
        }
        for (int row = 3; row < this.game.getSize(); row++) {
            for (int column = 0; column <= this.game.getSize() - 4; column++) {
                int score = this.scorePosition(row, column, -1, +1);
                if (score == this.game.score) return this.game.score;
                if (score == -this.game.score) return -this.game.score;
                diagonal_points2 += score;
            }

        }

        points = horizontal_points + vertical_points + diagonal_points1 + diagonal_points2;
        return points;
    }

    /**
     * Board'ın dolup dolmadığını kontrol eder.
     * @return eğer dolu ise 1 döndürür.
     */
    private boolean isFull() {
        for (int i = 0; i < this.game.getSize(); i++) {
            if (this.field[0][i]==-1) {
                return false;
            }
        }
        return true;
    }

    /**
     * Var olan board'ın bir kopyasını döndürür.
     * @return Kopyalanmış halini return eder.
     */
    Board copy() {
        
        int size = this.game.getSize();
        Integer [][] new_board = new Integer[size][size];
        for(int i=0;i<game.getSize();i++){
            for(int j=0;j<game.getSize();j++){
               if(this.field[i][j]==-1)
                   new_board[i][j]=-1;
               else if(this.field[i][j]==0)
                   new_board[i][j]=0;
               else if(this.field[i][j]==1)
                   new_board[i][j]=1;

            }
        }
        return new Board(this.game, new_board, this.player);
    }

}
