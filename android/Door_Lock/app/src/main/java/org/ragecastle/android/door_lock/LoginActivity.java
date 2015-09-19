package org.ragecastle.android.door_lock;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import java.io.IOException;

import io.particle.android.sdk.cloud.SparkCloud;
import io.particle.android.sdk.cloud.SparkCloudException;
import io.particle.android.sdk.cloud.SparkDevice;
import io.particle.android.sdk.utils.Async;
import io.particle.android.sdk.utils.Toaster;

public class LoginActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        findViewById(R.id.login_button).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(final View v) {
                        final String email = ((EditText) findViewById(R.id.email)).getText().toString();
                        final String password = ((EditText) findViewById(R.id.password)).getText().toString();

                        // Don't:
                        AsyncTask task = new AsyncTask() {
                            @Override
                            protected Object doInBackground(Object[] params) {
                                try {
                                    SparkCloud.get(LoginActivity.this).logIn(email, password);

                                } catch (final SparkCloudException e) {
                                    Runnable mainThread = new Runnable() {
                                        @Override
                                        public void run() {
                                            Toaster.l(LoginActivity.this, e.getBestMessage());
                                            e.printStackTrace();
                                            Log.d("info", e.getBestMessage());
//                                            Log.d("info", e.getCause().toString());
                                        }
                                    };
                                    runOnUiThread(mainThread);

                                }

                                return null;
                            }

                        };

                        Async.executeAsync(SparkCloud.get(v.getContext()), new Async.ApiWork<SparkCloud, SparkDevice>() {

                            public SparkDevice callApi(SparkCloud sparkCloud) throws SparkCloudException, IOException {
                                sparkCloud.logIn(email, password);
                                //TODO: fix this
                                return sparkCloud.getDevice("50ff6a065067545631110587");
                            }

                            @Override
                        public void onSuccess(SparkDevice device){
                                Toaster.l(LoginActivity.this, "Logged In");
                                Intent intent = DoorActivity.buildIntent(LoginActivity.this, device.getID());
                                startActivity(intent);
                            }

                            @Override
                        public void onFailure(SparkCloudException e){
                                Log.e("Something", String.valueOf(e));
                                Toaster.l(LoginActivity.this, "Shit Broke");
                            }
                        });


                    }
                }

        );
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_login, menu);
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
