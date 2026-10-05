package xean.ai;

import arc.struct.IntSeq;
import mindustry.Vars;
import mindustry.ai.Pathfinder;
import mindustry.world.Tile;
import mindustry.world.blocks.environment.AirBlock;
import xean.world.block.environment.CrackedFloor;
import xean.world.xeno.SpreadBlock;

public class Cryst_ExtendedPathfinder extends Pathfinder{
    public static int crackedTile = -1;

    public static class CrackedTileField extends Flowfield{
        public CrackedTileField(){
            refreshRate = 300;
        }

        @Override
        protected void getPositions(IntSeq out){
            for(Tile tile : Vars.world.tiles){
                if(tile.floor() instanceof CrackedFloor && (tile.block() instanceof AirBlock || !(tile.block() instanceof SpreadBlock))){
                    out.add(tile.array());
                }
            }
        }
    }

    public static void addonFieldTypes(){
        crackedTile = Pathfinder.fieldTypes.size;
        Pathfinder.fieldTypes.add(CrackedTileField::new);
    }
}