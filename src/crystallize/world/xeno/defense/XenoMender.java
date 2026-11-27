package crystallize.world.xeno.defense;

import arc.util.Time;
import arc.util.io.Reads;
import arc.util.io.Writes;
import crystallize.world.xeno.XenoBlock;
import mindustry.Vars;
import mindustry.gen.Groups;

public class XenoMender extends XenoBlock {
    public XenoMender(String name) {
        super(name);
    }
    
    public int radius = 5;
    public float healPercent = 0.1f;
    public float healTime = 60f;
    
    public class XenoMenderBuild extends XenoBuild {
        private float healTimer;
        
        public void healing() {
            healTimer += Time.delta;
            if(healTimer < healTime) return;
            healTimer -= healTime;
            
            Groups.build.intersect(x - radius * Vars.tilesize, y - radius * Vars.tilesize, radius * 16f, radius * 16f).each(b -> {
                if(b.team == team) {
                    b.heal(b.maxHealth() * healPercent);
                }
            });
        }
        
        @Override
        public void updateTile() {
            super.updateTile();
            healing();
        }
        
        @Override
        public void write(Writes w) {
            super.write(w);
            w.f(healTimer);
        }
        
        @Override
        public void read(Reads r, byte re) {
            super.read(r, re);
            healTimer = r.f();
        }
    }
}