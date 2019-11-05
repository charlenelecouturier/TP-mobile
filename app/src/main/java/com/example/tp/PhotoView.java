package com.example.tp;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.os.Build;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;


import androidx.annotation.RequiresApi;

import java.io.File;
import java.nio.file.Paths;
import java.util.ArrayList;

public class PhotoView extends View {
    public  ArrayList<String> urlPhotos;
    public Paint mPaint=new Paint();

    public PhotoView(Context context) {
        super(context);

        urlPhotos=fetchGalleryImages(context);
        /**int i=0;
        for(i=0;i<urlPhotos.size();i++){
            File f = new File(urlPhotos.get(i));
            Bitmap bmp = BitmapFactory.decodeFile(f.getAbsolutePath());
           Canvas canvas = new Canvas(bmp);

            canvas.drawBitmap(bmp, 0,0, mPaint);
        }**/

}

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onDraw(Canvas c) {
        int i;

        for (i = 0; i < urlPhotos.size(); i++) {
            File f = new File(urlPhotos.get(i));
            Bitmap bmp = BitmapFactory.decodeFile(f.getAbsolutePath());
            Bitmap mutableBitmap = bmp.copy(Bitmap.Config.ARGB_8888, true);


            c.drawBitmap(bmp, 0, 0, mPaint);
        }

    }


private  int actual_image_column_index;

    public static ArrayList<String> fetchGalleryImages(Context context) {
        ArrayList<String> galleryImageUrls;
        final String[] columns = {MediaStore.Images.Media.DATA, MediaStore.Images.Media._ID};//get all columns of type images
        final String orderBy = MediaStore.Images.Media.DATE_TAKEN;//order data by date

        Cursor imagecursor = context.getContentResolver().query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI, columns, null,
                null, orderBy + " DESC");//get all data in Cursor by sorting in DESC order

        galleryImageUrls = new ArrayList<String>();

        for (int i = 0; i < imagecursor.getCount(); i++) {
            imagecursor.moveToPosition(i);
            int dataColumnIndex = imagecursor.getColumnIndex(MediaStore.Images.Media.DATA);//get column index
            galleryImageUrls.add(imagecursor.getString(dataColumnIndex));//get Image from column index
            Log.i("URLLLLL",imagecursor.getString(dataColumnIndex));

        }
        Log.i("TESTTTTTTT","images");
        return galleryImageUrls;
    }
}
