package javax.microedition.lcdui;

import jademula.Jademula;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.lang.ref.WeakReference;
import java.util.ArrayList;

import javax.microedition.lcdui.game.Sprite;


public class Graphics {
	public static final int HCENTER = 1;
	public static final int VCENTER = 2;
	public static final int LEFT = 4;
	public static final int RIGHT = 8;
	public static final int TOP = 16;
	public static final int BOTTOM = 32;
	public static final int BASELINE = 64;
	public static final int SOLID = 0;
	public static final int DOTTED = 1;
	
	private Graphics2D g2d;
	private final boolean useZoom;
	private Font font = Font.getFont(0, 0, 0);
	private int cx, cy, cw, ch;
	private int tx, ty;
	private int strokeStyle;
	
	public void translate(int x, int y) {
		tx = x;
		ty = y;
		g2d.translate(x * getZoom(), y * getZoom());
	}
	
	public int getTranslateX() {
		return tx;
	}

	public int getTranslateY() {
		return ty;
	}

	public int getColor() {
		return g2d.getColor().getRGB();
	}

	public int getRedComponent() {
		return g2d.getColor().getRed();
	}

	public int getGreenComponent() {
		return g2d.getColor().getGreen();
	}

	public int getBlueComponent() {
		return g2d.getColor().getBlue();
	}

	public int getGrayScale() {
		return (getRedComponent() + getGreenComponent() + getBlueComponent()) / 3;
	}

	public void setColor(int red, int green, int blue) {
		g2d.setColor(new Color(red, green, blue));
	}

	public void setColor(int RGB) {
		g2d.setColor(new Color(RGB));
	}

	public void setGrayScale(int value) {
		setColor(value, value, value);
	}

	public Font getFont() {
		return font;
	}

	public void setStrokeStyle(int style) {
		System.err.println("Graphics.strokeStyle not used.");
		strokeStyle = style;
	}

	public int getStrokeStyle() {
		return strokeStyle;
	}

	public void setFont(Font font) {
		this.font = font;
		g2d.setFont(font._getFont());
	}

	public int getClipX() {
		return cx;
	}

	public int getClipY() {
		return cy;
	}

	public int getClipWidth() {
		return cw;
	}

	public int getClipHeight() {
		return ch;
	}

	public void clipRect(int x, int y, int width, int height) { //Hä?
		g2d.clipRect(x * getZoom(), y * getZoom(), width * getZoom(), height * getZoom());
	}

	public void setClip(int x, int y, int width, int height) {
		cx = x; cy = y; cw = width; ch = height;
		g2d.setClip(x * getZoom(), y * getZoom(), width * getZoom(), height * getZoom());
	}

	public void drawLine(int x1, int y1, int x2, int y2) {
		g2d.drawLine(x1 * getZoom(), y1 * getZoom(), x2 * getZoom(), y2 * getZoom());
	}

	public void fillRect(int x, int y, int width, int height) {
		g2d.fillRect(x * getZoom(), y * getZoom(), width * getZoom(), height * getZoom());
	}

	public void drawRect(int x, int y, int width, int height) {
		g2d.drawRect(x * getZoom(), y * getZoom(), width * getZoom(), height * getZoom());
	}

	public void drawRoundRect(int x, int y, int width, int height, int arcWidth, int arcHeight) {
		g2d.drawRoundRect(x * getZoom(), y * getZoom(),
				width * getZoom(), height * getZoom(),
				arcWidth * getZoom(), arcHeight * getZoom());
	}

	public void fillRoundRect(int x, int y, int width, int height, int arcWidth, int arcHeight) {
		g2d.fillRoundRect(x * getZoom(), y * getZoom(),
				width * getZoom(), height * getZoom(),
				arcWidth * getZoom(), arcHeight * getZoom());
	}

	public void fillArc(int x, int y, int width, int height, int startAngle, int arcAngle) {
		g2d.fillArc(x * getZoom(), y * getZoom(), width * getZoom(), height * getZoom(), startAngle, arcAngle);
	}

	public void drawArc(int x, int y, int width, int height, int startAngle, int arcAngle) {
		g2d.drawArc(x * getZoom(), y * getZoom(), width * getZoom(), height * getZoom(), startAngle, arcAngle);
	}

	public void drawString(String str, int x, int y, int anchor) {
		g2d.drawString(
				str,
				x * getZoom() - xanchor(anchor, g2d.getFontMetrics().stringWidth(str)),
				y * getZoom() + g2d.getFontMetrics().getAscent() - yanchor(anchor, g2d.getFontMetrics().getHeight())
		);
	}

	public void drawSubstring(String str, int offset, int len, int x, int y, int anchor) {
		drawString(str.substring(offset, offset + len), x, y, anchor);
	}

	public void drawChar(char character, int x, int y, int anchor) {
		drawString("" + character, x, y, anchor);
	}

	public void drawChars(char[] data, int offset, int length, int x, int y, int anchor) {
		drawString(new String(data, offset, length), x, y, anchor);
	}

	public void drawImage(Image img, int x, int y, int anchor) {
		if (img != null) {
			g2d.drawImage(
				img._getImage(getZoom()),
				x * getZoom() - xanchor(anchor, img.getWidth() * getZoom()),
				y * getZoom() - yanchor(anchor, img.getHeight() * getZoom()),
				null
			);
		}
	}

	public void drawRegion(Image src, int x_src, int y_src, int width, int height, int transform, int x_dest, int y_dest, int anchor) {
		if (src != null) {
			Sprite sprite = new Sprite(null, width, height);
			if (transform != 0) sprite.setTransform(transform);
			sprite.setPosition(
				x_dest - xanchor(anchor, width),
				y_dest - yanchor(anchor, height));
			AffineTransform t = sprite._getTransform(getZoom());
			AffineTransform oldT = g2d.getTransform();
			g2d.setTransform(t);
			int zoom = getZoom();
			g2d.drawImage(src._getImage(getZoom()), 0, 0, width * zoom, height * zoom, x_src * zoom, y_src * zoom, (x_src + width) * zoom, (y_src + height) * zoom, null);
			g2d.setTransform(oldT);
		}
	}

	public void copyArea(int x_src, int y_src, int width, int height, int x_dest, int y_dest, int anchor) {
		System.err.println("Graphics.copyArea not implemented.");
	}

	public void fillTriangle(int x1, int y1, int x2, int y2, int x3, int y3) {
		g2d.fillPolygon(
				new int[]{x1 * getZoom(), x2 * getZoom(), x3 * getZoom()},
				new int[]{y1 * getZoom(), y2 * getZoom(), y3 * getZoom()}, 3);
	}

	public void drawRGB(int[] rgbData, int offset, int scanlength, int x, int y, int width, int height, boolean processAlpha) {
		BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		img.setRGB(0, 0, width, height, rgbData, offset, scanlength);
		/*for (int ix = 0; ix < width; ++ix) {
			for (int iy = 0; iy < height; ++iy) {
				img.setRGB(ix, iy, rgbData[offset + ix + iy * scanlength]);
			}
		}*/
		int z = getZoom();
		g2d.drawImage(img.getScaledInstance(width * z, height * z, 0), x * z, y * z, null);
	}

	public int getDisplayColor(int color) {
		return color;
	}
	
	private Graphics(Graphics2D g2d, boolean zoom) {
		this.g2d = g2d;
		this.useZoom = zoom;
		g2d.setStroke(new BasicStroke(getZoom()));
		graphicsInstances.add(new WeakReference<Graphics>(this));
	}
	
	public static Graphics _create(Graphics2D g2d, boolean zoom) {
		return new Graphics(g2d, zoom);
	}
	
	private int yanchor(int anchor, int height) {
		if ((anchor & VCENTER) != 0) return height / 2;
		if ((anchor & BASELINE) != 0) System.err.println("Baseline");
		if ((anchor & BOTTOM) != 0) return height;
		else return 0;
	}
	
	private int xanchor(int anchor, int width) {
		if ((anchor & HCENTER) != 0) return width / 2;
		if ((anchor & RIGHT) != 0) return width;
		return 0;
	}
	
	public Graphics2D _getGraphics() {
		return g2d;
	}
	
	public int getZoom() {
		return useZoom ? Jademula.getZoom() : 1;
	}
	
	public static void _updateSize() {
		for (int i = 0; i < graphicsInstances.size(); ++i) {
			if (graphicsInstances.get(i).get() == null) {
				graphicsInstances.remove(i);
				--i;
				continue;
			}
			Graphics g = graphicsInstances.get(i).get();
			g.setFont(g.font);
		}
	}
	
	private static java.util.List<WeakReference<Graphics>> graphicsInstances = new ArrayList<WeakReference<Graphics>>();
}