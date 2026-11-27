package crystallize.type.unit;

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
    public boolean isCloaked(Unit unit) {
        return !unit.hasEffect(revealedEffect);
    }
}
