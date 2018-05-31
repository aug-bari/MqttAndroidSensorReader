package com.github.augbari.mqttandroidsensorreader

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
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
        when {
            event?.sensor?.type == Sensor.TYPE_GYROSCOPE -> {
                val gyroValues = event.values

                val gyroVector = "[${gyroValues?.get(0)}, ${gyroValues?.get(1)}, ${gyroValues?.get(2)}]"
                gyroText.text = "GYRO: $gyroVector"

                mqttHelper.publishMessage("gyro", gyroVector)

            }
            event?.sensor?.type == Sensor.TYPE_LINEAR_ACCELERATION -> {
                val accValues = event.values

                val accVector = "[${accValues?.get(0)}, ${accValues?.get(1)}, ${accValues?.get(2)}]"
                accelerationText.text = "ACCELERATION: $accVector"

                mqttHelper.publishMessage("acceleration", accVector)
            }
        }
    }


    private fun validateForms(): Boolean = (serverUri.isValid()
            && clientId.isValid()
            && accPublishTopic.isValid()
            && gyroPublishTopic.isValid())

    private fun LinearLayout.show() {
        this.visibility = View.VISIBLE
    }

    private fun LinearLayout.hide() {
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

