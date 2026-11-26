package crystallize.type.unit;

import mindustry.content.StatusEffects;
import mindustry.content.Weathers;
import mindustry.game.Team;
import mindustry.gen.Unit;
import mindustry.type.StatusEffect;
import mindustry.type.UnitType;
import mindustry.type.Weather;

public class XenoUnitType extends UnitType {
    public XenoUnitType(String name) {
        super(name);
        useUnitCap = false;
    }
    
    public Weather weather = Weathers.fog;
    public StatusEffect revealedEffect = StatusEffects.none;

    protected boolean weatherHides() {
        return weather != null && weather.isActive();
    }

    @Override
    public boolean targetable(Unit unit, Team targetter) {
        if(unit.hasEffect(revealedEffect)) return true;
        return !weatherHides() && super.targetable(unit, targetter);
    }

    @Override
    public void draw(Unit unit) {
        if(!unit.hasEffect(revealedEffect)) return;
        if(weatherHides()) return;
        super.draw(unit);
    }
}