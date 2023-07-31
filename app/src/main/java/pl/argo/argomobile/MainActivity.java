package pl.argo.argomobile;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.RadioButton;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import org.ros.android.MessageCallable;
import org.ros.android.view.RosTextView;
import org.ros.node.NodeConfiguration;
import org.ros.node.NodeMainExecutor;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;

import io.github.controlwear.virtual.joystick.android.JoystickView;
import pl.argo.argomobile.data.Rover;

public class MainActivity extends RosActivity { // AppCompatActivity -?

    public static final String TAG = "ArgoMobile";
    private ParametrizedTalker talker;
    private RosTextView<std_msgs.String> rosTextView;

    private double scale = 0.5;
    private double x, y, z;

    public MainActivity() {
        super("ArgoMobile", "ArgoMobile", null);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.settings_menu, menu);
        return true;
    }

//    ActivityResultLauncher<Intent> activityResultLauncher = registerForActivityResult(
//            new ActivityResultContracts.StartActivityForResult(),
//            new ActivityResultCallback<ActivityResult>() {
//                @Override
//                public void onActivityResult(ActivityResult result) {
//                    if (result.getResultCode() == 1) {
//                        Intent intent = result.getData();
//                        chosenRoverId = intent.getIntExtra("roverId", -1);
//                        updateActivityContentAndRoverObject();
//                    }
//                }
//            }
//    );

    @SuppressLint("DefaultLocale")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        rosTextView = findViewById(R.id.rosTextView);
        rosTextView.setTopicName(null);
        rosTextView.setMessageType(std_msgs.String._TYPE);
        rosTextView.setMessageToStringCallable(new MessageCallable<String, std_msgs.String>() {
            @Override
            public String call(std_msgs.String message) {
                return message.getData();
            }
        });

        updateActivityContentAndRoverObject();

        JoystickView cmdVelJoystick = findViewById(R.id.cmd_vel_joystick);

        cmdVelJoystick.setOnMoveListener((angle, strength) -> {
            ((TextView) findViewById(R.id.cmd_vel_joystick_info)).setText(countJoystickSwingValues(angle, strength));

            if (!((Switch) findViewById(R.id.isAngular)).isChecked()) z = 0;
            else {
                z = -x;
                x = 0;
            }

            try {
                talker.getLinear().setX(x);
                talker.getLinear().setY(y);
                talker.getAngular().setZ(z);
            } catch (NullPointerException e) {
                Toast.makeText(MainActivity.this, R.string.unable_to_communicate_with_ros_server, Toast.LENGTH_SHORT).show();
            }
        });

        SeekBar[] manips = new SeekBar[6];
        for (int i = 0; i < 6; ++i) {
            manips[i] = findViewById(getResId(String.format("manip_%d_seekbar", (i + 1)), R.id.class));

            int iCopy = i;
            manips[i].setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    try {
                        talker.jointsEfforts[iCopy] = seekBar.getProgress() - 100;
                    } catch (NullPointerException e) {
                        Toast.makeText(MainActivity.this, R.string.unable_to_communicate_with_ros_server, Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {
                    //nic
                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                    seekBar.setProgress(100);
                }
            });

            SeekBar seekBar = findViewById(getResId(String.format("manip_%d_seekbar", (i + 1)), R.id.class));
            TextView textView = findViewById(getResId(String.format("manip_%d_info", (i + 1)), R.id.class));

            seekBar.setVisibility(View.INVISIBLE);
            textView.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.settingsButton) {
//            Intent intent = new Intent(this, RoverChooserActivity.class);
//            activityResultLauncher.launch(intent);
            Toast.makeText(this, "No i czego ty oczekujesz kurwa", Toast.LENGTH_SHORT).show();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public static int getResId(String resName, Class<?> c) {
        try {
            Field idField = c.getDeclaredField(resName);
            return idField.getInt(idField);
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

    /**
     * Metoda aktualizująca aktualne activity oraz obiekt samego łazika — w zależności od wybranego łazika (różne joysticki dla różnych łazików)
     */
    private void updateActivityContentAndRoverObject() {
        Rover rover = new Rover(1, "Dzik", "argo_mini", new ArrayList<>(Arrays.asList(
                "front_left_wheel_joint",
                "front_right_wheel_joint",
                "middle_left_wheel_joint",
                "middle_right_wheel_joint",
                "rear_left_wheel_joint",
                "rear_right_wheel_joint"
        )));

        TextView roverName = findViewById(R.id.roverName);

        roverName.setText(getString(R.string.current_rover) + rover.getName());

        if (talker != null) {
            if (rover.getJointNames() != null) talker.initializeJointsEfforts(rover.getJointNames().size());
            else talker.initializeJointsEfforts(0);
            talker.setRover(rover);
        }

        for (int i = 0; i < 6; ++i) {
            SeekBar seekBar = findViewById(getResId(String.format("manip_%d_seekbar", (i + 1)), R.id.class));
            TextView textView = findViewById(getResId(String.format("manip_%d_info", (i + 1)), R.id.class));

            if (rover.getJointNames() != null && i < rover.getJointNames().size()) {
                seekBar.setVisibility(View.VISIBLE);
                textView.setVisibility(View.VISIBLE);
                textView.setText(rover.getJointNames().get(i));
            } else {
                seekBar.setVisibility(View.INVISIBLE);
                textView.setVisibility(View.INVISIBLE);
            }
        }
    }

    @Override
    protected void init(NodeMainExecutor nodeMainExecutor) {
        talker = new ParametrizedTalker();

        // At this point, the user has already been prompted to either enter the URI
        // of a master to use or to start a master locally.

        // The user can easily use the selected ROS Hostname in the master chooser activity.
        NodeConfiguration nodeConfiguration = NodeConfiguration.newPublic(getRosHostname());
        nodeConfiguration.setMasterUri(getMasterUri());
        nodeMainExecutor.execute(talker, nodeConfiguration);


        // The RosTextView is also a NodeMain that must be executed in order to
        // start displaying incoming messages.
        nodeMainExecutor.execute(rosTextView, nodeConfiguration);
    }


    public void onRadioButtonClicked(View view) {
        // Is the button now checked?
        boolean checked = ((RadioButton) view).isChecked();

        // Check which radio button was clicked
        switch (view.getId()) {
            case R.id.radio_0_2:
                if (checked)
                    scale = 0.2;
                break;
            case R.id.radio_0_5:
                if (checked)
                    scale = 0.5;
                break;
            case R.id.radio_1:
                if (checked)
                    scale = 1;
                break;
            case R.id.radio_3:
                if (checked)
                    scale = 2;
                break;
            case R.id.radio_6:
                if (checked)
                    scale = 4;
                break;
        }
        Log.d("scale = ", String.valueOf(scale));
    }

    private String countJoystickSwingValues(int angle, int strength) {
        x = Math.round(strength * Math.cos(angle * Math.PI / 180) * 100 * scale / 100.00) / 100.00;
        y = Math.round(strength * Math.sin(angle * Math.PI / 180) * 100 * scale / 100.00) / 100.00;

        String out = "";
        out += "angle = " + angle + "°\n";
        out += "strength = " + strength + "%\n";
        out += "x = " + x + "\n";
        out += "y = " + y + "\n";

        return out;
    }
}