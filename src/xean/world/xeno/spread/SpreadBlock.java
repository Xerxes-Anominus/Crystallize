package xean.world.xeno.spread;

import java.util.ArrayList;
import java.util.List;

import arc.struct.EnumSet;
import arc.util.Nullable;
import arc.util.Time;
import arc.util.io.Reads;
import arc.util.io.Writes;
import mindustry.Vars;
import mindustry.content.Blocks;
import mindustry.entities.TargetPriority;
import mindustry.game.Difficulty;
import mindustry.gen.Building;
import mindustry.world.Block;
import mindustry.world.Tile;
import mindustry.world.meta.BlockFlag;
import xean.util.Check;
import xean.util.Pair;
import xean.world.xeno.XenoBlock;

public class SpreadBlock extends XenoBlock {
    public SpreadBlock(String name) {
        super(name);
        flags = EnumSet.of(BlockFlag.core);
        priority = TargetPriority.core;
    }
    
    public Block rootBlock = Blocks.copperWall;
    public int growCount = 4;
    
    public class SpreadBuild extends XenoBuild {
        private float
        growTimer, growPoint;
        
        private float refreshTimer;
        
        public List<Pair<Building, Pair<Float, Float>>> dependList = new ArrayList<>();
        
        protected void addThis() {
            dependList.add(0, new Pair<Building, Pair<Float, Float>>(this, new Pair<Float, Float>(this.x, this.y)));
        }
        
        protected void selfAdd() {
            if(dependList.isEmpty()) addThis();
            if(dependList.get(0).a == this) return;
            
            for(int i = 0; i < dependList.size(); i++) {
                if(dependList.get(i).a == this) {
                    dependList.remove(i);
                    addThis();
                    return;
                }
            }
            addThis();
        }
        
        protected void grow() {
            if(dependList.isEmpty()) return;
            
            for(int index = 0; index < dependList.size(); index++) {
                Pair<Building, Pair<Float, Float>> pair = dependList.get(index);
                
                int size = pair.a.block.size;
                float offset = (size + 1) / 2;
                
                for(int i = 0; i < 4; i++) {
                    if(growPoint < 1) return;
                    switch(i) {
                        case 0 -> {
                            float growx = pair.a.x - offset;
                            if(Check.validGrow(growx, pair.a.y)) {
                                growPoint--;
                                Tile tile = Vars.world.tileWorld(growx, pair.a.y);
                                tile.setBlock(rootBlock, this.team);
                                dependList.add(new Pair<Building, Pair<Float, Float>>(tile.build, new Pair<Float, Float>(growx, pair.a.y)));
                                break;
                            }
                        }
                        case 1 -> {
                            float growx = pair.a.x + offset;
                            if(Check.validGrow(growx, pair.a.y)) {
                                growPoint--;
                                Tile tile = Vars.world.tileWorld(growx, pair.a.y);
                                tile.setBlock(rootBlock, this.team);
                                dependList.add(new Pair<Building, Pair<Float, Float>>(tile.build, new Pair<Float, Float>(growx, pair.a.y)));
                                break;
                            }
                        }
                        case 2 -> {
                            float growy = pair.a.y - offset;
                            if(Check.validGrow(pair.a.x, growy)) {
                                growPoint--;
                                Tile tile = Vars.world.tileWorld(pair.a.x, growy);
                                tile.setBlock(rootBlock, this.team);
                                dependList.add(new Pair<Building, Pair<Float, Float>>(tile.build, new Pair<Float, Float>(pair.a.x, growy)));
                                break;
                            }
                        }
                        case 3 -> {
                            float growy = pair.a.y + offset;
                            if(Check.validGrow(pair.a.x, growy)) {
                                growPoint--;
                                Tile tile = Vars.world.tileWorld(pair.a.x, growy);
                                tile.setBlock(rootBlock, this.team);
                                dependList.add(new Pair<Building, Pair<Float, Float>>(tile.build, new Pair<Float, Float>(pair.a.x, growy)));
                                break;
                            }
                        }
                    }
                }
                if(growPoint < 1) return;
            }
        }
        
        protected void growPoint() {
            float multi = 1.5f;
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
            if(growTimer >= 60) {
                growTimer = 0;
                growPoint();
                grow();
            }else{
                growTimer += Time.delta;
            }
        }
        
        protected void restore(float x, float y) {
            Tile tile = Vars.world.tileWorld(x, y);
            Building build = tile.build;
            if(build != null && build.team == this.team) {
                dependList.add(new Pair<Building, Pair<Float, Float>>(build, new Pair<Float, Float>(x, y)));
            }
        }
        
        protected void killDepend() {
            if(dependList.isEmpty()) return;
            
            for(int i = 0; i < dependList.size(); i++) {
                Building build  = dependList.get(i).a;
                if(build != this) build.kill();
            }
        }
        
        protected void refresh() {
            if(dependList.isEmpty()) return;
            
            if(refreshTimer < 30) {
                refreshTimer += Time.delta;
            }
            refreshTimer = 0;
            
            for(int i = 0; i < dependList.size(); i++) {
                Building build = dependList.get(i).a;
                Tile tile = Vars.world.tileWorld(dependList.get(i).b.a, dependList.get(i).b.b);
                if(tile.build == build) {
                    if(build.dead()) {
                        dependList.remove(i);
                        i = 0;
                    }
                }
            }
        }
        
        @Override
        public void updateTile() {
            super.updateTile();
            selfAdd();
            grows();
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
            write.i(dependList.isEmpty() ? 0 : dependList.size());
            for(int i = 0; i < dependList.size(); i++) {
                Pair<Building, Pair<Float, Float>> pair = dependList.get(i);
                write.f(pair.b.a);
                write.f(pair.b.b);
            }
        }
        
        @Override
        public void read(Reads read, byte revision) {
            super.read(read, revision);
            growPoint = read.f();
            growTimer = read.f();
            refreshTimer = read.f();
            int size = read.i();
            for(int i = 0; i < size; i++) {
                float x = read.f();
                float y = read.f();
                restore(x, y);
            }
        }
    }
}