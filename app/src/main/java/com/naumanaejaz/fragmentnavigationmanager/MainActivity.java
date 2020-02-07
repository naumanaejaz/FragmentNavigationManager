package com.naumanaejaz.fragmentnavigationmanager;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;

import com.naumanaejaz.fragmentnavigationmanager.lib.FragmentNavigationManager;

public class MainActivity extends AppCompatActivity {

    protected FragmentNavigationManager fragmentNavigationManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        fragmentNavigationManager = new FragmentNavigationManager(getSupportFragmentManager(), android.R.id.content, true);
    }

    @Override
    public void onBackPressed() {
        if(!fragmentNavigationManager.onBackPressed()) {
            new AlertDialog.Builder(this)
                    .setTitle("")
                    .setMessage("Quit app?")
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            MainActivity.super.onBackPressed();
                        }
                    })
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    }).show();
        }
    }
}
