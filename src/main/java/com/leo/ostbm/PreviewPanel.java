package com.leo.ostbm;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileNameExtensionFilter;

public class PreviewPanel extends JPanel implements ActionListener {

	private static final long serialVersionUID = 1L;

	public static final String A_COPY_BOXES = "copyBoxes";
	public static final String A_SAVE_BOXES = "saveBoxes";

	private BufferedImage image;
	private ImageIcon previewImage;
	private JButton saveButton, copyButton;

	public PreviewPanel(BufferedImage image) {
		this.image = image;
		previewImage = new ImageIcon(image, "textbox(es) preview");
		initPanel(this);
	}

	public PreviewPanel(ImageIcon previewImage, ActionListener l) {
		this.previewImage = previewImage;
		initPanel(l);
	}

	private void initPanel(ActionListener l) {
		setLayout(new BorderLayout());
		setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
		JPanel previewPanel = new JPanel();
		JLabel previewLabel = new JLabel(previewImage);
		previewLabel.setMinimumSize(new Dimension(0, previewImage.getIconHeight()));
		previewPanel.add(previewLabel);
		previewPanel.setMinimumSize(new Dimension(previewImage.getIconWidth(), 0));
		JScrollPane previewScroll = new JScrollPane(previewPanel);
		add(previewScroll, BorderLayout.CENTER);
		JPanel buttonPanel = new JPanel();
		copyButton = new JButton("Copy to Clipboard");
		copyButton.addActionListener(l);
		copyButton.setActionCommand(A_COPY_BOXES);
		copyButton.setToolTipText("Copy this textbox (or these textboxes) to the clipboard");
		buttonPanel.add(copyButton);
		saveButton = new JButton("Save to File");
		saveButton.addActionListener(l);
		saveButton.setActionCommand(A_SAVE_BOXES);
		saveButton.setToolTipText("Save this textbox (or these textboxes) as an image");
		saveButton.setPreferredSize(copyButton.getPreferredSize());
		buttonPanel.add(saveButton);
		add(buttonPanel, BorderLayout.PAGE_END);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		Component parent = SwingUtilities.getRoot(this);
		String cmd = e.getActionCommand();
		switch (cmd) {
		case A_COPY_BOXES:
			Clipboard cb = Toolkit.getDefaultToolkit().getSystemClipboard();
			if (cb == null) {
				JOptionPane.showMessageDialog(parent,
						"Java does not support accessing this operating system's clipboard!",
						"Couldn't copy image to clipboard!", JOptionPane.ERROR_MESSAGE);
				break;
			}
			try {
				cb.setContents(new TransferableImage(image), null);
			} catch (IllegalStateException ex) {
				Main.LOGGER.error("Error while copying image to clipboard!", ex);
				JOptionPane.showMessageDialog(parent,
						"An exception occured while copying the image to the clipboard:\n" + ex,
						"Couldn't copy image to clipboard!", JOptionPane.ERROR_MESSAGE);
				break;
			}
			JOptionPane.showMessageDialog(parent, "Successfully copied the image to the clipboard.", "Success!",
					JOptionPane.INFORMATION_MESSAGE);
			break;
		case A_SAVE_BOXES:
			File sel = Main.openFileDialog(true, parent, "Save textbox(es) image",
					new FileNameExtensionFilter("PNG files", "png"));
			if (sel == null)
				return;
			if (sel.exists()) {
				int confirm = JOptionPane.showConfirmDialog(parent,
						"File \"" + sel.getName() + "\" already exists?\nOverwrite it?", "Overwrite existing file?",
						JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
				if (confirm != JOptionPane.YES_OPTION)
					return;
				sel.delete();
			}
			try {
				ImageIO.write(image, "png", sel);
			} catch (IOException ex) {
				Main.LOGGER.error("Error while saving image!", ex);
				JOptionPane.showMessageDialog(parent, "An exception occured while saving the image:\n" + ex,
						"Couldn't save image!", JOptionPane.ERROR_MESSAGE);
				break;
			}
			JOptionPane.showMessageDialog(parent, "Successfully saved the image to:\n" + sel.getAbsolutePath(),
					"Success!", JOptionPane.INFORMATION_MESSAGE);
			break;
		default:
			Main.LOGGER.debug("Undefined action: " + cmd);
		}
	}

	public static class TransferableImage implements Transferable {

		Image i;

		public TransferableImage(Image i) {
			this.i = i;
		}

		public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
			if (flavor.equals(DataFlavor.imageFlavor) && i != null) {
				return i;
			} else {
				throw new UnsupportedFlavorException(flavor);
			}
		}

		public DataFlavor[] getTransferDataFlavors() {
			return new DataFlavor[] { DataFlavor.imageFlavor };
		}

		public boolean isDataFlavorSupported(DataFlavor flavor) {
			return DataFlavor.imageFlavor.equals(flavor);
		}
	}
}
