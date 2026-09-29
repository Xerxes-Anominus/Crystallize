package xean.content;

import mindustry.content.Blocks;
import mindustry.content.Items;
import mindustry.type.Category;
import mindustry.type.ItemStack;
import mindustry.world.Block;
import xean.world.xeno.root.RootBlock;
import xean.world.xeno.spread.SpreadBlock;

public class XenoBlocks {
    public static Block
    //Roots
    xenoRoot1,
    
    //Cores
    xenoCryst;
    
    public static void load() {
        xenoRoot1 = new RootBlock("xenoRoot1"){{
            requirements(Category.distribution, ItemStack.with(Items.copper, 1));
            growBlockList.add(Blocks.copperWall);
            growBlockList.add(Blocks.duo);
            growBlockList.add(Blocks.malign);
        }};
        
        xenoCryst = new SpreadBlock("xenoCryst"){{
            requirements(Category.distribution, ItemStack.with(Items.copper, 1));
            size = 3;
        }};
    }
}