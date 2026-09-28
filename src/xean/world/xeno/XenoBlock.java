package xean.world.xeno;

import java.util.ArrayList;
import java.util.List;

import arc.audio.Sound;
import arc.math.Mathf;
import arc.util.Nullable;
import arc.util.Time;
import arc.util.io.Reads;
import arc.util.io.Writes;
import mindustry.content.Fx;
import mindustry.entities.Damage;
import mindustry.entities.Effect;
import mindustry.gen.Building;
import mindustry.gen.Groups;
import mindustry.gen.Sounds;
import mindustry.type.StatusEffect;
import mindustry.type.UnitType;
import mindustry.world.Block;
import mindustry.world.Tile;
import xean.util.Check;
import xean.util.Pair;
import xean.world.xeno.spread.SpreadBlock.SpreadBuild;

public class XenoBlock extends Block {
    public XenoBlock(String name) {
        super(name);
        update = true;
    }
    
    public boolean
    explodable, transmittable, spawnable,
    deathSpawnable;
    
    public float
    explodeDamage,
    explodeRadius,
    shake,
    shakeDuration;
    public Sound explodeSound = Sounds.none;
    public Effect explodeEffect = Fx.none;
    
    public float
    infectTime = 15f * 60,
    radius = 40f;
    public @Nullable StatusEffect infectEffect = null;
    
    public float spawnTime = 15f * 60;
    public @Nullable UnitType miteUnit = null;
    
    public int mites = 4;
    public float spawnRadius;
    public @Nullable UnitType miteType = null;
    
    public class XenoBuild extends Building {
        private float spawnTimer;
        public SpreadBuild owner;
        
        protected void explode() {
            if(!explodable) return;
            
            Damage.damage(this.x, this.y, explodeRadius, explodeDamage);
            Effect.shake(shake, shakeDuration, this.x, this.y);
            explodeSound.at(this.x, this.y);
            explodeEffect.at(this.x, this.y);
        }
        
        protected void infect() {
            if(!transmittable) return;
            
            Groups.unit.each(unit -> {
                if(Check.range(radius, this.x, this.y, unit.x, unit.y) && unit.team != this.team) {
                    unit.apply(infectEffect, infectTime);
                }
            });
        }
        
        protected void spawn() {
            if(!spawnable && (miteUnit != null || miteType != null)) return;
            
            UnitType unit;
            if(miteUnit == null) {
                unit = miteType;
            }else{
                unit = miteUnit;
            }
            
            if(spawnTimer >= spawnTime) {
                unit.spawn(this.team, this.x, this.y);
                spawnTimer = 0;
            }else{
                spawnTimer += Time.delta;
            }
        }
        
        protected void deathSpawn() {
            if(!deathSpawnable && (miteUnit != null || miteType != null) && mites > 0) return;
            
            UnitType unit;
            if(miteUnit == null) {
                unit = miteType;
            }else{
                unit = miteUnit;
            }
            
            for(int i = 0; i < mites; i++) {
                float
                sRange = Mathf.random(spawnRadius),
                sAngle = Mathf.random(360f),
                spawnX = this.x + sRange * Mathf.cosDeg(sAngle),
                spawnY = this.y + sRange * Mathf.sinDeg(sAngle);
                
                unit.spawn(this.team, spawnX, spawnY, sAngle);
            }
        }
        
        @Override
        public void killed() {
            super.killed();
            deathSpawn();
            explode();
            infect();
        }
        
        @Override
        public void updateTile() {
            super.updateTile();
            spawn();
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