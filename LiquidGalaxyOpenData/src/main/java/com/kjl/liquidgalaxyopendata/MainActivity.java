package com.kjl.liquidgalaxyopendata;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.service.textservice.SpellCheckerService;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.LinearLayout;
import android.widget.ListView;
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


/*
this is the apps main screen
user is shown a list of all data files
on click a connection is opened
then the file to be shown is written on kmls.txt on the Liquid Galaxy
and the geo-location of the data is written on query.txt
 */
public class MainActivity extends Activity {

    private final String TAG = "MainActivity";
    String[] params;                                        //conection params
    dataBank DB = new dataBank();                           //class containing all data
    ArrayList<dataSource> DBsources = DB.getDataSources();  //list of data files
    int listPosition;                                       //int indicating the last file the user has selected

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Load the list of files to the listview
        final ListView DSlist = (ListView) findViewById(R.id.datalist);
        final ArrayAdapter<String> listAdapter ;
        listAdapter = new ArrayAdapter<String>(this, R.layout.listlayout);
        for(int i = 0; i<DBsources.size(); i++){
            listAdapter.add(DBsources.get(i).getName());
        }
        DSlist.setAdapter(listAdapter);

        //Set the click listener
        DSlist.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> arg0, View v, int position, long id) {
                listPosition=position;

                //gets the connection params from configuration file
                //This part may be moved earlier to optimize speed by preloading
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

                //connection thread
                //opens a SSH conection with JSch
                //and sends the commands to the Liquid Galaxy
                //to show the clicked data file
                //and to fly there
                new Thread(new Runnable() {
                    public void run() {
                        try {
                            String username = params[0];
                            String password = params[1];
                            String ipaddress = params[2];
                            String query = params[4];
                            String kmlstxt = params[5];
                            String serverURL =params[7];
                            String filename = DBsources.get(listPosition).getName();
                            String lat = getLatLong(DBsources.get(listPosition).getName())[1];
                            String lon = getLatLong(DBsources.get(listPosition).getName())[0];
                            String writeonkmls = "echo '"+serverURL+filename+"' > "+kmlstxt+"";
                            String flyto = "flytoview=<LookAt><longitude>"+lon+"</longitude><latitude>"+lat+"</latitude><altitude>0</altitude><tilt>68.68179673613697</tilt><range>774.4323347622752</range><altitudeMode>relativeToGround</altitudeMode><gx:altitudeMode>relativeToSeaFloor</gx:altitudeMode></LookAt>";

                            //command to write the file on kmls.txt
                            executeRemoteCommand(username, password, ipaddress, 22, writeonkmls);
                            //command to write the data position on query.txt
                            executeRemoteCommand(username, password, ipaddress, 22, "echo '"+flyto+"' > "+query+"");

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }).start();
            }
        });
        showAllData();

        //THIS IS TESTING FUNCTIONALITY
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
                            executeRemoteCommand(params[0], params[1], params[2], 22, "whoami > test.txt");
                            //executeRemoteCommand(params[0], params[1], params[2], 22, "echo 'hola' > test.txt");        //escriure el kml al /var/www/kml/kmls.txt
                            //executeRemoteCommand(params[0], params[1], params[2], 22, "echo 'search=pobla de segur' > /tmp/query.txt");       //escriure la localització a query.txt
                        } catch (Exception e) {
                            e.printStackTrace();
                            TextView tv1 = (TextView) findViewById(R.id.textView);
                            tv1.append("exeption: "+e.getMessage()); //here the app would crash since can't print from the thread
                        }
                    }
                }).start();
            }
        });
        //END OF TESTING FUN
    }

    //Returns the latitude and longitude of the first placemark on a file as a String[]
    private String[] getLatLong(String name){
        ArrayList<dataSource> dataFiles = DB.getDataSources();
        String[] coords = new String[2];
        for(int i = 0; i<dataFiles.size(); i++){
            if (dataFiles.get(i).getName().equalsIgnoreCase(name)){
                //gets the first coord from the file
                File kmlfile = new File(Environment.getExternalStorageDirectory()+"/LGOD/"+name);
                NavigationDataSet kmldata;
                kmldata=getDataset(kmlfile);
                String tempcoords=kmldata.getPlacemarks().get(0).getCoordinates();
                coords=tempcoords.split(",");
                return coords;
            }
        }
        return coords;
    }

//Opens a SSH connection and sends a coomand
    public String executeRemoteCommand(
            final String username,
            final String password,
            final String hostname,
            int port,
            final String command) throws Exception {

        JSch jsch = new JSch();
        String privateKey = Environment.getExternalStorageDirectory()+"/LGOD/conf/lg-id_rsa";   //now hardcoded, data from configuration file needs to be used
        jsch.addIdentity(privateKey);
        Session session = jsch.getSession(username, hostname, port);
        session.setPassword(password);
        Properties prop = new Properties();
        prop.put("StrictHostKeyChecking", "no");
        session.setConfig(prop);
        session.connect();

        ChannelExec channelssh = (ChannelExec)
        session.openChannel("exec");
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        channelssh.setOutputStream(baos);

        channelssh.setCommand(command);
        channelssh.connect();
        channelssh.disconnect();

        return baos.toString();
    }

    //TESTING FUNCTION
    // loads data froma all files added to the app on a textview
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
    //Returns the content of a kml file as objects that can be handled
    public NavigationDataSet getDataset(File kmlfile){
        //returns a NavigationDataSet containing all the placemarks from the given kml file
        NavigationDataSet navigationDataSet = null;
        try
        {
            SAXParserFactory spf = SAXParserFactory.newInstance();
            SAXParser sp = spf.newSAXParser();

            XMLReader xr = sp.getXMLReader();

            NavigationSaxHandler navSax2Handler = new NavigationSaxHandler();
            xr.setContentHandler(navSax2Handler);

            InputStream inputStream= new FileInputStream(kmlfile);
            Reader reader = new InputStreamReader(inputStream,"UTF-8");

            InputSource is = new InputSource(reader);

            xr.parse(is);

            navigationDataSet = navSax2Handler.getParsedData();

        } catch (Exception e) {
            navigationDataSet = null;
        }
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