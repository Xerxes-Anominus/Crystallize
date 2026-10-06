package xean;

import mindustry.mod.Mod;
import xean.ai.Cryst_ExtendedPathfinder;
import xean.content.Cryst_EnvBlocks;
import xean.content.Cryst_Items;
import xean.content.Cryst_Planets;

public class Crystallize extends Mod {
    @Override
    public void loadContent() {
        Cryst_ExtendedPathfinder.addonFieldTypes();
        
        Cryst_Items.load();
        Cryst_EnvBlocks.load();
        Cryst_Planets.load();
    }
}