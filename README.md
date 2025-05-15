# Space Game

A simple 2D space game built with Android's SurfaceView and Canvas APIs. This game demonstrates basic game development concepts in Android including:

- Game loop implementation using SurfaceView and threads
- 2D rendering with Canvas
- Input handling (touch and accelerometer)
- Collision detection
- Sound effects
- Game state management

## Features

- **Player Ship**: Control a spaceship at the bottom of the screen using the device's accelerometer
- **Falling Stones**: Dodge or shoot asteroids that fall from the top of the screen
- **Bullet System**: Tap the screen to fire bullets and destroy stones
- **Lives System**: Player has 3 lives
- **Stone Health**: Stones have varying health levels (1-3), requiring multiple hits to destroy
- **Animations**: Stones rotate using a simple frame animation
- **Game Over Screen**: Displays when player loses all lives
- **Sound Effects**: For shooting, collisions, and game over

## Implementation Details

- Uses SurfaceView for efficient rendering
- Implements a custom game loop with controlled FPS
- Uses the accelerometer sensor for player movement
- Simple rectangle-based collision detection
- Vector drawables for game graphics
- SoundPool for audio effects

## How to Play

1. Tilt your device left and right to move the spaceship
2. Tap the screen to fire bullets
3. Avoid or destroy the falling stones
4. Game ends when you lose all 3 lives
5. Tap the screen to restart after game over

## Code Structure

- **MainActivity**: Sets up the game environment and handles the accelerometer
- **GameView**: Main game class managing the game loop, rendering, and game logic
- **Player**: Represents the player's spaceship
- **Stone**: Represents the falling obstacles with health and animation
- **Bullet**: Represents the projectiles fired by the player
- **Background**: Handles the space-themed background

## Requirements

- Android 5.0 (API level 21) or higher
- Device with accelerometer sensor

## License

This project is open source and available for educational purposes. 