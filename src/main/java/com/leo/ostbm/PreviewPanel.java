package com.leo.ostbm;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.filechooser.FileNameExtensionFilter;

public class PreviewPanel extends JPanel implements ActionListener {

	private static final long serialVersionUID = 1L;

	public static final String A_SAVE_TEXTBOX = "saveTextbox";

	private BufferedImage image;
	private ImageIcon previewImage;
	private JButton saveButton;

	public PreviewPanel(BufferedImage image) {
		this.image = image;
		previewImage = new ImageIcon(image);
		setLayout(new BorderLayout());
		JScrollPane previewScroll = new JScrollPane(new JLabel(previewImage));
		add(previewScroll, BorderLayout.CENTER);
		saveButton = new JButton("Save");
		saveButton.addActionListener(this);
		saveButton.setActionCommand(A_SAVE_TEXTBOX);
		add(saveButton, BorderLayout.PAGE_END);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (A_SAVE_TEXTBOX.equals(e.getActionCommand())) {
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
				if (!selName.contains(".")
						|| !selName.substring(selName.lastIndexOf(".") + 1, selName.length()).equalsIgnoreCase("png")) {
					selName += ".png";
					sel = new File(sel.getParentFile().getPath() + "/" + selName);
				}
				try {
					ImageIO.write(image, "png", sel);
				} catch (IOException e1) {
					e1.printStackTrace();
					JOptionPane.showMessageDialog(this, "An exception occured while saving the image:\n" + e1,
							"Couldn't save image!", JOptionPane.ERROR_MESSAGE);
				}
			}
		}
	}
}
