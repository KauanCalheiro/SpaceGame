package com.example.spacegame;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

public class Background {
    private Bitmap bitmap;
    private int width;
    private int height;
    private Paint paint;
    
    public Background(Context context, int screenWidth, int screenHeight) {
        width = screenWidth;
        height = screenHeight;
        
        // Load background bitmap or create placeholder
        try {
            bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.space_background);
        } catch (Exception e) {
            bitmap = null;
        }
        
        if (bitmap == null) {
            // Create a simple space-like background as placeholder
            bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            Paint tempPaint = new Paint();
            tempPaint.setColor(Color.BLACK);
            
            Canvas tempCanvas = new Canvas(bitmap);
            tempCanvas.drawRect(0, 0, width, height, tempPaint);
            
            // Draw some stars
            tempPaint.setColor(Color.WHITE);
            for (int i = 0; i < 100; i++) {
                int starX = (int) (Math.random() * width);
                int starY = (int) (Math.random() * height);
                int starSize = (int) (Math.random() * 5) + 1;
                tempCanvas.drawCircle(starX, starY, starSize, tempPaint);
            }
        } else {
            // Scale the bitmap to fit the screen
            try {
                bitmap = Bitmap.createScaledBitmap(bitmap, width, height, true);
            } catch (Exception e) {
                // If scaling fails, create a simple background
                bitmap = createSimpleBackground(width, height);
            }
        }
        
        paint = new Paint();
    }
    
    private Bitmap createSimpleBackground(int width, int height) {
        Bitmap simpleBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Paint tempPaint = new Paint();
        tempPaint.setColor(Color.BLACK);
        
        Canvas tempCanvas = new Canvas(simpleBitmap);
        tempCanvas.drawRect(0, 0, width, height, tempPaint);
        
        // Draw some stars
        tempPaint.setColor(Color.WHITE);
        for (int i = 0; i < 100; i++) {
            int starX = (int) (Math.random() * width);
            int starY = (int) (Math.random() * height);
            int starSize = (int) (Math.random() * 5) + 1;
            tempCanvas.drawCircle(starX, starY, starSize, tempPaint);
        }
        
        return simpleBitmap;
    }
    
    public void draw(Canvas canvas) {
        if (canvas != null && bitmap != null) {
            canvas.drawBitmap(bitmap, 0, 0, paint);
        } else if (canvas != null) {
            // If bitmap is still null, draw a black background
            Paint blackPaint = new Paint();
            blackPaint.setColor(Color.BLACK);
            canvas.drawRect(0, 0, width, height, blackPaint);
        }
    }
} 