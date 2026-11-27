package crystallize.type.unit;

import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.math.Mathf;
import arc.util.Time;
import crystallize.graphics.CrystShaders;
import mindustry.Vars;
import mindustry.content.StatusEffects;
import mindustry.content.Weathers;
import mindustry.game.Team;
import mindustry.gen.Unit;
import mindustry.graphics.Layer;
import mindustry.type.StatusEffect;
import mindustry.type.UnitType;
import mindustry.type.Weather;

import java.util.WeakHashMap;

public class XenoUnitType extends UnitType {
    public XenoUnitType(String name) {
        super(name);
        useUnitCap = false;
    }

    public Weather weather = Weathers.fog;
    public StatusEffect revealedEffect = StatusEffects.none;
    
    public boolean cloakEffect = true;
    public float cloakAlphaAlly = 0.35f;
    public float cloakAlphaEnemy = 0.05f;
    public float cloakFadeTime = 30f;
    public Color cloakColor = new Color(0.45f, 0.9f, 1f, 1f);
    
    private final WeakHashMap<Unit, float[]> cloakState = new WeakHashMap<>();

    protected boolean weatherHides() {
        return weather != null && weather.isActive();
    }
    
    public boolean isCloaked(Unit unit) {
        if(unit.hasEffect(revealedEffect)) return false;
        return weatherHides();
    }
    
    public float cloakLevel(Unit unit) {
        float target = isCloaked(unit) ? 1f : 0f;
        float[] st = cloakState.get(unit);
        if(st == null || st[1] != unit.id) return target;
        return st[0];
    }

    @Override
    public void update(Unit unit) {
        super.update(unit);

        float target = isCloaked(unit) ? 1f : 0f;
        float[] st = cloakState.get(unit);
        if(st == null || st[1] != unit.id) {
            st = new float[]{target, unit.id};
            cloakState.put(unit, st);
        }
        st[0] = Mathf.approach(st[0], target, Time.delta / Math.max(1f, cloakFadeTime));
    }

    @Override
    public boolean targetable(Unit unit, Team targetter) {
        if(unit.hasEffect(revealedEffect)) return true;
        return !isCloaked(unit) && super.targetable(unit, targetter);
    }
    
    protected float drawZ(Unit unit) {
        return unit.elevation > 0.5f
            ? (lowAltitude ? Layer.flyingUnitLow : Layer.flyingUnit)
            : groundLayer + Mathf.clamp(hitSize / 4000f, 0f, 0.01f);
    }

    protected float cloakAlphaFor(Unit unit) {
        return unit.team == Vars.player.team() ? cloakAlphaAlly : cloakAlphaEnemy;
    }

    @Override
    public void draw(Unit unit) {
        float level = cloakLevel(unit);
        CrystShaders.CloakShader sh = CrystShaders.cloak;

        if(!cloakEffect || sh == null) {
            if(level > 0.5f) return;
            super.draw(unit);
            return;
        }

        if(level <= 0.001f) {
            super.draw(unit);
            return;
        }

        float alpha = cloakAlphaFor(unit);
        if(level >= 0.999f && alpha <= 0.003f) return;
        
        Draw.draw(drawZ(unit), () -> drawNow(unit));
    }
    
    protected void drawNow(Unit unit) {
        float level = cloakLevel(unit);
        CrystShaders.CloakShader sh = CrystShaders.cloak;

        if(!cloakEffect || sh == null) {
            if(level <= 0.5f) super.draw(unit);
            return;
        }
        if(level <= 0.001f) {
            super.draw(unit);
            return;
        }

        sh.cx = unit.x;
        sh.cy = unit.y;
        sh.cloak = level;
        sh.alpha = cloakAlphaFor(unit);
        sh.color.set(cloakColor);

        Draw.flush();
        Draw.shader(sh);
        try {
            super.draw(unit);
        } finally {
            Draw.flush();
            Draw.shader();
        }
    }
}
