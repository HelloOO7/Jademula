package jademula.gui;

import jademula.ButtonListener;
import jademula.Handy;
import jademula.Jademula;

import java.awt.BorderLayout;
import java.awt.Canvas;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics2D;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferStrategy;

import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public class MainFrame {

	private static MainFrame instance;
	private int width, height;
	private Display display;
	private JFrame frame;
	private JFrame fullscreenFrame;
	private Canvas canvas;
	private BufferStrategy bs;
	private JPanel displayPanel, commandsPanel, fullscreenPanel;
	private JButton[] commands;
	private int commandIndex;
	private Graphics2D g;
	private Menu menu;

	private boolean fullscreenActive = false;
	private boolean fullscreenOpening;

	static final GraphicsDevice device = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();

	public static void init() {
		if (!SwingUtilities.isEventDispatchThread()) {
			throw new RuntimeException("This must be run from the EDT!");
		}
		instance = new MainFrame(Handy.getCurrent().getWidth() * Jademula.getZoom(), Handy.getCurrent().getHeight() * Jademula.getZoom());

		Action toggleFullscreenAction = new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Runnable r = () -> {
					if (instance.current != null) {
						instance.current._hide();
						if (isFullscreen()) {
							device.setFullScreenWindow(null);
							instance.fullscreenFrame.setVisible(false);
							instance.width = Handy.getCurrent().getWidth();
							instance.height = Handy.getCurrent().getHeight();
							instance.frame.setVisible(true);
						} else {
							instance.frame.setVisible(false);
							Dimension ss = Toolkit.getDefaultToolkit().getScreenSize();
							instance.width = ss.width;
							instance.height = ss.height;
							device.setFullScreenWindow(instance.fullscreenFrame);
							instance.fullscreenOpening = true;
						}
						instance.fullscreenActive = !instance.fullscreenActive;
						instance.updateCanvas();
					}
				};
				if (SwingUtilities.isEventDispatchThread()) {
					r.run();
				} else {
					SwingUtilities.invokeLater(r);
				}
			}
		};

		instance.frame.getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("F11"), "toggleFullScreen");
		instance.frame.getRootPane().getActionMap().put("toggleFullScreen", toggleFullscreenAction);
		instance.fullscreenFrame.getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("F11"), "toggleFullScreen");
		instance.fullscreenFrame.getRootPane().getActionMap().put("toggleFullScreen", toggleFullscreenAction);
	}

	private void deactivateDisplayable() {
		if (current != null) {
			current._deactivate();
			current = null;
		}
	}

	private void updateCanvas() {
		setDisplayable(current);
	}

	public void updateHandies() {
		menu.updateHandies();
	}

	private MainFrame(int width, int height) {
		//super("Jademula " + Jademula.getVersion());
		this.width = width;
		this.height = height;
		display = Display._create();
		setLookAndFeel();
		frame = new JFrame("Jademula " + Jademula.getVersion());
		fullscreenFrame = new JFrame("FullscreenCanvasFrame");
		//frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				device.setFullScreenWindow(null);
				fullscreenFrame.dispose();
				Jademula.exit();
			}
		});
		fullscreenFrame.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				Jademula.exit();
			}

			public void windowActivated(WindowEvent e) {
				if (fullscreenOpening) {
					fullscreenOpening = false;
					return;
				}
				if (isFullscreen()) {
					updateCanvas();
				}
			}
		});
		frame.setJMenuBar(menu = new Menu());
		FlowLayout lyt = new FlowLayout(FlowLayout.CENTER, 5, 0);
		displayPanel = new JPanel(lyt);
		displayPanel.setPreferredSize(new Dimension(width, height));
		lyt = new FlowLayout(FlowLayout.CENTER, 5, 0);
		fullscreenPanel = new JPanel(lyt);
		canvas = new Canvas();
		canvas.setIgnoreRepaint(true);
		canvas.setPreferredSize(new Dimension(width, height));
		displayPanel.add(canvas);
		frame.getContentPane().add(displayPanel);
		fullscreenFrame.getContentPane().add(fullscreenPanel);

		commandsPanel = new JPanel();
		commands = new JButton[2];
		commands[0] = new JButton("0");
		commands[1] = new JButton("1");
		//commands[2] = new JButton("2");
		commandsPanel.add(commands[0]);
		commandsPanel.add(commands[1]);
		//commandsPanel.add(commands[2]);
		frame.getContentPane().add(commandsPanel, BorderLayout.PAGE_END);

		frame.setResizable(false);
		frame.pack();
		canvas.createBufferStrategy(2);
		bs = canvas.getBufferStrategy();
		frame.pack();
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
	}

	public static MainFrame getInstance() {
		return instance;
	}

	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}

	public Display getDisplay() {
		return display;
	}

	public Graphics2D getGraphics() {
		if (g == null) {
			g = (Graphics2D) bs.getDrawGraphics();
		}
		return g;
	}

	public void flipBuffers() {
		bs.show();
	}

	private void addCommand(Command cmd) {
		commands[commandIndex].setText(cmd.getLabel());
		commands[commandIndex].repaint();
		++commandIndex;
	}

	private void removeCommands() {
		commandIndex = 0;
	}

	Displayable current;

	public void setDisplayable(Displayable displayable) {
		if (current != null) {
			current._deactivate();
		}
		current = null;
		removeCommands();
		if (displayable != null) {
			if (displayable._getCommands().length > 0) {
				addCommand(displayable._getCommands()[0]);
			}
			if (displayable._getCommands().length > 1) {
				addCommand(displayable._getCommands()[1]);
			}
			if (displayable._getListener() != null) {
				if (displayable._getCommands().length > 0) {
					commands[0].removeAll();
					commands[0].addActionListener(
							new ButtonListener(displayable, displayable._getCommands()[0], displayable._getListener())
					);
				}
				if (displayable._getCommands().length > 1) {
					commands[1].removeAll();
					commands[1].addActionListener(
							new ButtonListener(displayable, displayable._getCommands()[1], displayable._getListener())
					);
				}
			}
		}
		displayPanel.removeAll();
		fullscreenPanel.removeAll();
		current = displayable;
		if (current != null) {
			if (isFullscreen()) {
				current._activate(fullscreenPanel);
			} else {
				current._activate(displayPanel);
				frame.pack();
			}
		}
	}

	private static boolean isFullscreen() {
		return instance.fullscreenActive;
	}

	private static void setLookAndFeel() {
		//JFrame.setDefaultLookAndFeelDecorated(true);
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}

	public JFrame getFrame() {
		return frame;
	}

	public void setInternalResolution(int width, int height) {
		setDisplaySize(width * Jademula.getZoom(), height * Jademula.getZoom());
	}

	public void setDisplaySize(int width, int height) {
		if (width == this.width && height == this.height) {
			return;
		}
		if (current != null) {
			current._deactivate();
		}
		this.width = width;
		this.height = height;
		displayPanel.setPreferredSize(new Dimension(width, height));
		displayPanel.setSize(displayPanel.getPreferredSize());
		//if (current != null) current._resize(width, height);
		frame.pack();
		if (current != null) {
			current._activate(displayPanel);
		} else {
			System.err.println("current is null");
		}

		//System.err.println("DPX: " + displayPanel.getWidth());
		/*canvas.setPreferredSize(new Dimension(width, height));
		canvas.setSize(new Dimension(width, height));
		frame.pack();
		//displayPanel.removeAll();
		//displayPanel.add(canvas);
		//canvas.setPreferredSize(new Dimension(width, height));
		//bs.dispose();
		//canvas.addNotify();
		canvas.createBufferStrategy(2);
		bs = canvas.getBufferStrategy();
		 */
 /*displayPanel.removeAll();
		canvas = new Canvas();
		canvas.setIgnoreRepaint(true);
		canvas.setPreferredSize(new Dimension(width, height));
		displayPanel.add(canvas);
		canvas.createBufferStrategy(2);
		bs = canvas.getBufferStrategy();*/
	}
}
