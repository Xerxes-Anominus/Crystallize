package crystallize.world.block.production;

import arc.math.Mathf;
import arc.util.io.Reads;
import arc.util.io.Writes;
import mindustry.entities.Effect;
import mindustry.world.blocks.production.BurstDrill;

public class RampDrill extends BurstDrill {
    public RampDrill(String name) {
        super(name);
    }
    
    public float maxBoost = 3f;
    public float boostPower = 0.25f;

    public class RampDrillBuild extends BurstDrillBuild {
        public float boosted = 1f;

        @Override
        public void updateTile(){
            if(dominantItem == null){
                return;
            }

            if(invertTime > 0f) invertTime -= delta() / invertedTime;

            if(timer(timerDump, dumpTime / timeScale)){
                dump(items.has(dominantItem) ? dominantItem : null);
            }

            float drillTime = getDrillTime(dominantItem);

            smoothProgress = Mathf.lerpDelta(smoothProgress, progress / (drillTime - 20f), 0.1f);

            if(items.total() <= itemCapacity - dominantItems && dominantItems > 0 && efficiency > 0){
                warmup = Mathf.approachDelta(warmup, progress / drillTime, 0.01f);
                
                float speed = Mathf.lerp(1f, liquidBoostIntensity, optionalEfficiency) * efficiency * boosted;

                timeDrilled += speedCurve.apply(progress / drillTime) * speed;

                lastDrillSpeed = 1f / drillTime * speed * dominantItems;
                progress += delta() * speed;
            }else{
                warmup = Mathf.approachDelta(warmup, 0f, 0.01f);
                lastDrillSpeed = 0f;
                return;
            }

            if(dominantItems > 0 && progress >= drillTime && items.total() < itemCapacity){
                for(int i = 0; i < dominantItems; i++){
                    offload(dominantItem);
                }
                
                boosted += boostPower;
                if(boosted > maxBoost){
                    boosted = 1f;
                }

                invertTime = 1f;
                progress %= drillTime;

                if(wasVisible){
                    Effect.shake(shake, shake, this);
                    drillSound.at(x, y, 1f + Mathf.range(drillSoundPitchRand), drillSoundVolume);
                    drillEffect.at(x + Mathf.range(drillEffectRnd), y + Mathf.range(drillEffectRnd), dominantItem.color);
                }
            }
        }
        
        @Override
        public void write(Writes write){
            super.write(write);
            write.f(boosted);
        }

        @Override
        public void read(Reads read, byte revision){
            super.read(read, revision);
            boosted = read.f();
        }
    }
}
