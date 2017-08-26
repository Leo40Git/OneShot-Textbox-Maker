package com.leo.ostbm;

import java.awt.Dimension;
import java.io.File;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

public class Main {

	public static final String VERSION = "1.2.3";

	private static JFrame frame;

	public static void main(String[] args) {
		final String nolaf = "nolaf";
		if (new File(System.getProperty("user.dir") + "/" + nolaf).exists())
			System.out.println("No L&F file detected, skipping setting Look & Feel");
		else
			try {
				UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
			} catch (ClassNotFoundException | InstantiationException | IllegalAccessException
					| UnsupportedLookAndFeelException e) {
				e.printStackTrace();
				JOptionPane.showMessageDialog(null, "Could not set Look & Feel!\nPlease add a file named \"" + nolaf
						+ "\" (all lowercase, no extension) to the application folder, and then restart the application.",
						"Could not set Look & Feel", JOptionPane.ERROR_MESSAGE);
				System.exit(1);
			}
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
		frame.setTitle("OneShot Textbox Maker v" + VERSION);
		frame.setVisible(true);
	}

}
