package tech.quilt.client.modules.impl.combat.rotation;

import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import tech.quilt.utility.game.player.rotation.Rotation;
import tech.quilt.utility.game.player.rotation.RotationUtil;
import tech.quilt.utility.interfaces.IMinecraft;

import java.util.Random;

/**
 * Человекоподобная ротация, максимально похожая на реального игрока.
 * Обходит античиты (Grimm, Vulcan, etc.) за счёт:
 * - Случайных микро-дражений камеры (human jitter)
 * - Несовершенного слежения (tracking lag)
 * - Естественных ограничений скорости вращения
 * - Случайных пауз и задержек
 * - Реалистичного ускорения/замедления камеры
 */
public class HumanRotation extends RotationBase implements IMinecraft {
    
    // Текущая цель
    private LivingEntity currentTarget;
    
    // Параметры человекоподобности
    private float maxSpeed = 150.0f; // Максимальная скорость вращения (градусов/тик)
    private float acceleration = 0.3f; // Ускорение камеры
    private float deceleration = 0.8f; // Замедление камеры
    private float jitterAmount = 0.15f; // Амплитуда микро-дражений
    private float trackingLag = 0.05f; // Задержка слежения
    private float pauseChance = 0.002f; // Шанс паузы (0-1)
    private int pauseDuration = 2; // Длительность паузы в тиках
    
    // Текущее состояние
    private float targetYaw;
    private float targetPitch;
    private float currentYawVelocity;
    private float currentPitchVelocity;
    private float smoothedTargetYaw;
    private float smoothedTargetPitch;
    private boolean isPaused = false;
    private int pauseTicksLeft = 0;
    
    // Генератор случайных чисел
    private final Random random = new Random();
    
    // Предыдущие углы для плавности
    private float prevYaw = 0;
    private float prevPitch = 0;
    
    // Таймеры для случайных событий
    private long lastJitterTime = 0;
    private final long jitterCooldown = 100; // 100ms между джиттерами
    
    // Накопленная ошибка слежения (для реалистичного отставания)
    private float yawError = 0;
    private float pitchError = 0;
    
    // Фильтр Калмана для сглаживания движений
    private float kalmanYaw = 0;
    private float kalmanPitch = 0;
    private float kalmanUncertainty = 0.1f;
    
    public HumanRotation() {
    }
    
    @Override
    public void update(Rotation targetAngle, boolean elytraVisual) {
        if (mc.player == null || mc.world == null) return;
        
        // Получаем целевые углы из targetAngle
        float targetYaw = targetAngle.getYaw();
        float targetPitch = targetAngle.getPitch();
        
        // Если пауза активна, просто продолжаем движение по инерции
        if (isPaused) {
            pauseTicksLeft--;
            if (pauseTicksLeft <= 0) {
                isPaused = false;
            }
            // Продолжаем движение по инерции даже во время паузы
            updateInertia();
            return;
        }
        
        // Случайная пауза
        if (random.nextFloat() < pauseChance) {
            isPaused = true;
            pauseTicksLeft = 1 + random.nextInt(pauseDuration);
        }
        
        // Добавляем случайный джиттер (микро-движения) к целевым углам
        targetYaw = addJitter(targetYaw);
        targetPitch = addJitter(targetPitch);
        
        // Применяем задержку слежения (lag)
        applyTrackingLag(targetYaw, targetPitch);
        
        // Вычисляем разницу углов
        float yawDiff = MathHelper.wrapDegrees(smoothedTargetYaw - mc.player.getYaw());
        float pitchDiff = MathHelper.wrapDegrees(smoothedTargetPitch - mc.player.getPitch());
        
        // Накапливаем ошибку слежения
        yawError += yawDiff * 0.1f;
        pitchError += pitchDiff * 0.1f;
        
        // Ограничиваем накопленную ошибку
        yawError = MathHelper.clamp(yawError, -5.0f, 5.0f);
        pitchError = MathHelper.clamp(pitchError, -5.0f, 3.0f);
        
        // Применяем фильтр Калмана
        applyKalmanFilter(yawDiff, pitchDiff);
        
        // Вычисляем ускорение на основе ошибки
        float urgency = 0.5f; // Средняя срочность
        
        // Ускоряем или замедляем камеру
        float targetYawVel = MathHelper.clamp(
            (yawDiff + yawError) * urgency * this.speedMultiplier * 2.0f,
            -maxSpeed,
            maxSpeed
        );
        float targetPitchVel = MathHelper.clamp(
            (pitchDiff + pitchError) * urgency * this.speedMultiplier * 2.0f,
            -maxSpeed,
            maxSpeed
        );
        
        // Плавно изменяем скорость камеры (ускорение/замедление)
        currentYawVelocity = currentYawVelocity * deceleration + targetYawVel * acceleration;
        currentPitchVelocity = currentPitchVelocity * deceleration + targetPitchVel * acceleration;
        
        // Применяем движение
        float newYaw = mc.player.getYaw() + currentYawVelocity * 0.15f;
        float newPitch = mc.player.getPitch() + currentPitchVelocity * 0.15f;
        
        // Ограничиваем углы
        newPitch = MathHelper.clamp(newPitch, -90.0f, 90.0f);
        
        // Применяем сглаживание для окончательного результата
        newYaw = smoothAngle(prevYaw, newYaw, 0.3f);
        newPitch = smoothAngle(prevPitch, newPitch, 0.3f);
        
        // Сохраняем предыдущие углы
        prevYaw = newYaw;
        prevPitch = newPitch;
        
        // Уменьшаем ошибку слежения (мы частично исправили её)
        yawError *= 0.9f;
        pitchError *= 0.9f;
        
        // Применяем ротацию
        this.lastYaw = newYaw;
        this.lastPitch = newPitch;
    }
    
    /**
     * Обновляет движение по инерции (используется во время пауз)
     */
    private void updateInertia() {
        float newYaw = mc.player.getYaw() + currentYawVelocity * 0.15f;
        float newPitch = mc.player.getPitch() + currentPitchVelocity * 0.15f;
        
        newPitch = MathHelper.clamp(newPitch, -90.0f, 90.0f);
        
        // Замедляем камеру со временем
        currentYawVelocity *= 0.95f;
        currentPitchVelocity *= 0.95f;
        
        if (Math.abs(currentYawVelocity) < 0.1f) currentYawVelocity = 0;
        if (Math.abs(currentPitchVelocity) < 0.1f) currentPitchVelocity = 0;
        
        this.lastYaw = newYaw;
        this.lastPitch = newPitch;
    }
    
    /**
     * Добавляет случайный джиттер (микро-движения камеры)
     */
    private float addJitter(float angle) {
        long currentTime = System.currentTimeMillis();
        float jitter = 0;
        
        // Джиттер с определенной частотой
        if (currentTime - lastJitterTime > jitterCooldown) {
            jitter += (random.nextFloat() * 2 - 1) * jitterAmount;
            lastJitterTime = currentTime;
        }
        
        // Добавляем небольшой шум каждый тик
        jitter += (random.nextFloat() * 2 - 1) * jitterAmount * 0.3f;
        
        return angle + jitter;
    }
    
    /**
     * Применяет задержку слежения (tracking lag)
     */
    private void applyTrackingLag(float targetYaw, float targetPitch) {
        // Плавно приближаем сглаженные углы к целевым
        float lagFactor = 0.1f + random.nextFloat() * 0.05f; // 10-15% lag
        
        this.smoothedTargetYaw = smoothAngle(this.smoothedTargetYaw, targetYaw, lagFactor);
        this.smoothedTargetPitch = smoothAngle(this.smoothedTargetPitch, targetPitch, lagFactor);
    }
    
    /**
     * Применяет фильтр Калмана для сглаживания движений
     */
    private void applyKalmanFilter(float yawDiff, float pitchDiff) {
        float predictionError = kalmanUncertainty + 0.1f;
        float measurementError = 0.5f;
        
        float kalmanGain = predictionError / (predictionError + measurementError);
        
        kalmanYaw = kalmanYaw + kalmanGain * (yawDiff - kalmanYaw);
        kalmanPitch = kalmanPitch + kalmanGain * (pitchDiff - kalmanPitch);
        
        kalmanUncertainty = (1 - kalmanGain) * predictionError;
    }
    
    /**
     * Сглаживает угол
     */
    private float smoothAngle(float current, float target, float factor) {
        float diff = MathHelper.wrapDegrees(target - current);
        return current + diff * factor;
    }
    
    /**
     * Обновляет ротацию с учётом цели (для совместимости с Aura)
     */
    public void update(LivingEntity target, Rotation targetAngle, boolean elytraVisual) {
        setTarget(target);
        update(targetAngle, elytraVisual);
    }
    
    /**
     * Сбрасывает состояние
     */
    public void resetHistory() {
        reset();
    }
    
    /**
     * Полный сброс состояния
     */
    private void reset() {
        currentTarget = null;
        targetYaw = 0;
        targetPitch = 0;
        currentYawVelocity = 0;
        currentPitchVelocity = 0;
        smoothedTargetYaw = 0;
        smoothedTargetPitch = 0;
        isPaused = false;
        pauseTicksLeft = 0;
        yawError = 0;
        pitchError = 0;
        kalmanYaw = 0;
        kalmanPitch = 0;
        kalmanUncertainty = 0.1f;
        prevYaw = 0;
        prevPitch = 0;
    }
    
    /**
     * Устанавливает текущую цель
     */
    public void setTarget(LivingEntity target) {
        if (currentTarget != target) {
            reset();
            currentTarget = target;
            if (target != null) {
                Vec3d lookVec = target.getBoundingBox().getCenter().subtract(mc.player.getEyePos());
                Rotation rot = RotationUtil.fromVec3d(lookVec);
                targetYaw = rot.getYaw();
                targetPitch = rot.getPitch();
                smoothedTargetYaw = targetYaw;
                smoothedTargetPitch = targetPitch;
            }
        }
    }
    
    /**
     * Устанавливает максимальную скорость вращения
     */
    public void setMaxSpeed(float speed) {
        this.maxSpeed = MathHelper.clamp(speed, 10.0f, 360.0f);
    }
    
    /**
     * Устанавливает ускорение камеры
     */
    public void setAcceleration(float accel) {
        this.acceleration = MathHelper.clamp(accel, 0.1f, 1.0f);
    }
    
    /**
     * Устанавливает замедление камеры
     */
    public void setDeceleration(float decel) {
        this.deceleration = MathHelper.clamp(decel, 0.5f, 0.99f);
    }
    
    /**
     * Устанавливает амплитуду джиттера
     */
    public void setJitterAmount(float amount) {
        this.jitterAmount = MathHelper.clamp(amount, 0.0f, 0.5f);
    }
    
    /**
     * Устанавливает задержку слежения
     */
    public void setTrackingLag(float lag) {
        this.trackingLag = MathHelper.clamp(lag, 0.0f, 0.3f);
    }
}
