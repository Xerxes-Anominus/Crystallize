// Crystallize - go dat noi len khi don vi di chuyen duoi long dat (BurrowUnitType).
// Vung dat phong hinh vom, keo dai theo huong di, co vet nut, song gon va bui.
#define HIGHP

uniform float u_time;
uniform float u_radius;  // ban kinh go dat
uniform float u_power;   // 0..1 muc do hien (chuyen trang thai)
uniform float u_speed;   // 0..1 toc do di chuyen
uniform float u_angle;   // huong di (radian)
uniform vec3  u_color;   // mau mat dat

varying vec4 v_color;
varying vec2 v_local;

const float TAU = 6.28318531;

float hash21(vec2 p){
    p = fract(p * vec2(123.34, 456.21));
    p += dot(p, p + 45.32);
    return fract(p.x * p.y);
}
float noise(vec2 p){
    vec2 i = floor(p), f = fract(p);
    f = f * f * (3.0 - 2.0 * f);
    return mix(mix(hash21(i), hash21(i + vec2(1,0)), f.x),
               mix(hash21(i + vec2(0,1)), hash21(i + vec2(1,1)), f.x), f.y);
}
vec2 rot(vec2 p, float a){
    float c = cos(a), s = sin(a);
    return vec2(c * p.x - s * p.y, s * p.x + c * p.y);
}

void main(){
    vec2 p = rot(v_local, -u_angle);                  // x doc theo huong di
    float stretch = 1.0 + 0.55 * u_speed;
    vec2 q = vec2(p.x / (u_radius * stretch), p.y / u_radius);
    q.x += 0.12 * u_speed;                            // dinh go lech ve phia truoc
    float d = length(q);

    // rung nhe va meo vien de go dat trong "song"
    float wob = noise(v_local * 0.35 + u_time * 1.3);
    d += (wob - 0.5) * 0.14;

    // --- hinh vom ---
    float dome = sqrt(max(0.0, 1.0 - d * d));
    vec2 grad = -q / max(dome, 0.25);                 // do doc cua mat vom
    vec2 gradW = -rot(grad, u_angle);   // phap tuyen = -gradient do cao
    float shade = dot(normalize(vec3(gradW, 1.0)), normalize(vec3(-0.5, 0.65, 0.6)));
    vec3 col = u_color * (0.55 + shade * 0.75);

    // --- vet nut toa tia o vien ---
    float ang = atan(q.y, q.x);
    float crack = pow(abs(sin(ang * 5.0 + noise(vec2(ang * 2.0, 0.0)) * 6.0)), 18.0);
    crack *= smoothstep(0.55, 0.9, d) * (1.0 - smoothstep(0.95, 1.1, d));
    col = mix(col, u_color * 0.18, crack * 0.85);

    // --- ria dat toi quanh chan go ---
    float rimDark = smoothstep(0.62, 1.0, d) * (1.0 - smoothstep(1.0, 1.15, d));
    col *= 1.0 - rimDark * 0.35;

    float alpha = (1.0 - smoothstep(0.9, 1.05, d)) * 0.92;

    // --- song gon lan ra ngoai go ---
    float ring = 0.5 + 0.5 * sin(d * 13.0 - u_time * 3.2);
    float rippleMask = smoothstep(1.0, 1.15, d) * (1.0 - smoothstep(1.15, 1.9, d));
    float rippleA = rippleMask * ring * (0.10 + 0.22 * u_speed);

    // --- vet luong dat phia sau ---
    float behind = smoothstep(0.0, -1.0, p.x / u_radius);
    float furrow = (1.0 - smoothstep(0.0, 0.45, abs(p.y) / u_radius)) * behind
                 * (1.0 - smoothstep(0.0, 2.2, -p.x / u_radius)) * u_speed;
    float fl = 0.5 + 0.5 * sin(p.x * 0.9 + u_time * 1.2);
    float furrowA = furrow * (0.12 + 0.14 * fl);

    // --- hat bui ---
    float spk = step(0.93, hash21(floor(v_local * 1.5) + floor(u_time * 6.0))) * dome;

    vec3 outRgb = col;
    float a = alpha;
    outRgb = mix(outRgb, u_color * 1.2, spk * 0.5);
    // gop song + vet luong (mau toi) vao phan ngoai go
    float outer = max(rippleA, furrowA);
    outRgb = mix(outRgb, u_color * 0.35, step(a, 0.01));
    a = max(a, outer);

    gl_FragColor = vec4(min(outRgb, vec3(1.0)), a * u_power * v_color.a);
}
