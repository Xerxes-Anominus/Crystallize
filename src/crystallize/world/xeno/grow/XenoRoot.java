package crystallize.world.xeno.grow;

import arc.math.Mathf;
import arc.struct.Seq;
import arc.util.Time;
import arc.util.io.Reads;
import arc.util.io.Writes;
import crystallize.world.xeno.XenoBlock;
import mindustry.Vars;
import mindustry.game.Team;
import mindustry.gen.Building;
import mindustry.world.Block;
import mindustry.world.Tile;

public class XenoRoot extends XenoBlock {
    public XenoRoot(String name) {
        super(name);
        isDuct = true;
        underBullets = true;
        solid = false;
    }

    private static final Seq<Building> removeTmp = new Seq<>();

    public Seq<GrowEntry> growList = new Seq<>();
    public float
    minGrowTime = 5f * 60f,
    maxGrowTime = 15f * 60f;
    public int growDistance = 3;

    public void addGrow(Block block, float chance) {
        growList.add(new GrowEntry(block, chance));
    }

    public class XenoRootBuild extends XenoBuild {
        public float growTime = Mathf.random(minGrowTime, maxGrowTime);
        public float growTimer;
        
        private Block pick() {
            float total = 0f;
            for(GrowEntry e : growList) {
                if(e.block != null && e.chance > 0f) total += e.chance;
            }
            if(total <= 0f) return null;

            float r = Mathf.random(total);
            Block last = null;
            for(GrowEntry e : growList) {
                if(e.block == null || e.chance <= 0f) continue;
                last = e.block;
                if(r < e.chance) return e.block;
                r -= e.chance;
            }
            return last;
        }
        
        private boolean covered(int lowX, int lowY, int s) {
            for(int x = lowX; x < lowX + s; x++) {
                for(int y = lowY; y < lowY + s; y++) {
                    Tile t = Vars.world.tile(x, y);
                    if(t == null) return false;
                    if(!(t.build instanceof XenoRootBuild rb)) return false;
                    if(rb.owner != owner || rb.team != team) return false;
                }
            }
            return true;
        }
        
        private boolean clear(int lowX, int lowY, int s) {
            for(int dx = -growDistance; dx < s + growDistance; dx++) {
                for(int dy = -growDistance; dy < s + growDistance; dy++) {
                    Tile t = Vars.world.tile(dx + lowX, dy + lowY);
                    if(t == null || t.build == null) continue;
                    if(t.build instanceof XenoBuild && !(t.build instanceof XenoRootBuild)) return false;
                }
            }
            return true;
        }

        private boolean tryGrow() {
            if(owner == null) return false;

            Block b = pick();
            if(b == null) return false;

            int s = b.size;
            int total = s * s;
            int start = Mathf.random(total - 1);
            
            for(int i = 0; i < total; i++) {
                int idx = (start + i) % total;
                int lowX = tile.x - idx % s;
                int lowY = tile.y - idx / s;

                if(!covered(lowX, lowY, s)) continue;
                if(!clear(lowX, lowY, s)) continue;

                Tile target = Vars.world.tile(lowX + (s - 1) / 2, lowY + (s - 1) / 2);
                if(target == null || !b.canPlaceOn(target, team, 0)) continue;

                replace(b, target, lowX, lowY, s);
                return true;
            }
            return false;
        }

        private void replace(Block b, Tile target, int lowX, int lowY, int s) {
            XenoBuild own = owner;
            Team tm = team;

            removeTmp.clear();
            for(int x = lowX; x < lowX + s; x++) {
                for(int y = lowY; y < lowY + s; y++) {
                    Tile t = Vars.world.tile(x, y);
                    if(t != null && t.build != null) removeTmp.addUnique(t.build);
                }
            }
            for(Building rb : removeTmp) rb.tile.setAir();
            removeTmp.clear();

            target.setBlock(b, tm);
            if(target.build == null) return;

            if(target.build instanceof XenoBuild gb) gb.owner = own;
            if(own instanceof XenoCore.XenoCoreBuild core) core.dependList.add(target.build);
        }

        @Override
        public void updateTile() {
            super.updateTile();
            if(growList.isEmpty()) return;

            growTimer += Time.delta;
            if(growTimer < growTime) return;

            growTimer = 0f;
            growTime = Mathf.random(minGrowTime, maxGrowTime);
            tryGrow();
        }

        @Override
        public void write(Writes w) {
            super.write(w);
            w.f(growTime);
            w.f(growTimer);
        }

        @Override
        public void read(Reads r, byte re) {
            super.read(r, re);
            growTime = r.f();
            growTimer = r.f();
        }
    }
    
    public static class GrowEntry {
        public Block block;
        public float chance;

        public GrowEntry() {}

        public GrowEntry(Block block, float chance) {
            this.block = block;
            this.chance = chance;
        }
    }
}
