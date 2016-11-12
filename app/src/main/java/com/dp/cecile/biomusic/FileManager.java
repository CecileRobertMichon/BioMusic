package com.dp.cecile.biomusic;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.util.Log;
import android.content.IntentSender;
import android.content.IntentSender.SendIntentException;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveApi.DriveContentsResult;
import com.google.android.gms.drive.MetadataChangeSet;


public class FileManager implements ConnectionCallbacks,
        OnConnectionFailedListener {

    private MainActivity mActivity;
    private GoogleApiClient mGoogleApiClient;
    private static final String TAG = "drive";

    /**
     * Constructor
     */
    public FileManager(MainActivity activity) {
        mActivity = activity;
    }

    public void showConnectToDrive() {
        if (mGoogleApiClient == null) {
            // Create the API client and bind it to an instance variable.
            // We use this instance as the callback for connection and connection
            // failures.
            // Since no account name is passed, the user is prompted to choose.
            mGoogleApiClient = new GoogleApiClient.Builder(mActivity)
                    .addApi(Drive.API)
                    .addScope(Drive.SCOPE_FILE)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .build();
        }
        mGoogleApiClient.connect();
    }

    public void saveFileToDrive() {

//
//        PackageManager m = mActivity.getPackageManager();
//        String s = mActivity.getPackageName();
//        try {
//            PackageInfo p = m.getPackageInfo(s, 0);
//            s = p.applicationInfo.dataDir;
//        } catch (PackageManager.NameNotFoundException e) {
//            Log.w("yourtag", "Error Package name not found ", e);
//        }
//
//        File filelocation = new File(s, "hello_file");
//        Uri path = Uri.fromFile(filelocation);
//
//        Intent emailIntent = new Intent(Intent.ACTION_SEND);
//        // The intent does not have a URI, so declare the "text/plain" MIME type
//        emailIntent.setType("vnd.android.cursor.dir/email");
//        emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[] {"mombeys@hotmail.com"}); // recipients
//        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Email subject");
//        emailIntent.putExtra(Intent.EXTRA_TEXT, "Email message text");
//        emailIntent.putExtra(Intent.EXTRA_STREAM, path);
//        mActivity.startActivity(Intent.createChooser(emailIntent , "Send email..."));

        // Start by creating a new contents, and setting a callback.
        Log.i(TAG, "Creating new contents.");
        final String string = "hello world!";
        Drive.DriveApi.newDriveContents(mGoogleApiClient)
                .setResultCallback(new ResultCallback<DriveContentsResult>() {

                    @Override
                    public void onResult(DriveContentsResult result) {
                        // If the operation was not successful, we cannot do anything
                        // and must
                        // fail.
                        if (!result.getStatus().isSuccess()) {
                            Log.i(TAG, "Failed to create new contents.");
                            return;
                        }
                        // Otherwise, we can write our data to the new contents.
                        Log.i(TAG, "New contents created.");
                        OutputStream fos = result.getDriveContents().getOutputStream();
                        try {
                            fos.write(string.getBytes());
                            fos.close();
                        } catch (Exception e) {
                            Log.i(TAG, "Unable to write file contents.");
                        }
                        // Create the initial metadata - MIME type and title.
                        // Note that the user will be able to change the title later.
                        MetadataChangeSet metadataChangeSet = new MetadataChangeSet.Builder()
                                .setMimeType("text/plain").setTitle("Hello.txt").build();
                        // Create an intent for the file chooser, and start it.
                        IntentSender intentSender = Drive.DriveApi
                                .newCreateFileActivityBuilder()
                                .setInitialMetadata(metadataChangeSet)
                                .setInitialDriveContents(result.getDriveContents())
                                .build(mGoogleApiClient);
//                        try {
//                            startIntentSenderForResult(
//                                    intentSender, 2, null, 0, 0, 0);
//                        } catch (SendIntentException e) {
//                            Log.i(TAG, "Failed to launch file chooser.");
//                        }
                    }
                });

    }

    @Override
    public void onConnected(Bundle connectionHint) {
        Log.i(TAG, "API client connected.");
        saveFileToDrive();
    }

    @Override
    public void onConnectionSuspended(int cause) {
        Log.i(TAG, "GoogleApiClient connection suspended");
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        // Called whenever the API client fails to connect.
        Log.i(TAG, "GoogleApiClient connection failed: " + result.toString());
        if (!result.hasResolution()) {
            // show the localized error dialog.
            GoogleApiAvailability.getInstance().getErrorDialog(mActivity, result.getErrorCode(), 0).show();
            return;
        }
        // The failure has a resolution. Resolve it.
        // Called typically when the app is not yet authorized, and an
        // authorization
        // dialog is displayed to the user.
        try {
            result.startResolutionForResult(mActivity, 3);
        } catch (SendIntentException e) {
            Log.e(TAG, "Exception while starting resolution activity", e);
        }
    }


}
