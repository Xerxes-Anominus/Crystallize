package crystallize.world.xeno.defense;

import arc.audio.Sound;
import arc.math.Mathf;
import crystallize.world.xeno.XenoBlock;
import mindustry.content.Fx;
import mindustry.content.StatusEffects;
import mindustry.entities.Damage;
import mindustry.entities.Effect;
import mindustry.entities.TargetPriority;
import mindustry.entities.Units;
import mindustry.gen.Groups;
import mindustry.gen.Sounds;
import mindustry.type.StatusEffect;
import mindustry.world.meta.BlockGroup;

public class XenoMine extends XenoBlock {
    public XenoMine(String name) {
        super(name);
        solid = false;
        isDuct = false;
        underBullets = true;
        priority = TargetPriority.under;
        group = BlockGroup.none;
    }
    
    public float mineDamage = 50f;
    public float mineRange = 40f;
    public Sound mineSound = Sounds.explosion;
    public Effect mineEffect = Fx.explosion;
    public StatusEffect statusEffect = StatusEffects.slow;
    public float statusDuration = 120f;
    public float statusChance = 0.5f;
    
    public class XenoMineBuild extends XenoBuild {
        private boolean active;

        @Override
        public void updateTile() {
            super.updateTile();

            if(!active) {
                float half = size * 4f;
                Groups.unit.intersect(x - half, y - half, block.size, block.size).each(u -> {
                    if(u.isGrounded() && !u.dead) active = true;
                });
            }

            if(active) {
                explode();
            }
        }

        private void explode() {
            Damage.damage(team, x, y, mineRange, mineDamage, false, false, true);
            
            Groups.unit.intersect(x - block.size * 4f, y - block.size* 4f, mineRange * 2f, mineRange * 2f).each(u -> {
                if(u.isGrounded() && u.within(x, y, mineRange + u.hitSize / 2f)
                        && Mathf.chance(statusChance)) {
                    u.apply(statusEffect, statusDuration);
                }
            });

            mineSound.at(x, y);
            mineEffect.at(x, y);
            kill();
        }
    }
}