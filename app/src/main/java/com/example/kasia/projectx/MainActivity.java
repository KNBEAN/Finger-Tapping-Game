package com.example.kasia.projectx;

import android.animation.ValueAnimator;
import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class MainActivity extends Activity{

    /**
     * dziesiętne sekundy odliczane w runTime()
     */
    private int mseconds=0;

    /**
     * licznik tapnięć
     */
    private int counter=0;

    /**
     * zmienna opisująca ostatnio kliknięty przycisk
     */
    private int mPreviousClickedButton = -1; // -1: before start, 0:left button, 1:right button

    /**
     * limit czasu pomiaru
     */
    private int timeLimit;

    /**
     * TextView z informacją o częstotliwości przeciwnika
     */
    private TextView bobFreq;

    /**
     * TextView z informacją o częstotliwości gracza
     */
    private TextView frequency;

    /**
     * TextView z informacją o pozostałym czasie pomiaru
     */
    private TextView timeView;

    /**
     * TextView z informacją o wygranej lub przegranej przeciwnika (WINNER/LOSER)
     */
    private TextView Bob;

    /**
     * TextView z informacją o wygranej lub przegranej gracza (WINNER/LOSER)
     */
    private TextView You;


    /**
     * obliczana częstotliwość gracza
     */
    float mFrequencyValue;


    /**
     * animacja poruszających się fal
     */
    final ValueAnimator animator = ValueAnimator.ofFloat(0.0f, 1.0f);

    /**
     * obrazek z czerwonym wioslarzem
     */
    private ImageView rower1;

    /**
     * obrazek z czarnym wioslarzem
     */
    private ImageView rower2;

    /**
     * mediaPlayer do odtworzenia dźwięku wygranej/przegranej
     */
    MediaPlayer sound;

    /**
     * lista parametrów do zapisu w pliku // obecnie nieużywany
     */
    private List<String> lineList = new ArrayList<>();

    /**
     * przycisk do zapisu pliku //obecnie nieużywany
     */
    private Button saveBtn;

    /**
     * obiekt klasy BluetoothConnectionService
     */
    private BluetoothConnectionService mBluetoothConnectionService;

    /**
     * stała z wartością przypisaną prawemu przyciskowi
     */
    public static final byte RIGHT_BYTE_VALUE = 55;

    /**
     * stała z wartością przypisaną lewemu przyciskowi
     */
    public static final byte LEFT_BYTE_VALUE = 65;

    /**
     * lewy przycisk do Finger Tapping Test
     */
    private Button mLeftButton;

    /**
     * prawy przycisk do Finger Tapping Test
     */
    private Button mRightButton;

    /**
     * obiekt klasy Dialog
     */
    private Dialog dialog;

    /**
     * odbierana i "odszyfrowana" wartość częstotliwości przeciwnika
     */
    private float mBobFreqValue;

    /**
     * przycisk START do rozpoczęcia gry
     */
    private Button mStartBtn;
    private Button connectBtn;
    private ImageView winner;
    private ImageView loser;



    /**
     * obiekt klasy CountDownTimer do odliczania czasu od naciśnięcia przycisku START do rozpoczęcia gry
     */
    private CountDownTimer mCountDownTimer = new CountDownTimer(4000, 1000) {
        /**
         * metoda wywoływana co zadany odstęp czasu (countDownInterval)
         */
        @Override
        public void onTick(long l) {
            mStartBtn.setText(Long.toString(l/1000));
        }

        /**
         * metoda wywoływana po upływie zadanego czasu (millisInFuture)
         */
        @Override
        public void onFinish() {
            mStartBtn.setText("0");
            startRace();
        }
    };


    /**
     * Obiekt anonimowej klasy ActivityCallback, implementujący metody interfejsu
     */
    private ActivityCallback mActivityCallback = new ActivityCallback() {

        /**
         * Metoda obsługująca bajty przekazane z obiektu BluetoothConnectionService
         * @param data tablica bajtów
         */
        @Override
        public void setReceivedBytes(final byte[] data) {
            switch(data[0]) {
                case 1:
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            start();
                        }
                    });
                    break;
                case 2:
                    moveRower(data);
                    break;
                case 3:
                    mBobFreqValue = ByteBuffer.wrap(Arrays.copyOfRange(data, 1, 5)).getFloat();
                    final String newFreq = Float.toString(mBobFreqValue);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            bobFreq.setText(newFreq);
                        }
                    });


            }

        }

        /**
         * Metoda do animacji wioślarza
         * @param data tablica bajtów
         */
        private void moveRower(final byte[] data) {
            runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    if(data[1] == LEFT_BYTE_VALUE){
                        rower2.setVisibility(View.INVISIBLE);
                    }
                    else if (data[1] == RIGHT_BYTE_VALUE){
                        rower2.setVisibility(View.VISIBLE);
                    }
                }
            });
        }

        /**
         * Metoda ustawiająca instancję BluetoothConnectionService; przypisuje referencję do obiektu, który nawiązał połaczenie z 2. urzadzeniem
         * @param instance obiekt klasy BluetoothConnectionService
         */
        @Override
        public void setBlueToothConnectionInstance(BluetoothConnectionService instance) {
            mBluetoothConnectionService = instance;
        }

        /**
         * Metoda zamykjąca okno dialogowe
         */
        @Override
        public void dismissConnectionDialog() {
            dialog.dismiss();
        }

        /**
         * Metoda ustawiająca status połączenia; rozważane przypadki CONNECTED/DISCONNECTED
         * manipuluje widocznością przycisku START
         * @param status enum z możliwymi statusami połączenia
         */
        @Override
        public void setConnectionStatus(final BluetoothConnectionService.ConnectionStatus status) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    switch (status) {
                        case CONNECTED:
                            Toast.makeText(MainActivity.this, "Connected",Toast.LENGTH_LONG).show();
                            mStartBtn.setEnabled(true);
                            break;
                        case DISCONNECTED:
                            Toast.makeText(MainActivity.this, "Disconnected",Toast.LENGTH_LONG).show();
                            mStartBtn.setEnabled(false);
                            break;
                    }

                }
            });
        }

    };


    /**
     * Metoda, do inicjalizacji Głównej Aktywności; budowanie interfejsu użytkownika, powiązananie danych z kontrolerami
     * @param savedInstanceState obiekt klasy Bundle; zapisany stan instancji
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        bobFreq = (TextView) findViewById(R.id.bob);
        frequency = (TextView) findViewById(R.id.frequency);
        timeView = (TextView) findViewById(R.id.time);



        mRightButton = (Button) findViewById(R.id.RightBtn);
        mLeftButton = (Button) findViewById(R.id.LeftBtn);
        mStartBtn = (Button) findViewById(R.id.startBtn);
        mStartBtn.setEnabled(false);
        mRightButton.setEnabled(false);
        mLeftButton.setEnabled(false);
        connectBtn = (Button) findViewById(R.id.connectBtn);


        Bob = (TextView) findViewById(R.id.BOB);
        You = (TextView) findViewById(R.id.You);
        saveBtn = (Button) findViewById(R.id.saveBtn);

        frequency.setText("0.0");
        bobFreq.setText("0.0");
        timeView.setText("00.0");
        saveBtn.setVisibility(View.INVISIBLE);

        timeLimit=10;

        final ImageView backgroundOne = (ImageView) findViewById(R.id.background1);
        final ImageView backgroundTwo = (ImageView) findViewById(R.id.background2);

       // final ValueAnimator animator = ValueAnimator.ofFloat(0.0f, 1.0f);
        animator.setRepeatCount(ValueAnimator.INFINITE);
        animator.setInterpolator(new LinearInterpolator());
        animator.setDuration(10000L);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                final float progress = (float) animation.getAnimatedValue();
                final float width = backgroundOne.getWidth();
                final float translationX = width * progress;
                backgroundOne.setTranslationX(translationX);
                backgroundTwo.setTranslationX(translationX - width);
            }
        });


        rower1 = (ImageView)findViewById(R.id.rower1b);
        rower2 = (ImageView)findViewById(R.id.rower1);
        winner = (ImageView) findViewById(R.id.winnerImage);
        loser = (ImageView) findViewById(R.id.loserImage);

    }

    private void runTime(){

        final Handler handler = new Handler();

        handler.post(new Runnable() {
            @Override
            public void run() {

                int sec = mseconds/10;
                int msec = mseconds%10;


                String time = String.format(Locale.getDefault(),"%02d.%d",sec,msec);
                timeView.setText(time);


                mFrequencyValue = (float) (Math.round(((double) counter / mseconds * 10) * 10) / 10.);

                frequency.setText(Float.toString(mFrequencyValue));

                mBluetoothConnectionService.write(convertFloatToBytes(mFrequencyValue));

                lineList.add(((double) mseconds/10) + "," +counter+","+ mFrequencyValue);

                mseconds++;


                if (sec==timeLimit){
                    mStartBtn.setEnabled(true);
                    mStartBtn.setText("START");
                    mPreviousClickedButton = -1;
                    mRightButton.setEnabled(false);
                    mLeftButton.setEnabled(false);
                    mseconds=0;
                    counter=0;
                    animator.pause();
                    saveBtn.setVisibility(View.VISIBLE);
                    mStartBtn.setVisibility(View.INVISIBLE);
                    mLeftButton.setVisibility(View.INVISIBLE);
                    mRightButton.setVisibility(View.INVISIBLE);
                    connectBtn.setVisibility(View.INVISIBLE);

                    if(mFrequencyValue > mBobFreqValue){
                       // You.setText("WINNER");
                       // Bob.setText("LOSER");
                        sound= MediaPlayer.create(MainActivity.this,R.raw.winsound);
                        winner.setVisibility(View.VISIBLE);
                        sound.start();
                    }
                    else{
                       // Bob.setText("WINNER");
                       // You.setText("LOSER");
                        sound= MediaPlayer.create(MainActivity.this,R.raw.failsound);
                        loser.setVisibility(View.VISIBLE);
                        sound.start();
                    }
                    return;
                }
                handler.postDelayed(this,100);

            }
        });
    }

    private void start() {
        frequency.setText("0.0");
        //tapCounter.setText("0");
        You.setText("");
        Bob.setText("");
        lineList.clear();
        lineList.add("time,tap counter,frequency");
        saveBtn.setVisibility(View.INVISIBLE);
        mCountDownTimer.start();
    }
    public void OnClickStartBtn(View view) {
        mStartBtn.setEnabled(false);
        //wysyłanie komendy start
        mBluetoothConnectionService.write(new byte[] {1, 0, 0, 0, 0});
        start();
    }

    public void OnClickConnectBtn(View view){
        dialog = new ConnectionDialog(MainActivity.this, mActivityCallback);
        dialog.show();
    }
    private void startRace() {
        mLeftButton.setEnabled(true);
        mRightButton.setEnabled(true);
        animator.start();
        runTime();
    }

    public void OnClickLeftBtn(View view) {
        if (mPreviousClickedButton == 1 || mPreviousClickedButton == -1) {
            mBluetoothConnectionService.write(new byte[] {2, LEFT_BYTE_VALUE, 0, 0, 0});
            counter++;
            mPreviousClickedButton = 0;
            moveRower(rower1);
        }

    }
    public void OnClickRightBtn(View view) {
        if (mPreviousClickedButton == 0 || mPreviousClickedButton == -1) {
            mBluetoothConnectionService.write(new byte[] {2, RIGHT_BYTE_VALUE, 0, 0, 0});
            counter++;
            mPreviousClickedButton = 1;
            moveRower(rower1);
        }

    }
    private void moveRower(ImageView rower) {
        if(rower.getVisibility()==View.VISIBLE){
            rower.setVisibility(View.INVISIBLE);
        }
        else{
            rower.setVisibility(View.VISIBLE);
        }
    }

    public void OnClickSaveBtn(View view){

        saveBtn.setVisibility(View.INVISIBLE);
        mStartBtn.setVisibility(View.VISIBLE);
        mLeftButton.setVisibility(View.VISIBLE);
        mRightButton.setVisibility(View.VISIBLE);
        connectBtn.setVisibility(View.VISIBLE);
        winner.setVisibility(View.INVISIBLE);
        loser.setVisibility(View.INVISIBLE);

        Intent intent = new Intent(this,SecondActivity.class);
        intent.putStringArrayListExtra(SecondActivity.MY_MESSAGE,(ArrayList<String>)lineList);
        startActivity(intent);

    }

    private byte[] convertFloatToBytes(float value) {
        byte[] parsedData = ByteBuffer.allocate(4).putFloat(value).array();
        byte[] output = new byte[5];
        output[0] = 3;
        int i = 1;
        for (byte temp : parsedData) {
            output[i++] = temp;
        }
        return output;
    }

}
