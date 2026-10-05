package xean.type.unit;

import arc.util.Nullable;
import mindustry.Vars;
import mindustry.gen.CrawlUnit;
import mindustry.gen.Crawlc;
import mindustry.gen.Unit;
import mindustry.world.Block;
import mindustry.world.Tile;
import xean.world.block.environment.CrackedFloor;
import xean.world.xeno.SpreadBlock;

public class MiteUnitType extends XenoUnitType {
    public MiteUnitType(String name) {
        super(name);
        constructor = CrawlUnit::create;
    }
    
    public @Nullable Block growBlock = null;
    
    @Override
    public void update(Unit unit) {
        super.update(unit);
        if(growBlock != null) {
            Tile tile = Vars.world.tileWorld(unit.x, unit.y);
            if(tile == null) return;
            
            if(tile.floor() instanceof CrackedFloor && !(tile.block() instanceof SpreadBlock)) {
                unit.kill();
                tile.setBlock(growBlock, unit.team);
            }
        }
    }
}