package callscreenoff.rejh.com.callscreenoff;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class CsoActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cso);

        Intent serviceIntent = new Intent(CsoActivity.this, CsoService.class);
        startService(serviceIntent);

    }
}
