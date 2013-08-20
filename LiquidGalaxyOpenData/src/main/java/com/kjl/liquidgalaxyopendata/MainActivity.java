package com.kjl.liquidgalaxyopendata;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.service.textservice.SpellCheckerService;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Scanner;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import android.util.Log;

import com.jcraft.jsch.*;

public class MainActivity extends Activity {

    private final String TAG = "MainActivity";
    String[] params;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final TextView maintext = (TextView) findViewById(R.id.textView);

        showAllData();

        Button button= (Button) findViewById(R.id.refresh);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //showAllData();

                //testing ssh

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
                        params = reader.readLine().split(";");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                new Thread(new Runnable() {
                    public void run() {


                        String text="";
                        try {

                            BufferedReader br = new BufferedReader(new FileReader(Environment.getExternalStorageDirectory()+"/LGOD/educacio_primaria.kml"));
                            try {
                                StringBuilder sb = new StringBuilder();
                                String line = br.readLine();

                                while (line != null) {
                                    sb.append(line);
                                    sb.append('\n');
                                    line = br.readLine();
                                }
                                text = sb.toString();
                            } finally {
                                br.close();
                            }


                            sendFile("educacio2.kml");
                            executeRemoteCommand(params[0], params[1], params[2], 22, "googleearth /home/lg/hostaleria_LGOD.kml");

                            //executeRemoteCommand(params[0], params[1], params[2], 22, "echo '"+text+"' > /home/lg/educacio2.kml");  //dynamic version (port still hardcoded to 22)

                            //uploadFile(params[0], params[1], params[2], 22);  //dynamic version (port still hardcoded to 22)
                            //executeRemoteCommand(params[0], params[1], params[2], 22, "echo 'search=pobla de segur' > /tmp/query.txt");  //dynamic version (port still hardcoded to 22)
                            //executeRemoteCommand("lg", "pinballmod", "10.42.42.1", 22,"echo 'search=Pobla de segur' > /tmp/query.txt"); //hardcoded version
                        } catch (Exception e) {
                            e.printStackTrace();
                            TextView tv1 = (TextView) findViewById(R.id.textView);
                            tv1.append("exeption: "+e.getMessage()); //here the app would crash since can't print from the thread
                        }
                    }
                }).start();
                //end testing ssh


                //generating keys is no longer used

            }
        });
    }
    public void sendFile(final String localFile){
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
                params = reader.readLine().split(";");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        new Thread(new Runnable() {
            public void run() {

                String text="";
                try {
                    BufferedReader br = new BufferedReader(new FileReader(Environment.getExternalStorageDirectory()+"/LGOD/"+localFile));
                    //reads the file's content
                    try {
                        StringBuilder sb = new StringBuilder();
                        String line = br.readLine();
                        while (line != null) {
                            sb.append(line);
                            sb.append('\n');
                            line = br.readLine();
                        }
                        text = sb.toString();
                    } finally {
                        br.close();
                    }
                    //sends the file's content
                    executeRemoteCommand(params[0], params[1], params[2], 22, "echo '"+text+"' > /home/lg/"+localFile);  //dynamic version (port still hardcoded to 22)
                } catch (Exception e) {
                    e.printStackTrace();
                    TextView tv1 = (TextView) findViewById(R.id.textView);
                    tv1.append("exeption: "+e.getMessage()); //here the app would crash since can't print from the thread
                }
            }
        }).start();
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

        return baos.toString();
    }

public void generateKeys(){

    TextView tv = (TextView)findViewById(R.id.textView);

    String _type="rsa";
    int type=0;
    if(_type.equals("rsa")){type=KeyPair.RSA;}
    else if(_type.equals("dsa")){type=KeyPair.DSA;}
    else {
        System.err.println(
                "usage: java KeyGen rsa output_keyfile comment\n"+
                        "       java KeyGen dsa  output_keyfile comment");
        System.exit(-1);
    }
    String filename=Environment.getExternalStorageDirectory()+"/LGOD/conf/lg-id_rsa";
    String comment="Liquid Galaxy";
    JSch jsch=new JSch();


    String passphrase="";

    try{
        KeyPair kpair=KeyPair.genKeyPair(jsch, type,4096);
        kpair.setPassphrase(passphrase);

        kpair.writePrivateKey(filename);
        kpair.writePublicKey(filename+".pub", comment);
        System.out.println("Finger print: "+kpair.getFingerPrint());
                tv.append("Finger print: "+kpair.getFingerPrint());
        kpair.dispose();
    }
    catch(Exception e){
        System.out.println(e);
        tv.append("exeption: "+e.getMessage());
    }
}

    public void showAllData(){
        //for each file in /LGOD/ do getDataset and append to a expandable list
        File dir = new File(Environment.getExternalStorageDirectory()+"/LGOD/");
        if (dir.isDirectory()){
            TextView test = (TextView)findViewById(R.id.textView);
            NavigationDataSet kmldata;

            for (File child : dir.listFiles()) {
               //check if file is a kml
               if(getFileExtension(child.getName()).equalsIgnoreCase("kml")){
                   kmldata=getDataset(child);
                   test.append(child.getName()+"--------------\n------------\n\n"+kmldata.toString()+"\n\n"); //this is a test
               }
                // + Add the list to an expandable list view.
            }
        }

    }

private String getFileExtension(String urlInput) {
    String extension = "";

    int i = urlInput.lastIndexOf('.');
    if (i > 0) {
        extension = urlInput.substring(i+1);
    }
    return extension;
}

    public NavigationDataSet getDataset(File kmlfile){
        //returns a NavigationDataSet containing all the placemarks from the given kml file
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

            InputStream inputStream= new FileInputStream(kmlfile);
            Reader reader = new InputStreamReader(inputStream,"UTF-8");

            InputSource is = new InputSource(reader);

            //is.setEncoding("UTF-8");
            xr.parse(is);

            /* Our NavigationSaxHandler now provides the parsed data to us. */
            navigationDataSet = navSax2Handler.getParsedData();

        } catch (Exception e) {
            navigationDataSet = null;
        }
        //return navigationDataSet;
        return navigationDataSet;

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
