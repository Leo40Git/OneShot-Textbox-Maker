package com.leo.ostbm;

import java.awt.Dimension;

import javax.swing.JFrame;

public class Main {

	private static JFrame frame;

	public static void main(String[] args) {
		try {
			Resources.initFont();
		} catch (Exception e) {
			System.err.println("font loading failed");
			e.printStackTrace();
		}
		try {
			Resources.initImages();
		} catch (Exception e) {
			System.err.println("image loading failed");
			e.printStackTrace();
		}
		frame = new JFrame();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		final Dimension size = new Dimension(640, 480);
		frame.setPreferredSize(size);
		frame.setMaximumSize(size);
		frame.setMinimumSize(size);
		frame.setResizable(false);
		frame.add(new MakerPanel());
		frame.pack();
		frame.setLocationRelativeTo(null);
		frame.setTitle("OneShot Textbox Maker by Leo");
		frame.setVisible(true);
	}

}
