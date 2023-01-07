package jademula;

import jademula.gui.MainFrame;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Locale;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Graphics;

public class Jademula {

	//public static int gameindex = 1;
	//private static final String gamedir = "games/" +
	//"collection/";
	//"johnny_rumble/";
	//"robin/";
	//private static Handy handy = new Handy(176, 208);
	//private static String[] games;
	private static MidletLoader loader;

	private static void setProperties() {
		System.setProperty("microedition.platform", "Nokia3650");
		System.setProperty("microedition.locale", Locale.getDefault().toLanguageTag());
		//System.setProperty("microedition.platformName", null);
		System.setProperty("microedition.encodingDefault", "ISO8859_1");
		System.setProperty("microedition.configuration", "CLDC-1.0");
		//System.setProperty("microedition.profilesNames", null);
	}

	public static void main(String[] args) {
		setProperties();
		load();
		//if (args.length > 0) gameindex = Integer.parseInt(args[0]);
		new Thread(new DirectInput(MainFrame.getInstance().getFrame())).start(); //ungut?
		/*File dir = new File(gamedir);
		games = dir.list();
		System.out.println("Starting " + gameindex + ": " + games[gameindex - 1]);
		loader = new MidletLoader(gamedir + games[gameindex - 1]);
		loader.run();*/
 /*javax.swing.SwingUtilities.invokeLater(
				new Runnable()
				{
					public void run()
					{
						handy = new Handy(200,200);
					}
				}
			);
		 */
	}

	public static void unload() {
		if (loader != null) {
			loader.stop();
		}
	}

	public static Attributes getOwnAttributes() {
		try {
			return new Manifest(Jademula.class.getResourceAsStream("/META-INF/MANIFEST.MF")).getMainAttributes();
		} catch (IOException ex) {
			return null;
		}
	}

	public static void loadMyself() {
		unload();
		loader = new MidletLoader(null, getOwnAttributes());

		if (loader != null) {
			loader.run();
		}
	}

	public static void load(String filename, Attributes attr) {
		unload();
		loader = new MidletLoader(filename, attr);
		loader.run();
	}

	public static void load(String filename) {
		unload();
		loader = new MidletLoader(filename);
		loader.run();
	}

	public static void reload() {
		if (loader != null) {
			loader.restart();
		}
	}

	//public static Handy getHandy() {
	//return handy;
	//}
	//public static String getGame() {
	//	return games[gameindex - 1];
	//}
	public static String getName() {
		return loader.getAttribute("MIDlet-Name");
	}

	public static String getVendor() {
		return loader.getAttribute("MIDlet-Vendor").trim();
	}

	public static String getVersion() {
		return "0.24";
	}

	private static int zoom = 1;
	private static int reqZoom = 1;

	public static void setZoom(int zoomlevel) {
		reqZoom = zoomlevel;
		if (MainFrame.getInstance().getDisplay().getCurrent() == null) {
			confirmChangeZoom();
		}
	}

	public static void confirmChangeZoom() {
		if (zoom != reqZoom) {
			zoom = reqZoom;
			MainFrame.getInstance().setDisplaySize(Handy.getCurrent().getWidth() * zoom, Handy.getCurrent().getHeight() * zoom);
			Font._updateSize();
			Graphics._updateSize(); //commit font size stuff to AWT backend
		}
	}

	public static int getZoom() {
		return zoom;
	}

	public static void saveOptions() {
		FileOutputStream fos = null;
		ObjectOutputStream out = null;
		try {
			fos = new FileOutputStream("options.dat");
			out = new ObjectOutputStream(fos);
			Handy.save(out);
			out.writeObject(InputManager.getInstance());
			out.close();
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}

	private static final Runnable atexit = new Runnable() {
		@Override
		public void run() {
			saveOptions();
		}
	};

	static {
		Runtime.getRuntime().addShutdownHook(new Thread(atexit));
	}

	public static void exit() {
		System.exit(0);
	}

	private static void load() {
		File file = new File("options.dat");
		if (!file.exists()) {
			return;
		}
		FileInputStream fis = null;
		ObjectInputStream in = null;
		try {
			fis = new FileInputStream("options.dat");
			in = new ObjectInputStream(fis);
			Handy.load(in);
			InputManager.load((InputManager) in.readObject());
			in.close();
		} catch (IOException ex) {
			ex.printStackTrace();
		} catch (ClassNotFoundException ex) {
			ex.printStackTrace();
		}
	}
}
