// Crystallize - XenoProjector hex shield (fragment)
// Khien nang luong chia thanh cac o luc giac: vien phat sang, quet song,
// gon song lan ra tu diem trung dan, o hong nhap nhay khi mau thap.
#define HIGHP

uniform float u_time;    // giay
uniform float u_radius;  // ban kinh ngoai (circumradius) cua khien, da nhan radscl
uniform float u_sides;   // so canh da giac khien (6 = luc giac)
uniform float u_rot;     // goc xoay (radian)
uniform float u_cell;    // ban kinh moi o luc giac (don vi the gioi)
uniform float u_hit;     // 0..1 chop sang toan khien khi bi trung dan
uniform float u_health;  // 0..1 mau khien
uniform vec3  u_color;   // mau khien
uniform vec4  u_h0;      // diem trung dan: xy = vi tri (so voi tam), z = do tuoi 1->0, w = suc manh
uniform vec4  u_h1;
uniform vec4  u_h2;
uniform vec4  u_h3;

varying vec4 v_color;
varying vec2 v_local;

const float PI  = 3.14159265;
const float TAU = 6.28318531;
const vec2  S   = vec2(1.7320508, 1.0);

float hash21(vec2 p){
    p = fract(p * vec2(123.34, 456.21));
    p += dot(p, p + 45.32);
    return fract(p.x * p.y);
}

// Luoi luc giac: tra ve (toa do local trong o, tam o) - don vi: apothem = 0.5
vec4 hexCell(vec2 p){
    vec4 hC = floor(vec4(p, p - vec2(1.0, 0.5)) / S.xyxy) + 0.5;
    vec4 h  = vec4(p - hC.xy * S, p - (hC.zw + 0.5) * S);
    return dot(h.xy, h.xy) < dot(h.zw, h.zw)
        ? vec4(h.xy, hC.xy * S)
        : vec4(h.zw, (hC.zw + 0.5) * S);
}

// khoang cach toi tam o theo chuan luc giac: 0 o tam, 0.5 o canh
float hexNorm(vec2 p){
    p = abs(p);
    return max(dot(p, S * 0.5), p.y);
}

vec2 rot(vec2 p, float a){
    float c = cos(a), s = sin(a);
    return vec2(c * p.x - s * p.y, s * p.x + c * p.y);
}

// khoang cach tu diem toi canh da giac khien (duong = ben trong)
float polyEdge(vec2 p){
    float len = length(p);
    float a   = atan(p.y, p.x) - u_rot;
    float seg = TAU / u_sides;
    float n   = floor(a / seg);
    float d   = cos(a - (n + 0.5) * seg) * len;
    float apothem = u_radius * cos(PI / u_sides);
    return apothem - d;
}

// anh huong cua mot lan trung dan len o co tam cc
float hitFx(vec4 h, vec2 cc, float cellW){
    if(h.z <= 0.001) return 0.0;
    float d     = length(cc - h.xy);
    float t     = 1.0 - h.z;                       // 0 moi trung -> 1 het
    float flash = exp(-d / (cellW * 2.4)) * h.z * h.z;
    float ringR = t * u_radius * 1.25;             // vong song lan ra
    float ring  = exp(-pow((d - ringR) / (cellW * 1.3), 2.0)) * h.z;
    return (flash * 1.2 + ring * 0.9) * h.w;
}

void main(){
    float edgeD = polyEdge(v_local);
    if(edgeD <= -1.0) discard;

    float unit = u_cell * 1.7320508;               // 1 don vi luoi = apothem*2
    vec2  q    = rot(v_local, -u_rot) / unit;      // luoi xoay theo khien

    vec4  hc   = hexCell(q);
    vec2  hl   = hc.xy;
    vec2  cc   = rot(hc.zw * unit, u_rot);         // tam o (khong gian local, world units)
    float rnd  = hash21(hc.zw + 7.13);
    float rnd2 = hash21(hc.zw * 1.7 + 3.1);

    // 0 tai canh o -> 0.5 tai tam o
    float ed = 0.5 - hexNorm(hl);
    float cellW = u_cell * 1.7320508;

    // --- trang thai o: song / hong theo mau ---
    float aliveThr = u_health * 1.25 - 0.1;
    float alive    = 1.0 - step(aliveThr, rnd);
    float flick    = step(0.965, hash21(hc.zw + floor(u_time * 9.0)));
    float lowHP    = 1.0 - u_health;

    // --- nhip dap + song quet ---
    float pulse = 0.5 + 0.5 * sin(u_time * 1.6 + rnd * TAU);
    vec2  dir   = vec2(cos(0.6), sin(0.6));
    float sweepP = dot(cc, dir) / u_radius;
    float sweep  = smoothstep(0.82, 1.0, sin(sweepP * 2.6 - u_time * 1.1));
    float radial = smoothstep(0.88, 1.0, sin(length(cc) / u_radius * 6.0 - u_time * 1.8));

    // --- gon song trung dan ---
    float hits = hitFx(u_h0, cc, cellW) + hitFx(u_h1, cc, cellW)
               + hitFx(u_h2, cc, cellW) + hitFx(u_h3, cc, cellW);
    hits = min(hits, 1.6);

    // --- cac lop hinh hoc cua o ---
    float border = 1.0 - smoothstep(0.0, 0.055, ed);          // vien ngoai
    float inner  = 1.0 - smoothstep(0.0, 0.018, abs(ed - 0.15)); // vien trong mong
    float panel  = smoothstep(0.012, 0.05, ed);               // khe giua cac o
    float core   = 1.0 - smoothstep(0.0, 0.32, hexNorm(hl) * 1.0); // tam o sang nhe

    float e = 0.0;  // nang luong sang cua o
    e += (0.10 + 0.12 * pulse * rnd2) * alive;
    e += sweep * 0.18 * alive;
    e += radial * 0.08 * alive;
    e += hits * 0.55;
    e += flick * (1.0 - alive) * 0.35;
    e += (1.0 - alive) * 0.035;   // o hong van hoi thay mo

    float fill = panel * (e + core * 0.06 * alive) * (0.55 + 0.45 * rnd);
    float line = border * (0.42 + 0.30 * pulse + sweep * 0.35 + hits * 0.9) * mix(0.35, 1.0, alive);
    line += inner * 0.16 * alive * (0.5 + 0.5 * pulse);

    // --- vien ngoai cua khien ---
    float rim  = 1.0 - smoothstep(0.0, 2.2, edgeD);
    float halo = exp(-max(edgeD, 0.0) * 0.07) * 0.22;

    // --- mau ---
    vec3 warn = vec3(1.0, 0.38, 0.30);
    vec3 base = mix(u_color, warn, lowHP * lowHP * 0.7);
    vec3 col  = base * (fill * 1.4 + line * 1.2 + rim * 1.4 + halo);
    col += mix(base, vec3(1.0), 0.55) * (hits * border * 0.55 + u_hit * 0.30 + rim * (0.10 + u_hit * 0.8));
    col += base * fill * hits * 0.6;

    float a = fill * 0.85 + line * 0.9 + rim * 0.95 + halo * 0.5 + u_hit * 0.10;
    a *= smoothstep(-1.0, 0.0, edgeD);       // khu rang cua ben ngoai
    a = clamp(a, 0.0, 1.0);

    gl_FragColor = vec4(min(col, vec3(1.0)), a * v_color.a);
}
