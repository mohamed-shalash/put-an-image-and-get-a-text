package com.example.textreader;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.SparseArray;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {
    ImageView iv;
    TextView tv;
    Bitmap bitmap;
    Dialog dialog;
    ImageButton copy;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        iv =findViewById(R.id.imageView);
        tv =findViewById(R.id.textView);
        dialog=new Dialog(this);
        copy =findViewById(R.id.copy_img);

        ActivityResultLauncher<Intent> activityResult =registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        iv.setImageURI(result.getData().getData());
                        Uri uri =result.getData().getData();
                        try {
                            //savePath =uri.getPath();
                            System.out.println(uri.getPath());
                            bitmap = MediaStore.Images.Media.getBitmap(getBaseContext().getContentResolver(), uri);
                            get_text();
                        }catch (IOException e) {
                            // TODO Handle the exception
                        }
                    }
                }
        );
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA}, 0);
        }


        ActivityResultLauncher<Intent> mStartForResult =  registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        Bundle bundle =result.getData().getExtras();
                        Bitmap bitmap = (Bitmap) bundle.get("data");
                        //iv.setImageURI(result.getData().getData());
                        iv.setImageBitmap(bitmap);
                        get_text();
                    }
                }
                );

        copy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ClipboardManager clipboardManager = (ClipboardManager) getSystemService(MainActivity.CLIPBOARD_SERVICE);
                ClipData clip =ClipData.newPlainText("TextView",tv.getText().toString());
                clipboardManager.setPrimaryClip(clip);
                Toast.makeText(MainActivity.this, "Copied", Toast.LENGTH_SHORT).show();
            }
        });

        iv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                 dialog.setContentView(R.layout.image_picker);
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.WHITE));

                ImageButton camera =dialog.findViewById(R.id.camera_image_button);
                ImageButton Gallary =dialog.findViewById(R.id.gallary_image_button);

                camera.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                        try {
                            mStartForResult.launch(takePictureIntent);
                        } catch (ActivityNotFoundException e) {
                            // display error state to the user
                        }
                        dialog.dismiss();

                    }
                });

                Gallary.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent();
                        intent.setType("image/*");
                        intent.setAction(Intent.ACTION_GET_CONTENT);
                        activityResult.launch(intent);
                        dialog.dismiss();
                    }
                });
                dialog.show();
            }
        });


    }

    void get_text(){
        TextRecognizer recognizer =new TextRecognizer.Builder(getApplicationContext()).build();

        BitmapDrawable drawable = (BitmapDrawable) iv.getDrawable();
        Bitmap bitmap = drawable.getBitmap();
        Frame frame =new Frame.Builder().setBitmap(bitmap).build();

        SparseArray<TextBlock> items =recognizer.detect(frame);
        StringBuilder sb =new StringBuilder();

        for (int i =0;i<items.size();i++){
            TextBlock myItem = items.valueAt(i);
            sb.append(myItem.getValue());
            sb.append("\n");
        }
        tv.setText(sb.toString());
    }
}