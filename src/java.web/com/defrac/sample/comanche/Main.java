package com.defrac.sample.comanche;

import defrac.ui.FrameBuilder;

public final class Main {
  public static void main(String[] args) {
    FrameBuilder.
        forScreen(new ComancheScreen()).
        title("Comanche").
        show();
  }
}
