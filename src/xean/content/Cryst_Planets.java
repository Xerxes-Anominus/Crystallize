package xean.content;

import arc.func.*;
import arc.graphics.*;
import arc.math.*;
import arc.math.geom.*;
import arc.struct.*;
import arc.util.*;
import mindustry.content.*;
import mindustry.game.*;
import mindustry.graphics.*;
import mindustry.graphics.g3d.*;
import mindustry.graphics.g3d.PlanetGrid.*;
import mindustry.maps.generators.PlanetGenerator;
import mindustry.maps.planet.*;
import mindustry.type.*;
import mindustry.world.*;
import mindustry.world.meta.*;

public class Cryst_Planets {
    public static Planet
    star, anthos, caqpode, levos, virelith, zenda;
    
    public static void load() {
        star = new Planet("star", null, 5.5f) {{
            localizedName = "Norus";
            bloom = true;
            alwaysUnlocked = true;
            accessible = false;
            orbitRadius = 10000;
            
            solarSystem = this;
            
            meshLoader = () -> new SunMesh(
            this,
            4,
            5,
            0.3,
            1.7,
            1.2,
            1,
            1.1f,
            Color.valueOf("ff00ff"),
            Color.valueOf("ff3fff"),
            Color.valueOf("ff7fff"),
            Color.valueOf("ffbfff")
            );
        }};
        
        anthos = makeAsteroid("anthos", star, Blocks.air, Blocks.air, -5, 0.4f, 7, 1f, gen -> {
            gen.min = 25;
            gen.max = 35;
            gen.carbonChance = 0.6f;
            gen.iceChance = 0f;
            gen.berylChance = 0.1f;
        });
        anthos.solarSystem = star;
        anthos.orbitRadius = 24;
        
        caqpode = makeAsteroid("caqpode", star, Blocks.air, Blocks.air, -4, 0.55f, 9, 1.3f, gen -> {
            gen.berylChance = 0.8f;
            gen.iceChance = 0f;
            gen.carbonChance = 0.01f;
            gen.min = 20;
            gen.max = 30;
        });
        caqpode.solarSystem = star;
        caqpode.orbitRadius = 36;
        
        levos = makeAsteroid("levos", star, Blocks.stoneWall, Blocks.iceWall, -1, 0.5f, 12, 2f, gen -> {
            gen.berylChance = 0f;
            gen.iceChance = 0.6f;
            gen.carbonChance = 0.1f;
            gen.ferricChance = 0f;
        });
        levos.solarSystem = star;
        levos.orbitRadius = 67;
        
        virelith = new Planet("virelith", star, 0.95f, 3) {{
            generator = new PlanetGenerator() {};
            
            meshLoader = () -> new MultiMesh(
            new NoiseMesh(this, 10, 4, Color.valueOf("bf00ff"), 0.96f, 2, 0.98f, 1.1f, 0.76f),
            new SunMesh(this, 2, 2.8, 1.2, 1.4, 0.869, 1.22, 0.9f,
            Color.valueOf("3f00ff"),
            Color.valueOf("7f00ff")
            ));
            
            cloudMeshLoader = () -> new MultiMesh(
            new HexSkyMesh(this, 3, 1.2f, 0.2f, 6, Color.valueOf("7f00ff").a(0.7f), 3, 0.5f, 1, 0.5f),
            new HexSkyMesh(this, 4, -2f, 0.15f, 5, Color.valueOf("bf00ff").a(0.55f), 4, 0.44f, 1, 0.6f)
            );
            
            atmosphereColor = Color.valueOf("7f00ff").a(0.9f);
            atmosphereRadIn = 0.015f;
            atmosphereRadOut = 0.2f;
            
            localizedName = "Virelith";
            alwaysUnlocked = true;
            accessible = true;
            startSector = 41;
            solarSystem = star;
            orbitRadius = 56;
            iconColor = Color.valueOf("7f00ff");
            
            defaultCore = Blocks.coreAcropolis;
            
            allowLaunchLoadout = true;
            allowLaunchSchematics = false;
            allowLaunchToNumbered = false;
            allowSectorInvasion = false;
            
            campaignRules.clearSectorOnLose = true;
            campaignRules.pauseDisabled = true;
            campaignRules.rtsAI = true;
            campaignRuleDefaults.clearSectorOnLose = true;
            campaignRuleDefaults.pauseDisabled = true;
            campaignRuleDefaults.rtsAI = true;
            showRtsAIRule = false;
            
            ruleSetter = rule -> {
                rule.allowEditRules = false;
                rule.allowEditWorldProcessors = false;
                rule.allowEnvironmentDeconstruct = false;
                rule.derelictRepair = false;
                rule.disableUnitCap = false;
                rule.editor = false;
                
                rule.waveTeam = Team.get(14);
            };
        }};
        
        zenda = new Planet("zenda", virelith, 0.43f) {{
            alwaysUnlocked = false;
            accessible = false;
            orbitRadius = 9;
            solarSystem = star;
            
            meshLoader = () -> new MultiMesh(
            new NoiseMesh(this, 97, 3, Color.valueOf("c8c8c8"), 0.44f, 2, 0.9f, 1.1f, 1.32f),
            new NoiseMesh(this, 45, 4, Color.valueOf("ebebeb"), 0.444f, 1, 0.8f, 0.8f, 1.19f),
            new NoiseMesh(this ,182, 5, Color.valueOf("b2b2b2"), 0.4444f, 4, 0.99f, 1.27f, 0.9f)
            );
        }};
    }
    
    private static Planet makeAsteroid(String name, Planet parent, Block base, Block tint, int seed, float tintThresh, int pieces, float scale, Cons<AsteroidGenerator> cgen){
        return new Planet(name, parent, 0.12f){{
            hasAtmosphere = false;
            updateLighting = false;
            sectors.add(new Sector(this, Ptile.empty));
            camRadius = 0.68f * scale;
            minZoom = 0.6f;
            drawOrbit = false;
            accessible = false;
            clipRadius = 2f;
            defaultEnv = Env.space;
            icon = "commandRally";
            generator = new AsteroidGenerator();
            cgen.get((AsteroidGenerator)generator);

            meshLoader = () -> {
                iconColor = tint.mapColor;
                Color tinted = tint.mapColor.cpy().a(1f - tint.mapColor.a);
                Seq<GenericMesh> meshes = new Seq<>();
                Color color = base.mapColor;
                Rand rand = new Rand(id + 2);

                meshes.add(new NoiseMesh(
                    this, seed, 2, radius, 2, 0.55f, 0.45f, 14f,
                    color, tinted, 3, 0.6f, 0.38f, tintThresh
                ));

                for(int j = 0; j < pieces; j++){
                    meshes.add(new MatMesh(
                        new NoiseMesh(this, seed + j + 1, 1, 0.022f + rand.random(0.039f) * scale, 2, 0.6f, 0.38f, 20f,
                        color, tinted, 3, 0.6f, 0.38f, tintThresh),
                        new Mat3D().setToTranslation(Tmp.v31.setToRandomDirection(rand).setLength(rand.random(0.44f, 1.4f) * scale)))
                    );
                }

                return new MultiMesh(meshes.toArray(GenericMesh.class));
            };
        }};
    }
}