package crystallize.world.xeno.defense;

import arc.Core;
import arc.func.Cons;
import arc.graphics.Color;
import arc.graphics.g2d.*;
import arc.math.Mathf;
import arc.struct.EnumSet;
import arc.util.Time;
import arc.util.io.Reads;
import arc.util.io.Writes;
import crystallize.graphics.CrystShaders;
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
    
    public boolean hexShader = true;
    public float hexSize = 11f;

    public class XenoProjectorBuild extends XenoBuild {
        public float currentShieldHealth = shieldHealth;
        public float shieldTimer;
        public boolean shieldBroke;
        public float radscl;
        public float hit;
        
        final float[] hitData = new float[16];
        int hitIndex;

        final Cons<Bullet> shieldConsumer = b -> {
            if(b.team != team && b.type.absorbable
                && Mathf.dst(b.x, b.y, x, y) < realRadius()) {
                b.absorb();
                Fx.absorb.at(b);
                hit = 1f;
                int i = (hitIndex++ & 3) * 4;
                hitData[i] = b.x - x;
                hitData[i + 1] = b.y - y;
                hitData[i + 2] = 1f;
                hitData[i + 3] = Mathf.clamp(b.damage() / (shieldHealth * 0.04f), 0.45f, 1f);
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
            for(int i = 2; i < 16; i += 4) {
                hitData[i] = Math.max(0f, hitData[i] - Time.delta / 50f);
            }

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

            CrystShaders.HexShieldShader sh = CrystShaders.hexShield;

            if(hexShader && sh != null) {
                final float fr = r;
                final Color fc = c;
                
                Draw.draw(Layer.shields, () -> {
                    sh.cx = x;
                    sh.cy = y;
                    sh.radius = fr;
                    sh.sides = sides;
                    sh.rotation = shieldRotation;
                    sh.cell = hexSize;
                    sh.hit = Mathf.clamp(hit);
                    sh.health = Mathf.clamp(currentShieldHealth / shieldHealth);
                    sh.color.set(fc);
                    System.arraycopy(hitData, 0, sh.hits, 0, 16);

                    Draw.color();
                    Draw.shader(sh);
                    float size = fr * 2f + 4f;
                    Draw.rect(Core.atlas.white(), x, y, size, size);
                    Draw.shader();
                    Draw.reset();
                });
                return;
            }
            
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