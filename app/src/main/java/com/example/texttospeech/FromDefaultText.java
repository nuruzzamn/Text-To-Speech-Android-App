package com.example.texttospeech;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.orhanobut.logger.AndroidLogAdapter;
import com.orhanobut.logger.Logger;

import java.util.HashMap;
import java.util.Locale;

public class FromDefaultText extends AppCompatActivity {

    private Button button;
    private TextView textView;

    private TextToSpeech textToSpeech;
    private String text;
    private String[] paragraphList;
    private int paragraphCount = 0;
    private int paragraphListLength = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_from_default_text);
        Logger.addLogAdapter(new AndroidLogAdapter());

        button = (Button) findViewById(R.id.button);
        textView = (TextView) findViewById(R.id.textView);
        //button.setOnClickListener((View.OnClickListener) this);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(!textToSpeech.isSpeaking()) {
                    button.setText("Stop");

                    HashMap<String, String> map = new HashMap<>();
                    map.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "speak");

                    for(int i=0; i<paragraphList.length; i++) {
                        textToSpeech.speak(paragraphList[i], TextToSpeech.QUEUE_ADD, map);
                        textToSpeech.playSilence(250, TextToSpeech.QUEUE_ADD, null);
                    }
                } else {
                    textToSpeech.stop();
                    button.setText("Play");
                }


            }
        });

        text = getString(R.string.long_article); //R.string.long_article also available in string file

        //TextToSpeech engine can't speak a text more than 4000 characters
        //So we have to split the long article and then precess the text chunks
        //here I split it by `\n\n`. You should split with your own logic
        paragraphList = text.split("\\n\\n");
        paragraphListLength = paragraphList.length;

        button.setText("Play");
        textView.setText(text);

        initializeTextToSpeech();
    }


    private void initializeTextToSpeech() {
        textToSpeech = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if(status == TextToSpeech.SUCCESS){
                    textToSpeech.setLanguage(Locale.ENGLISH);
                    textToSpeech.setSpeechRate((float) 1.0); //1.0 is normal. lower value decrease the speed and upper value increase

                    textToSpeech.setOnUtteranceProgressListener(new UtteranceProgressListener() {
                        @Override
                        public void onStart(String s) {
                            Logger.d("Started TextToSpeech");
                        }

                        @Override
                        public void onDone(String s) {
                            paragraphCount++;
                            Logger.d("Done text chunk: " + paragraphCount + " List Length: " + paragraphListLength);
                            if(paragraphCount==paragraphListLength){
                                paragraphCount = 0;

                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        button.setText("Play");
                                    }
                                });
                            }
                        }

                        @Override
                        public void onError(String s) {
                            Logger.d("Error occurred: " + s);
                        }
                    });
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(textToSpeech!=null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
        }
    }
}

