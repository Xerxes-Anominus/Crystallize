package crystallize.world.draw;

import arc.graphics.Blending;
import arc.graphics.g2d.Draw;
import crystallize.world.xeno.defense.XenoTurret;
import mindustry.entities.part.DrawPart;
import mindustry.gen.Building;
import mindustry.graphics.Drawf;
import mindustry.graphics.Layer;
import mindustry.world.draw.DrawTurret;

public class DrawXenoTurret extends DrawTurret {
    public DrawXenoTurret(String basePrefix) {
        super(basePrefix);
    }

    public DrawXenoTurret() {
        super();
    }

    @Override
    public void draw(Building build) {
        XenoTurret turret = (XenoTurret) build.block;
        XenoTurret.XenoTurretBuild tb = (XenoTurret.XenoTurretBuild) build;

        float rx = tb.recoilOffset.x, ry = tb.recoilOffset.y;

        Draw.rect(base, build.x, build.y);
        Draw.color();

        Draw.z(Layer.turret - 0.5f);
        Drawf.shadow(preview, build.x + rx - turret.elevation, build.y + ry - turret.elevation, tb.drawrot());

        Draw.z(Layer.turret);
        
        if (turret.region.found()) {
            Draw.rect(turret.region, build.x + rx, build.y + ry, tb.drawrot());
        }
        
        if (tb.heat > 0.00001f && heat.found()) {
            Draw.color(turret.heatColor, tb.heat);
            Draw.blend(Blending.additive);
            Draw.rect(heat, build.x + rx, build.y + ry, tb.drawrot());
            Draw.blend();
            Draw.color();
        }
        
        if (parts.size > 0) {
            if (outline.found()) {
                Draw.z(Layer.turret - 0.01f);
                Draw.rect(outline, build.x + rx, build.y + ry, tb.drawrot());
                Draw.z(Layer.turret);
            }

            float progress = tb.progress();
            var params = DrawPart.params.set(
                    tb.shootWarmup, 1f - progress, 1f - progress,
                    tb.heat, tb.curRecoil, tb.charge,
                    tb.x + rx, tb.y + ry, tb.turretRotation);

            for (var part : parts) {
                part.draw(params);
            }
        }

        if (top.found()) {
            Draw.rect(top, build.x + rx, build.y + ry, tb.drawrot());
        }
    }
}