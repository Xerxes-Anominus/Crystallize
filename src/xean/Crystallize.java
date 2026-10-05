package xean;

import mindustry.mod.ClassMap;
import mindustry.mod.Mod;
import xean.ai.MiteAI;
import xean.content.Cryst_Blocks;
import xean.type.unit.MiteUnitType;
import xean.world.block.environment.CrackedFloor;
import xean.world.xeno.RootBlock;
import xean.world.xeno.SpreadBlock;
import xean.world.xeno.XenoBlock;
import xean.world.xeno.XenoTurret;

public class Crystallize extends Mod {
    public Crystallize() {
        ClassMap.classes.put("CrackedFloor", CrackedFloor.class);
        ClassMap.classes.put("XenoBlock", XenoBlock.class);
        ClassMap.classes.put("RootBlock", RootBlock.class);
        ClassMap.classes.put("SpreadBlock", SpreadBlock.class);
        ClassMap.classes.put("XenoTurret", XenoTurret.class);
        ClassMap.classes.put("MiteUnitType", MiteUnitType.class);
        ClassMap.classes.put("MiteUnitType", MiteUnitType.class);
        ClassMap.classes.put("MiteAI", MiteAI.class);
    }
    
    @Override
    public void loadContent() {
        Cryst_Blocks.load();
    }
}