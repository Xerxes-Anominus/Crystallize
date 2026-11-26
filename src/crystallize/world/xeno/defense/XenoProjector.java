package crystallize.world.xeno.defense;

import arc.func.Cons;
import arc.graphics.Color;
import arc.graphics.g2d.*;
import arc.math.Mathf;
import arc.struct.EnumSet;
import arc.util.Time;
import arc.util.io.Reads;
import arc.util.io.Writes;
import crystallize.world.xeno.XenoBlock;
import mindustry.content.Fx;
import mindustry.gen.Bullet;
import mindustry.gen.Groups;
import mindustry.graphics.Layer;
import mindustry.world.meta.BlockFlag;
import mindustry.world.meta.BlockGroup;

public class XenoProjector extends XenoBlock {
    public XenoProjector(String name) {
        super(name);
        flags = EnumSet.of(BlockFlag.shield);
        group = BlockGroup.projectors;
    }
    
    public boolean consumeHit = true;
    public boolean canRegen = true;
    public float shieldHealth = 750f;
    public float shieldRadius = 101.7f;
    public float shieldRegen = 1.75f;
    public float shieldCooldown = 60f * 5f;
    public int sides = 6;
    public float shieldRotation = 0f;
    public Color shieldColor = null;

    public class XenoProjectorBuild extends XenoBuild {
        public float currentShieldHealth = shieldHealth;
        public float shieldTimer;
        public boolean shieldBroke;
        public float radscl;
        public float hit;

        final Cons<Bullet> shieldConsumer = b -> {
            if(b.team != team && b.type.absorbable
                && Mathf.dst(b.x, b.y, x, y) < realRadius()) {
                b.absorb();
                Fx.absorb.at(b);
                hit = 1f;
                currentShieldHealth -= consumeHit ? b.damage() : 1f;
            }
        };

        public float realRadius() {
            return shieldRadius * radscl;
        }

        @Override
        public void updateTile() {
            super.updateTile();

            radscl = Mathf.lerpDelta(radscl, shieldBroke ? 0f : 1f, 0.06f);
            hit = Mathf.lerpDelta(hit, 0f, 0.1f);

            if(shieldBroke) {
                shieldTimer += Time.delta;
                if(shieldTimer >= shieldCooldown) {
                    shieldTimer = 0f;
                    shieldBroke = false;
                    currentShieldHealth = shieldHealth;
                }
                return;
            }

            if(canRegen && currentShieldHealth < shieldHealth) {
                currentShieldHealth = Math.min(shieldHealth, currentShieldHealth + shieldRegen * Time.delta);
            }

            if(radscl > 0.05f) {
                float r = realRadius();
                Groups.bullet.intersect(x - r, y - r, r * 2f, r * 2f, shieldConsumer);
            }

            if(currentShieldHealth <= 0f) {
                currentShieldHealth = 0f;
                shieldBroke = true;
                shieldTimer = 0f;
                Fx.shieldBreak.at(x, y, shieldRadius, shieldColor != null ? shieldColor : team.color);
            }
        }

        @Override
        public void draw() {
            super.draw();
            if(radscl < 0.01f) return;

            float r = realRadius();
            Color c = shieldColor != null ? shieldColor : team.color;

            Draw.z(Layer.shields);
            Draw.color(c, Color.white, Mathf.clamp(hit));
            
            
            Lines.stroke(1.5f);
            Draw.alpha(0.09f + Mathf.clamp(0.08f * hit));
            Fill.poly(x, y, sides, r, shieldRotation);
            Draw.alpha(1f);
            Lines.poly(x, y, sides, r, shieldRotation);

            Draw.reset();
        }
        
        @Override
        public void write(Writes w) {
            super.write(w);
            w.f(currentShieldHealth);
            w.f(shieldTimer);
            w.bool(shieldBroke);
            w.f(radscl);
            w.f(hit);
        }
        
        @Override
        public void read(Reads r, byte re) {
            super.read(r, re);
            currentShieldHealth = r.f();
            shieldTimer = r.f();
            shieldBroke = r.bool();
            radscl = r.f();
            hit = r.f();
        }
    }
}