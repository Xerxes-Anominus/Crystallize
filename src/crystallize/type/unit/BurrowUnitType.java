package crystallize.type.unit;

import arc.Core;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Fill;
import arc.graphics.g2d.Lines;
import arc.math.Angles;
import arc.math.Interp;
import arc.math.Mat;
import arc.math.Mathf;
import arc.math.Rand;
import arc.util.Time;
import arc.util.Tmp;
import crystallize.graphics.CrystShaders;
import mindustry.Vars;
import mindustry.content.Fx;
import mindustry.content.StatusEffects;
import mindustry.entities.Damage;
import mindustry.entities.Effect;
import mindustry.entities.Units;
import mindustry.entities.units.WeaponMount;
import mindustry.game.Team;
import mindustry.gen.Legsc;
import mindustry.gen.Unit;
import mindustry.graphics.Layer;
import mindustry.graphics.Pal;
import mindustry.type.StatusEffect;
import mindustry.world.Tile;

public class BurrowUnitType extends XenoUnitType {
    public enum TransitionStyle { slow, leap }

    public BurrowUnitType(String name) {
        super(name);

        burrowedEffect = new StatusEffect(name + "-burrowed") {{
            permanent = true;
        }};
        burrowingEffect = new StatusEffect(name + "-burrowing");
        surfacingEffect = new StatusEffect(name + "-surfacing");
        cooldownEffect = new StatusEffect(name + "-burrow-cooldown");
    }

    public float surfaceHealthFraction = 0.4f;
    public float reburrowHealthFraction = 0.6f;
    public float burrowDrainPerSecond = 0f;
    
    public float transitionTime = 45f;
    public float leapTime = 80f;
    public float switchCooldown = 180f;
    
    public TransitionStyle burrowStyle = TransitionStyle.leap;
    public TransitionStyle surfaceStyle = TransitionStyle.slow;

    public float burrowSpeedMultiplier = 0.6f;
    public float transitionSpeedMultiplier = 0.1f;

    public float burrowDamage = 20f;
    public float burrowRadius = 48f;
    public float burrowInterval = 20f;
    public StatusEffect slowEffect = StatusEffects.slow;
    public float slowDuration = 40f;

    public float surfaceDamage = 400f;
    public float surfaceRadius = 96f;

    public StatusEffect burrowedEffect;
    public StatusEffect burrowingEffect;
    public StatusEffect surfacingEffect;
    public StatusEffect cooldownEffect;
    
    public float jumpHeight = 0f;
    public float emergeDepth = 16f;
    public float diveSquash = 0.6f;
    public float leapShake = 1f;
    public float jumpEnd = 0.3f, diveEnd = 0.5f;
    
    public float moundScale = 0.85f;
    public boolean moundEffect = true;
    
    protected static final float DOWN_CROUCH = 0.20f, DOWN_LAND = 0.72f;
    protected static final float UP_RUMBLE = 0.22f, UP_LAND = 0.78f;

    private static final Rand fxRand = new Rand();
    
    public Effect dustFx = new Effect(40f, 120f, e -> {
        float k = fxScaleOf(e.data);
        Draw.color(e.color, Color.lightGray, 0.25f);
        Draw.alpha(0.85f * e.fout());
        Angles.randLenVectors(e.id, 5, (2f + e.finpow() * 16f) * k, e.rotation, 38f, (x, y) ->
            Fill.circle(e.x + x, e.y + y, (0.8f + e.fout() * 2.4f) * k));
    });
    
    public Effect trailFx = new Effect(80f, 120f, e -> {
        float k = fxScaleOf(e.data);
        float len = (e.data instanceof Float f ? f : 8f) * (0.35f + e.fin() * 0.25f);
        Draw.color(e.color, Color.black, 0.5f);
        Draw.alpha(0.6f * e.fout(Interp.pow2Out));
        Lines.stroke(1.3f * k);
        Lines.lineAngle(e.x, e.y, e.rotation + 150f, len);
        Lines.lineAngle(e.x, e.y, e.rotation - 150f, len);
        Lines.stroke(0.9f * k);
        Lines.lineAngle(e.x, e.y, e.rotation + 180f, len * 1.4f);
    }).layer(Layer.debris);
    
    public Effect burstFx = new Effect(45f, 400f, e -> {
        float k = fxScaleOf(e.data);
        Draw.color(e.color, Color.white, e.fout() * 0.3f);
        Lines.stroke((e.fout() * 3f + 0.2f) * k);
        Lines.circle(e.x, e.y, e.rotation * Interp.pow3Out.apply(e.fin()));
        Draw.color(e.color);
        Angles.randLenVectors(e.id, 14, e.rotation * 0.9f * e.finpow(), (x, y) ->
            Fill.square(e.x + x, e.y + y, (1f + e.fout() * 3f) * k, e.finpow() * 180f));
    });
    
    public Effect debrisFx = new Effect(62f, 500f, e -> {
        float size = e.data instanceof Float f ? f : 8f;
        float force = Math.max(0.2f, e.rotation);
        Rand r = fxRand;
        r.setSeed(e.id);
        int n = 10 + (int)Math.min(16f, size / 2f);
        float t = e.fin();
        float ease = 1f - (1f - t) * (1f - t);
        float fade = Mathf.clamp((1f - t) / 0.2f);
        float k = fxScaleOf(e.data);

        for(int i = 0; i < n; i++) {
            float ang = r.random(360f);
            float dist = size * r.random(0.6f, 2.0f) * force;
            float peak = size * r.random(0.9f, 2.6f) * force;
            float sz = (0.9f + r.random(2.2f)) * k;
            float shade = r.random(0.55f, 1.15f);
            float rot = r.random(360f) + t * r.random(-720f, 720f);

            float px = e.x + Mathf.cosDeg(ang) * dist * ease;
            float py = e.y + Mathf.sinDeg(ang) * dist * ease * 0.8f;
            float z = 4f * peak * t * (1f - t);
            
            Draw.color(Pal.shadow);
            Draw.alpha(0.3f * fade * Mathf.clamp(1f - z / (peak + 1f) * 0.6f));
            Fill.circle(px, py, sz * 0.9f);

            Draw.color(Tmp.c2.set(e.color).mul(shade));
            Draw.alpha(fade);
            Fill.square(px, py + z, sz, rot);
        }
    });
    
    public Effect fallFx = new Effect(28f, 300f, e -> {
        float size = 8f, h = 10f;
        if(e.data instanceof float[] d) { size = d[0]; h = d[1]; }
        float k = fxScale(size);
        Rand r = fxRand;
        r.setSeed(e.id);
        float t = e.fin();
        for(int i = 0; i < 3; i++) {
            float ox = r.range(size * 0.5f), oy = r.range(size * 0.3f);
            float start = h * r.random(0.7f, 1f);
            float y = e.y + oy + start * (1f - t * t);
            Draw.color(Pal.shadow);
            Draw.alpha(0.25f * Mathf.clamp(t * 3f) * e.fout());
            Fill.circle(e.x + ox, e.y + oy, 1.1f * k);
            Draw.color(Tmp.c2.set(e.color).mul(0.8f));
            Draw.alpha(Mathf.clamp((1f - t) * 4f));
            Fill.square(e.x + ox, y, (0.9f + r.random(1.4f)) * k, r.random(360f));
        }
    });
    
    public Effect craterFx = new Effect(260f, 400f, e -> {
        float size = e.data instanceof Float f ? f : 8f;
        float power = Math.max(0.3f, e.rotation);
        float grow = Interp.pow2Out.apply(Mathf.clamp(e.time / 8f));
        float base = size * 0.9f * power * (0.35f + 0.65f * grow);
        float a = Mathf.clamp(e.fout() * 2.2f);
        Rand r = fxRand;
        r.setSeed(e.id);

        int n = 10;
        float[] rad = new float[n];
        for(int i = 0; i < n; i++) rad[i] = base * r.random(0.8f, 1.15f);
        
        Draw.color(Tmp.c2.set(e.color).mul(0.7f));
        Draw.alpha(0.5f * a);
        for(int i = 0; i < n; i++) {
            float a0 = i * 360f / n, a1 = (i + 1) * 360f / n;
            float r0 = rad[i] * 1.25f, r1 = rad[(i + 1) % n] * 1.25f;
            Fill.tri(e.x, e.y,
                e.x + Mathf.cosDeg(a0) * r0, e.y + Mathf.sinDeg(a0) * r0 * 0.9f,
                e.x + Mathf.cosDeg(a1) * r1, e.y + Mathf.sinDeg(a1) * r1 * 0.9f);
        }
        
        Draw.color(Tmp.c2.set(e.color).mul(0.25f));
        Draw.alpha(0.75f * a);
        for(int i = 0; i < n; i++) {
            float a0 = i * 360f / n, a1 = (i + 1) * 360f / n;
            float r0 = rad[i], r1 = rad[(i + 1) % n];
            Fill.tri(e.x, e.y,
                e.x + Mathf.cosDeg(a0) * r0, e.y + Mathf.sinDeg(a0) * r0 * 0.9f,
                e.x + Mathf.cosDeg(a1) * r1, e.y + Mathf.sinDeg(a1) * r1 * 0.9f);
        }
        
        Draw.color(Tmp.c2.set(e.color).mul(0.2f));
        Lines.stroke(Math.max(0.8f, fxScale(size) * 1.1f));
        for(int i = 0; i < 9; i++) {
            float ang = r.random(360f);
            float len = base * r.random(1.3f, 2.4f) * grow;
            Draw.alpha(0.55f * a);
            Lines.lineAngle(e.x + Mathf.cosDeg(ang) * base * 0.9f, e.y + Mathf.sinDeg(ang) * base * 0.8f, ang, len - base * 0.9f);
        }
    }).layer(Layer.debris);
    
    public Effect impactFx = new Effect(42f, 500f, e -> {
        float size = e.data instanceof Float f ? f : 8f;
        float force = Math.max(0.3f, e.rotation);
        float k = fxScale(size);

        Draw.color(e.color, Color.white, 0.35f * e.fout());
        Lines.stroke(Mathf.lerp(3.4f, 0.3f, e.fin()) * k);
        Lines.circle(e.x, e.y, size * (0.5f + 2.8f * force * Interp.pow3Out.apply(e.fin())));

        Draw.color(e.color, Color.lightGray, 0.3f);
        Draw.alpha(0.7f * e.fout());
        Angles.randLenVectors(e.id, 14, size * (0.5f + 2.2f * force) * e.finpow(), (x, y) ->
            Fill.circle(e.x + x, e.y + y * 0.85f, (1.2f + e.fout() * 3.2f) * k));
    });
    
    public Effect columnFx = new Effect(48f, 500f, e -> {
        float size = e.data instanceof Float f ? f : 8f;
        float force = Math.max(0.3f, e.rotation);
        float k = fxScale(size);
        Draw.color(e.color, Color.lightGray, 0.35f);
        Draw.alpha(0.75f * e.fout());
        Angles.randLenVectors(e.id, 12, size * 2.6f * force * e.finpow(), 90f, 24f, (x, y) ->
            Fill.circle(e.x + x * 0.6f, e.y + y, (1.6f + e.fout() * 3.6f) * k));
    });

    private static float fxScaleOf(Object data) {
        return fxScale(data instanceof Float f ? f : 8f);
    }

    private static float fxScale(float size) {
        return Mathf.clamp(size / 12f, 0.4f, 4f);
    }

    private final Mat animMat = new Mat();
    private final Mat prevMat = new Mat();

    @Override
    public void init() {
        super.init();
        if(reburrowHealthFraction <= surfaceHealthFraction) {
            reburrowHealthFraction = Math.min(1f, surfaceHealthFraction + 0.1f);
        }
        burrowedEffect.speedMultiplier = burrowSpeedMultiplier;
        burrowingEffect.speedMultiplier = transitionSpeedMultiplier;
        surfacingEffect.speedMultiplier = transitionSpeedMultiplier / burrowSpeedMultiplier;
    }

    public boolean isBurrowed(Unit unit) {
        return unit.hasEffect(burrowedEffect);
    }

    public boolean isTransitioning(Unit unit) {
        return unit.hasEffect(burrowingEffect) || unit.hasEffect(surfacingEffect);
    }

    protected float durationOf(TransitionStyle style) {
        return style == TransitionStyle.leap ? leapTime : transitionTime;
    }

    protected float burrowDuration() { return durationOf(burrowStyle); }
    protected float surfaceDuration() { return durationOf(surfaceStyle); }

    protected float leapHeight(Unit unit) {
        return jumpHeight > 0f ? jumpHeight : unit.hitSize * 3f + 20f;
    }

    @Override
    public void update(Unit unit) {
        if(isBurrowed(unit)) disableWeapons(unit);
        super.update(unit);
        if(!unit.isValid()) return;

        updateState(unit);

        if(!isBurrowed(unit)) return;

        disableWeapons(unit);

        if(burrowDrainPerSecond > 0f) {
            unit.health -= burrowDrainPerSecond * Time.delta / 60f;
        }

        float spd = unit.vel.len();
        if(moundEffect && spd > 0.1f) {
            Color gc = groundColor(unit);
            float ang = unit.vel.angle();
            if((Time.time + unit.id) % 5f < Time.delta) dustFx.at(unit.x, unit.y, ang + 180f, gc, unit.hitSize);
            if((Time.time + unit.id) % 9f < Time.delta) trailFx.at(unit.x, unit.y, ang, gc, unit.hitSize);
        } else if(Time.time % 6f < Time.delta) {
            Fx.smoke.at(unit.x, unit.y);
        }

        if((Time.time + unit.id) % burrowInterval < Time.delta) {
            float r = burrowRadius;

            Vars.indexer.allBuildings(unit.x, unit.y, r, b -> {
                if(b.team != unit.team && b.team != Team.derelict) {
                    b.damage(unit.team, burrowDamage);
                }
            });

            Units.nearbyEnemies(unit.team, unit.x - r, unit.y - r, r * 2f, r * 2f, u -> {
                if(u.within(unit, r) && u.isGrounded() && !(u instanceof Legsc)) {
                    u.apply(slowEffect, slowDuration);
                }
            });
        }
    }

    protected void updateState(Unit unit) {
        float threshold = Time.delta * 2f;

        if(unit.hasEffect(burrowingEffect)) {
            float total = burrowDuration();
            float p1 = progress(unit, burrowingEffect, total);
            transitionEvents(unit, true, burrowStyle, p1 - Time.delta / total, p1);

            if(unit.getDuration(burrowingEffect) <= threshold) {
                unit.unapply(burrowingEffect);
                unit.apply(burrowedEffect, 60f);
                unit.apply(cooldownEffect, switchCooldown);
                Fx.smoke.at(unit.x, unit.y);
                if(burrowStyle == TransitionStyle.slow) {
                    burstFx.at(unit.x, unit.y, burrowRadius * 0.6f, groundColor(unit), unit.hitSize);
                }
            }
            return;
        }

        if(unit.hasEffect(surfacingEffect)) {
            float total = surfaceDuration();
            float p1 = progress(unit, surfacingEffect, total);
            float p0 = p1 - Time.delta / total;
            transitionEvents(unit, false, surfaceStyle, p0, p1);
            
            if(surfaceStyle == TransitionStyle.leap && isBurrowed(unit) && crossed(p0, p1, UP_LAND)) {
                surface(unit);
            }

            if(unit.getDuration(surfacingEffect) <= threshold) {
                unit.unapply(surfacingEffect);
                if(isBurrowed(unit)) surface(unit);
                unit.apply(cooldownEffect, switchCooldown);
            }
            return;
        }

        if(unit.hasEffect(cooldownEffect)) return;

        if(isBurrowed(unit)) {
            if(unit.health <= unit.maxHealth * surfaceHealthFraction) {
                unit.apply(surfacingEffect, surfaceDuration());
            }
        } else {
            if(unit.health >= unit.maxHealth * reburrowHealthFraction) {
                unit.apply(burrowingEffect, burrowDuration());
            }
        }
    }

    protected float progress(Unit unit, StatusEffect eff, float total) {
        return Mathf.clamp(1f - unit.getDuration(eff) / Math.max(1f, total));
    }

    protected static boolean crossed(float p0, float p1, float t) {
        return p0 < t && p1 >= t;
    }

    protected void surface(Unit unit) {
        unit.unapply(burrowedEffect);

        Damage.damage(unit.team, unit.x, unit.y, surfaceRadius, surfaceDamage, true, true, true);
        Fx.massiveExplosion.at(unit.x, unit.y);
        Effect.shake(6f * leapShake, 20f, unit.x, unit.y);
        burstFx.at(unit.x, unit.y, surfaceRadius, groundColor(unit), unit.hitSize);
    }

    protected void disableWeapons(Unit unit) {
        unit.isShooting = false;

        for(WeaponMount mount : unit.mounts) {
            mount.shoot = false;
            mount.rotate = false;
            mount.reload = mount.weapon.reload;
            mount.warmup = 0f;
            if(mount.bullet != null) mount.bullet.remove();
        }
    }

    @Override
    public boolean targetable(Unit unit, Team targetter) {
        if(isBurrowed(unit)) return false;
        return super.targetable(unit, targetter);
    }

    @Override
    public boolean hittable(Unit unit) {
        if(isBurrowed(unit)) return false;
        return super.hittable(unit);
    }

    protected void dustAround(Unit unit, Color gc, int count) {
        for(int i = 0; i < count; i++) {
            Tmp.v1.trns(Mathf.random(360f), Mathf.random(unit.hitSize * 0.6f));
            dustFx.at(unit.x + Tmp.v1.x, unit.y + Tmp.v1.y, Mathf.random(360f), gc, unit.hitSize);
        }
    }
    
    protected void transitionEvents(Unit unit, boolean down, TransitionStyle style, float p0, float p1) {
        if(Vars.headless) return;

        float size = unit.hitSize;
        float x = unit.x, y = unit.y;
        Color gc = groundColor(unit);
        boolean tick3 = (Time.time + unit.id) % 3f < Time.delta;
        boolean tick4 = (Time.time + unit.id) % 4f < Time.delta;
        float shk = leapShake * (1f + size / 40f);

        if(style == TransitionStyle.slow) {
            if(p0 <= 0f) Effect.shake(1.5f * shk, 12f, x, y);
            if(tick4) dustAround(unit, gc, 1);
            if((Time.time + unit.id) % 10f < Time.delta) debrisFx.at(x, y, 0.35f, gc, size);
            if(!down && crossed(p0, p1, 0.92f)) {
                impactFx.at(x, y, 0.45f, gc, size);
                Effect.shake(2f * shk, 10f, x, y);
            }
            return;
        }

        if(down) {
            if(p1 < DOWN_CROUCH && tick4) dustAround(unit, gc, 1);
            
            if(crossed(p0, p1, DOWN_CROUCH)) {
                impactFx.at(x, y, 0.55f, gc, size);
                debrisFx.at(x, y, 0.6f, gc, size);
                dustAround(unit, gc, 3);
                Effect.shake((2f + size * 0.06f) * leapShake, 14f, x, y);
            }
            
            if(crossed(p0, p1, DOWN_LAND)) {
                impactFx.at(x, y, 1.1f, gc, size);
                debrisFx.at(x, y, 1.1f, gc, size);
                craterFx.at(x, y, 0.7f, gc, size);
                columnFx.at(x, y, 0.6f, gc, size);
                Effect.shake((4f + size * 0.15f) * leapShake, 22f, x, y);
            }
        } else {
            if(p1 < UP_RUMBLE) {
                if(tick3) dustAround(unit, gc, 1);
                if((Time.time + unit.id) % 6f < Time.delta) Effect.shake((1f + p1 * 4f) * shk, 8f, x, y);
            }
            
            if(crossed(p0, p1, UP_RUMBLE)) {
                craterFx.at(x, y, 1f, gc, size);
                columnFx.at(x, y, 1f, gc, size);
                impactFx.at(x, y, 1f, gc, size);
                debrisFx.at(x, y, 1.5f, gc, size);
                Effect.shake((6f + size * 0.25f) * leapShake, 26f, x, y);
            }
            
            if(p1 > UP_RUMBLE && p1 < UP_LAND - 0.1f && tick3) {
                Pose o = new Pose();
                computePose(unit, false, surfaceStyle, p1, o);
                if(o.h > 2f) fallFx.at(x, y, 0f, gc, new float[]{size, o.h});
            }
            
            if(crossed(p0, p1, UP_LAND)) {
                impactFx.at(x, y, 1.3f, gc, size);
                debrisFx.at(x, y, 1.0f, gc, size);
                craterFx.at(x, y, 0.8f, gc, size);
                dustAround(unit, gc, 4);
                Effect.shake((5f + size * 0.2f) * leapShake, 22f, x, y);
            }
        }
    }

    protected static class Pose {
        float h;
        float sx = 1f, sy = 1f;
        float scale = 1f;
        float jx;
        float mound;
        float moundR = 1f;
        float shadowA = 1f;
        float shadowS = 1f;
        boolean draw = true;
    }

    private static float spring(float u, float amp, float decay, float freq) {
        return amp * (float)Math.exp(-decay * u) * Mathf.cos(u * Mathf.PI * 2f * freq);
    }
    
    protected void computePose(Unit unit, boolean down, TransitionStyle style, float p, Pose o) {
        float squash = (1f - diveSquash);
        float H = leapHeight(unit);
        float size = unit.hitSize;

        if(style == TransitionStyle.slow) {
            if(down) {
                float e = Interp.smooth.apply(Mathf.clamp((p - 0.1f) / 0.9f));
                o.scale = 1f - e;
                o.h = -emergeDepth * e;
                o.sx = 1f + 0.06f * e;
                o.sy = 1f - 0.12f * e;
                o.jx = Mathf.sin(Time.time, 1.3f, 0.7f) * (1f - e) * size / 16f;
                o.mound = Interp.smooth.apply(Mathf.clamp(p * 1.2f));
                o.shadowA = 1f - e;
                o.shadowS = 1f - 0.4f * e;
            } else {
                float e = Interp.smooth.apply(Mathf.clamp((p - 0.15f) / 0.85f));
                o.scale = e;
                o.h = -emergeDepth * (1f - e);
                float settle = p > 0.85f ? Mathf.sin((p - 0.85f) / 0.15f * Mathf.PI) * 0.06f : 0f;
                o.sy = 1f + settle;
                o.sx = 1f - settle * 0.5f;
                o.jx = Mathf.sin(Time.time, 1.1f, 0.7f) * (1f - e) * size / 16f;
                o.mound = 1f - Interp.smooth.apply(Mathf.clamp((p - 0.3f) / 0.7f));
                o.moundR = 0.8f + 0.2f * Interp.smooth.apply(Mathf.clamp(p / 0.3f));
                o.shadowA = e;
                o.shadowS = 0.6f + 0.4f * e;
                o.draw = e > 0.03f;
            }
            return;
        }

        if(down) {
            if(p < DOWN_CROUCH) {
                float u = p / DOWN_CROUCH;
                float s = Interp.pow2.apply(u);
                o.sy = 1f - squash * 0.65f * s;
                o.sx = 1f + squash * 0.33f * s;
                o.h = -size * 0.08f * s;
                o.mound = 0f;
            } else if(p < DOWN_LAND) {
                float u = (p - DOWN_CROUCH) / (DOWN_LAND - DOWN_CROUCH);
                float par = 4f * u * (1f - u);
                float launch = Mathf.clamp(1f - u / 0.22f);
                launch *= launch;
                float fall = Mathf.clamp((u - 0.72f) / 0.28f);
                fall *= fall;
                float release = Interp.smooth.apply(Mathf.clamp(u / 0.1f));
                float crouchSy = 1f - squash * 0.65f, crouchSx = 1f + squash * 0.33f;
                o.h = H * par;
                o.sy = Mathf.lerp(crouchSy, 1f + 0.22f * launch + 0.18f * fall, release);
                o.sx = Mathf.lerp(crouchSx, 1f - 0.10f * launch - 0.08f * fall, release);
                o.scale = 1f + 0.18f * par;
                o.shadowA = 1f - 0.5f * par;
                o.shadowS = 1f - 0.3f * par;
                o.mound = 0f;
            } else {
                float u = (p - DOWN_LAND) / (1f - DOWN_LAND);
                float e = Interp.pow2Out.apply(u);
                o.scale = 1f - e;
                o.h = -emergeDepth * e;
                float a = spring(u, squash * 0.75f, 3.2f, 0.6f);
                o.sy = 1f - a;
                o.sx = 1f + a * 0.55f;
                o.mound = Interp.smooth.apply(u);
                o.shadowA = 1f - e;
                o.shadowS = 1f - 0.3f * e;
            }
        } else {
            if(p < UP_RUMBLE) {
                float u = p / UP_RUMBLE;
                o.draw = false;
                o.mound = 1f;
                o.moundR = 0.7f + 0.5f * Interp.pow2In.apply(u) + Mathf.sin(Time.time, 1.4f, 0.04f * u);
            } else if(p < UP_LAND) {
                float u = (p - UP_RUMBLE) / (UP_LAND - UP_RUMBLE);
                float par = 4f * u * (1f - u);
                float emerge = Interp.pow2Out.apply(Mathf.clamp(u / 0.14f));
                float launch = Mathf.clamp(1f - u / 0.3f);
                launch *= launch;
                float fall = Mathf.clamp((u - 0.72f) / 0.28f);
                fall *= fall;
                o.h = H * par;
                o.scale = (0.35f + 0.65f * emerge) * (1f + 0.18f * par);
                o.sy = 1f + 0.30f * launch + 0.18f * fall;
                o.sx = 1f - 0.14f * launch - 0.08f * fall;
                o.shadowA = (0.2f + 0.8f * emerge) * (1f - 0.5f * par);
                o.shadowS = 1f - 0.3f * par;
                o.mound = Mathf.clamp(1f - u / 0.25f);
                o.moundR = 1.2f;
            } else {
                float u = (p - UP_LAND) / (1f - UP_LAND);
                float a = spring(u, squash * 0.75f, 3.2f, 1.1f);
                o.sy = 1f - a;
                o.sx = 1f + a * 0.55f;
            }
        }
    }

    protected Color groundColor(Unit unit) {
        Tile t = unit.tileOn();
        if(t == null || t.floor() == null) return Tmp.c1.set(0.42f, 0.35f, 0.28f, 1f);
        return Tmp.c1.set(t.floor().mapColor);
    }

    @Override
    public void draw(Unit unit) {
        boolean burrowing = unit.hasEffect(burrowingEffect);
        boolean surfacing = unit.hasEffect(surfacingEffect);

        if(burrowing || surfacing) {
            drawTransition(unit, burrowing);
            return;
        }
        if(isBurrowed(unit)) {
            drawMound(unit, 1f, 1f);
            return;
        }
        super.draw(unit);
    }
    
    protected void drawMound(Unit unit, float power, float radiusMul) {
        CrystShaders.MoundShader sh = CrystShaders.mound;
        if(!moundEffect || sh == null || power <= 0.01f) return;

        float spd = unit.vel.len();
        float speedFrac = Mathf.clamp(spd / Math.max(0.01f, speed * burrowSpeedMultiplier));
        float ang = spd > 0.05f ? unit.vel.angle() : unit.rotation;
        float r = hitSize * moundScale * radiusMul;
        float x = unit.x, y = unit.y;
        Color gc = groundColor(unit);
        float cr = gc.r, cg = gc.g, cb = gc.b;

        Draw.draw(Layer.groundUnit - 1f, () -> {
            sh.cx = x;
            sh.cy = y;
            sh.radius = r;
            sh.power = power;
            sh.speed = speedFrac;
            sh.angle = ang;
            sh.color.set(cr, cg, cb, 1f);

            Draw.color();
            Draw.shader(sh);
            float size = r * 6.4f;
            Draw.rect(Core.atlas.white(), x, y, size, size);
            Draw.shader();
            Draw.reset();
        });
    }

    protected void drawTransition(Unit unit, boolean down) {
        TransitionStyle style = down ? burrowStyle : surfaceStyle;
        StatusEffect eff = down ? burrowingEffect : surfacingEffect;
        float p = progress(unit, eff, durationOf(style));

        final Pose o = new Pose();
        computePose(unit, down, style, p, o);

        drawMound(unit, o.mound, o.moundR);
        if(!o.draw || o.scale <= 0.02f) return;
        
        Draw.draw(drawZ(unit), () -> drawPosed(unit, o));
    }
    
    protected void drawPosed(Unit unit, Pose o) {
        float ox = unit.x, oy = unit.y;
        boolean softShadow = drawSoftShadow;
        float shadowElev = shadowElevation;

        prevMat.set(Draw.trans());

        try {
            drawGroundShadow(unit, o, ox, oy);
            
            drawSoftShadow = false;
            shadowElevation = 0f;

            unit.x = ox + o.jx;
            unit.y = oy + o.h;

            Draw.flush();
            animMat.setToTranslation(unit.x, unit.y)
            .scale(o.sx * o.scale, o.sy * o.scale)
            .translate(-unit.x, -unit.y);
            Draw.trans(animMat);

            drawNow(unit);

            Draw.flush();
        } finally {
            Draw.flush();
            Draw.trans(prevMat);
            unit.x = ox;
            unit.y = oy;
            drawSoftShadow = softShadow;
            shadowElevation = shadowElev;
        }
    }

    protected void drawGroundShadow(Unit unit, Pose o, float gx, float gy) {
        if(shadowRegion == null) return;
        float a = o.shadowA * (1f - cloakLevel(unit));
        if(a <= 0.01f) return;

        float s = Math.max(0f, o.shadowS);
        Draw.color(Pal.shadow, Pal.shadow.a * 2.2f * a);
        Draw.rect(shadowRegion, gx, gy,
            shadowRegion.width * Draw.scl * s, shadowRegion.height * Draw.scl * s,
            unit.rotation - 90f);
        Draw.color();
    }
}