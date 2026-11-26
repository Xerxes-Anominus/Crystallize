package crystallize.type.unit;

import arc.graphics.g2d.Draw;
import arc.math.Interp;
import arc.math.Mat;
import arc.math.Mathf;
import arc.util.Time;
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
import mindustry.type.StatusEffect;

public class BurrowUnitType extends XenoUnitType {
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
    public float switchCooldown = 180f;
    
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
    
    public float jumpHeight = 24f;
    public float emergeDepth = 16f;
    public float diveSquash = 0.6f;
    public float jumpEnd = 0.3f, diveEnd = 0.5f;

    private final Mat animMat = new Mat();

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

        if(Time.time % 6f < Time.delta) Fx.smoke.at(unit.x, unit.y);

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
            if(unit.getDuration(burrowingEffect) <= threshold) {
                unit.unapply(burrowingEffect);
                unit.apply(burrowedEffect, 60f);
                unit.apply(cooldownEffect, switchCooldown);
                Fx.smoke.at(unit.x, unit.y);
            }
            return;
        }
        
        if(unit.hasEffect(surfacingEffect)) {
            if(unit.getDuration(surfacingEffect) <= threshold) {
                unit.unapply(surfacingEffect);
                surface(unit);
                unit.apply(cooldownEffect, switchCooldown);
            }
            return;
        }
        
        if(unit.hasEffect(cooldownEffect)) return;

        if(isBurrowed(unit)) {
            if(unit.health <= unit.maxHealth * surfaceHealthFraction) {
                unit.apply(surfacingEffect, transitionTime);
            }
        } else {
            if(unit.health >= unit.maxHealth * reburrowHealthFraction) {
                unit.apply(burrowingEffect, transitionTime);
            }
        }
    }

    protected void surface(Unit unit) {
        unit.unapply(burrowedEffect);

        Damage.damage(unit.team, unit.x, unit.y, surfaceRadius, surfaceDamage, true, true, true);
        Fx.massiveExplosion.at(unit.x, unit.y);
        Effect.shake(6f, 20f, unit.x, unit.y);
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

    @Override
    public void draw(Unit unit) {
        boolean burrowing = unit.hasEffect(burrowingEffect);
        boolean surfacing = unit.hasEffect(surfacingEffect);

        if(burrowing || surfacing) {
            drawTransition(unit, burrowing);
            return;
        }
        if(isBurrowed(unit)) return;
        super.draw(unit);
    }

    protected void drawTransition(Unit unit, boolean down) {
        StatusEffect eff = down ? burrowingEffect : surfacingEffect;
        float p = Mathf.clamp(1f - unit.getDuration(eff) / Math.max(1f, transitionTime));

        float h = 0f;
        float along = 1f;
        float scale = 1f;

        if(down) {
            if(p < jumpEnd) {
                h = jumpHeight * Interp.pow2Out.apply(p / jumpEnd);
            } else if(p < diveEnd) {
                float t = (p - jumpEnd) / (diveEnd - jumpEnd);
                h = jumpHeight * (1f - Interp.pow2In.apply(t));
                along = Mathf.lerp(1f, diveSquash, Interp.pow2Out.apply(t));
                if(t > 0.95f) Fx.smoke.at(unit.x, unit.y);
            } else {
                float t = (p - diveEnd) / (1f - diveEnd);
                along = diveSquash;
                scale = 1f - Interp.smooth.apply(t);
                h = -emergeDepth * Interp.smooth.apply(t);
                if(Time.time % 4f < Time.delta) Fx.smoke.at(unit.x, unit.y);
            }
        } else {
            float e = Interp.smooth.apply(p);
            scale = Mathf.lerp(0.2f, 1f, e);
            h = -emergeDepth * (1f - e);
            if(Time.time % 4f < Time.delta) Fx.smoke.at(unit.x, unit.y);
        }

        if(scale <= 0.02f) return;

        float ox = unit.x, oy = unit.y, oe = unit.elevation;
        Mat prev = Draw.trans();

        try {
            if(h > 0f) unit.elevation = Math.max(oe, Mathf.clamp(h / jumpHeight));
            unit.y = oy + h;
            
            Draw.flush();
            animMat.setToTranslation(ox, oy + h)
            .rotate(unit.rotation)
            .scale(along * scale, scale)
            .rotate(-unit.rotation)
            .translate(-ox, -(oy + h));
            Draw.trans(animMat);

            super.draw(unit);

            Draw.flush();
        } finally {
            Draw.trans(prev);
            unit.x = ox;
            unit.y = oy;
            unit.elevation = oe;
        }
    }
}