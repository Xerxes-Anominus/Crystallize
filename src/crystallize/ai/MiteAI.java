package crystallize.ai;

import arc.struct.IntSeq;
import crystallize.world.xeno.grow.XenoCore;
import crystallize.world.xeno.grow.XenoCoreZone;
import crystallize.world.xeno.grow.XenoCore.XenoCoreBuild;
import mindustry.Vars;
import mindustry.ai.Pathfinder;
import mindustry.entities.units.AIController;
import mindustry.world.Tile;

public class MiteAI extends AIController {
    public static class MitePathfinder extends Pathfinder {
        public static int coreTile = -1;
        
        public static class CoreZoneField extends Flowfield {
            public CoreZoneField() {
                refreshRate = 300;
            }
            
            @Override
            public void getPositions(IntSeq out) {
                for(Tile t : Vars.world.tiles) {
                    if(!(t.floor() instanceof XenoCoreZone)) continue;
                    if(t.block() instanceof XenoCore) continue;
                    out.add(t.array());
                }
            }
        }
        
        public static void addonFieldTypes() {
            if(coreTile != -1) return;
            coreTile = Pathfinder.fieldTypes.size;
            Pathfinder.fieldTypes.add(CoreZoneField::new);
        }
    }
    
    @Override
    public void updateMovement() {
        pathfind(MitePathfinder.coreTile, true);
        faceMovement();
    }
}