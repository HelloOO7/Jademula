package javax.microedition.lcdui;

import jademula.Jademula;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.util.ArrayList;

import javax.imageio.ImageIO;


public class Image {
	private BufferedImage original;
	
	private final BufferedImage[] scaled = new BufferedImage[1 + 4];
	
	private int width, height;
	private boolean hasToUpdate = false;

	public static Image createImage(int width, int height) {
		return new Image(width, height);
	}

	public static Image createImage(Image source) {
		return new Image(source);
	}

	public static Image createImage(String name) throws IOException {
		if (!name.startsWith("/")) name = "/" + name;
		InputStream stream = "".getClass().getResourceAsStream(name);
		try {
			return new Image(ImageIO.read(stream));
		}
		catch (IOException ex) {
			System.err.println("Unexpected IOException in Image.createImage()");
			throw ex;
		}
	}

	public static Image createImage(byte[] imageData, int imageOffset, int imageLength) {
		try {
			return new Image(ImageIO.read(new ByteArrayInputStream(imageData, imageOffset, imageLength)));
		}
		catch (IOException ex) {
			System.err.println("Unexpected IOException in Image.createImage()");
			throw new IllegalArgumentException();
		}
	}

	public static Image createImage(Image image, int x, int y, int width, int height, int transform) {
		return new Image(image);
	}

	public static Image createImage(java.io.InputStream stream) throws IOException {
		try {
			return new Image(ImageIO.read(stream));
		}
		catch (IOException ex) {
			System.err.println("Unexpected IOException in Image.createImage()");
			throw ex;
		}
	}

	public Graphics getGraphics() {
		hasToUpdate = true;
		return Graphics._create(original.createGraphics(), false);
	}

	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}

	public boolean isMutable() {
		return true;
	}

	public static Image createRGBImage(int[] rgb, int width, int height, boolean processAlpha) {
		if (!processAlpha) {
			int[] rgbnew = new int[rgb.length];
			for (int i = 0; i < rgb.length; ++i) rgbnew[i] = rgb[i] | 0xff000000;
			rgb = rgbnew;
		}
		return new Image(rgb, width, height);
	}

	public void getRGB(int[] rgbData, int offset, int scanlength, int x, int y, int width, int height) {
		//System.err.println("Image.getRGB() not implemented.");
		original.getRGB(x, y, width, height, rgbData, offset, scanlength);
	}
	
	private Image(BufferedImage image) {
		width = image.getWidth();
		height = image.getHeight();
		this.original = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		this.original.getGraphics().drawImage(image, 0, 0, null);
	}
	
	private Image(Image source) {
		this.original = source.original;
		this.width = source.width;
		this.height = source.height;
	}

	private Image(int width, int height) {
		original = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		this.width = width;
		this.height = height;
	}
	
	private Image(int[] rgb, int width, int height) {
		this(width, height);
		original.setRGB(0, 0, width, height, rgb, 0, width);
	}
	
	private void _createScaled(int zoom) {
		BufferedImage bi = scaled[zoom] == null ? new BufferedImage(width * zoom, height * zoom, BufferedImage.TYPE_INT_ARGB) : scaled[zoom];
		bi.getGraphics().drawImage(original.getScaledInstance(original.getWidth() * zoom, original.getHeight() * zoom, 0), 0, 0, null);
		scaled[zoom] = bi;
	}
	
	private void _ensureScaledInstance(int zoom) {
		if (scaled[zoom] == null) {
			_createScaled(zoom);
		}
	}
	
	public BufferedImage _getImage(int zoom) {
		if (hasToUpdate) {
			_createScaled(zoom);
			hasToUpdate = false;
		}
		else {
			_ensureScaledInstance(zoom);
		}
		return scaled[zoom];
	}
	
	public static Image _createImage(BufferedImage image) {
		return new Image(image);
	}
}