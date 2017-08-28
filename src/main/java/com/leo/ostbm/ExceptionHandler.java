package com.leo.ostbm;

import java.lang.Thread.UncaughtExceptionHandler;

import javax.swing.JOptionPane;

public class ExceptionHandler implements UncaughtExceptionHandler {

	@Override
	public void uncaughtException(Thread t, Throwable e) {
		System.err.println("Uncaught exception in thread \"" + t.getName() + "\":");
		e.printStackTrace();
		JOptionPane.showMessageDialog(null,
				"An uncaught exception has occured!\nPlease report this error here:\n" + Main.ISSUES_SITE,
				"Uncaught exception!", JOptionPane.ERROR_MESSAGE);
		System.exit(1);
	}

}
