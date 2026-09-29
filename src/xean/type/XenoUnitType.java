package xean.type;

import arc.audio.Sound;
import arc.util.Nullable;
import mindustry.Vars;
import mindustry.content.Fx;
import mindustry.entities.Damage;
import mindustry.entities.Effect;
import mindustry.game.Team;
import mindustry.gen.Groups;
import mindustry.gen.Sounds;
import mindustry.gen.Unit;
import mindustry.type.StatusEffect;
import mindustry.type.UnitType;
import xean.util.Check;

public class XenoUnitType extends UnitType {
    public XenoUnitType(String name) {
        super(name);
    }
    
    public @Nullable StatusEffect invisibleEffect = null;
    
    public boolean
    explodable, transmittable;
    
    public float
    explodeDamage, explodeRadius,
    shake, shakeDuration;
    public Effect explodeEffect = Fx.none;
    public Sound explodeSound = Sounds.none;
    
    public float
    infectRadius, infectTime;
    public @Nullable StatusEffect infectEffect = null;
    
    @Override
    public boolean targetable(Unit unit, Team targeter) {
        if(invisibleEffect == null) return super.targetable(unit, targeter);
        
        if(unit.hasEffect(invisibleEffect)) {
            return false;
        }else{
            return true;
        }
    }
    
    protected void infect(Unit unit) {
        if(infectEffect == null || !transmittable) return;
        
        Groups.unit.each(u -> {
            if(Check.range(infectRadius, unit.x, unit.y, u.x, u.y) && u.team != unit.team) {
                u.apply(infectEffect, infectTime);
            }
        });
    }
    
    protected void explode(Unit unit) {
        if(!explodable) return;
        
        Damage.damage(unit.x, unit.y, explodeRadius, explodeDamage);
    }
    
    @Override
    public void killed(Unit unit) {
        super.killed(unit);
        infect(unit);
        explode(unit);
    }
}