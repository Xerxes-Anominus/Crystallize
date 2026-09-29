package xean.world.xeno.effect;

import arc.math.Mathf;
import arc.util.Time;
import arc.util.io.Reads;
import arc.util.io.Writes;
import mindustry.game.Team;
import mindustry.gen.Building;
import mindustry.gen.Groups;
import mindustry.gen.Unit;
import mindustry.type.UnitType;
import xean.world.xeno.XenoBlock;

public class XenoBomb extends XenoBlock {
    public XenoBomb(String name) {
        super(name);
        explodable = true;
        explodeDamage = 500f;
        explodeRadius = 56f;
        shake = 2f;
        shakeDuration = 40f;
    }
    
    public float explodeTime = 120f;
    
    public class XenoBombBuild extends XenoBuild {
        private float explodeTimer;
        protected boolean hasTarget = false;
        
        protected void hasTarget() {
            Groups.unit.each(b -> {
                if(b.team != this.team) {
                    float dx = b.x - this.x;
                    float dy = b.y - this.y;
                    float range = dx * dx + dy * dy;
                    if(range < explodeRadius) {
                        hasTarget = true;
                        return;
                    }else{
                        hasTarget = false;
                    }
                }
            });
        }
        
        protected void explosion() {
            if(!hasTarget) {
                explodeTimer -= Time.delta;
                explodeTimer = Math.max(explodeTimer, 0f);
                return;
            }
            
            if(explodeTimer >= explodeTime) {
                kill();
            }else{
                explodeTimer += Time.delta;
            }
        }
        
        @Override
        public void updateTile() {
            super.updateTile();
            hasTarget();
            explosion();
        }
        
        @Override
        public void write(Writes write) {
            super.write(write);
            write.f(explodeTimer);
        }
        
        @Override
        public void read(Reads read, byte revision) {
            super.read(read, revision);
            explodeTimer = read.f();
        }
    }
}