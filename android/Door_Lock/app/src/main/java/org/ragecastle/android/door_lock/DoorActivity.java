package org.ragecastle.android.door_lock;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import java.io.IOException;

import io.particle.android.sdk.cloud.SparkCloud;
import io.particle.android.sdk.cloud.SparkCloudException;
import io.particle.android.sdk.cloud.SparkDevice;
import io.particle.android.sdk.utils.Async;
import io.particle.android.sdk.utils.Toaster;

public class DoorActivity extends Activity {

    private static final String ARG_DEVICEID = "ARG_DEVICEID";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_door);

        //TODO: add check for door state
        // Block to Lock the Door
        findViewById(R.id.lock_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //...
                // Do network work on background thread
                Async.executeAsync(SparkCloud.get(DoorActivity.this), new Async.ApiWork<SparkCloud, Integer>() {
                    @Override
                    public Integer callApi(SparkCloud sparkCloud) throws SparkCloudException, IOException {
                        SparkDevice device = sparkCloud.getDevice(getIntent().getStringExtra(ARG_DEVICEID));
                        Integer locked = 0;
                        try {
                            locked = device.callFunction("lock");
                        } catch (SparkDevice.FunctionDoesNotExistException e) {
                            e.printStackTrace();
                        }
                        return locked;

                    }

                    @Override
                    public void onSuccess(Integer i) // this goes on the main thread
                    {
                        Toaster.l(DoorActivity.this, "Door_Locked");
                    }

                    @Override
                    public void onFailure(SparkCloudException e) {
                        Toaster.l(DoorActivity.this, "Shit");
                        e.printStackTrace();
                    }
                });
            }
        });

        // Block to Unlock the Door
        findViewById(R.id.unlock_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //...
                // Do network work on background thread
                Async.executeAsync(SparkCloud.get(DoorActivity.this), new Async.ApiWork<SparkCloud, Integer>() {
                    @Override
                    public Integer callApi(SparkCloud sparkCloud) throws SparkCloudException, IOException {
                        SparkDevice device = sparkCloud.getDevice(getIntent().getStringExtra(ARG_DEVICEID));
                        Integer locked = 0;
                        try {
                            locked = device.callFunction("unlock");
                        } catch (SparkDevice.FunctionDoesNotExistException e) {
                            e.printStackTrace();
                        }
                        return locked;

                    }

                    @Override
                    public void onSuccess(Integer i) // this goes on the main thread
                    {
                        Toaster.l(DoorActivity.this, "Door_Unlocked");
                    }

                    @Override
                    public void onFailure(SparkCloudException e) {
                        Toaster.l(DoorActivity.this, "Shit");
                        e.printStackTrace();
                    }
                });
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_door, menu);
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

    public static Intent buildIntent(Context ctx, String deviceId) {
        Intent intent = new Intent(ctx, DoorActivity.class);
        intent.putExtra(ARG_DEVICEID, deviceId);

        return intent;
    }
}
