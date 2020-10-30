package com.alirnp.goblin01.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ImageWriter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.alirnp.goblin01.R;
import com.bumptech.glide.Glide;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.CharacterIterator;
import java.text.StringCharacterIterator;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Locale;

import javax.xml.transform.Templates;

import id.zelory.compressor.Compressor;

public class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.Holder> {

    ArrayList<String> imagesPath;

    public ImageAdapter(ArrayList<String> imagesPath) {
        this.imagesPath = imagesPath;
    }

    @NonNull
    @Override
    public ImageAdapter.Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_image, parent, false);
        return new Holder(view);
    }

    private Context context ;
    private static final String TAG = "LOG_ME";

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        this.context = recyclerView.getContext();
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        holder.bind(imagesPath.get(position));
    }


    @Override
    public int getItemCount() {
        if (imagesPath == null) return 0;
        return imagesPath.size();
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

    class Holder extends RecyclerView.ViewHolder {

        private ImageView mImageViewPicture;
        private TextView mTextViewImageSize;

        public Holder(@NonNull final View itemView) {
            super(itemView);

            mImageViewPicture = itemView.findViewById(R.id.imageView_image);
            mTextViewImageSize = itemView.findViewById(R.id.textView_imageSize);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(
                            itemView.getContext(),
                            "position " + getAbsoluteAdapterPosition() + " " + imagesPath.get(getAbsoluteAdapterPosition()),
                            Toast.LENGTH_LONG).show();
                }
            });

        }

        void bind(String path) {

            Glide.with(itemView.getContext()).load(path)
                    .placeholder(R.drawable.ic_launcher_foreground).centerCrop()
                    .into(mImageViewPicture);

            File file = new File(path);
            try {
                file = new Compressor(itemView.getContext()).compressToFile(file);
            } catch (IOException e) {
                e.printStackTrace();
            }
            long length = file.length();
            mTextViewImageSize.setText(humanReadableByteCountBin(length));

        }

        public File saveBitmapToFile(File file) {
            try {

                // BitmapFactory options to downsize the image
                BitmapFactory.Options o = new BitmapFactory.Options();
                o.inJustDecodeBounds = true;
                o.inSampleSize = 6;
                // factor of downsizing the image

                FileInputStream inputStream = new FileInputStream(file);
                //Bitmap selectedBitmap = null;
                BitmapFactory.decodeStream(inputStream, null, o);
                inputStream.close();

                // The new size we want to scale to
                final int REQUIRED_SIZE = 75;

                // Find the correct scale value. It should be the power of 2.
                int scale = 1;
                while (o.outWidth / scale / 2 >= REQUIRED_SIZE &&
                        o.outHeight / scale / 2 >= REQUIRED_SIZE) {
                    scale *= 2;
                }

                BitmapFactory.Options o2 = new BitmapFactory.Options();
                o2.inSampleSize = scale;
                inputStream = new FileInputStream(file);

                Bitmap selectedBitmap = BitmapFactory.decodeStream(inputStream, null, o2);
                inputStream.close();

                // here i override the original image file
                file.createNewFile();
                FileOutputStream outputStream = new FileOutputStream(file);

                selectedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);

                return file;
            } catch (Exception e) {
                return null;
            }
        }
    }

}
