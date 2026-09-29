package xean;

import mindustry.mod.Mod;
import xean.content.XenoBlocks;
import xean.content.XenoUnitTypes;

public class Crystallize extends Mod {
    @Override
    public void loadContent() {
        XenoBlocks.load();
        XenoUnitTypes.load();
    }
}