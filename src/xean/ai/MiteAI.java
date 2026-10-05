package xean.ai;

import mindustry.entities.units.AIController;
import mindustry.gen.Teamc;

public class MiteAI extends AIController{

    @Override
    public void updateMovement(){
        pathfind(Cryst_ExtendedPathfinder.crackedTile, true);
        faceMovement();
    }
}