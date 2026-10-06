package xean.content;

import mindustry.content.Items;
import mindustry.entities.bullet.BasicBulletType;
import mindustry.type.Category;
import mindustry.type.ItemStack;
import mindustry.world.Block;
import xean.world.block.environment.CrackedFloor;
import xean.world.xeno.RootBlock;
import xean.world.xeno.SpreadBlock;
import xean.world.xeno.XenoBlock;
import xean.world.xeno.XenoTurret;

public class Cryst_Blocks {
    public static Block
    crack,
    turretTest, rootTest, spreadTest;
    
    public static void load() {
        crack = new CrackedFloor("crack");
        
        turretTest = new XenoTurret("turretTest") {{
            requirements(Category.distribution, ItemStack.with(Items.copper, 1));
            shootType = new BasicBulletType(2.5f, 9);
            consumePower(1f);
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