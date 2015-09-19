package org.ragecastle.android.door_lock;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import java.io.IOException;

import io.particle.android.sdk.cloud.SparkCloud;
import io.particle.android.sdk.cloud.SparkCloudException;
import io.particle.android.sdk.cloud.SparkDevice;
import io.particle.android.sdk.utils.Async;
import io.particle.android.sdk.utils.Toaster;

public class ValueActivity extends Activity {

    private static final String ARG_VALUE = "ARG_VALUE";
    private static final String ARG_DEVICEID = "ARG_DEVICEID";

    private TextView tv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_value);
        tv = (TextView) findViewById(R.id.value);
        tv.setText(String.valueOf(getIntent().getIntExtra(ARG_VALUE, 0)));

        findViewById(R.id.refresh_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //...
                // Do network work on background thread
                Async.executeAsync(SparkCloud.get(ValueActivity.this), new Async.ApiWork<SparkCloud, Integer>() {
                    @Override
                    public Integer callApi(SparkCloud ParticleCloud) throws SparkCloudException, IOException {
                        SparkDevice device = ParticleCloud.getDevice(getIntent().getStringExtra(ARG_DEVICEID));
                        Integer variable;
                        try {
                            variable = device.getVariable("analogvalue");
                        } catch (SparkDevice.VariableDoesNotExistException e) {
                            Toaster.l(ValueActivity.this, e.getMessage());
                            variable = -1;
                        }
                        return variable;
                    }

                    @Override
                    public void onSuccess(Integer i) // this goes on the main thread
                    {
                        tv.setText(i.toString());
                    }

                    @Override
                    public void onFailure(SparkCloudException e) {
                        e.printStackTrace();
                    }
                });
            }
        });
    }

    public static Intent buildIntent(Context ctx, Integer value, String deviceid) {
        Intent intent = new Intent(ctx, ValueActivity.class);
        intent.putExtra(ARG_VALUE, value);
        intent.putExtra(ARG_DEVICEID, deviceid);

        return intent;
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_value, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
