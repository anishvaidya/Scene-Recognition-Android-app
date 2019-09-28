package com.example.vanish.tensorflowtest;

import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import org.tensorflow.contrib.android.TensorFlowInferenceInterface;

import java.io.InputStream;

public class MainActivity extends AppCompatActivity {

    //Load the tensorflow inference library
    static {
        System.loadLibrary("tensorflow_inference");
    }

    //PATH TO OUR MODEL FILE AND NAMES OF THE INPUT AND OUTPUT NODES
    private String MODEL_PATH = "final9class200px.pb";
    private String INPUT_NAME = "vgg16_input";
    private String OUTPUT_NAME = "output_node0";
    private TensorFlowInferenceInterface tf;

    //ARRAY TO HOLD THE PREDICTIONS AND FLOAT VALUES TO HOLD THE IMAGE DATA
    float[] PREDICTIONS = new float[1000];
    private float[] floatValues;
    private int[] INPUT_SIZE = {200,200,3};

    TextView tv1, tv2;
    Button btnPic, btnPredict;
    ImageView iv1;
    Bitmap photo;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // initialize TF with AssetsManager and model
        tf = new TensorFlowInferenceInterface(getAssets(), MODEL_PATH);

        tv1 = (TextView) findViewById(R.id.tv1);
        tv2 = (TextView) findViewById(R.id.tv2);
        btnPic = (Button) findViewById(R.id.btnPic);
        btnPredict = findViewById(R.id.btnPredict);
        iv1 = findViewById(R.id.iv1);


        btnPic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//                Intent i = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(i, 123);
            }
        });


        btnPredict.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try{

                    //READ THE IMAGE FROM ASSETS FOLDER
//                    InputStream imageStream = getAssets().open("computerroom1.jpg");
//
//                    Bitmap bitmap = BitmapFactory.decodeStream(imageStream);

//                    iv1.setImageBitmap(bitmap);

//                    progressBar.show();

                    predict(photo);


                }
                catch (Exception e){

                }
            }
        });


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 123 && resultCode == RESULT_OK){

            photo = (Bitmap)data.getExtras().get("data");
            iv1.setImageBitmap(photo);
        }

    }

    //FUNCTION TO COMPUTE THE MAXIMUM PREDICTION AND ITS CONFIDENCE
    public Object[] argmax(float[] array){


        int best = -1;
        float best_confidence = 0.0f;

        for(int i = 0;i < array.length;i++){

            float value = array[i];

            if (value > best_confidence){

                best_confidence = value;
                best = i;
            }
        }

        return new Object[]{best,best_confidence};


    }

    public void predict(final Bitmap bitmap){

        long startTime = System.currentTimeMillis();
        //Runs inference in background thread
        new AsyncTask<Integer,Integer,Integer>(){

            @Override

            protected Integer doInBackground(Integer ...params){

                //Resize the image into 200 x 200
                Bitmap resized_image = ImageUtils.processBitmap(bitmap,200);

                //Normalize the pixels
                floatValues = ImageUtils.normalizeBitmap(resized_image,200,127.5f,1.0f);

                //Pass input into the tensorflow
                tf.feed(INPUT_NAME,floatValues,1,200,200,3);

                //compute predictions
                tf.run(new String[]{OUTPUT_NAME});

                //copy the output into the PREDICTIONS array
                tf.fetch(OUTPUT_NAME,PREDICTIONS);

                //Obtained highest prediction
                Object[] results = argmax(PREDICTIONS);
//                Log.v(String.results);

                final int class_index = (Integer) results[0];
                float confidence = (Float) results[1];


                try{

                    final String conf = String.valueOf(confidence * 100).substring(0,5);

                    //Convert predicted class index into actual label name
//                    final String label = ImageUtils.getLabel(getAssets().open("labels.json"),class_index);



                    //Display result on UI
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

//                            progressBar.dismiss();
                            tv1.setText(class_index + " : " + conf + "%");

                        }
                    });

                }

                catch (Exception e){


                }


                return 0;
            }



        }.execute(0);
        long endTime = System.currentTimeMillis();
        tv2.setText("Time is:" + (endTime - startTime + "ms"));
    }
}


// check this come on

// heyajfucojuawhwco