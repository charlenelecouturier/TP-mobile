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

    private static float MIN_ZOOM =1f;
    private static float MAX_ZOOM = 5f;

    //These constants specify the mode that we're in
    private static int NONE = 0;
    private static int DRAG = 1;
    private static int ZOOM = 2;

    private int mode;

//These two variables keep track of the X and Y coordinate of the finger when it first
        //touches the screen
    private float startX = 0f;
    private float startY = 0f;

    //These two variables keep track of the amount we need to translate the canvas along the X
        //and the Y coordinate
    private float translateX = 0f;
    private float translateY = 0f;

    //These two variables keep track of the amount we translated the X and Y coordinates, the last time we
        //panned.
    private float previousTranslateX = 0f;
    private float previousTranslateY = 0f;
    float xCanvas=0;
    float yCanvas=0;

    private ArrayList<Bitmap>listBitmap;

    public PhotoView(Context context) {
        super(context);
        scaleDetector = new ScaleGestureDetector(getContext(),new ScaleListener());
        urlPhotos=fetchGalleryImages(context);
        listBitmap = new ArrayList<Bitmap>();
        for (int i = 0; i < urlPhotos.size() && i<16; i++) {

            File f = new File(urlPhotos.get(i));
            BitmapFactory.Options option = new BitmapFactory.Options();
            option.inSampleSize = 16;

            Bitmap bmp = BitmapFactory.decodeFile(f.getAbsolutePath(),option);
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
        scaleDetector = new ScaleGestureDetector(getContext(), new ScaleListener());

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
        float xactu = xCanvas;
         for (int i = 0; i< listBitmap.size();i++){
             xBmp= listBitmap.get(i).getWidth();
             ybmp = listBitmap.get(i).getHeight();
             if (xCanvas+xBmp>1080){
                 xCanvas = xactu;
                 yCanvas += ybmp;
             }
             c.drawBitmap(listBitmap.get(i),xCanvas, yCanvas,mPaint);
             xCanvas += xBmp;
         }

        c.save();
        //We're going to scale the X and Y coordinates by the same amount
        c.scale(this.scaleFactor, this.scaleFactor, this.scaleDetector.getFocusX(), this.scaleDetector.getFocusY());

        //If translateX times -1 is lesser than zero, let's set it to zero. This takes care of the left bound

        if((translateX * -1) < 0) {

            translateX = 0;

        }

        //This is where we take care of the right bound. We compare translateX times -1 to (scaleFactor - 1) * displayWidth.

        //If translateX is greater than that value, then we know that we've gone over the bound. So we set the value of

        //translateX to (1 - scaleFactor) times the display width. Notice that the terms are interchanged; it's the same

        //as doing -1 * (scaleFactor - 1) * displayWidth
        else if((translateX * -1) > (scaleFactor - 1) * displayWidth) {

            translateX = (1 - scaleFactor) * displayWidth;
        }
        if(translateY * -1 < 0) {

            translateY = 0;
        }
        //We do the exact same thing for the bottom bound, except in this case we use the height of the display
        else if((translateY * -1) > (scaleFactor - 1) * displayHeight) {

            translateY = (1 - scaleFactor) * displayHeight;
        }
        //We need to divide by the scale factor here, otherwise we end up with excessive panning based on our zoom level
        //because the translation amount also gets scaled according to how much we've zoomed into the canvas.
        c.translate(translateX / scaleFactor, translateY / scaleFactor);

        /* The rest of your canvas-drawing code */
        c.restore();
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
        return galleryImageUrls;
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // TODO Auto-generated method stub

        switch(event.getAction()){
            case MotionEvent.ACTION_DOWN:
                xCanvas = event.getX();
                yCanvas = event.getY();
                invalidate();
                break;
            case MotionEvent.ACTION_MOVE:
                xCanvas = event.getX();
                yCanvas = event.getY();
                invalidate();
                break;
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
            scaleFactor = Math.max(MIN_ZOOM, Math.min(scaleFactor, MAX_ZOOM));
            invalidate();
            return true;
        }
    }


}
