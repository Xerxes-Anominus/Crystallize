package crystallize.type.unit;

import mindustry.gen.Unit;

/** Tang hinh vinh vien: chi hien khi dinh revealedEffect, khong phu thuoc thoi tiet. */
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
