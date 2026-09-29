package xean.type;

import arc.util.Nullable;
import mindustry.Vars;
import mindustry.gen.Unit;
import mindustry.world.Block;
import mindustry.world.Tile;
import mindustry.world.blocks.environment.AirBlock;
import mindustry.world.blocks.environment.Floor;
import xean.ai.MiteAI;
import xean.world.xeno.spread.SpreadBlock;

public class MiteUnitType extends XenoUnitType {
    public MiteUnitType(String name) {
        super(name);
        aiController = MiteAI::new;
    }
    
    public @Nullable Floor targetFloor = null;
    public @Nullable Block blockGrow = null;
    
    @Override
    public void update(Unit unit) {
        if(blockGrow == null || targetFloor == null) return;
        
        Tile tile = Vars.world.tileWorld(unit.x, unit.y);
        if(tile.floor() == targetFloor && (!(tile.block() instanceof SpreadBlock) || tile.block() instanceof AirBlock)) {
            unit.kill();
            tile.setBlock(blockGrow, unit.team);
        }
    }
}