package com.kjl.liquidgalaxyopendata;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.net.URLConnection;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import android.util.Log;

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

                NavigationDataSet navigationDataSet = null;
                try
                {
                /* Get a SAXParser from the SAXPArserFactory. */
                    SAXParserFactory spf = SAXParserFactory.newInstance();
                    SAXParser sp = spf.newSAXParser();

                /* Get the XMLReader of the SAXParser we created. */
                    XMLReader xr = sp.getXMLReader();

                /* Create a new ContentHandler and apply it to the XML-Reader*/
                    NavigationSaxHandler navSax2Handler = new NavigationSaxHandler();
                    xr.setContentHandler(navSax2Handler);

                /* Parse the xml-data from our URL. */
                    File kmlfile=new File( Environment.getExternalStorageDirectory() + "/LGOD/parades_bus.kml");
                    InputStream inputStream= new FileInputStream(kmlfile);
                    Reader reader = new InputStreamReader(inputStream,"UTF-8");

                    InputSource is = new InputSource(reader);
                    //is.setEncoding("UTF-8");

                    xr.parse(is);

                /* Our NavigationSaxHandler now provides the parsed data to us. */
                    navigationDataSet = navSax2Handler.getParsedData();

                /* Set the result to be displayed in our GUI. */
                    //Log.d(myapp.APP,"navigationDataSet: "+navigationDataSet.toString());

                } catch (Exception e) {
                    // Log.e(myapp.APP, "error with kml xml", e);
                    navigationDataSet = null;
                }

                //return navigationDataSet;
                TextView tv = (TextView)findViewById(R.id.textView);
                /*tv.append(navigationDataSet.getPlacemarks().get(1).getTitle());
                tv.append(navigationDataSet.getPlacemarks().get(1).getDescription());
                tv.append(navigationDataSet.getPlacemarks().get(2).getTitle());*/

                tv.append(navigationDataSet.toString());

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
