package com.github.augbari.mqttandroidsensorreader;

import android.content.Context;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.jetbrains.annotations.NotNull;

import kotlin.Function;
import kotlin.Unit;
import kotlin.jvm.internal.Reflection;


public class MqttHelper {

    private String serverUri;
    private String clientId;
    private MqttConnectOptions mqttConnectOptions;

    private MqttAndroidClient mqttAndroidClient;

    public MqttHelper(Context context, String serverUri, String clientId) {
        this.serverUri = serverUri;
        this.clientId = clientId;
        this.mqttAndroidClient = new MqttAndroidClient(context, serverUri, clientId);
        this.mqttConnectOptions = new MqttConnectOptions();
        mqttConnectOptions.setAutomaticReconnect(true);
        mqttConnectOptions.setCleanSession(false);
    }

    public void connect(IMqttActionListener listener) throws MqttException {
        mqttAndroidClient.connect(mqttConnectOptions, null, listener);
    }

    public void setUsername(String username) {
        mqttConnectOptions.setUserName(username);
    }

    public void setPassword(String password) {
        mqttConnectOptions.setPassword(password.toCharArray());
    }

    public void publishMessage(String topic, String payload) throws MqttException {
        MqttMessage message = new MqttMessage();
        message.setPayload(payload.getBytes());
        mqttAndroidClient.publish(topic, message);
    }

    public void publishMessage(String topic, byte[] payload) throws MqttException {
        MqttMessage message = new MqttMessage();
        message.setPayload(payload);
        mqttAndroidClient.publish(topic, message);
    }

}
