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

    private int mseconds=0;
    private boolean running=false;
    private boolean start=false;
    private SeekBar timeSeek;
    private int counter=0;
    private int mPreviousClickedButton = -1; // -1: before start, 0:left button, 1:right button
    private int timeLimit;
    private TextView bobFreq;
    private TextView frequency;
    private TextView timeView;
    private TextView seemTime;
    private TextView Bob;
    private TextView You;
    int step;
    int max;
    int min;
    float mFrequencyValue;
    final ValueAnimator animator = ValueAnimator.ofFloat(0.0f, 1.0f);
    private ImageView rower1;
    private ImageView rower2;
    MediaPlayer sound;
    private List<String> lineList = new ArrayList<>();
    private Button saveBtn;
    private BluetoothConnectionService mBluetoothConnectionService;
    public static final byte RIGHT_BYTE_VALUE = 55;
    public static final byte LEFT_BYTE_VALUE = 65;
    private Button mLeftButton;
    private Button mRightButton;
    private Dialog dialog;


    private CountDownTimer mCountDownTimer = new CountDownTimer(4000, 1000) {
        @Override
        public void onTick(long l) {
            seemTime.setText(Long.toString(l/1000));
        }

        @Override
        public void onFinish() {
            seemTime.setText("0");
            startRace();
        }
    };


    private ActivityCallback mActivityCallback = new ActivityCallback() {
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
                    float f = ByteBuffer.wrap(Arrays.copyOfRange(data, 1, 5)).getFloat();
                    final String newFreq = Float.toString(f);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            bobFreq.setText(newFreq);
                        }
                    });


            }

        }
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

        @Override
        public void setBlueToothConnectionInstance(BluetoothConnectionService instance) {
            mBluetoothConnectionService = instance;
        }

        @Override
        public void dismissConnectionDialog() {
            dialog.dismiss();
        }

        @Override
        public void setConnectionStatus(final BluetoothConnectionService.ConnectionStatus status) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    switch (status) {
                        case CONNECTED:
                            Toast.makeText(MainActivity.this, "Connected",Toast.LENGTH_LONG).show();
                            break;
                        case DISCONNECTED:
                            Toast.makeText(MainActivity.this, "Disconnected",Toast.LENGTH_LONG).show();
                            break;
                    }

                }
            });
        }

    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        bobFreq = (TextView) findViewById(R.id.bob);
        frequency = (TextView) findViewById(R.id.frequency);
        timeView = (TextView) findViewById(R.id.time);

        timeView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog = new ConnectionDialog(MainActivity.this, mActivityCallback);
                dialog.show();
            }
        });

        mRightButton = (Button) findViewById(R.id.RightBtn);
        mLeftButton = (Button) findViewById(R.id.LeftBtn);

        mRightButton.setEnabled(false);
        mLeftButton.setEnabled(false);

        seemTime =(TextView) findViewById(R.id.mTime);
        Bob = (TextView) findViewById(R.id.BOB);
        You = (TextView) findViewById(R.id.You);
        saveBtn = (Button) findViewById(R.id.saveBtn);

        frequency.setText("0.0");
        bobFreq.setText("10.0");
        timeView.setText("00.0");
        saveBtn.setVisibility(View.INVISIBLE);

        step=5;
        max=20;
        min=5;

        timeSeek= (SeekBar) findViewById(R.id.timeSeek);
        timeSeek.setMax((max-min)/step);
        //timeSeek.incrementProgressBy(10);
        timeSeek.setProgress(1);
        timeLimit=10;
        seemTime.setText("10");

        timeSeek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean b) {

                int value = min+ (progress*step);
                timeLimit=value;
                seemTime.setText(Integer.toString(timeLimit));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

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

               /* byte[] parsedData = ByteBuffer.allocate(4).putFloat(mFrequencyValue).array();
                byte[] output = new byte[5];
                output[0] = 3;
                int i = 1;
                for (byte temp : parsedData) {
                    output[i++] = temp;
                }
                mBluetoothConnectionService.write(output);*/
                //MessageCreator.createFrequencyMessage(mFrequencyValue, mBluetoothConnectionService);

                lineList.add(((double) mseconds/10) + "," +counter+","+ mFrequencyValue);

                mseconds++;


                if (sec==timeLimit){
                    running=false;
                    start=false;
                    mseconds=0;
                    counter=0;
                    animator.pause();
                    saveBtn.setVisibility(View.VISIBLE);

                    if(mFrequencyValue >10){
                        You.setText("WINNER");
                        Bob.setText("LOSER");
                        sound= MediaPlayer.create(MainActivity.this,R.raw.winsound);
                        sound.start();
                    }
                    else{
                        Bob.setText("WINNER");
                        You.setText("LOSER");
                        sound= MediaPlayer.create(MainActivity.this,R.raw.failsound);
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
        mBluetoothConnectionService.write(new byte[] {1, 0, 0, 0, 0});
        start();
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

        Intent intent = new Intent(this,SecondActivity.class);
        intent.putStringArrayListExtra(SecondActivity.MY_MESSAGE,(ArrayList<String>)lineList);
        startActivity(intent);

    }

}
