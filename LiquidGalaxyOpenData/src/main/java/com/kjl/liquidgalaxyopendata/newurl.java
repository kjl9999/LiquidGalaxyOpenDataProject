package com.kjl.liquidgalaxyopendata;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.MenuItem;
import android.support.v4.app.NavUtils;
import android.view.View;

import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupActionBar();
        setContentView(R.layout.newurl);

        thisActivity = this;
        downloaderThread = null;
        progressDialog = null;

        Button back= (Button) findViewById(R.id.BtnBack);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                NavUtils.navigateUpFromSameTask(newurl.this);
            }
        });

        Button next= (Button) findViewById(R.id.BtnNext);
        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                EditText urlInputField = (EditText) findViewById(R.id.url_input);
                String urlInput = urlInputField.getText().toString();
                String ext = getFileExtension(urlInput);

                if(ext.equalsIgnoreCase("kml")){
                    downloaderThread = new DownloaderThread(thisActivity, urlInput);
                    downloaderThread.start();
                }
                else if(ext=="csv"){
                    Toast.makeText(getApplicationContext(), "CSV parsing not implemented yet.", Toast.LENGTH_LONG).show();
                }
                else {
                    Toast.makeText(getApplicationContext(), "This file extension is not supported.", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private String getFileExtension(String urlInput) {
        String extension = "";

        int i = urlInput.lastIndexOf('.');
        if (i > 0) {
            extension = urlInput.substring(i+1);
        }
        return extension;
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
