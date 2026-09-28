package xean.util;

import mindustry.Vars;
import mindustry.world.Tile;
import mindustry.world.blocks.environment.AirBlock;
import mindustry.world.blocks.environment.StaticProp;

public class Check {
    public static boolean range(float range, float fromx, float fromy, float tox, float toy) {
        float
        length = (tox - fromx) + (toy - fromy),
        length2 = length * length;
        return length <= range;
    }
    
    public static boolean validGrow(float growx, float growy) {
        Tile tile = Vars.world.tileWorld(growx, growy);
        if(tile.floor().isLiquid || tile.floor().isDeep()) return false;
        if(tile.block() instanceof AirBlock || tile.block() instanceof StaticProp) return true;
        return false;
    }
}