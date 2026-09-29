package xean.world.xeno.effect;

import arc.util.Time;
import arc.util.io.Reads;
import arc.util.io.Writes;
import mindustry.gen.Groups;
import xean.world.xeno.XenoBlock;

public class XenoMine extends XenoBlock {
    public XenoMine(String name) {
        super(name);
        solid = false;
        targetable = false;
        explodable = true;
        explodeDamage = 20f;
        explodeRadius = 16f;
        shake = shakeDuration = 0;
    }
    
    public class XenoMineBuild extends XenoBuild {
        private float mineTimer;
        
        protected void mine() {
            if(mineTimer >= 20) {
                Groups.unit.each(unit -> {
                    if(unit.x == this.x && unit.y == this.y) {
                        this.kill();
                    }
                });
            }else{
                mineTimer += Time.delta;
            }
        }
        
        @Override
        public void updateTile() {
            super.updateTile();
            mine();
        }
        
        @Override
        public void write(Writes write) {
            super.write(write);
            write.f(mineTimer);
        }
        
        @Override
        public void read(Reads read, byte revision) {
            super.read(read, revision);
            mineTimer = read.f();
        }
    }
}