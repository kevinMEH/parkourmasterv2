package io.github.kevinmeh.desktop;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import io.github.kevinmeh.ParkourMaster;

public class DesktopLauncher {
	public static void main (String[] arg) {
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		config.title = "Parkour Master v2";
		config.width = 1600;
		config.height = 900;
		new LwjglApplication(new ParkourMaster(), config);
	}
}
