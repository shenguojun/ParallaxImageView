package com.shengj.parallaximageview

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.view.Surface
import android.view.WindowManager
import androidx.core.content.ContextCompat.getSystemService
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner

/**
 * @description 获取手机重力变化
 *
 * @author shengj
 * @date 2021/7/26 13:56
 */
class GravitySensor(private val context: Context) : DefaultLifecycleObserver, SensorEventListener {

    companion object {
        // 重力加速度
        const val G = 9.80665f

        // 对于刷新率为120Hz的屏幕，大概8毫秒需要刷新一次
        const val REFRESH_RATE = 8000
    }

    private var sensorManager: SensorManager? = null
    var listener: GravityListener? = null

    override fun onCreate(owner: LifecycleOwner) {
        sensorManager = getSystemService(context, SensorManager::class.java)
    }

    override fun onResume(owner: LifecycleOwner) {
        sensorManager?.registerListener(
            this,
            sensorManager?.getDefaultSensor(Sensor.TYPE_GRAVITY),
            REFRESH_RATE
        )
    }

    override fun onPause(owner: LifecycleOwner) {
        sensorManager?.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_GRAVITY) {
            var (x, y) = event.values
            val rotation = (context
                .getSystemService(Context.WINDOW_SERVICE) as WindowManager)
                .defaultDisplay
                .rotation
            when (rotation) {
                Surface.ROTATION_90 -> {
                    val tmp = x
                    x = -y
                    y = tmp
                }
                Surface.ROTATION_180 -> {
                    x = -x
                    y = -y
                }
                Surface.ROTATION_270 -> {
                    val tmp = x
                    x = y
                    y = -tmp
                }
                Surface.ROTATION_0 -> {
                }
            }
            listener?.onGravityChange(-x, y)
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
    }

    interface GravityListener {
        /**
         * 根据重力返回重力偏移，一个单位重力加速度[G]数值为9.80665f
         * 每[REFRESH_RATE]毫秒会调用一次，注意不要调用耗时任务
         *
         * @param x 左右角度变化，手机左边朝下为-[G]，右边朝下为[G]，平放为0
         * @param y 上下角度变化，手机上边朝下为-[G]，下边朝下为[G]，平方为0
         */
        fun onGravityChange(x: Float, y: Float)
    }
}