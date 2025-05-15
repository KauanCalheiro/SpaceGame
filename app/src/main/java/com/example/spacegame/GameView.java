package com.example.spacegame;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.media.AudioAttributes;
import android.media.SoundPool;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;

public class GameView extends SurfaceView implements Runnable {

    // Game thread
    private Thread gameThread = null;
    private volatile boolean isPlaying;
    private boolean gameOver = false;

    // Drawing objects
    private SurfaceHolder surfaceHolder;
    private Paint paint;
    private Canvas canvas;

    // Game objects
    private Player player;
    private List<Stone> stones;
    private List<Bullet> bullets;
    private Background background;

    // Screen dimensions
    private int screenWidth;
    private int screenHeight;

    // Game variables
    private long lastStoneTime;
    private static final long STONE_SPAWN_INTERVAL = 2000; // 2 seconds
    private Random random;

    // Sound effects
    private SoundPool soundPool;
    private int shootSound;
    private int explosionSound;
    private int gameOverSound;
    private boolean soundsLoaded = false;

    public GameView(Context context, int screenWidth, int screenHeight) {
        super(context);
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;
        
        // Initialize objects
        surfaceHolder = getHolder();
        paint = new Paint();
        random = new Random();
        
        // Initialize game objects
        player = new Player(context, screenWidth, screenHeight);
        // Use thread-safe collections to prevent ConcurrentModificationException
        stones = new CopyOnWriteArrayList<>();
        bullets = new CopyOnWriteArrayList<>();
        background = new Background(context, screenWidth, screenHeight);
        
        // Initialize game state
        lastStoneTime = System.currentTimeMillis();
        
        // Initialize sounds
        initSounds(context);
    }

    private void initSounds(Context context) {
        // Create SoundPool with builder for compatibility
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            AudioAttributes audioAttributes = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_GAME)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build();
            
            soundPool = new SoundPool.Builder()
                    .setMaxStreams(5)
                    .setAudioAttributes(audioAttributes)
                    .build();
        } else {
            // Deprecated method for older devices
            soundPool = new SoundPool(5, android.media.AudioManager.STREAM_MUSIC, 0);
        }
        
        // Set flag when sounds are loaded
        soundPool.setOnLoadCompleteListener((soundPool, sampleId, status) -> {
            if (status == 0) {
                soundsLoaded = true;
            }
        });
        
        // Load sound effects
        try {
            shootSound = soundPool.load(context, R.raw.shoot, 1);
            explosionSound = soundPool.load(context, R.raw.explosion, 1);
            gameOverSound = soundPool.load(context, R.raw.game_over, 1);
            
            // Pre-load by playing silent sounds (workaround for common SoundPool issue)
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                if (soundPool != null) {
                    soundPool.play(shootSound, 0, 0, 1, 0, 1.0f);
                    soundPool.play(explosionSound, 0, 0, 1, 0, 1.0f);
                    soundPool.play(gameOverSound, 0, 0, 1, 0, 1.0f);
                }
            }, 300);
            
        } catch (Exception e) {
            // Sounds might not be available, handle the exception
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        while (isPlaying) {
            update();
            draw();
            control();
        }
    }

    private void update() {
        // Update player position based on accelerometer data
        player.update();
        
        // Update bullets
        updateBullets();
        
        // Update stones
        updateStones();
        
        // Check if it's time to spawn a new stone
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastStoneTime > STONE_SPAWN_INTERVAL) {
            spawnStone();
            lastStoneTime = currentTime;
        }
        
        // Check for collisions
        checkCollisions();
        
        // Check game over condition
        if (player.getLives() <= 0 && !gameOver) {
            gameOver = true;
            
            // Play game over sound
            if (soundsLoaded) {
                soundPool.play(gameOverSound, 1.0f, 1.0f, 1, 0, 1.0f);
            }
        }
    }

    private void updateBullets() {
        Iterator<Bullet> iterator = bullets.iterator();
        List<Bullet> bulletsToRemove = new ArrayList<>();
        
        // Identify bullets to remove
        for (Bullet bullet : bullets) {
            bullet.update();
            
            // Mark bullets that are off-screen for removal
            if (bullet.getY() < 0) {
                bulletsToRemove.add(bullet);
            }
        }
        
        // Remove bullets that are off-screen
        bullets.removeAll(bulletsToRemove);
    }

    private void updateStones() {
        List<Stone> stonesToRemove = new ArrayList<>();
        
        // Identify stones to remove
        for (Stone stone : stones) {
            stone.update();
            
            // Check if explosion animation is complete
            if (stone.isExploding() && stone.isExplosionComplete()) {
                stonesToRemove.add(stone);
            }
            // Mark stones that are off-screen for removal
            else if (stone.getY() > screenHeight) {
                // Player loses a life if a stone passes the bottom without being destroyed
                if (!stone.isExploding()) {
                    player.decreaseLives();
                    
                    // Play explosion sound
                    if (soundsLoaded) {
                        soundPool.play(explosionSound, 1.0f, 1.0f, 1, 0, 1.0f);
                    }
                    
                    // Force the stone to explode as visual feedback
                    stone.decreaseHealth();
                    while (stone.getHealth() > 0) {
                        stone.decreaseHealth();
                    }
                    
                    // We don't immediately remove exploding stones
                    // They'll be removed after their explosion animation completes
                } else {
                    stonesToRemove.add(stone);
                }
            }
        }
        
        // Remove stones that are off-screen or finished exploding
        stones.removeAll(stonesToRemove);
    }

    private void spawnStone() {
        // Position the stone randomly along the X-axis at the top of the screen
        int x = random.nextInt(screenWidth - 100);
        int health = random.nextInt(3) + 1; // Random health between 1 and 3
        Stone stone = new Stone(getContext(), x, 0, health);
        stones.add(stone);
    }

    private void checkCollisions() {
        // Lists to track items that need to be removed
        List<Bullet> bulletsToRemove = new ArrayList<>();
        List<Stone> stonesToRemove = new ArrayList<>();
        
        // Check bullet-stone collisions
        for (Bullet bullet : bullets) {
            if (bulletsToRemove.contains(bullet)) continue;
            
            for (Stone stone : stones) {
                // Skip stones that are already exploding or marked for removal
                if (stonesToRemove.contains(stone) || stone.isExploding()) continue;
                
                // Simple rectangle-based collision detection
                if (Rect.intersects(bullet.getCollisionRect(), stone.getCollisionRect())) {
                    // Bullet hit stone
                    stone.decreaseHealth();
                    bulletsToRemove.add(bullet);
                    
                    // Play explosion sound if stone is destroyed (health <= 0)
                    if (stone.getHealth() <= 0 && soundsLoaded) {
                        soundPool.play(explosionSound, 0.7f, 0.7f, 1, 0, 1.0f);
                    }
                    
                    break; // Bullet can only hit one stone
                }
            }
        }
        
        // Check player-stone collisions
        for (Stone stone : stones) {
            // Skip stones that are already exploding or marked for removal
            if (stonesToRemove.contains(stone) || stone.isExploding()) continue;
            
            if (Rect.intersects(player.getCollisionRect(), stone.getCollisionRect())) {
                // Stone hit player
                player.decreaseLives();
                stone.decreaseHealth(); // This will trigger the explosion animation
                
                // Play explosion sound
                if (soundsLoaded) {
                    soundPool.play(explosionSound, 1.0f, 1.0f, 1, 0, 1.0f);
                }
            }
        }
        
        // Apply all the removals at once
        bullets.removeAll(bulletsToRemove);
        stones.removeAll(stonesToRemove);
    }

    private void draw() {
        if (surfaceHolder.getSurface().isValid()) {
            canvas = surfaceHolder.lockCanvas();
            
            // Draw background
            background.draw(canvas);
            
            // Draw player
            player.draw(canvas);
            
            // Thread-safe way to draw bullets
            for (Bullet bullet : bullets) {
                if (bullet != null) {
                    bullet.draw(canvas);
                }
            }
            
            // Thread-safe way to draw stones
            for (Stone stone : stones) {
                if (stone != null) {
                    stone.draw(canvas);
                }
            }
            
            // Draw HUD (player lives)
            drawHUD();
            
            // Draw game over screen if game is over
            if (gameOver) {
                drawGameOver();
            }
            
            surfaceHolder.unlockCanvasAndPost(canvas);
        }
    }

    private void drawHUD() {
        paint.setColor(Color.WHITE);
        paint.setTextSize(50);
        canvas.drawText("Vidas: " + player.getLives(), 50, 50, paint);
    }

    private void drawGameOver() {
        paint.setColor(Color.RED);
        paint.setTextSize(100);
        canvas.drawText("GAME OVER", screenWidth / 2 - 250, screenHeight / 2, paint);
        
        paint.setColor(Color.WHITE);
        paint.setTextSize(50);
        canvas.drawText("Toque para reiniciar", screenWidth / 2 - 150, screenHeight / 2 + 100, paint);
    }

    private void control() {
        // Control the game's frame rate
        try {
            gameThread.sleep(17); // Approximately 60 FPS
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void pause() {
        isPlaying = false;
        try {
            gameThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        // Release sound resources when game is paused
        if (soundPool != null) {
            soundPool.autoPause();
        }
    }

    public void resume() {
        isPlaying = true;
        gameThread = new Thread(this);
        gameThread.start();
        
        // Resume sounds
        if (soundPool != null) {
            soundPool.autoResume();
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                // Fire a bullet when the screen is tapped
                if (!gameOver) {
                    Bullet bullet = new Bullet(getContext(), player.getX() + player.getWidth() / 2, player.getY());
                    bullets.add(bullet);
                    
                    // Play shoot sound
                    if (soundsLoaded) {
                        soundPool.play(shootSound, 0.5f, 0.5f, 1, 0, 1.0f);
                    }
                } else {
                    // Restart the game if it's over
                    restartGame();
                }
                break;
        }
        return true;
    }

    private void restartGame() {
        gameOver = false;
        player.reset();
        stones.clear();
        bullets.clear();
        lastStoneTime = System.currentTimeMillis();
    }

    // Method to update player's horizontal acceleration
    public void updatePlayerAcceleration(float acceleration) {
        player.setAcceleration(acceleration);
    }
    
    // Release resources when the game view is destroyed
    public void destroy() {
        if (soundPool != null) {
            soundPool.release();
            soundPool = null;
        }
    }
} 