package crystallize;

import arc.graphics.Color;
import arc.math.Mathf;
import arc.math.Rand;
import arc.math.geom.Mat3D;
import arc.struct.Seq;
import arc.util.Tmp;
import mindustry.content.Blocks;
import mindustry.game.Team;
import mindustry.graphics.g3d.GenericMesh;
import mindustry.graphics.g3d.HexSkyMesh;
import mindustry.graphics.g3d.MatMesh;
import mindustry.graphics.g3d.MultiMesh;
import mindustry.graphics.g3d.NoiseMesh;
import mindustry.graphics.g3d.SunMesh;
import mindustry.maps.generators.PlanetGenerator;
import mindustry.type.Planet;

public class CrystPlanets {
    public static Planet
    norus, redan, folust, arpun, virelith, zenda, benaut, sequo;
    
    private static final float BELT_CENTER = 50f;
    private static final float BELT_ORBIT_OFFSET = 0f;
    private static final float BELT_HALF_WIDTH = 2.5f;

    public static void load() {
        norus = new Planet("norus", null, 8f) {{
            localizedName = "Norus";
            bloom = true;
            alwaysUnlocked = true;
            accessible = false;
            orbitRadius = 10000;
            updateLighting = false;

            solarSystem = this;

            meshLoader = () -> new MultiMesh(
            new SunMesh(
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
            ),
            
            makeRing(this, 300,
            BELT_CENTER + BELT_ORBIT_OFFSET - BELT_HALF_WIDTH,
            BELT_CENTER + BELT_ORBIT_OFFSET + BELT_HALF_WIDTH,
            0.5f, 0.04f, 0.15f, 0f, 9001,
            "6e6660", "8a7f74", "4f4a46", "a09284", "5b3f35")
            );
        }};
        
        redan = new Planet("redan", norus, 0.3f) {{
            localizedName = "Redan";
            alwaysUnlocked = false;
            accessible = false;
            hasAtmosphere = false;
            updateLighting = true;
            tidalLock = true;
            orbitRadius = 20;
            solarSystem = norus;

            meshLoader = () -> new MultiMesh(
            new NoiseMesh(this, 11, 4, Color.valueOf("f2a93b"), 0.3f, 2, 0.8f, 1.2f, 0.9f),
            new NoiseMesh(this, 23, 4, Color.valueOf("d98a22"), 0.303f, 3, 0.7f, 1.4f, 0.8f),
            new NoiseMesh(this, 37, 4, Color.valueOf("ffc15a"), 0.306f, 4, 0.9f, 1.1f, 0.6f)
            );
        }};
        
        folust = new Planet("folust", norus, 0.38f) {{
            localizedName = "Folust";
            alwaysUnlocked = false;
            accessible = false;
            hasAtmosphere = false;
            updateLighting = true;
            orbitRadius = 32;
            solarSystem = norus;

            meshLoader = () -> new MultiMesh(
            new NoiseMesh(this, 51, 4, Color.valueOf("d9501f"), 0.38f, 2, 0.8f, 1.2f, 0.9f),
            new NoiseMesh(this, 63, 4, Color.valueOf("b8381a"), 0.384f, 3, 0.7f, 1.4f, 0.8f),
            new NoiseMesh(this, 79, 4, Color.valueOf("f06a2e"), 0.388f, 4, 0.9f, 1.1f, 0.6f)
            );
        }};
        
        arpun = new Planet("arpun", norus, 0.8f) {{
            localizedName = "Arpun";
            alwaysUnlocked = false;
            accessible = false;
            hasAtmosphere = true;
            updateLighting = true;
            orbitRadius = 44;
            solarSystem = norus;

            meshLoader = () -> new MultiMesh(
            new NoiseMesh(this, 101, 5, Color.valueOf("1a0a0a"), 0.8f, 3, 0.7f, 1.3f, 1.0f),
            new NoiseMesh(this, 113, 5, Color.valueOf("ff2a00"), 0.804f, 4, 0.9f, 1.6f, 0.7f),
            new NoiseMesh(this, 127, 5, Color.valueOf("3a0a05"), 0.808f, 3, 0.8f, 1.2f, 0.85f)
            );

            cloudMeshLoader = () -> new HexSkyMesh(this, 5, 0.6f, 0.1f, 5, Color.valueOf("ff3a10").a(0.35f), 3, 0.5f, 1f, 0.55f);

            atmosphereColor = Color.valueOf("ff3a10").a(0.8f);
            atmosphereRadIn = 0.02f;
            atmosphereRadOut = 0.25f;
        }};
        
        makeMoon("pyrel", "Pyrel", arpun, 0.18f, 2.2f, "5a4a48", "8a3a2a", 140);
        
        virelith = new Planet("virelith", norus, 0.95f, 3) {{
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
            solarSystem = norus;
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

                rule.waveTeam = Team.blue;
            };
        }};
        
        zenda = new Planet("zenda", virelith, 0.43f) {{
            alwaysUnlocked = false;
            accessible = false;
            orbitRadius = 9;
            solarSystem = norus;

            meshLoader = () -> new MultiMesh(
            new NoiseMesh(this, 97, 3, Color.valueOf("c8c8c8"), 0.44f, 2, 0.9f, 1.1f, 1.32f),
            new NoiseMesh(this, 45, 4, Color.valueOf("ebebeb"), 0.444f, 1, 0.8f, 0.8f, 1.19f),
            new NoiseMesh(this, 182, 5, Color.valueOf("b2b2b2"), 0.4444f, 4, 0.99f, 1.27f, 0.9f)
            );
        }};
        
        benaut = new Planet("benaut", norus, 2.2f) {{
            localizedName = "Benaut";
            alwaysUnlocked = false;
            accessible = false;
            hasAtmosphere = true;
            updateLighting = true;
            orbitRadius = 78;
            solarSystem = norus;
            clipRadius = 5.5f;

            meshLoader = () -> new MultiMesh(
            new NoiseMesh(this, 201, 5, Color.valueOf("c9a27a"), 2.2f, 2, 0.9f, 0.6f, 0.5f),
            new NoiseMesh(this, 211, 5, Color.valueOf("a8744a"), 2.21f, 3, 0.8f, 0.9f, 0.4f),
            new NoiseMesh(this, 223, 5, Color.valueOf("e8d4b0"), 2.22f, 2, 0.95f, 0.5f, 0.35f),
            
            makeRing(this, 260, 3.0f, 4.6f, 0.03f, 0.025f, 0.06f, 20f, 7001,
            "e8d4b0", "d9c7a0", "b89a70", "f5e6c8", "8a6a4a")
            );

            cloudMeshLoader = () -> new MultiMesh(
            new HexSkyMesh(this, 7, 0.8f, 0.12f, 6, Color.valueOf("f5e6c8").a(0.45f), 3, 0.5f, 0.9f, 0.5f),
            new HexSkyMesh(this, 8, -1.4f, 0.09f, 6, Color.valueOf("8a5a35").a(0.4f), 3, 0.45f, 1f, 0.55f)
            );

            atmosphereColor = Color.valueOf("d9b88a").a(0.8f);
            atmosphereRadIn = 0.02f;
            atmosphereRadOut = 0.3f;
        }};
        
        makeMoon("oruk", "Oruk", benaut, 0.22f, 6.5f, "9a8f84", "6e645a", 410);
        makeMoon("venmar", "Venmar", benaut, 0.30f, 8.8f, "c8b090", "a08060", 420);
        makeMoon("talis", "Talis", benaut, 0.16f, 11.5f, "d8d8e0", "a8a8b8", 430);
        
        sequo = new Planet("sequo", norus, 1.9f) {{
            localizedName = "Sequo";
            alwaysUnlocked = false;
            accessible = false;
            hasAtmosphere = true;
            updateLighting = true;
            orbitRadius = 108;
            solarSystem = norus;
            
            meshLoader = () -> new NoiseMesh(
            this, 301, 5, radius, 2, 0.9f, 0.6f, 20f,
            Color.valueOf("2a5f86"), Color.valueOf("9a1fe0"), 3, 0.6f, 0.9f, 0.42f
            );
            
            cloudMeshLoader = () -> new HexSkyMesh(this, 9, 0.9f, 0.04f, 6, Color.valueOf("ff3fff").a(0.9f), 3, 0.5f, 1.2f, 0.7f);

            atmosphereColor = Color.valueOf("7f00ff").a(0.8f);
            atmosphereRadIn = 0.02f;
            atmosphereRadOut = 0.28f;
        }};
        
        makeMoon("sela", "Sela", sequo, 0.20f, 4.0f, "b8b8c8", "8888a0", 510);
        makeMoon("miru", "Miru", sequo, 0.28f, 5.6f, "8a6fa8", "5a4478", 520);
        makeMoon("ovan", "Ovan", sequo, 0.14f, 7.2f, "c8a890", "987860", 530);
        makeMoon("quill", "Quill", sequo, 0.24f, 9.0f, "6fa0b0", "48707e", 540);
        makeMoon("davar", "Davar", sequo, 0.18f, 10.8f, "a09890", "706860", 550);
        makeMoon("neris", "Neris", sequo, 0.30f, 12.8f, "d0c0e0", "a890c0", 560);
    }
    
    private static Planet makeMoon(String key, String display, Planet host, float size, float dist,
                                   String baseColor, String accentColor, int noiseSeed) {
        return new Planet(key, host, size) {{
            localizedName = display;
            alwaysUnlocked = false;
            accessible = false;
            hasAtmosphere = false;
            updateLighting = true;
            orbitRadius = dist;
            solarSystem = norus;

            meshLoader = () -> new MultiMesh(
            new NoiseMesh(this, noiseSeed, 3, Color.valueOf(baseColor), size, 2, 0.8f, 1.2f, 0.9f),
            new NoiseMesh(this, noiseSeed + 13, 3, Color.valueOf(accentColor), size * 1.01f, 3, 0.7f, 1.4f, 0.7f)
            );
        }};
    }
    
    private static GenericMesh makeRing(Planet host, int count, float inner, float outer, float thickness,
                                        float minSize, float maxSize, float tilt, int noiseSeed, String... palette) {
        Rand rand = new Rand(noiseSeed);
        Seq<GenericMesh> meshes = new Seq<>();
        float cos = Mathf.cosDeg(tilt), sin = Mathf.sinDeg(tilt);

        for(int i = 0; i < count; i++){
            float ang = rand.random(Mathf.PI2);
            float d = rand.random(inner, outer);
            float x = Mathf.cos(ang) * d;
            float z = Mathf.sin(ang) * d;
            float y = rand.range(thickness);

            float ty = y * cos - z * sin;
            float tz = y * sin + z * cos;

            Color c = Color.valueOf(palette[rand.random(palette.length - 1)]);

            meshes.add(new MatMesh(
            new NoiseMesh(host, noiseSeed + i + 1, 1, c, rand.random(minSize, maxSize), 1, 0.6f, 0.38f, 0.6f),
            new Mat3D().setToTranslation(Tmp.v31.set(x, ty, tz))
            ));
        }

        return new MultiMesh(meshes.toArray(GenericMesh.class));
    }
}