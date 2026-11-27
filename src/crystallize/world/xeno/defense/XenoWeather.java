package crystallize.world.xeno.defense;

import arc.math.Mathf;
import arc.util.Time;
import arc.util.io.Reads;
import arc.util.io.Writes;
import crystallize.world.xeno.XenoBlock;
import mindustry.content.Weathers;
import mindustry.type.Weather;

public class XenoWeather extends XenoBlock {
    public XenoWeather(String name) {
        super(name);
    }
    
    public Weather weather = Weathers.rain;
    public float minCallTime = 3600f;
    public float maxCallTime = 7200f;
    public float weatherDuration = 7200f;
    
    public class XenoWeatherBuild extends XenoBuild {
        private float callTime = Mathf.random(minCallTime, maxCallTime);
        private float callTimer;
        
        @Override
        public void updateTile() {
            super.updateTile();
            
            if(weather.isActive()) {
                callTimer = 0f;
                return;
            } else {
                callTimer += Time.delta;
                if(callTimer < callTime) return;
                callTime = Mathf.random(minCallTime, maxCallTime);
                
                weather.create(1f, weatherDuration);
            }
        }
        
        @Override
        public void write(Writes w) {
            super.write(w);
            w.f(callTime);
            w.f(callTimer);
        }
        
        @Override
        public void read(Reads r, byte re) {
            super.read(r, re);
            callTime = r.f();
            callTimer = r.f();
        }
    }
}