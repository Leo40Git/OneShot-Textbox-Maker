package com.leo.ostbm;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
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
import javax.swing.filechooser.FileNameExtensionFilter;

public class PreviewPanel extends JPanel implements ActionListener, ClipboardOwner {

	private static final long serialVersionUID = 1L;

	public static final String A_COPY_BOXES = "copyBoxes";
	public static final String A_SAVE_BOXES = "saveBoxes";

	private BufferedImage image;
	private ImageIcon previewImage;
	private JButton saveButton, copyButton;

	public PreviewPanel(BufferedImage image) {
		this.image = image;
		previewImage = new ImageIcon(image, "textbox(es) preview");
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
		copyButton.addActionListener(this);
		copyButton.setActionCommand(A_COPY_BOXES);
		copyButton.setToolTipText("Copy this textbox (or these textboxes) to the clipboard");
		buttonPanel.add(copyButton);
		saveButton = new JButton("Save to File");
		saveButton.addActionListener(this);
		saveButton.setActionCommand(A_SAVE_BOXES);
		saveButton.setToolTipText("Save this textbox (or these textboxes) as an image");
		saveButton.setPreferredSize(copyButton.getPreferredSize());
		buttonPanel.add(saveButton);
		add(buttonPanel, BorderLayout.PAGE_END);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		String cmd = e.getActionCommand();
		switch (cmd) {
		case A_COPY_BOXES:
			Clipboard cb = Toolkit.getDefaultToolkit().getSystemClipboard();
			if (cb == null) {
				// TODO error popup
				System.err.println("clipboard not supported");
				break;
			}
			cb.setContents(new TransferableImage(image), this);
			break;
		case A_SAVE_BOXES:
			File sel = Main.openFileDialog(true, this, "Save textbox(es) image",
					new FileNameExtensionFilter("PNG files", "png"));
			if (sel == null)
				return;
			try {
				ImageIO.write(image, "png", sel);
			} catch (IOException e1) {
				e1.printStackTrace();
				JOptionPane.showMessageDialog(this, "An exception occured while saving the image:\n" + e1,
						"Couldn't save image!", JOptionPane.ERROR_MESSAGE);
			}
			break;
		default:
			System.out.println("Undefined action: " + cmd);
		}
	}

	class TransferableImage implements Transferable {

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
			DataFlavor[] flavors = new DataFlavor[1];
			flavors[0] = DataFlavor.imageFlavor;
			return flavors;
		}

		public boolean isDataFlavorSupported(DataFlavor flavor) {
			DataFlavor[] flavors = getTransferDataFlavors();
			for (int i = 0; i < flavors.length; i++) {
				if (flavor.equals(flavors[i])) {
					return true;
				}
			}

			return false;
		}
	}

	@Override
	public void lostOwnership(Clipboard clipboard, Transferable contents) {
	}
}
