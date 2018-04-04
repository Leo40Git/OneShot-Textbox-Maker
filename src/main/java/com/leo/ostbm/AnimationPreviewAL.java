package com.leo.ostbm;

import java.awt.Component;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;
import javax.imageio.stream.ImageOutputStream;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileNameExtensionFilter;

public class AnimationPreviewAL implements ActionListener {
	
	private Component parent;
	private final byte[] animData;
	
	public AnimationPreviewAL(byte[] animData) {
		this.animData = animData;
	}
	
	public void setParent(Component parent) {
		this.parent = parent;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		String cmd = e.getActionCommand();
		switch (cmd) {
		case PreviewPanel.A_SAVE_BOXES:
			File sel = DialogUtil.openFileDialog(true, parent, "Save textbox(es) animation",
					new FileNameExtensionFilter("GIF files", "gif"));
			if (sel == null)
				return;
			if (sel.exists()) {
				int confirm = JOptionPane.showConfirmDialog(parent,
						"File \"" + sel.getName() + "\" already exists.\nOverwrite it?", "Overwrite existing file?",
						JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
				if (confirm != JOptionPane.YES_OPTION)
					return;
				sel.delete();
			}
			try (ImageOutputStream out = ImageIO.createImageOutputStream(sel)) {
				out.write(animData);
			} catch (IOException e1) {
				Main.LOGGER.error("Error while saving animation!", e1);
				JOptionPane.showMessageDialog(parent, "An exception occured while saving the animation:\n" + e1,
						"Couldn't save animation!", JOptionPane.ERROR_MESSAGE);
				break;
			}
			JOptionPane.showMessageDialog(parent, "Successfully saved the animation to:\n" + sel.getAbsolutePath(),
					"Success!", JOptionPane.INFORMATION_MESSAGE);
			break;
		case PreviewPanel.A_COPY_BOXES:
			Clipboard cb = Toolkit.getDefaultToolkit().getSystemClipboard();
			if (cb == null) {
				JOptionPane.showMessageDialog(parent,
						"Java does not support accessing this operating system's clipboard!",
						"Couldn't copy image to clipboard!", JOptionPane.ERROR_MESSAGE);
				break;
			}
			File tmp = new File(System.getProperty("java.io.tmpdir"), "clipboard.gif");
			tmp.deleteOnExit();
			try (ImageOutputStream out = ImageIO.createImageOutputStream(tmp)) {
				out.write(animData);
			} catch (IOException e1) {
				Main.LOGGER.error("Error while copying animation to clipboard!", e1);
				JOptionPane.showMessageDialog(parent,
						"An exception occured while copying the animation to the clipboard:\n" + e1,
						"Couldn't copy animation to clipboard!", JOptionPane.ERROR_MESSAGE);
			}
			try {
				cb.setContents(new TransferableFileList(tmp), new ClipboardOwner() {
					@Override
					public void lostOwnership(Clipboard clipboard, Transferable contents) {
						tmp.delete();
					}
				});
			} catch (IllegalStateException ex) {
				Main.LOGGER.error("Error while copying animation to clipboard!", ex);
				JOptionPane.showMessageDialog(parent,
						"An exception occured while copying the animation to the clipboard:\n" + ex,
						"Couldn't copy animation to clipboard!", JOptionPane.ERROR_MESSAGE);
				break;
			}
			JOptionPane.showMessageDialog(parent, "Successfully copied the animation to the clipboard.", "Success!",
					JOptionPane.INFORMATION_MESSAGE);
			break;
		default:
			Main.LOGGER.debug("Undefined action: " + cmd);
			break;
		}
	}

	public static class TransferableFileList implements Transferable {

		private List<File> listOfFiles;

		public TransferableFileList(List<File> listOfFiles) {
			this.listOfFiles = listOfFiles;
		}

		public TransferableFileList(File file) {
			listOfFiles = new ArrayList<>();
			listOfFiles.add(file);
		}

		@Override
		public DataFlavor[] getTransferDataFlavors() {
			return new DataFlavor[] { DataFlavor.javaFileListFlavor };
		}

		@Override
		public boolean isDataFlavorSupported(DataFlavor flavor) {
			return DataFlavor.javaFileListFlavor.equals(flavor);
		}

		@Override
		public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
			if (flavor.equals(DataFlavor.javaFileListFlavor) && listOfFiles != null) {
				return listOfFiles;
			} else {
				throw new UnsupportedFlavorException(flavor);
			}
		}
	}

}
