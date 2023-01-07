package jademula;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

import javax.microedition.midlet.MIDlet;

public class MidletLoader {

	private Attributes attr;
	private ClassLoader loader;
	private String midletName;
	private MIDlet m;

	public MidletLoader(String midletName) {
		this.midletName = midletName;
		start();
	}

	public MidletLoader(String midletName, Attributes attr) {
		this.midletName = midletName;
		this.attr = attr;
		if (midletName == null) {
			this.loader = ClassLoader.getSystemClassLoader();
		} else {
			start();
		}
	}

	public void run(Class<?> c) {
		try {
			MIDlet._setAttr(attr);
			this.m = (MIDlet) c.newInstance();
			m._startApp();
		} catch (InstantiationException ex) {
			throw new RuntimeException(ex);
		} catch (IllegalAccessException ex) {
			throw new RuntimeException(ex);
		}
	}

	public void run() {
		try {
			Class<?> c = loader.loadClass(getMidletClass().replace('/', '.'));
			run(c);
		} catch (Throwable t) {
			t.printStackTrace();
			throw new RuntimeException("Could not start the MIDlet. " + t);
		}
	}

	public void restart() {
		m._setDestroyListener(new Runnable() {
			@Override
			public void run() {
				MidletLoader.this.run();
			}
		});
		m._destroyApp();
	}

	private void start() {
		try {
			loader = new URLClassLoader(new URL[]{new File(midletName).toURI().toURL()}, ClassLoader.getSystemClassLoader());
			if (midletName.endsWith(".jar")) {
				JarFile midlet = new JarFile(midletName);
				Manifest manifest = midlet.getManifest();
				attr = manifest.getMainAttributes();
			}
			ClassPathHacker.addFile(midletName);
		} catch (Throwable t) {
			throw new RuntimeException("Could not start the MIDlet. " + t);
		}
	}

	public void stop() {
		ClassPathHacker.removeFile(midletName);
	}

	public String getAttribute(String name) {
		return attr.getValue(name);
	}

	private String getMidletClass() {
		return attr.getValue("MIDlet-1").split(",")[2].trim();
	}
}
