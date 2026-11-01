package com.example.pocketdefense;

import android.graphics.Color;

final class GameBalance {
    static final int INITIAL_GOLD = 150;
    static final int INITIAL_LIVES = 15;
    static final int ENEMIES_PER_WAVE_BASE = 6;
    static final int ENEMIES_PER_WAVE_INCREMENT = 2;
    static final int PURPLE_ENEMY_WAVE_INTERVAL = 4;
    static final long SPAWN_INTERVAL_BASE_MS = 1050;
    static final long SPAWN_INTERVAL_DECREASE_PER_WAVE_MS = 35;
    static final long SPAWN_INTERVAL_MIN_MS = 420;

    static final TowerType BASIC_TOWER = new TowerType(50, 24, 135, 0.48f, 390);

    static final EnemyType STANDARD_ENEMY = new EnemyType(
            38, 13, 48, 2.2f, 12, 12, Color.rgb(238, 103, 91));
    static final EnemyType PURPLE_ENEMY = new EnemyType(
            38, 13, 48, 2.2f, 12, 12, Color.rgb(185, 133, 255));

    private GameBalance() {
    }

    static int enemiesInWave(int wave) {
        return ENEMIES_PER_WAVE_BASE + wave * ENEMIES_PER_WAVE_INCREMENT;
    }

    static long spawnIntervalMs(int wave) {
        return Math.max(SPAWN_INTERVAL_MIN_MS,
                SPAWN_INTERVAL_BASE_MS - wave * SPAWN_INTERVAL_DECREASE_PER_WAVE_MS);
    }

    static EnemyType enemyTypeForWave(int wave) {
        return wave % PURPLE_ENEMY_WAVE_INTERVAL == 0 ? PURPLE_ENEMY : STANDARD_ENEMY;
    }
}
