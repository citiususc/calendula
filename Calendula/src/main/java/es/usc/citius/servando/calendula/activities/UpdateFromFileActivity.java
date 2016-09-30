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

package es.usc.citius.servando.calendula.activities;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.util.List;

import es.usc.citius.servando.calendula.R;

public class UpdateFromFileActivity extends ActionBarActivity {

    public static final String TAG = "UpdateActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_from_file);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(getResources().getColor(R.color.android_blue_statusbar));
        }

        try{
            String fileContents = readFile();
            Log.d(TAG, "Text from file: " + fileContents);

            if(fileContents!=null){
                Intent intent = new Intent(getApplicationContext(), ConfirmSchedulesActivity.class);
                Bundle b = new Bundle();
                b.putString("qr_data", fileContents);
                intent.putExtras(b);
                startActivity(intent);
            }else{
                Toast.makeText(this,"El fichero de actualización no es válido!", Toast.LENGTH_LONG).show();
            }
            finish();
        }catch (Exception e){
            e.printStackTrace();
            finish();
        }

    }


    private String readFile(){
        Intent intent = getIntent();
        InputStream is = null;
        String fullPath = null;

        try {
            String action = intent.getAction();
            if (!Intent.ACTION_VIEW.equals(action)) {
                return null;
            }

            Uri uri = intent.getData();
            String scheme = uri.getScheme();
            String name = null;

            if (scheme.equals("file")) {
                List<String> pathSegments = uri.getPathSegments();
                if (pathSegments.size() > 0) {
                    name = pathSegments.get(pathSegments.size() - 1);
                }
            } else if (scheme.equals("content")) {
                Cursor cursor = getContentResolver().query(uri, new String[] {
                        MediaStore.MediaColumns.DISPLAY_NAME
                }, null, null, null);
                cursor.moveToFirst();
                int nameIndex = cursor.getColumnIndex(MediaStore.MediaColumns.DISPLAY_NAME);
                if (nameIndex >= 0) {
                    name = cursor.getString(nameIndex);
                }
            } else {
                return null;
            }

            if (name == null) {
                return null;
            }

            int n = name.lastIndexOf(".");
            String fileName, fileExt;

            if (n == -1) {
                return null;
            } else {
                fileName = name.substring(0, n);
                fileExt = name.substring(n);
                if (!fileName.contains("calendula") && !fileName.contains("Calendula")) {
                    return null;
                }
            }

            is = getContentResolver().openInputStream(uri);


            byte[] buffer = new byte[4096];
            int count;
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            while ((count = is.read(buffer)) > 0) {
                os.write(buffer, 0, count);
            }
            os.close();
            is.close();

            return os.toString("UTF-8");

        } catch (Exception e) {
            if (is != null) {
                try {
                    is.close();
                } catch (Exception e1) {
                }
            }

            if (fullPath != null) {
                File f = new File(fullPath);
                f.delete();
            }
        }
        return null;
    }

}
