package com.leo.ostbm;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.leo.ostbm.Resources.Facepic;
import com.leo.ostbm.Resources.Icon;
import com.leo.ostbm.TextboxUtil.*;
import com.leo.ostbm.util.TableColumnAdjuster;
import org.jetbrains.annotations.NotNull;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.ImageWriter;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.metadata.IIOMetadataNode;
import javax.imageio.stream.ImageOutputStream;
import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.*;
import java.lang.reflect.Type;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;

import static com.leo.ostbm.TextboxUtil.*;

public class MakerPanel extends JPanel implements ActionListener, ListSelectionListener, ItemListener {

    public static final FileNameExtensionFilter TBPROJ_FILTER = new FileNameExtensionFilter("Project files", "tbproj");
    public static final Color COLOR_TEXTBOX = new Color(24, 12, 30);
    public static final Color COLOR_TEXTBOX_B = COLOR_TEXTBOX.brighter().brighter();
    public static final String HTMLC_TEXTBOX = ModifierHelpPanel.colorToHTML(COLOR_TEXTBOX);
    public static final String A_FACE_FOLDER = "faceFolder";
    public static final String A_ADD_FACE = "addFace";
    public static final String A_ADD_BOX = "addBox";
    public static final String A_CLONE_BOX = "cloneBox";
    public static final String A_INSERT_BOX_BEFORE = "insertBoxBefore";
    public static final String A_INSERT_BOX_AFTER = "insertBoxAfter";
    public static final String A_MOVE_BOX_UP = "moveBoxUp";
    public static final String A_MOVE_BOX_DOWN = "moveBoxDown";
    public static final String A_REMOVE_BOX = "removeBox";
    public static final String A_MODIFIER_HELP = "modifierHelp";
    public static final String A_MAKE_BOXES = "makeBoxes";
    public static final String A_MAKE_BOXES_ANIM = "makeBoxesAnim";
    private static final long serialVersionUID = 1L;

    private final List<Textbox> boxes;
    private File projectFile;
    private int currentBox;
    private JList<Textbox> boxSelect;
    private JComboBox<String> faceSelect;
    private JEditorPane textPane;

    public MakerPanel() {
        currentBox = 0;
        boxes = new LinkedList<>();
        boxes.add(new Textbox());
        initPanel();
    }

    private void initPanel() {
        setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
        setLayout(new BorderLayout());
        initBoxSelectPanel();
        initBoxEditPanel();
    }

    private void initBoxSelectPanel() {
        final JPanel boxSelectPanel = new JPanel();
        boxSelectPanel.setLayout(new BorderLayout());
        final JPanel boxControlPanel = new JPanel();
        boxControlPanel.setLayout(new GridLayout(1, 7));
        JButton addBoxButton = new JButton(Resources.getIcon(Icon.ADD_TEXTBOX));
        addBoxButton.addActionListener(this);
        addBoxButton.setActionCommand(A_ADD_BOX);
        addBoxButton.setToolTipText("Add a new textbox");
        boxControlPanel.add(addBoxButton);
        JButton cloneBoxButton = new JButton(Resources.getIcon(Icon.CLONE_TEXTBOX));
        cloneBoxButton.addActionListener(this);
        cloneBoxButton.setActionCommand(A_CLONE_BOX);
        cloneBoxButton.setToolTipText("Clone the currently selected textbox");
        boxControlPanel.add(cloneBoxButton);
        JButton insertBoxBeforeButton = new JButton(Resources.getIcon(Icon.INSERT_TEXTBOX_BEFORE));
        insertBoxBeforeButton.addActionListener(this);
        insertBoxBeforeButton.setActionCommand(A_INSERT_BOX_BEFORE);
        insertBoxBeforeButton.setToolTipText("Insert a textbox before the currently selected one");
        boxControlPanel.add(insertBoxBeforeButton);
        JButton insertBoxAfterButton = new JButton(Resources.getIcon(Icon.INSERT_TEXTBOX_AFTER));
        insertBoxAfterButton.addActionListener(this);
        insertBoxAfterButton.setActionCommand(A_INSERT_BOX_AFTER);
        insertBoxAfterButton.setToolTipText("Insert a textbox after the currently selected one");
        boxControlPanel.add(insertBoxAfterButton);
        JButton moveBoxUpButton = new JButton(Resources.getIcon(Icon.MOVE_TEXTBOX_UP));
        moveBoxUpButton.addActionListener(this);
        moveBoxUpButton.setActionCommand(A_MOVE_BOX_UP);
        moveBoxUpButton.setToolTipText("Move the currently selected textbox up");
        boxControlPanel.add(moveBoxUpButton);
        JButton moveBoxDownButton = new JButton(Resources.getIcon(Icon.MOVE_TEXTBOX_DOWN));
        moveBoxDownButton.addActionListener(this);
        moveBoxDownButton.setActionCommand(A_MOVE_BOX_DOWN);
        moveBoxDownButton.setToolTipText("Move the currently selected textbox down");
        boxControlPanel.add(moveBoxDownButton);
        JButton removeBoxButton = new JButton(Resources.getIcon(Icon.REMOVE_TEXTBOX));
        removeBoxButton.addActionListener(this);
        removeBoxButton.setActionCommand(A_REMOVE_BOX);
        removeBoxButton.setToolTipText("Remove the currently selected textbox");
        boxControlPanel.add(removeBoxButton);
        boxSelectPanel.add(boxControlPanel, BorderLayout.PAGE_START);
        boxSelect = new JList<>();
        boxSelect.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        updateBoxList();
        boxSelect.addListSelectionListener(this);
        boxSelect.setBackground(COLOR_TEXTBOX);
        boxSelect.setForeground(Color.WHITE);
        boxSelect.setCellRenderer(new TextboxListRenderer());
        final JScrollPane scroll = new JScrollPane(boxSelect);
        boxSelectPanel.add(scroll, BorderLayout.CENTER);
        add(boxSelectPanel, BorderLayout.LINE_START);
        final Dimension bsps = boxSelectPanel.getPreferredSize();
        boxSelectPanel.setPreferredSize(new Dimension(240, bsps.height));
    }

    private void initBoxEditPanel() {
        final JPanel boxEditPanel = new JPanel();
        boxEditPanel.setLayout(new BorderLayout());
        boxEditPanel.setBorder(BorderFactory.createEmptyBorder(0, 4, 0, 0));
        final JPanel faceSelectPanel = new JPanel();
        faceSelectPanel.setLayout(new BoxLayout(faceSelectPanel, BoxLayout.LINE_AXIS));
        faceSelectPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 4, 0));
        faceSelect = new JComboBox<>();
        updateFaces();
        faceSelect.addItemListener(this);
        faceSelect.setBackground(COLOR_TEXTBOX);
        faceSelect.setForeground(Color.WHITE);
        faceSelect.setRenderer(new FacesComboBoxRenderer());
        faceSelect.setToolTipText("Select a facepic to add to the textbox");
        faceSelectPanel.add(faceSelect);
        final JPanel faceControlPanel = new JPanel();
        faceControlPanel.setLayout(new BoxLayout(faceControlPanel, BoxLayout.PAGE_AXIS));
        JButton openFaceFolderButton = new JButton(Resources.getIcon(Icon.FACE_FOLDER));
        openFaceFolderButton.addActionListener(this);
        openFaceFolderButton.setActionCommand(A_FACE_FOLDER);
        String openFaceFolderTooltip = "Open the facepic folder";
        if (!Desktop.isDesktopSupported()) {
            openFaceFolderButton.setEnabled(false);
            openFaceFolderTooltip += "\n(this operation is not supported on your system...)";
        }
        openFaceFolderButton.setToolTipText(openFaceFolderTooltip);
        openFaceFolderButton.setPreferredSize(new Dimension(24, 24));
        faceControlPanel.add(openFaceFolderButton);
        JButton customFaceButton = new JButton(Resources.getIcon(Icon.ADD_FACE));
        customFaceButton.addActionListener(this);
        customFaceButton.setActionCommand(A_ADD_FACE);
        customFaceButton.setToolTipText("Add a facepic to the folder");
        customFaceButton.setPreferredSize(new Dimension(24, 24));
        faceControlPanel.add(customFaceButton);
        faceSelectPanel.add(faceControlPanel);
        boxEditPanel.add(faceSelectPanel, BorderLayout.PAGE_START);
        final JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BorderLayout());
        textPane = new TextboxEditorPane(this, boxes.get(currentBox).text);
        textPane.setBackground(COLOR_TEXTBOX);
        textPane.setCaretColor(Color.WHITE);
        centerPanel.add(new JScrollPane(textPane), BorderLayout.CENTER);
        JButton modifierHelpButton = new JButton("Modifier Help");
        modifierHelpButton.addActionListener(this);
        modifierHelpButton.setActionCommand(A_MODIFIER_HELP);
        centerPanel.add(modifierHelpButton, BorderLayout.PAGE_END);
        boxEditPanel.add(centerPanel, BorderLayout.CENTER);
        final JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new GridLayout(1, 2));
        JButton makeTextboxButton = new JButton("Generate Textbox(es)!");
        makeTextboxButton.addActionListener(this);
        makeTextboxButton.setActionCommand(A_MAKE_BOXES);
        bottomPanel.add(makeTextboxButton);
        JButton makeTextboxAnimButton = new JButton("Generate Animated Textbox(es)!");
        makeTextboxAnimButton.addActionListener(this);
        makeTextboxAnimButton.setActionCommand(A_MAKE_BOXES_ANIM);
        bottomPanel.add(makeTextboxAnimButton);
        boxEditPanel.add(bottomPanel, BorderLayout.PAGE_END);
        add(boxEditPanel, BorderLayout.CENTER);
    }

    public void updateCurrentBoxFace(@NotNull final Textbox box) {
        box.face = (String) faceSelect.getSelectedItem();
        boxSelect.repaint();
    }

    public void updateCurrentBoxFace() {
        updateCurrentBoxFace(boxes.get(currentBox));
    }

    public void updateCurrentBox() {
        final Textbox box = boxes.get(currentBox);
        box.text = textPane.getText();
        updateCurrentBoxFace(box);
    }

    public void updateBoxComponents(final boolean updateFaceSelect) {
        final Textbox box = boxes.get(currentBox);
        if (updateFaceSelect)
            faceSelect.setSelectedItem(box.face);
        textPane.setText(box.text);
    }

    public void updateBoxComponents() {
        updateBoxComponents(true);
    }

    public void updateBoxList() {
        final DefaultListModel<Textbox> boxSelectModel = new DefaultListModel<>();
        for (final Textbox b : boxes)
            boxSelectModel.addElement(b);
        boxSelect.setModel(boxSelectModel);
        boxSelect.setSelectedIndex(currentBox);
        boxSelect.ensureIndexIsVisible(currentBox);
    }

    public void updateFaces() {
        DefaultComboBoxModel<String> faceSelectModel = new DefaultComboBoxModel<>(Resources.getFaces());
        faceSelect.setModel(faceSelectModel);
        for (final Textbox box : boxes)
            if (faceSelectModel.getIndexOf(box.face) == -1)
                box.face = Resources.FACE_BLANK;
        faceSelect.setSelectedItem(boxes.get(currentBox).face);
    }

    public boolean isProjectEmpty() {
        updateCurrentBox();
        boolean emptyProject = true;
        for (final Textbox box : boxes)
            if (!box.text.isEmpty() || !Resources.FACE_BLANK.equals(box.face)) {
                emptyProject = false;
                break;
            }
        return emptyProject;
    }

    public void newProjectFile() throws IOException {
        updateCurrentBox();
        if (!isProjectEmpty()) {
            final int result = JOptionPane.showConfirmDialog(this,
                    "Do you want to save the current project before creating a new project?",
                    "Save before creating new project?", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
            if (result == JOptionPane.CANCEL_OPTION)
                return;
            else if (result == JOptionPane.YES_OPTION)
                saveProjectFile(null, false);
        }
        projectFile = null;
        boxes.clear();
        boxes.add(new Textbox());
        currentBox = 0;
        updateBoxComponents();
        updateBoxList();
    }

    public void saveProjectFile(File dest, final boolean saveAs) throws IOException {
        updateCurrentBox();
        if (dest == null || saveAs) {
            if (projectFile == null || saveAs) {
                final File sel = DialogUtil.openFileDialog(true, this, "Save project file", TBPROJ_FILTER);
                if (sel == null)
                    return;
                projectFile = sel;
            }
            dest = projectFile;
        }
        if (dest.exists() && !dest.equals(projectFile)) {
            final int result = JOptionPane.showConfirmDialog(this,
                    "File \"" + dest + "\" already exists.\nOverwrite it?", "Destination file exists",
                    JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (result == JOptionPane.NO_OPTION)
                return;
            if (!dest.delete())
                throw new IOException("could not delete file");
            if (!dest.createNewFile())
                throw new IOException("could not create new file");
        }
        final Gson gson = new GsonBuilder().setPrettyPrinting().create();
        try (FileWriter fw = new FileWriter(dest); BufferedWriter writer = new BufferedWriter(fw)) {
            gson.toJson(boxes, writer);
        }
        projectFile = dest;
    }

    public void loadProjectFile(File src) throws IOException {
        final boolean emptyProject = isProjectEmpty();
        if (!emptyProject) {
            final int result = JOptionPane.showConfirmDialog(this,
                    "Do you want to save the current project before loading another project?",
                    "Save before loading other project?", JOptionPane.YES_NO_CANCEL_OPTION,
                    JOptionPane.WARNING_MESSAGE);
            if (result == JOptionPane.CANCEL_OPTION)
                return;
            else if (result == JOptionPane.YES_OPTION)
                saveProjectFile(null, false);
        }
        if (src == null) {
            src = DialogUtil.openFileDialog(false, this, "Load project file", TBPROJ_FILTER);
            if (src == null)
                return;
        }
        final Gson gson = new GsonBuilder().create();
        try (FileReader fr = new FileReader(src); BufferedReader reader = new BufferedReader(fr)) {
            final Type type = new TypeToken<List<Textbox>>() {
            }.getType();
            final List<Textbox> temp = gson.fromJson(reader, type);
            boolean ignoreFacepicErrors = false;
            for (int i = 0; i < temp.size(); i++) {
                final Textbox box = temp.get(i);
                if (Resources.getFace(box.face) == null)
                    if (ignoreFacepicErrors)
                        box.face = Resources.FACE_BLANK;
                    else {
                        final int result = DialogUtil.showCustomConfirmDialog(this, "Textbox " + (i + 1)
                                        + " specifies a facepic that isn't currently loaded: \"" + box.face
                                        + "\"\nPress \"Abort\" (or close this dialog box) to stop loading this project file,\n\"Ignore\" to ignore this error,\nor \"Ingore All\" to ignore this error and all future errors of this type."
                                        + "\nPlease note that ignoring textboxes with this error will remove their facepic.",
                                "Missing facepic", new String[]{"Abort", "Ignore", "Ignore All"},
                                JOptionPane.ERROR_MESSAGE);
                        switch (result) {
                            case JOptionPane.CLOSED_OPTION:
                            case 0: // Abort
                            default:
                                return;
                            case 2: // Ignore All
                                ignoreFacepicErrors = true;
                            case 1: // Ignore
                                box.face = Resources.FACE_BLANK;
                                break;
                        }
                    }
            }
            boxes.clear();
            boxes.addAll(temp);
            currentBox = 0;
            updateBoxComponents(true);
            updateBoxList();
            projectFile = src;
        }
    }

    @Override
    public void actionPerformed(@NotNull final ActionEvent e) {
        final String a = e.getActionCommand();
        JFileChooser fc;
        int ret;
        Textbox box, copyBox;
        LoadFrame gf;
        final boolean cloneFacepics = Config.getBoolean(Config.KEY_COPY_FACEPICS, true);
        final Frame parent = (Frame) SwingUtilities.getWindowAncestor(this);
        switch (a) {
            case A_FACE_FOLDER:
                if (!Desktop.isDesktopSupported())
                    return;
                try {
                    Desktop.getDesktop().browse(new File("res/faces").toURI());
                } catch (final IOException e2) {
                    Main.LOGGER.error("Error while browsing to face folder!", e2);
                }
                break;
            case A_ADD_FACE:
                fc = new JFileChooser();
                fc.setMultiSelectionEnabled(true);
                fc.setAcceptAllFileFilterUsed(false);
                fc.setDialogTitle("Open face image(s)");
                fc.setFileFilter(new FileNameExtensionFilter("PNG files", "png"));
                fc.setCurrentDirectory(new File(System.getProperty("user.dir")));
                ret = fc.showOpenDialog(this);
                if (ret == JFileChooser.APPROVE_OPTION) {
                    final File[] sels = fc.getSelectedFiles();
                    boolean loadedOne = false;
                    for (final File sel : sels) {
                        String faceName = sel.getName();
                        faceName = faceName.substring(0, faceName.lastIndexOf('.'));
                        try {
                            final BufferedImage image = ImageIO.read(sel);
                            try {
                                Resources.addCustomFace(faceName, sel, image);
                            } catch (final IllegalArgumentException exc) {
                                JOptionPane.showMessageDialog(this, exc.getMessage(), "Bad face dimensions!",
                                        JOptionPane.ERROR_MESSAGE);
                                continue;
                            }
                        } catch (final IOException e1) {
                            Main.LOGGER.error("Error while loading facepic!", e1);
                            JOptionPane.showMessageDialog(this, "An exception occured while loading the facepic:\n" + e1,
                                    "Couldn't load facepic!", JOptionPane.ERROR_MESSAGE);
                            continue;
                        }
                        loadedOne = true;
                    }
                    if (loadedOne)
                        updateFaces();
                }
                break;
            case A_ADD_BOX:
                updateCurrentBox();
                copyBox = boxes.get(boxes.size() - 1);
                box = new Textbox();
                if (cloneFacepics)
                    box.face = copyBox.face;
                boxes.add(box);
                currentBox = boxes.size() - 1;
                updateBoxComponents();
                updateBoxList();
                break;
            case A_CLONE_BOX:
                updateCurrentBox();
                box = new Textbox(boxes.get(currentBox));
                boxes.add(currentBox, box);
                currentBox = boxes.size() - 1;
                updateBoxComponents();
                updateBoxList();
                break;
            case A_INSERT_BOX_BEFORE:
                updateCurrentBox();
                copyBox = boxes.get(currentBox);
                box = new Textbox();
                if (cloneFacepics)
                    box.face = copyBox.face;
                boxes.add(currentBox, box);
                updateBoxComponents();
                updateBoxList();
                break;
            case A_INSERT_BOX_AFTER:
                updateCurrentBox();
                copyBox = boxes.get(currentBox);
                box = new Textbox();
                if (cloneFacepics)
                    box.face = copyBox.face;
                boxes.add(++currentBox, box);
                updateBoxComponents();
                updateBoxList();
                break;
            case A_MOVE_BOX_UP:
                updateCurrentBox();
                box = boxes.get(currentBox);
                boxes.remove(box);
                currentBox--;
                if (currentBox < 0)
                    currentBox = 0;
                boxes.add(currentBox, box);
                updateBoxComponents();
                updateBoxList();
                break;
            case A_MOVE_BOX_DOWN:
                updateCurrentBox();
                box = boxes.get(currentBox);
                boxes.remove(box);
                currentBox++;
                if (currentBox > boxes.size())
                    currentBox = boxes.size();
                boxes.add(currentBox, box);
                updateBoxComponents();
                updateBoxList();
                break;
            case A_REMOVE_BOX:
                updateCurrentBox();
                box = boxes.get(currentBox);
                if (!box.text.isEmpty()) {
                    final int result = JOptionPane.showConfirmDialog(this,
                            "Are you sure you want to delete textbox " + (currentBox + 1) + "?", "Confirm deleting textbox",
                            JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                    if (result != JOptionPane.YES_OPTION)
                        break;
                }
                boxes.remove(box);
                if (boxes.size() == 0)
                    boxes.add(new Textbox());
                if (currentBox == boxes.size())
                    currentBox--;
                if (currentBox < 0)
                    currentBox = 0;
                updateBoxComponents();
                updateBoxList();
                break;
            case A_MODIFIER_HELP:
                final JDialog modFrame = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Modifier Help", true);
                modFrame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
                modFrame.setResizable(false);
                modFrame.add(new ModifierHelpPanel());
                modFrame.pack();
                modFrame.setLocationRelativeTo(null);
                modFrame.setVisible(true);
                break;
            case A_MAKE_BOXES:
                updateCurrentBox();
                final BufferedImage boxesImage = new BufferedImage(608, 128 * boxes.size() + 2 * (boxes.size() - 1),
                        BufferedImage.TYPE_4BYTE_ABGR);
                final Graphics big = boxesImage.getGraphics();
                if (!checkTextboxes())
                    return;
                gf = new LoadFrame("Generating textbox(es)...", false);
                SwingUtilities.invokeLater(() -> {
                    if (Config.getBoolean(Config.KEY_OPAQUE_TEXTBOXES, false))
                        TextboxUtil.setTextboxImage(Resources.getTextboxImageOpaque());
                    else
                        TextboxUtil.setTextboxImage(Resources.getTextboxImage());
                    for (int i = 0; i < boxes.size(); i++) {
                        final Textbox b = boxes.get(i);
                        drawTextbox(big, b.face, b.text, 0, 130 * i, i < boxes.size() - 1);
                    }
                    gf.dispose();
                    final JDialog previewFrame = new JDialog(parent, "Textbox(es) preview", true);
                    previewFrame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
                    previewFrame.setResizable(false);
                    previewFrame.add(new PreviewPanel(boxesImage));
                    previewFrame.pack();
                    previewFrame.setLocationRelativeTo(null);
                    previewFrame.setVisible(true);
                });
                break;
            case A_MAKE_BOXES_ANIM:
                updateCurrentBox();
                if (!checkTextboxes())
                    return;
                gf = new LoadFrame("Generating textbox(es)...", false);
                SwingUtilities.invokeLater(() -> {
                    byte[] data;
                    Image image;
                    final List<BufferedImage> boxFrames = makeTextboxAnimation(boxes);
                    try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
                         ImageOutputStream ios = ImageIO.createImageOutputStream(baos)) {
                        final ImageWriter writer = ImageIO.getImageWritersByFormatName("gif").next();
                        final IIOMetadata meta = writer.getDefaultImageMetadata(
                                ImageTypeSpecifier.createFromBufferedImageType(boxFrames.get(0).getType()),
                                writer.getDefaultWriteParam());
                        final String metaFormatName = meta.getNativeMetadataFormatName();
                        final IIOMetadataNode root = (IIOMetadataNode) meta.getAsTree(metaFormatName);
                        final IIOMetadataNode graphicsControl = getNode(root, "GraphicControlExtension");
                        graphicsControl.setAttribute("disposalMethod", "none");
                        graphicsControl.setAttribute("userInputFlag", "FALSE");
                        graphicsControl.setAttribute("transparentColorFlag", "FALSE");
                        graphicsControl.setAttribute("transparentColorIndex", "0");
                        graphicsControl.setAttribute("delayTime", "5");
                        final IIOMetadataNode comments = getNode(root, "CommentExtensions");
                        comments.setAttribute("CommentExtension",
                                "Animated OneShot-style textbox, generated using OneShot Textbox Maker by Leo\n" +
                                        "https://github.com/Leo40Git/OneShot-Textbox-Maker");
                        final IIOMetadataNode application = getNode(root, "ApplicationExtensions");
                        final IIOMetadataNode child = new IIOMetadataNode("ApplicationExtension");
                        child.setAttribute("applicationID", "NETSCAPE");
                        child.setAttribute("authenticationCode", "2.0");
                        child.setUserObject(new byte[]{1, 0, 0});
                        application.appendChild(child);
                        meta.setFromTree(metaFormatName, root);
                        writer.setOutput(ios);
                        writer.prepareWriteSequence(null);
                        for (final BufferedImage frame : boxFrames) {
                            writer.writeToSequence(new IIOImage(frame, null, meta), writer.getDefaultWriteParam());
                        }
                        writer.endWriteSequence();
                        ios.flush();
                        data = baos.toByteArray();
                        image = Toolkit.getDefaultToolkit().createImage(data);
                    } catch (final IOException ex) {
                        Main.LOGGER.error("Error while generating animation!", ex);
                        JOptionPane.showMessageDialog(this, "An exception occurred while generating the animation:\n" + ex,
                                "Couldn't generate animation!", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    gf.dispose();
                    assert image != null;
                    final ImageIcon preview = new ImageIcon(image);
                    final JDialog previewFrame = new JDialog(parent, "Textbox(es) preview", true);
                    previewFrame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
                    previewFrame.setResizable(false);
                    AnimationPreviewAL al;
                    final PreviewPanel previewPanel = new PreviewPanel(preview, al = new AnimationPreviewAL(data));
                    al.setParent(previewPanel);
                    previewFrame.add(previewPanel);
                    previewFrame.pack();
                    previewFrame.setLocationRelativeTo(null);
                    previewFrame.setVisible(true);
                });
                break;
            default:
                Main.LOGGER.debug("Undefined action: " + a);
                break;
        }
    }

    private IIOMetadataNode getNode(@NotNull final IIOMetadataNode rootNode, final String nodeName) {
        final int nNodes = rootNode.getLength();
        for (int i = 0; i < nNodes; i++)
            if (rootNode.item(i).getNodeName().compareToIgnoreCase(nodeName) == 0)
                return (IIOMetadataNode) rootNode.item(i);
        final IIOMetadataNode node = new IIOMetadataNode(nodeName);
        rootNode.appendChild(node);
        return node;
    }

    private boolean checkTextboxes() {
        final String[] columnNames = new String[]{"Box #", "Line#", "Description"};
        final Vector<String[]> errors = new Vector<>();
        for (int i = 0; i < boxes.size(); i++) {
            final Textbox b = boxes.get(i);
            final ParsedTextbox tpd = parseTextbox(b.face, b.text);
            for (final TextboxError err : tpd.errors)
                errors.add(new String[]{Integer.toString(i + 1), err.lineNum < 0 ? "N/A" : Integer.toString(err.lineNum + 1), err.message});
            if (errors.isEmpty())
                return true;
        }
        final JTable errorTable = new JTable(new AbstractTableModel() {

            private static final long serialVersionUID = 1L;

            @Override
            public int getRowCount() {
                return errors.size();
            }

            @Override
            public int getColumnCount() {
                return columnNames.length;
            }

            @Override
            public String getColumnName(final int column) {
                return columnNames[column];
            }

            @Override
            public Object getValueAt(final int rowIndex, final int columnIndex) {
                final String[] row = errors.get(rowIndex);
                return row[columnIndex];
            }

            @Override
            public boolean isCellEditable(final int rowIndex, final int columnIndex) {
                return false;
            }

        });
        final JTableHeader header = errorTable.getTableHeader();
        header.setResizingAllowed(false);
        header.setReorderingAllowed(false);
        final TableColumnAdjuster tca = new TableColumnAdjuster(errorTable);
        tca.adjustColumns();
        final JDialog errFrame = new JDialog((Frame) SwingUtilities.getWindowAncestor(this),
                "Errors while generating textbox(es)", true);
        errFrame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        final Dimension size = new Dimension(652, 280);
        errFrame.setMinimumSize(size);
        errFrame.setPreferredSize(size);
        errFrame.setMaximumSize(size);
        errFrame.setResizable(false);
        final JPanel errPanel = new JPanel();
        errPanel.setLayout(new BorderLayout());
        errPanel.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
        errPanel.add(new JLabel(
                        "There were errors while the textbox(es) were being generated. Please fix these errors and try again."),
                BorderLayout.NORTH);
        errPanel.add(new JScrollPane(errorTable), BorderLayout.CENTER);
        errFrame.add(errPanel);
        errFrame.pack();
        errFrame.setLocationRelativeTo(null);
        errFrame.setVisible(true);
        return false;
    }

    @Override
    public void valueChanged(@NotNull final ListSelectionEvent e) {
        if (e.getSource().equals(boxSelect)) {
            updateCurrentBox();
            final int sel = boxSelect.getSelectedIndex();
            if (sel < 0)
                return;
            currentBox = sel;
            updateBoxComponents();
        }
    }

    @Override
    public void itemStateChanged(@NotNull final ItemEvent e) {
        if (e.getSource().equals(faceSelect)) {
            updateCurrentBoxFace();
            updateBoxComponents(false);
        }
    }

    static class TextboxListRenderer extends JLabel implements ListCellRenderer<Textbox> {

        private static final long serialVersionUID = 1L;

        private static final BufferedImage IMAGE = new BufferedImage(1, 1, BufferedImage.TYPE_4BYTE_ABGR);

        public TextboxListRenderer() {
            setOpaque(true);
            setHorizontalAlignment(LEFT);
            setVerticalAlignment(CENTER);

        }

        @Override
        public Component getListCellRendererComponent(final JList<? extends Textbox> list, final Textbox value,
                                                      final int index, final boolean isSelected, final boolean cellHasFocus) {
            if (isSelected)
                setBackground(COLOR_TEXTBOX_B);
            else
                setBackground(COLOR_TEXTBOX);
            final String text = "<html><p style=\"color:white;\">Textbox " + (index + 1)
                    + "</p><p style=\"color:gray;\"><i>" + value.toString() + "</i></p></html>";
            setText(text);
            final ImageIcon icon = Resources.getFace(value.face).getIcon();
            setIcon(icon);
            // getGraphics() returns null here so we need to make our own Graphics object
            final Graphics g = IMAGE.getGraphics();
            g.setFont(getFont());
            final Rectangle2D bounds = g.getFontMetrics().getStringBounds(text, g);
            setPreferredSize(new Dimension(100 + icon.getIconWidth(),
                    Math.max((int) bounds.getHeight() + g.getFontMetrics().getHeight(), icon.getIconHeight())));
            return this;
        }

    }

    static class FacesComboBoxRenderer extends JLabel implements ListCellRenderer<String> {

        private static final long serialVersionUID = 1L;

        public FacesComboBoxRenderer() {
            setOpaque(true);
            setHorizontalAlignment(LEFT);
            setVerticalAlignment(CENTER);
        }

        @Override
        public Component getListCellRendererComponent(final JList<? extends String> list, final String value,
                                                      final int index, final boolean isSelected, final boolean cellHasFocus) {
            if (isSelected)
                setBackground(COLOR_TEXTBOX_B);
            else
                setBackground(COLOR_TEXTBOX);
            final Facepic face = Resources.getFace(value);
            final ImageIcon faceIcon = face.getIcon();
            if (faceIcon == null)
                setIcon(Resources.getFace(Resources.FACE_BLANK).getIcon());
            else
                setIcon(faceIcon);
            String text = "<html><p style=\"color:white;\">" + face.getName();
            final File faceFile = face.getFile();
            if (faceFile != null)
                text += "</p><p style=\"color:gray;\"><i>" + faceFile.getPath() + "</i>";
            text += "</p></html>";
            setText(text);
            return this;
        }

    }

    static class TextboxEditorPane extends JEditorPane {

        private static final long serialVersionUID = 1L;
        private final MakerPanel panel;
        private final SimpleAttributeSet styleNormal, styleMod, styleOver;

        public TextboxEditorPane(final MakerPanel panel, final String text) {
            super();
            this.panel = panel;
            setEditorKit(new TextboxEditorKit());
            final StyledDocument doc = new DefaultStyledDocument() {
                private static final long serialVersionUID = 1L;

                @Override
                public void insertString(final int offs, String str, final AttributeSet a) throws BadLocationException {
                    str = str.replaceAll("\t", "    ");
                    super.insertString(offs, str, a);
                }
            };
            setDocument(doc);
            final Font font = new Font("Terminus", Font.BOLD, 20);
            styleNormal = new SimpleAttributeSet();
            StyleConstants.setFontFamily(styleNormal, font.getFamily());
            StyleConstants.setFontSize(styleNormal, font.getSize());
            StyleConstants.setForeground(styleNormal, Color.WHITE);
            StyleConstants.setBold(styleNormal, font.isBold());
            styleMod = new SimpleAttributeSet();
            StyleConstants.setFontFamily(styleMod, font.getFamily());
            StyleConstants.setFontSize(styleMod, font.getSize());
            StyleConstants.setForeground(styleMod, Color.GRAY);
            StyleConstants.setBold(styleMod, font.isBold());
            styleOver = new SimpleAttributeSet();
            StyleConstants.setFontFamily(styleOver, font.getFamily());
            StyleConstants.setFontSize(styleOver, font.getSize());
            StyleConstants.setBackground(styleOver, Color.RED);
            StyleConstants.setForeground(styleOver, Color.WHITE);
            StyleConstants.setBold(styleOver, font.isBold());
            setText(text);
            addKeyListener(new KeyAdapter() {
                @Override
                public void keyReleased(final KeyEvent e) {
                    panel.updateCurrentBox();
                    highlight();
                }
            });
        }

        @Override
        public void setText(final String t) {
            super.setText(t);
            highlight();
        }

        @Override
        public void paint(Graphics g) {
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
            super.paint(g2d);
        }

        private void highlight() {
            final Document doc = getDocument();
            if (doc instanceof StyledDocument) {
                final StyledDocument stylDoc = (StyledDocument) doc;
                MutableAttributeSet style = styleNormal;
                stylDoc.setParagraphAttributes(0, doc.getLength(), style, true);
                stylDoc.setCharacterAttributes(0, doc.getLength(), style, true);
                ParsedTextbox tpd = null;
                try {
                    tpd = TextboxUtil.parseTextbox(panel.boxes.get(panel.currentBox).face,
                            stylDoc.getText(0, doc.getLength()));
                } catch (final BadLocationException e) {
                    Main.LOGGER.catching(e);
                }
                if (tpd != null) {
                    for (final StyleSpan span : tpd.styleSpans) {
                        switch (span.type) {
                            case ERROR:
                                style = styleOver;
                                break;
                            case MODIFIER:
                                style = styleMod;
                                break;
                            case NORMAL:
                                style = new SimpleAttributeSet(styleNormal);
                                if (span.color != null)
                                    StyleConstants.setForeground(style, span.color);
                                StyleConstants.setBold(style, !span.format.contains("b"));
                                StyleConstants.setItalic(style, span.format.contains("i"));
                                StyleConstants.setUnderline(style, span.format.contains("u"));
                                StyleConstants.setStrikeThrough(style, span.format.contains("s"));
                                break;
                            default:
                                break;
                        }
                        stylDoc.setCharacterAttributes(span.pos, span.length, style, true);
                    }
                }
            }
        }

        static class TextboxEditorKit extends DefaultEditorKit {

            private static final long serialVersionUID = 1L;

            @Override
            public ViewFactory getViewFactory() {
                return new TextboxViewFactory();
            }

            static class TextboxViewFactory implements ViewFactory {

                @Override
                public View create(@NotNull final Element elem) {
                    final String kind = elem.getName();
                    switch (kind) {
                        case AbstractDocument.ContentElementName:
                            return new LabelView(elem);
                        case AbstractDocument.ParagraphElementName:
                            return new TextboxParagraphView(elem);
                        case AbstractDocument.SectionElementName:
                            return new BoxView(elem, View.Y_AXIS);
                        case StyleConstants.ComponentElementName:
                            return new ComponentView(elem);
                        case StyleConstants.IconElementName:
                            return new IconView(elem);
                        default:
                            break;
                    }
                    return new LabelView(elem);
                }

                static class TextboxParagraphView extends ParagraphView {

                    public TextboxParagraphView(final Element elem) {
                        super(elem);
                    }

                    @Override
                    protected void layout(final int width, final int height) {
                        super.layout(Short.MAX_VALUE, height);
                    }

                    @Override
                    public float getMinimumSpan(final int axis) {
                        return super.getPreferredSpan(axis);
                    }

                }

            }

        }

    }

}
