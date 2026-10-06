package xean;

import mindustry.mod.ClassMap;
import mindustry.mod.Mod;
import xean.ai.Cryst_ExtendedPathfinder;
import xean.ai.MiteAI;
import xean.content.Cryst_Blocks;
import xean.content.Cryst_UnitTypes;
import xean.type.unit.MiteUnitType;
import xean.world.block.environment.CrackedFloor;
import xean.world.xeno.RootBlock;
import xean.world.xeno.SpreadBlock;
import xean.world.xeno.XenoBlock;
import xean.world.xeno.XenoTurret;

public class Crystallize extends Mod {
    @Override
    public void loadContent() {
        Cryst_ExtendedPathfinder.addonFieldTypes();
        
        Cryst_Blocks.load();
        Cryst_UnitTypes.load();
    }
}