package com.leo.ostbm;

import com.leo.ostbm.Resources.Icon;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;

public class Main {

    public static final Logger LOGGER = LogManager.getLogger("OSTBM");

    public static final Version VERSION = new Version("2.0");
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

    @Contract(pure = true)
    public static JFrame getFrame() {
        return frame;
    }

    @Contract(pure = true)
    public static MakerPanel getPanel() {
        return panel;
    }

    public static void main(final String[] args) {
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
        try {
            Resources.initAppIcons();
        } catch (final IOException e1) {
            resourceError(e1);
        }
        LoadFrame loadFrame;
        final String skipuc = "skipuc";
        boolean skipucF = new File(System.getProperty("user.dir") + "/" + skipuc).exists();
        final boolean skipucR = Config.getBoolean(Config.KEY_SKIP_UPDATE_CHECK, false);
        if (skipucR) {
            Config.setBoolean(Config.KEY_SKIP_UPDATE_CHECK, false);
            skipucF = skipucR;
        }
        if (skipucF) {
            LOGGER.info("Update check: skip file detected, skipping");
            loadFrame = new LoadFrame("Loading...", true);
        } else {
            loadFrame = updateCheck(false, false);
            SwingUtilities.invokeLater(() -> {
                loadFrame.setLoadString("Loading...");
                loadFrame.repaint();
            });
        }
        Resources.checkResFolder();
        try {
            Resources.initFonts();
            Resources.initImages();
        } catch (final Exception e) {
            resourceError(e);
        }
        SwingUtilities.invokeLater(() -> {
            frame = new JFrame();
            frame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
            frame.addWindowListener(new ConfirmCloseWindowListener());
            final Dimension size = new Dimension(880, 600);
            frame.setPreferredSize(size);
            frame.setMaximumSize(size);
            frame.setMinimumSize(size);
            frame.setResizable(false);
            final JMenuBar mb = createMenuBar();
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
        final MenuActionListener l = new MenuActionListener();
        final JMenuBar mb = new JMenuBar();
        final JMenu mFile = new JMenu("File");
        // "File" Menu
        mb.add(mFile);
        final JMenuItem miFileNew = new JMenuItem("New Project", Resources.getIcon(Icon.NEW_PROJECT));
        miFileNew.addActionListener(l);
        miFileNew.setActionCommand(A_FILE_NEW);
        mFile.add(miFileNew);
        final JMenuItem miFileLoad = new JMenuItem("Load Project", Resources.getIcon(Icon.LOAD_PROJECT));
        miFileLoad.addActionListener(l);
        miFileLoad.setActionCommand(A_FILE_LOAD);
        mFile.add(miFileLoad);
        mFile.addSeparator();
        final JMenuItem miFileSave = new JMenuItem("Save Project", Resources.getIcon(Icon.SAVE_PROJECT));
        miFileSave.addActionListener(l);
        miFileSave.setActionCommand(A_FILE_SAVE);
        mFile.add(miFileSave);
        final JMenuItem miFileSaveAs = new JMenuItem("Save Project As...", Resources.getIcon(Icon.SAVE_PROJECT_AS));
        miFileSaveAs.addActionListener(l);
        miFileSaveAs.setActionCommand(A_FILE_SAVE_AS);
        mFile.add(miFileSaveAs);
        mFile.addSeparator();
        final JMenuItem miFileSettings = new JMenuItem("Settings", Resources.getIcon(Icon.SETTINGS));
        miFileSettings.addActionListener(l);
        miFileSettings.setActionCommand(A_FILE_SETTINGS);
        mFile.add(miFileSettings);
        mFile.addSeparator();
        final JMenuItem miFileExit = new JMenuItem("Exit OBSTM", Resources.getIcon(Icon.EXIT));
        miFileExit.addActionListener(l);
        miFileExit.setActionCommand(A_FILE_EXIT);
        mFile.add(miFileExit);
        final JMenu mQuestion = new JMenu("Help");
        // "Help" Menu
        final JMenuItem miQuestionUpdate = new JMenuItem("Check for Updates",
                Resources.getIcon(Icon.CHECK_FOR_UPDATES));
        miQuestionUpdate.addActionListener(l);
        miQuestionUpdate.setActionCommand(A_HELP_UPDATE);
        mQuestion.add(miQuestionUpdate);
        mQuestion.addSeparator();
        final JMenuItem miQuestionAbout = new JMenuItem("About OSTBM", Resources.getIcon(Icon.ABOUT));
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
            final int sel = JOptionPane.showConfirmDialog(panel,
                    "Do you want to save your project before exiting OSTBM?", "Save before exiting?",
                    JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
            if (sel == JOptionPane.CANCEL_OPTION)
                return;
            if (sel == JOptionPane.YES_OPTION)
                try {
                    panel.saveProjectFile(null, false);
                } catch (final IOException e1) {
                    MenuActionListener.fileError(A_FILE_SAVE, e1, "Saving project failed",
                            "Could not save project file!");
                }
            System.exit(0);
        }
    }

    public static void resourceError(final Throwable e) {
        if (e != null)
            LOGGER.error("Error while loading resources!", e);
        JOptionPane.showMessageDialog(null,
                "Could not load resources:" + e + "\nPlease report this error here:\n" + ISSUES_SITE,
                "Could not load resources!", JOptionPane.ERROR_MESSAGE);
        System.exit(1);
    }

    public static void downloadFile(final String url, final File dest) throws IOException {
        final URL site = new URL(url);
        try (InputStream siteIn = site.openStream();
             ReadableByteChannel rbc = Channels.newChannel(siteIn);
             FileOutputStream out = new FileOutputStream(dest)) {
            out.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
        }
    }

    public static boolean browseTo(final String url) throws URISyntaxException, IOException {
        final URI dlSite = new URI(url);
        if (Desktop.isDesktopSupported())
            Desktop.getDesktop().browse(dlSite);
        else
            return false;
        return true;
    }

    public static LoadFrame updateCheck(final boolean disposeOfLoadFrame, final boolean showUpToDate) {
        final LoadFrame loadFrame = new LoadFrame(true);
        final File verFile = new File(System.getProperty("user.dir") + "/temp.version");
        LOGGER.info("Update check: starting");
        try {
            downloadFile(UPDATE_CHECK_SITE, verFile);
        } catch (final IOException e1) {
            LOGGER.error("Update check failed: attempt to download caused exception", e1);
            JOptionPane.showMessageDialog(null, "The update check has failed!\nAre you not connected to the internet?",
                    "Update check failed", JOptionPane.ERROR_MESSAGE);
        }
        if (verFile.exists()) {
            LOGGER.info("Update check: reading version");
            try (FileReader fr = new FileReader(verFile); BufferedReader reader = new BufferedReader(fr)) {
                final Version check = new Version(reader.readLine());
                if (VERSION.compareTo(check) < 0) {
                    LOGGER.info("Update check successful: have update");
                    final JPanel panel = new JPanel();
                    panel.setLayout(new BorderLayout());
                    panel.add(new JLabel("A new update is available: " + check), BorderLayout.PAGE_START);
                    final String defaultCl = "No changelog provided.";
                    String cl = defaultCl;
                    while (reader.ready())
                        if (defaultCl.equals(cl))
                            cl = reader.readLine();
                        else
                            cl += "\n" + reader.readLine();
                    final JTextArea chglog = new JTextArea(cl);
                    chglog.setEditable(false);
                    chglog.setPreferredSize(new Dimension(800, 450));
                    final JScrollPane scrollChglog = new JScrollPane(chglog);
                    panel.add(scrollChglog, BorderLayout.CENTER);
                    panel.add(
                            new JLabel("Click \"Yes\" to go to the download site, click \"No\" to continue to OSTBM."),
                            BorderLayout.PAGE_END);
                    final int result = JOptionPane.showConfirmDialog(null, panel, "New update!",
                            JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE);
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
                    if (showUpToDate)
                        JOptionPane.showMessageDialog(null,
                                "You are using the most up to date version of the OneShot Textbox Maker! Have fun!",
                                "Up to date!", JOptionPane.INFORMATION_MESSAGE);
                }
            } catch (final IOException e) {
                LOGGER.error("Update check failed: attempt to read downloaded file caused exception", e);
                JOptionPane.showMessageDialog(null,
                        "The update check has failed!\nAn exception occured while reading update check results:\n" + e,
                        "Update check failed", JOptionPane.ERROR_MESSAGE);
            } catch (final URISyntaxException e1) {
                LOGGER.error("Browse to download site failed: bad URI syntax", e1);
                JOptionPane.showMessageDialog(null, "Failed to browse to the download site...",
                        "Well, this is awkward.", JOptionPane.ERROR_MESSAGE);
            } finally {
                if (!verFile.delete())
                    LOGGER.warn("Could not delete file \"" + verFile.toString() + "\"");
            }
        } else
            LOGGER.error("Update check failed: downloaded file doesn't exist");
        if (disposeOfLoadFrame) {
            loadFrame.dispose();
            return null;
        }
        return loadFrame;
    }

    private static void openSettings() {
        final JDialog settingsFrame = new JDialog(frame, "Settings", true);
        settingsFrame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        settingsFrame.add(new SettingsPanel());
        settingsFrame.pack();
        settingsFrame.setLocationRelativeTo(null);
        settingsFrame.setIconImage(Resources.getIcon(Icon.SETTINGS).getImage());
        settingsFrame.setVisible(true);
    }

    static class MenuActionListener implements ActionListener {

        private ImageIcon aboutIcon;

        public static void fileError(final String command, final IOException e, final String title,
                                     final String message) {
            LOGGER.error("File command \"" + command + "\" failed!", e);
            JOptionPane.showMessageDialog(frame, message, title, JOptionPane.ERROR_MESSAGE);
        }

        @Override
        public void actionPerformed(@NotNull final ActionEvent e) {
            final String cmd = e.getActionCommand();
            switch (cmd) {
                // "File" Menu
                case A_FILE_NEW:
                    try {
                        panel.newProjectFile();
                    } catch (final IOException e1) {
                        fileError(cmd, e1, "Saving project failed", "Could not save project file!");
                    }
                    break;
                case A_FILE_LOAD:
                    try {
                        panel.loadProjectFile(null);
                    } catch (final IOException e1) {
                        fileError(cmd, e1, "Loading project failed", "Could not load project file!");
                    }
                    break;
                case A_FILE_SAVE:
                    try {
                        panel.saveProjectFile(null, false);
                    } catch (final IOException e1) {
                        fileError(cmd, e1, "Saving project failed", "Could not save project file!");
                    }
                    break;
                case A_FILE_SAVE_AS:
                    try {
                        panel.saveProjectFile(null, true);
                    } catch (final IOException e1) {
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
                        aboutIcon = new ImageIcon(Resources.getAppIcons().get(1), "About");
                    JOptionPane.showMessageDialog(frame,
                            "<html>OneShot Textbox Maker (OSTBM) version " + VERSION + "<br/>Made by ADudeCalledLeo<br/><br/>"
                                    + "Facepic credits:<br/>"
                                    + "    - <code>original</code> - NightMargin, GIR and Elizavq<br/>"
                                    + "    - <code>osdiscord</code> - People from OneShot Discord Server<br/>"
                                    + "    - <code>ninja8tyu</code> - ninja8tyu.tumblr.com<br/>"
                                    + "    - <code>tehawesomestkitteh</code> - tehawesomestkitteh.tumblr.com"
                                    + "    - <code>adudecalledleo</code> - Me, obviously!"
                                    + "</html>",
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
        public void windowClosing(final WindowEvent e) {
            Main.close();
        }

    }

}
