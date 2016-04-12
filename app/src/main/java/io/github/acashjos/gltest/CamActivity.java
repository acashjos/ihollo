package io.github.acashjos.gltest;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.media.MediaPlayer;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.os.Bundle;
import android.os.Vibrator;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.Window;
import android.view.WindowManager;

import com.google.vrtoolkit.cardboard.CardboardActivity;
import com.google.vrtoolkit.cardboard.CardboardView;
import com.google.vrtoolkit.cardboard.Eye;
import com.google.vrtoolkit.cardboard.HeadTransform;
import com.google.vrtoolkit.cardboard.Viewport;
import com.google.vrtoolkit.cardboard.audio.CardboardAudioEngine;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;

import io.github.acashjos.gltest.Utils.Utils;


// Activity
public class CamActivity extends CardboardActivity implements CardboardView.StereoRenderer, SurfaceTexture.OnFrameAvailableListener {
    private static final String TAG = "Camactivity";
    public static final String CAM_SPLITTER_FLAG = "split";
    //private MainView mView;
//    private PowerManager.WakeLock mWL;


    private int camTex;

    private FloatBuffer camBGVertex;
    private FloatBuffer camTexLCoord;
    private FloatBuffer camTexRCoord;
    private FloatBuffer camTexUnsplitCoord;
    private int camProgram;

    private Camera mCamera;
    private SurfaceTexture mCamSTexture;

    private boolean mUpdateCamST = false;

    private static final float Z_NEAR = 0.1f;
    private static final float Z_FAR = 100.0f;

    private static final float CAMERA_Z = 0.01f;
    private static final float TIME_DELTA = 0.3f;

    private static final float YAW_LIMIT = 0.12f;
    private static final float PITCH_LIMIT = 0.12f;

    private static final int COORDS_PER_VERTEX = 3;

    // We keep the light always position just above the user.
    private static final float[] LIGHT_POS_IN_WORLD_SPACE = new float[] {0.0f, 2.0f, 0.0f, 1.0f};

    private static final float MIN_MODEL_DISTANCE = 3.0f;
    private static final float MAX_MODEL_DISTANCE = 7.0f;

    private static final String SOUND_FILE = "cube_sound.wav";
//    private static final String SOUND_FILE = "vid_audio.mp3";

    private final float[] lightPosInEyeSpace = new float[4];

    private FloatBuffer floorVertices;
    private FloatBuffer floorColors;
    private FloatBuffer floorNormals;

    private FloatBuffer canvasVertices;
    private FloatBuffer canvasColors;
    private FloatBuffer canvasFoundColors;
    private FloatBuffer canvasNormals;
    private FloatBuffer canvasTexCoords;
    private FloatBuffer canvasVidLeftCoords;
    private FloatBuffer canvasVidRightCoords;

    private int cubeProgram;
    private int floorProgram;

    private int cubePositionParam;
    private int cubeNormalParam;
    private int cubeColorParam;
    private int cubeModelParam;
    private int cubeModelViewParam;
    private int cubeModelViewProjectionParam;
    private int cubeLightPosParam;

    private int floorPositionParam;
    private int floorNormalParam;
    private int floorColorParam;
    private int floorModelParam;
    private int floorModelViewParam;
    private int floorModelViewProjectionParam;
    private int floorLightPosParam;

    private float[] modelCube;
    private float[] camera;
    private float[] view;
    private float[] headView;
    private float[] modelViewProjection;
    private float[] modelView;
    private float[] modelFloor;

    private float[] modelPosition;
    private float[] headRotation;

    private float objectDistance = MAX_MODEL_DISTANCE / 2.0f;
    private float floorDepth = 20f;

    private Vibrator vibrator;

    private CardboardAudioEngine cardboardAudioEngine;
    private volatile int soundId = CardboardAudioEngine.INVALID_ID;
    private int portalTex;
    private MediaPlayer mVidPlayer;
    private int videoTex;
    private boolean mUpdateVidST;
    private SurfaceTexture mVidSurfaceTex;
    private boolean split=true;

    /**
     * Converts a raw text file, saved as a resource, into an OpenGL ES shader.
     *
     * @param type The type of shader we will be creating.
     * @param resId The resource ID of the raw text file about to be turned into a shader.
     * @return The shader object handler.
     */
    private int loadGLShader(int type, int resId) {
        String code = readRawTextFile(resId);
        int shader = GLES20.glCreateShader(type);
        GLES20.glShaderSource(shader, code);
        GLES20.glCompileShader(shader);

        // Get the compilation status.
        final int[] compileStatus = new int[1];
        GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compileStatus, 0);

        // If the compilation failed, delete the shader.
        if (compileStatus[0] == 0) {
            Log.e(TAG, "Error compiling shader: " + GLES20.glGetShaderInfoLog(shader));
            GLES20.glDeleteShader(shader);
            shader = 0;
        }

        if (shader == 0) {
            throw new RuntimeException("Error creating shader.");
        }

        return shader;
    }
    /**
     * Checks if we've had an error inside of OpenGL ES, and if so what that error is.
     *
     * @param label Label to report in case of error.
     */
    private static void checkGLError(String label) {
        int error;
        while ((error = GLES20.glGetError()) != GLES20.GL_NO_ERROR) {
            Log.e(TAG, label + ": glError " + error);
            throw new RuntimeException(label + ": glError " + error);
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // full screen & full brightness
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
//        mWL = ((PowerManager)getSystemService ( Context.POWER_SERVICE )).newWakeLock(PowerManager.FULL_WAKE_LOCK, "WakeLock");
//        mWL.acquire();
        //mView = new MainView(this);
        //setContentView ( mView );

        setContentView(R.layout.common_ui);

        split=getIntent().getBooleanExtra(CAM_SPLITTER_FLAG,true);
        CardboardView cardboardView = (CardboardView) findViewById(R.id.cardboard_view);
        cardboardView.setRenderer(this);
        cardboardView.setTransitionViewEnabled(true);
        cardboardView.setOnCardboardBackButtonListener(new Runnable() {
            @Override
            public void run() {
                onBackPressed();
            }
        });
        setCardboardView(cardboardView);


        modelCube = new float[16];
        camera = new float[16];
        view = new float[16];
        modelViewProjection = new float[16];
        modelView = new float[16];
        modelFloor = new float[16];
        // Model first appears directly in front of user.
        modelPosition = new float[] {0.0f, 0.0f, -MAX_MODEL_DISTANCE / 2.0f};
        headRotation = new float[4];
        headView = new float[16];
        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        // Initialize 3D audio engine.
        cardboardAudioEngine =
                new CardboardAudioEngine(this, CardboardAudioEngine.RenderingMode.BINAURAL_HIGH_QUALITY);
    }

    @Override
    protected void onPause() {
//        if ( mWL.isHeld() )
//            mWL.release();
        //mView.onPause();
        cardboardAudioEngine.pause();
        mVidPlayer.pause();
        if (mCamera != null) {
            mCamera.stopPreview();
            mCamera.setPreviewCallback(null);
            mCamera.lock();
            mCamera.release();
            mCamera=null;
        }
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        cardboardAudioEngine.resume();
        if(mVidPlayer!=null)
            mVidPlayer.start();
        //mView.onResume();
//        mWL.acquire();
    }

    public void close()
    {
        mUpdateCamST = false;
        mCamSTexture.release();
        mCamera.stopPreview();
        mCamera = null;
        GLES20.glDeleteTextures(1, new int[]{camTex}, 0);
        GLES20.glDeleteTextures(1, new int[]{videoTex}, 0);
        GLES20.glDeleteTextures(1, new int[]{portalTex}, 0);
    }

    /**
     * Converts a raw text file into a string.
     *
     * @param resId The resource ID of the raw text file about to be turned into a shader.
     * @return The context of the text file, or null in case of error.
     */
    private String readRawTextFile(int resId) {
        InputStream inputStream = getResources().openRawResource(resId);
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }
            reader.close();
            return sb.toString();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
    @Override
    public void onNewFrame(HeadTransform headTransform) {
// Build the Model part of the ModelView matrix.
        //Matrix.rotateM(modelCube, 0, TIME_DELTA, 0.5f, 0.5f, 1.0f);

        // Build the camera matrix and apply it to the ModelView.
        Matrix.setLookAtM(camera, 0, 0.0f, 0.0f, CAMERA_Z, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f);

        headTransform.getHeadView(headView, 0);

        // Update the 3d audio engine with the most recent head rotation.
        headTransform.getQuaternion(headRotation, 0);
        //Log.e(TAG, Arrays.toString(headRotation));
        cardboardAudioEngine.setHeadRotation(
                headRotation[0], headRotation[1], headRotation[2], headRotation[3]);
        // Regular update call to cardboard audio engine.
        cardboardAudioEngine.update();

        checkGLError("onReadyToDraw");
    }

    @Override
    public void onDrawEye(Eye eye) {
        GLES20.glClear( GLES20.GL_COLOR_BUFFER_BIT );


      //  eye.
        synchronized(this) {
            if (mUpdateCamST) {
                mCamSTexture.updateTexImage();
                mUpdateCamST = false;
            }

            if (mUpdateVidST) {
                mVidSurfaceTex.updateTexImage();
                //mVidSurfaceTex.getTransformMatrix(mSTMatrix);
                mUpdateVidST = false;
            }
        }

        GLES20.glEnable(GLES20.GL_DEPTH_TEST);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

        checkGLError("colorParam");

        // Apply the eye transformation to the camera.
        Matrix.multiplyMM(view, 0, eye.getEyeView(), 0, camera, 0);

        // Set the position of the light
        Matrix.multiplyMV(lightPosInEyeSpace, 0, view, 0, LIGHT_POS_IN_WORLD_SPACE, 0);

        // Build the ModelView and ModelViewProjection matrices
        // for calculating cube position and light.
        float[] perspective = eye.getPerspective(Z_NEAR, Z_FAR);
       Matrix.multiplyMM(modelView, 0, view, 0, modelCube, 0);

        modelViewProjection=perspective;
//        Matrix.multiplyMM(modelViewProjection, 0, perspective, 0, modelView, 0);


       /* if(eye.getEyeView()[12]>0)//left
            return;*/

        boolean left=eye.getEyeView()[12]>0;
        GLES20.glUseProgram(camProgram);

        int ph = GLES20.glGetAttribLocation(camProgram, "vPosition");
        int tch = GLES20.glGetAttribLocation (camProgram, "vTexCoord" );
        int th = GLES20.glGetUniformLocation (camProgram, "sTexture" );

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, camTex);
        GLES20.glUniform1i(th, 0);

        GLES20.glVertexAttribPointer(ph, 3, GLES20.GL_FLOAT, false, 4*2, camBGVertex );
        GLES20.glVertexAttribPointer(tch, 2, GLES20.GL_FLOAT, false, 4*2, split?left?camTexLCoord:camTexRCoord:camTexUnsplitCoord);
        GLES20.glEnableVertexAttribArray(ph);
        GLES20.glEnableVertexAttribArray(tch);

        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);


        drawCube(left);

        GLES20.glFlush();

    }

    @Override
    public void onFinishFrame(Viewport viewport) {

    }
    public void drawCube(boolean left) {

        GLES20.glUseProgram(cubeProgram);

        GLES20.glEnable(GLES20.GL_BLEND);
        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);


        int th = GLES20.glGetUniformLocation (cubeProgram, "portalTexture" );
        int vh = GLES20.glGetUniformLocation (cubeProgram, "vidTexture" );
        int tch = GLES20.glGetAttribLocation(cubeProgram, "a_TexCoord");
        int vch = GLES20.glGetAttribLocation(cubeProgram, "a_VidCoord");

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, portalTex);
        GLES20.glUniform1i(th, 0);

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0+2);
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, videoTex);
        GLES20.glUniform1i(vh, 2);

        canvasTexCoords.position(0);
        GLES20.glVertexAttribPointer(tch, 2, GLES20.GL_FLOAT, false, 0, canvasTexCoords);
        GLES20.glEnableVertexAttribArray(tch);
        if(left){
            canvasVidLeftCoords.position(0);
            GLES20.glVertexAttribPointer(vch, 2, GLES20.GL_FLOAT, false, 0, canvasVidLeftCoords);
        }
        else {
            canvasVidRightCoords.position(0);
            GLES20.glVertexAttribPointer(vch, 2, GLES20.GL_FLOAT, false, 0, canvasVidRightCoords);
        }
        GLES20.glEnableVertexAttribArray(vch);

        GLES20.glUniform3fv(cubeLightPosParam, 1, lightPosInEyeSpace, 0);

        // Set the Model in the shader, used to calculate lighting
        GLES20.glUniformMatrix4fv(cubeModelParam, 1, false, modelCube, 0);

        // Set the ModelView in the shader, used to calculate lighting
        GLES20.glUniformMatrix4fv(cubeModelViewParam, 1, false, modelView, 0);

        // Set the position of the cube
        GLES20.glVertexAttribPointer(
                cubePositionParam, COORDS_PER_VERTEX, GLES20.GL_FLOAT, false, 0, canvasVertices);

        // Set the ModelViewProjection matrix in the shader.
        GLES20.glUniformMatrix4fv(cubeModelViewProjectionParam, 1, false, modelViewProjection, 0);

        // Set the normal positions of the cube, again for shading
        GLES20.glVertexAttribPointer(cubeNormalParam, 3, GLES20.GL_FLOAT, false, 0, canvasNormals);
        GLES20.glVertexAttribPointer(cubeColorParam, 4, GLES20.GL_FLOAT, false, 0,
                isLookingAtObject() ? canvasFoundColors : canvasColors);

        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 6);

        GLES20.glDisable(GLES20.GL_BLEND);

        checkGLError("Drawing canvas");
    }
    /**
     * Draw the floor.
     *
     * <p>This feeds in data for the floor into the shader. Note that this doesn't feed in data about
     * position of the light, so if we rewrite our code to draw the floor first, the lighting might
     * look strange.
     */
    public void drawFloor() {
        GLES20.glUseProgram(floorProgram);

        // Set ModelView, MVP, position, normals, and color.
        GLES20.glUniform3fv(floorLightPosParam, 1, lightPosInEyeSpace, 0);
        GLES20.glUniformMatrix4fv(floorModelParam, 1, false, modelFloor, 0);
        GLES20.glUniformMatrix4fv(floorModelViewParam, 1, false, modelView, 0);
        GLES20.glUniformMatrix4fv(floorModelViewProjectionParam, 1, false, modelViewProjection, 0);
        GLES20.glVertexAttribPointer(
                floorPositionParam, COORDS_PER_VERTEX, GLES20.GL_FLOAT, false, 0, floorVertices);
        GLES20.glVertexAttribPointer(floorNormalParam, 3, GLES20.GL_FLOAT, false, 0, floorNormals);
        GLES20.glVertexAttribPointer(floorColorParam, 4, GLES20.GL_FLOAT, false, 0, floorColors);

        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 24);

        checkGLError("drawing floor");
    }
    @Override
    public void onSurfaceChanged(int i, int i1) {

    }

    @Override
    public void onSurfaceCreated(EGLConfig eglConfig) {
        initTex();
        mCamSTexture = new SurfaceTexture ( camTex );
        mCamSTexture.setOnFrameAvailableListener(this);


        Log.e(TAG, "about to open camera");
        mCamera = Camera.open();
        try {
            mCamera.setPreviewTexture(mCamSTexture);
            mCamera.startPreview();
        } catch ( Exception ioe) {
            ioe.printStackTrace();
        }

//        GLES20.glClearColor(1.0f, 1.0f, 0.0f, 1.0f);

        //camProgram = loadShader ( vss, fss );
        float[] vtmp = { 1.0f, -1.0f, -1.0f, -1.0f, 1.0f, 1.0f, -1.0f, 1.0f };
        float[] ttmpl = { 0.5f, 1.0f, 0.0f, 1.0f, 0.5f, 0.0f, 0.0f, 0.0f };
        float[] ttmpr = { 1.0f, 1.0f, 0.5f, 1.0f, 1.0f, 0.0f, 0.5f, 0.0f };
        float[] ttmpUnsplit = { 1.0f, 1.0f, 0.0f, 1.0f, 1.0f, 0.0f, 0.0f, 0.0f };

        //if viewsplitter is available
        //asuming 4:3 aspect ratio
        /*float aspectR=1.0f;//(2*(4f/3))-1;
        float[] vLtmp = { aspectR, -1.0f, -1.0f, -1.0f, aspectR, 1.0f, -1.0f, 1.0f };
        float[] vRtmp = { 1.0f, -1.0f, aspectR, -1.0f, 1.0f, 1.0f, aspectR, 1.0f };*/

        camBGVertex = ByteBuffer.allocateDirect(8*4).order(ByteOrder.nativeOrder()).asFloatBuffer();
        camBGVertex.put(vtmp);
        camBGVertex.position(0);

       /* camLeftBGVertex = ByteBuffer.allocateDirect(8*4).order(ByteOrder.nativeOrder()).asFloatBuffer();
        camLeftBGVertex.put(vLtmp);
        camLeftBGVertex.position(0);

        camRightBGVertex = ByteBuffer.allocateDirect(8*4).order(ByteOrder.nativeOrder()).asFloatBuffer();
        camRightBGVertex.put(vRtmp);
        camRightBGVertex.position(0);*/

        camTexUnsplitCoord = ByteBuffer.allocateDirect(8*4).order(ByteOrder.nativeOrder()).asFloatBuffer();
        camTexUnsplitCoord.put(ttmpUnsplit).position(0);

        camTexLCoord = ByteBuffer.allocateDirect(8*4).order(ByteOrder.nativeOrder()).asFloatBuffer();
        camTexLCoord.put(ttmpl);
        camTexLCoord.position(0);

        camTexRCoord = ByteBuffer.allocateDirect(8*4).order(ByteOrder.nativeOrder()).asFloatBuffer();
        camTexRCoord.put(ttmpr);
        camTexRCoord.position(0);

        Log.i(TAG, "onSurfaceCreated");
        GLES20.glClearColor(0.1f, 0.1f, 0.1f, 0.5f); // Dark background so text shows up well.

        ByteBuffer bbVertices = ByteBuffer.allocateDirect(WorldLayoutData.FLOATING_CANVAS_COORDS.length * 4);
        bbVertices.order(ByteOrder.nativeOrder());
        canvasVertices = bbVertices.asFloatBuffer();
        canvasVertices.put(WorldLayoutData.FLOATING_CANVAS_COORDS);
        canvasVertices.position(0);

        ByteBuffer bbColors = ByteBuffer.allocateDirect(WorldLayoutData.FLOATING_CANVAS_COLORS.length * 4);
        bbColors.order(ByteOrder.nativeOrder());
        canvasColors = bbColors.asFloatBuffer();
        canvasColors.put(WorldLayoutData.FLOATING_CANVAS_COLORS);
        canvasColors.position(0);

        ByteBuffer bbFoundColors =
                ByteBuffer.allocateDirect(WorldLayoutData.FLOATING_CANVAS_FOUND_COLORS.length * 4);
        bbFoundColors.order(ByteOrder.nativeOrder());
        canvasFoundColors = bbFoundColors.asFloatBuffer();
        canvasFoundColors.put(WorldLayoutData.FLOATING_CANVAS_FOUND_COLORS);
        canvasFoundColors.position(0);

        ByteBuffer bbNormals = ByteBuffer.allocateDirect(WorldLayoutData.FLOATING_CANVAS_NORMALS.length * 4);
        bbNormals.order(ByteOrder.nativeOrder());
        canvasNormals = bbNormals.asFloatBuffer();
        canvasNormals.put(WorldLayoutData.FLOATING_CANVAS_NORMALS);
        canvasNormals.position(0);


        canvasTexCoords = ByteBuffer.allocateDirect(WorldLayoutData.FLOATING_CANVAS_TEX_COORDS.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        canvasTexCoords.put(WorldLayoutData.FLOATING_CANVAS_TEX_COORDS).position(0);

        canvasVidLeftCoords = ByteBuffer.allocateDirect(WorldLayoutData.FLOATING_VIDEO_LEFT_TEX_COORDS.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        canvasVidLeftCoords.put(WorldLayoutData.FLOATING_VIDEO_LEFT_TEX_COORDS).position(0);

        canvasVidRightCoords =ByteBuffer.allocateDirect(WorldLayoutData.FLOATING_VIDEO_RIGHT_TEX_COORDS.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        canvasVidRightCoords.put(WorldLayoutData.FLOATING_VIDEO_RIGHT_TEX_COORDS).position(0);

        // make a floor
        ByteBuffer bbFloorVertices = ByteBuffer.allocateDirect(WorldLayoutData.FLOOR_COORDS.length * 4);
        bbFloorVertices.order(ByteOrder.nativeOrder());
        floorVertices = bbFloorVertices.asFloatBuffer();
        floorVertices.put(WorldLayoutData.FLOOR_COORDS);
        floorVertices.position(0);

        ByteBuffer bbFloorNormals = ByteBuffer.allocateDirect(WorldLayoutData.FLOOR_NORMALS.length * 4);
        bbFloorNormals.order(ByteOrder.nativeOrder());
        floorNormals = bbFloorNormals.asFloatBuffer();
        floorNormals.put(WorldLayoutData.FLOOR_NORMALS);
        floorNormals.position(0);

        ByteBuffer bbFloorColors = ByteBuffer.allocateDirect(WorldLayoutData.FLOOR_COLORS.length * 4);
        bbFloorColors.order(ByteOrder.nativeOrder());
        floorColors = bbFloorColors.asFloatBuffer();
        floorColors.put(WorldLayoutData.FLOOR_COLORS);
        floorColors.position(0);

        int vertexShader = loadGLShader(GLES20.GL_VERTEX_SHADER, R.raw.light_vertex);
        int gridShader = loadGLShader(GLES20.GL_FRAGMENT_SHADER, R.raw.grid_fragment);
        int passthroughShader = loadGLShader(GLES20.GL_FRAGMENT_SHADER, R.raw.passthrough_fragment);
        int camShader = loadGLShader(GLES20.GL_FRAGMENT_SHADER, R.raw.cam_fragment);
        int camVertxShader = loadGLShader(GLES20.GL_VERTEX_SHADER, R.raw.cam_vertex);

        cubeProgram = GLES20.glCreateProgram();
        GLES20.glAttachShader(cubeProgram, vertexShader);
        GLES20.glAttachShader(cubeProgram, passthroughShader);
        GLES20.glLinkProgram(cubeProgram);
        GLES20.glUseProgram(cubeProgram);

        checkGLError("Cube program");

        cubePositionParam = GLES20.glGetAttribLocation(cubeProgram, "a_Position");
        cubeNormalParam = GLES20.glGetAttribLocation(cubeProgram, "a_Normal");
        cubeColorParam = GLES20.glGetAttribLocation(cubeProgram, "a_Color");

        cubeModelParam = GLES20.glGetUniformLocation(cubeProgram, "u_Model");
        cubeModelViewParam = GLES20.glGetUniformLocation(cubeProgram, "u_MVMatrix");
        cubeModelViewProjectionParam = GLES20.glGetUniformLocation(cubeProgram, "u_MVP");
        cubeLightPosParam = GLES20.glGetUniformLocation(cubeProgram, "u_LightPos");

        GLES20.glEnableVertexAttribArray(cubePositionParam);
        GLES20.glEnableVertexAttribArray(cubeNormalParam);
        GLES20.glEnableVertexAttribArray(cubeColorParam);

        checkGLError("Cube program params");

        camProgram = GLES20.glCreateProgram();
        GLES20.glAttachShader(camProgram, camVertxShader);
        GLES20.glAttachShader(camProgram, camShader);
        GLES20.glLinkProgram(camProgram);

        checkGLError("Cam program");


        floorProgram = GLES20.glCreateProgram();
        GLES20.glAttachShader(floorProgram, vertexShader);
        GLES20.glAttachShader(floorProgram, gridShader);
        GLES20.glLinkProgram(floorProgram);
        GLES20.glUseProgram(floorProgram);

        checkGLError("Floor program");

        floorModelParam = GLES20.glGetUniformLocation(floorProgram, "u_Model");
        floorModelViewParam = GLES20.glGetUniformLocation(floorProgram, "u_MVMatrix");
        floorModelViewProjectionParam = GLES20.glGetUniformLocation(floorProgram, "u_MVP");
        floorLightPosParam = GLES20.glGetUniformLocation(floorProgram, "u_LightPos");

        floorPositionParam = GLES20.glGetAttribLocation(floorProgram, "a_Position");
        floorNormalParam = GLES20.glGetAttribLocation(floorProgram, "a_Normal");
        floorColorParam = GLES20.glGetAttribLocation(floorProgram, "a_Color");

        GLES20.glEnableVertexAttribArray(floorPositionParam);
        GLES20.glEnableVertexAttribArray(floorNormalParam);
        GLES20.glEnableVertexAttribArray(floorColorParam);

        checkGLError("Floor program params");

        Matrix.setIdentityM(modelFloor, 0);
        Matrix.translateM(modelFloor, 0, 0, -floorDepth, 0); // Floor appears below user.

        // Avoid any delays during start-up due to decoding of sound files.
        new Thread(
                new Runnable() {
                    @Override
                    public void run() {
                        // Start spatial audio playback of SOUND_FILE at the model postion. The returned
                        //soundId handle is stored and allows for repositioning the sound object whenever
                        // the cube position changes.
                        cardboardAudioEngine.preloadSoundFile(SOUND_FILE);
                        soundId = cardboardAudioEngine.createSoundObject(SOUND_FILE);
                        cardboardAudioEngine.setSoundObjectPosition(
                                soundId, modelPosition[0], modelPosition[1], modelPosition[2]);
                        cardboardAudioEngine.playSound(soundId, true /* looped playback */);
                    }
                })
                .start();

        updateModelPosition();

        checkGLError("onSurfaceCreated");
    }

    /**
     * Updates the cube model position.
     */
    private void updateModelPosition() {
        Matrix.setIdentityM(modelCube, 0);
        Matrix.translateM(modelCube, 0, modelPosition[0], modelPosition[1], modelPosition[2]);

        // Update the sound location to match it with the new cube position.
        if (soundId != CardboardAudioEngine.INVALID_ID) {
            cardboardAudioEngine.setSoundObjectPosition(
                    soundId, modelPosition[0], modelPosition[1], modelPosition[2]);
        }
        checkGLError("updateCubePosition");
    }
    private void initTex() {
        int[] textures = new int[1];
        GLES20.glGenTextures(1, textures, 0);
        camTex=textures[0];
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, camTex);
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_NEAREST);


        portalTex= Utils.loadTexture(this, R.drawable.portal);

        AssetFileDescriptor afd = null;
        try {
            afd = getAssets().openFd("test.3gp");
            mVidPlayer = new MediaPlayer();
            mVidPlayer.setDataSource(afd.getFileDescriptor(),afd.getStartOffset(),afd.getLength());
            int[] vtexes = new int[1];
            GLES20.glGenTextures(1, textures, 0);

            videoTex = vtexes[0];
            GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, videoTex);
            checkGLError("glBindTexture videoTex");

            GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MIN_FILTER,
                    GLES20.GL_NEAREST);
            GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MAG_FILTER,
                    GLES20.GL_LINEAR);

            /*
             * Create the SurfaceTexture that will feed this textureID,
             * and pass it to the MediaPlayer
             */
            mVidSurfaceTex = new SurfaceTexture(videoTex);
            mVidSurfaceTex.setOnFrameAvailableListener(this);

            Surface surface = new Surface(mVidSurfaceTex);
            mVidPlayer.setSurface(surface);
            surface.release();

            mVidPlayer.prepare();


            synchronized(this) {
                mUpdateVidST = false;
            }
            mVidPlayer.setLooping(true);
            mVidPlayer.setVolume(0f, 0f);
            mVidPlayer.start();
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Couldn't load video");
        }


    }


    @Override
    public void onRendererShutdown() {
        close();
    }

    @Override
    public synchronized void onFrameAvailable ( SurfaceTexture st ) {
        //Log.e(TAG,"frameAvailable");
        if(st==mCamSTexture)
            mUpdateCamST = true;
        else if(st==mVidSurfaceTex)
            mUpdateVidST=true;

    }

    /**
     * Check if user is looking at object by calculating where the object is in eye-space.
     *
     * @return true if the user is looking at the object.
     */
    private boolean isLookingAtObject() {
        float[] initVec = {0, 0, 0, 1.0f};
        float[] objPositionVec = new float[4];

        // Convert object space to camera space. Use the headView from onNewFrame.
        Matrix.multiplyMM(modelView, 0, headView, 0, modelCube, 0);
        Matrix.multiplyMV(objPositionVec, 0, modelView, 0, initVec, 0);

        float pitch = (float) Math.atan2(objPositionVec[1], -objPositionVec[2]);
        float yaw = (float) Math.atan2(objPositionVec[0], -objPositionVec[2]);

        return Math.abs(pitch) < PITCH_LIMIT && Math.abs(yaw) < YAW_LIMIT;
    }
}



// View
class MainView extends GLSurfaceView {
    MainRenderer mRenderer;

    MainView ( Context context ) {
        super ( context );
        mRenderer = new MainRenderer(this);
        setEGLContextClientVersion ( 2 );
        setRenderer ( mRenderer );
        setRenderMode ( GLSurfaceView.RENDERMODE_WHEN_DIRTY );
    }

    public void surfaceCreated ( SurfaceHolder holder ) {
        super.surfaceCreated ( holder );
    }

    public void surfaceDestroyed ( SurfaceHolder holder ) {
        mRenderer.close();
        super.surfaceDestroyed ( holder );
    }

    public void surfaceChanged ( SurfaceHolder holder, int format, int w, int h ) {
        super.surfaceChanged ( holder, format, w, h );
    }
}


