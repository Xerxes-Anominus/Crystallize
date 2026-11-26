package crystallize.type;

import mindustry.game.Team;
import mindustry.gen.Unit;
import mindustry.type.StatusEffect;

public class InfectEffect extends StatusEffect {
    public InfectEffect(String name) {
        super(name);
    }
    
    public Team team = Team.crux;
    
    @Override
    public void onRemoved(Unit unit) {
        if(team != null) {
            unit.team = team;
        }
        super.onRemoved(unit);
    }
}