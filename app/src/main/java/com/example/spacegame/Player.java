package com.example.spacegame;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Matrix;

public class Player {
    private Bitmap[] bitmapFrames;
    private int x;
    private int y;
    private int speed;
    private int lives;
    private int screenWidth;
    private float acceleration;
    
    // Default dimensions (reduced by 50%)
    private static final int DEFAULT_WIDTH = 50;
    private static final int DEFAULT_HEIGHT = 50;
    
    // Scale factor for resizing
    private static final float SCALE_FACTOR = 0.5f; // 50% size
    
    // Animation variables
    private int currentFrame;
    private long lastFrameChangeTime;
    private int frameLengthInMilliseconds = 150; // milliseconds
    
    // Total number of animation frames
    private static final int FRAME_COUNT = 3;

    // For collision detection
    private Rect collisionRect;
    private Paint debugPaint;

    public Player(Context context, int screenWidth, int screenHeight) {
        // Initialize bitmap frames for animation
        bitmapFrames = new Bitmap[FRAME_COUNT];
        boolean bitmapsLoaded = true;
        
        // Load the player bitmap frames
        try {
            // Load original bitmaps
            Bitmap originalBitmap0 = BitmapFactory.decodeResource(context.getResources(), R.drawable.spaceship_00);
            Bitmap originalBitmap1 = BitmapFactory.decodeResource(context.getResources(), R.drawable.spaceship_01);
            Bitmap originalBitmap2 = BitmapFactory.decodeResource(context.getResources(), R.drawable.spaceship_02);
            
            // Scale down bitmaps by 50%
            if (originalBitmap0 != null && originalBitmap1 != null && originalBitmap2 != null) {
                bitmapFrames[0] = scaleBitmap(originalBitmap0, SCALE_FACTOR);
                bitmapFrames[1] = scaleBitmap(originalBitmap1, SCALE_FACTOR);
                bitmapFrames[2] = scaleBitmap(originalBitmap2, SCALE_FACTOR);
                
                // Recycle original bitmaps to free memory
                originalBitmap0.recycle();
                originalBitmap1.recycle();
                originalBitmap2.recycle();
            } else {
                bitmapsLoaded = false;
            }
            
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
        
        // If bitmap loading failed, create placeholders
        if (!bitmapsLoaded) {
            for (int i = 0; i < bitmapFrames.length; i++) {
                bitmapFrames[i] = createSpaceshipPlaceholder(i);
            }
        }
        
        // Initialize animation variables
        currentFrame = 0;
        lastFrameChangeTime = System.currentTimeMillis();
        
        this.screenWidth = screenWidth;
        
        int width = getWidth();
        int height = getHeight();
        
        // Set initial position (center bottom of screen)
        x = screenWidth / 2 - width / 2;
        y = screenHeight - height - 50;
        
        // Set initial speed
        speed = 10;
        
        // Set initial lives
        lives = 3;
        
        // Initialize collision rectangle
        collisionRect = new Rect(x, y, x + width, y + height);
        
        // Initialize debug paint for visual debugging if needed
        debugPaint = new Paint();
        debugPaint.setColor(Color.GREEN);
        debugPaint.setStyle(Paint.Style.STROKE);
    }
    
    // Helper method to scale bitmap by a factor
    private Bitmap scaleBitmap(Bitmap originalBitmap, float scaleFactor) {
        int width = Math.round(originalBitmap.getWidth() * scaleFactor);
        int height = Math.round(originalBitmap.getHeight() * scaleFactor);
        
        return Bitmap.createScaledBitmap(originalBitmap, width, height, true);
    }
    
    private Bitmap createSpaceshipPlaceholder(int frameIndex) {
        // Create placeholder at 50% size
        Bitmap bitmap = Bitmap.createBitmap(DEFAULT_WIDTH, DEFAULT_HEIGHT, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        Paint paint = new Paint();
        
        // Base ship color with slight variations per frame
        switch(frameIndex) {
            case 0:
                paint.setColor(Color.BLUE);
                break;
            case 1:
                paint.setColor(Color.rgb(70, 70, 200)); // Slightly lighter blue
                break;
            case 2:
                paint.setColor(Color.rgb(50, 50, 180)); // Different blue
                break;
        }
        
        // Draw ship body - scaled down coordinates
        canvas.drawRect(15, 5, 35, 45, paint);
        
        // Draw ship engines with animation
        paint.setColor(Color.RED);
        int engineHeight = 15 + (frameIndex * 5); // Engine flame changes with frames (scaled down)
        canvas.drawRect(20, 35, 30, 35 + engineHeight, paint);
        
        // Draw cockpit
        paint.setColor(Color.YELLOW);
        canvas.drawCircle(25, 15, 5, paint);
        
        return bitmap;
    }

    public void update() {
        // Update player position based on accelerometer
        x += acceleration * speed;
        
        // Keep player within screen bounds
        if (x < 0) {
            x = 0;
        } else if (x > screenWidth - getWidth()) {
            x = screenWidth - getWidth();
        }
        
        // Update collision rectangle
        collisionRect.left = x;
        collisionRect.right = x + getWidth();
        
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
            
            // Uncomment to debug collision rectangle
            // canvas.drawRect(collisionRect, debugPaint);
        }
    }

    public void decreaseLives() {
        lives--;
    }

    public void reset() {
        lives = 3;
    }

    public int getLives() {
        return lives;
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

    public void setAcceleration(float acceleration) {
        // Convert accelerometer reading to movement
        // Negative acceleration moves right, positive moves left (based on phone orientation)
        this.acceleration = -acceleration;
    }
} 