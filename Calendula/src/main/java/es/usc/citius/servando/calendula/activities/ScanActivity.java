package es.usc.citius.servando.calendula.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
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

public class ScanActivity extends CalendulaActivity {


    TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan);
        setupStatusBar(getResources().getColor(R.color.android_blue_darker));
        findViewById(R.id.button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doScan();
            }
        });
    }

    @Override
    protected void onDestroy() {
        Log.d("ScanActivity", "onDestroy");
        super.onDestroy();
    }

    public void doScan() {
        IntentIntegrator integrator = new IntentIntegrator(this);
        integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE_TYPES);
        integrator.setPrompt(getString(R.string.scan_qr));
        //integrator.setResultDisplayDuration(500);
        integrator.initiateScan();
    }

    public static String[] byteArrayToHex(byte[] a) {
        String[] arr = new String[a.length];
        int i = 0;
        for (byte b : a) {
            arr[i++] = String.format("%02x", b & 0xff);

        }
        return arr;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);



        if (result != null && requestCode == IntentIntegrator.REQUEST_CODE && data !=null) {

            byte[] dataBytes = data.getByteArrayExtra("SCAN_RESULT_BYTE_SEGMENTS_0");
            byte[] dataBytes2 = data.getByteArrayExtra("SCAN_RESULT_BYTE_SEGMENTS_1");

            if (result.getContents() == null) {
                Toast.makeText(this, "Cancelled", Toast.LENGTH_LONG).show();
            } else {

                boolean gziped = false;
                String content = result.getContents();

                Log.d("ScanActivity", "SCAN_RESULT_BYTE_SEGMENTS_0 : " + Arrays.toString(byteArrayToHex(dataBytes)));
                Log.d("ScanActivity", "SCAN_RESULT_BYTE_SEGMENTS_1: " + Arrays.toString(byteArrayToHex(dataBytes2)));
                Log.d("ScanActivity", "CONTENTS: " + Arrays.toString(byteArrayToHex(content.getBytes())));

                // first, decode base64 QR content
                byte[] raw = Base64.decode(content.getBytes(), Base64.DEFAULT);

                Log.d("ZIP", "Raw length:" + raw.length + " contents: " + content);

                // now, try decompress GZIP
                if (raw[0] == (byte) 0x1f && raw[1] == (byte) 0x8b) {
                    Log.d("ScanActivity", "Has GZIP magic");

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
                        Log.d("ScanActivity", "Unzipped qr contents: " + content);

                    } catch (Exception e) {
                        Log.e("ScanActivity", "Error unzipping qr contents", e);
                    }
                }

                Intent intent = new Intent(getApplicationContext(), ConfirmSchedulesActivity.class);
                Bundle b = new Bundle();
                b.putString("qr_data", content);
                intent.putExtras(b);
                startActivity(intent);
                finish();
            }
        }
    }

}
