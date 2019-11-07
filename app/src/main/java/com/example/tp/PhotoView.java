package com.example.tp;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.os.Build;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Display;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.WindowManager;


import androidx.annotation.RequiresApi;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.nio.file.Paths;
import java.util.ArrayList;

public class PhotoView extends View {
    public  ArrayList<String> urlPhotos;
    public Paint mPaint=new Paint();
    private ScaleGestureDetector scaleDetector;
    private float scaleFactor = 1.f;
    private float prevScaleFactor = 1.f;
    private boolean zoom = false;


    private int mode;

    float xCanvas=0;
    float yCanvas=0;
    private ArrayList<Bitmap>OGBitmap;
    private ArrayList<Bitmap>listBitmap;
    private Bitmap bmp;

    public PhotoView(Context context) {
        super(context);
        scaleDetector = new ScaleGestureDetector(context,new ScaleListener());

        urlPhotos=fetchGalleryImages(context);
        listBitmap = new ArrayList<Bitmap>();
        OGBitmap = new ArrayList<Bitmap>();
        for (int i = 0; i < urlPhotos.size() && i<16; i++) {

            File f = new File(urlPhotos.get(i));
            BitmapFactory.Options option = new BitmapFactory.Options();
            option.inSampleSize = 16;
            Bitmap bmpOG = BitmapFactory.decodeFile(f.getAbsolutePath());
            bmp = BitmapFactory.decodeFile(f.getAbsolutePath(),option);
            OGBitmap.add(bmpOG);
            Bitmap resized = Bitmap.createScaledBitmap(bmp,216, 200, true);
            listBitmap.add(resized);
        }


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
    protected void onDraw(Canvas c) {
        super.onDraw(c);
        int xBmp;
        int ybmp;
        WindowManager wm = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int displayWidth = size.x;
        int displayHeight = size.y;

         /*   for (int i = 0; i < urlPhotos.size() && i<16; i++) {

                File f = new File(urlPhotos.get(i));
                BitmapFactory.Options option = new BitmapFactory.Options();
                option.inSampleSize = 16;

                Bitmap bmp = BitmapFactory.decodeFile(f.getAbsolutePath(),option);
                Bitmap resized = Bitmap.createScaledBitmap(bmp,216, 200, true);

                xBmp=resized.getWidth();
                ybmp=resized.getHeight();
                if(xCanvas+xBmp>1080){
                    xCanvas=0;
                    yCanvas+=ybmp;
                }
                c.drawBitmap(resized, xCanvas, yCanvas, mPaint);
                xCanvas+=xBmp;

            } */
         for (int i = 0; i< listBitmap.size();i++){
             xBmp= listBitmap.get(i).getWidth();
             ybmp = listBitmap.get(i).getHeight();
             if (xCanvas+xBmp>displayWidth){
                 xCanvas = 0;
                 yCanvas += ybmp;
             }
             c.drawBitmap(listBitmap.get(i),xCanvas, yCanvas,mPaint);
             xCanvas += xBmp;
         }

        zoom = false;
    }


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
        return galleryImageUrls;
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        scaleDetector.onTouchEvent(event);
        int pointerCount = event.getPointerCount();
        switch(event.getAction()){

            case MotionEvent.ACTION_MOVE:
            if(!zoom ){
                xCanvas = 0;
                yCanvas = event.getY();

                invalidate();
                break;
                }

        }
        return true;
    }



   /* public boolean onTouchEvent(MotionEvent event) {
        boolean dragged=false;
        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:

                mode = DRAG;

                //We assign the current X and Y coordinate of the finger to startX and startY minus the previously translated

                //amount for each coordinates This works even when we are translating the first time because the initial

                //values for these two variables is zero.

                startX = event.getX() - previousTranslateX;

                startY = event.getY() - previousTranslateY;

                break;

            case MotionEvent.ACTION_MOVE:

                translateX = event.getX() - startX;

                translateY = event.getY() - startY;

                //We cannot use startX and startY directly because we have adjusted their values using the previous translation values.

                //This is why we need to add those values to startX and startY so that we can get the actual coordinates of the finger.

                double distance = Math.sqrt(Math.pow(event.getX() - (startX + previousTranslateX), 2) +

                        Math.pow(event.getY() - (startY + previousTranslateY), 2)

                );

                if(distance > 0) {

                    dragged = true;

                }



                break;

            case MotionEvent.ACTION_POINTER_DOWN:

                mode = ZOOM;

                break;

            case MotionEvent.ACTION_UP:

                mode = NONE;

                dragged = false;

                //All fingers went up, so let's save the value of translateX and translateY into previousTranslateX and

                //previousTranslate

                previousTranslateX = translateX;

                previousTranslateY = translateY;

                break;



            case MotionEvent.ACTION_POINTER_UP:
                mode = DRAG;

                //This is not strictly necessary; we save the value of translateX and translateY into previousTranslateX

                //and previousTranslateY when the second finger goes up

                previousTranslateX = translateX;

                previousTranslateY = translateY;

                break;

        }

        scaleDetector.onTouchEvent(event);

        //We redraw the canvas only in the following cases:

        //

        // o The mode is ZOOM

        //        OR

        // o The mode is DRAG and the scale factor is not equal to 1 (meaning we have zoomed) and dragged is

        //   set to true (meaning the finger has actually moved)

        if ((mode == DRAG && scaleFactor != 1f && dragged) || mode == ZOOM) {
            invalidate();
        }
        return true;
    } */

    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            scaleFactor *= detector.getScaleFactor();
            zoom = true;
            Log.i("OG", String.valueOf(scaleFactor));
            listBitmap.clear();
            if(scaleFactor>prevScaleFactor){
                for (int i =0; i<OGBitmap.size();i++){
                    Bitmap resized = Bitmap.createScaledBitmap(OGBitmap.get(i),OGBitmap.get(i).getWidth(), OGBitmap.get(i).getHeight(), true);
                    listBitmap.add(resized);

                }

            }else{
                for (int i =0; i<OGBitmap.size();i++){
                    Bitmap resized = Bitmap.createScaledBitmap(OGBitmap.get(i),216, 200, true);
                    listBitmap.add(resized);
                }
            }
            prevScaleFactor = scaleFactor;
            xCanvas = 0;
            yCanvas = 0;
            invalidate();
            return true;
        }
    }


}
