package es.usc.citius.servando.calendula.activities;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import es.usc.citius.servando.calendula.HomePagerActivity;


public class StartActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        startActivity(new Intent(this,HomePagerActivity.class));
        finish();
    }
}
