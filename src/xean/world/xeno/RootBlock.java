package xean.world.xeno;

import java.util.ArrayList;
import java.util.List;

import arc.math.Mathf;
import arc.util.Time;
import arc.util.io.Reads;
import arc.util.io.Writes;
import mindustry.Vars;
import mindustry.gen.Building;
import mindustry.world.Block;
import mindustry.world.Tile;
import xean.world.xeno.SpreadBlock.SpreadBuild;

public class RootBlock extends XenoBlock {
    public RootBlock(String name) {
        super(name);
        solid = false;
        underBullets = true;
        isDuct = true;
    }
    
    public List<Block> growBlockList = new ArrayList<>();

    public float minGrowTime = 300f, maxGrowTime = 1500f;
    
    public int growDistance = 3;

    public class RootBuild extends XenoBuild {
        private float growTimer, growTime = Mathf.random(minGrowTime, maxGrowTime);
        
        protected boolean canGrow(Block block, int x0, int y0) {
            int size = block.size;
            
            for(int dx = 0; dx < size; dx++) {
                for(int dy = 0; dy < size; dy++) {
                    Tile t = Vars.world.tile(x0 + dx, y0 + dy);
                    if(t == null) return false;
                    if(t.floor().isLiquid) return false;
                    if(t.floor().isDeep()) return false;
                    if(!(t.block() instanceof RootBlock)) return false;
                }
            }
            
            int d = growDistance;
            for(int dx = -d; dx < size + d; dx++) {
                for(int dy = -d; dy < size + d; dy++) {
                    if(dx >= 0 && dx < size && dy >= 0 && dy < size) continue;

                    Tile t = Vars.world.tile(x0 + dx, y0 + dy);
                    if(t == null) continue;

                    Building b = t.build;
                    if(b != null && b.block.solid) return false;
                }
            }
            return true;
        }

        protected void grow() {
            if(growBlockList.isEmpty()) return;
            if(owner == null || !owner.isValid()) return;

            if(growTimer < growTime) {
                growTimer += Time.delta;
                return;
            }

            growTimer = 0;
            growTime = Mathf.random(minGrowTime, maxGrowTime);

            if(Mathf.chance(0.5)) return;

            Block block = growBlockList.get(Mathf.random(growBlockList.size() - 1));
            if(block == null) return;

            int size = block.size;
            int off = -(size - 1) / 2;
            Tile origin = this.tile;
            if(origin == null) return;

            int x0 = origin.x + off, y0 = origin.y + off;

            if(!canGrow(block, x0, y0)) return;

            SpreadBuild o = (SpreadBuild)owner;
            
            for(int dx = 0; dx < size; dx++) {
                for(int dy = 0; dy < size; dy++) {
                    Tile t = Vars.world.tile(x0 + dx, y0 + dy);
                    if(t == null) continue;

                    Building b = t.build;
                    if(b != null) o.remove(b);
                    if(t != origin) t.setAir();
                }
            }

            origin.setBlock(block, this.team);

            if(origin.build != null) {
                o.add(origin.build, origin.x * 8f, origin.y * 8f);
            }
        }

        @Override
        public void updateTile() {
            super.updateTile();
            if(!Vars.net.client()) grow();
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