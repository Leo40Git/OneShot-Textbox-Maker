package com.leo.ostbm;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.GraphicsEnvironment;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.leo.ostbm.Resources.Icon;

public class Main {

	public static final Logger LOGGER = LogManager.getLogger("OSTBM");

	public static final Version VERSION = new Version("1.6");
	public static final String UPDATE_CHECK_SITE = "https://raw.githubusercontent.com/Leo40Git/OneShot-Textbox-Maker/master/.version";
	public static final String DOWNLOAD_SITE = "https://github.com/Leo40Git/OneShot-Textbox-Maker/releases/latest/";
	public static final String ISSUES_SITE = "https://github.com/Leo40Git/OneShot-Textbox-Maker/issues";

	public static final String A_FILE_NEW = "file:new";
	public static final String A_FILE_LOAD = "file:load";
	public static final String A_FILE_SAVE = "file:save";
	public static final String A_FILE_SAVE_AS = "file:saveAs";
	public static final String A_FILE_SETTINGS = "file:settings";
	public static final String A_FILE_EXIT = "file:exit";
	public static final String A_HELP_UPDATE = "help:update";
	public static final String A_HELP_ABOUT = "help:about";

	private static JFrame frame;
	private static MakerPanel panel;

	static class MenuActionListener implements ActionListener {

		private ImageIcon aboutIcon;

		public static void fileError(String command, IOException e, String title, String message) {
			LOGGER.error("File command \"" + command + "\" failed!", e);
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
					fileError(cmd, e1, "Saving project failed", "Could not save project file!");
				}
				break;
			case A_FILE_LOAD:
				try {
					panel.loadProjectFile(null);
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
			case A_FILE_SETTINGS:
				openSettings();
				break;
			case A_FILE_EXIT:
				close();
				break;
			// "Help" Menu
			case A_HELP_UPDATE:
				SwingUtilities.invokeLater(() -> {
					Main.updateCheck(true, true);
				});
				break;
			case A_HELP_ABOUT:
				if (aboutIcon == null)
					aboutIcon = new ImageIcon(Resources.getAppIcons().get(Resources.APPICON_32), "About");
				JOptionPane.showMessageDialog(frame,
						"OneShot Textbox Maker (OSTBM) version " + VERSION + "\nMade by Leo\nFacepic credits:\n"
								+ "\"original\" and \"solstice\" - Nightmargin, GIR and Elizavq\n"
								+ "\"osdiscord\" - OneShot Discord Server\n" + "\"ninja8tyu\" - ninja8tyu.tumblr.com\n"
								+ "\"tehawesomestkitteh\" - tehawesomestkitteh.tumblr.com",
						"About OneShot Textbox Maker v" + VERSION, JOptionPane.INFORMATION_MESSAGE, aboutIcon);
				break;
			default:
				LOGGER.debug("Undefined action: " + e.getActionCommand());
				break;
			}
		}

	}

	static class ConfirmCloseWindowListener extends WindowAdapter {

		@Override
		public void windowClosing(WindowEvent e) {
			Main.close();
		}

	}

	public static void main(String[] args) {
		if (GraphicsEnvironment.isHeadless()) {
			System.out.println("Headless mode is enabled!\nOSTBM cannot run in headless mode!");
			System.exit(0);
		}
		Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler());
		final String nolaf = "nolaf";
		if (new File(System.getProperty("user.dir") + "/" + nolaf).exists())
			LOGGER.info("No L&F file detected, skipping setting Look & Feel");
		else
			try {
				UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
			} catch (ClassNotFoundException | InstantiationException | IllegalAccessException
					| UnsupportedLookAndFeelException e) {
				LOGGER.error("Error while setting Look & Feel!", e);
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
			LOGGER.info("Update check: skip file detected, skipping");
			loadFrame = new LoadFrame();
		} else {
			loadFrame = updateCheck(false, false);
		}
		SwingUtilities.invokeLater(() -> {
			loadFrame.setLoadString("Loading...");
			loadFrame.repaint();
		});
		Resources.checkResFolder();
		try {
			Resources.initFonts();
			Resources.initImages();
		} catch (Exception e) {
			resourceError(e);
		}
		SwingUtilities.invokeLater(() -> {
			frame = new JFrame();
			frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
			frame.addWindowListener(new ConfirmCloseWindowListener());
			final Dimension size = new Dimension(880, 600);
			frame.setPreferredSize(size);
			frame.setMaximumSize(size);
			frame.setMinimumSize(size);
			frame.setResizable(false);
			JMenuBar mb = createMenuBar();
			frame.setJMenuBar(mb);
			panel = new MakerPanel();
			frame.add(panel);
			frame.pack();
			frame.setLocationRelativeTo(null);
			frame.setTitle("OneShot Textbox Maker v" + VERSION);
			frame.setIconImages(Resources.getAppIcons());
			frame.setVisible(true);
			frame.requestFocus();
			loadFrame.dispose();
		});
	}

	private static JMenuBar createMenuBar() {
		MenuActionListener l = new MenuActionListener();
		JMenuBar mb = new JMenuBar();
		JMenu mFile = new JMenu("File");
		// "File" Menu
		mb.add(mFile);
		JMenuItem miFileNew = new JMenuItem("New Project", Resources.getIcon(Icon.NEW_PROJECT));
		miFileNew.addActionListener(l);
		miFileNew.setActionCommand(A_FILE_NEW);
		mFile.add(miFileNew);
		JMenuItem miFileLoad = new JMenuItem("Load Project", Resources.getIcon(Icon.LOAD_PROJECT));
		miFileLoad.addActionListener(l);
		miFileLoad.setActionCommand(A_FILE_LOAD);
		mFile.add(miFileLoad);
		mFile.addSeparator();
		JMenuItem miFileSave = new JMenuItem("Save Project", Resources.getIcon(Icon.SAVE_PROJECT));
		miFileSave.addActionListener(l);
		miFileSave.setActionCommand(A_FILE_SAVE);
		mFile.add(miFileSave);
		JMenuItem miFileSaveAs = new JMenuItem("Save Project As...", Resources.getIcon(Icon.SAVE_PROJECT_AS));
		miFileSaveAs.addActionListener(l);
		miFileSaveAs.setActionCommand(A_FILE_SAVE_AS);
		mFile.add(miFileSaveAs);
		mFile.addSeparator();
		JMenuItem miFileSettings = new JMenuItem("Settings", Resources.getIcon(Icon.SETTINGS));
		miFileSettings.addActionListener(l);
		miFileSettings.setActionCommand(A_FILE_SETTINGS);
		mFile.add(miFileSettings);
		mFile.addSeparator();
		JMenuItem miFileExit = new JMenuItem("Exit OBSTM", Resources.getIcon(Icon.EXIT));
		miFileExit.addActionListener(l);
		miFileExit.setActionCommand(A_FILE_EXIT);
		mFile.add(miFileExit);
		JMenu mQuestion = new JMenu("Help");
		// "Help" Menu
		JMenuItem miQuestionUpdate = new JMenuItem("Check for Updates", Resources.getIcon(Icon.CHECK_FOR_UPDATES));
		miQuestionUpdate.addActionListener(l);
		miQuestionUpdate.setActionCommand(A_HELP_UPDATE);
		mQuestion.add(miQuestionUpdate);
		mQuestion.addSeparator();
		JMenuItem miQuestionAbout = new JMenuItem("About OSTBM", Resources.getIcon(Icon.ABOUT));
		miQuestionAbout.addActionListener(l);
		miQuestionAbout.setActionCommand(A_HELP_ABOUT);
		mQuestion.add(miQuestionAbout);
		mb.add(mQuestion);
		return mb;
	}

	public static void close() {
		if (panel.isProjectEmpty())
			System.exit(0);
		else {
			int sel = JOptionPane.showConfirmDialog(panel, "Do you want to save your project before exiting OSTBM?",
					"Save before exiting?", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
			if (sel == JOptionPane.CANCEL_OPTION)
				return;
			if (sel == JOptionPane.YES_OPTION)
				try {
					panel.saveProjectFile(null);
				} catch (IOException e1) {
					MenuActionListener.fileError(A_FILE_SAVE, e1, "Saving project failed",
							"Could not save project file!");
				}
			System.exit(0);
		}
	}

	private static void resourceError(Throwable e) {
		if (e != null)
			LOGGER.error("Error while loading resources!", e);
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
		File verFile = new File(System.getProperty("user.dir") + "/temp.version");
		LOGGER.info("Update check: starting");
		try {
			downloadFile(UPDATE_CHECK_SITE, verFile);
		} catch (IOException e1) {
			LOGGER.error("Update check failed: attempt to download caused exception", e1);
			JOptionPane.showMessageDialog(null, "The update check has failed!\nAre you not connected to the internet?",
					"Update check failed", JOptionPane.ERROR_MESSAGE);
		}
		if (verFile.exists()) {
			LOGGER.info("Update check: reading version");
			try (FileReader fr = new FileReader(verFile); BufferedReader reader = new BufferedReader(fr);) {
				Version check = new Version(reader.readLine());
				if (VERSION.compareTo(check) < 0) {
					LOGGER.info("Update check successful: have update");
					JPanel panel = new JPanel();
					panel.setLayout(new BorderLayout());
					panel.add(new JLabel("A new update is available: " + check), BorderLayout.PAGE_START);
					final String defaultCl = "No changelog provided.";
					String cl = defaultCl;
					while (reader.ready()) {
						if (defaultCl.equals(cl))
							cl = reader.readLine();
						else
							cl += "\n" + reader.readLine();
					}
					JTextArea chglog = new JTextArea(cl);
					chglog.setEditable(false);
					chglog.setPreferredSize(new Dimension(800, 450));
					JScrollPane scrollChglog = new JScrollPane(chglog);
					panel.add(scrollChglog, BorderLayout.CENTER);
					panel.add(
							new JLabel("Click \"Yes\" to go to the download site, click \"No\" to continue to OSTBM."),
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
					LOGGER.info("Update check successful: up to date");
					if (showUpToDate) {
						JOptionPane.showMessageDialog(null,
								"You are using the most up to date version of the OneShot Textbox Maker! Have fun!",
								"Up to date!", JOptionPane.INFORMATION_MESSAGE);
					}
				}
			} catch (IOException e) {
				LOGGER.error("Update check failed: attempt to read downloaded file caused exception", e);
				JOptionPane.showMessageDialog(null,
						"The update check has failed!\nAn exception occured while reading update check results:\n" + e,
						"Update check failed", JOptionPane.ERROR_MESSAGE);
			} catch (URISyntaxException e1) {
				LOGGER.error("Browse to download site failed: bad URI syntax", e1);
				JOptionPane.showMessageDialog(null, "Failed to browse to the download site...",
						"Well, this is awkward.", JOptionPane.ERROR_MESSAGE);
			} finally {
				verFile.delete();
			}
		} else
			LOGGER.error("Update check failed: downloaded file doesn't exist");
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

	private static void openSettings() {
		JDialog settingsFrame = new JDialog(frame, "Settings", true);
		settingsFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		JPanel settingsPanel = new JPanel();
		settingsPanel.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
		JPanel spoilerPanel = new JPanel();
		spoilerPanel.setLayout(new BoxLayout(spoilerPanel, BoxLayout.PAGE_AXIS));
		spoilerPanel.setBorder(BorderFactory.createTitledBorder("Spoilers"));
		spoilerPanel.add(new JLabel(
				"<html>By default, OSTBM hides facepics that are exclusive to the Solstice route to prevent spoilers.<br>Please note that changing this will reload all facepics and <b>remove all custom facepics</b>.</html>"));
		JCheckBox solsticeFacepics = new JCheckBox("Hide Solstice facepics",
				Config.getBoolean(Config.KEY_HIDE_SOLSTICE_FACES, true));
		solsticeFacepics.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Config.setBoolean(Config.KEY_HIDE_SOLSTICE_FACES, solsticeFacepics.isSelected());
				try {
					panel.updateCurrentBox();
					Resources.initFaces();
					panel.updateFaces();
				} catch (IOException e1) {
					resourceError(e1);
				}
			}
		});
		spoilerPanel.add(solsticeFacepics);
		settingsPanel.add(spoilerPanel);
		settingsFrame.add(settingsPanel);
		settingsFrame.pack();
		settingsFrame.setLocationRelativeTo(null);
		settingsFrame.setIconImage(Resources.getIcon(Icon.SETTINGS).getImage());
		settingsFrame.setVisible(true);
	}

}
