package crystallize.world.block.defense;

import arc.graphics.g2d.Draw;
import arc.util.Time;
import mindustry.Vars;
import mindustry.content.Fx;
import mindustry.entities.Effect;
import mindustry.entities.Units;
import mindustry.gen.Building;
import mindustry.gen.Unit;
import mindustry.graphics.Layer;
import mindustry.world.Block;
import mindustry.world.meta.BlockGroup;
import mindustry.world.meta.Stat;
import mindustry.world.meta.StatUnit;
import crystallize.type.unit.BurrowUnitType;

public class LandMine extends Block {
    // Bán kính chạm để kích hoạt (pixel)
    public float triggerRadius = 10f;
    // Bán kính nổ
    public float blastRadius = 40f;
    // Sát thương lên BurrowUnitType đang ở dưới đất
    public float burrowDamage = 600f;
    // Tỉ lệ sát thương lên đơn vị mặt đất khác (0.25 = 25%)
    public float groundDamageFraction = 0.25f;
    // Bán kính kích nổ dây chuyền và độ trễ (tick)
    public float chainRadius = 56f;
    public float chainDelay = 6f;
    public float shake = 4f;
    public Effect blastEffect = Fx.blastExplosion;

    public LandMine(String name) {
        super(name);
        update = true;
        solid = false;          // đơn vị mặt đất đi qua bình thường
        underBullets = true;
        hasShadow = false;
        group = BlockGroup.none;
        destroyEffect = Fx.none;
    }

    @Override
    public void setStats() {
        super.setStats();
        stats.add(Stat.damage, burrowDamage, StatUnit.none);
        stats.add(Stat.range, blastRadius / Vars.tilesize, StatUnit.blocks);
    }

    @Override
    public void drawPlace(int x, int y, int rotation, boolean valid) {
        super.drawPlace(x, y, rotation, valid);
        arc.graphics.g2d.Lines.stroke(1f);
        mindustry.graphics.Drawf.dashCircle(x * Vars.tilesize + offset, y * Vars.tilesize + offset,
            blastRadius, mindustry.graphics.Pal.remove);
    }

    public class LandMineBuild extends Building {
        public boolean triggered = false;

        @Override
        public void updateTile() {
            if(triggered) return;
            // Quét mỗi 3 tick cho nhẹ
            if(((int)Time.time + id) % 3 != 0) return;

            float r = triggerRadius;
            boolean[] hit = {false};
            Units.nearbyEnemies(team, x - r, y - r, r * 2f, r * 2f, u -> {
                if(hit[0] || !u.isValid()) return;
                if(u.type instanceof BurrowUnitType b && b.isBurrowed(u) && u.within(this, r)) {
                    hit[0] = true;
                }
            });

            if(hit[0]) detonate();
        }

        /** Gọi từ mine khác để kích nổ dây chuyền. */
        public void detonate() {
            if(triggered || !isValid()) return;
            triggered = true;

            float r = blastRadius;

            Units.nearbyEnemies(team, x - r, y - r, r * 2f, r * 2f, u -> {
                if(!u.isValid() || !u.within(this, r)) return;

                if(u.type instanceof BurrowUnitType b && b.isBurrowed(u)) {
                    // Trừ thẳng máu để bỏ qua miễn nhiễm của trạng thái burrowed
                    u.health -= burrowDamage;
                    if(u.health <= 0f) u.kill();
                } else if(u.isGrounded()) {
                    u.damage(burrowDamage * groundDamageFraction);
                }
            });

            blastEffect.at(x, y, r / Vars.tilesize);
            Effect.shake(shake, shake * 2f, x, y);

            // Kích nổ các LandMine gần đó (có độ trễ để tránh đệ quy sâu)
            Vars.indexer.eachBlock(team, x, y, chainRadius,
                b -> b instanceof LandMineBuild m && m != this && !m.triggered,
                b -> Time.run(chainDelay, () -> {
                    if(b.isValid()) ((LandMineBuild)b).detonate();
                })
            );

            kill();
        }

        @Override
        public void draw() {
            Draw.z(Layer.blockUnder);
            Draw.rect(block.region, x, y);
            Draw.reset();
        }
    }
}