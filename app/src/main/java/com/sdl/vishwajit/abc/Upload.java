package com.sdl.vishwajit.abc;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;

public class Upload extends AppCompatActivity {


    String pictureFilePath;
    ImageView circleImageView;
    LinearLayout layout;
    private static final String IMAGE_DIRECTORY = "/YourDirectName";
    private Context mContext;
    ArrayList<Bitmap> bitmaps;
    RecyclerView mRecyclerView;
    Upload_Adapter adapter;
    GridView gridView;
    ArrayAdapter<Bitmap> arrayAdapter;
    private int GALLERY = 1, CAMERA = 2;
    public int PERMISSIONS_MULTIPLE_REQUEST=1;
    EditText title,loc,desc;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload);

        ImageButton addpic = findViewById(R.id.upload_addpic);
        title= findViewById(R.id.upload_title);
        loc = findViewById(R.id.upload_location);
        desc = findViewById(R.id.upload_desc);
        Button submit = findViewById(R.id.upload_button);



        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                int time = (int) (System.currentTimeMillis());
                Timestamp tsTemp = new Timestamp(time);
                String ts =  tsTemp.toString();

                FirebaseDatabase fd = FirebaseDatabase.getInstance();
                DatabaseReference ref = fd.getReference("A").child("Problems");

                ref.child("user"+ts).child("title").setValue(title.getText().toString());
                ref.child("user"+ts).child("location").setValue(loc.getText().toString());
                ref.child("user"+ts).child("desc").setValue(desc.getText().toString());
            }
        });


        bitmaps = new ArrayList<>();

        layout = findViewById(R.id.linearlayout);
      //  Button upload = findViewById(R.id.upload);
       addpic.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if(((Upload_Adapter) mRecyclerView.getAdapter()).getItemCount()<5)
                    checkPermission();
                else
                    Toast.makeText(v.getContext(),"ONLY FIVE PHOTOS ARE ALLOWS",Toast.LENGTH_SHORT).show();

            }
        });

        mRecyclerView = findViewById(R.id.upload_pic_list);
        GridLayoutManager mGridLayoutManager = new GridLayoutManager(this, 2);
        mRecyclerView.setLayoutManager(mGridLayoutManager);

        Upload_Adapter myAdapter = new Upload_Adapter(this,mRecyclerView, new ArrayList<Bitmap>());
        mRecyclerView.setAdapter(myAdapter);
    }


    void showPictureDialog(){
    Intent galleryIntent = new Intent();
        galleryIntent.setType("image/*");
        galleryIntent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
    startActivityForResult(Intent.createChooser(galleryIntent,"SELECT..."), GALLERY);
}

    private void takePhotoFromCamera() {
        Intent intent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, CAMERA);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == this.RESULT_CANCELED) {
            return;
        }
        if (requestCode == GALLERY) {
            if (data != null) {
                if (data.getClipData() != null) {
                    int count = data.getClipData().getItemCount();
                    int pic_count =((Upload_Adapter) mRecyclerView.getAdapter()).getItemCount();
                    if (count > 5 - pic_count) {
                        count = 5 - pic_count;
                        Toast.makeText(this, "ONLY FIVE PHOTOS ARE ALLOWS", Toast.LENGTH_SHORT).show();
                    }

                    for (int i = 0; i < count && pic_count <= 5; i++) {
                        Uri contentURI = data.getClipData().getItemAt(i).getUri();
                        try {
                            Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), contentURI);
                            ((Upload_Adapter) mRecyclerView.getAdapter()).update(bitmap);
                            bitmaps.add(bitmap);


                        } catch (IOException e) {
                            e.printStackTrace();
                            Toast.makeText(getApplicationContext(), "Failed!", Toast.LENGTH_SHORT).show();
                            break;
                        }
                    }
                }

                else{
                    Uri uri = data.getData();
                    try {
                        Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), uri);
                        Toast.makeText(getApplicationContext(), "Image Saved!", Toast.LENGTH_SHORT).show();
                        ((Upload_Adapter) mRecyclerView.getAdapter()).update(bitmap);

                    } catch (IOException e) {
                        e.printStackTrace();
                        Toast.makeText(getApplicationContext(), "Failed!", Toast.LENGTH_SHORT).show();
                    }

                }
            }

        } else if (requestCode == CAMERA) {
            Bitmap thumbnail = (Bitmap) data.getExtras().get("data");
            ((Upload_Adapter) mRecyclerView.getAdapter()).update(thumbnail);
            Toast.makeText(getApplicationContext(), "Image Saved!", Toast.LENGTH_SHORT).show();
        }
    }

    private void checkPermission() {
        if (ContextCompat.checkSelfPermission(Upload.this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(Upload.this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            showPictureDialog();
        }
        else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.CAMERA}, 9);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions, @NonNull int[] grantResults) {

        switch (requestCode) {
            case 9:
                if (grantResults.length > 0) {
                    boolean cameraPermission = grantResults[1] == PackageManager.PERMISSION_GRANTED;
                    boolean readExternalFile = grantResults[0] == PackageManager.PERMISSION_GRANTED;

                    if(cameraPermission && readExternalFile)
                    {
                        showPictureDialog();
                    } else {
                        Toast.makeText(this,"Please Give Permission",Toast.LENGTH_SHORT).show();

                    }
                }
                break;
        }
    }




}
