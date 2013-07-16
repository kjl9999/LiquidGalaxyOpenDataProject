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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;
import java.util.Properties;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import android.util.Log;

import com.jcraft.jsch.*;

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
                //showAllData();
                //testing ssh

                new Thread(new Runnable() {
                    public void run() {
                        try {
                            executeRemoteCommand("lg", "lqgalaxy", "10.42.42.1", 22);
                        } catch (Exception e) {
                            e.printStackTrace();
                            TextView tv1 = (TextView) findViewById(R.id.textView);
                            tv1.append("exeption: "+e.getMessage()); //here the app would crash since can't print from the thread
                        }
                    }
                }).start();
                /*
                try {
                    maintext.append(executeRemoteCommand("lg","lqgalaxy","10.42.42.1",22));
                } catch (Exception e) {
                    e.printStackTrace();
                    maintext.append("exeption: "+e.getMessage());
                }*/

                //end testing ssh

                //test generate key
                //generateKeys();
                //end test
            }
        });
        TextView tv = (TextView)findViewById(R.id.textView);
    }

    public static String executeRemoteCommand(
            String username,
            String password,
            String hostname,
            int port) throws Exception {

        JSch jsch = new JSch();
        String privateKey = Environment.getExternalStorageDirectory()+"/LGOD/lg-id_rsa";
        jsch.addIdentity(privateKey);

        Session session = jsch.getSession(username, hostname, 22);
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
        channelssh.setCommand("touch /home/lg/asdasd");
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
    String filename=Environment.getExternalStorageDirectory()+"/LGOD/lg-id_rsa";   ///////////////////////
    String comment="Liquid Galaxy";                                                          //what is that comment?
                                                                                ///////////////////////
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
                kmldata=getDataset(child);
                test.append(child.getName()+"--------------\n------------\n\n"+kmldata.toString()+"\n\n"); //this is a test

                // + Add the list to an expandable list view.
            }
        }

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
