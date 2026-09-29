package xean.content;

import mindustry.content.Blocks;
import mindustry.content.StatusEffects;
import xean.type.MiteUnitType;
import xean.type.XenoUnitType;

public class XenoUnitTypes {
    public static XenoUnitType
    mite, test;
    
    public static void load() {
        mite = new MiteUnitType("mite") {{
            targetFloor = Blocks.ice;
            blockGrow = XenoBlocks.xenoCryst;
        }};
        
        test = new XenoUnitType("test") {{
            invisibleEffect = StatusEffects.wet;
        }};
    }
}