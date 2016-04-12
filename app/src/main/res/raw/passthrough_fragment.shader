#extension GL_OES_EGL_image_external : require
precision mediump float;
varying vec4 v_Color;
uniform sampler2D portalTexture;
uniform samplerExternalOES vidTexture;
varying vec2 texCoord;
varying vec2 vidCoord;

void main() {
    /*gl_FragColor = v_Color;*/
    vec4 texPortal = texture2D(portalTexture,texCoord);
    vec4 texVid = texture2D(vidTexture,vidCoord);
    if(texVid.x>0.94 && texVid.y>0.94 && texVid.z>0.94) texVid.a=0.0;
    gl_FragColor= mix(texVid, texPortal, texPortal.a);

    /*gl_FragColor = vec4(texture2D(portalTexture,texCoord).aaa, 1.0);*/
}
