package xean.content;

import mindustry.content.Items;
import mindustry.type.Category;
import mindustry.world.Block;
import xean.world.xeno.spread.SpreadBlock;

import static mindustry.type.ItemStack.*;

public class CrystBlocks {
    public static Block
    //Xeno blocks
    xenoCryst;
    
    public static void load() {
        xenoCryst = new SpreadBlock("xenoCryst"){{
            requirements(Category.distribution, with(Items.copper, 1));
        }};
    }
}