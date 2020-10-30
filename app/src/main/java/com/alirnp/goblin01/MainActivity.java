package com.alirnp.goblin01;

import java.io.File;
import java.io.IOException;
import java.text.CharacterIterator;
import java.text.StringCharacterIterator;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import android.Manifest;
import android.app.Activity;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.MediaStore.MediaColumns;
import android.util.Log;

import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.alirnp.goblin01.adapters.ImageAdapter;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;

import id.zelory.compressor.Compressor;

/**
 * The Class GallarySample.
 */
public class MainActivity extends Activity {

    private static final String TAG = "LOG_ME";

    private RecyclerView mRecyclerViewGallery;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mRecyclerViewGallery = findViewById(R.id.recyclerView_gallery);

        Dexter.withContext(this)
                .withPermissions(
                        Manifest.permission.READ_EXTERNAL_STORAGE
                ).withListener(new MultiplePermissionsListener() {
            @Override
            public void onPermissionsChecked(MultiplePermissionsReport report) {
                getImages();
            }

            @Override
            public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {/* ... */}
        }).check();


    }

    private void getImages() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(this) ;
        mRecyclerViewGallery.setLayoutManager(layoutManager);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(mRecyclerViewGallery.getContext(), layoutManager.getOrientation());
        mRecyclerViewGallery.addItemDecoration(dividerItemDecoration);
        mRecyclerViewGallery.setHasFixedSize(true);
        ArrayList<String> imagePathList = getAllShownImagesPath();
        mRecyclerViewGallery.setAdapter(new ImageAdapter(imagePathList));

        compressImages(imagePathList);

    }

    private void compressImages(final ArrayList<String> imagePathList) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Log.i(TAG, String.format("Images => %s",imagePathList.size()));
                for (int i = 0; i < imagePathList.size(); i++) {
                    File file = new File(imagePathList.get(i));
                    long length = file.length();
                    String s1 = humanReadableByteCountBin(length);
                    try {
                        File compressedImageFile = new Compressor(MainActivity.this).compressToFile(file);
                        long length2 = compressedImageFile.length();
                        String s2 = humanReadableByteCountBin(length2);

                        Log.i(TAG, String.format("%s|%s",s1,s2));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();


    }

    public String humanReadableByteCountBin(long bytes) {
        long absB = bytes == Long.MIN_VALUE ? Long.MAX_VALUE : Math.abs(bytes);
        if (absB < 1024) {
            return bytes + " B";
        }
        long value = absB;
        CharacterIterator ci = new StringCharacterIterator("KMGTPE");
        for (int i = 40; i >= 0 && absB > 0xfffccccccccccccL >> i; i -= 10) {
            value >>= 10;
            ci.next();
        }
        value *= Long.signum(bytes);
        return String.format(Locale.getDefault(), "%.1f %cB", value / 1024.0, ci.current());
    }


    /**
     * Getting All Images Path.
     *
     * @return ArrayList with images Path
     */
    private ArrayList<String> getAllShownImagesPath() {
        Uri uri;
        Cursor cursor;
        int column_index_data, column_index_folder_name;
        ArrayList<String> listOfAllImages = new ArrayList<String>();
        String absolutePathOfImage = null;
        uri = android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI;

        String[] projection = {MediaColumns.DATA,
                MediaStore.Images.Media.BUCKET_DISPLAY_NAME};

        cursor = getContentResolver().query(uri, projection, null,
                null, null);

        column_index_data = cursor.getColumnIndexOrThrow(MediaColumns.DATA);
        column_index_folder_name = cursor
                .getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_DISPLAY_NAME);
        while (cursor.moveToNext()) {
            absolutePathOfImage = cursor.getString(column_index_data);

            listOfAllImages.add(absolutePathOfImage);
        }

        Log.i(TAG, "getAllShownImagesPath: " + listOfAllImages.size());
        return listOfAllImages;
    }

}