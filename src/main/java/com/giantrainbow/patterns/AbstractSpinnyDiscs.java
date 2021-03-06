package com.giantrainbow.patterns;

import static processing.core.PConstants.CLAMP;

import com.giantrainbow.model.space.Lissajous;
import heronarts.lx.LX;
import heronarts.lx.parameter.CompoundParameter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;
import org.joml.sampling.Callback2d;
import org.joml.sampling.PoissonSampling.Disk;
import processing.core.PImage;

/**
 * AbstractSpinnyDiscs animates a number of variable sized, rotating color wheels in 2D space. They
 * are implemented as a texture mapped circle w/ alpha surround. The balls are positioned by a
 * https://en.wikipedia.org/wiki/Lissajous_curve.
 */
public abstract class AbstractSpinnyDiscs extends CanvasPattern2D {
  public final float MIN_SIZE = 1;
  public final float MAX_SIZE = 8;

  public final float MOVEMENT_RANGE = .5f; // Movement range as a ratio of width/height
  public final float ROTATE_RATE = 100f; // Speed range (individual balls)
  public final float SPIN_RATE = 10f; // Speed range (whole canvas)
  public final float MAX_SPEED = 5f; // Speed range
  public final float MIN_SPEED = 1f; // Speed range
  public final float AB_MAX = 6;
  public final float MSHZ = 1 / 100000f; // Arbitrary slowdown of the millisecond counter

  public final int BALL_COUNT = 1000;

  // Speed determines the overall speed of the entire pattern.
  public final CompoundParameter speedKnob =
      new CompoundParameter("Speed", 5, 0, 10).setDescription("Speed");
  public final CompoundParameter sizeKnob =
      new CompoundParameter("Size", 5, 1, 15).setDescription("Size");
  public final CompoundParameter rangeKnob =
      new CompoundParameter("Range", .5, 0, 1).setDescription("Range");

  // Count determines the number of balls that render.  They are
  // animated continuously, so raising and lower the number will
  // show/hide them while they continue moving with the animation.
  public final CompoundParameter countKnob =
      new CompoundParameter("Count", 150, 1, BALL_COUNT).setDescription("Count");

  // The "delta" paramter of a Lissajous curve that moves all the balls.
  public final CompoundParameter deltaKnob =
      new CompoundParameter("Delta", Math.PI / 4, -Math.PI / 2, Math.PI / 2)
          .setDescription("Delta");

  // The "rotate" paramter determines how fast the whole thing spins.
  public final CompoundParameter rotateKnob =
      new CompoundParameter("Rotate", 1, -10, 10).setDescription("Rotate");

  Ball balls[];
  float telapsed;
  float relapsed;

  public AbstractSpinnyDiscs(LX lx) {
    super(lx);

    Random rnd = new Random();

    this.telapsed = 0;
    this.relapsed = 0;
    this.balls = new Ball[BALL_COUNT];

    float radius = distance(0, 0, canvas.width() / 2, canvas.height());

    pg.textureWrap(CLAMP);

    int minDist = 20;
    for (; ; ) {
      ArrayList<Ball> list = new ArrayList<Ball>();
      new Disk(
          rnd.nextLong(),
          radius,
          minDist,
          25,
          new Callback2d() {
            public void onNewSample(float x, float y) {
              Ball ball = new Ball();
              ball.Num = list.size();
              ball.X = x;
              ball.Y = y;
              ball.A = 2 * radius * rnd.nextFloat() * MOVEMENT_RANGE;
              ball.Bratio = rnd.nextFloat() + .5f;
              ball.a = (rnd.nextFloat() * (AB_MAX - 1) + 1);
              ball.b = (rnd.nextFloat() * (AB_MAX - 1) + 1);
              ball.R = MIN_SIZE + (MAX_SIZE - MIN_SIZE) * rnd.nextFloat();
              ball.ST = MIN_SPEED + (MAX_SPEED - MIN_SPEED) * 2f * (rnd.nextFloat() - 0.5f);
              ball.SR = ROTATE_RATE * 2f * (rnd.nextFloat() - 0.5f);
              ball.Angle = (float) rnd.nextFloat() * (float) Math.PI;
              list.add(ball);
            }
          });

      if (list.size() < BALL_COUNT) {
        minDist /= 2;
        continue;
      }

      if (list.size() > BALL_COUNT * 1.25) {
        minDist *= 1.25;
        continue;
      }

      Collections.shuffle(list, rnd);

      balls = list.toArray(balls);
      break;
    }

    removeParameter(fpsKnob);
    addParameter(speedKnob);
    addParameter(countKnob);
    addParameter(deltaKnob);
    addParameter(rotateKnob);
    addParameter(sizeKnob);
    addParameter(rangeKnob);
  }

  float distance(float x0, float y0, float x1, float y1) {
    return (float) Math.sqrt((x0 - x1) * (x0 - x1) + (y0 - y1) * (y0 - y1));
  }

  public void draw(double deltaMs) {
    if (telapsed == 0) {
      pg.background(0);
    }
    double speed = speedKnob.getValue();
    telapsed += (float) (deltaMs * speed);
    double rotate = rotateKnob.getValue();
    relapsed += (float) (deltaMs * rotate);

    if (hasBackground()) {
      pg.background(getBackground(deltaMs));
    }

    pg.translate(canvas.width() / 2, 0);
    pg.rotate(relapsed * SPIN_RATE * MSHZ);

    for (int i = 0; i < countKnob.getValue(); i++) {
      if (i >= balls.length) {
        break;
      }
      balls[i].draw();
    }
  }

  public class Ball {
    int Num; // Sequence number
    float X; // Y location
    float Y; // X location
    float A; // Lissajous B
    float Bratio; // Lissajous B/A ratio
    float a; // Lissajous a
    float b; // Lissajous b
    float R; // Radius
    float ST; // Translation speed
    float SR; // Rotation speed
    float Angle; // Angle offset

    void draw() {
      PImage texture = getTexture(Num);
      if (texture == null) {
        return;
      }

      pg.pushMatrix();

      float delta = (float) deltaKnob.getValue();
      float position = (float) telapsed * ST * MSHZ;
      float rotation = (float) (relapsed + telapsed) * SR * MSHZ;
      float range = (float) rangeKnob.getValue();
      float x = Lissajous.locationX(range * A, a, delta, position);
      float y = Lissajous.locationY(range * A * Bratio, b, position);

      pg.rotate(Angle);
      pg.translate(x, y);
      pg.rotate(-Angle);
      pg.translate(X, Y);
      pg.rotate(rotation);

      pg.beginShape();
      pg.noStroke();
      pg.texture(texture);

      float w = texture.width;
      float r = R * (float) sizeKnob.getValue();

      pg.vertex(-r, -r, 0, 0);
      pg.vertex(r, -r, w, 0);
      pg.vertex(r, r, w, w);
      pg.vertex(-r, r, 0, w);
      pg.endShape();

      pg.popMatrix();
    }
  };

  abstract PImage getTexture(int number);

  abstract int getBackground(double deltaMs);

  abstract boolean hasBackground();
}
