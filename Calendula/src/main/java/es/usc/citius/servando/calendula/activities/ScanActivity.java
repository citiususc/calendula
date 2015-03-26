package es.usc.citius.servando.calendula.activities;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import es.usc.citius.servando.calendula.R;
import es.usc.citius.servando.calendula.persistence.Medicine;

public class ScanActivity extends ActionBarActivity{

     
    TextView textView;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {        
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan);
        
        Log.d("ScanActivity", "onCreate");
        findViewById(R.id.button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doScan();
            }
        });

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(getResources().getColor(R.color.android_blue_statusbar));
        }
    }

    @Override
    protected void onDestroy() {
        Log.d("ScanActivity", "onDestroy");
        super.onDestroy();
    }

    public void doScan(){
        IntentIntegrator integrator = new IntentIntegrator(this);
        integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE_TYPES);
        integrator.setPrompt("Scan QR");
        //integrator.setResultDisplayDuration(500);
        integrator.initiateScan();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if(result != null && requestCode == IntentIntegrator.REQUEST_CODE) {
            if(result.getContents() == null) {
                Toast.makeText(this, "Cancelled", Toast.LENGTH_LONG).show();
            } else {
                //Toast.makeText(this, "Scanned: " + result.getContents(), Toast.LENGTH_LONG).show();
                Log.d("ScanActivity", result.getContents());
                final String content = result.getContents();
                Class<?> cls = Medicine.class.getClass();

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
