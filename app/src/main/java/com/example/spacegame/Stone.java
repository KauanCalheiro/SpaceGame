package com.example.spacegame;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.Color;
import android.graphics.Paint;

public class Stone {
    private Bitmap[] bitmapFrames;
    private int x;
    private int y;
    private int speed;
    private int health;
    private Rect collisionRect;
    
    // Animation variables
    private int currentFrame;
    private long lastFrameChangeTime;
    private int frameLengthInMilliseconds = 200; // milliseconds
    
    // Default size for stones if bitmap can't be loaded (reduced by 50%)
    private static final int DEFAULT_WIDTH = 40;
    private static final int DEFAULT_HEIGHT = 40;
    
    // Scale factor for resizing
    private static final float SCALE_FACTOR = 0.5f; // 50% size
    
    // Total number of animation frames
    private static final int FRAME_COUNT = 4;
    
    // Explosion animation
    private Bitmap[] explosionFrames;
    private boolean exploding = false;
    private int explosionFrame = 0;
    private int explosionFrameLength = 100; // faster animation for explosion
    private long lastExplosionFrameTime;
    private boolean explosionComplete = false;
    private Context context;
    
    public Stone(Context context, int x, int y, int health) {
        this.context = context;
        this.x = x;
        this.y = y;
        this.health = health;
        
        // Set speed based on health (lower health = faster)
        this.speed = 10 - health + 5;
        
        // Initialize bitmap frames for animation
        bitmapFrames = new Bitmap[FRAME_COUNT];
        boolean bitmapsLoaded = true;
        
        // Load stone bitmaps
        try {
            // Load original bitmaps
            Bitmap originalBitmap0 = BitmapFactory.decodeResource(context.getResources(), R.drawable.rock_00);
            Bitmap originalBitmap1 = BitmapFactory.decodeResource(context.getResources(), R.drawable.rock_01);
            Bitmap originalBitmap2 = BitmapFactory.decodeResource(context.getResources(), R.drawable.rock_02);
            Bitmap originalBitmap3 = BitmapFactory.decodeResource(context.getResources(), R.drawable.rock_03);
            
            // Scale down bitmaps by 50%
            if (originalBitmap0 != null && originalBitmap1 != null && 
                originalBitmap2 != null && originalBitmap3 != null) {
                
                bitmapFrames[0] = scaleBitmap(originalBitmap0, SCALE_FACTOR);
                bitmapFrames[1] = scaleBitmap(originalBitmap1, SCALE_FACTOR);
                bitmapFrames[2] = scaleBitmap(originalBitmap2, SCALE_FACTOR);
                bitmapFrames[3] = scaleBitmap(originalBitmap3, SCALE_FACTOR);
                
                // Recycle original bitmaps to free memory
                originalBitmap0.recycle();
                originalBitmap1.recycle();
                originalBitmap2.recycle();
                originalBitmap3.recycle();
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
        
        // If any bitmap failed to load, create placeholders for all
        if (!bitmapsLoaded) {
            for (int i = 0; i < bitmapFrames.length; i++) {
                bitmapFrames[i] = createRockPlaceholder(i);
            }
        }
        
        // Initialize animation variables
        currentFrame = 0;
        lastFrameChangeTime = System.currentTimeMillis();
        
        // Get bitmap dimensions safely
        int width = getWidth();
        int height = getHeight();
        
        // Initialize collision rectangle
        collisionRect = new Rect(x, y, x + width, y + height);
    }
    
    // Helper method to scale bitmap by a factor
    private Bitmap scaleBitmap(Bitmap originalBitmap, float scaleFactor) {
        int width = Math.round(originalBitmap.getWidth() * scaleFactor);
        int height = Math.round(originalBitmap.getHeight() * scaleFactor);
        
        return Bitmap.createScaledBitmap(originalBitmap, width, height, true);
    }
    
    // Create a placeholder rock bitmap
    private Bitmap createRockPlaceholder(int frameIndex) {
        Bitmap bitmap = Bitmap.createBitmap(DEFAULT_WIDTH, DEFAULT_HEIGHT, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        Paint paint = new Paint();
        
        // Draw rock body in gray with slight color variation per frame
        paint.setColor(Color.rgb(100 + (frameIndex * 10), 100 + (frameIndex * 10), 100 + (frameIndex * 10)));
        
        // Draw a slightly different shape for each frame to simulate rotation
        switch (frameIndex) {
            case 0:
                canvas.drawCircle(DEFAULT_WIDTH / 2, DEFAULT_HEIGHT / 2, DEFAULT_WIDTH / 2, paint);
                break;
            case 1:
                canvas.drawOval(5, 10, DEFAULT_WIDTH - 5, DEFAULT_HEIGHT - 10, paint);
                break;
            case 2:
                canvas.drawOval(10, 5, DEFAULT_WIDTH - 10, DEFAULT_HEIGHT - 5, paint);
                break;
            case 3:
                canvas.drawCircle(DEFAULT_WIDTH / 2, DEFAULT_HEIGHT / 2, DEFAULT_WIDTH / 2 - 2, paint);
                break;
        }
        
        // Add rock details
        paint.setColor(Color.DKGRAY);
        canvas.drawCircle(DEFAULT_WIDTH / 3, DEFAULT_HEIGHT / 3, DEFAULT_WIDTH / 10, paint);
        canvas.drawCircle(DEFAULT_WIDTH * 2 / 3, DEFAULT_HEIGHT * 2 / 3, DEFAULT_WIDTH / 10, paint);
        
        return bitmap;
    }
    
    // Load explosion animation frames
    private void loadExplosionFrames() {
        explosionFrames = new Bitmap[FRAME_COUNT];
        boolean explosionLoaded = true;
        
        try {
            // Load original explosion bitmaps
            Bitmap explBitmap0 = BitmapFactory.decodeResource(context.getResources(), R.drawable.explode_rock_00);
            Bitmap explBitmap1 = BitmapFactory.decodeResource(context.getResources(), R.drawable.explode_rock_01);
            Bitmap explBitmap2 = BitmapFactory.decodeResource(context.getResources(), R.drawable.explode_rock_02);
            Bitmap explBitmap3 = BitmapFactory.decodeResource(context.getResources(), R.drawable.explode_rock_03);
            
            if (explBitmap0 != null && explBitmap1 != null && 
                explBitmap2 != null && explBitmap3 != null) {
                
                // Scale explosion bitmaps
                explosionFrames[0] = scaleBitmap(explBitmap0, SCALE_FACTOR);
                explosionFrames[1] = scaleBitmap(explBitmap1, SCALE_FACTOR);
                explosionFrames[2] = scaleBitmap(explBitmap2, SCALE_FACTOR);
                explosionFrames[3] = scaleBitmap(explBitmap3, SCALE_FACTOR);
                
                // Recycle original bitmaps
                explBitmap0.recycle();
                explBitmap1.recycle();
                explBitmap2.recycle();
                explBitmap3.recycle();
            } else {
                explosionLoaded = false;
            }
            
            // Check if any bitmap is null
            for (int i = 0; i < explosionFrames.length; i++) {
                if (explosionFrames[i] == null) {
                    explosionLoaded = false;
                    break;
                }
            }
        } catch (Exception e) {
            explosionLoaded = false;
        }
        
        // If any explosion bitmap failed to load, create placeholders
        if (!explosionLoaded) {
            for (int i = 0; i < explosionFrames.length; i++) {
                explosionFrames[i] = createExplosionPlaceholder(i);
            }
        }
    }
    
    // Create a placeholder explosion bitmap
    private Bitmap createExplosionPlaceholder(int frameIndex) {
        Bitmap bitmap = Bitmap.createBitmap(DEFAULT_WIDTH, DEFAULT_HEIGHT, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        Paint paint = new Paint();
        
        // Draw explosion in yellow/orange with expanding size per frame
        int radius = DEFAULT_WIDTH / 4 + (frameIndex * DEFAULT_WIDTH / 4);
        
        // Change color based on frame (yellow to orange to red)
        switch (frameIndex) {
            case 0:
                paint.setColor(Color.YELLOW);
                break;
            case 1:
                paint.setColor(Color.rgb(255, 200, 0));  // Orange-yellow
                break;
            case 2:
                paint.setColor(Color.rgb(255, 150, 0));  // Orange
                break;
            case 3:
                paint.setColor(Color.rgb(255, 100, 0));  // Red-orange
                break;
        }
        
        canvas.drawCircle(DEFAULT_WIDTH / 2, DEFAULT_HEIGHT / 2, radius, paint);
        
        // Add some particles
        paint.setColor(Color.WHITE);
        for (int i = 0; i < 5 + frameIndex * 3; i++) {
            float particleX = (float) (DEFAULT_WIDTH / 2 + Math.cos(i) * radius * 0.8);
            float particleY = (float) (DEFAULT_HEIGHT / 2 + Math.sin(i) * radius * 0.8);
            canvas.drawCircle(particleX, particleY, 2, paint);
        }
        
        return bitmap;
    }
    
    public void update() {
        if (exploding) {
            // Update explosion animation
            if (System.currentTimeMillis() > lastExplosionFrameTime + explosionFrameLength) {
                explosionFrame++;
                if (explosionFrame >= FRAME_COUNT) {
                    explosionComplete = true;
                } else {
                    lastExplosionFrameTime = System.currentTimeMillis();
                }
            }
        } else {
            // Move the stone down
            y += speed;
            
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
    }
    
    public void draw(Canvas canvas) {
        if (canvas == null) return;
        
        if (exploding) {
            // Draw explosion animation
            if (!explosionComplete && explosionFrames != null && explosionFrame < explosionFrames.length && explosionFrames[explosionFrame] != null) {
                canvas.drawBitmap(explosionFrames[explosionFrame], x, y, null);
            }
        } else {
            // Draw regular stone animation
            if (bitmapFrames != null && currentFrame < bitmapFrames.length && bitmapFrames[currentFrame] != null) {
                canvas.drawBitmap(bitmapFrames[currentFrame], x, y, null);
            }
        }
    }
    
    public int getWidth() {
        return (bitmapFrames != null && currentFrame < bitmapFrames.length && bitmapFrames[currentFrame] != null) ? 
               bitmapFrames[currentFrame].getWidth() : DEFAULT_WIDTH;
    }
    
    public int getHeight() {
        return (bitmapFrames != null && currentFrame < bitmapFrames.length && bitmapFrames[currentFrame] != null) ? 
               bitmapFrames[currentFrame].getHeight() : DEFAULT_HEIGHT;
    }
    
    public void decreaseHealth() {
        health--;
        if (health <= 0 && !exploding) {
            // Start explosion animation
            startExplosion();
        }
    }
    
    private void startExplosion() {
        exploding = true;
        explosionFrame = 0;
        explosionComplete = false;
        lastExplosionFrameTime = System.currentTimeMillis();
        
        // Load explosion frames if not loaded
        if (explosionFrames == null) {
            loadExplosionFrames();
        }
    }
    
    public int getHealth() {
        return health;
    }
    
    public int getX() {
        return x;
    }
    
    public int getY() {
        return y;
    }
    
    public Rect getCollisionRect() {
        return collisionRect;
    }
    
    public boolean isExploding() {
        return exploding;
    }
    
    public boolean isExplosionComplete() {
        return explosionComplete;
    }
} 