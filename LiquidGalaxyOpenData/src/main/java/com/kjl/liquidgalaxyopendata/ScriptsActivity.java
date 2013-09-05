package com.kjl.liquidgalaxyopendata;

import android.os.Bundle;
import android.app.Activity;
import android.os.Environment;
import android.view.Menu;
import android.view.MenuItem;
import android.support.v4.app.NavUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Properties;

public class ScriptsActivity extends Activity {

    ArrayList<String> names = new ArrayList<String>();
    ArrayList<String> scripts = new ArrayList<String>();
    int listPosition;
    String[] params;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.scripts_main);
        // Show the Up button in the action bar.
        setupActionBar();



        final ListView scriptlist = (ListView) findViewById(R.id.scriptlist);
        final ArrayAdapter<String> listAdapter ;
        listAdapter = new ArrayAdapter<String>(this, R.layout.listlayout);


        //get scripts from file and put them on a list
        File path = new File(Environment.getExternalStorageDirectory()+"/LGOD/conf/");
        path.mkdirs();
        File scriptsFile = new File(Environment.getExternalStorageDirectory()+"/LGOD/conf/scripts.conf");
        // if file doesnt exists, then create it
        if (!scriptsFile.exists()) {
            try {
                scriptsFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        InputStream inputStream=null;
        try {
            inputStream= new FileInputStream(scriptsFile);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        String line;
        try {
            while((line = reader.readLine()) != null) {
                String[] strings = line.split(";");
                names.add(strings[0]);
                listAdapter.add(strings[0]);
                scripts.add(strings[1]);
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        scriptlist.setAdapter(listAdapter);

        //click listener
        scriptlist.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> arg0, View v, int position, long id) {
                File file = new File(Environment.getExternalStorageDirectory() + "/LGOD/conf/connection.conf");
                listPosition = position;

                if (file.exists()) {
                    FileInputStream inputStream = null;
                    try {
                        inputStream = new FileInputStream(file);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                    BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                    try {
                        params = reader.readLine().split(";");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                new Thread(new Runnable() {
                    public void run() {
                        //gets the file content as a string
                        try {
                            //String command = "./"+scripts.get(listPosition);
                          String command = "/home/lg/lg-relaunch";

                            executeRemoteCommand(params[0], params[1], params[2], 22, command);        //executes the given command

                            executeRemoteCommand(params[0], params[1], params[2], 22, "echo '"+command+"' >> lglog.txt");    //test

                        } catch (Exception e) {
                            e.printStackTrace();
                            TextView tv1 = (TextView) findViewById(R.id.textView);
                            tv1.append("exeption: " + e.getMessage()); //here the app would crash since can't print from the thread
                        }
                    }
                }).start();
            }
        });
    }


    public String executeRemoteCommand(
            final String username,
            final String password,
            final String hostname,
            int port,
            final String command) throws Exception {

        JSch jsch = new JSch();
        String privateKey = Environment.getExternalStorageDirectory()+"/LGOD/conf/lg-id_rsa";
        jsch.addIdentity(privateKey);

        Session session = jsch.getSession(username, hostname, port);
        session.setPassword(password);


        // Avoid asking for key confirmation
        Properties prop = new Properties();
        prop.put("StrictHostKeyChecking", "no");
        session.setConfig(prop);

        session.connect();

        // SSH Channel
        ChannelExec channelssh = (ChannelExec)
                session.openChannel("exec");
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        channelssh.setOutputStream(baos);

        // Execute command
        channelssh.setCommand(command);
        channelssh.connect();
        channelssh.disconnect();
        session.disconnect();
//close session?
        return baos.toString();
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
        getMenuInflater().inflate(R.menu.scripts, menu);
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
