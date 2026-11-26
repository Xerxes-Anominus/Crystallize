package crystallize.type.unit;

import crystallize.world.xeno.grow.XenoCoreZone;
import crystallize.world.xeno.grow.XenoCore.XenoCoreBuild;
import mindustry.Vars;
import mindustry.content.Blocks;
import mindustry.gen.Unit;
import mindustry.world.Block;
import mindustry.world.Tile;

public class MiteUnitType extends XenoUnitType {
    public MiteUnitType(String name) {
        super(name);
    }
    
    public Block core = Blocks.coreShard;
    
    @Override
    public void update(Unit unit) {
        if(core != null) {
            Tile t = Vars.world.tileWorld(unit.x, unit.y);
            if(t.build instanceof XenoCoreBuild) return;
            if(!(t.floor() instanceof XenoCoreZone)) return;
            t.setBlock(core, unit.team);
            unit.kill();
        }
        super.update(unit);
    }
}