// Crystallize - hieu ung tang hinh cho don vi Xeno.
// Ve lai sprite don vi: pixel tan ra theo nhieu (dissolve) co vien phat sang,
// phan con lai thanh bong ma trong suot co scanline va nhieu hang glitch.
#define HIGHP

uniform sampler2D u_texture;
uniform float u_time;
uniform float u_cloak;   // 0 = hien ro, 1 = tang hinh hoan toan
uniform float u_alpha;   // do dac cua "bong ma" khi da tang hinh
uniform vec3  u_color;   // mau anh sang cloak

varying vec4 v_color;
varying vec4 v_mix_color;
varying vec2 v_texCoords;
varying vec2 v_local;

float hash21(vec2 p){
    p = fract(p * vec2(123.34, 456.21));
    p += dot(p, p + 45.32);
    return fract(p.x * p.y);
}

void main(){
    vec4 c = texture2D(u_texture, v_texCoords);
    if(c.a < 0.01) discard;

    // mau goc, giu nguyen co chop trang khi bi danh (mix color) va tint
    vec3 base = mix(c.rgb * v_color.rgb, v_mix_color.rgb, v_mix_color.a);
    float a0  = c.a * v_color.a;

    vec2  pc = floor(v_local * 2.0);          // luoi pixel the gioi
    float n  = hash21(pc);
    float r  = 1.0 - u_cloak;                 // muc hien ra
    float solid = step(n, r);                 // pixel con "dac"
    float edge  = (1.0 - solid) * (1.0 - smoothstep(0.0, 0.16, n - r)) * step(0.001, r);

    float lum  = dot(base, vec3(0.299, 0.587, 0.114));
    float scan = 0.5 + 0.5 * sin(v_local.y * 2.4 - u_time * 4.0);
    float shim = 0.55 + 0.45 * sin(u_time * 2.0 + (v_local.x + v_local.y) * 0.35);
    float glitchRow = step(0.96, hash21(vec2(floor(v_local.y * 0.8), floor(u_time * 12.0))));
    float sweep = smoothstep(0.92, 1.0, sin(v_local.y * 0.18 + u_time * 1.4)); // dai sang chay ngang

    vec3 ghost = u_color * (0.25 + lum * 1.1);
    float ga = u_alpha * (0.45 + 0.55 * scan) * shim * (0.65 + 0.35 * n);
    ga += glitchRow * u_alpha * 1.6 + sweep * u_alpha * 1.2;

    vec3  rgb = mix(ghost, base, solid);
    float a   = mix(ga, 1.0, solid) * a0;

    rgb += u_color * (edge * 1.4 + (glitchRow + sweep) * 0.4 * (1.0 - solid));
    a = max(a, edge * 0.9 * a0);

    gl_FragColor = vec4(min(rgb, vec3(1.0)), clamp(a, 0.0, 1.0));
}
