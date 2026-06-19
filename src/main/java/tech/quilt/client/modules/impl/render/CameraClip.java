package tech.quilt.client.modules.impl.render;

import com.darkmagician6.eventapi.EventTarget;
import net.minecraft.client.option.Perspective;
import net.minecraft.util.math.Vec3d;
import tech.quilt.base.animations.base.Animation;
import tech.quilt.base.animations.base.Easing;
import tech.quilt.base.events.impl.render.EventCamera;
import tech.quilt.base.events.impl.render.EventCameraPosition;
import tech.quilt.base.events.impl.render.EventRender3D;
import tech.quilt.client.modules.api.Category;
import tech.quilt.client.modules.api.Module;
import tech.quilt.client.modules.api.ModuleAnnotation;
import tech.quilt.client.modules.api.setting.impl.BooleanSetting;
import tech.quilt.client.modules.api.setting.impl.ModeSetting;
import tech.quilt.client.modules.api.setting.impl.NumberSetting;

@ModuleAnnotation(
        name = "CameraClip",
        category = Category.RENDER,
        description = "Плавная камера от третьего лица без клипа"
)
public class CameraClip extends Module {

    public static final CameraClip INSTANCE = new CameraClip();

    // ── Настройки ────────────────────────────────────────────────────────────
    public final BooleanSetting noFirstPerson =
            new BooleanSetting("NoFirst", true);

    public final ModeSetting mode =
            new ModeSetting("Режим", "Normal", "Action");

    // Normal
    private final NumberSetting distance =
            new NumberSetting("Дистанция", 3.5f, 1.0f, 20.0f, 0.5f,
                    () -> mode.is("Normal"));
    private final NumberSetting speed =
            new NumberSetting("Скорость", 10.0f, 1.0f, 50.0f, 0.5f,
                    () -> mode.is("Normal"));

    // Action
    private final NumberSetting actionDistance =
            new NumberSetting("Act.Дистанция", 4.0f, 0.5f, 20.0f, 0.5f,
                    () -> mode.is("Action"));
    private final NumberSetting smoothness =
            new NumberSetting("Плавность", 0.3f, 0.1f, 0.95f, 0.01f,
                    () -> mode.is("Action"));
    private final NumberSetting maxDistance =
            new NumberSetting("Макс.Дист.", 20.0f, 5.0f, 50.0f, 0.5f,
                    () -> mode.is("Action"));
    private final NumberSetting rotationSmoothness =
            new NumberSetting("Плавн.Вращ.", 0.15f, 0.01f, 0.5f, 0.01f,
                    () -> mode.is("Action"));
    private final NumberSetting rotationOffset =
            new NumberSetting("Смещ.Вращ.", 2.0f, 0.0f, 10.0f, 0.1f,
                    () -> mode.is("Action"));

    // ── Состояние ─────────────────────────────────────────────────────────────
    private final Animation anim = new Animation(200L, 0.0f, Easing.CUBIC_OUT);
    private Vec3d cameraPos = null;
    private Perspective lastPerspective = null;
    private float smoothYaw = 0f;
    private float smoothPitch = 0f;

    private CameraClip() {}

    // ── Lifecycle ─────────────────────────────────────────────────────────────
    @Override
    public void onDisable() {
        cameraPos = null;
        smoothYaw = 0f;
        smoothPitch = 0f;
        lastPerspective = null;
        super.onDisable();
    }

    // ── Tick (анимация) ───────────────────────────────────────────────────────
    @EventTarget
    public void onRender3D(EventRender3D e) {
        if (mc.player == null) return;

        Perspective current = mc.options.getPerspective();

        // Сброс при смене перспективы
        if (lastPerspective != null && lastPerspective != current) {
            if (current == Perspective.FIRST_PERSON) {
                anim.setValue(0f);
                cameraPos = null;
                smoothYaw = 0f;
                smoothPitch = 0f;
            } else {
                anim.setValue(0f);
            }
        }
        lastPerspective = current;

        boolean firstPerson = current == Perspective.FIRST_PERSON;

        if (mode.is("Normal")) {
            anim.update(firstPerson ? 0f : 1f);
        } else {
            anim.update(firstPerson ? 0f : 1f);
        }

        // Обновляем сглаженную позицию камеры в Action режиме
        if (mode.is("Action") && !firstPerson) {
            updateActionCamera(mc.player.getPos());
        }
    }

    // ── EventCamera — управляет дистанцией в Normal режиме ────────────────────
    @EventTarget
    public void onCamera(EventCamera e) {
        if (mc.player == null) return;
        if (mc.options.getPerspective() == Perspective.FIRST_PERSON) return;

        if (mode.is("Normal")) {
            float animVal = anim.getValue();
            float dist = 1f + (distance.getCurrent() - 1f) * animVal;
            e.setDistance(dist);
            e.setCameraClip(true);
            e.setCancelled(true);
        }
    }

    // ── EventCameraPosition — управляет позицией камеры в Action режиме ───────
    @EventTarget
    public void onCameraPosition(EventCameraPosition e) {
        if (mc.player == null) return;
        if (!mode.is("Action")) return;
        if (mc.options.getPerspective() == Perspective.FIRST_PERSON) return;

        Vec3d pos = getCameraPos();
        if (pos != null) {
            e.setPos(pos);
        }
    }

    // ── Вспомогательные методы ────────────────────────────────────────────────

    private void updateActionCamera(Vec3d playerPos) {
        if (mc.player == null) return;

        if (cameraPos == null) {
            cameraPos = mc.player.getEyePos();
            smoothYaw = mc.player.getYaw();
            smoothPitch = mc.player.getPitch();
            return;
        }

        float currentYaw = mc.player.getYaw();
        float currentPitch = mc.player.getPitch();

        float rotSmooth = rotationSmoothness.getCurrent();
        smoothYaw += (currentYaw - smoothYaw) * rotSmooth;
        smoothPitch += (currentPitch - smoothPitch) * rotSmooth;

        float yawDelta = currentYaw - smoothYaw;
        float pitchDelta = currentPitch - smoothPitch;
        float offsetMul = rotationOffset.getCurrent();

        double yawRad = Math.toRadians(smoothYaw);
        double rightX = -Math.cos(yawRad);
        double rightZ = -Math.sin(yawRad);

        double rotOffX = rightX * yawDelta * offsetMul * 0.02;
        double rotOffY = pitchDelta * offsetMul * 0.02;
        double rotOffZ = rightZ * yawDelta * offsetMul * 0.02;

        double dist = cameraPos.distanceTo(playerPos);
        float maxDist = maxDistance.getCurrent();

        if (dist > maxDist) {
            cameraPos = playerPos;
            return;
        }

        float smooth = smoothness.getCurrent();
        double dynFactor = smooth * (1.0 - Math.exp(-dist / maxDist));

        double eyeY = playerPos.y + mc.player.getEyeHeight(mc.player.getPose());
        double dx = playerPos.x - cameraPos.x + rotOffX;
        double dy = eyeY - cameraPos.y + rotOffY;
        double dz = playerPos.z - cameraPos.z + rotOffZ;

        cameraPos = new Vec3d(
                cameraPos.x + dx * dynFactor,
                cameraPos.y + dy * dynFactor,
                cameraPos.z + dz * dynFactor
        );
    }

    public Vec3d getCameraPos() {
        if (mc.options.getPerspective() == Perspective.FIRST_PERSON) {
            return new Vec3d(
                    mc.player.getX(),
                    mc.player.getY() + mc.player.getEyeHeight(mc.player.getPose()),
                    mc.player.getZ()
            );
        }
        return cameraPos;
    }

    public boolean isNormal() {
        return isEnabled() && mode.is("Normal");
    }

    public boolean isAction() {
        return isEnabled() && mode.is("Action");
    }

    public boolean shouldModifyCamera() {
        return isEnabled() && mode.is("Action")
                && mc.options.getPerspective() != Perspective.FIRST_PERSON;
    }
}
