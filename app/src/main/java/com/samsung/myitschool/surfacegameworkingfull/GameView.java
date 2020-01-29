package com.samsung.myitschool.surfacegameworkingfull;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.media.MediaPlayer;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.ArrayList;


public class GameView extends SurfaceView implements Runnable {

    volatile boolean playing;
    private Thread gameThread = null;
    private Player player;

    private Paint paint;
    private Canvas canvas;
    private SurfaceHolder surfaceHolder;

    private Enemy enemies;


    private Friend friend;



    private ArrayList<Star> stars = new ArrayList<Star>();


    private Boom boom;


    int screenX;

    int countMisses;

    boolean flag ;

    private boolean isGameOver;

    int score;

    int highScore[] = new int[4];

    SharedPreferences sharedPreferences;

    public static MediaPlayer gameOnsound;
    public final MediaPlayer killedEnemysound;
    public final MediaPlayer gameOversound;

    Context context;

    public GameView(Context context, int screenX, int screenY) {
        super(context);
        player = new Player(context, screenX, screenY);

        surfaceHolder = getHolder();
        paint = new Paint();

        int starNums = 100;
        for (int i = 0; i < starNums; i++) {
            Star s = new Star(screenX, screenY);
            stars.add(s);
        }

        enemies = new Enemy(context, screenX, screenY);

        boom = new Boom(context);

        friend = new Friend(context, screenX, screenY);

        this.screenX = screenX;
        countMisses = 0;
        isGameOver = false;


        score = 0;
        sharedPreferences = context.getSharedPreferences("SHAR_PREF_NAME", Context.MODE_PRIVATE);


        highScore[0] = sharedPreferences.getInt("score1", 0);
        highScore[1] = sharedPreferences.getInt("score2", 0);
        highScore[2] = sharedPreferences.getInt("score3", 0);
        highScore[3] = sharedPreferences.getInt("score4", 0);
        this.context = context;


        gameOnsound = MediaPlayer.create(context,R.raw.gameon);
        killedEnemysound = MediaPlayer.create(context,R.raw.killedenemy);
        gameOversound = MediaPlayer.create(context,R.raw.gameover);


        gameOnsound.start();
    }

    @Override
    public boolean onTouchEvent(MotionEvent motionEvent) {
        switch (motionEvent.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_UP:
                player.stopBoosting();
                break;
            case MotionEvent.ACTION_DOWN:
                player.setBoosting();
                break;

        }

        if(isGameOver){
            if(motionEvent.getAction()==MotionEvent.ACTION_DOWN){
                context.startActivity(new Intent(context,MainActivity.class));
            }
        }
        return true;
    }

    @Override
    public void run() {
        while (playing) {
            update();
            draw();
            control();
        }
    }

    public void draw() {
        if (surfaceHolder.getSurface().isValid()) {
            canvas = surfaceHolder.lockCanvas();
            canvas.drawColor(Color.BLACK);


            paint.setColor(Color.WHITE);
            paint.setTextSize(20);

            for (Star s : stars) {
                paint.setStrokeWidth(s.getStarWidth());
                canvas.drawPoint(s.getX(), s.getY(), paint);
            }

            paint.setTextSize(30);
            canvas.drawText("Score:"+score,100,50,paint);

            canvas.drawBitmap(
                    player.getBitmap(),
                    player.getX(),
                    player.getY(),
                    paint);


            canvas.drawBitmap(
                    enemies.getBitmap(),
                    enemies.getX(),
                    enemies.getY(),
                    paint
            );

            canvas.drawBitmap(
                    boom.getBitmap(),
                    boom.getX(),
                    boom.getY(),
                    paint
            );

            canvas.drawBitmap(

                    friend.getBitmap(),
                    friend.getX(),
                    friend.getY(),
                    paint
            );

            if(isGameOver){
                paint.setTextSize(150);
                paint.setTextAlign(Paint.Align.CENTER);
                gameOversound.start();
                int yPos=(int) ((canvas.getHeight() / 2) - ((paint.descent() + paint.ascent()) / 2));
                canvas.drawText("Game Over",canvas.getWidth()/2,yPos,paint);
            }

            surfaceHolder.unlockCanvasAndPost(canvas);

        }
    }


    public static void stopMusic(){
        gameOnsound.stop();
    }

    private void update() {
        score++;

        player.update();

        boom.setX(-250);
        boom.setY(-250);

        for (Star s : stars) {
            s.update(player.getSpeed());
        }

        if(enemies.getX()==screenX){
            flag = true;
        }

        enemies.update(player.getSpeed());

        if (Rect.intersects(player.getDetectCollision(), enemies.getDetectCollision())) {

            boom.setX(enemies.getX());
            boom.setY(enemies.getY());

            killedEnemysound.start();
            enemies.setX(-100);
        }

        else{
            if(flag){

                if(player.getDetectCollision().exactCenterX()>=enemies.getDetectCollision().exactCenterX()){

                    countMisses++;

                    flag = false;

                    if(countMisses==3){

                        playing = false;
                        isGameOver = true;
                        gameOnsound.stop();
                        gameOversound.start();

                        for(int i=0;i<4;i++){
                            if(highScore[i]<score){

                                final int finalI = i;
                                highScore[i] = score;
                                break;
                            }
                        }

                        SharedPreferences.Editor e = sharedPreferences.edit();

                        for(int i=0;i<4;i++){

                            int j = i+1;
                            e.putInt("score"+j,highScore[i]);
                        }
                        e.apply();

                    }

                }
            }

        }

        friend.update(player.getSpeed());
        if(Rect.intersects(player.getDetectCollision(),friend.getDetectCollision())){

            boom.setX(friend.getX());
            boom.setY(friend.getY());
            playing = false;
            isGameOver = true;

            gameOnsound.stop();
            gameOversound.start();

            for(int i=0;i<4;i++){

                if(highScore[i]<score){

                    final int finalI = i;
                    highScore[i] = score;
                    break;
                }
            }
            SharedPreferences.Editor e = sharedPreferences.edit();

            for(int i=0;i<4;i++){

                int j = i+1;
                e.putInt("score"+j,highScore[i]);
            }
            e.apply();
        }
    }

    private void control() {
        try {
            gameThread.sleep(17);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void pause() {
        playing = false;
        try {
            gameThread.join();
        } catch (InterruptedException e) {
        }
    }

    public void resume() {
        playing = true;
        gameThread = new Thread(this);
        gameThread.start();
    }
    public MediaPlayer getGameOnsound(){
        return gameOnsound;
    }


}