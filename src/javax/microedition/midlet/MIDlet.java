package javax.microedition.midlet;

import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.jar.Attributes;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.microedition.io.ConnectionNotFoundException;

public abstract class MIDlet {

	private static Attributes attributes;
	private Attributes attr;

	private Runnable destroyListener = null;

	public boolean _destroyed;

	public static void _setAttr(Attributes attr) {
		attributes = attr;
	}

	public void _setDestroyListener(Runnable r) {
		destroyListener = r;
	}

	protected MIDlet() {
		attr = attributes;
	}

	protected abstract void startApp() throws MIDletStateChangeException;

	protected abstract void pauseApp();

	protected abstract void destroyApp(boolean unconditional) throws MIDletStateChangeException;

	public final void notifyDestroyed() {
		if (destroyListener != null) {
			destroyListener.run();
		}
		_destroyed = true;
	}

	public final void notifyPaused() {

	}

	public final String getAppProperty(String key) {
		String prop = attr.getValue(key);
		if (prop != null) {
			prop = prop.trim();
		}
		return prop;
	}

	public final void resumeRequest() {

	}

	public final boolean platformRequest(String URL) throws ConnectionNotFoundException {
		Desktop desktop = java.awt.Desktop.getDesktop();
		try {
			URI oURL = new URI(URL);
			desktop.browse(oURL);
		} catch (IOException | URISyntaxException e) {
			throw new ConnectionNotFoundException();
		}
		return true;
	}

	public final int checkPermission(String permission) {
		return 0;
	}

	public void _startApp() {
		try {
			startApp();
		} catch (MIDletStateChangeException ex) {
			ex.printStackTrace();
		}
	}

	public void _destroyApp() {
		try {
			destroyApp(true);
		} catch (Throwable t) {
			System.out.println("Error destroying app");
		}
	}
}
