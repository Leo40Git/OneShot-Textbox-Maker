package com.leo.ostbm;

import java.awt.BorderLayout;
import java.awt.image.BufferedImage;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class PreviewPanel extends JPanel {

	private static final long serialVersionUID = 1L;

	private ImageIcon image;

	public PreviewPanel(BufferedImage image) {
		this.image = new ImageIcon(image);
		setLayout(new BorderLayout());
		add(new JLabel(this.image), BorderLayout.CENTER);
	}
}
