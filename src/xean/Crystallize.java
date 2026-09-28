package xean;

import mindustry.mod.Mod;
import xean.content.CrystBlocks;

public class Crystallize extends Mod {
    @Override
    public void loadContent() {
        CrystBlocks.load();
    }
}