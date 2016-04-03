package com.example.sara.mycompass;


import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.DialogInterface;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.content.DialogInterface.OnClickListener;
import android.app.AlertDialog;
import android.view.MenuItem;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;



public class MyActivity extends AppCompatActivity implements SensorEventListener {
    private SensorManager senSensorManager;
    private Sensor senAccelerometer;
    private long lastUpdate = 0;
    private float last_x, last_y, last_z;

    private AlertDialog mAboutDialog;

    // define the display assembly compass picture
    private ImageView image;

    // record the compass picture angle turned
    private float currentDegree = 0f;

    // device sensor manager
   // private SensorManager mSensorManager;

    TextView tvHeading;

    private ImageView mPointer;
    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private Sensor mMagnetometer;
    private float[] mLastAccelerometer = new float[3];
    private float[] mLastMagnetometer = new float[3];
    private boolean mLastAccelerometerSet = false;
    private boolean mLastMagnetometerSet = false;
    private float[] mR = new float[9];
    private float[] mOrientation = new float[3];
    private float mCurrentDegree = 0f;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my);
        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        senSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        senAccelerometer = senSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        senSensorManager.registerListener(this, senAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mMagnetometer = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);



        Button button = (Button) findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Context context = view.getContext();
                mAboutDialog = new AlertDialog.Builder(context)
                        .setTitle("Accelerometer values")
                        .setMessage("x = " + Float.toString(last_x) + "\ny = " + Float.toString(last_y) + "\nz = " + Float.toString(last_z))
                .setPositiveButton("ok", new OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                        .show();
            }
        });
        Button compass = (Button) findViewById(R.id.button2);
        compass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                RelativeLayout container = (RelativeLayout) findViewById(R.id.main_container);
//                View inflatedLayout= getLayoutInflater().inflate(R.layout.compass, null, false);
//                container.addView(inflatedLayout);

                System.out.println("XXX: " + (getSupportFragmentManager().getBackStackEntryCount()));
                Fragment newFragment = new CompassFragment();
                FragmentTransaction ft = getFragmentManager().beginTransaction();
                ft.add(R.id.my_activity, newFragment, CompassFragment.TAG).addToBackStack(null).commit();

                mPointer = (ImageView) findViewById(R.id.imageViewCompass);

                System.out.println("XXX: " + mPointer);

            }
        });

    }

    @Override
    public void onBackPressed() {
        if (getSupportFragmentManager().getBackStackEntryCount() != 0) {
            getSupportFragmentManager().popBackStack();
            System.out.println("XXX: inside");
        } else {
            super.onBackPressed();
            System.out.println("XXX: nope");
        }
    }

    /** Called when the user clicks the button1 */
    public void sendMessage(View view) {
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_my, menu);
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

    @Override
    public void onSensorChanged(SensorEvent event) {
            if(event.sensor == senAccelerometer) {
                Sensor mySensor = event.sensor;
                float x = 0;
                float y = 0;
                float z = 0;

                if (mySensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                    x = event.values[0];
                    y = event.values[1];
                    z = event.values[2];
                }
                long curTime = System.currentTimeMillis();

                if ((curTime - lastUpdate) > 100) {
                    long diffTime = (curTime - lastUpdate);
                    lastUpdate = curTime;
                }

                last_x = x;
                last_y = y;
                last_z = z;
            }else {
                if (event.sensor == mAccelerometer) {
                    System.arraycopy(event.values, 0, mLastAccelerometer, 0, event.values.length);
                    mLastAccelerometerSet = true;
                } else if (event.sensor == mMagnetometer) {
                    System.arraycopy(event.values, 0, mLastMagnetometer, 0, event.values.length);
                    mLastMagnetometerSet = true;
                }
                if (mLastAccelerometerSet && mLastMagnetometerSet) {
                    SensorManager.getRotationMatrix(mR, null, mLastAccelerometer, mLastMagnetometer);
                    SensorManager.getOrientation(mR, mOrientation);
                    float azimuthInRadians = mOrientation[0];
                    float azimuthInDegress = (float) (Math.toDegrees(azimuthInRadians) + 360) % 360;
                    RotateAnimation ra = new RotateAnimation(
                            mCurrentDegree,
                            -azimuthInDegress,
                            Animation.RELATIVE_TO_SELF, 0.5f,
                            Animation.RELATIVE_TO_SELF,
                            0.5f);

                    ra.setDuration(250);

                    ra.setFillAfter(true);

                    if (mPointer != null) {
                        System.out.println("XXX: inne i else");
                        mPointer.startAnimation(ra);
                    }
                    mCurrentDegree = -azimuthInDegress;
                }
            }

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    protected void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(this, mAccelerometer);
        mSensorManager.unregisterListener(this, mMagnetometer);

    }

    protected void onResume() {
        super.onResume();
        mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_GAME);
        mSensorManager.registerListener(this, mMagnetometer, SensorManager.SENSOR_DELAY_GAME);
    }


}
