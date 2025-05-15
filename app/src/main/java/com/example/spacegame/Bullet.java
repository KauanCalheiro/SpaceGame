package com.example.spacegame;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.Color;
import android.graphics.Paint;

public class Bullet {
    private Bitmap[] bitmapFrames;
    private int x;
    private int y;
    private int speed;
    private Rect collisionRect;
    
    // Default dimensions
    private static final int DEFAULT_WIDTH = 20;
    private static final int DEFAULT_HEIGHT = 40;
    
    // Animation variables
    private int currentFrame;
    private long lastFrameChangeTime;
    private int frameLengthInMilliseconds = 50; // milliseconds - faster than stones
    
    // Total number of animation frames
    private static final int FRAME_COUNT = 4;
    
    public Bullet(Context context, int x, int y) {
        this.x = x;
        this.y = y;
        
        // Set bullet speed
        this.speed = 20;
        
        // Initialize bitmap frames for animation
        bitmapFrames = new Bitmap[FRAME_COUNT];
        boolean bitmapsLoaded = true;
        
        // Load bullet bitmaps
        try {
            bitmapFrames[0] = BitmapFactory.decodeResource(context.getResources(), R.drawable.bullet_00);
            bitmapFrames[1] = BitmapFactory.decodeResource(context.getResources(), R.drawable.bullet_01);
            bitmapFrames[2] = BitmapFactory.decodeResource(context.getResources(), R.drawable.bullet_02);
            bitmapFrames[3] = BitmapFactory.decodeResource(context.getResources(), R.drawable.bullet_03);
            
            // Check if any bitmap is null
            for (int i = 0; i < bitmapFrames.length; i++) {
                if (bitmapFrames[i] == null) {
                    bitmapsLoaded = false;
                    break;
                }
            }
        } catch (Exception e) {
            bitmapsLoaded = false;
        }
        
        if (!bitmapsLoaded) {
            // Create placeholders if resources not found
            for (int i = 0; i < bitmapFrames.length; i++) {
                bitmapFrames[i] = createBulletPlaceholder(i);
            }
        }
        
        // Initialize animation variables
        currentFrame = 0;
        lastFrameChangeTime = System.currentTimeMillis();
        
        // Get bitmap dimensions
        int width = getWidth();
        int height = getHeight();
        
        // Initialize collision rectangle
        collisionRect = new Rect(x, y, x + width, y + height);
    }
    
    private Bitmap createBulletPlaceholder(int frameIndex) {
        Bitmap bitmap = Bitmap.createBitmap(DEFAULT_WIDTH, DEFAULT_HEIGHT, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        Paint paint = new Paint();
        
        // Different colors for different frames
        switch(frameIndex % 4) {
            case 0:
                paint.setColor(Color.YELLOW);
                break;
            case 1:
                paint.setColor(Color.rgb(255, 200, 0)); // Orange-yellow
                break;
            case 2:
                paint.setColor(Color.rgb(255, 150, 0)); // Orange
                break;
            case 3:
                paint.setColor(Color.rgb(255, 100, 0)); // Reddish-orange
                break;
        }
        
        // Draw a bullet shape
        canvas.drawRect(5, 0, DEFAULT_WIDTH - 5, DEFAULT_HEIGHT, paint);
        
        // Add some visual interest based on frame
        paint.setColor(Color.WHITE);
        int yOffset = (frameIndex * 10) % DEFAULT_HEIGHT;
        canvas.drawRect(8, yOffset, DEFAULT_WIDTH - 8, yOffset + 10, paint);
        
        return bitmap;
    }
    
    public void update() {
        // Move the bullet up
        y -= speed;
        
        // Update collision rectangle
        collisionRect.top = y;
        collisionRect.bottom = y + getHeight();
        
        // Update animation frame
        if (System.currentTimeMillis() > lastFrameChangeTime + frameLengthInMilliseconds) {
            currentFrame++;
            if (currentFrame >= bitmapFrames.length) {
                currentFrame = 0;
            }
            lastFrameChangeTime = System.currentTimeMillis();
        }
    }
    
    public void draw(Canvas canvas) {
        if (canvas != null && bitmapFrames[currentFrame] != null) {
            canvas.drawBitmap(bitmapFrames[currentFrame], x, y, null);
        }
    }
    
    public int getX() {
        return x;
    }
    
    public int getY() {
        return y;
    }
    
    public int getWidth() {
        return bitmapFrames != null && bitmapFrames[currentFrame] != null ? 
               bitmapFrames[currentFrame].getWidth() : DEFAULT_WIDTH;
    }
    
    public int getHeight() {
        return bitmapFrames != null && bitmapFrames[currentFrame] != null ? 
               bitmapFrames[currentFrame].getHeight() : DEFAULT_HEIGHT;
    }
    
    public Rect getCollisionRect() {
        return collisionRect;
    }
} 