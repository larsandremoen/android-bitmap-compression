package com.example.larsandre.picturebitmapcompression;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class CompressMyPicture extends AppCompatActivity {

    private final String APP_TAG = "BitmapCompression";
    private final static int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 1337;
    private String photoFileName = "myPicture.jpg";
    private ImageButton takePictureAndCompress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_compress_my_picture);

        takePictureAndCompress = (ImageButton) findViewById(R.id.compressedImageView);

        takePictureAndCompress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onLaunchCamera(view);
            }
        });
    }

    public void onLaunchCamera(View view) {

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, getPhotoFileUri(photoFileName)); // set the picture file name

        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(intent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {

                // At this point, picture is on disk
                Uri takenPhotoUri = getPhotoFileUri(photoFileName);

                Bitmap rawTakenImage = BitmapFactory.decodeFile(takenPhotoUri.getPath());

                //BitmapScaler, "simply" scales the bitmap
                Bitmap resizedBitmap = BitmapScaler.scaleToFitWidth(rawTakenImage, 700);

                ByteArrayOutputStream bytes = new ByteArrayOutputStream();

                // Compress the image further
                resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 70, bytes);

                // Create a new file for the resized bitmap (`getPhotoFileUri` defined above)
                Uri resizedUri = getPhotoFileUri(photoFileName + "_resized");
                takePictureAndCompress.setImageBitmap(resizedBitmap);
                try {
                    //IF you want to write to a file, and decode it to at bitmap, this is how

                    // The bitmap gets written fo a file
                    File resizedFile = new File(resizedUri.getPath());
                    resizedFile.createNewFile();
                    FileOutputStream fos = new FileOutputStream(resizedFile);
                    fos.write(bytes.toByteArray());
                    fos.close();

                    //Again, file to bitmap, for making it available for image view
                    Bitmap myBitmapFile = BitmapFactory.decodeFile(resizedFile.getAbsolutePath());

                    //Set my compressed image to the image view
                    //takePictureAndCompress.setImageBitmap(myBitmapFile);
                }
                catch (IOException e){
                    return;
                }
            }
            else { // Result was a failure
                Toast.makeText(this, "Picture wasn't taken!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public Uri getPhotoFileUri(String fileName) {
        // Only continue if the SD Card is mounted
        if (isExternalStorageAvailable()) {
            // Get safe storage directory for photos
            // Use `getExternalFilesDir` on Context to access package-specific directories.
            // This way, we don't need to request external read/write runtime permissions.
            File mediaStorageDir = new File(
                    getExternalFilesDir(Environment.DIRECTORY_PICTURES), APP_TAG);

            // Create the storage directory if it does not exist
            if (!mediaStorageDir.exists() && !mediaStorageDir.mkdirs()) {
                Log.d(APP_TAG, "Directory failed to create");
            }

            // Return the file target for the photo based on filename
            return Uri.fromFile(new File(mediaStorageDir.getPath() + File.separator + fileName));
        }
        return null;
    }

    // Returns true if external storage for photos is available
    private boolean isExternalStorageAvailable() {
        String state = Environment.getExternalStorageState();
        return state.equals(Environment.MEDIA_MOUNTED);
    }

}
