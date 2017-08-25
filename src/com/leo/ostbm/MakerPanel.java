package com.leo.ostbm;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.ListCellRenderer;
import javax.swing.filechooser.FileNameExtensionFilter;

public class MakerPanel extends JPanel implements ActionListener {

	private static final long serialVersionUID = 1L;

	private JComboBox<String> faceSelect;
	private JTextArea textArea;
	private JButton makeTextboxButton;

	public MakerPanel() {
		setLayout(new BorderLayout());
		JPanel faceSelectPanel = new JPanel();
		faceSelectPanel.setLayout(new BoxLayout(faceSelectPanel, BoxLayout.LINE_AXIS));
		faceSelectPanel.add(new JLabel("Face: "));
		faceSelect = new JComboBox<String>();
		faceSelect.setModel(new DefaultComboBoxModel<>(Resources.getFaces()));
		FacesComboBoxRenderer renderer = new FacesComboBoxRenderer();
		faceSelect.setRenderer(renderer);
		faceSelectPanel.add(faceSelect);
		add(faceSelectPanel, BorderLayout.PAGE_START);
		textArea = new JTextArea();
		textArea.setFont(Resources.getFont());
		add(textArea, BorderLayout.CENTER);
		makeTextboxButton = new JButton("Make a Textbox!");
		makeTextboxButton.addActionListener(this);
		add(makeTextboxButton, BorderLayout.PAGE_END);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (!(e.getSource() == makeTextboxButton))
			return;
		String text = textArea.getText().trim();
		if (text.isEmpty()) {
			JOptionPane.showMessageDialog(this, "Please enter some text.", "Text cannot be blank!",
					JOptionPane.ERROR_MESSAGE);
			return;
		}
		if (text.split("\n").length > 4) {
			JOptionPane.showMessageDialog(this, "Only 4 lines per textbox, please!", "Too many lines!",
					JOptionPane.ERROR_MESSAGE);
			return;
		}
		BufferedImage textbox = drawTextbox(faceSelect.getItemAt(faceSelect.getSelectedIndex()), text);
		int result = JOptionPane.showOptionDialog(this, new PreviewPanel(textbox), "Textbox preview", 0,
				JOptionPane.PLAIN_MESSAGE, null, new String[] { "Save" }, "Save");
		if (result == 0) {
			JFileChooser fc = new JFileChooser();
			fc.setMultiSelectionEnabled(false);
			fc.setAcceptAllFileFilterUsed(false);
			fc.setDialogTitle("Save textbox image");
			fc.setFileFilter(new FileNameExtensionFilter("PNG files", "png"));
			fc.setCurrentDirectory(new File(System.getProperty("user.dir")));
			int ret = fc.showSaveDialog(this);
			if (ret == JFileChooser.APPROVE_OPTION) {
				File sel = fc.getSelectedFile();
				String selName = sel.toString();
				if (!selName.contains(".") || !selName.substring(0, selName.lastIndexOf('.')).equalsIgnoreCase("png"))
					sel = new File(selName + ".png");
				try {
					System.out.println("writing image to " + sel);
					ImageIO.write(textbox, "png", sel);
				} catch (IOException e1) {
					e1.printStackTrace();
					JOptionPane.showMessageDialog(this, "An exception occured while saving the image:\n" + e1,
							"Couldn't save image!", JOptionPane.ERROR_MESSAGE);
				}
			}
		}
	}

	public static BufferedImage drawTextbox(String face, String text) {
		BufferedImage ret = new BufferedImage(608, 128, BufferedImage.TYPE_INT_ARGB);
		Graphics g = ret.getGraphics();
		drawNineSlice(g, Resources.getBox(), 0, 0, 608, 128);
		g.setFont(Resources.getFont());
		g.setColor(Color.WHITE);
		drawString(g, text, 20, 10);
		g.drawImage(Resources.getFace(face), 496, 16, null);
		return ret;
	}

	public static void drawString(Graphics g, String str, int x, int y) {
		final int lineSpace = g.getFontMetrics().getHeight() + 1;
		for (String line : str.split("\n")) {
			y += lineSpace;
			g.drawString(line, x, y);
		}
	}

	public static void drawNineSlice(Graphics g, Image img, int x, int y, int w, int h) {
		final int iw = img.getWidth(null), ih = img.getHeight(null);
		final int sw = iw / 3, sh = ih / 3;
		// top left
		g.drawImage(img, x, y, x + sw, y + sh, 0, 0, sw, sh, null);
		// top middle
		g.drawImage(img, x + sw, y, x + (w - sw), y + sh, sw, 0, sw * 2, sh, null);
		// top right
		g.drawImage(img, x + (w - sw), y, x + w, y + sh, sw * 2, 0, sw * 3, sh, null);
		// center left
		g.drawImage(img, x, y + sh, x + sw, y + (h - sh), 0, sh, sw, sh * 2, null);
		// center middle
		g.drawImage(img, x + sw, y + sh, x + (w - sw), y + (h - sh), sw, sh, sw * 2, sh * 2, null);
		// center right
		g.drawImage(img, x + (w - sw), y + sh, x + w, y + (h - sh), sw * 2, sh, sw * 3, sh * 2, null);
		// bottom left
		g.drawImage(img, x, y + (h - sh), x + sw, y + h, 0, sh * 2, sw, sh * 3, null);
		// bottom middle
		g.drawImage(img, x + sw, y + (h - sh), x + (w - sw), y + h, sw, sh * 2, sw * 2, sh * 3, null);
		// bottom right
		g.drawImage(img, x + (w - sw), y + (h - sh), x + w, y + h, sw * 2, sh * 2, sw * 3, sh * 3, null);
	}

	class FacesComboBoxRenderer extends JLabel implements ListCellRenderer<String> {

		private static final long serialVersionUID = 1L;

		public FacesComboBoxRenderer() {
			setOpaque(true);
			setHorizontalAlignment(LEFT);
			setVerticalAlignment(CENTER);
		}

		@Override
		public Component getListCellRendererComponent(JList<? extends String> list, String value, int index,
				boolean isSelected, boolean cellHasFocus) {
			if (isSelected) {
				setBackground(list.getSelectionBackground());
				setForeground(list.getSelectionForeground());
			} else {
				setBackground(list.getBackground());
				setForeground(list.getForeground());
			}
			setIcon(Resources.getFaceIcon(value));
			setText(value);
			return this;
		}

	}

}
