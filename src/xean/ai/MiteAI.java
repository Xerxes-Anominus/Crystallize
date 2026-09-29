package xean.ai;

import arc.math.geom.Vec2;
import mindustry.Vars;
import mindustry.entities.units.AIController;
import mindustry.gen.Unit;
import mindustry.world.Tile;
import mindustry.world.blocks.environment.AirBlock;
import xean.type.MiteUnitType;
import xean.world.xeno.spread.SpreadBlock;

public class MiteAI extends AIController {
    private int targetX = -1, targetY = -1;

    @Override
    public void updateMovement() {
        if(!(unit.type instanceof MiteUnitType type) || type.targetFloor == null) return;
        
        if(targetX == -1 || timer.get(timerTarget2, 60f)) {
            findTarget(type);
        }

        if(targetX != -1) {
            moveTo(new Vec2(targetX * Vars.tilesize, targetY * Vars.tilesize), 0f);
        }
    }

    void findTarget(MiteUnitType type) {
        float shortest = Float.MAX_VALUE;
        targetX = targetY = -1;

        for(int x = 0; x < Vars.world.width(); x++) {
            for(int y = 0; y < Vars.world.height(); y++) {
                Tile tile = Vars.world.tile(x, y);
                if(tile == null) continue;

                if(tile.floor() == type.targetFloor
                    && !(tile.block() instanceof SpreadBlock)
                    && tile.block() instanceof AirBlock) {
                    float dx = x * Vars.tilesize - unit.x;
                    float dy = y * Vars.tilesize - unit.y;
                    float range = dx * dx + dy * dy;
                    if(range < shortest) {
                        shortest = range;
                        targetX = x;
                        targetY = y;
                    }
                }
            }
        }
    }
}