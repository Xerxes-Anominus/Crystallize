package xean.world.xeno;

import arc.audio.Sound;
import arc.math.Mathf;
import arc.util.Time;
import arc.util.io.Reads;
import arc.util.io.Writes;
import mindustry.content.Fx;
import mindustry.content.StatusEffects;
import mindustry.content.UnitTypes;
import mindustry.entities.Damage;
import mindustry.entities.Effect;
import mindustry.gen.Building;
import mindustry.gen.Groups;
import mindustry.gen.Sounds;
import mindustry.type.StatusEffect;
import mindustry.type.UnitType;
import mindustry.world.Block;

public class XenoBlock extends Block {
    public XenoBlock(String name) {
        super(name);
        createRubble = false;
        update = true;
    }
    
    public boolean
    explodable,
    spawnable, deathSpawnable,
    transmittable;
    
    public float
    explodeDamage, explodeRadius,
    shake, shakeDuration;
    public Effect explodeEffect = Fx.none;
    public Sound explodeSound = Sounds.none;
    
    public UnitType unitType = UnitTypes.alpha;
    public int spawnCount = 4;
    public float
    spawnTime = 1500f, spawnRadius = 40f;
    public Effect spawnEffect = Fx.none;
    public Sound spawnSound = Sounds.none;
    
    public StatusEffect infectEffect = StatusEffects.corroded;
    public float
    infectTime = 3000f, infectRadius = 80f;
    
    public class XenoBuild extends Building {
        private float spawnTimer;
        public Building owner = null;
        
        protected void explode() {
            if(!explodable) return;
            if(!dead()) kill();
            
            Damage.damage(this.team, this.x, this.y, explodeRadius, explodeDamage);
            explodeEffect.at(this.x, this.y);
            explodeSound.at(this.x, this.y, 1f, 1f);
        }
        
        protected void spawn() {
            if(!spawnable) return;
            
            spawnTimer += Time.delta;
            if(spawnTimer < spawnTime) return;
            spawnTimer = 0;
            unitType.spawn(this.team, this.x, this.y);
            spawnEffect.at(this.x, this.y);
            spawnSound.at(this.x, this.y, 1f, 1f);
        }
        
        protected void deathSpawn() {
            if(!spawnable) return;
            if(!dead()) kill();;
            
            for(int i = 0; i < spawnCount; i++) {
                float
                r = Mathf.random(spawnRadius),
                a = Mathf.random(360f),
                sx = this.x + r * Mathf.cosDeg(a),
                sy = this.y + r * Mathf.sinDeg(a);
                unitType.spawn(this.team, sx, sy);
            }
        }
        
        protected void infect() {
            if(!transmittable) return;
            
            Groups.unit.each(unit -> {
                float
                dx = unit.x - this.x,
                dy = unit.y - this.y,
                d = dx * dx + dy * dy;
                if(d <= infectRadius && unit.team != this.team) {
                    unit.apply(infectEffect, infectTime);
                }
            });
        }
        
        @Override
        public void updateTile() {
            super.updateTile();
            spawn();
        }
        
        @Override
        public void killed() {
            super.killed();
            explode();
            deathSpawn();
            infect();
        }
        
        @Override
        public void write(Writes write) {
            super.write(write);
            write.f(spawnTimer);
        }
        
        @Override
        public void read(Reads read, byte revision) {
            super.read(read, revision);
            spawnTimer = read.f();
        }
    }
}