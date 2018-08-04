package com.example.shiz.zipcontacts;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;
import com.opencsv.CSVWriter;
import ir.mahdi.mzip.zip.ZipArchive;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import rx.Observable;
import rx.Observer;
import rx.Subscriber;
import retrofit2.Retrofit;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity {

    Button btn_saveContacts;
    Boolean permissionsBool;
    RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        btn_saveContacts = (Button) findViewById(R.id.saveContacts);
        recyclerView.addOnItemTouchListener(new RecyclerTouchListener(this,
                recyclerView, new RecyclerTouchListener.ClickListener() {
            @Override
            public void onClick(View view, final int position) {
                Intent intent = new Intent(MainActivity.this, ViewImage.class);
                ImageView imageView = (ImageView) view.findViewById(R.id.bitmapview);
                Drawable drawable = imageView.getDrawable();
                BitmapDrawable bitmapDrawable = ((BitmapDrawable) drawable);
                Bitmap bitmap = bitmapDrawable.getBitmap();
                intent.putExtra("DATA", bitmap);
                startActivity(intent);

            }

            @Override
            public void onLongClick(View view, int position) {
            }
        }));

        //OnClickListener for Save Contacts Button
        btn_saveContacts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Asking for runtime permissions
                ActivityCompat.requestPermissions(MainActivity.this,
                        new String[]{Manifest.permission.READ_CONTACTS, Manifest.permission.READ_EXTERNAL_STORAGE},
                        1);
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 1: {

                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    permissionsBool = true;
                    doBackgroundTasks();

                } else {

                    permissionsBool = false;
                    Toast.makeText(MainActivity.this, "Permissions are Denied", Toast.LENGTH_LONG).show();
                }
                return;
            }

        }
    }

    public void doBackgroundTasks() {

        //Observable
        Observable<String> myObservable = Observable.create(
                new Observable.OnSubscribe<String>() {
                    @Override
                    public void call(Subscriber<? super String> sub) {
                        Cursor cursor = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, null);
                        Log.d("CURSOR", String.valueOf(cursor.getCount()));

                        List<String[]> data = new ArrayList<String[]>();

                        String csv = android.os.Environment.getExternalStorageDirectory().getAbsolutePath().concat("/contacts.csv");
                        CSVWriter writer = null;
                        try {
                            writer = new CSVWriter(new FileWriter(csv));
                        } catch (IOException e) {
                            Log.d("EMPTY", e.getMessage());
                            e.printStackTrace();
                        }

                        //Storing id, name , number in List "data"
                        while (cursor.moveToNext()) {

                            String id = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));
                            String name = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                            String phoneNumber = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));

                            data.add(new String[]{id, name, phoneNumber});

                        }

                        //Writing List "data" to CSV
                        if (writer != null) {
                            writer.writeAll(data);
                        } else {
                        }

                        //Closing cursor and writer
                        cursor.close();
                        try {
                            writer.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        ZipArchive zipArchive = new ZipArchive();
                        zipArchive.zip(android.os.Environment.getExternalStorageDirectory().getAbsolutePath().concat("/contact.csv")
                                , android.os.Environment.getExternalStorageDirectory().getAbsolutePath().concat("/contact.zip"), "");

                        sub.onCompleted();
                    }
                }
        );


        myObservable
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<String>() {
                    @Override
                    public void onNext(String s) {
                    }

                    @Override
                    public void onCompleted() {
                        //Showing Snackbar on Completion
                        Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content), ".Zip has been saved to the Root Directory of Your Device", Snackbar.LENGTH_LONG);
                        snackbar.show();

                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.d("Error", e.getMessage());
                        e.printStackTrace();
                    }
                });

    }
}
