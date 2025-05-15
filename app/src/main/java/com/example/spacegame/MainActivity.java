package com.example.spacegame;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.WindowManager;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private GameView gameView;
    private SensorManager sensorManager;
    private Sensor accelerometer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Set window to fullscreen
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, 
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        
        // Keep screen on
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        
        // Get screen dimensions
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int screenWidth = displayMetrics.widthPixels;
        int screenHeight = displayMetrics.heightPixels;
        
        // Initialize accelerometer
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        if (sensorManager != null) {
            accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        }
        
        // Create and set GameView
        gameView = new GameView(this, screenWidth, screenHeight);
        setContentView(gameView);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Register accelerometer listener
        if (sensorManager != null && accelerometer != null) {
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_GAME);
        }
        // Resume game
        gameView.resume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Unregister accelerometer listener
        if (sensorManager != null) {
            sensorManager.unregisterListener(this);
        }
        // Pause game
        gameView.pause();
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Release resources
        if (gameView != null) {
            gameView.destroy();
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            // Get accelerometer data (we only need x-axis for horizontal movement)
            float acceleration = event.values[0];
            
            // Pass acceleration value to GameView to update player position
            gameView.updatePlayerAcceleration(acceleration);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Not used in this implementation
    }
}