package com.leo.ostbm;

import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.lang.Thread.UncaughtExceptionHandler;

public class ExceptionHandler implements UncaughtExceptionHandler {

	@Override
	public void uncaughtException(@NotNull final Thread t, final Throwable e) {
		Main.LOGGER.error("Uncaught exception in thread \"" + t.getName() + "\":", e);
		JOptionPane.showMessageDialog(null,
				"An uncaught exception has occurred!\nPlease report this error here:\n" + Main.ISSUES_SITE,
				"Uncaught exception!", JOptionPane.ERROR_MESSAGE);
		System.exit(1);
	}

}
