package com.example.shiz.zipcontacts;
import android.graphics.Bitmap;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;

//This class contains just a ImageView
public class ViewImage extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.view_image);

        //getting out image from intent and showing it in ImageView
        Bitmap bitmap = this.getIntent().getParcelableExtra("DATA");
        ImageView viewBitmap = (ImageView) findViewById(R.id.bitmapview);
        viewBitmap.setImageBitmap(bitmap);
    }
}
