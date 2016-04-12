/*
 * Copyright 2014 Google Inc. All Rights Reserved.

 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.github.acashjos.gltest;

/**
 * Contains vertex, normal and color data.
 */
public final class WorldLayoutData {

  public static final float[] FLOATING_CANVAS_COORDS = new float[] {
      // Front face
      -1.0f, 1.0f, -5.0f,
      -1.0f, -1.0f, -5.0f,
      1.0f, 1.0f, -5.0f,
      -1.0f, -1.0f, -5.0f,
      1.0f, -1.0f, -5.0f,
      1.0f, 1.0f, -5.0f
  };

  public static final float[] FLOATING_CANVAS_COLORS = new float[] {
      // front, green
      0f, 0.5273f, 0.2656f, 1.0f,
      0f, 0.5273f, 0.2656f, 1.0f,
      0f, 0.5273f, 0.2656f, 1.0f,
      0f, 0.5273f, 0.2656f, 1.0f,
      0f, 0.5273f, 0.2656f, 1.0f,
      0f, 0.5273f, 0.2656f, 1.0f
  };

  public static final float[] FLOATING_CANVAS_FOUND_COLORS = new float[] {
      // front, yellow
      1.0f,  0.6523f, 0.0f, 1.0f,
      1.0f,  0.6523f, 0.0f, 1.0f,
      1.0f,  0.6523f, 0.0f, 1.0f,
      1.0f,  0.6523f, 0.0f, 1.0f,
      1.0f,  0.6523f, 0.0f, 1.0f,
      1.0f,  0.6523f, 0.0f, 1.0f
  };

  public static final float[] FLOATING_CANVAS_NORMALS = new float[] {
      // Front face
      0.0f, 0.0f, 1.0f,
      0.0f, 0.0f, 1.0f,
      0.0f, 0.0f, 1.0f,
      0.0f, 0.0f, 1.0f,
      0.0f, 0.0f, 1.0f,
      0.0f, 0.0f, 1.0f
  };

    public static final float[] FLOATING_CANVAS_TEX_COORDS = {
            0.0f, -0.5f,
            0.0f, 1.0f,
            1.0f, -0.5f,
            0.0f, 1.0f,
            1.0f, 1.0f,
            1.0f, -0.5f
    };
//swap left and right for sbs videos.. for demo only
    public static final float[] FLOATING_VIDEO_RIGHT_TEX_COORDS  = {
            0.0f, 0.0f,
            0.0f, 1.0f,
            0.5f, 0.0f,
            0.0f, 1.0f,
            0.5f, 1.0f,
            0.5f, 0.0f
    };


    public static final float[] FLOATING_VIDEO_LEFT_TEX_COORDS = {
            0.5f, 0.0f,
            0.5f, 1.0f,
            1.0f, 0.0f,
            0.5f, 1.0f,
            1.0f, 1.0f,
            1.0f, 0.0f
    };

  // The grid lines on the floor are rendered procedurally and large polygons cause floating point
  // precision problems on some architectures. So we split the floor into 4 quadrants.
  public static final float[] FLOOR_COORDS = new float[] {
      // +X, +Z quadrant
      200, 0, 0,
      0, 0, 0,
      0, 0, 200,
      200, 0, 0,
      0, 0, 200,
      200, 0, 200,

      // -X, +Z quadrant
      0, 0, 0,
      -200, 0, 0,
      -200, 0, 200,
      0, 0, 0,
      -200, 0, 200,
      0, 0, 200,

      // +X, -Z quadrant
      200, 0, -200,
      0, 0, -200,
      0, 0, 0,
      200, 0, -200,
      0, 0, 0,
      200, 0, 0,

      // -X, -Z quadrant
      0, 0, -200,
      -200, 0, -200,
      -200, 0, 0,
      0, 0, -200,
      -200, 0, 0,
      0, 0, 0,
  };

  public static final float[] FLOOR_NORMALS = new float[] {
      0.0f, 1.0f, 0.0f,
      0.0f, 1.0f, 0.0f,
      0.0f, 1.0f, 0.0f,
      0.0f, 1.0f, 0.0f,
      0.0f, 1.0f, 0.0f,
      0.0f, 1.0f, 0.0f,
      0.0f, 1.0f, 0.0f,
      0.0f, 1.0f, 0.0f,
      0.0f, 1.0f, 0.0f,
      0.0f, 1.0f, 0.0f,
      0.0f, 1.0f, 0.0f,
      0.0f, 1.0f, 0.0f,
      0.0f, 1.0f, 0.0f,
      0.0f, 1.0f, 0.0f,
      0.0f, 1.0f, 0.0f,
      0.0f, 1.0f, 0.0f,
      0.0f, 1.0f, 0.0f,
      0.0f, 1.0f, 0.0f,
      0.0f, 1.0f, 0.0f,
      0.0f, 1.0f, 0.0f,
      0.0f, 1.0f, 0.0f,
      0.0f, 1.0f, 0.0f,
      0.0f, 1.0f, 0.0f,
      0.0f, 1.0f, 0.0f,
  };

  public static final float[] FLOOR_COLORS = new float[] {
      0.0f, 0.3398f, 0.9023f, 1.0f,
      0.0f, 0.3398f, 0.9023f, 1.0f,
      0.0f, 0.3398f, 0.9023f, 1.0f,
      0.0f, 0.3398f, 0.9023f, 1.0f,
      0.0f, 0.3398f, 0.9023f, 1.0f,
      0.0f, 0.3398f, 0.9023f, 1.0f,
      0.0f, 0.3398f, 0.9023f, 1.0f,
      0.0f, 0.3398f, 0.9023f, 1.0f,
      0.0f, 0.3398f, 0.9023f, 1.0f,
      0.0f, 0.3398f, 0.9023f, 1.0f,
      0.0f, 0.3398f, 0.9023f, 1.0f,
      0.0f, 0.3398f, 0.9023f, 1.0f,
      0.0f, 0.3398f, 0.9023f, 1.0f,
      0.0f, 0.3398f, 0.9023f, 1.0f,
      0.0f, 0.3398f, 0.9023f, 1.0f,
      0.0f, 0.3398f, 0.9023f, 1.0f,
      0.0f, 0.3398f, 0.9023f, 1.0f,
      0.0f, 0.3398f, 0.9023f, 1.0f,
      0.0f, 0.3398f, 0.9023f, 1.0f,
      0.0f, 0.3398f, 0.9023f, 1.0f,
      0.0f, 0.3398f, 0.9023f, 1.0f,
      0.0f, 0.3398f, 0.9023f, 1.0f,
      0.0f, 0.3398f, 0.9023f, 1.0f,
      0.0f, 0.3398f, 0.9023f, 1.0f,
  };
}
