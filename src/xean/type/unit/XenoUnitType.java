package xean.type.unit;

import arc.audio.Sound;
import mindustry.Vars;
import mindustry.content.Fx;
import mindustry.content.StatusEffects;
import mindustry.content.Weathers;
import mindustry.entities.Damage;
import mindustry.entities.Effect;
import mindustry.game.Team;
import mindustry.gen.Groups;
import mindustry.gen.Sounds;
import mindustry.gen.Unit;
import mindustry.type.StatusEffect;
import mindustry.type.UnitType;
import mindustry.type.Weather;

public class XenoUnitType extends UnitType {
    public XenoUnitType(String name) {
        super(name);
    }
    
    public Weather weather = Weathers.fog;
    
    public boolean
    explodable, transmittable, invisible;
    
    public float
    explodeDamage, explodeRadius,
    shake, shakeDuration;
    public Effect explodeEffect = Fx.none;
    public Sound explodeSound = Sounds.none;
    
    public float
    infectTime, infectRadius;
    public StatusEffect infectEffect = StatusEffects.none;
    
    @Override
    public void draw(Unit unit) {
        if(invisible()) return;
        super.draw(unit);
    }
    
    @Override
    public boolean targetable(Unit unit, Team targeter) {
        if(!invisible) super.targetable(unit, targeter);
        return invisible;
    }
    
    protected boolean invisible() {
        if(weather.isActive()) return true;
        return false;
    }
    
    protected void infect(Unit unit) {
        if(!transmittable) return;
        
        Groups.unit.each(u -> {
            float
            dx = u.x - unit.x,
            dy = u.y - unit.y,
            d = dx * dx + dy * dy;
            if(d <= infectRadius && u.team != unit.team) {
                u.apply(infectEffect, infectTime);
            }
        });
    }
    
    protected void explode(Unit unit) {
        if(!explodable) return;
        Damage.damage(unit.x, unit.y, explodeRadius, explodeDamage);
    }
}