package com.example.pocketdefense;

final class EnemyType {
    final float baseHealth;
    final float healthPerWave;
    final float baseSpeed;
    final float speedPerWave;
    final int reward;
    final float radius;
    final int color;

    EnemyType(float baseHealth, float healthPerWave, float baseSpeed, float speedPerWave,
              int reward, float radius, int color) {
        this.baseHealth = baseHealth;
        this.healthPerWave = healthPerWave;
        this.baseSpeed = baseSpeed;
        this.speedPerWave = speedPerWave;
        this.reward = reward;
        this.radius = radius;
        this.color = color;
    }

    float healthForWave(int wave) {
        return baseHealth + healthPerWave * wave;
    }

    float speedForWave(int wave) {
        return baseSpeed + speedPerWave * wave;
    }
}
