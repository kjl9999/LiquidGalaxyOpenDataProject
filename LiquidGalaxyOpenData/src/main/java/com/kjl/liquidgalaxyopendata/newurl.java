package com.kjl.liquidgalaxyopendata;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.view.MenuItem;
import android.support.v4.app.NavUtils;
import android.view.View;

import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;


public class newurl extends Activity {

    // Used to communicate state changes in the DownloaderThread
    public static final int MESSAGE_DOWNLOAD_STARTED = 1000;
    public static final int MESSAGE_DOWNLOAD_COMPLETE = 1001;
    public static final int MESSAGE_UPDATE_PROGRESS_BAR = 1002;
    public static final int MESSAGE_DOWNLOAD_CANCELED = 1003;
    public static final int MESSAGE_CONNECTING_STARTED = 1004;
    public static final int MESSAGE_ENCOUNTERED_ERROR = 1005;

    // instance variables
    private newurl thisActivity;
    private Thread downloaderThread;
    private ProgressDialog progressDialog;
    private int step; //0=name 1=description 2=coordType 3=coords/coordX 4=coordY 10=Finish
    int[] positions = new int[4]; //0=name 1=description 2=coords/coordX 3=coordY
    int coordmode = 0; //0=coords 1=coordX 2=coordY
    csv csvFile = new csv();
    private ArrayList<Placemark> placemarks;
    Button next;
    dataBank bank;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupActionBar();
        setContentView(R.layout.newurl);

        thisActivity = this;
        downloaderThread = null;
        progressDialog = null;
        step=0;
        bank = new dataBank();

        EditText urlInputField = (EditText) findViewById(R.id.url_input);
        //urlInputField.setText("https://googledrive.com/host/0B3IQnYh_y3OXNUoyb1k3YlF0TTA/hostaleria.csv");
        urlInputField.setText("https://googledrive.com/host/0B3IQnYh_y3OXNUoyb1k3YlF0TTA/educacio_primaria.kmz");


        Button back= (Button) findViewById(R.id.BtnBack);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                NavUtils.navigateUpFromSameTask(newurl.this);
            }
        });

        next= (Button) findViewById(R.id.BtnNext);
        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                Toast.makeText(getApplicationContext(),"step: "+String.valueOf(step),Toast.LENGTH_LONG);
                EditText urlInputField = (EditText) findViewById(R.id.url_input);
                String urlInput = urlInputField.getText().toString();
                String ext = getFileExtension(urlInput);

                if(ext.equalsIgnoreCase("kml")){
                    downloaderThread = new DownloaderThread(thisActivity, urlInput);
                    downloaderThread.start();
                    try {
                        downloaderThread.join();            //thread.join() waits untill the file is downloaded, fut freezes the screen, need to find a better way.
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    bank.addDataSource(getNewestFileInDirectory().getName(),urlInput,getNewestFileInDirectory().getAbsolutePath());
                    NavUtils.navigateUpFromSameTask(newurl.this);
                }
                else if(ext.equalsIgnoreCase("csv")){


                    RadioGroup rg = (RadioGroup) findViewById(R.id.radioName);
                    RadioGroup rgCoords = (RadioGroup) findViewById(R.id.radioCoords);
                    TextView desc = (TextView) findViewById(R.id.description);
                    TextView tilte = (TextView) findViewById(R.id.title);

                    //read csv document and name mapping
                    if(step==0){

                        //creates a thread that downloads the file
                        //the main thread waits with .join()
                        downloaderThread = new DownloaderThread(thisActivity, urlInput);
                        downloaderThread.start();
                        try {
                            downloaderThread.join();            //thread.join() waits untill the file is downloaded, fut freezes the screen, need to find a better way.
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                        csvFile.setCsvReader(getNewestFileInDirectory().getName());
                        //gets the first row from csv so users can select fields
                        ArrayList<String> line=csvFile.getLine(0);

                        LinearLayout urllayout = (LinearLayout) findViewById(R.id.urllayout);
                        LinearLayout mappinglayout = (LinearLayout) findViewById(R.id.mappinglayout);
                        urllayout.setVisibility(View.GONE);
                        mappinglayout.setVisibility(View.VISIBLE);

                        final RadioButton[] rb = new RadioButton[line.size()];
                        rg= (RadioGroup) findViewById(R.id.radioName);

                        for(int i=0; i<line.size(); i++){
                            rb[i]  = new RadioButton(thisActivity);
                            rb[i].setText(line.get(i));
                            rb[i].setId(i);
                            rg.addView(rb[i]);
                        }
                        step=1;

                    }
                    //description mapping
                    else if(step==1){
                        //stores the name position selected in the last step
                        positions[0]=rg.getCheckedRadioButtonId();

                        desc.setText(R.string.csvmappingdesc);
                        tilte.setText(R.string.desc);
                        step=2;
                    }
                    //select coord mapping method
                    else if(step==2){
                        //stores the description position selected in the last step
                        positions[1]=rg.getCheckedRadioButtonId();

                        //asks the user how the coords are to be mapped
                        desc.setText(R.string.csvmappingquestion);
                        tilte.setText(R.string.coordtype);
                        rg.setVisibility(View.GONE);
                        RadioButton rb1  = new RadioButton(thisActivity);
                        rb1.setText(R.string.singlecoords);
                        rb1.setId(0);
                        rgCoords.addView(rb1);
                        RadioButton rb2  = new RadioButton(thisActivity);
                        rb2.setText(R.string.separatecoords);
                        rb2.setId(1);
                        rgCoords.addView(rb2);
                        rgCoords.setVisibility(View.VISIBLE);
                        step=3;
                    }
                    //coordinade mapping
                    else if(step==3){
                        if(coordmode!=2)coordmode=rgCoords.getCheckedRadioButtonId();

                        rgCoords.setVisibility(View.GONE);
                        rg.setVisibility(View.VISIBLE);
                        //one-field coordinades
                        if(coordmode==0){
                            desc.setText(R.string.csvmappingcoords);
                            tilte.setText(R.string.coords);
                            step=10;
                            Button finish = (Button) findViewById(R.id.BtnNext);
                            finish.setText(R.string.Finish);
                        }
                        //X coordinate
                        else if(coordmode==1){
                            desc.setText(R.string.csvmappingcoordx);
                            tilte.setText(R.string.coordx);
                            coordmode=2;
                        }
                        else if(coordmode==2){
                            positions[2]=rg.getCheckedRadioButtonId();
                            desc.setText(R.string.csvmappingcoordy);
                            tilte.setText(R.string.coordy);
                            step=10;
                            next.setText(R.string.Finish);
                        }

                    }
                    //read the csv file and create the kml file
                    else if (step==10){
                        LinearLayout urllayout = (LinearLayout) findViewById(R.id.urllayout);
                        LinearLayout mappinglayout = (LinearLayout) findViewById(R.id.mappinglayout);
                        urllayout.setVisibility(View.VISIBLE);
                        mappinglayout.setVisibility(View.GONE);

                        EditText url = (EditText) findViewById(R.id.url_input);
                        url.setVisibility(View.GONE);
                        TextView finaltv = (TextView) findViewById(R.id.finaltv);
                        finaltv.setVisibility(View.VISIBLE);

                        if(coordmode==0){
                            placemarks = csvFile.readCSV(positions[0], positions[1], positions[2]);
                            //finaltv.setText("Name: "+String.valueOf(positions[0])+" Description: "+String.valueOf(positions[1])+" Coord: "+String.valueOf(positions[2]));
                        }
                        else {
                            positions[3]=rg.getCheckedRadioButtonId();
                            placemarks = csvFile.readCSV(positions[0], positions[1], positions[2], positions[3]);
                            //finaltv.setText("Name: "+String.valueOf(positions[0])+" Description: "+String.valueOf(positions[1])+" Coord x: "+String.valueOf(positions[2])+" Coord y: "+String.valueOf(positions[3]));
                        }
                        //printing the data
                        for(int i=0; i<placemarks.size();i++){
                            // + check if first line is relevant
                            finaltv.append("\n"+placemarks.get(i).getTitle()+" "+placemarks.get(i).getDescription()+" "+placemarks.get(i).getCoordinates());
                        }

                        //writing a kml file
                        writeKMLFile(placemarks);
                        bank.addDataSource(getNewestFileInDirectory().getName(),urlInput,getNewestFileInDirectory().getAbsolutePath());
                        NavUtils.navigateUpFromSameTask(newurl.this);

                    }

                }
                else if(ext.equalsIgnoreCase("kmz")){
                    //creates a thread that downloads the file
                    //the main thread waits with .join()
                    downloaderThread = new DownloaderThread(thisActivity, urlInput);
                    downloaderThread.start();
                    try {
                        downloaderThread.join();            //thread.join() waits untill the file is downloaded, fut freezes the screen, need to find a better way.
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    File kmzFile = getNewestFileInDirectory();

                    unpackZip(Environment.getExternalStorageDirectory()+"/LGOD/", kmzFile.getName());
                    bank.addDataSource(getNewestFileInDirectory().getName(),urlInput,getNewestFileInDirectory().getAbsolutePath());
                    NavUtils.navigateUpFromSameTask(newurl.this);

                }
                else {
                    Toast.makeText(getApplicationContext(), "This file extension is not supported: "+ext, Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    // this function will be used if more coordinate formats are accepted by the app
    public String convertCoordinates(String coordinates){
        if (checkDecimalCoords(coordinates)){
            //coords need no transformation
        }
        else if(checkDMScoord1(coordinates)){
           //convert to decimal
        }
        else if(checkDMScoord2(coordinates)){
            //convert to decimal
        }
        else if(checkDegreecoord1(coordinates)){
            //convert to decimal
        }
        else if(checkDegreecoord2(coordinates)){
            //convert to decimal
        }

        return coordinates;
    }

    private boolean unpackZip(String path, String zipname)
    {
        InputStream is;
        ZipInputStream zis;
        try
        {
            String filename;
            is = new FileInputStream(path + zipname);
            zis = new ZipInputStream(new BufferedInputStream(is));
            ZipEntry ze;
            byte[] buffer = new byte[1024];
            int count;

            while ((ze = zis.getNextEntry()) != null)
            {
                filename = ze.getName();

                if (ze.isDirectory()) {
                    File fmd = new File(path + zipname);
                    fmd.mkdirs();
                    continue;
                }

                if(getFileExtension(filename).equalsIgnoreCase("kml")){
                    //not handled when there are more than one kml in the kmz
                    FileOutputStream fout = new FileOutputStream(path + zipname.replace("kmz","kml"));

                    while ((count = zis.read(buffer)) != -1)
                    {
                        fout.write(buffer, 0, count);
                    }
                    fout.close();
                }

                zis.closeEntry();
            }

            zis.close();
        }
        catch(IOException e)
        {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    public boolean checkDMScoord1(String coordinates){
        // º ' " with +/-
        String regex_coords = "([+-]?(\\d+)D(\\d+)M(\\d+\\.\\d+)?S)\\s*,\\s*([+-]?(\\d+)D(\\d+)M(\\d+\\.\\d+)?S)";

        Pattern regExPattern = Pattern.compile(regex_coords);
        Matcher regExMatcher= regExPattern.matcher(coordinates);
        if(regExMatcher.matches()){
            return true;
        }
        else return false;
    }
    public boolean checkDMScoord2(String coordinates){
        // º ' " with NSEW
        String regex_coords = "((\\d+)D(\\d+)M(\\d+\\.\\d+)?S[NSEW]?)\\s*,\\s*((\\d+)D(\\d+)M(\\d+\\.\\d+)?S[NSEW]?)";

        Pattern regExPattern = Pattern.compile(regex_coords);
        Matcher regExMatcher= regExPattern.matcher(coordinates);
        if(regExMatcher.matches()){
            return true;
        }
        else return false;
    }
    public boolean checkDegreecoord1(String coordinates){
    // º ' " with +/-
        String regex_coords = "([+-]?(\\d+)°(\\d+)'(\\d+\\.\\d+)?\")\\s*,\\s*([+-]?(\\d+)°(\\d+)'(\\d+\\.\\d+)?\")";

        Pattern regExPattern = Pattern.compile(regex_coords);
        Matcher regExMatcher= regExPattern.matcher(coordinates);
        if(regExMatcher.matches()){
            return true;
        }
        else return false;
    }
    public boolean checkDegreecoord2(String coordinates){
    // º ' " with NSEW
        String regex_coords = "((\\d+)°(\\d+)'(\\d+\\.\\d+)?\"[NSEW]?)\\s*,\\s*((\\d+)°(\\d+)'(\\d+\\.\\d+)?\"[NSEW]?)";

        Pattern regExPattern = Pattern.compile(regex_coords);
        Matcher regExMatcher= regExPattern.matcher(coordinates);
        if(regExMatcher.matches()){
            return true;
        }
        else return false;
    }
    public boolean checkDecimalCoords(String coordinates){
        String regex_coords = "([+-]?\\d+\\.?\\d+)\\s*,\\s*([+-]?\\d+\\.?\\d+)";

        Pattern regExPattern = Pattern.compile(regex_coords);
        Matcher regExMatcher= regExPattern.matcher(coordinates);
        if(regExMatcher.matches()){
            return true;
        }
        else return false;
    }

    public String getFileNameNoExtension(File file){
        String name = file.getName();
        int pos = name.lastIndexOf(".");
        if (pos > 0) {
            name = name.substring(0, pos);
        }
        return name;
    }

    public void writeKMLFile(ArrayList<Placemark> placemarks){
        String kmlFile = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<kml xmlns=\"http://www.opengis.net/kml/2.2\" xmlns:gx=\"http://www.google.com/kml/ext/2.2\" xmlns:kml=\"http://www.opengis.net/kml/2.2\" xmlns:atom=\"http://www.w3.org/2005/Atom\">\n" +
                "<Folder>\n" +
                "\t<name>" +
                getNewestFileInDirectory().getName() +
                "</name>\n" +
                "<description></description>\n" +
                getKMLPlacemarcks(placemarks) +
                "</Folder>\n" +
                "</kml>\n"
                ;

        try {
            File file = new File(Environment.getExternalStorageDirectory()+"/LGOD/"+getFileNameNoExtension(getNewestFileInDirectory())+"_LGOD.kml");

            // if file doesnt exists, then create it
            if (!file.exists()) {
                file.createNewFile();
            }

            FileWriter fw = new FileWriter(file.getAbsoluteFile());
            BufferedWriter bw = new BufferedWriter(fw);
            bw.write(kmlFile);
            bw.close();

        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    public String getKMLPlacemarcks(ArrayList<Placemark> placemarks){
        String kmlplacemarks = "";
        for(int i=1; i<placemarks.size();i++){ //this for ignores the first line, suposed to be titles
            kmlplacemarks+=
                    "\t<Placemark>\n" +
                        "\t\t<name>"+ placemarks.get(i).getTitle() +"</name>\n" +
                        "\t\t<description>"+ placemarks.get(i).getDescription() +"</description>\n" +
                        "\t\t<Point>\n" +
                            "\t\t\t<coordinates>"+ placemarks.get(i).getCoordinates() +"</coordinates>\n" +
                        "\t\t</Point>\n" +
                    "\t</Placemark>\n";
        }
        return kmlplacemarks;
    }

    public File getNewestFileInDirectory() {
        File dir = new File(Environment.getExternalStorageDirectory()+"/LGOD/");
        File newestFile = null;

        if (dir.isDirectory()){
            TextView test = (TextView)findViewById(R.id.textView);
            NavigationDataSet kmldata;

            for (File child : dir.listFiles()) {
                if (newestFile == null || child.lastModified()>(newestFile.lastModified())) {
                    newestFile = child;
                }
            }
        }
        return newestFile;
    }

    private String getFileExtension(String urlInput) {
        String extension = "";

        int i = urlInput.lastIndexOf('.');
        if (i > 0) {
            extension = urlInput.substring(i+1);
        }
        return extension;
    }

    private String getFileName(String urlInput) {
        String name = "";

        int i = urlInput.lastIndexOf('/');
        if (i > 0) {
            name = urlInput.substring(i+1);
        }
        return name;
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


//DOWNLOAD CODE
    public Handler activityHandler = new Handler()
    {
        public void handleMessage(Message msg)
        {
            switch(msg.what)
            {
                                /*
                                 * Handling MESSAGE_UPDATE_PROGRESS_BAR:
                                 * 1. Get the current progress, as indicated in the arg1 field
                                 *    of the Message.
                                 * 2. Update the progress bar.
                                 */
                case MESSAGE_UPDATE_PROGRESS_BAR:
                    if(progressDialog != null)
                    {
                        int currentProgress = msg.arg1;
                        progressDialog.setProgress(currentProgress);
                    }
                    break;

                                /*
                                 * Handling MESSAGE_CONNECTING_STARTED:
                                 * 1. Get the URL of the file being downloaded. This is stored
                                 *    in the obj field of the Message.
                                 * 2. Create an indeterminate progress bar.
                                 * 3. Set the message that should be sent if user cancels.
                                 * 4. Show the progress bar.
                                 */
                case MESSAGE_CONNECTING_STARTED:
                    if(msg.obj != null && msg.obj instanceof String)
                    {
                        String url = (String) msg.obj;
                        // truncate the url
                        if(url.length() > 16)
                        {
                            String tUrl = url.substring(0, 15);
                            tUrl += "...";
                            url = tUrl;
                        }
                        String pdTitle = thisActivity.getString(R.string.progress_dialog_title_connecting);
                        String pdMsg = thisActivity.getString(R.string.progress_dialog_message_prefix_connecting);
                        pdMsg += " " + url;

                        dismissCurrentProgressDialog();
                        progressDialog = new ProgressDialog(thisActivity);
                        progressDialog.setTitle(pdTitle);
                        progressDialog.setMessage(pdMsg);
                        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                        progressDialog.setIndeterminate(true);
                        // set the message to be sent when this dialog is canceled
                        Message newMsg = Message.obtain(this, MESSAGE_DOWNLOAD_CANCELED);
                        progressDialog.setCancelMessage(newMsg);
                        progressDialog.show();
                    }
                    break;

                                /*
                                 * Handling MESSAGE_DOWNLOAD_STARTED:
                                 * 1. Create a progress bar with specified max value and current
                                 *    value 0; assign it to progressDialog. The arg1 field will
                                 *    contain the max value.
                                 * 2. Set the title and text for the progress bar. The obj
                                 *    field of the Message will contain a String that
                                 *    represents the name of the file being downloaded.
                                 * 3. Set the message that should be sent if dialog is canceled.
                                 * 4. Make the progress bar visible.
                                 */
                case MESSAGE_DOWNLOAD_STARTED:
                    // obj will contain a String representing the file name
                    if(msg.obj != null && msg.obj instanceof String)
                    {
                        int maxValue = msg.arg1;
                        String fileName = (String) msg.obj;
                        String pdTitle = thisActivity.getString(R.string.progress_dialog_title_downloading);
                        String pdMsg = thisActivity.getString(R.string.progress_dialog_message_prefix_downloading);
                        pdMsg += " " + fileName;

                        dismissCurrentProgressDialog();
                        progressDialog = new ProgressDialog(thisActivity);
                        progressDialog.setTitle(pdTitle);
                        progressDialog.setMessage(pdMsg);
                        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                        progressDialog.setProgress(0);
                        progressDialog.setMax(maxValue);
                        // set the message to be sent when this dialog is canceled
                        Message newMsg = Message.obtain(this, MESSAGE_DOWNLOAD_CANCELED);
                        progressDialog.setCancelMessage(newMsg);
                        progressDialog.setCancelable(true);
                        progressDialog.show();
                    }
                    break;

                                /*
                                 * Handling MESSAGE_DOWNLOAD_COMPLETE:
                                 * 1. Remove the progress bar from the screen.
                                 * 2. Display Toast that says download is complete.
                                 */
                case MESSAGE_DOWNLOAD_COMPLETE:
                    dismissCurrentProgressDialog();
                    displayMessage(getString(R.string.user_message_download_complete));
                    break;

                                /*
                                 * Handling MESSAGE_DOWNLOAD_CANCELLED:
                                 * 1. Interrupt the downloader thread.
                                 * 2. Remove the progress bar from the screen.
                                 * 3. Display Toast that says download is complete.
                                 */
                case MESSAGE_DOWNLOAD_CANCELED:
                    if(downloaderThread != null)
                    {
                        downloaderThread.interrupt();
                    }
                    dismissCurrentProgressDialog();
                    displayMessage(getString(R.string.user_message_download_canceled));
                    break;

                                /*
                                 * Handling MESSAGE_ENCOUNTERED_ERROR:
                                 * 1. Check the obj field of the message for the actual error
                                 *    message that will be displayed to the user.
                                 * 2. Remove any progress bars from the screen.
                                 * 3. Display a Toast with the error message.
                                 */
                case MESSAGE_ENCOUNTERED_ERROR:
                    // obj will contain a string representing the error message
                    if(msg.obj != null && msg.obj instanceof String)
                    {
                        String errorMessage = (String) msg.obj;
                        dismissCurrentProgressDialog();
                        displayMessage(errorMessage);
                    }
                    break;

                default:
                    // nothing to do here
                    break;
            }
        }
    };


    /**
     * If there is a progress dialog, dismiss it and set progressDialog to
     * null.
     */
    public void dismissCurrentProgressDialog()
    {
        if(progressDialog != null)
        {
            progressDialog.hide();
            progressDialog.dismiss();
            progressDialog = null;
        }
    }

    /**
     * Displays a message to the user, in the form of a Toast.
     * @param message Message to be displayed.
     */
    public void displayMessage(String message)
    {
        if(message != null)
        {
            Toast.makeText(thisActivity, message, Toast.LENGTH_SHORT).show();
        }
    }

//DOWNLOAD CODE

}
