package crystallize.world.xeno.grow;

import java.util.ArrayList;
import java.util.List;

import arc.math.Mathf;
import arc.struct.EnumSet;
import arc.util.Time;
import arc.util.io.Reads;
import arc.util.io.Writes;
import crystallize.world.xeno.XenoBlock;
import mindustry.Vars;
import mindustry.entities.Effect;
import mindustry.entities.TargetPriority;
import mindustry.gen.Building;
import mindustry.gen.Groups;
import mindustry.type.StatusEffect;
import mindustry.type.UnitType;
import mindustry.world.Block;
import mindustry.world.Tile;
import mindustry.world.meta.BlockFlag;
import mindustry.world.meta.BlockGroup;

public class XenoCore extends XenoBlock {
    public XenoCore(String name) {
        super(name);
        flags = EnumSet.of(BlockFlag.core);
        group = BlockGroup.none;
        priority = TargetPriority.core;
    }

    public UnitType miteType;
    public float miteTime;
    public float miteRadius;
    public int mites;
    public boolean miteOnDeath;

    public Block rootType;
    public float growPower;
    public float maxGrow = 10f;
    
    public StatusEffect infectStatus;
    public float infectTime;
    public float infectRadius;
    public float infectChance;
    public boolean transmittable;
    public Effect infectEffect;

    public class XenoCoreBuild extends XenoBuild {
        private float miteTimer;
        private float growPoint;

        public List<Building> dependList = new ArrayList<>();
        
        private int[] pending;
        private boolean needInit = true;

        public void spawnMite() {
            if(miteType == null || miteTime <= 0f) return;

            miteTimer += Time.delta;
            if(miteTimer < miteTime) return;
            miteTimer -= miteTime;
            miteType.spawn(team, x, y);
        }

        public void spawnMiteOnDeath() {
            if(!miteOnDeath) return;
            if(miteType == null) return;
            if(mites < 1) return;

            for(int i = 0; i < mites; i++) {
                float
                rad = Mathf.random(miteRadius),
                rot = Mathf.random(360f),
                spawnX = x + rad * Mathf.cosDeg(rot),
                spawnY = y + rad * Mathf.sinDeg(rot);

                miteType.spawn(team, spawnX, spawnY);
            }
        }

        public void growPoint() {
            growPoint = Math.min(growPoint + growPower, maxGrow);
        }

        private boolean growable(float growX, float growY) {
            Tile t = Vars.world.tileWorld(growX, growY);
            if(t == null) return false;
            if(t.isDeep()) return false;
            if(t.floor().isLiquid) return false;
            if(t.floor().isDeep()) return false;
            if(t.build != null) return false;
            return true;
        }

        public void growing() {
            int[][] dir = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}};
            
            int n = dependList.size();

            for(int i = 0; i < n; i++) {
                Building b = dependList.get(i);
                if(!b.isValid()) continue;

                float off = 8f * (b.block.size + 1) / 2f;

                for(int j = 0; j < 4; j++) {
                    if(growPoint < 1f) return;

                    float
                    gx = b.x + off * dir[j][0],
                    gy = b.y + off * dir[j][1];

                    if(!growable(gx, gy)) continue;

                    Tile t = Vars.world.tileWorld(gx, gy);
                    t.setBlock(rootType, team);
                    if(t.build == null) continue;

                    dependList.add(t.build);
                    growPoint -= 1f;
                    if(t.build instanceof XenoBuild gb) {
                        gb.owner = owner;
                    }
                }
            }
        }

        public void grow() {
            if(rootType == null) return;
            dependList.removeIf(b -> b != this && !b.isValid());
            if(dependList.isEmpty()) return;
            growPoint();
            growing();
        }

        private void initList() {
            if(!needInit) return;
            needInit = false;

            if(owner == null) owner = this;
            if(!dependList.contains(this)) dependList.add(0, this);
            
            if(pending != null) {
                for(int p : pending) {
                    Tile t = Vars.world.tile(p);
                    if(t == null || t.build == null) continue;
                    if(dependList.contains(t.build)) continue;
                    dependList.add(t.build);
                    if(t.build instanceof XenoBuild b) b.owner = this;
                }
                pending = null;
            }
        }
        
        public void infect() {
            if(infectEffect == null) return;
            if(infectTime < 0f) return;
            if(infectRadius < 0f) return;
            if(!dead) return;
            
            if(infectChance < 0f) return;
            if(!Mathf.chance(infectChance)) return;
            Groups.unit.intersect(x - infectRadius, y - infectRadius, 2f * infectRadius, 2f * infectRadius).each(unit -> {
                if(unit.team != team && Mathf.dst(unit.x, unit.y, x, y) < infectRadius) {
                    unit.apply(infectStatus, infectTime);
                    infectEffect.at(x, y);
                }
            });
        }

        @Override
        public void created() {
            super.created();
            if(owner == null) owner = this;
            if(!dependList.contains(this)) dependList.add(0, this);
        }

        @Override
        public void killed() {
            super.killed();
            spawnMiteOnDeath();
            infect();
        }

        @Override
        public void updateTile() {
            initList();
            super.updateTile();
            spawnMite();
            grow();
        }

        @Override
        public void write(Writes w) {
            super.write(w);
            w.f(miteTimer);
            w.f(growPoint);

            int count = 0;
            for(Building b : dependList) {
                if(b != this && b.isValid()) count++;
            }
            w.i(count);
            for(Building b : dependList) {
                if(b != this && b.isValid()) w.i(b.tile.pos());
            }
        }

        @Override
        public void read(Reads r, byte re) {
            super.read(r, re);
            miteTimer = r.f();
            growPoint = r.f();

            int s = r.i();
            pending = new int[s];
            for(int i = 0; i < s; i++) pending[i] = r.i();

            dependList.clear();
            needInit = true;
        }
    }
}
