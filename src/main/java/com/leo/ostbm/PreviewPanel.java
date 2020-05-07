package com.leo.ostbm;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class PreviewPanel extends JPanel implements ActionListener {

	public static final String A_COPY_BOXES = "copyBoxes";
	public static final String A_SAVE_BOXES = "saveBoxes";
	private static final long serialVersionUID = 1L;
	private final ImageIcon previewImage;
	private BufferedImage image;

	public PreviewPanel(final BufferedImage image) {
		this.image = image;
		previewImage = new ImageIcon(image, "textbox(es) preview");
		initPanel(this);
	}

	public PreviewPanel(final ImageIcon previewImage, final ActionListener l) {
		this.previewImage = previewImage;
		initPanel(l);
	}

	private void initPanel(final ActionListener l) {
		setLayout(new BorderLayout());
		setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
		final JPanel previewPanel = new JPanel();
		final JLabel previewLabel = new JLabel(previewImage);
		previewLabel.setMinimumSize(new Dimension(0, previewImage.getIconHeight()));
		previewPanel.add(previewLabel);
		previewPanel.setMinimumSize(new Dimension(previewImage.getIconWidth(), 0));
		final JScrollPane previewScroll = new JScrollPane(previewPanel);
		add(previewScroll, BorderLayout.CENTER);
		final JPanel buttonPanel = new JPanel();
		JButton copyButton = new JButton("Copy to Clipboard");
		copyButton.addActionListener(l);
		copyButton.setActionCommand(A_COPY_BOXES);
		copyButton.setToolTipText("Copy this textbox (or these textboxes) to the clipboard");
		buttonPanel.add(copyButton);
		JButton saveButton = new JButton("Save to File");
		saveButton.addActionListener(l);
		saveButton.setActionCommand(A_SAVE_BOXES);
		saveButton.setToolTipText("Save this textbox (or these textboxes) as an image");
		saveButton.setPreferredSize(copyButton.getPreferredSize());
		buttonPanel.add(saveButton);
		add(buttonPanel, BorderLayout.PAGE_END);
	}

	@Override
	public void actionPerformed(@NotNull final ActionEvent e) {
		final String cmd = e.getActionCommand();
		switch (cmd) {
		case A_COPY_BOXES:
			final Clipboard cb = Toolkit.getDefaultToolkit().getSystemClipboard();
			if (cb == null) {
				JOptionPane.showMessageDialog(this,
						"Java does not support accessing this operating system's clipboard!",
						"Couldn't copy image to clipboard!", JOptionPane.ERROR_MESSAGE);
				break;
			}
			try {
				cb.setContents(new TransferableImage(image), null);
			} catch (final IllegalStateException ex) {
				Main.LOGGER.error("Error while copying image to clipboard!", ex);
				JOptionPane.showMessageDialog(this,
						"An exception occured while copying the image to the clipboard:\n" + ex,
						"Couldn't copy image to clipboard!", JOptionPane.ERROR_MESSAGE);
				break;
			}
			JOptionPane.showMessageDialog(this, "Successfully copied the image to the clipboard.", "Success!",
					JOptionPane.INFORMATION_MESSAGE);
			break;
		case A_SAVE_BOXES:
			final File sel = DialogUtil.openFileDialog(true, this, "Save textbox(es) image",
					new FileNameExtensionFilter("PNG files", "png"));
			if (sel == null)
				return;
			if (sel.exists()) {
				final int confirm = JOptionPane.showConfirmDialog(this,
						"File \"" + sel.getName() + "\" already exists?\nOverwrite it?", "Overwrite existing file?",
						JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
				if (confirm != JOptionPane.YES_OPTION)
					return;
				if (!sel.delete()) {
					JOptionPane.showMessageDialog(this, "Could not delete file.", "Could not overwrite file", JOptionPane.ERROR_MESSAGE);
					return;
				}
			}
			try {
				ImageIO.write(image, "png", sel);
			} catch (final IOException ex) {
				Main.LOGGER.error("Error while saving image!", ex);
				JOptionPane.showMessageDialog(this, "An exception occurred while saving the image:\n" + ex,
						"Couldn't save image!", JOptionPane.ERROR_MESSAGE);
				break;
			}
			JOptionPane.showMessageDialog(this, "Successfully saved the image to:\n" + sel.getAbsolutePath(),
					"Success!", JOptionPane.INFORMATION_MESSAGE);
			break;
		default:
			Main.LOGGER.debug("Undefined action: " + cmd);
		}
	}

	public static class TransferableImage implements Transferable {
		Image i;

		@Contract(pure = true)
		public TransferableImage(final Image i) {
			this.i = i;
		}

		@NotNull
		@Override
		public Object getTransferData(@NotNull final DataFlavor flavor) throws UnsupportedFlavorException, IOException {
			if (flavor.equals(DataFlavor.imageFlavor) && i != null)
				return i;
			else
				throw new UnsupportedFlavorException(flavor);
		}

		@Override
		public DataFlavor[] getTransferDataFlavors() {
			return new DataFlavor[] { DataFlavor.imageFlavor };
		}

		@Override
		public boolean isDataFlavorSupported(final DataFlavor flavor) {
			return DataFlavor.imageFlavor.equals(flavor);
		}
	}
}
