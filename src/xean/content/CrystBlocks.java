package xean.content;

import mindustry.world.Block;
import xean.world.xeno.spread.SpreadBlock;

public class CrystBlocks {
    public static Block
    //Xeno blocks
    xenoCryst;
    
    public static void load() {
        xenoCryst = new SpreadBlock("xenoCryst");
    }
}