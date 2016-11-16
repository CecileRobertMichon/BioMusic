package com.dp.cecile.biomusic;

import android.util.Log;
import android.content.IntentSender;
import android.content.IntentSender.SendIntentException;
import android.os.Bundle;

import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.text.DateFormat;
import java.util.Date;
import java.util.ArrayList;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveApi.DriveContentsResult;
import com.google.android.gms.drive.MetadataChangeSet;

public class FileManager implements ConnectionCallbacks, OnConnectionFailedListener {

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

        // Start by creating a new contents, and setting a callback.
        Log.i(TAG, "Creating new contents.");
        Drive.DriveApi.newDriveContents(mGoogleApiClient).setResultCallback(new ResultCallback<DriveContentsResult>() {
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
                OutputStream fos = result.getDriveContents().getOutputStream();
                OutputStreamWriter out = new OutputStreamWriter(fos);
                try {
                    out.write("DATA COLLECTION START: " + mActivity.startTime);
                    out.write(System.getProperty("line.separator"));
                    out.write(System.getProperty("line.separator"));
                    out.write("BVP   SC   Temp");
                    out.write(System.getProperty("line.separator"));
                    int sizeBVP = mActivity.getMusicMaker().getBVP_data_string().size();
                    int sizeSC = mActivity.getMusicMaker().getSC_data_string().size();
                    for (int i=0; i<sizeBVP; i++){
                        if (i < sizeSC){
                            out.write(mActivity.getMusicMaker().getBVP_data_string().get(i) + "   "
                                    + mActivity.getMusicMaker().getSC_data_string().get(i) + "   "
                                    + mActivity.getMusicMaker().getTEMP_data_string().get(i));
                            out.write(System.getProperty("line.separator"));
                        } else {
                            out.write(mActivity.getMusicMaker().getBVP_data_string().get(i));
                            out.write(System.getProperty("line.separator"));
                        }
                    }
                    out.write(System.getProperty("line.separator"));
                    out.write("DATA COLLECTION STOP: " + mActivity.stopTime);
                    out.close();
                    fos.close();
                } catch (Exception e) {
                    Log.i(TAG, "Unable to write file contents.");
                }
                Log.i(TAG, "New contents created.");


                // Create the initial metadata - MIME type and title.
                // Note that the user will be able to change the title later.
                String currentDateTimeString = DateFormat.getDateTimeInstance().format(new Date());
                MetadataChangeSet metadataChangeSet = new MetadataChangeSet.Builder()
                        .setMimeType("text/plain").setTitle(currentDateTimeString).build();

                // Create an intent for the file chooser, and start it.
                IntentSender intentSender = Drive.DriveApi
                        .newCreateFileActivityBuilder()
                        .setInitialMetadata(metadataChangeSet)
                        .setInitialDriveContents(result.getDriveContents())
                        .build(mGoogleApiClient);
                try {
                    mActivity.startIntentSenderForResult(
                            intentSender, 2, null, 0, 0, 0);
                } catch (SendIntentException e) {
                            Log.i(TAG, "Failed to launch file chooser.");
                }
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
