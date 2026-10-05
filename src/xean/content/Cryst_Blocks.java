package xean.content;

import mindustry.content.Items;
import mindustry.type.Category;
import mindustry.type.ItemStack;
import mindustry.world.Block;
import xean.world.xeno.RootBlock;
import xean.world.xeno.SpreadBlock;
import xean.world.xeno.XenoBlock;
import xean.world.xeno.XenoTurret;

public class Cryst_Blocks {
    public static Block
    turretTest, rootTest, spreadTest;
    
    public static void load() {
        turretTest = new XenoTurret("turretTest") {{
            requirements(Category.distribution, ItemStack.with(Items.copper, 1));
        }};
        
        rootTest = new RootBlock("rootTest") {{
            requirements(Category.distribution, ItemStack.with(Items.copper, 1));
            growBlockList.add(turretTest);
        }};
        
        spreadTest = new SpreadBlock("spreadTest") {{
            requirements(Category.distribution, ItemStack.with(Items.copper, 1));
            rootBlock = (XenoBlock)rootTest;
        }};
    }
}