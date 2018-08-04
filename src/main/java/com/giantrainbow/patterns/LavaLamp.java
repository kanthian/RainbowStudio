/*
 * Created by shawn on 8/4/18 10:54 AM.
 */
package com.giantrainbow.patterns;

import static heronarts.lx.color.LXColor.BLACK;
import static heronarts.lx.color.LXColor.WHITE;
import static processing.core.PApplet.constrain;
import static processing.core.PApplet.pow;
import static processing.core.PConstants.ARGB;
import static processing.core.PConstants.BLUR;
import static processing.core.PConstants.P2D;
import static processing.core.PConstants.RGB;
import static processing.core.PConstants.THRESHOLD;

import com.giantrainbow.RainbowStudio;
import com.giantrainbow.colors.ColorRainbow;
import com.giantrainbow.colors.Colors;
import heronarts.lx.LX;
import heronarts.lx.LXCategory;
import java.util.Arrays;
import processing.core.PImage;

/**
 * Based on: <a href="http://openprocessing.org/sketch/4675">Rorschach Generator</a>
 */
@LXCategory(LXCategory.FORM)
public class LavaLamp extends PGPixelPerfect {
  private static final float COLOR_CHANGE_TIME = 10.0f;
  private static final float DEFAULT_FPS = 60.0f;

  private static final int SEGMENT_W = 60;

  private float[][] balls;  // i: x, y, vx, vy

  private int nBalls = 42;
  private float thresh = 0.1f;
  private float vMax = 3;
  private int radius;

  private float speedScale;

  private PImage ballImage;

  // Color interpolation
  private ColorRainbow rainbow = new ColorRainbow(
      new ColorRainbow.NextColor() {
        private Integer firstColor;

        protected void reset() {
          firstColor = BLACK;
        }

        /**
         * Chooses a random color from the palette that isn't the last color.
         */
        protected ColorRainbow.ColorTransition get() {
          if (firstColor != null) {
            int c = firstColor;
            firstColor = null;
            return new ColorRainbow.ColorTransition(c, COLOR_CHANGE_TIME);
          }

          while (true) {
            int c = Colors.randomColor(6);
            if (!lastColorMatches(c)) {
              return new ColorRainbow.ColorTransition(c, COLOR_CHANGE_TIME);
            }
          }
        }
      });

  public LavaLamp(LX lx) {
    super(lx, P2D);

    fpsKnob.addListener(lxParameter -> {
          if (lxParameter.getValue() > 0.0) {
            speedScale = 3.0f / fpsKnob.getValuef();
          }
        });
  }

  @Override
  public void onActive() {
    fpsKnob.setValue(DEFAULT_FPS);

    // Parameters
    radius = SEGMENT_W / 4;//(int) (pg.width / 10 * 1.2);
    speedScale = 3.0f / DEFAULT_FPS;

    balls = new float[nBalls][4];

    pg.beginDraw();
    generateCircleImage();
    pg.endDraw();
    generateBalls();

    rainbow.reset(DEFAULT_FPS);
  }

  @Override
  public void onInactive() {
//    ballImage = null;
    balls = null;
  }

  /**
   * Returns the complement of a color.
   */
  private int complementColor(int c) {
    return (c & 0xff000000)
        | (0xff0000 - (c & 0xff0000))
        | (0xff00 - (c & 0xff00))
        | (0xff - (c & 0xff));
  }

  @Override
  protected void draw(double deltaDrawMs) {
    pg.colorMode(RGB, 255);
    int bgColor = rainbow.get(pg, fpsKnob.getValuef());
    int ballColor = complementColor(bgColor);

    moveBalls();

    pg.background(WHITE);
    pg.loadPixels();
    int[] p1 = Arrays.copyOf(pg.pixels, pg.pixels.length);
    for (float[] ball : balls) {
      pg.image(ballImage, ball[0] - radius, ball[1] - radius);
    }
//    if (!Arrays.equals(p1, pg.pixels)) {
//      System.out.println("HERE!");
//    }


    pg.filter(THRESHOLD, thresh);

    //apply color changes
    pg.loadPixels();
    for (int i = pg.pixels.length; --i >= 0; ) {
      if (pg.pixels[i] == WHITE) {
        pg.pixels[i] = bgColor;
      } else {
        pg.pixels[i] = ballColor;
      }
    }
    pg.updatePixels();
    pg.filter(BLUR);
  }

  private void moveBalls() {
    for(float[] ball : balls) {
      if (ball[0] <= 0 || ball[0] >= pg.width) {
        ball[2] = -ball[2];
      }

      if (ball[1] <= 0 || ball[1] >= pg.height) {
        ball[3] = -ball[3];
      }

      ball[2] += RainbowStudio.pApplet.random(-0.1f, 0.1f);
      ball[3] += RainbowStudio.pApplet.random(-0.1f, 0.1f);
      ball[2] = constrain(ball[2], -vMax, vMax);
      ball[3] = constrain(ball[3], -vMax, vMax);

      ball[0] += ball[2] * speedScale;
      ball[1] += ball[3] * speedScale;
    }
  }

  private void generateCircleImage() {
    ballImage = RainbowStudio.pApplet.createImage(radius * 2, radius * 2, ARGB);
    for(int x = 0; x <= radius; x++) {
      for (int y = 0; y <= radius; y++) {
        float r2 = pow(x - radius, 2) + pow(y - radius, 2);
        if (r2 < radius * radius) {
          int c = pg.color(
              0, 0, 0,
              255 * (1 - r2/(radius*radius)));
          ballImage.set(x, y, c);
          ballImage.set(2*radius - x, y, c);
          ballImage.set(2*radius - x, 2 * radius - y, c);
          ballImage.set(x, 2*radius - y, c);
        } else {
          ballImage.set(x, y, pg.color(0, 0, 0, 0));
        }
      }
    }
  }

  private void generateBalls() {
    for (float[] ball : balls) {
      ball[0] = RainbowStudio.pApplet.random(radius, pg.width - radius);
      ball[1] = RainbowStudio.pApplet.random(radius, pg.height - radius);
      ball[2] = RainbowStudio.pApplet.random(-vMax, vMax);
      ball[3] = RainbowStudio.pApplet.random(-vMax, vMax);
    }
  }
}