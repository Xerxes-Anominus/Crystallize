package crystallize.world.xeno.unit;

import arc.math.Mathf;
import arc.struct.EnumSet;
import arc.util.Time;
import arc.util.io.Reads;
import arc.util.io.Writes;
import crystallize.world.xeno.XenoBlock;
import mindustry.Vars;
import mindustry.entities.TargetPriority;
import mindustry.graphics.Drawf;
import mindustry.type.UnitType;
import mindustry.world.meta.BlockFlag;
import mindustry.world.meta.BlockGroup;

public class XenoHive extends XenoBlock {
    public XenoHive(String name) {
        super(name);
        group = BlockGroup.units;
        flags = EnumSet.of(BlockFlag.factory);
    }
    
    public UnitType unitType;
    public float productionTime;
    
    public class XenoHiveBuild extends XenoBuild {
        private float productionTimer;
        private float spawnRot = Mathf.random(360f);
        
        @Override
        public void updateTile() {
            super.updateTile();
            
            productionTimer += Time.delta * Vars.state.rules.unitBuildSpeed(team);
            if(productionTimer < productionTime) return;
            productionTimer -= productionTime;
            
            if(unitType == null) return;
            unitType.spawn(team, x, y, spawnRot);
            spawnRot = Mathf.random(360f);
        }
        
        @Override
        public void draw() {
            super.draw();
            
            if(unitType == null || productionTimer < 0f) return;
            Drawf.construct(this, unitType, spawnRot,
            productionTimer / productionTime, 1f,
            Time.time * Vars.state.rules.unitBuildSpeed(team));
        }
        
        @Override
        public void write(Writes w) {
            super.write(w);
            w.f(productionTimer);
            w.f(spawnRot);
        }
        
        @Override
        public void read(Reads r, byte re) {
            super.read(r, re);
            productionTimer = r.f();
            spawnRot = r.f();
        }
    }
}