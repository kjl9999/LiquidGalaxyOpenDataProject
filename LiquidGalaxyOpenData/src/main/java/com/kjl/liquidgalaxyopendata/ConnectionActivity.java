package com.kjl.liquidgalaxyopendata;

import android.os.Bundle;
import android.app.Activity;
import android.os.Environment;
import android.provider.ContactsContract;
import android.view.Menu;
import android.view.MenuItem;
import android.support.v4.app.NavUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;




public class ConnectionActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connection);
        // Show the Up button in the action bar.
        setupActionBar();

        //getting stored connection info
        EditText user = (EditText) findViewById(R.id.useret);
        EditText pwd = (EditText) findViewById(R.id.pwdet);
        EditText ip = (EditText) findViewById(R.id.ipet);
        EditText ssh = (EditText) findViewById(R.id.sshet);
        EditText query = (EditText) findViewById(R.id.queryet);
        EditText kmlstxt = (EditText) findViewById(R.id.kmlstxtet);
        EditText kmls = (EditText) findViewById(R.id.kmlset);
        EditText kmlsurl = (EditText) findViewById(R.id.kmlsurlet);

        File file = new File(Environment.getExternalStorageDirectory()+"/LGOD/conf/connection.conf");

        if (file.exists()){
            FileInputStream inputStream=null;

            try {
                inputStream = new FileInputStream(file);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }

            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

            try {
                String line = reader.readLine();
                String[] params = line.split(";");
                user.setText(params[0]);
                pwd.setText(params[1]);
                ip.setText(params[2]);
                ssh.setText(params[3]);
                query.setText(params[4]);
                kmlstxt.setText(params[5]);
                kmls.setText(params[6]);
                kmlsurl.setText(params[7]);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        Button savebutton = (Button) findViewById(R.id.BtnNext);
        savebutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                EditText user = (EditText) findViewById(R.id.useret);
                EditText pwd = (EditText) findViewById(R.id.pwdet);
                EditText ip = (EditText) findViewById(R.id.ipet);
                EditText ssh = (EditText) findViewById(R.id.sshet);
                EditText query = (EditText) findViewById(R.id.queryet);
                EditText kmlstxt = (EditText) findViewById(R.id.kmlstxtet);
                EditText kmls = (EditText) findViewById(R.id.kmlset);
                EditText kmlsurl = (EditText) findViewById(R.id.kmlsurlet);

                if(user.getText().toString().matches("") ){
                    Toast.makeText(getApplicationContext(),"Enter a user.",Toast.LENGTH_LONG);
                }
                else if(pwd.getText().toString().matches("") ){
                    Toast.makeText(getApplicationContext(),"Enter a password.",Toast.LENGTH_LONG);
                }
                else if(ip.getText().toString().matches("") ){
                    Toast.makeText(getApplicationContext(),"Enter an IP.",Toast.LENGTH_LONG);
                }
                else if(validIP(pwd.getText().toString())){
                    Toast.makeText(getApplicationContext(),"Enter a valid IP.",Toast.LENGTH_LONG);
                }
                else {
                    try {
                        File file = new File(Environment.getExternalStorageDirectory()+"/LGOD/conf/");
                        file.mkdirs();
                        file = new File(Environment.getExternalStorageDirectory()+"/LGOD/conf/connection.conf");

                        if (!file.exists()) {
                            file.createNewFile();
                        }

                        FileWriter fw = new FileWriter(file.getAbsoluteFile());
                        BufferedWriter bw = new BufferedWriter(fw);
                        bw.write(user.getText().toString() + ";"
                                + pwd.getText().toString() + ";"
                                + ip.getText().toString() + ";"
                                + ssh.getText().toString() + ";"
                                + query.getText().toString() + ";"
                                + kmlstxt.getText().toString() + ";"
                                + kmls.getText().toString() + ";"
                                + kmlsurl.getText().toString()); //user;pwd;ip
                        bw.close();

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                NavUtils.navigateUpFromSameTask(ConnectionActivity.this);
            }
        });

        Button discardbutton = (Button) findViewById(R.id.BtnBack);
        discardbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NavUtils.navigateUpFromSameTask(ConnectionActivity.this);
            }
        });

    }

    public static boolean validIP(String ip) {
        if (ip == null || ip.isEmpty()) return false;
        ip = ip.trim();
        if ((ip.length() < 6) & (ip.length() > 15)) return false;

        try {
            Pattern pattern = Pattern.compile("^(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$");
            Matcher matcher = pattern.matcher(ip);
            return matcher.matches();
        } catch (PatternSyntaxException ex) {
            return false;
        }
    }

    /**
     * Set up the {@link android.app.ActionBar}.
     */
    private void setupActionBar() {
        
        getActionBar().setDisplayHomeAsUpEnabled(true);
        
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.connection, menu);
        return true;
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
                NavUtils.navigateUpFromSameTask(this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
