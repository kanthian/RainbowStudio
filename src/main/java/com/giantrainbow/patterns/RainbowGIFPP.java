package com.giantrainbow.patterns;

import com.giantrainbow.model.RainbowBaseModel;
import heronarts.lx.LX;
import heronarts.lx.LXCategory;

/**
 * Pixel perfect animated GIFs.  Uses base class with directory data/gifpp, default file of life2.gif, and
 * no antialias toggle.
 */
@LXCategory(LXCategory.FORM)
public class RainbowGIFPP extends RainbowGIFBase {
  public RainbowGIFPP(LX lx) {
    super(lx, ((RainbowBaseModel)lx.model).pointsWide, ((RainbowBaseModel)lx.model).pointsHigh,
        "gifpp/",
        "life2",
        false);
  }

  protected void renderToPoints() {
    RenderImageUtil.imageToPointsPixelPerfect(colors, images[(int)currentFrame]);
  }
}
