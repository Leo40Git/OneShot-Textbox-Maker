package com.leo.ostbm;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

public class LoadFrame extends JFrame {

	private static final long serialVersionUID = 1L;

	private JLabel loadLabel;

	public void setLoadString(String loadString) {
		loadLabel.setText(loadString);
	}

	public LoadFrame(String loadString, boolean important) {
		setDefaultCloseOperation((important ? JFrame.EXIT_ON_CLOSE : JFrame.DO_NOTHING_ON_CLOSE));
		setUndecorated(true);
		final Dimension size = new Dimension(320, 120);
		setPreferredSize(size);
		setMaximumSize(size);
		setMinimumSize(size);
		setResizable(false);
		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());
		panel.setBorder(BorderFactory.createLineBorder(MakerPanel.COLOR_TEXTBOX_B, 4));
		panel.setBackground(MakerPanel.COLOR_TEXTBOX);
		loadLabel = new JLabel(loadString);
		loadLabel.setFont(loadLabel.getFont().deriveFont(Font.BOLD, 20));
		loadLabel.setHorizontalAlignment(SwingConstants.CENTER);
		loadLabel.setVerticalAlignment(SwingConstants.CENTER);
		loadLabel.setForeground(Color.WHITE);
		panel.add(loadLabel, BorderLayout.CENTER);
		add(panel);
		pack();
		setLocationRelativeTo(null);
		setIconImages(Resources.getAppIcons());
		setVisible(true);
		requestFocus();
	}

	public LoadFrame(boolean important) {
		this("Checking for updates...", important);
	}

}
