package crystallize.type.unit;

import mindustry.game.Team;
import mindustry.gen.Unit;

public class StealthUnitType extends XenoUnitType {
    public StealthUnitType(String name) {
        super(name);
    }

    @Override
    protected boolean weatherHides() {
        return false;
    }

    @Override
    public boolean targetable(Unit unit, Team targetter) {
        return unit.hasEffect(revealedEffect);
    }
}