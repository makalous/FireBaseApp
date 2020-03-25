package com.example.firebaseapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.text.FirebaseVisionCloudTextRecognizerOptions;
import com.google.firebase.ml.vision.text.FirebaseVisionText;
import com.google.firebase.ml.vision.text.FirebaseVisionTextRecognizer;
import com.wonderkiln.camerakit.CameraKitError;
import com.wonderkiln.camerakit.CameraKitEvent;
import com.wonderkiln.camerakit.CameraKitEventListener;
import com.wonderkiln.camerakit.CameraKitImage;
import com.wonderkiln.camerakit.CameraKitVideo;
import com.wonderkiln.camerakit.CameraView;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Random;

import dmax.dialog.SpotsDialog;

public class TextActivity extends AppCompatActivity {

    CameraView cameraView;
    Button btnCapture;
    AlertDialog waitingDialog;
    GraphicOverlay graphicOverlay;

    @Override
    protected void onResume() {
        super.onResume();
        cameraView.start();
    }

    @Override
    protected void onPause() {
        super.onPause();
        cameraView.stop();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_text);

        waitingDialog = new SpotsDialog.Builder().setContext(this).setMessage("Loading").setCancelable(false).build();
        cameraView = (CameraView)findViewById(R.id.camera_view2);
        btnCapture = (Button)findViewById(R.id.btn_recognize);
        graphicOverlay = (GraphicOverlay)findViewById(R.id.graphic_overlay);
        //btnCapture.setText("Recognition");

        btnCapture.setOnClickListener(new View.OnClickListener(){
            int status = 1;
            @Override
            public void onClick(View v) {
                if(status == 1) {btnCapture.setText("Retry"); status=0; }
                else {btnCapture.setText("Recognition"); status=1;}
                cameraView.start();
                cameraView.captureImage();
                graphicOverlay.clear();

            }
        });

        cameraView.addCameraKitListener(new CameraKitEventListener() {
            @Override
            public void onEvent(CameraKitEvent cameraKitEvent) {

            }

            @Override
            public void onError(CameraKitError cameraKitError) {

            }

            @Override
            public void onImage(CameraKitImage cameraKitImage) {
                waitingDialog.show();
                Bitmap bitmap = cameraKitImage.getBitmap();
                bitmap = Bitmap.createScaledBitmap(bitmap, cameraView.getWidth(), cameraView.getHeight(),false);
                cameraView.stop();
                recognizeText(bitmap);
                //storeImage(bitmap);

            }

            @Override
            public void onVideo(CameraKitVideo cameraKitVideo) {

            }
        });


    }

    private void recognizeText(Bitmap bitmap) {
        FirebaseVisionImage image = FirebaseVisionImage.fromBitmap(bitmap);
        FirebaseVisionTextRecognizer detector = FirebaseVision.getInstance().getOnDeviceTextRecognizer();
                //.getCloudTextRecognizer();

        detector.processImage(image).addOnSuccessListener(new OnSuccessListener<FirebaseVisionText>() {
            @Override
            public void onSuccess(FirebaseVisionText firebaseVisionText) {

                drawTextResult(firebaseVisionText);

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

                Log.d("Error Cloud", e.getMessage());
            }
        });


       /* FirebaseVisionCloudTextRecognizerOptions options = new FirebaseVisionCloudTextRecognizerOptions.Builder()
                .setLanguageHints(Arrays.asList("en")).build();
        FirebaseVisionTextRecognizer textRecognizer = FirebaseVision.getInstance().getCloudTextRecognizer(options);

        textRecognizer.processImage(image).addOnSuccessListener(new OnSuccessListener<FirebaseVisionText>() {
            @Override
            public void onSuccess(FirebaseVisionText firebaseVisionText) {

                drawTextResult(firebaseVisionText);

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

                Log.d("Error Cloud", e.getMessage());
            }
        });*/
    }

    private void drawTextResult(FirebaseVisionText firebaseVisionText) {
        List<FirebaseVisionText.TextBlock> blocks = firebaseVisionText.getTextBlocks();
        if (blocks.size()==0){
            Toast.makeText(this, "No text found", Toast.LENGTH_LONG).show();
            waitingDialog.dismiss();
            return;
        }
        graphicOverlay.clear();

        for(int i=0; i<blocks.size();i++){
            List<FirebaseVisionText.Line> lines = blocks.get(i).getLines();
            for (int j=0; j<lines.size(); j++){
                List<FirebaseVisionText.Element> elements = lines.get(j).getElements();
                for(int k=0;k<elements.size();k++){
                    TextGraphic textGraphic = new TextGraphic(graphicOverlay, elements.get(k));
                    graphicOverlay.add(textGraphic);
                }
            }
        }
        waitingDialog.dismiss();
    }

    public void storeImage(Bitmap bitmap){
        String file_path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/CustomDir";
        File dir = new File(file_path);
        if(!dir.exists()) dir.mkdirs();
        String format = new SimpleDateFormat("yyyyMMddHHmmss", java.util.Locale.getDefault()).format(new Date());
        File file = new File(dir, format + ".png");
        FileOutputStream fOut;
        try {
            fOut = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 85, fOut);
            fOut.flush();
            fOut.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        Intent intent = new Intent(android.content.Intent.ACTION_SEND);
        intent.setType("image/*");
        startActivity(intent);
    }
}
