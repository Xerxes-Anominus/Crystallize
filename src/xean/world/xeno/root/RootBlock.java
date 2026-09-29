package xean.world.xeno.root;

import java.util.ArrayList;
import java.util.List;

import arc.math.Mathf;
import arc.util.Time;
import arc.util.io.Reads;
import arc.util.io.Writes;
import mindustry.Vars;
import mindustry.world.Block;
import mindustry.world.Tile;
import xean.util.Check;
import xean.world.xeno.XenoBlock;

public class RootBlock extends XenoBlock {
    public RootBlock(String name) {
        super(name);
        solid = false;
        isDuct = true;
        underBullets = true;
    }
    
    public List<Block> growBlockList = new ArrayList<>();
    
    public float
    minGrowTime = 300f, maxGrowTime = 1500f;
    
    public class RootBuild extends XenoBuild {
        private float
        growTimer, growTime = Mathf.random(minGrowTime, maxGrowTime);
        
        protected void grow() {
            if(growBlockList.isEmpty()) return;
            
            if(growTimer >= growTime) {
                growTimer = 0;
                growTime = Mathf.random(minGrowTime, maxGrowTime);
                
                if(Mathf.chance(0.5)) return;
                
                int index = Mathf.clamp(Mathf.random(growBlockList.size()), 0, growBlockList.size() - 1);
                Block block = growBlockList.get(index);
                int size = block.size;
                
                float
                offset = 4 * (size - 1),
                minx = this.x - offset, miny = this.y - offset,
                maxx = this.x + offset, maxy = this.y + offset;
                
                for(float ix = minx; ix <= maxx; ix++) {
                    for(float iy = miny; iy <= maxy; iy++) {
                        Tile tile = Vars.world.tileWorld(ix, iy);
                        if(tile.floor().isLiquid) return;
                        if(tile.floor().isDeep()) return;
                        if(!(tile.block() instanceof RootBlock)) return;
                    }
                }
                Vars.world.tileWorld(this.x, this.y).setBlock(block, this.team);
            }else{
                growTimer += Time.delta;
            }
        }
        
        @Override
        public void updateTile() {
            super.updateTile();
            grow();
        }
        
        @Override
        public void write(Writes write) {
            super.write(write);
            write.f(growTime);
            write.f(growTimer);
        }
        
        @Override
        public void read(Reads read, byte revision) {
            super.read(read, revision);
            growTime = read.f();
            growTimer = read.f();
        }
    }
}