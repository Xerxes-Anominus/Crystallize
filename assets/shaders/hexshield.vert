// Crystallize - XenoProjector hex shield (vertex)
// Dung cho SpriteBatch cua Arc: vertex nam o toa do the gioi.
attribute vec4 a_position;
attribute vec4 a_color;
attribute vec2 a_texCoord0;
attribute vec4 a_mix_color;

uniform mat4 u_projTrans;
uniform vec2 u_center; // tam khien (toa do the gioi)

varying vec4 v_color;
varying vec4 v_mix_color;
varying vec2 v_texCoords;
varying vec2 v_local;  // vi tri pixel so voi tam khien (don vi the gioi)

void main(){
    v_color = a_color;
    v_color.a = v_color.a * (255.0/254.0);
    v_mix_color = a_mix_color;
    v_mix_color.a *= (255.0/254.0);
    v_texCoords = a_texCoord0;
    // tru tam ngay o vertex shader de tranh mat do chinh xac khi toa do lon
    v_local = a_position.xy - u_center;
    gl_Position = u_projTrans * a_position;
}
