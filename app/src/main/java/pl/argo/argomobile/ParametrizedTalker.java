package pl.argo.argomobile;

import android.util.Log;

import org.ros.concurrent.CancellableLoop;
import org.ros.message.Time;
import org.ros.namespace.GraphName;
import org.ros.node.AbstractNodeMain;  // This library give us the AbstractNodeMain interface (see ahead)
import org.ros.node.ConnectedNode;
import org.ros.node.NodeMain;
import org.ros.node.topic.Publisher;  // Import the publisher

import java.util.Calendar;

import geometry_msgs.Twist;
//import sensor_msgs.JointState;

//import lombok.Data;
import pl.argo.argomobile.data.Rover;
import sensor_msgs.JointState;


/**
 * A simple {@link Publisher} {@link NodeMain}.
 */

public class ParametrizedTalker extends AbstractNodeMain { // Java nodes NEEDS to implement AbstractNodeMain

    private static final int sleepTime = 100; // czas pomiędzy kolejnymi wywołaniami wysyłania wiadomości wyrażony w milisekundach
    int seq = 0;
    boolean isAngular = false;

    Vector3Implementation linear = new Vector3Implementation();
    Vector3Implementation angular = new Vector3Implementation();
    Publisher<geometry_msgs.Twist> twistPublisher;
    Publisher<sensor_msgs.JointState> jointStatePublisher;
    Rover rover;

    private Twist twist; // http://docs.ros.org/en/melodic/api/geometry_msgs/html/msg/Twist.html
    private JointState joints; // http://docs.ros.org/en/melodic/api/sensor_msgs/html/msg/JointState.html
    double[] jointsEfforts = new double[6]; // wartości effortu od -100 do 100

    @Override
    public GraphName getDefaultNodeName() {
        return GraphName.of("rosjava/talker");
    }

    @Override
    public void onStart(final ConnectedNode connectedNode) {
        linear = new Vector3Implementation();
        angular = new Vector3Implementation();

        connectedNode.executeCancellableLoop(new CancellableLoop() { // This CancellableLoop will be canceled automatically when the node shuts down

            @Override
            protected void setup() { }

            @Override
            protected void loop() throws InterruptedException {
                if(rover != null) {
                    twistPublisher = connectedNode.newPublisher("/" + rover.getTopicPrefix() + "/cmd_vel", "geometry_msgs/Twist");
                    jointStatePublisher = connectedNode.newPublisher("/" + rover.getTopicPrefix() + "/joint_states", sensor_msgs.JointState._TYPE);

                    long startTime = Calendar.getInstance().getTimeInMillis();

                    twist = twistPublisher.newMessage(); // Init a msg variable that of the publisher type

                    /*
                     * Aplikacja obsługująca sterowanie joystickiem na komputerze miała wartości od -0.5 do 0.5 dla osi linearX i linearY,
                     * a od -2.0 do 2.0 dla osi angularZ - stąd takie przeliczniki
                     */
                    twist.getLinear().setX(linear.getY());//zamienione osie X i Y (tak mają roboty)
                    twist.getLinear().setY(-linear.getX());
                    twist.getLinear().setZ(linear.getZ());

                    twist.getAngular().setX(angular.getX());
                    twist.getAngular().setY(angular.getY());
                    twist.getAngular().setZ(angular.getZ() * 4);

                    twistPublisher.publish(twist);

//                    if(rover.getJointNames() != null) {
                    if(!rover.getJointNames().isEmpty()) {
                        joints = jointStatePublisher.newMessage();

                        joints.getHeader().setSeq(++seq);
                        joints.getHeader().setStamp(Time.fromMillis(System.currentTimeMillis()));//dodawanie dodatkowo frame_id jest niepotrzebne

                        joints.setName(rover.getJointNames());
                        //manips.setPosition(manipsStates);
                        //manips.setVelocity(manipsStates);
                        joints.setEffort(jointsEfforts);
                        jointStatePublisher.publish(joints);
                    }

                    long elapsedTime = Calendar.getInstance().getTimeInMillis() - startTime;

                    if (elapsedTime < sleepTime) {
                        Thread.sleep(sleepTime - elapsedTime);
                    }
                    else {
                        Log.e(MainActivity.TAG, "loop: Wysyłanie wiadomości trwało więcej niż domyślny czas na to przeznaczony (" + elapsedTime +" ms).");
                    }
                }
            }
        });

    }

    public Vector3Implementation getLinear() {
        return linear;
    }

    public Vector3Implementation getAngular() {
        return angular;
    }

    public void setRover(Rover rover) {
        this.rover = rover;
    }

    public void initializeJointsEfforts(int size) {
        jointsEfforts = new double[size];
    }

}
