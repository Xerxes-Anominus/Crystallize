package crystallize.world.xeno;

import arc.util.Time;
import arc.util.io.Reads;
import arc.util.io.Writes;
import mindustry.gen.Building;
import mindustry.world.Block;

public class XenoBlock extends Block {
    public XenoBlock(String name) {
        super(name);
        drawTeamOverlay = false;
        createRubble = false;
        update = true;
        solid = true;
    }
    
    public float lifetime = 15f * 60f;
    
    public class XenoBuild extends Building {
        public XenoBuild owner;
        private float lifetimer;
        
        public void dying() {
            if(owner != null) {
                lifetimer = 0f;
                return;
            }
            
            lifetimer += Time.delta;
            if(lifetimer < lifetime) return;
            kill();
        }
        
        @Override
        public void updateTile() {
            super.updateTile();
            dying();
        }
        
        @Override
        public void write(Writes w) {
            super.write(w);
            w.f(lifetimer);
        }
        
        @Override
        public void read(Reads r, byte re) {
            super.read(r, re);
            lifetimer = r.f();
        }
    }
}