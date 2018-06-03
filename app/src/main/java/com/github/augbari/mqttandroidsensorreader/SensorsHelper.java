package com.github.augbari.mqttandroidsensorreader;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

public class SensorsHelper {
    private SensorManager sensorManager;
    double accX, accY, accZ;
    double gyroX, gyroY, gyroz;

    public SensorsHelper(Context context) {
        sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
    }

    public void registerSensor(int sensorType, SensorEventListener listener) {
        sensorManager.registerListener(listener, sensorManager.getDefaultSensor(sensorType), SensorManager.SENSOR_DELAY_FASTEST);
    }

    public  void unregisterSensor(SensorEventListener listener){
        sensorManager.unregisterListener(listener);
    }
}
