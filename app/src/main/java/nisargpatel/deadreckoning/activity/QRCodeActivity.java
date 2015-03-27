package nisargpatel.deadreckoning.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Toast;

import com.dm.zbar.android.scanner.ZBarConstants;
import com.dm.zbar.android.scanner.ZBarScannerActivity;

import net.sourceforge.zbar.Symbol;

public final class QRCodeActivity extends Activity {

    public static final int ZBAR_QR_SCANNER_REQUEST = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Note: this activity does not have an XML layout associated with it.

        runScanner();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            Toast.makeText(getApplicationContext(), data.getStringExtra(ZBarConstants.SCAN_RESULT), Toast.LENGTH_LONG).show();
        } else if (resultCode == RESULT_CANCELED) {
            Toast.makeText(getApplicationContext(), "QR Code Scanner canceled.", Toast.LENGTH_SHORT).show();
        }

        finish();

    }

    private void runScanner() {
        if (isCameraAvailable()) {
            Intent myIntent = new Intent(getApplicationContext(), ZBarScannerActivity.class);
            myIntent.putExtra(ZBarConstants.SCAN_MODES, new int[]{Symbol.QRCODE});
            startActivityForResult(myIntent, ZBAR_QR_SCANNER_REQUEST);
        } else {
            Toast.makeText(getApplicationContext(), "Camera not available.", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean isCameraAvailable() {
        return getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA);
    }

}
