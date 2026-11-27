package crystallize.graphics;

import arc.graphics.Color;
import arc.graphics.gl.Shader;
import arc.util.Log;
import arc.util.Time;
import mindustry.Vars;

public class CrystShaders {
    public static HexShieldShader hexShield;
    public static CloakShader cloak;
    public static MoundShader mound;

    public static void init() {
        if(Vars.headless) return;
        try {
            hexShield = new HexShieldShader();
        } catch(Throwable t) {
            Log.err("[Crystallize] Khong nap duoc hexshield shader, dung che do ve mac dinh.", t);
            hexShield = null;
        }
        try {
            cloak = new CloakShader();
            mound = new MoundShader();
        } catch(Throwable t) {
            Log.err("[Crystallize] Khong nap duoc shader don vi (cloak/mound), dung che do mac dinh.", t);
            cloak = null;
            mound = null;
        }
    }
    
    public static class CloakShader extends Shader {
        public float cx, cy;
        public float cloak = 1f;
        public float alpha = 0.3f;
        public final Color color = new Color(0.45f, 0.9f, 1f, 1f);

        public CloakShader() {
            super(Vars.tree.get("shaders/unitfx.vert"), Vars.tree.get("shaders/cloak.frag"));
        }

        @Override
        public void apply() {
            setUniformf("u_time", Time.time / 60f);
            setUniformf("u_center", cx, cy);
            setUniformf("u_cloak", cloak);
            setUniformf("u_alpha", alpha);
            setUniformf("u_color", color.r, color.g, color.b);
        }
    }
    
    public static class MoundShader extends Shader {
        public float cx, cy;
        public float radius = 12f;
        public float power = 1f;
        public float speed = 0f;
        public float angle = 0f; // do
        public final Color color = new Color(0.4f, 0.33f, 0.25f, 1f);

        public MoundShader() {
            super(Vars.tree.get("shaders/unitfx.vert"), Vars.tree.get("shaders/mound.frag"));
        }

        @Override
        public void apply() {
            setUniformf("u_time", Time.time / 60f);
            setUniformf("u_center", cx, cy);
            setUniformf("u_radius", radius);
            setUniformf("u_power", power);
            setUniformf("u_speed", speed);
            setUniformf("u_angle", angle * arc.math.Mathf.degRad);
            setUniformf("u_color", color.r, color.g, color.b);
        }
    }

    public static class HexShieldShader extends Shader {
        public float cx, cy;
        public float radius = 100f;
        public float sides = 6f;
        public float rotation = 0f;
        public float cell = 12f;
        public float hit = 0f;
        public float health = 1f;
        public final Color color = new Color(1f, 1f, 1f, 1f);
        
        public final float[] hits = new float[16];

        public HexShieldShader() {
            super(Vars.tree.get("shaders/hexshield.vert"), Vars.tree.get("shaders/hexshield.frag"));
        }

        @Override
        public void apply() {
            setUniformf("u_time", Time.time / 60f);
            setUniformf("u_center", cx, cy);
            setUniformf("u_radius", radius);
            setUniformf("u_sides", sides);
            setUniformf("u_rot", rotation * arc.math.Mathf.degRad);
            setUniformf("u_cell", cell);
            setUniformf("u_hit", hit);
            setUniformf("u_health", health);
            setUniformf("u_color", color.r, color.g, color.b);
            setUniformf("u_h0", hits[0], hits[1], hits[2], hits[3]);
            setUniformf("u_h1", hits[4], hits[5], hits[6], hits[7]);
            setUniformf("u_h2", hits[8], hits[9], hits[10], hits[11]);
            setUniformf("u_h3", hits[12], hits[13], hits[14], hits[15]);
        }
    }
}
