package crystallize;

import arc.Events;
import crystallize.ai.MiteAI;
import crystallize.ai.StealthAI;
import crystallize.graphics.CrystShaders;
import crystallize.type.InfectEffect;
import crystallize.type.unit.BurrowUnitType;
import crystallize.type.unit.MiteUnitType;
import crystallize.type.unit.StealthUnitType;
import crystallize.type.unit.XenoUnitType;
import crystallize.world.draw.DrawXenoTurret;
import crystallize.world.block.defense.LandMine;
import crystallize.world.block.defense.TileRadar;
import crystallize.world.block.production.RampDrill;
import crystallize.world.xeno.XenoBlock;
import crystallize.world.xeno.defense.XenoMender;
import crystallize.world.xeno.defense.XenoMine;
import crystallize.world.xeno.defense.XenoProjector;
import crystallize.world.xeno.defense.XenoTurret;
import crystallize.world.xeno.defense.XenoWeather;
import crystallize.world.xeno.grow.XenoCore;
import crystallize.world.xeno.grow.XenoCoreZone;
import crystallize.world.xeno.grow.XenoRoot;
import crystallize.world.xeno.unit.XenoHive;
import mindustry.Vars;
import mindustry.game.EventType.SectorCaptureEvent;
import mindustry.mod.ClassMap;
import mindustry.mod.Mod;

public class CrystallizeMod extends Mod {
    public CrystallizeMod() {
        put("XenoBlock", XenoBlock.class);
        put("XenoCore", XenoCore.class);
        put("XenoRoot", XenoRoot.class);
        put("XenoCoreZone", XenoCoreZone.class);
        put("XenoProjector", XenoProjector.class);
        put("XenoMine", XenoMine.class);
        put("XenoHive", XenoHive.class);
        put("XenoMender", XenoMender.class);
        put("XenoTurret", XenoTurret.class);
        put("XenoWeather", XenoWeather.class);

        put("RampDrill", RampDrill.class);
        put("LandMine", LandMine.class);
        put("TileRadar", TileRadar.class);

        put("XenoUnitType", XenoUnitType.class);
        put("MiteUnitType", MiteUnitType.class);
        put("StealthUnitType", StealthUnitType.class);
        put("BurrowUnitType", BurrowUnitType.class);

        put("MiteAI", MiteAI.class);
        put("StealthAI", StealthAI.class);

        put("DrawXenoTurret", DrawXenoTurret.class);

        put("InfectEffect", InfectEffect.class);

        MiteAI.MitePathfinder.addonFieldTypes();
    }

    @Override
    public void loadContent() {
        CrystPlanets.load();
    }

    @Override
    public void init() {
        CrystShaders.init();

        Events.on(SectorCaptureEvent.class, event -> {
            if(Vars.state.isCampaign() && Vars.state.rules.planet == CrystPlanets.virelith) {
                Vars.state.rules.waveSending = false;
                Vars.state.wave = 1;
                Vars.state.wavetime = Float.MAX_VALUE;
                Vars.state.rules.waves = true;
                Vars.state.rules.dropZoneRadius = 0f;
            }
        });
    }

    private void put(String key, Class<?> value) {
        ClassMap.classes.put(key, value);
    }
}