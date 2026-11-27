package crystallize.ai;

import mindustry.ai.Pathfinder;
import mindustry.entities.units.AIController;

public class StealthAI extends AIController {
    @Override
    public void updateMovement() {
        pathfind(Pathfinder.fieldCore, true);
        faceMovement();
        faceTarget();
    }
}