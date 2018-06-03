package com.github.augbari.mqttandroidsensorreader

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.EditText
import kotlinx.android.synthetic.main.activity_main.*
import org.eclipse.paho.client.mqttv3.IMqttActionListener
import org.eclipse.paho.client.mqttv3.IMqttToken
import org.jetbrains.anko.sdk25.coroutines.onClick
import org.jetbrains.anko.toast

class MainActivity : AppCompatActivity(), SensorEventListener {

    private val mqttHelper by lazy {
        MqttHelper(this, serverUri.value(), clientId.value())
    }

    private val sensorsHelper by lazy {
        SensorsHelper(this)
    }

    var collectingData = false
    var startedCollettingGyro = false
    var startedCollectingAcc = false

    lateinit var currentGyroValues: FloatArray
    lateinit var currentAccValues: FloatArray

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        connect.onClick {
            if (validateForms()) {
                mqttHelper.setUsername(username.value())
                mqttHelper.setPassword(password.value())
                mqttHelper.connect(object  : IMqttActionListener{
                    override fun onSuccess(asyncActionToken: IMqttToken?) {
                        startCollectingData()
                    }

                    override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                        toast(exception?.message.toString())
                    }
                })
            } else {
                toast("Please fill all the fields")
            }
        }

        sendSingleJson.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                jsonTopic.show()
                accPublishTopic.hide()
                gyroPublishTopic.hide()
            } else {
                jsonTopic.hide()
                accPublishTopic.show()
                gyroPublishTopic.show()
            }
        }
    }

    private fun startCollectingData() {
        // First change layout visibility
        connectLayout.hide()
        dataLayout.show()

        registerSensors()
        collectingData = true
    }

    private fun registerSensors() {
        sensorsHelper.registerSensor(Sensor.TYPE_LINEAR_ACCELERATION, this)
        sensorsHelper.registerSensor(Sensor.TYPE_GYROSCOPE, this)
    }

    private fun unregisterSensors(){
        sensorsHelper.unregisterSensor(this)
    }


    override fun onSensorChanged(event: SensorEvent?) {
        val jsonEnabled = sendSingleJson.isChecked
        when {
            event?.sensor?.type == Sensor.TYPE_GYROSCOPE -> {
                currentGyroValues = event.values

                startedCollettingGyro = true
                if (!jsonEnabled) mqttHelper.publishMessage("gyro",
                        "[${currentGyroValues.get(0)}, ${currentGyroValues.get(1)}, ${currentGyroValues.get(2)}]")
            }
            event?.sensor?.type == Sensor.TYPE_LINEAR_ACCELERATION -> {
                currentAccValues = event.values

                startedCollectingAcc = true
                if (!jsonEnabled) mqttHelper.publishMessage("acceleration",
                        "[${currentAccValues.get(0)}, ${currentAccValues.get(1)}, ${currentAccValues.get(2)}]")
            }
        }

        if (jsonEnabled) {
            if (startedCollectingAcc && startedCollettingGyro) {
                mqttHelper.publishMessage(jsonTopic.value(),
                        "{\"gyro\":{\"x\":${currentGyroValues[0]},\"y\":${currentGyroValues[1]},\"z\":${currentGyroValues[2]}}," +
                                "\"accel\":{\"x\":${currentAccValues[0]},\"y\":${currentAccValues[1]},\"z\":${currentAccValues[2]}}}")
            }
        }
    }


    private fun validateForms(): Boolean = (serverUri.isValid()
            && clientId.isValid())

    private fun View.show() {
        this.visibility = View.VISIBLE
    }

    private fun View.hide() {
        this.visibility = View.GONE
    }

    private fun EditText.value(): String? {
        return this.text.toString()
    }

    private fun EditText.isValid(): Boolean {
        return this.text.toString().isNotEmpty()
    }

    override fun onResume() {
        super.onResume()
        if (collectingData) {
            registerSensors()
        }
    }

    override fun onPause() {
        super.onPause()
        unregisterSensors()
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterSensors()
    }

    override fun onAccuracyChanged(p0: Sensor?, p1: Int) {}
}

