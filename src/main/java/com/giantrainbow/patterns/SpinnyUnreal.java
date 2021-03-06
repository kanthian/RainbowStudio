package com.giantrainbow.patterns;

import com.giantrainbow.RainbowStudio;
import heronarts.lx.LX;
import heronarts.lx.LXCategory;
import processing.core.PImage;

@LXCategory(LXCategory.FORM)
public class SpinnyUnreal extends AbstractSpinnyDiscs {

  PImage textures[];

  String inputs[] = {
    "images/unreal-disc-level=1.02-sat=1.32.png",
    "images/unreal-disc-level=1.02-sat=1.35.png",
    "images/unreal-disc-level=1.02-sat=1.41.png",
    "images/unreal-disc-level=1.02-sat=1.42.png",
    "images/unreal-disc-level=1.02-sat=1.44.png",
    "images/unreal-disc-level=1.02-sat=1.49.png",
    "images/unreal-disc-level=1.02-sat=1.66.png",
    "images/unreal-disc-level=1.02-sat=1.70.png",
    "images/unreal-disc-level=1.09-sat=1.42.png",
    "images/unreal-disc-level=1.09-sat=1.43.png",
    "images/unreal-disc-level=1.09-sat=1.47.png",
    "images/unreal-disc-level=1.09-sat=1.48.png",
    "images/unreal-disc-level=1.09-sat=1.49.png",
    "images/unreal-disc-level=1.09-sat=1.54.png",
    "images/unreal-disc-level=1.09-sat=1.57.png",
    "images/unreal-disc-level=1.16-sat=1.30.png",
    "images/unreal-disc-level=1.16-sat=1.37.png",
    "images/unreal-disc-level=1.16-sat=1.49.png",
    "images/unreal-disc-level=1.16-sat=1.59.png",
    "images/unreal-disc-level=1.23-sat=1.36.png",
    "images/unreal-disc-level=1.23-sat=1.39.png",
    "images/unreal-disc-level=1.23-sat=1.42.png",
    "images/unreal-disc-level=1.23-sat=1.55.png",
    "images/unreal-disc-level=1.23-sat=1.62.png",
    "images/unreal-disc-level=1.23-sat=1.65.png",
  };

  BackgroundPulse pulse;

  public SpinnyUnreal(LX lx) {
    super(lx);

    pulse = new BackgroundPulse(this, "BG");

    textures = new PImage[inputs.length];

    sizeKnob.setValue(10);
    pulse.levelKnob.setValue(0);

    new Thread(
            new Runnable() {
              public void run() {
                loadTextures();
              }
            })
        .start();
  }

  void loadTextures() {
    for (int i = 0; i < inputs.length; i++) {
      PImage img = RainbowStudio.pApplet.loadImage(inputs[i]);
      img.loadPixels();
      synchronized (textures) {
        this.textures[i] = img;
      }
    }
  }

  PImage getTexture(int number) {
    synchronized (textures) {
      return textures[number % textures.length];
    }
  }

  boolean hasBackground() {
    return true;
  }

  int getBackground(double deltaMs) {
    return pulse.get(deltaMs);
  }
};
