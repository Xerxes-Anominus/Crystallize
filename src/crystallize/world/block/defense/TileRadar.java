package crystallize.world.block.defense;

import arc.Core;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Lines;
import arc.graphics.g2d.TextureRegion;
import arc.math.Mathf;
import arc.util.Time;
import crystallize.world.meta.CrystStat;
import mindustry.content.StatusEffects;
import mindustry.entities.Units;
import mindustry.gen.Building;
import mindustry.gen.Groups;
import mindustry.graphics.Drawf;
import mindustry.graphics.Layer;
import mindustry.graphics.Pal;
import mindustry.type.StatusEffect;
import mindustry.world.Block;
import mindustry.world.meta.BlockGroup;
import mindustry.world.meta.Stat;
import mindustry.world.meta.StatUnit;

public class TileRadar extends Block {
    public TileRadar(String name) {
        super(name);
        update = true;
        solid = true;
        group = BlockGroup.projectors;
    }
    
    public float radius = 80f;
    public float markTime = 60f;
    public float rotateSpeed = 2f;

    public StatusEffect markEffect = StatusEffects.none;
    public float markDuration = 120f;

    public TextureRegion baseRegion, rotateRegion;
    
    @Override
    public void load() {
        super.load();
        baseRegion = Core.atlas.find(name);
        rotateRegion = Core.atlas.find(name + "-rotate");
    }

    @Override
    public TextureRegion[] icons() {
        return new TextureRegion[]{baseRegion, rotateRegion};
    }

    @Override
    public void drawPlace(int x, int y, int rotation, boolean valid) {
        super.drawPlace(x, y, rotation, valid);
        Drawf.dashCircle(x * 8 + offset, y * 8 + offset, radius, Pal.accent);
    }

    @Override
    public void setStats() {
        super.setStats();
        stats.add(Stat.range, radius / 8f, StatUnit.blocks);
        stats.add(CrystStat.markTime, markTime / 60f, StatUnit.seconds);
        stats.add(CrystStat.markDuration, markDuration, StatUnit.seconds);
    }

    public class TileRadarBuild extends Building {
        public float timer = 0f;
        public float spin = 0f;
        public float smoothEfficiency = 0f;

        @Override
        public void updateTile() {
            smoothEfficiency = Mathf.lerpDelta(smoothEfficiency, efficiency, 0.08f);
            spin += smoothEfficiency * rotateSpeed * Time.delta;

            if (efficiency <= 0f) return;

            timer += edelta();
            if (timer >= markTime) {
                timer %= markTime;
                mark();
            }
        }

        public void mark() {
            if (markEffect == StatusEffects.none) return;

            Groups.unit.intersect(x - radius, y - radius, 2f * radius, 2f * radius).each(u -> {
                if(u.team != team && Mathf.dst(u.x, u.y, x, y) < radius) {
                    u.apply(markEffect, markDuration);
                }
            });
        }

        @Override
        public void draw() {
            Draw.rect(baseRegion, x, y);
            Draw.z(Layer.turret);
            Draw.rect(rotateRegion, x, y, spin);
        }

        @Override
        public void drawSelect() {
            Drawf.dashCircle(x, y, radius, team.color);
        }

        @Override
        public boolean shouldConsume() {
            return true;
        }
    }
}