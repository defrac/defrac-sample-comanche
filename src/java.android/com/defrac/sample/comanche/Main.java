package com.defrac.sample.comanche.;

import android.content.Intent;
import android.os.Build;
import com.defrac.sample.comanche.ComancheScreen;
import defrac.dni.Activity;
import defrac.dni.IntentFilter;
import defrac.dni.UsesSdk;
import defrac.ui.Screen;
import defrac.ui.ScreenActivity;

@Activity(label = "Comanche", filter = @IntentFilter(action = Intent.ACTION_MAIN, category = Intent.CATEGORY_LAUNCHER))
@UsesSdk(minSdkVersion = Build.VERSION_CODES.ICE_CREAM_SANDWICH)
public final class Main extends ScreenActivity {
  @Override
  protected Screen createScreen() {
    return new ComancheScreen();
  }
}
