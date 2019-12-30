package com.leo.ostbm;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import javax.imageio.ImageIO;
import javax.imageio.stream.ImageOutputStream;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class AnimationPreviewAL implements ActionListener {

	private final byte[] animData;
	private Component parent;

	@Contract(pure = true)
	AnimationPreviewAL(final byte[] animData) {
		this.animData = animData;
	}

	void setParent(final Component parent) {
		this.parent = parent;
	}

	@Override
	public void actionPerformed(@NotNull final ActionEvent e) {
		final String cmd = e.getActionCommand();
		switch (cmd) {
		case PreviewPanel.A_SAVE_BOXES:
			final File sel = DialogUtil.openFileDialog(true, parent, "Save textbox(es) animation",
					new FileNameExtensionFilter("GIF files", "gif"));
			if (sel == null)
				return;
			if (sel.exists()) {
				final int confirm = JOptionPane.showConfirmDialog(parent,
						"File \"" + sel.getName() + "\" already exists.\nOverwrite it?", "Overwrite existing file?",
						JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
				if (confirm != JOptionPane.YES_OPTION)
					return;
				if (!sel.delete())
					Main.LOGGER.warn("Could not delete file \"" + sel.toString() + "\"");
			}
			try (ImageOutputStream out = ImageIO.createImageOutputStream(sel)) {
				out.write(animData);
			} catch (final IOException e1) {
				Main.LOGGER.error("Error while saving animation!", e1);
				JOptionPane.showMessageDialog(parent, "An exception occured while saving the animation:\n" + e1,
						"Couldn't save animation!", JOptionPane.ERROR_MESSAGE);
				break;
			}
			JOptionPane.showMessageDialog(parent, "Successfully saved the animation to:\n" + sel.getAbsolutePath(),
					"Success!", JOptionPane.INFORMATION_MESSAGE);
			break;
		case PreviewPanel.A_COPY_BOXES:
			final Clipboard cb = Toolkit.getDefaultToolkit().getSystemClipboard();
			if (cb == null) {
				JOptionPane.showMessageDialog(parent,
						"Java does not support accessing this operating system's clipboard!",
						"Couldn't copy image to clipboard!", JOptionPane.ERROR_MESSAGE);
				break;
			}
			final File tmp = new File(System.getProperty("java.io.tmpdir"), "clipboard.gif");
			tmp.deleteOnExit();
			try (ImageOutputStream out = ImageIO.createImageOutputStream(tmp)) {
				out.write(animData);
			} catch (final IOException e1) {
				Main.LOGGER.error("Error while copying animation to clipboard!", e1);
				JOptionPane.showMessageDialog(parent,
						"An exception occured while copying the animation to the clipboard:\n" + e1,
						"Couldn't copy animation to clipboard!", JOptionPane.ERROR_MESSAGE);
			}
			try {
				cb.setContents(new TransferableFile(tmp), (clipboard, contents) -> {
					if (!tmp.delete())
						Main.LOGGER.warn("Could not delete file \"" + tmp.toString() + "\"");
				});
			} catch (final IllegalStateException ex) {
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

	static class TransferableFile implements Transferable {

		private final List<File> file;

		TransferableFile(final File file) {
			this.file = new ArrayList<>();
			this.file.add(file);
		}

		@Override
		public DataFlavor[] getTransferDataFlavors() {
			return new DataFlavor[] { DataFlavor.javaFileListFlavor };
		}

		@Override
		public boolean isDataFlavorSupported(final DataFlavor flavor) {
			return DataFlavor.javaFileListFlavor.equals(flavor);
		}

		@NotNull
		@Override
		public Object getTransferData(@NotNull final DataFlavor flavor) throws UnsupportedFlavorException {
			if (flavor.equals(DataFlavor.javaFileListFlavor) && file != null)
				return file;
			else
				throw new UnsupportedFlavorException(flavor);
		}
	}

}
