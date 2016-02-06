/*
 * Comanche engine adopted from http://simulationcorner.net/index.php?page=comanche
 */

package com.defrac.sample.comanche;

import defrac.display.*;
import defrac.geom.Point;
import defrac.resource.BinaryResource;
import defrac.resource.ResourceGroup;
import defrac.ui.ContentScreen;
import defrac.ui.DisplayList;

import javax.annotation.Nonnull;
import java.util.List;

public final class ComancheScreen extends ContentScreen {
  static final int WIDTH = 512;
  static final int HEIGHT = 256;
  static final int DEPTH = 400;

  static final boolean AUTOPILOT = true;
  static final double AUTOPILOT_PHASE_INC = 110.0 / 44100.0;
  static double AUTOPILOT_PHASE = 0.0;

  static final int STATE_MOVE_FWD =   1;
  static final int STATE_MOVE_BCK =   2;
  static final int STATE_MOVE_UPW =   4;
  static final int STATE_MOVE_DWN =   8;
  static final int STATE_ROTA_LFT =  16;
  static final int STATE_ROTA_RGT =  32;
  static final int STATE_ROTA_UPW =  64;
  static final int STATE_ROTA_DWN = 128;

  int state = 0;
  ComancheEngine engine;

  DisplayList displayList;

  @Override
  protected void onCreate() {
    super.onCreate();

    displayList = new DisplayList();

    final ResourceGroup<byte[]> resources =
        ResourceGroup.of(
          BinaryResource.from("map20.color"),
          BinaryResource.from("map20.height"),
          BinaryResource.from("map20.palette"));

    resources.listener(new ResourceGroup.SimpleListener<byte[]>() {
      @Override
      public void onResourceGroupComplete(@Nonnull ResourceGroup<byte[]> resourceGroup,
                                          @Nonnull List<byte[]> content) {
        displayList.onStageReady(
            stage -> continueWithResourcesAndStage(stage, content.get(0), content.get(1), content.get(2)));
      }
    });

    resources.load();

    rootView(displayList);
  }

  @Override
  protected void onPause() {
    super.onPause();
    displayList.onPause();
  }

  @Override
  protected void onResume() {
    super.onResume();
    displayList.onResume();
  }

  void continueWithResourcesAndStage(final Stage stage,
                                     final byte[] colorMap,
                                     final byte[] heightMap,
                                     final byte[] paletteMap) {
    // We create a dummy TextureData the engine dynamically
    // update while rendering
    TextureData textureData =
        TextureData.Persistent.fromData(
            new byte[WIDTH * HEIGHT * 3], WIDTH, HEIGHT,
            TextureDataFormat.RGB,
            TextureDataRepeat.NO_REPEAT,
            TextureDataSmoothing.NO_SMOOTHING_WITHOUT_MIPMAP);

    // Create the engine and render at least once.
    engine = new ComancheEngine(WIDTH, HEIGHT, DEPTH, colorMap, heightMap, paletteMap, textureData);
    engine.render();

    // In order do visualize the texture data, we add it as a
    // child to the stage and scale its size to fit the width/height
    stage.
        addChild(new Image(textureData).scaleToSize(width(), height())).
        addChild(new Stats());

    // Update the screen each frame
    stage.globalEvents().onEnterFrame.add(enterFrameEvent -> onEnterFrame());

    // Now we just add some listeners for keyboard and pointer input
    stage.globalEvents().onKeyDown.add(event -> onKeyDown(event.keyCode));
    stage.globalEvents().onKeyUp.add(event -> onKeyUp(event.keyCode));
    stage.globalEvents().onPointerDown.add(event -> onPointerDown(event.pos));
    stage.globalEvents().onPointerMove.add(event -> onPointerMove(event.pos));
    stage.globalEvents().onPointerUp.add(event -> onPointerUp(event.pos));

    // Adjust the size of the Image when the Stage is resized
    stage.globalEvents().onResize.add(event -> onResize(event.stage, event.width, event.height));
  }

  private void onEnterFrame() {
    if(AUTOPILOT) {
      engine.moveForwards();

      AUTOPILOT_PHASE += AUTOPILOT_PHASE_INC;
      if(AUTOPILOT_PHASE > 1.0) {
        --AUTOPILOT_PHASE;
      }

      double mapHeight = engine.currentHeight();

      engine.hp += ((-mapHeight + 100) - engine.hp) * 0.06125;
      engine.vp = -10 - (int)Math.round(Math.cos(AUTOPILOT_PHASE * Math.PI * 2.0) * 10);
      engine.rotation(Math.sin(AUTOPILOT_PHASE * Math.PI * 2.0) * Math.PI * 0.25);
      engine.render();
    } else {
      // only render if something is actually happening
      if (state != 0) {
        if ((state & STATE_MOVE_FWD) != 0) engine.moveForwards();
        if ((state & STATE_MOVE_BCK) != 0) engine.moveBackwards();
        if ((state & STATE_MOVE_UPW) != 0) engine.moveUpwards();
        if ((state & STATE_MOVE_DWN) != 0) engine.moveDownwards();
        if ((state & STATE_ROTA_LFT) != 0) engine.rotateLeft();
        if ((state & STATE_ROTA_RGT) != 0) engine.rotateRight();
        if ((state & STATE_ROTA_UPW) != 0) engine.rotateUpwards();
        if ((state & STATE_ROTA_DWN) != 0) engine.rotateDownwards();
        engine.render();
      }
    }
  }

  private void onKeyDown(final int keyCode) {
    if(AUTOPILOT) {
      return;
    }

    int bit = 0;
    switch(keyCode) {
      case 87: bit = STATE_MOVE_FWD; break; // w
      case 83: bit = STATE_MOVE_BCK; break; // s
      case 81: bit = STATE_MOVE_UPW; break; // q
      case 69: bit = STATE_MOVE_DWN; break; // e
      case 65: bit = STATE_ROTA_LFT; break; // a
      case 68: bit = STATE_ROTA_RGT; break; // d
      case 82: bit = STATE_ROTA_UPW; break; // r
      case 70: bit = STATE_ROTA_DWN; break; // f
    }
    state |= bit;
  }

  private void onKeyUp(final int keyCode) {
    if(AUTOPILOT) {
      return;
    }

    int bit = 0;
    switch(keyCode) {
      case 87: bit = STATE_MOVE_FWD; break; // w
      case 83: bit = STATE_MOVE_BCK; break; // s
      case 81: bit = STATE_MOVE_UPW; break; // q
      case 69: bit = STATE_MOVE_DWN; break; // e
      case 65: bit = STATE_ROTA_LFT; break; // a
      case 68: bit = STATE_ROTA_RGT; break; // d
      case 82: bit = STATE_ROTA_UPW; break; // r
      case 70: bit = STATE_ROTA_DWN; break; // f
    }
    state &= ~bit;
  }

  private void onPointerDown(final Point point) {
    if(AUTOPILOT) {
      return;
    }

    state |= STATE_MOVE_FWD;
  }

  private void onPointerMove(final Point point) {
    if(AUTOPILOT) {
      return;
    }

    if((state & STATE_MOVE_FWD) == 0) {
      return;
    }

    float rx = point.x / width();
    float ry = point.y / height();

    if(rx > 0.75) {
      engine.rotateRight();
    } else if (rx < 0.25) {
      engine.rotateLeft();
    }

    if(ry > 0.75) {
      engine.rotateDownwards();
    } else if(ry < 0.25) {
      engine.rotateUpwards();
    }
  }

  private void onPointerUp(final Point point) {
    if(AUTOPILOT) {
      return;
    }

    state &= ~STATE_MOVE_FWD;
  }

  private void onResize(@Nonnull final Stage stage, final float width, final float height) {
    if(stage.numChildren() > 0) {
      stage.childAt(0).scaleToSize(width, height);
    }
  }
}
