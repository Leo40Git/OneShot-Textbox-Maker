package com.leo.ostbm;

import java.awt.Component;
import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileNameExtensionFilter;

public class DialogUtil {

	public static int showCustomConfirmDialog(final Component parentComponent, final Object message, final String title,
			final String[] options, final int messageType) {
		return JOptionPane.showOptionDialog(parentComponent, message, title, JOptionPane.DEFAULT_OPTION, messageType,
				null, options, null);
	}

	public static File openFileDialog(final boolean openOrSave, final Component parent, final String title,
			final FileNameExtensionFilter filter) {
		final JFileChooser fc = new JFileChooser();
		fc.setMultiSelectionEnabled(false);
		fc.setAcceptAllFileFilterUsed(false);
		fc.setDialogTitle(title);
		fc.setFileFilter(filter);
		fc.setCurrentDirectory(new File(System.getProperty("user.dir")));
		if (openOrSave) {
			final int ret = fc.showSaveDialog(parent);
			if (ret == JFileChooser.APPROVE_OPTION) {
				File sel = fc.getSelectedFile();
				String selName = sel.getName();
				final String ext = filter.getExtensions()[0];
				if (!selName.contains(".")
						|| !selName.substring(selName.lastIndexOf(".") + 1, selName.length()).equalsIgnoreCase(ext)) {
					selName += "." + ext;
					sel = new File(sel.getParentFile().getPath() + "/" + selName);
				}
				return sel;
			}
		} else {
			final int ret = fc.showOpenDialog(parent);
			if (ret == JFileChooser.APPROVE_OPTION)
				return fc.getSelectedFile();
		}
		return null;
	}

}
