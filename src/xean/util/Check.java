package xean.util;

import mindustry.Vars;
import mindustry.content.Blocks;
import mindustry.world.Tile;
import mindustry.world.blocks.environment.Prop;

public class Check {
    public static boolean range(float range, float fromx, float fromy, float tox, float toy) {
        float
        radius = range * range,
        length = (tox - fromx) + (toy - fromy);
        return length <= radius;
    }
    
    public static boolean validGrow(float growx, float growy) {
        Tile tile = Vars.world.tileWorld(growx, growy);
        if(tile.floor().isLiquid) return false;
        if(tile.floor().isDeep()) return false;
        if(tile.block() != Blocks.air) return false;
        if(tile.block() instanceof Prop) return true;
        return false;
    }
}