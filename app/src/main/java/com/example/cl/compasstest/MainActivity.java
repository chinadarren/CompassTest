package com.example.cl.compasstest;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;

public class MainActivity extends Activity {

    private SensorManager sensorManager;
    private ImageView compassImg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        compassImg = (ImageView) findViewById(R.id.compass_img);

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        //地磁传感器实例
        Sensor magneticSensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        //加速度传感器实例
        Sensor accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        //注册监听地磁传感器，要求精确度比较高，使用SENSOR_DELAY_GAME参数
        sensorManager.registerListener(listener, magneticSensor, SensorManager.SENSOR_DELAY_GAME);
       //注册监听加速度传感器，要求精确度比较高，使用SENSOR_DELAY_GAME参数
        sensorManager.registerListener(listener, accelerometerSensor, SensorManager.SENSOR_DELAY_GAME);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (sensorManager != null) {
            sensorManager.unregisterListener(listener);
        }
    }

    private SensorEventListener listener = new SensorEventListener() {

        float[] accelerometerValues = new float[3];
        float[] magneticValues = new float[3];
        private float lastRotateDegree;
// onSensorChanged()方法中可以获取到 SensorEvent的 values数组，分别记录着 加速度传感器和地磁传感器输出的值
        @Override
        public void onSensorChanged(SensorEvent sensorEvent) {
            //判断当前是加速度传感器还是地磁传感器
            if (sensorEvent.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                //注意赋值时要调用clone()方法
                //不 然 accelerometerValues和 magneticValues将会指向同一个引用
                accelerometerValues = sensorEvent.values.clone();
            } else if (sensorEvent.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
                //注意赋值时需要调用clone()方法
                magneticValues = sensorEvent.values.clone();
            }
            float[] R = new float[9];
            float[] values = new float[3];
            //后调用 getRotationMatrix()方法为 R数组赋值
            SensorManager.getRotationMatrix(R, null, accelerometerValues, magneticValues);
            //调用 getOrientation()方法为 values数组赋值
            //values是一个长度为 3的 float数组，
            // 手机在各个方向上的旋转数据都会被存放到这个数 组当中。
            // 其中 values[0]记录着手机围绕着图 12.3中 Z轴的旋转弧度，
            // values[1]记录着手机围 绕 X轴的旋转弧度，
            // values[2]记录着手机围绕 Y轴的旋转弧度
            SensorManager.getOrientation(R, values);

            //将计算出的旋转角度取反，用于旋转指南针的背景
            float rotateDegree = -(float) Math.toDegrees(values[0]);
            if(Math.abs(rotateDegree - lastRotateDegree) > 1){
                //onSensorChanged()方法中使用到了旋转动画技术，我们创建了一个 RotateAnimation的实例
                //第一个参数表示旋转的起始角
                // 第二个参数表示旋转的终止角度
                // 后面四个参数用于指定旋转的中心点
                // 这里把传感器中获取到的旋转角度取反
                // 传递给 RotateAnimation
                // 并指定旋转的中心点为指南针背景图的中心
                // 然后调用 ImageView的 startAnimation ()方法来执行旋转动画
                RotateAnimation animation = new RotateAnimation(lastRotateDegree,rotateDegree,
                        Animation.RELATIVE_TO_SELF,0.5f,Animation.RELATIVE_TO_SELF,0.5f);
                animation.setFillAfter(true);
                compassImg.startAnimation(animation);
                lastRotateDegree = rotateDegree;
            }

            //调用 Math.toDegrees()方法将它转换成角度
            Log.d("MainActivity", "value[0] is " + Math.toDegrees(values[0]));
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int i) {

        }
    };

}
