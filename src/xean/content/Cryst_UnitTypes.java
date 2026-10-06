package xean.content;

import mindustry.gen.CrawlUnit;
import mindustry.type.UnitType;
import xean.ai.MiteAI;
import xean.type.unit.MiteUnitType;

public class Cryst_UnitTypes {
    public static UnitType
    mite;
    
    public static void load() {
        mite = new MiteUnitType("mite") {{
            growBlock = Cryst_Blocks.spreadTest;
            aiController = MiteAI::new;
            constructor = CrawlUnit::create;
            flying = false;
        }};
    }
}