package com.example.pocketdefense;

final class TowerType {
    final int cost;
    final float damage;
    final float range;
    final float fireIntervalSeconds;
    final float projectileSpeed;

    TowerType(int cost, float damage, float range, float fireIntervalSeconds, float projectileSpeed) {
        this.cost = cost;
        this.damage = damage;
        this.range = range;
        this.fireIntervalSeconds = fireIntervalSeconds;
        this.projectileSpeed = projectileSpeed;
    }
}
