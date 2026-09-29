package xean.content;

import mindustry.content.Blocks;
import mindustry.content.Items;
import mindustry.type.Category;
import mindustry.type.ItemStack;
import mindustry.world.Block;
import xean.world.xeno.effect.XenoBomb;
import xean.world.xeno.effect.XenoMine;
import xean.world.xeno.root.RootBlock;
import xean.world.xeno.spread.SpreadBlock;

public class XenoBlocks {
    public static Block
    //Effects
    xenoBomb, xenoMine,
    
    //Roots
    xenoRoot1,
    
    //Cores
    xenoCryst;
    
    public static void load() {
        xenoBomb = new XenoBomb("xenoBomb") {{
            requirements(Category.distribution, ItemStack.with(Items.copper, 1));
            size = 3;
        }};
        
        xenoMine = new XenoMine("xenoMine") {{
            requirements(Category.distribution, ItemStack.with(Items.copper, 1));
        }};
        
        xenoRoot1 = new RootBlock("xenoRoot1") {{
            requirements(Category.distribution, ItemStack.with(Items.copper, 1));
            growBlockList.add(Blocks.copperWall);
            growBlockList.add(Blocks.duo);
            growBlockList.add(Blocks.malign);
        }};
        
        xenoCryst = new SpreadBlock("xenoCryst") {{
            requirements(Category.distribution, ItemStack.with(Items.copper, 1));
            size = 3;
        }};
    }
}