package com.kjl.liquidgalaxyopendata;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.ekito.simpleKML.model.Kml;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class MainActivity extends Activity {

    private final String TAG = "MainActivity";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final TextView maintext = (TextView) findViewById(R.id.textView);
        Button button= (Button) findViewById(R.id.refresh);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //File kmlfile=new File( Environment.getExternalStorageDirectory() + "/LGOD/parades_bus.kml");
                //String kmlfilepath=kmlfile.getAbsolutePath();
                //new ParsingTask().execute(kmlfilepath);

                com.ekito.simpleKML.Serializer kmlSerializer;
                kmlSerializer = new com.ekito.simpleKML.Serializer();
                Log.d(TAG, "read started");
                // this will create a Kml class based on the informations described in params[0] (assets/test.kml)
                Kml kml = null;
                try {
                    InputStream is = new FileInputStream(new File( Environment.getExternalStorageDirectory() + "/LGOD/parades_bus.kml"));
                    Log.d(TAG, "parsing started");
                    kml = kmlSerializer.read(is);
                    Log.d(TAG, "parsing done");

                } catch (IOException e) {
                    Log.e(TAG, e.getMessage());
                } catch (Exception e) {
                    Log.e(TAG, e.getMessage());
                }
                Log.d(TAG, "read done");

                if (kml != null) {
                    Log.d(TAG, "write started");
                    // this will output the KML to /data/data/com.ekito.simplekmldemo/example_out.kml
                    File out = new File(getDir("assets", Context.MODE_PRIVATE), "test.kml");
                    try {
                        kmlSerializer.write(kml, out);
                    } catch (Exception e) {
                        Log.e(TAG, e.getMessage());
                    }
                    Log.d(TAG, "write done");
                }
                maintext.append(kml.getFeature().getName() + "\n" + kml.getFeature().getDescription() + "\n" + kml.getFeature().getAddress());

            }
        });


        TextView tv = (TextView)findViewById(R.id.textView);

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {

            case R.id.menu_settings:
                Intent intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
                break;

            default:
                break;
        }

        return true;
    }


}
