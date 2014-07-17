package org.ubimon.server.station;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;

import javax.swing.JPanel;

import org.ubimon.server.ResourceDatabase;
import org.ubimon.server.model.Ubimon;
import org.ubimon.server.model.UbimonData;

@SuppressWarnings("serial")
public class UbimonViewer extends JPanel {
	public static final int BORDER_WIDTH = 2;
	public static final int LEFT = 1;
	public static final int RIGHT = 2;
	public static final int UP = 4;
	public static final int DOWN = 8;

	private static final Font pokemonFont = ResourceDatabase.loadFont("pokemongb.ttf");

	private UbimonIcon[] icons;
	private Dimension iconsBounds;
	private UbimonIcon selected;

	public UbimonViewer() {
		super();
	}

	public Ubimon getSelectedUbimon() {
		if (selected != null)
			return selected.ubimon;

		return null;
	}

	@Override
	public void paint(Graphics g) {
		super.paint(g);

		if ((icons != null) && (icons.length > 0)) {
			Rectangle view = getVisibleRect();
			Graphics2D g2d = (Graphics2D) g;
			g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			g2d.setFont(propFont());

			g2d.translate(view.width / 2 - iconsBounds.width / 2, 0);
			AffineTransform baseTransform = g2d.getTransform();
			Stroke baseStroke = new BasicStroke(1f);
			for (UbimonIcon icon : icons) {
				g2d.setStroke(baseStroke);

				// Draws image.
				int imgx = icon.canvas.x + icon.canvas.width / 2 - icon.screenSize.width / 2;
				int imgy = icon.canvas.y + icon.insets.top + BORDER_WIDTH;
				g2d.drawImage(icon.image, imgx, imgy, icon.screenSize.width, icon.screenSize.height, null);

				// Draws label.
				TextLayout label = new TextLayout(icon.ubimon.getName(), g2d.getFont(), g2d.getFontRenderContext());
				Shape outline = label.getOutline(null);
				Rectangle labelBounds = outline.getBounds();
				AffineTransform t = new AffineTransform(baseTransform);
				t.translate(
						icon.canvas.x + icon.canvas.width / 2 - labelBounds.width / 2,
						imgy + icon.screenSize.height + labelBounds.height);
				g2d.setTransform(t);
				g2d.setColor(Color.white);
				g2d.drawString(icon.ubimon.getName(), 0, 0);
				// g2d.setColor(Color.black);
				// g2d.draw(outline);
				g2d.setTransform(baseTransform);

				// Draws border.
				if (icon == selected) {
					int left = icon.canvas.x + icon.insets.left;
					int right = icon.canvas.x + icon.canvas.width - icon.insets.right;
					int top = icon.canvas.y + icon.insets.top;
					int bottom = icon.canvas.y + icon.canvas.height - icon.insets.bottom;

					g2d.setColor(Color.red);
					g2d.setStroke(new BasicStroke(BORDER_WIDTH));
					g2d.drawLine(left, top, right, top);
					g2d.drawLine(right, top, right, bottom);
					g2d.drawLine(right, bottom, left, bottom);
					g2d.drawLine(left, bottom, left, top);
				}
			}
		}
	}

	public void moveCursorLeft() {
		if ((selected != null) && (selected.canvas.left != null)) {
			selected = selected.canvas.left;
			repaint();
		}
	}

	public void moveCursorRight() {
		if ((selected != null) && (selected.canvas.right != null)) {
			selected = selected.canvas.right;
			repaint();
		}
	}

	public void moveCursorDown() {
		if ((selected != null) && (selected.canvas.down != null)) {
			selected = selected.canvas.down;
			repaint();
		}
	}

	public void moveCursorUp() {
		if ((selected != null) && (selected.canvas.up != null)) {
			selected = selected.canvas.up;
			repaint();
		}
	}

	public void setIcons(UbimonIcon[] icons) {
		this.icons = icons;
		this.iconsBounds = new Dimension(0, 0);
		this.selected = null;

		if ((icons != null) && (icons.length > 0)) {
			int xmin = Integer.MAX_VALUE, ymin = Integer.MAX_VALUE;
			int xmax = 0, ymax = 0;
			for (UbimonIcon icon : icons) {
				xmin = Math.min(xmin, icon.canvas.x);
				xmax = Math.max(xmax, icon.canvas.x + icon.canvas.width);
				ymin = Math.min(ymin, icon.canvas.y);
				ymax = Math.max(ymax, icon.canvas.y + icon.canvas.height);
			}

			this.iconsBounds = new Dimension(xmax - xmin, ymax - ymin);
			this.selected = icons[0];
		}
	}

	public UbimonIcon[] fit(List<Ubimon> list, Ubimon toAdd) {
		// First, stores all elements in an array and descending sorts
		// largest screen dimension.
		UbimonIcon[] result = sortedIconArray(list, toAdd);

		// Uses a greedy algorithm to fit all the icons in the available screen.
		PriorityQueue<UbimonIconCanvas> heap = new PriorityQueue<UbimonIconCanvas>(new Comparator<UbimonIconCanvas>() {
			public int compare(UbimonIconCanvas o1, UbimonIconCanvas o2) {
				return Integer.compare(o2.area(), o1.area());
			}
		});
		Rectangle view = getVisibleRect();
		heap.add(new UbimonIconCanvas(0, 0, view.width, view.height));
		for (int i = 0; i < result.length; ++i) {
			UbimonIconCanvas canvas = result[i].canvas;

			// Gets the largest available region...
			UbimonIconCanvas r = heap.poll();
			if (!r.fits(canvas.width, canvas.height))
				return null;

			// Uses this region's position and links to its parents...
			canvas.x = r.x;
			canvas.y = r.y;
			if (r.left != null) {
				canvas.left = r.left;
				if (canvas.left.canvas.right == null)
					canvas.left.canvas.right = result[i];
			}
			if (r.up != null) {
				canvas.up = r.up;
				if (canvas.up.canvas.down == null)
					canvas.up.canvas.down = result[i];
			}

			// Splits the largest region in remaining spaces...
			UbimonIconCanvas right = new UbimonIconCanvas(
					r.x + canvas.width, r.y, r.width - canvas.width, r.height);
			right.left = result[i];
			heap.add(right);
			UbimonIconCanvas down = new UbimonIconCanvas(
					r.x, r.y + canvas.height, canvas.width, r.height - canvas.height);
			down.up = result[i];
			heap.add(down);
		}

		return result;
	}

	private UbimonIcon[] sortedIconArray(List<Ubimon> list, Ubimon toAdd) {
		UbimonIcon[] result = new UbimonIcon[list.size() + ((toAdd != null) ? 1 : 0)];
		int i = 0;
		for (Ubimon ubimon : list)
			result[i++] = icon(ubimon);
		if (i < result.length)
			result[i] = icon(toAdd);
		Arrays.sort(result, new Comparator<UbimonIcon>() {
			public int compare(UbimonIcon o1, UbimonIcon o2) {
				// Inverse comparison.
				return Integer.compare(o2.canvas.area(), o1.canvas.area());
			}
		});

		return result;
	}

	public UbimonIcon icon(Ubimon ubimon) {
		UbimonIcon ui = new UbimonIcon();

		ui.ubimon = ubimon;
		ui.image = UbimonData.byName(ubimon.getPrototype()).getSprite();
		float prop = prop();
		ui.screenSize = new Dimension(
				(int) Math.floor(prop * ui.image.getWidth()), (int) Math.floor(prop * ui.image.getHeight()));
		ui.insets = new Insets(3, 3, 3, 3);

		Graphics2D g2d = (Graphics2D) getGraphics();
		TextLayout label = new TextLayout(ubimon.getName(), propFont(), g2d.getFontRenderContext());
		Rectangle labelBounds = label.getOutline(null).getBounds();
		ui.canvas = new UbimonIconCanvas(
				0, 0,
				Math.max(labelBounds.width, ui.screenSize.width) + ui.insets.left + ui.insets.right + 2 * BORDER_WIDTH,
				ui.screenSize.height + labelBounds.height + ui.insets.top + ui.insets.bottom + 2 * BORDER_WIDTH);

		return ui;
	}

	public float prop() {
		return (float) getVisibleRect().height / UbimonData.refResolution.height;
	}

	private Font propFont() {
		return pokemonFont.deriveFont(Font.BOLD, UbimonData.refFontSize * prop());
	}
}
