/*
 *    Calendula - An assistant for personal medication management.
 *    Copyright (C) 2016 CITIUS - USC
 *
 *    Calendula is free software; you can redistribute it and/or modify
 *    it under the terms of the GNU General Public License as published by
 *    the Free Software Foundation; either version 3 of the License, or
 *    (at your option) any later version.
 *
 *    This program is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU General Public License for more details.
 *
 *    You should have received a copy of the GNU General Public License
 *    along with this software.  If not, see <http://www.gnu.org/licenses/>.
 */

package es.usc.citius.servando.calendula.drugdb;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import es.usc.citius.servando.calendula.R;

/**
 * Created by joseangel.pineiro on 9/2/15.
 */
public class DownloadDatabaseDialogHelper {

    public static final String TAG = "DDDialogHelper.class";

    public interface DownloadDatabaseDialogCallback{
        void onDownloadAcceptedOrCancelled(boolean accepted);
    }

    public static void showDownloadDatabaseDialog(final Context dialogCtx, final String database, final DownloadDatabaseDialogCallback callback){

            Log.d(TAG, "ShowDownloadDatabase");
            final Context appContext = dialogCtx.getApplicationContext();
            AlertDialog.Builder builder = new AlertDialog.Builder(dialogCtx);
            builder.setTitle("Setup med database");
            builder.setCancelable(false);
            builder.setMessage("The database will be downloaded and configured. This may take a few seconds, but you can continue using the application in the meantime.")
                    .setCancelable(false)
                    .setPositiveButton("Download and setup", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            Toast.makeText(dialogCtx, "Downloading medicines info...", Toast.LENGTH_SHORT).show();
                            if(callback != null){
                                callback.onDownloadAcceptedOrCancelled(true);
                            }
                            DBDownloader.DBDownloadListener l = new DBDownloader.DBDownloadListener() {
                                @Override
                                public void onComplete(boolean success, String path) {
                                    Log.d("DOWNLOAD", "Success: " + success + ", path: " + path);
                                    if (success) {
                                        SetupDBService.startSetup(appContext, path, database);
                                    }else{
                                        Intent bcIntent = new Intent();
                                        bcIntent.setAction(SetupDBService.ACTION_COMPLETE);
                                        appContext.sendBroadcast(bcIntent);
                                    }
                                }
                            };
                            try {
                                PrescriptionDBMgr mgr = DBRegistry.instance().db(database);
                                if (mgr != null)
                                    DBDownloader.download(appContext,mgr, l);
                                else
                                    Toast.makeText(dialogCtx, "Database not available :(", Toast.LENGTH_SHORT).show();
                            } catch (IllegalArgumentException e) {
                                e.printStackTrace();
                                Toast.makeText(dialogCtx, "Database not available :(", Toast.LENGTH_SHORT).show();
                            }
                        }
                    })
                    .setNegativeButton(dialogCtx.getString(R.string.cancel), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                            if (callback != null) {
                                callback.onDownloadAcceptedOrCancelled(false);
                            }
                        }
                    });
            AlertDialog alert = builder.create();
            alert.show();
    }

}
