package com.kjl.liquidgalaxyopendata;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.view.MenuItem;
import android.support.v4.app.NavUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class SettingsActivity extends Activity {

    private static final boolean ALWAYS_SIMPLE_PREFS = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupActionBar();
        setContentView(R.layout.settings);

        //DATA SOURCES
        final ListView dslist = (ListView) findViewById(R.id.datasourcelist);
        final ArrayAdapter<String> listAdapter ;
        // Create ArrayAdapter using the planet list.
        listAdapter = new ArrayAdapter<String>(this, R.layout.listlayout);
        listAdapter.add(getString(R.string.newds));
        listAdapter.add(getString(R.string.manageds));
        dslist.setAdapter(listAdapter);

        dslist.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            public void onItemClick(AdapterView<?> arg0, View v, int position, long id)
            {

                if(position==0){

                    Intent intent;
                    intent = new Intent(getApplicationContext() , newurl.class);
                    startActivity(intent);

                }

                else{
                    AlertDialog.Builder adb = new AlertDialog.Builder(
                            SettingsActivity.this);
                    adb.setTitle("ListView OnClick");
                    adb.setMessage("Selected Item is = " + dslist.getItemAtPosition(position));
                    adb.setPositiveButton("Ok", null);
                    adb.show();
                }
            }
        });


        //LG
        final ListView lglist = (ListView) findViewById(R.id.lglist);
        final ArrayAdapter<String> listAdapter2;
        // Create ArrayAdapter using the planet list.
        listAdapter2 = new ArrayAdapter<String>(this, R.layout.listlayout);
        listAdapter2.add(getString(R.string.connect));
        lglist.setAdapter(listAdapter2);

        lglist.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            public void onItemClick(AdapterView<?> arg0, View v, int position, long id)
            {
                Intent intent;
                intent = new Intent(getApplicationContext() , ConnectionActivity.class);
                startActivity(intent);
            }
        });



        //SECURITY
        final ListView pwdlist = (ListView) findViewById(R.id.pwdlist);
        final ArrayAdapter<String> listAdapter3;
        // Create ArrayAdapter using the planet list.
        listAdapter3 = new ArrayAdapter<String>(this, R.layout.listlayout);
        listAdapter3.add(getString(R.string.pwd));
        pwdlist.setAdapter(listAdapter3);

        pwdlist.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            public void onItemClick(AdapterView<?> arg0, View v, int position, long id)
            {
                AlertDialog.Builder adb = new AlertDialog.Builder(
                        SettingsActivity.this);
                adb.setTitle("ListView OnClick");
                adb.setMessage("Selected Item is = " + pwdlist.getItemAtPosition(position));
                adb.setPositiveButton("Ok", null);
                adb.show();
            }
        });



    }

    /**
     * Set up the {@link android.app.ActionBar}, if the API is available.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private void setupActionBar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            // Show the Up button in the action bar.
            getActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                // This ID represents the Home or Up button. In the case of this
                // activity, the Up button is shown. Use NavUtils to allow users
                // to navigate up one level in the application structure. For
                // more details, see the Navigation pattern on Android Design:
                //
                // http://developer.android.com/design/patterns/navigation.html#up-vs-back
                //
                // TODO: If Settings has multiple levels, Up should navigate up
                // that hierarchy.
                NavUtils.navigateUpFromSameTask(this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
