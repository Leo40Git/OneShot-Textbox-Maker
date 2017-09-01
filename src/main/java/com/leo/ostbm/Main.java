package com.leo.ostbm;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.GraphicsEnvironment;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.filechooser.FileNameExtensionFilter;

public class Main {

	public static final Version VERSION = new Version("1.4");
	public static final String UPDATE_CHECK_SITE = "https://raw.githubusercontent.com/Leo40Git/OneShot-Textbox-Maker/master/.version";
	public static final String DOWNLOAD_SITE = "https://github.com/Leo40Git/OneShot-Textbox-Maker/releases/latest/";
	public static final String ISSUES_SITE = "https://github.com/Leo40Git/OneShot-Textbox-Maker/issues";

	public static final String A_FILE_NEW = "file_new";
	public static final String A_FILE_LOAD = "file_load";
	public static final String A_FILE_LOAD_LAST = "file_load_last";
	public static final String A_FILE_SAVE = "file_save";
	public static final String A_FILE_SAVE_AS = "file_save_as";
	public static final String A_QUESTION_UPDATE = "question_update";
	public static final String A_QUESTION_ABOUT = "question_about";

	private static JFrame frame;
	private static MakerPanel panel;

	static class FNCPrintStream extends PrintStream {

		private PrintStream consoleOut;

		public FNCPrintStream(OutputStream file, boolean err) throws FileNotFoundException {
			super(file);
			if (err)
				consoleOut = new PrintStream(new FileOutputStream(FileDescriptor.err));
			else
				consoleOut = new PrintStream(new FileOutputStream(FileDescriptor.out));
		}

		@Override
		public void write(int b) {
			synchronized (this) {
				super.write(b);
				consoleOut.write(b);
			}
		}

		@Override
		public void write(byte[] buf, int off, int len) {
			synchronized (this) {
				super.write(buf, off, len);
				consoleOut.write(buf, off, len);
			}
		}
	}

	static class MenuActionListener implements ActionListener {

		private void fileError(String command, IOException e, String title, String message) {
			System.err.println("File command \"" + command + "\" failed!");
			e.printStackTrace();
			JOptionPane.showMessageDialog(frame, message, title, JOptionPane.ERROR_MESSAGE);
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			String cmd = e.getActionCommand();
			switch (cmd) {
			// "File" Menu
			case A_FILE_NEW:
				try {
					panel.newProjectFile();
				} catch (IOException e1) {
					fileError(cmd, e1, "New project failed", "Could not create new project!");
				}
				break;
			case A_FILE_LOAD:
				try {
					panel.loadProjectFile(null);
				} catch (IOException e1) {
					fileError(cmd, e1, "Loading project failed", "Could not load project file!");
				}
				break;
			case A_FILE_LOAD_LAST:
				try {
					File sel = new File(Config.get(Config.KEY_LAST_PROJECT_FILE, ""));
					if (!sel.exists())
						break;
					panel.loadProjectFile(sel);
				} catch (IOException e1) {
					fileError(cmd, e1, "Loading project failed", "Could not load project file!");
				}
				break;
			case A_FILE_SAVE:
				try {
					panel.saveProjectFile(null);
				} catch (IOException e1) {
					fileError(cmd, e1, "Saving project failed", "Could not save project file!");
				}
				break;
			case A_FILE_SAVE_AS:
				try {
					File sel = openFileDialog(true, frame, "Save project file", MakerPanel.TBPROJ_FILTER);
					if (sel == null)
						break;
					panel.saveProjectFile(sel);
				} catch (IOException e1) {
					fileError(cmd, e1, "Saving project failed", "Could not save project file!");
				}
				break;
			// "?" Menu
			case A_QUESTION_UPDATE:
				Main.updateCheck(true, true);
				break;
			case A_QUESTION_ABOUT:
				JOptionPane.showMessageDialog(frame,
						"OneShot Textbox Maker (OSTBM) version " + VERSION + "\nMade by Leo",
						"About OneShot Textbox Maker v" + VERSION, JOptionPane.INFORMATION_MESSAGE);
				break;
			default:
				System.out.println("Undefined action: " + e.getActionCommand());
				break;
			}
		}

	}

	public static void main(String[] args) {
		if (GraphicsEnvironment.isHeadless()) {
			System.out.println("Headless mode is enabled!\nOSTBM cannot run in headless mode!");
			System.exit(0);
		}
		Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler());
		File log = new File("ostbm.log");
		if (log.exists())
			log.delete();
		try {
			log.createNewFile();
		} catch (IOException e) {
			e.printStackTrace();
		}
		FileOutputStream logOut = null;
		try {
			logOut = new FileOutputStream(log);
		} catch (FileNotFoundException e2) {
			e2.printStackTrace();
		}
		try {
			System.setOut(new FNCPrintStream(logOut, false));
			System.setErr(new FNCPrintStream(logOut, true));
		} catch (FileNotFoundException e1) {
			System.exit(1);
		}
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
		Config.init();
		LoadFrame loadFrame;
		final String skipuc = "skipuc";
		boolean skipucF = new File(System.getProperty("user.dir") + "/" + skipuc).exists();
		boolean skipucR = Config.getBoolean(Config.KEY_SKIP_UPDATE_CHECK, false);
		if (skipucR) {
			Config.setBoolean(Config.KEY_SKIP_UPDATE_CHECK, false);
			skipucF = skipucR;
		}
		if (skipucF) {
			System.out.println("Update check: skip file detected, skipping");
			loadFrame = new LoadFrame();
		} else {
			loadFrame = updateCheck(false, false);
		}
		SwingUtilities.invokeLater(() -> {
			loadFrame.setLoadString("Loading...");
			loadFrame.repaint();
		});
		try {
			Resources.initFonts();
			Resources.initImages();
		} catch (Exception e) {
			resourceError(e);
		}
		SwingUtilities.invokeLater(() -> {
			frame = new JFrame();
			frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			final Dimension size = new Dimension(640, 480);
			frame.setPreferredSize(size);
			frame.setMaximumSize(size);
			frame.setMinimumSize(size);
			frame.setResizable(false);
			MenuActionListener l = new MenuActionListener();
			JMenuBar mb = new JMenuBar();
			JMenu mFile = new JMenu("File");
			mb.add(mFile);
			JMenuItem miFileNew = new JMenuItem("New Project");
			miFileNew.addActionListener(l);
			miFileNew.setActionCommand(A_FILE_NEW);
			mFile.add(miFileNew);
			JMenuItem miFileLoad = new JMenuItem("Load Project");
			miFileLoad.addActionListener(l);
			miFileLoad.setActionCommand(A_FILE_LOAD);
			mFile.add(miFileLoad);
			JMenuItem miFileLoadLast = new JMenuItem("Load Last Project");
			miFileLoadLast.addActionListener(l);
			miFileLoadLast.setActionCommand(A_FILE_LOAD_LAST);
			mFile.add(miFileLoadLast);
			mFile.addSeparator();
			JMenuItem miFileSave = new JMenuItem("Save Project");
			miFileSave.addActionListener(l);
			miFileSave.setActionCommand(A_FILE_SAVE);
			mFile.add(miFileSave);
			JMenuItem miFileSaveAs = new JMenuItem("Save Project As...");
			miFileSaveAs.addActionListener(l);
			miFileSaveAs.setActionCommand(A_FILE_SAVE_AS);
			mFile.add(miFileSaveAs);
			JMenu mQuestion = new JMenu("?");
			JMenuItem miQuestionUpdate = new JMenuItem("Check for Updates");
			miQuestionUpdate.addActionListener(l);
			miQuestionUpdate.setActionCommand(A_QUESTION_UPDATE);
			mQuestion.add(miQuestionUpdate);
			mQuestion.addSeparator();
			JMenuItem miQuestionAbout = new JMenuItem("About OSTBM");
			miQuestionAbout.addActionListener(l);
			miQuestionAbout.setActionCommand(A_QUESTION_ABOUT);
			mQuestion.add(miQuestionAbout);
			mb.add(mQuestion);
			frame.setJMenuBar(mb);
			panel = new MakerPanel();
			frame.add(panel);
			frame.pack();
			frame.setLocationRelativeTo(null);
			frame.setTitle("OneShot Textbox Maker v" + VERSION);
			frame.setVisible(true);
			frame.requestFocus();
			loadFrame.dispose();
		});
	}

	private static void resourceError(Throwable e) {
		if (e != null)
			e.printStackTrace();
		JOptionPane.showMessageDialog(null, "Could not load resources!\nPlease report this error here:\n" + ISSUES_SITE,
				"Could not load resources!", JOptionPane.ERROR_MESSAGE);
		System.exit(1);
	}

	public static void downloadFile(String url, File dest) throws IOException {
		URL site = new URL(url);
		try (InputStream siteIn = site.openStream();
				ReadableByteChannel rbc = Channels.newChannel(siteIn);
				FileOutputStream out = new FileOutputStream(dest)) {
			out.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
		}
	}

	public static boolean browseTo(String url) throws URISyntaxException, IOException {
		URI dlSite = new URI(url);
		if (Desktop.isDesktopSupported())
			Desktop.getDesktop().browse(dlSite);
		else
			return false;
		return true;
	}

	public static LoadFrame updateCheck(boolean disposeOfLoadFrame, boolean showUpToDate) {
		LoadFrame loadFrame = new LoadFrame();
		loadFrame.requestFocus();
		File verFile = new File(System.getProperty("user.dir") + "/temp.version");
		System.out.println("Update check: starting");
		try {
			downloadFile(UPDATE_CHECK_SITE, verFile);
		} catch (IOException e1) {
			System.err.println("Update check failed: attempt to download caused exception");
			e1.printStackTrace();
			JOptionPane.showMessageDialog(null, "The update check has failed!\nAre you not connected to the internet?",
					"Update check failed", JOptionPane.ERROR_MESSAGE);
		}
		if (verFile.exists()) {
			System.out.println("Update check: reading version");
			try (FileReader fr = new FileReader(verFile); BufferedReader reader = new BufferedReader(fr);) {
				Version check = new Version(reader.readLine());
				if (VERSION.compareTo(check) < 0) {
					System.out.println("Update check successful: have update");
					JPanel panel = new JPanel();
					panel.setLayout(new BorderLayout());
					panel.add(new JLabel("A new update is available: " + check), BorderLayout.PAGE_START);
					final String defaultCl = "None.";
					String cl = defaultCl;
					while (reader.ready()) {
						if (defaultCl.equals(cl))
							cl = reader.readLine();
						else
							cl += "\n" + reader.readLine();
					}
					JTextArea chglog = new JTextArea(cl);
					chglog.setEditable(false);
					JScrollPane scrollChglog = new JScrollPane(chglog);
					scrollChglog.setPreferredSize(new Dimension(0, 200));
					panel.add(scrollChglog, BorderLayout.CENTER);
					panel.add(new JLabel(
							"Click \"Yes\" to go to the download site, click \"No\" to continue to the textbox maker."),
							BorderLayout.PAGE_END);
					int result = JOptionPane.showConfirmDialog(null, panel, "New update!", JOptionPane.YES_NO_OPTION,
							JOptionPane.PLAIN_MESSAGE);
					if (result == JOptionPane.YES_OPTION) {
						if (!browseTo(DOWNLOAD_SITE))
							JOptionPane.showMessageDialog(null,
									"Sadly, we can't browse to the download site for you on this platform. :(\nHead to\n"
											+ DOWNLOAD_SITE + "\nto get the newest update!",
									"Operation not supported...", JOptionPane.ERROR_MESSAGE);
						System.exit(0);
					}
				} else {
					System.out.println("Update check successful: up to date");
					if (showUpToDate) {
						JOptionPane.showMessageDialog(null,
								"You are using the most up to date version of the OneShot Textbox Maker! Have fun!",
								"Up to date!", JOptionPane.INFORMATION_MESSAGE);
					}
				}
			} catch (IOException e) {
				System.err.println("Update check failed: attempt to read downloaded file caused exception");
				e.printStackTrace();
				JOptionPane.showMessageDialog(null,
						"The update check has failed!\nAn exception occured while reading update check results:\n" + e,
						"Update check failed", JOptionPane.ERROR_MESSAGE);
			} catch (URISyntaxException e1) {
				System.out.println("Browse to download site failed: bad URI syntax");
				e1.printStackTrace();
				JOptionPane.showMessageDialog(null, "Failed to browse to the download site...",
						"Well, this is awkward.", JOptionPane.ERROR_MESSAGE);
			} finally {
				verFile.delete();
			}
		} else
			System.err.println("Update check failed: downloaded file doesn't exist");
		if (disposeOfLoadFrame) {
			loadFrame.dispose();
			return null;
		}
		return loadFrame;
	}

	public static File openFileDialog(boolean openOrSave, Component parent, String title,
			FileNameExtensionFilter filter) {
		JFileChooser fc = new JFileChooser();
		fc.setMultiSelectionEnabled(false);
		fc.setAcceptAllFileFilterUsed(false);
		fc.setDialogTitle(title);
		fc.setFileFilter(filter);
		fc.setCurrentDirectory(new File(System.getProperty("user.dir")));
		if (openOrSave) {
			int ret = fc.showSaveDialog(parent);
			if (ret == JFileChooser.APPROVE_OPTION) {
				File sel = fc.getSelectedFile();
				String selName = sel.getName();
				String ext = filter.getExtensions()[0];
				if (!selName.contains(".")
						|| !selName.substring(selName.lastIndexOf(".") + 1, selName.length()).equalsIgnoreCase(ext)) {
					selName += "." + ext;
					sel = new File(sel.getParentFile().getPath() + "/" + selName);
				}
				return sel;
			}
		} else {
			int ret = fc.showOpenDialog(parent);
			if (ret == JFileChooser.APPROVE_OPTION)
				return fc.getSelectedFile();
		}
		return null;
	}

}
