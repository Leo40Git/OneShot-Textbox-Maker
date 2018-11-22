package com.leo.ostbm;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;
import java.util.*;
import java.util.stream.Collectors;

public class Resources {

    public static final String FACE_BLANK = "(none)";
    public static final int APPICON_16 = 0;
    public static final int APPICON_32 = 1;
    public static final int APPICON_64 = 2;
    static final String[] DUMMY_STRING_ARRAY = new String[]{};
    private static Map<String, Facepic> faces;
    private static boolean loadingCustom;
    private static List<BufferedImage> appIcons;
    private static Map<Icon, ImageIcon> icons;
    private static BufferedImage textboxImage;
    private static BufferedImage textboxArrow;
    private static Font fontBase;
    private static Font textboxFont;

    public static void checkResFolder() {
        final File resFolder = new File("res");
        if (!resFolder.exists()) {
            JOptionPane.showMessageDialog(null,
                    "The resources folder doesn't exist!\nPlease make sure there's a \"res\" folder next to the application that contains the faces.",
                    "Resources folder doesn't exist!", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }
    }

    public static void initFonts() throws FontFormatException, IOException {
        fontBase = Font.createFont(Font.TRUETYPE_FONT, new File("res/textboxFont.ttf"));
        final GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        textboxFont = fontBase.deriveFont(Font.BOLD, 20);
        ge.registerFont(textboxFont);
    }

    public static void initAppIcons() throws IOException {
        appIcons = new LinkedList<>();
        final String[] sizes = new String[]{"16", "32", "64"};
        for (final String size : sizes)
            appIcons.add(ImageIO.read(Resources.class.getResourceAsStream("/appicon" + size + ".png")));
    }

    public static void initImages() throws IOException, URISyntaxException {
        textboxImage = ImageIO.read(new File("res/textboxImage.png"));
        textboxArrow = ImageIO.read(new File("res/textboxArrow.png"));
        final BufferedImage iconSheet = ImageIO.read(Resources.class.getResourceAsStream("/icons.png"));
        icons = new HashMap<>();
        int ix = 0, iy = 0;
        for (final Icon icon : Icon.values()) {
            icons.put(icon, new ImageIcon(iconSheet.getSubimage(ix, iy, 16, 16), icon.getDescription()));
            ix += 16;
            if (ix == iconSheet.getWidth()) {
                ix = 0;
                iy += 16;
            }
        }
        initFaces();
    }

    public static void initFaces() throws IOException {
        loadingCustom = false;
        faces = new HashMap<>();
        addFace(FACE_BLANK, null, new BufferedImage(96, 96, BufferedImage.TYPE_4BYTE_ABGR));
        final File facesFolder = new File("res/faces");
        if (!facesFolder.exists()) {
            JOptionPane.showMessageDialog(null,
                    "The faces folder doesn't exist!\nPlease make sure there's a \"faces\" folder inside the resources folder (\"res\").",
                    "Faces folder doesn't exist!", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }
        addFaces(facesFolder, Config.getBoolean(Config.KEY_HIDE_SOLSTICE_FACES, true));
        sortFaces();
        loadingCustom = true;
    }

    public static Font getTextboxFont() {
        return textboxFont;
    }

    public static BufferedImage getTextboxImage() {
        return textboxImage;
    }

    public static BufferedImage getTextboxArrow() {
        return textboxArrow;
    }

    public static List<BufferedImage> getAppIcons() {
        return appIcons;
    }

    public static ImageIcon getIcon(final Icon icon) {
        return icons.get(icon);
    }

    public static String[] getFaces() {
        return faces.keySet().toArray(DUMMY_STRING_ARRAY);
    }

    public static Facepic getFace(final String name) {
        return faces.get(name);
    }

    public static String addFace(final File face) throws IOException {
        String faceName = face.getName();
        if (!faceName.contains(".")
                || !faceName.substring(faceName.lastIndexOf(".") + 1).equalsIgnoreCase("png"))
            return null;
        faceName = faceName.substring(0, faceName.lastIndexOf('.'));
        final BufferedImage image = ImageIO.read(face);
        faceName = addFace(faceName, face, image);
        return faceName;
    }

    public static String addFace(String name, final File file, final BufferedImage face) {
        final int width = face.getWidth(), height = face.getHeight();
        if ((width != 96 || height != 96) && (width != 48 || height != 48))
            throw new IllegalArgumentException("Face dimensions must be 96 by 96 or 48 by 48!");
        final String origName = name;
        int nameCount = 1;
        while (faces.containsKey(name)) {
            nameCount++;
            name = origName + nameCount;
        }
        faces.put(name, new Facepic(name, file, face, loadingCustom));
        return name;
    }

    public static void addFaces(final File dir, final boolean ignoreSolstice) throws IOException {
        if (dir.isFile()) {
            addFace(dir);
            return;
        }
        for (final File face : dir.listFiles())
            if (face.isDirectory()) {
                if (ignoreSolstice) {
                    if (!"solstice".equals(face.getName()))
                        addFaces(face, false);
                } else
                    addFaces(face, false);
            } else
                try {
                    addFace(face);
                } catch (final Exception e) {
                    Main.LOGGER.error("Error while loading facepic!", e);
                    JOptionPane.showMessageDialog(null,
                            "Could not load facepic " + face.getName() + "!\n(at " + face.getAbsolutePath() + ")\n" + e,
                            "Could not load facepic!", JOptionPane.ERROR_MESSAGE);
                }
    }

    public static void sortFaces() {
        faces = faces.entrySet().stream().sorted(Map.Entry.comparingByKey())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
    }

    public enum Icon {
        NEW_PROJECT("New Project"), LOAD_PROJECT("Load Project"), SAVE_PROJECT("Save Project"),
        SAVE_PROJECT_AS("Save Project As..."), EXIT("Exit"), CHECK_FOR_UPDATES("Check for Updates"), ABOUT("About"),
        FACE_FOLDER("Open Facepic Folder"), ADD_FACE("Add Custom Facepic"), ADD_TEXTBOX("Add Textbox"),
        REMOVE_TEXTBOX("Remove Textbox"), INSERT_TEXTBOX_BEFORE("Insert Textbox Before"),
        INSERT_TEXTBOX_AFTER("Insert Textbox After"), MOVE_TEXTBOX_UP("Move Textbox Up"),
        MOVE_TEXTBOX_DOWN("Move Textbox Down"), SETTINGS("Settings"), CLONE_TEXTBOX("Clone Textbox");

        String description;

        Icon(final String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    public static class Facepic {
        private final String name;
        private final File file;
        private final BufferedImage image;
        private final boolean custom;
        private ImageIcon icon;

        public Facepic(final String name, final File file, final BufferedImage image, final boolean custom) {
            this.name = name;
            this.file = file;
            this.image = image;
            makeIcon();
            this.custom = custom;
        }

        private void makeIcon() {
            final int smolSize = 48;
            final BufferedImage imageSmol = new BufferedImage(smolSize, smolSize, BufferedImage.TYPE_4BYTE_ABGR);
            final Graphics g = imageSmol.getGraphics();
            g.drawImage(image, 0, 0, smolSize, smolSize, null);
            icon = new ImageIcon(imageSmol, "small icon for face " + name);
        }

        public String getName() {
            return name;
        }

        public File getFile() {
            return file;
        }

        public BufferedImage getImage() {
            return image;
        }

        public ImageIcon getIcon() {
            return icon;
        }

        public boolean isCustom() {
            return custom;
        }
    }

}
