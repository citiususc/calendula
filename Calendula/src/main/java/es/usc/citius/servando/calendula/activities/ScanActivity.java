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
 *    along with this software.  If not, see <http://www.gnu.org/licenses>.
 */

package es.usc.citius.servando.calendula.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.zip.GZIPInputStream;

import es.usc.citius.servando.calendula.CalendulaActivity;
import es.usc.citius.servando.calendula.R;
import es.usc.citius.servando.calendula.database.DB;
import es.usc.citius.servando.calendula.util.LogUtil;

public class ScanActivity extends CalendulaActivity {


    private static final String TAG = "ScanActivity";
    TextView textView;
    String afterScanPkg;
    String afterScanCls;
    Long patientId;

    public static String[] byteArrayToHex(byte[] a) {
        String[] arr = new String[a.length];
        int i = 0;
        for (byte b : a) {
            arr[i++] = String.format("%02x", b & 0xff);

        }
        return arr;
    }

    public void doScan() {
        IntentIntegrator integrator = new IntentIntegrator(this);
        integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE_TYPES);
        integrator.setPrompt(getString(R.string.scan_qr));
        //integrator.setResultDisplayDuration(500);
        integrator.initiateScan();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        int color = DB.patients().getActive(this).getColor();
        setContentView(R.layout.activity_scan);
        setupStatusBar(color);
        findViewById(R.id.container).setBackgroundColor(color);
        findViewById(R.id.button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doScan();
            }
        });

        afterScanPkg = getIntent().getStringExtra("after_scan_pkg");
        afterScanCls = getIntent().getStringExtra("after_scan_cls");
        patientId = getIntent().getLongExtra("patient_id", -1);
    }

    @Override
    protected void onDestroy() {
        LogUtil.d(TAG, "onDestroy");
        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);


        if (result != null && requestCode == IntentIntegrator.REQUEST_CODE && data != null) {

            byte[] dataBytes = data.getByteArrayExtra("SCAN_RESULT_BYTE_SEGMENTS_0");

            if (result.getContents() == null) {
                Toast.makeText(this, "Cancelled", Toast.LENGTH_LONG).show();
            } else {

                boolean gziped = false;
                String content = result.getContents();

                LogUtil.d(TAG, "SCAN_RESULT_BYTE_SEGMENTS_0 : " + Arrays.toString(byteArrayToHex(dataBytes)));
                LogUtil.d(TAG, "CONTENTS: " + Arrays.toString(byteArrayToHex(content.getBytes())));

                // first, decode base64 QR content
                byte[] raw = Base64.decode(content.getBytes(), Base64.DEFAULT);

                LogUtil.d(TAG, "Raw length:" + raw.length + " contents: " + content);

                // now, try decompress GZIP
                if (raw[0] == (byte) 0x1f && raw[1] == (byte) 0x8b) {
                    LogUtil.d(TAG, "Has GZIP magic");

                    Reader reader;
                    StringWriter writer;
                    try {
                        ByteArrayInputStream is = new ByteArrayInputStream(raw);
                        GZIPInputStream gis = new GZIPInputStream(is);
                        reader = new InputStreamReader(gis);
                        writer = new StringWriter();

                        char[] buffer = new char[10240];
                        for (int length; (length = reader.read(buffer)) > 0; ) {
                            writer.write(buffer, 0, length);
                        }

                        content = writer.toString();
                        LogUtil.d(TAG, "Unzipped qr contents: " + content);

                    } catch (Exception e) {
                        LogUtil.e(TAG, "Error unzipping qr contents", e);
                    }
                }

                Intent intent = new Intent();
                intent.setClassName(afterScanPkg, afterScanCls);
                Bundle b = new Bundle();
                b.putString("qr_data", content);
                b.putLong("patient_id", patientId);
                intent.putExtras(b);
                startActivity(intent);
                finish();
            }
        }
    }

}
