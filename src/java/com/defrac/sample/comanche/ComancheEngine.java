/*
 * Comanche engine adopted from http://simulationcorner.net/index.php?page=comanche
 */

package com.defrac.sample.comanche;

import defrac.display.TextureData;
import defrac.display.TextureDataBuffer;

import java.util.Arrays;

final class ComancheEngine {
  static final double MOVE_SPEED = 3.0;
  static final double ROTATE_SPEED = 0.1;

  int width, height, depth;
  int[] heightMap;
  int[] colorIndex;
  byte[] paletteMap;

  double xp = 512.0, yp = 800.0;
  int hp = -50, vp = -100;
  double angle = 0.0, angleCos = Math.cos(angle), angleSin = Math.sin(angle);

  TextureDataBuffer textureBuffer;
  byte[] pixels;

  static int[] toUint8(byte[] bytes) {
    int n = bytes.length;
    final int[] res = new int[n];
    while(--n > -1) {
      res[n] = bytes[n] & 0xff;
    }
    return res;
  }

  ComancheEngine(int width, int height, int depth,
                 byte[] colorIndex,
                 byte[] heightMap,
                 byte[] paletteMap,
                 TextureData textureData) {
    this.width = width;
    this.height = height;
    this.depth = depth;
    this.colorIndex = toUint8(colorIndex);
    this.heightMap = toUint8(heightMap);
    this.paletteMap = paletteMap;
    this.pixels = new byte[width * height * 3];

    // We obtain a buffer for the TextureData. This allows us manually
    // read and/or write pixels.
    //
    // The memory hint WRITE_ONLY gives the best performance in this case
    // since we only want to write data and are not interested in reading anything.
    textureBuffer = textureData.loadPixels(TextureDataBuffer.MemoryHint.WRITE_ONLY);
  }

  void render() {
    // Clear the pixel array
    Arrays.fill(pixels, (byte) 0);

    // Cast rays and fill pixel array
    for(int i = 0; i < width; i++) {
      double y3d = -depth * 1.5;
      double x3d = (i - width / 2) * 1.5 * 1.5;
      double rotx = angleCos * x3d + angleSin * y3d;
      double roty = -angleSin * x3d + angleCos * y3d;
      raycast(i, xp, yp, rotx + xp, roty + yp, y3d / Math.sqrt(x3d * x3d + y3d * y3d));
    }

    // Set pixels in TextureDataBuffer
    textureBuffer.setPixels(0, 0, width, height, pixels, 0);

    // Store all modifications to the TextureDataBuffer
    // and upload any changes to the GPU
    textureBuffer.storePixels();
  }

  void raycast(int line, double x1, double y1, double x2, double y2, double d) {
    double dx = x2 - x1;
    double dy = y2 - y1;
    double r = Math.floor(Math.sqrt(dx * dx + dy * dy));

    dx /= r;
    dy /= r;

    double ymin = height;
    double n = (int)Math.floor(r) - 20;

    for(double i = 0.0; i < n; i++) {
      x1 += dx;
      y1 += dy;

      int mapOffset =
        (((int)Math.floor(y1) & 1023) << 10) + ((int)Math.floor(x1) & 1023);

      double h = heightMap[mapOffset];
      int ci = colorIndex[mapOffset];

      h = (256 - h);
      h = h - 128 + hp;

      double y3 = i * Math.abs(d);
      double z3 = h / y3 * 100 - vp;

      if(z3 < 0.0) {
        z3 = 0.0;
      }

      if(z3 < height) {
        int z3i = (int)Math.floor(z3);
        int offset = z3i * width * 3 + line * 3;

        for(int j = z3i; j < ymin; j++) {
          int po = offset;
          int co = ci * 3;
          pixels[po++] = paletteMap[co++];
          pixels[po++] = paletteMap[co++];
          pixels[po  ] = paletteMap[co  ];
          offset += width * 3;
        }
      }

      if(ymin > z3) {
        ymin = z3;
      }
    }
  }

  void rotateLeft() {
    angle += ROTATE_SPEED;
    angleCos = Math.cos(angle);
    angleSin = Math.sin(angle);
  }

  void rotateRight() {
    angle -= ROTATE_SPEED;
    angleCos = Math.cos(angle);
    angleSin = Math.sin(angle);
  }

  void rotateUpwards() {
    vp -= 2;
  }

  void rotateDownwards() {
    vp += 2;
  }

  void moveForwards() {
    xp -= MOVE_SPEED * angleSin;
    yp -= MOVE_SPEED * angleCos;
  }

  void moveBackwards() {
    xp += MOVE_SPEED * angleSin;
    yp += MOVE_SPEED * angleCos;
  }

  void moveUpwards() {
    hp += 2;
  }

  void moveDownwards() {
    hp -= 2;
  }
}
