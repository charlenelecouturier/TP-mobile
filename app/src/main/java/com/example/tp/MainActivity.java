package com.example.tp;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.app.Activity;

import android.content.Intent;
import android.os.Bundle;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;


/**
 * class MainActivity
 * Lorsqu'elle est créée, on verifie les permissions d'accéder aux photos de la gallery
 * on charge la vue PhotoView si la permission est accordée
 */
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(isStoragePermissionGranted()) {

            PhotoView view = new PhotoView(this);
            setContentView(view);
        }

    }

    /**
     *  Fonction qui permet de demander la permission d'acceder aux photos
     *
     * @return boolean, true si permission accordée, false sinon
     */
    public  boolean isStoragePermissionGranted() {
        String TAG = "logcheck";
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE) //si la permission est accordée
                    == PackageManager.PERMISSION_GRANTED ) {
                Log.v(TAG,"Permission is granted");
                return true;
            } else { //sinon

                Log.v(TAG,"Permission is revoked");
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
                return false;
            }
        }
        else { //permission is automatically granted on sdk<23 upon installation
            Log.v(TAG,"Permission is granted");
            return true;
        }
    }

    /**
     * Fonction qui gère le resultat de la permission d'accéder aux photos
     * si elle est accordée, on relance l'activité
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults){

        boolean flag;
        flag = true;
        if(requestCode == 1)
        {
            for (int i:grantResults)
            {
                if( i == PackageManager.PERMISSION_DENIED)
                    flag = false;

            }
            if (flag)
            {
                Intent intent = new Intent(this,MainActivity.class);
                finish();
                startActivity(intent);
            }
        }
    }
}
