package xean.world.xeno.spread;

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
import mindustry.world.Block;
import mindustry.world.Tile;
import mindustry.world.meta.BlockFlag;
import xean.content.XenoBlocks;
import xean.util.Check;
import xean.util.Pair;
import xean.world.xeno.XenoBlock;
import xean.world.xeno.root.RootBlock;

public class SpreadBlock extends XenoBlock {
    public SpreadBlock(String name) {
        super(name);
        flags = EnumSet.of(BlockFlag.core);
        priority = TargetPriority.core;
    }

    public Block rootBlock = XenoBlocks.xenoRoot1;
    public int growCount = 4;

    public class SpreadBuild extends XenoBuild {
        private float growTimer, growPoint;
        private float refreshTimer;

        public List<Pair<Building, Pair<Float, Float>>> dependList = new ArrayList<>();
        
        private final List<Pair<Float, Float>> pending = new ArrayList<>();

        private void addDepend(Building build, float x, float y) {
            dependList.add(new Pair<Building, Pair<Float, Float>>(build, new Pair<Float, Float>(x, y)));
        }
        
        public void addDepend(Building build) {
            if(build == null) return;
            for(Pair<Building, Pair<Float, Float>> p : dependList) {
                if(p.a == build) return;
            }
            addDepend(build, build.x, build.y);
        }

        public void removeDepend(Building build) {
            if(build == this) return;
            dependList.removeIf(p -> p.a == build);
        }

        protected void addThis() {
            dependList.add(0, new Pair<Building, Pair<Float, Float>>(this, new Pair<Float, Float>(this.x, this.y)));
        }

        protected void selfAdd() {
            if(!dependList.isEmpty() && dependList.get(0).a == this) return;
            dependList.removeIf(p -> p.a == this);
            addThis();
        }

        protected void grow() {
            if(dependList.isEmpty()) return;
            
            final int[] dx = {-1, 1, 0, 0};
            final int[] dy = {0, 0, -1, 1};
            
            for(int index = 0; index < dependList.size(); index++) {
                if(growPoint < 1) return;

                Building src = dependList.get(index).a;
                if(src == null) continue;

                float offset = (src.block.size + 1) * 4;

                for(int i = 0; i < 4; i++) {
                    if(growPoint < 1) return;

                    float gx = src.x + dx[i] * offset;
                    float gy = src.y + dy[i] * offset;

                    if(!Check.validGrow(gx, gy)) continue;

                    Tile tile = Vars.world.tileWorld(gx, gy);
                    if(tile == null) continue;

                    tile.setBlock(rootBlock, this.team);
                    if(tile.build == null) continue;

                    growPoint--;
                    Building grown = tile.build;
                    if(grown instanceof RootBlock.RootBuild) ((RootBlock.RootBuild) grown).owner = this;
                    addDepend(grown, gx, gy);
                }
            }
        }

        protected void growPoint() {
            float multi = 1.5f;

            Difficulty diff = null;
            if(Vars.state.rules.planet != null && Vars.state.rules.planet.campaignRules != null) {
                diff = Vars.state.rules.planet.campaignRules.difficulty;
            }

            if(diff != null) {
                multi = switch(diff) {
                    case casual -> 0.5f;
                    case easy -> 1.0f;
                    case normal -> 1.5f;
                    case hard -> 2.5f;
                    case eradication -> 4.0f;
                    default -> 1.5f;
                };
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
            if(tile == null) return;

            Building build = tile.build;
            if(build == null || build == this || build.team != this.team) return;

            for(Pair<Building, Pair<Float, Float>> p : dependList) {
                if(p.a == build) return;
            }
            if(build instanceof RootBlock.RootBuild) ((RootBlock.RootBuild) build).owner = this;
            addDepend(build, x, y);
        }

        protected void restorePending() {
            if(pending.isEmpty()) return;
            for(Pair<Float, Float> p : pending) {
                restore(p.a, p.b);
            }
            pending.clear();
        }

        protected void killDepend() {
            if(dependList.isEmpty()) return;
            
            List<Pair<Building, Pair<Float, Float>>> copy = new ArrayList<>(dependList);
            dependList.clear();

            for(Pair<Building, Pair<Float, Float>> p : copy) {
                Building build = p.a;
                if(build != null && build != this && !build.dead()) build.kill();
            }
        }

        protected void refresh() {
            if(dependList.isEmpty()) return;

            refreshTimer += Time.delta;
            if(refreshTimer < 30) return;
            refreshTimer = 0;
            
            for(int i = dependList.size() - 1; i >= 0; i--) {
                Pair<Building, Pair<Float, Float>> p = dependList.get(i);
                Building build = p.a;

                if(build == this) continue;

                if(build == null) {
                    dependList.remove(i);
                    continue;
                }

                Tile tile = Vars.world.tileWorld(p.b.a, p.b.b);
                if(tile == null || tile.build != build || build.dead()) {
                    dependList.remove(i);
                }
            }
        }

        @Override
        public void updateTile() {
            super.updateTile();
            restorePending();
            selfAdd();
            if(!Vars.net.client()) grows();
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
            pending.clear();
            dependList.clear();
            for(int i = 0; i < size; i++) {
                float x = read.f();
                float y = read.f();
                pending.add(new Pair<Float, Float>(x, y));
            }
        }
    }
}
