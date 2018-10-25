package com.example.kasia.projectx;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

public class SecondActivity extends Activity {

    public static final String MY_MESSAGE="myMessage";
    private List<String> lineList;
    private EditText file;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_second);

        lineList=getIntent().getStringArrayListExtra(MY_MESSAGE);

        file = (EditText) findViewById(R.id.filename);
    }


    public void OnClickSaveBtn(View view){

        String filename = file.getText().toString();
        saveToFile(lineList, "/TEST/",filename);
    }

    public void OnClickBackBtn(View view){

        finish();
    }

    private void saveToFile(List<String> data, String folder, String fileName) {

        File root = android.os.Environment.getExternalStorageDirectory();
        File dir = new File(root.getAbsolutePath() + folder);
        dir.mkdirs();
        File file = new File(dir, fileName);

        String test = file.getAbsolutePath();
        Log.i("My", "FILE LOCATION: " + test);

        try {
            FileOutputStream f = new FileOutputStream(file);
            PrintWriter pw = new PrintWriter(f);


            for (int i = 0; i < data.size(); i++) {
                pw.println(data.get(i));
            }

            pw.flush();
            pw.close();
            f.close();

            Toast.makeText(getApplicationContext(),

                    "Data saved",

                    Toast.LENGTH_LONG).show();


        } catch (FileNotFoundException e) {
            e.printStackTrace();
            Log.i("My", "******* File not found. Did you" +
                    " add a WRITE_EXTERNAL_STORAGE permission to the manifest?");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
