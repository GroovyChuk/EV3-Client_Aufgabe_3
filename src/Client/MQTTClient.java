package src.Client;

import org.fusesource.mqtt.client.*;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

import static src.Client.JConstants.PARTICLE_SENSOR_AMOUNT;

/**
 * Created by Kalaman on 09.01.18.
 */
public class MQTTClient {
    private MQTT mqtt;
    private static BlockingConnection connection;
    private Topic [] topic;

    private ArrayList<MQTTListener> mqttListener;
    private static final String SERVER_IP = "localhost";
    private static final int SERVER_PORT = 1883;

    public static final String TOPIC_LOG = "log";
    public static final String TOPIC_DRIVE = "drive";
    public static final String TOPIC_SONIC_DISTANCE = "distance";

    public MQTTClient() {
        mqtt = new MQTT();
        mqttListener = new ArrayList<MQTTListener>();
        topic = new Topic[] {new Topic(TOPIC_LOG, QoS.EXACTLY_ONCE),new Topic(TOPIC_SONIC_DISTANCE, QoS.EXACTLY_ONCE)};

        try {
            mqtt.setHost(SERVER_IP, SERVER_PORT);
            connection = mqtt.blockingConnection();
            connection.connect();
            JConsolePanel.writeToConsole("Connected to MQTT-Server [" + SERVER_IP + ":" + SERVER_PORT + "]");
            connection.subscribe(topic);

        }
        catch (Exception e){
            e.printStackTrace();
            JConsolePanel.writeToConsole("Connection with MQTT-Server failed " + SERVER_IP + ":" + SERVER_PORT);
        }
    }

    public interface MQTTListener {
        public void onLogReceived(String jsonData);
        public void onUltrasonicDistanceReceived(double [] distancesInCM,boolean xaxis);
    }

    public boolean addMQTTListener(MQTTListener listener) {
        return mqttListener.add(listener);
    }

    public boolean removeMQTTListener(MQTTListener listener) {
        return mqttListener.remove(listener);
    }

    public void startListeningThread () {
        new Thread() {
            @Override
            public void run() {
                try {
                    while(true) {
                        Message message = connection.receive();
                        String payload = new String(message.getPayload(), StandardCharsets.UTF_8);

                        if (message.getTopic().equals(TOPIC_LOG)) {
                            for (MQTTListener listener : mqttListener)
                                listener.onLogReceived(payload);
                        }
                        else if (message.getTopic().equals(TOPIC_SONIC_DISTANCE)){
                            double [] degreeSensor = new double[PARTICLE_SENSOR_AMOUNT];

                            JSONObject obj = new JSONObject(payload);
                            int degIncr = 360 / JConstants.PARTICLE_SENSOR_AMOUNT;

                            boolean xaxis = obj.getBoolean("x_axis");

                            for (int i=0;i<PARTICLE_SENSOR_AMOUNT;++i) {
                                degreeSensor[i] = obj.getDouble(String.valueOf(degIncr * (i+1)));
                            }

                            for (MQTTListener listener : mqttListener)
                                listener.onUltrasonicDistanceReceived(degreeSensor,xaxis);
                        }
                        message.ack();
                    }
                }
                catch (Exception e) {
                    System.out.println("Sind beide ParticleSensorAmounts gleich ?");
                    e.printStackTrace();
                }
            }

        }.start();
    }

    public static void publish (String message, String topic) {
        try {
            connection.publish(topic, message.getBytes() ,QoS.EXACTLY_ONCE, false);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void publishLog (String msg) {
        publish(msg,TOPIC_LOG);
    }

}