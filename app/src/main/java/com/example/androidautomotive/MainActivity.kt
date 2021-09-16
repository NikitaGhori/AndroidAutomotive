package com.example.androidautomotive

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.car.Car
import android.car.hardware.CarSensorManager
import android.content.ComponentName
import android.content.Context
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity


class MainActivity : AppCompatActivity() {
    private lateinit var car: Car
    private var kmph: Int = 0
    private var mph: Float = 0.0f
    private lateinit var txtCarSpeed: TextView
    private lateinit var txtMph: TextView
    private lateinit var notificationChannel: NotificationChannel
    private lateinit var notificationManager: NotificationManager
    private lateinit var builder: Notification.Builder
    private val channelId = "12345"
    private val description = "Test Notification"
    private val permissions = arrayOf(Car.PERMISSION_SPEED)
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        txtCarSpeed = findViewById(R.id.txtCarSpeed)
        txtMph = findViewById(R.id.txtMPH)
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        initCar()
    }

    //set up a car service connection
    //invoke check and request functions before establishing a connection
    @Suppress("DEPRECATION")
    override fun onResume() {
        super.onResume()

        if (checkSelfPermission(permissions[0]) == PackageManager.PERMISSION_GRANTED) {
            if (!car.isConnected && !car.isConnecting) {
                car.connect()
            }
        } else {
            requestPermissions(permissions, 0)
        }

    }

    override fun onPause() {
        if (car.isConnected) {
            car.disconnect()
        }

        super.onPause()
    }

    //create our object and establish a connection with the car service
    @Suppress("DEPRECATION")
    private fun initCar() {
        if (!packageManager.hasSystemFeature(PackageManager.FEATURE_AUTOMOTIVE)) {
            return
        }

        if (::car.isInitialized) {
            return
        }

        car = Car.createCar(this, object : ServiceConnection {
            override fun onServiceConnected(componentName: ComponentName, iBinder: IBinder) {
                watchSpeedSensor()
            }

            override fun onServiceDisconnected(componentName: ComponentName) {
                // on failure callback
            }
        })
    }

    //gather data from the speed sensor
    @Suppress("DEPRECATION")
    private fun watchSpeedSensor() {

        val sensorManager = car.getCarManager(Car.SENSOR_SERVICE) as CarSensorManager

        sensorManager.registerListener(
            { carSensorEvent ->
                kmph = (carSensorEvent.floatValues[0] * 3.6).toFloat().toInt()
                txtCarSpeed.text = "Car Speed is : " + kmph.toString()
                mph = (0.6214 * kmph).toFloat()
                txtMph.text = "Speen in mph : " + mph.toString()
                if (kmph >= 140) {
                    getNotification()
                }
            },
            CarSensorManager.SENSOR_TYPE_CAR_SPEED,
            CarSensorManager.SENSOR_RATE_NORMAL
        )

    }

    private fun getNotification() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationChannel =
                NotificationChannel(channelId, description, NotificationManager.IMPORTANCE_HIGH)
            notificationChannel.lightColor = Color.BLUE
            notificationChannel.enableVibration(true)
            notificationManager.createNotificationChannel(notificationChannel)
            builder = Notification.Builder(this, channelId)
                .setContentTitle("NOTIFICATION")
                .setContentText("Please slow down")
                .setSmallIcon(R.drawable.ic_car_24)
        }
        notificationManager.notify(0, builder.build())

    }

    @Suppress("DEPRECATION")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (permissions[0] == Car.PERMISSION_SPEED && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            if (!car.isConnected && !car.isConnecting) {
                car.connect()
            }
        }
    }

}