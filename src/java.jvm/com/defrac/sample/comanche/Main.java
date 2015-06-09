package com.defrac.sample.comanche;

import defrac.ui.FrameBuilder;

public final class Main {
  public static void main(String[] args) {
    FrameBuilder.
        forScreen(new ComancheScreen()).
        resizable().
        maximized().
        title("Comanche").
        backgroundColor(0xff000000).
        show();
  }
}
