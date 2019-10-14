package hungnn.bongmaiitlimited.myapplication;


import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.extensions.android.json.AndroidJsonFactory;
import com.google.api.client.repackaged.org.apache.commons.codec.binary.Base64;
import com.google.api.services.speech.v1beta1.Speech;
import com.google.api.services.speech.v1beta1.SpeechRequestInitializer;
import com.google.api.services.speech.v1beta1.model.RecognitionAudio;
import com.google.api.services.speech.v1beta1.model.RecognitionConfig;
import com.google.api.services.speech.v1beta1.model.SpeechRecognitionResult;
import com.google.api.services.speech.v1beta1.model.SyncRecognizeRequest;
import com.google.api.services.speech.v1beta1.model.SyncRecognizeResponse;

import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;

public class MainActivity extends AppCompatActivity {
    private final String CLOUD_API_KEY = "ABCDEF1234567890";
    int RE_CODE=96;
    Uri soundUri;
    Intent filePicker=new Intent(Intent.ACTION_GET_CONTENT);

    TextView txtShow;
    Button btnShow;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        init();
        filePicker.setType("audio/flac");






        btnShow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivityForResult(filePicker, RE_CODE);

                new ProccessStreamNetwork();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode==RESULT_OK&&data!=null){
              soundUri=data.getData();
        }
    }
    private class ProccessStreamNetwork extends AsyncTask<Void, Void, Void>{

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
        }

        @Override
        protected Void doInBackground(Void... voids) {
            new Runnable() {
                @Override
                public void run() {
                    try {
                        InputStream stream = getContentResolver().openInputStream(soundUri);
                        byte[] audioData = IOUtils.toByteArray(stream);
                        stream.close();
                        String base64EncodedData =
                                Base64.encodeBase64String(audioData);
                        Speech speechService = new Speech.Builder(
                                AndroidHttp.newCompatibleTransport(),
                                new AndroidJsonFactory(),
                                null
                        ).setSpeechRequestInitializer(
                                new SpeechRequestInitializer(CLOUD_API_KEY))
                                .build();

                        RecognitionConfig recognitionConfig = new RecognitionConfig();
                        recognitionConfig.setLanguageCode("en-US");

                        RecognitionAudio recognitionAudio = new RecognitionAudio();
                        recognitionAudio.setContent(base64EncodedData);
                        // Create request
                        SyncRecognizeRequest request = new SyncRecognizeRequest();
                        request.setConfig(recognitionConfig);
                        request.setAudio(recognitionAudio);

// Generate response
                        SyncRecognizeResponse response = speechService.speech()
                                .syncrecognize(request)
                                .execute();

// Extract transcript
                        SpeechRecognitionResult result = response.getResults().get(0);
                        final String transcript = result.getAlternatives().get(0)
                                .getTranscript();
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                TextView speechToTextResult =
                                        (TextView)findViewById(R.id.textViewShow);
                                speechToTextResult.setText(transcript);
                            }
                        });
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    // More code here
                }
            };
            MediaPlayer player = new MediaPlayer();
            try {
                player.setDataSource(MainActivity.this, soundUri);
                player.prepare();
                player.start();
            } catch (IOException e) {
                e.printStackTrace();
            }


// Release the player
            player.setOnCompletionListener(
                    new MediaPlayer.OnCompletionListener() {
                        @Override
                        public void onCompletion(MediaPlayer mediaPlayer) {
                            mediaPlayer.release();
                        }
                    });
            return null;
        }
    }

    private void init() {
        btnShow=findViewById(R.id.buttonShowText);
        txtShow=findViewById(R.id.textViewShow);
    }


}
