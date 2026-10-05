package xean.world.xeno;

import java.util.ArrayList;
import java.util.List;

import arc.struct.EnumSet;
import arc.util.Time;
import arc.util.io.Reads;
import arc.util.io.Writes;
import mindustry.Vars;
import mindustry.entities.TargetPriority;
import mindustry.game.Difficulty;
import mindustry.gen.Building;
import mindustry.world.Tile;
import mindustry.world.blocks.environment.AirBlock;
import mindustry.world.blocks.environment.StaticProp;
import mindustry.world.meta.BlockFlag;

public class SpreadBlock extends XenoBlock {
    public SpreadBlock(String name) {
        super(name);
        flags = EnumSet.of(BlockFlag.core);
        priority = TargetPriority.core;
    }
    
    public int growCount = 4;
    public XenoBlock rootBlock = null;
    
    public class Pair<A, B> {
        A a;
        B b;
        public Pair(A a, B b) {
            this.a = a;
            this.b = b;
        }
    }
    
    public class SpreadBuild extends XenoBuild {
        private float
        growTimer, growPoint,
        refreshTimer;
        
        public List<Pair<Building, Pair<Float, Float>>> dependList = new ArrayList<>();
        private List<Pair<Float, Float>> pendingList = new ArrayList<>();
        
        public void add(Building build, float x, float y) {
            Tile tile = Vars.world.tileWorld(x, y);
            if(tile == null) return;
            if(!(tile.build instanceof Building) && tile.build != build) return;
            dependList.add(new Pair<Building, Pair<Float, Float>>(build, new Pair<Float, Float>(x, y)));
        }
        
        protected void selfAdd() {
            if(!dependList.isEmpty()) selfAdd(0);
            if(dependList.get(0).a != this) {
                for(int index = dependList.size() - 1; index >= 0; index--) {
                    if(dependList.get(index).a == this) {
                        dependList.remove(index);
                        selfAdd(0);
                        return;
                    }
                }
            }
            selfAdd(0);
        }
        
        protected void selfAdd(int index) {
            dependList.add(index, new Pair<Building, Pair<Float, Float>>(this, new Pair<Float, Float>(this.x, this.y)));
        }
        
        public void remove(Building build) {
            if(build == this) return;
            dependList.removeIf(pair -> pair.a == build);
        }
        
        protected boolean validGrow(float x, float y) {
            Tile tile = Vars.world.tileWorld(x, y);
            if(tile == null) return false;
            if(tile.floor().isLiquid || tile.floor().isDeep()) return false;
            if(!(tile.block() instanceof AirBlock)) return false;
            if(!(tile.block() instanceof StaticProp)) return false;
            return true;
        }
        
        protected void grow() {
            if(dependList.isEmpty() || rootBlock == null) return;
            
            int[]
            dx = {-1, 1, 0, 0},
            dy = {0, 0, -1, 1};
            
            for(int index = 0; index < dependList.size(); index++) {
                if(growPoint < 1) return;
                
                Building build = dependList.get(index).a;
                float offset = (build.block.size + 1) * 4;
                
                for(int i = 0; i < 4; i++) {
                    if(growPoint < 1) return;
                    
                    float
                    gx = build.x + dx[i] * offset,
                    gy = build.y + dy[1] * offset;
                    
                    if(validGrow(gx, gy)) continue;
                    
                    Tile tile = Vars.world.tileWorld(gx, gy);
                    if(tile == null || tile.block() instanceof XenoBlock) continue;
                    
                    tile.setBlock(rootBlock, this.team);
                    growPoint--;
                    if(tile.build instanceof XenoBuild) ((XenoBuild)tile.build).owner = this;
                    add(tile.build, tile.x, tile.y);
                }
            }
        }
        
        protected void growPoint() {
            float multi = 1.0f;
            
            Difficulty diff = Vars.state.rules.planet.campaignRules.difficulty;
            switch(diff) {
                case casual -> {
                    multi = 0.5f;
                    break;
                }
                case easy -> {
                    multi = 1.0f;
                    break;
                }
                case normal -> {
                    multi = 1.5f;
                    break;
                }
                case hard -> {
                    multi = 2.5f;
                    break;
                }
                case eradication -> {
                    multi = 4.0f;
                    break;
                }
            }
            
            growPoint += growCount * multi;
        }
        
        protected void grows() {
            growTimer += Time.delta;
            if(growTimer < 60f) return;
            growTimer = 0f;
            growPoint();
            grow();
        }
        
        protected void restore(float x, float y) {
            Tile tile = Vars.world.tileWorld(x, y);
            if(tile == null) return;
            
            Building build = tile.build;
            if(build == null || build == this || build.team != this.team) return;
            
            for(Pair<Building, Pair<Float, Float>> pair : dependList) {
                if(pair.a == build) return;
            }
            if(build instanceof XenoBuild) ((XenoBuild)build).owner = this;
            add(build, x, y);
        }
        
        protected void restorePending() {
            if(pendingList.isEmpty()) return;
            for(Pair<Float, Float> pair : pendingList) {
                restore(pair.a, pair.b);
            }
            pendingList.clear();
        }
        
        protected void killDepend() {
            if(dependList.isEmpty() || !this.dead()) return;
            
            for(Pair<Building, Pair<Float, Float>> pair : dependList) {
                if(pair.a != null && pair.a != this && !pair.a.dead()) pair.a.kill();
            }
        }
        
        protected void refresh() {
            if(dependList.isEmpty()) return;
            
            refreshTimer += Time.delta;
            if(refreshTimer < 20f) return;
            refreshTimer = 0f;
            
            for(int i = dependList.size() - 1; i >= 0; i--) {
                Pair<Building, Pair<Float, Float>> pair = dependList.get(i);
                
                Building build = pair.a;
                if(build == null || build.dead()) {
                    dependList.remove(i);
                    continue;
                }
                
                Tile tile = Vars.world.tileWorld(pair.b.a ,pair.b.b);
                if(tile == null || tile.build != build) {
                    dependList.remove(i);
                }
            }
        }
        
        @Override
        public void updateTile() {
            super.updateTile();
            restorePending();
            selfAdd();
            if(Vars.net.client()) grows();
            refresh();
        }
        
        @Override
        public void killed() {
            super.killed();
            killDepend();
        }
        
        @Override
        public void write(Writes write) {
            super.write(write);
            write.f(growPoint);
            write.f(growTimer);
            write.f(refreshTimer);
            write.i(dependList.size());
            for(int i = 0; i < dependList.size(); i++) {
                Pair<Float, Float> pair = dependList.get(i).b;
                write.f(pair.a);
                write.f(pair.b);
            }
        }

        @Override
        public void read(Reads read, byte revision) {
            super.read(read, revision);
            growPoint = read.f();
            growTimer = read.f();
            refreshTimer = read.f();
            int size = read.i();
            pendingList.clear();
            dependList.clear();
            for(int i = 0; i < size; i++) {
                float x = read.f();
                float y = read.f();
                pendingList.add(new Pair<Float, Float>(x, y));
            }
        }
    }
}