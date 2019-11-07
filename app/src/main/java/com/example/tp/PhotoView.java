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
import java.util.ArrayList;

/**
 *  class PhotoView
 *  herite de la classe View
 *  possède des attributs Paint, ScaleGestureDetector pour la gestion de l'affichage et du zomm sur les photos
 *  permet de recuperer et afficher les photos de l'appareil, de zoomer sur les photos et de scroller l'ecran
 */
public class PhotoView extends View {
    public  ArrayList<String> urlPhotos;
    public Paint mPaint=new Paint();
    private ScaleGestureDetector scaleDetector;
    private float scaleFactor = 1.f;
    private float prevScaleFactor = 1.f;
    private boolean zoom = false;
private int displayWidth,displayHeight;

    private int mode;

    float xCanvas=0;
    float yCanvas=0;
    private ArrayList<Bitmap>OGBitmap;
    private ArrayList<Bitmap>listBitmap;
    private Bitmap bmp;

    /**
     * Contructeur de la classe PhotoView
     * Initialise les attributs
     * Appelle la fonction fetchGalleryImages pour recuperer tous les chemins vers les photos de l'appareil
     * Génère une liste de Bitmap redimensionnés pour ne pas saturer la mémoire et pour pouvoir en afficher 7 par lignes
     * @param context
     */
    public PhotoView(Context context) {
        super(context);
        scaleDetector = new ScaleGestureDetector(context,new ScaleListener());
        urlPhotos=fetchGalleryImages(context);
        listBitmap = new ArrayList<Bitmap>();
        OGBitmap = new ArrayList<Bitmap>();

        for (int i = 0; i < urlPhotos.size() && i<16; i++) { //on limite à 16 pour la fluidité

            File f = new File(urlPhotos.get(i));
            BitmapFactory.Options option = new BitmapFactory.Options();
            option.inSampleSize = 16;
            Bitmap bmpOG = BitmapFactory.decodeFile(f.getAbsolutePath());
            bmp = BitmapFactory.decodeFile(f.getAbsolutePath(),option); //instanciation du bitmap en donction de son Path sur l'appareil
            OGBitmap.add(bmp);

            WindowManager wm = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
            Display display = wm.getDefaultDisplay();
            Point size = new Point();
            display.getSize(size);
          displayWidth = size.x; //largeur de l'ecram
            displayHeight = size.y;
            Bitmap resized = Bitmap.createScaledBitmap(bmp,displayWidth/7, 120, true); //on redimensionne les BitMaps
            listBitmap.add(resized); //on ajoute le bitmap redimensionne à la liste des BitMaps
        }



}

    /**
     * Fonction qui gère l'affichage des Photos dans un Canvas à l'ecran, associé à l'object Paint mPaint
     * @param c  Canvas
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onDraw(Canvas c) {
        super.onDraw(c);
        int xBmp;
        int ybmp;

         for (int i = 0; i< listBitmap.size();i++){
             xBmp= listBitmap.get(i).getWidth();
             ybmp = listBitmap.get(i).getHeight();
             if (xCanvas+xBmp>displayWidth){ //si l'image depasse la largeur de l'ecran on retourne à la ligne
                 xCanvas = 0;
                 yCanvas += ybmp;
             }
             c.drawBitmap(listBitmap.get(i),xCanvas, yCanvas,mPaint);
             xCanvas += xBmp;
         }

        zoom = false;
    }

    /**
     * Methode aqui permet de trouver et stocker dans une ArrayListe<String> toutes les URL des images présentes sur le téléphone
     * @param context Context
     * @return ArrayList<String> la liste des URL de toutes les images de la gallerie de l'appareil
     */
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
            Log.i("PATH",imagecursor.getString(dataColumnIndex));  // on log le chemin de l'image

        }
        return galleryImageUrls;
    }

    /**
     * Fonction qui intercepte un TouchEvent
     * On va appeler la fonction onTouchEvent du scaleDetector pour gérer le zoom
     * @param event
     * @return boolean
     */
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


    /**
     * class ScaleListner
     * Permet de créer un ScaleGestureDestector qui va detecter si une action de zoom est effectuée
     *
     */
    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {

        /**
         *fonction qui permet de zoomer ou dezoomer les images en fonction de si l'utilisateur agrandit ou retrecit le scalefFactor
         * @param detector ScaleGestureDetector
         * @return
         */
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            scaleFactor *= detector.getScaleFactor();
            zoom = true;
            Log.i("OG", String.valueOf(scaleFactor));
            listBitmap.clear();
            if(scaleFactor>prevScaleFactor){ //si on a zoomé
                for (int i =0; i<OGBitmap.size();i++){
                    Bitmap resized = Bitmap.createScaledBitmap(OGBitmap.get(i),displayWidth,displayHeight/2   , true); //on agrandit les photos
                    listBitmap.add(resized);

                }

            }else{
                for (int i =0; i<OGBitmap.size();i++){//sinon on revient a la taille initiale des photos
                    Bitmap resized = Bitmap.createScaledBitmap(OGBitmap.get(i),displayWidth/7, 120, true);
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
