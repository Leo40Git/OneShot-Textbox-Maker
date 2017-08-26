package com.leo.ostbm;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
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

	public static final Color COLOR_TEXTBOX = Color.decode("0x180C1E");
	public static final Color COLOR_TEXTBOX_B = COLOR_TEXTBOX.brighter().brighter();

	private JComboBox<String> faceSelect;
	private JButton customFaceButton;
	private JTextArea textArea;
	private JButton makeTextboxButton;

	public MakerPanel() {
		setLayout(new BorderLayout());
		JPanel faceSelectPanel = new JPanel();
		faceSelectPanel.setLayout(new BoxLayout(faceSelectPanel, BoxLayout.LINE_AXIS));
		faceSelectPanel.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
		faceSelectPanel.add(new JLabel("Face: "));
		faceSelect = new JComboBox<String>();
		faceSelect.setModel(new DefaultComboBoxModel<>(Resources.getFaces()));
		faceSelect.setBackground(COLOR_TEXTBOX);
		faceSelect.setForeground(Color.WHITE);
		FacesComboBoxRenderer renderer = new FacesComboBoxRenderer();
		faceSelect.setRenderer(renderer);
		faceSelectPanel.add(faceSelect);
		customFaceButton = new JButton("...");
		customFaceButton.addActionListener(this);
		faceSelectPanel.add(customFaceButton);
		add(faceSelectPanel, BorderLayout.PAGE_START);
		textArea = new JTextArea();
		textArea.setFont(Resources.getFont());
		textArea.setBackground(COLOR_TEXTBOX);
		textArea.setForeground(Color.WHITE);
		textArea.setCaretColor(Color.WHITE);
		add(textArea, BorderLayout.CENTER);
		makeTextboxButton = new JButton("Make a Textbox!");
		makeTextboxButton.addActionListener(this);
		add(makeTextboxButton, BorderLayout.PAGE_END);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		Object src = e.getSource();
		if (src == makeTextboxButton) {
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
					String selName = sel.getName();
					if (!selName.contains(".") || !selName.substring(selName.lastIndexOf(".") + 1, selName.length())
							.equalsIgnoreCase("png")) {
						selName += ".png";
						sel = new File(sel.getParentFile().getPath() + "/" + selName);
					}
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
		} else if (src == customFaceButton) {
			JFileChooser fc = new JFileChooser();
			fc.setMultiSelectionEnabled(true);
			fc.setAcceptAllFileFilterUsed(false);
			fc.setDialogTitle("Open face image(s)");
			fc.setFileFilter(new FileNameExtensionFilter("PNG files", "png"));
			fc.setCurrentDirectory(new File(System.getProperty("user.dir")));
			int ret = fc.showOpenDialog(this);
			if (ret == JFileChooser.APPROVE_OPTION) {
				File[] sels = fc.getSelectedFiles();
				for (File sel : sels) {
					String faceName = sel.getName();
					faceName = faceName.substring(0, faceName.lastIndexOf('.'));
					try {
						BufferedImage image = ImageIO.read(sel);
						if (image.getWidth() != 96 || image.getHeight() != 96) {
							JOptionPane.showMessageDialog(this, "Face must be 96 by 96!", "Bad face dimensions!",
									JOptionPane.ERROR_MESSAGE);
							return;
						}
						Resources.addFace(faceName, image);
						faceSelect.setModel(new DefaultComboBoxModel<>(Resources.getFaces()));
						faceSelect.setSelectedIndex(faceSelect.getModel().getSize() - 1);
					} catch (IOException e1) {
						e1.printStackTrace();
						JOptionPane.showMessageDialog(this, "An exception occured while loading the face:\n" + e1,
								"Couldn't load face!", JOptionPane.ERROR_MESSAGE);
					}
				}
			}
		}
	}

	public static BufferedImage drawTextbox(String face, String text) {
		BufferedImage ret = new BufferedImage(608, 128, BufferedImage.TYPE_INT_ARGB);
		Graphics g = ret.getGraphics();
		g.drawImage(Resources.getBox(), 0, 0, null);
		BufferedImage faceImage = Resources.getFace(face);
		if (faceImage != null)
			g.drawImage(faceImage, 496, 16, null);
		g.setFont(Resources.getFont());
		g.setColor(Color.WHITE);
		drawString(g, text, 20, 10);
		return ret;
	}

	public static void drawString(Graphics g, String str, int x, int y) {
		final int lineSpace = g.getFontMetrics().getHeight() + 1;
		for (String line : str.split("\n")) {
			y += lineSpace;
			g.drawString(line, x, y);
		}
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
			if (isSelected)
				setBackground(COLOR_TEXTBOX_B);
			else
				setBackground(COLOR_TEXTBOX);
			setForeground(Color.WHITE);
			ImageIcon faceIcon = Resources.getFaceIcon(value);
			if (faceIcon != null)
				setIcon(faceIcon);
			setText(value);
			return this;
		}

	}

}
