package com.example.pocketdefense;

import android.app.Activity;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class MainActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(new GameView(this));
    }

    private static final class GameView extends View {
        private static final int WIDTH = 420;
        private static final int HEIGHT = 800;
        private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final List<Tower> towers = new ArrayList<>();
        private final List<Enemy> enemies = new ArrayList<>();
        private final List<Bolt> bolts = new ArrayList<>();
        private final List<Point> route = new ArrayList<>();
        private final List<Point> pads = new ArrayList<>();
        private long previousFrame;
        private long spawnTimer;
        private int gold = GameBalance.INITIAL_GOLD;
        private int lives = GameBalance.INITIAL_LIVES;
        private int wave = 0;
        private int spawned = 0;
        private int waveSize = 0;
        private boolean waveRunning;
        private boolean gameOver;

        GameView(Activity activity) {
            super(activity);
            setLayerType(View.LAYER_TYPE_SOFTWARE, null);
            route.add(new Point(42, 170));
            route.add(new Point(330, 170));
            route.add(new Point(330, 330));
            route.add(new Point(90, 330));
            route.add(new Point(90, 500));
            route.add(new Point(330, 500));
            route.add(new Point(330, 690));
            pads.add(new Point(100, 235));
            pads.add(new Point(255, 235));
            pads.add(new Point(255, 405));
            pads.add(new Point(155, 405));
            pads.add(new Point(155, 575));
            pads.add(new Point(255, 575));
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            float scale = Math.min(getWidth() / (float) WIDTH, getHeight() / (float) HEIGHT);
            float offsetX = (getWidth() - WIDTH * scale) / 2f;
            float offsetY = (getHeight() - HEIGHT * scale) / 2f;
            canvas.save();
            canvas.translate(offsetX, offsetY);
            canvas.scale(scale, scale);
            drawBackground(canvas);
            drawHud(canvas);
            drawRoute(canvas);
            drawPads(canvas);
            drawTowers(canvas);
            drawEnemies(canvas);
            drawBolts(canvas);
            drawControls(canvas);
            if (gameOver) drawGameOver(canvas);
            canvas.restore();
            updateGame();
            postInvalidateDelayed(32);
        }

        private void drawBackground(Canvas canvas) {
            canvas.drawColor(Color.rgb(28, 63, 53));
            paint.setColor(Color.argb(24, 255, 255, 255));
            paint.setStrokeWidth(1f);
            for (int x = 10; x < WIDTH; x += 28) canvas.drawLine(x, 105, x, HEIGHT, paint);
            for (int y = 110; y < HEIGHT; y += 28) canvas.drawLine(0, y, WIDTH, y, paint);
            paint.setColor(Color.rgb(35, 75, 62));
            canvas.drawRect(0, 0, WIDTH, 105, paint);
        }

        private void drawHud(Canvas canvas) {
            text(canvas, "POCKET DEFENSE", 20, 38, 22, Color.WHITE, true);
            text(canvas, "GOLD  " + gold, 20, 76, 17, Color.rgb(255, 209, 102), true);
            text(canvas, "LIVES  " + lives, 160, 76, 17, Color.rgb(255, 132, 112), true);
            text(canvas, "WAVE  " + wave, 300, 76, 17, Color.WHITE, true);
        }

        private void drawRoute(Canvas canvas) {
            Path path = new Path();
            path.moveTo(route.get(0).x, route.get(0).y);
            for (int i = 1; i < route.size(); i++) path.lineTo(route.get(i).x, route.get(i).y);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(58);
            paint.setStrokeCap(Paint.Cap.ROUND);
            paint.setStrokeJoin(Paint.Join.ROUND);
            paint.setColor(Color.rgb(106, 87, 61));
            canvas.drawPath(path, paint);
            paint.setStrokeWidth(3);
            paint.setColor(Color.rgb(151, 129, 94));
            canvas.drawPath(path, paint);
            paint.setStyle(Paint.Style.FILL);
            circle(canvas, route.get(0).x, route.get(0).y, 17, Color.rgb(106, 213, 147));
            circle(canvas, route.get(route.size() - 1).x, route.get(route.size() - 1).y, 17, Color.rgb(255, 111, 97));
            text(canvas, "START", 18, 137, 11, Color.WHITE, true);
            text(canvas, "EXIT", 306, 722, 11, Color.WHITE, true);
        }

        private void drawPads(Canvas canvas) {
            for (int i = 0; i < pads.size(); i++) {
                Point p = pads.get(i);
                boolean occupied = findTower(i) != null;
                paint.setColor(occupied ? Color.rgb(71, 94, 72) : Color.rgb(58, 112, 82));
                canvas.drawRoundRect(new RectF(p.x - 24, p.y - 24, p.x + 24, p.y + 24), 10, 10, paint);
                paint.setColor(Color.argb(130, 255, 255, 255));
                paint.setStyle(Paint.Style.STROKE);
                paint.setStrokeWidth(2);
                canvas.drawRoundRect(new RectF(p.x - 24, p.y - 24, p.x + 24, p.y + 24), 10, 10, paint);
                paint.setStyle(Paint.Style.FILL);
                if (!occupied) text(canvas, "+", p.x - 7, p.y + 8, 24, Color.WHITE, true);
            }
        }

        private void drawTowers(Canvas canvas) {
            for (Tower tower : towers) {
                Point p = pads.get(tower.pad);
                circle(canvas, p.x, p.y, 26, Color.rgb(65, 145, 211));
                circle(canvas, p.x, p.y, 17, Color.rgb(146, 216, 255));
                circle(canvas, p.x, p.y, 8, Color.rgb(39, 82, 130));
                paint.setColor(Color.WHITE);
                canvas.drawRoundRect(new RectF(p.x - 3, p.y - 25, p.x + 3, p.y - 13), 3, 3, paint);
            }
        }

        private void drawEnemies(Canvas canvas) {
            for (Enemy enemy : enemies) {
                Point p = pointOnRoute(enemy.distance);
                circle(canvas, p.x, p.y + 2, enemy.type.radius + 2, Color.argb(70, 0, 0, 0));
                circle(canvas, p.x, p.y, enemy.type.radius, enemy.type.color);
                circle(canvas, p.x - 4, p.y - 3, 2, Color.WHITE);
                circle(canvas, p.x + 4, p.y - 3, 2, Color.WHITE);
                paint.setColor(Color.rgb(30, 35, 35));
                canvas.drawRect(p.x - 9, p.y - 17, p.x + 9, p.y - 14, paint);
                paint.setColor(Color.rgb(106, 213, 147));
                canvas.drawRect(p.x - 9, p.y - 17, p.x - 9 + 18 * Math.max(0, enemy.health / enemy.maxHealth), p.y - 14, paint);
            }
        }

        private void drawBolts(Canvas canvas) {
            for (Bolt bolt : bolts) {
                circle(canvas, bolt.x, bolt.y, 5, Color.rgb(255, 225, 117));
                circle(canvas, bolt.x, bolt.y, 2, Color.WHITE);
            }
        }

        private void drawControls(Canvas canvas) {
            paint.setColor(Color.argb(225, 24, 43, 40));
            canvas.drawRoundRect(new RectF(15, 735, 405, 785), 14, 14, paint);
            if (gameOver) {
                text(canvas, "TAP TO PLAY AGAIN", 117, 767, 18, Color.WHITE, true);
            } else if (waveRunning) {
                text(canvas, "Towers cost " + GameBalance.BASIC_TOWER.cost + " gold  |  Wave " + wave + " in progress", 32, 766, 15, Color.WHITE, true);
            } else {
                text(canvas, wave == 0 ? "TAP HERE TO START WAVE 1" : "TAP HERE TO START WAVE " + (wave + 1), 75, 766, 17, Color.WHITE, true);
            }
            text(canvas, "Tap a + pad to build a tower (" + GameBalance.BASIC_TOWER.cost + ")", 67, 717, 14, Color.rgb(255, 237, 177), false);
        }

        private void drawGameOver(Canvas canvas) {
            paint.setColor(Color.argb(190, 10, 20, 18));
            canvas.drawRect(0, 105, WIDTH, HEIGHT, paint);
            text(canvas, "GAME OVER", 100, 355, 34, Color.WHITE, true);
            text(canvas, "You reached wave " + wave, 125, 392, 19, Color.rgb(255, 209, 102), false);
            text(canvas, "Tap anywhere to try again", 102, 430, 17, Color.WHITE, false);
        }

        private void updateGame() {
            long now = System.currentTimeMillis();
            if (previousFrame == 0) previousFrame = now;
            float dt = Math.min((now - previousFrame) / 1000f, 0.05f);
            previousFrame = now;
            if (gameOver) return;

            if (waveRunning) {
                spawnTimer -= (long) (dt * 1000);
                if (spawned < waveSize && spawnTimer <= 0) {
                    enemies.add(new Enemy(GameBalance.enemyTypeForWave(wave), wave));
                    spawned++;
                    spawnTimer = GameBalance.spawnIntervalMs(wave);
                }
            }

            for (Enemy enemy : enemies) enemy.distance += enemy.speed * dt;
            for (Tower tower : towers) {
                tower.cooldown -= dt;
                if (tower.cooldown <= 0) {
                    Point origin = pads.get(tower.pad);
                    Enemy target = null;
                    float targetDistance = -1;
                    for (Enemy enemy : enemies) {
                        Point p = pointOnRoute(enemy.distance);
                        if (distance(origin.x, origin.y, p.x, p.y) <= tower.type.range && enemy.distance > targetDistance) {
                            target = enemy;
                            targetDistance = enemy.distance;
                        }
                    }
                    if (target != null) {
                        Point p = pointOnRoute(target.distance);
                        bolts.add(new Bolt(origin.x, origin.y, target, tower.type));
                        tower.cooldown = tower.type.fireIntervalSeconds;
                    }
                }
            }
            Iterator<Bolt> boltIterator = bolts.iterator();
            while (boltIterator.hasNext()) {
                Bolt bolt = boltIterator.next();
                if (bolt.target.health <= 0) {
                    boltIterator.remove();
                    continue;
                }
                Point target = pointOnRoute(bolt.target.distance);
                float dx = target.x - bolt.x;
                float dy = target.y - bolt.y;
                float length = (float) Math.sqrt(dx * dx + dy * dy);
                float travel = bolt.targetingTower.projectileSpeed * dt;
                if (length <= travel + bolt.target.type.radius) {
                    bolt.target.health -= bolt.targetingTower.damage;
                    boltIterator.remove();
                } else {
                    bolt.x += dx / length * travel;
                    bolt.y += dy / length * travel;
                }
            }
            Iterator<Enemy> enemyIterator = enemies.iterator();
            while (enemyIterator.hasNext()) {
                Enemy enemy = enemyIterator.next();
                if (enemy.health <= 0) {
                    gold += enemy.type.reward;
                    enemyIterator.remove();
                } else if (enemy.distance >= routeLength()) {
                    lives--;
                    enemyIterator.remove();
                    if (lives <= 0) gameOver = true;
                }
            }
            if (waveRunning && spawned == waveSize && enemies.isEmpty()) waveRunning = false;
        }

        private void startWave() {
            if (waveRunning || gameOver) return;
            wave++;
            waveSize = GameBalance.enemiesInWave(wave);
            spawned = 0;
            spawnTimer = 0;
            waveRunning = true;
        }

        private void resetGame() {
            gold = GameBalance.INITIAL_GOLD;
            lives = GameBalance.INITIAL_LIVES;
            wave = 0;
            spawned = 0;
            waveSize = 0;
            waveRunning = false;
            gameOver = false;
            towers.clear();
            enemies.clear();
            bolts.clear();
            previousFrame = System.currentTimeMillis();
        }

        @Override
        public boolean onTouchEvent(MotionEvent event) {
            if (event.getAction() != MotionEvent.ACTION_UP) return true;
            float scale = Math.min(getWidth() / (float) WIDTH, getHeight() / (float) HEIGHT);
            float x = (event.getX() - (getWidth() - WIDTH * scale) / 2f) / scale;
            float y = (event.getY() - (getHeight() - HEIGHT * scale) / 2f) / scale;
            if (gameOver) {
                resetGame();
                invalidate();
                return true;
            }
            if (y >= 735) {
                startWave();
                invalidate();
                return true;
            }
            for (int i = 0; i < pads.size(); i++) {
                Point pad = pads.get(i);
                if (distance(x, y, pad.x, pad.y) < 34 && findTower(i) == null
                        && gold >= GameBalance.BASIC_TOWER.cost) {
                    towers.add(new Tower(i, GameBalance.BASIC_TOWER));
                    gold -= GameBalance.BASIC_TOWER.cost;
                    invalidate();
                    return true;
                }
            }
            return true;
        }

        private Tower findTower(int pad) {
            for (Tower tower : towers) if (tower.pad == pad) return tower;
            return null;
        }

        private Point pointOnRoute(float distance) {
            float remaining = distance;
            for (int i = 1; i < route.size(); i++) {
                Point a = route.get(i - 1);
                Point b = route.get(i);
                float segment = distance(a.x, a.y, b.x, b.y);
                if (remaining <= segment) {
                    float t = segment == 0 ? 0 : remaining / segment;
                    return new Point(a.x + (b.x - a.x) * t, a.y + (b.y - a.y) * t);
                }
                remaining -= segment;
            }
            return route.get(route.size() - 1);
        }

        private float routeLength() {
            float length = 0;
            for (int i = 1; i < route.size(); i++) {
                Point a = route.get(i - 1);
                Point b = route.get(i);
                length += distance(a.x, a.y, b.x, b.y);
            }
            return length;
        }

        private static float distance(float x1, float y1, float x2, float y2) {
            float dx = x2 - x1;
            float dy = y2 - y1;
            return (float) Math.sqrt(dx * dx + dy * dy);
        }

        private void circle(Canvas canvas, float x, float y, float radius, int color) {
            paint.setColor(color);
            canvas.drawCircle(x, y, radius, paint);
        }

        private void text(Canvas canvas, String value, float x, float y, float size, int color, boolean bold) {
            paint.setColor(color);
            paint.setTextSize(size);
            paint.setTypeface(bold ? android.graphics.Typeface.create("sans-serif", android.graphics.Typeface.BOLD)
                    : android.graphics.Typeface.create("sans-serif", android.graphics.Typeface.NORMAL));
            paint.setStyle(Paint.Style.FILL);
            canvas.drawText(value, x, y, paint);
        }

        private static final class Point {
            final float x;
            final float y;

            Point(float x, float y) {
                this.x = x;
                this.y = y;
            }
        }

        private static final class Tower {
            final int pad;
            final TowerType type;
            float cooldown;

            Tower(int pad, TowerType type) {
                this.pad = pad;
                this.type = type;
            }
        }

        private static final class Enemy {
            final EnemyType type;
            float health;
            final float maxHealth;
            final float speed;
            float distance;

            Enemy(EnemyType type, int wave) {
                this.type = type;
                this.health = type.healthForWave(wave);
                this.maxHealth = health;
                this.speed = type.speedForWave(wave);
            }
        }

        private static final class Bolt {
            float x;
            float y;
            final Enemy target;
            final TowerType targetingTower;

            Bolt(float x, float y, Enemy target, TowerType targetingTower) {
                this.x = x;
                this.y = y;
                this.target = target;
                this.targetingTower = targetingTower;
            }
        }
    }
}
