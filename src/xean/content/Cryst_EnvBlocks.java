package xean.content;

import mindustry.world.Block;
import mindustry.world.blocks.environment.Floor;
import mindustry.world.blocks.environment.OverlayFloor;
import mindustry.world.blocks.environment.StaticWall;
import xean.world.block.environment.CrackedFloor;

public class Cryst_EnvBlocks {
    public static Block
    //Ferrite
    ferriteFloor, crackedFerriteFloor, ferriteWall,
    //Envast
    envastFloor, crackedEnvastFloor, envastWall,
    //Warin
    warinFloor, crackedWarinFloor, warinWall,
    //Xeno
    xenoFloor, crackedXenoFloor, xenoWall, xenoSpike,
    
    //Ores
    vanadiumOre, tantalumOre;
    
    public static void load() {
        ferriteFloor = new Floor("ferriteFloor");
        crackedFerriteFloor = new CrackedFloor("crackedFerriteFloor");
        ferriteWall = new StaticWall("ferriteWall");
        
        envastFloor = new Floor("envastFloor");
        crackedEnvastFloor = new CrackedFloor("crackedEnvastFloor");
        envastWall = new StaticWall("envastWall");
        
        warinFloor = new Floor("warinFloor");
        crackedWarinFloor = new CrackedFloor("crackedWarinFloor");
        warinWall = new StaticWall("warinWall");
        
        xenoFloor = new Floor("xenoFloor");
        crackedXenoFloor = new CrackedFloor("crackedXenoFloor");
        xenoWall = new StaticWall("xenoWall");
        xenoSpike = new StaticWall("xenoSpike");
        
        vanadiumOre = new OverlayFloor("vanadiumOre") {{
            itemDrop = Cryst_Items.vanadium;
        }};
        tantalumOre = new OverlayFloor("tantalumOre") {{
            itemDrop = Cryst_Items.tantalum;
        }};
    }
}