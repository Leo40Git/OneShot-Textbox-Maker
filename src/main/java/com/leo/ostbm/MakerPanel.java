package com.leo.ostbm;

import static com.leo.ostbm.TextboxUtil.*;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.ImageWriter;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.metadata.IIOMetadataNode;
import javax.imageio.stream.ImageOutputStream;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.BoxView;
import javax.swing.text.ComponentView;
import javax.swing.text.DefaultEditorKit;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.Document;
import javax.swing.text.Element;
import javax.swing.text.IconView;
import javax.swing.text.LabelView;
import javax.swing.text.ParagraphView;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import javax.swing.text.View;
import javax.swing.text.ViewFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.leo.ostbm.Resources.Facepic;
import com.leo.ostbm.Resources.Icon;
import com.leo.ostbm.TextboxUtil.Textbox;
import com.leo.ostbm.TextboxUtil.TextboxModifier;
import com.leo.ostbm.TextboxUtil.TextboxParseData;
import com.leo.ostbm.util.TableColumnAdjuster;

public class MakerPanel extends JPanel implements ActionListener, ListSelectionListener, ItemListener {

	private static final long serialVersionUID = 1L;

	public static final FileNameExtensionFilter TBPROJ_FILTER = new FileNameExtensionFilter("Project files", "tbproj");

	public static final Color COLOR_TEXTBOX = Color.decode("0x180C1E");
	public static final Color COLOR_TEXTBOX_B = COLOR_TEXTBOX.brighter().brighter();

	public static final String A_FACE_FOLDER = "faceFolder";
	public static final String A_CUSTOM_FACE = "customFace";
	public static final String A_ADD_BOX = "addBox";
	public static final String A_INSERT_BOX_BEFORE = "insertBoxBefore";
	public static final String A_INSERT_BOX_AFTER = "insertBoxAfter";
	public static final String A_MOVE_BOX_UP = "moveBoxUp";
	public static final String A_MOVE_BOX_DOWN = "moveBoxDown";
	public static final String A_REMOVE_BOX = "removeBox";
	public static final String A_MODIFIER_HELP = "modifierHelp";
	public static final String A_MAKE_BOXES = "makeBoxes";
	public static final String A_MAKE_BOXES_ANIM = "makeBoxesAnim";

	private File projectFile;
	private int currentBox;
	private List<Textbox> boxes;

	private JList<Textbox> boxSelect;
	private JButton addBoxButton, insertBoxBeforeButton, insertBoxAfterButton, moveBoxUpButton, moveBoxDownButton,
			removeBoxButton;

	private JComboBox<String> faceSelect;
	private DefaultComboBoxModel<String> faceSelectModel;
	private JButton openFaceFolderButton, customFaceButton;
	private JEditorPane textPane;
	private JButton modifierHelpButton, makeTextboxButton, makeTextboxAnimButton;

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
		JPanel boxSelectPanel = new JPanel();
		boxSelectPanel.setLayout(new BorderLayout());
		JPanel boxControlPanel = new JPanel();
		boxControlPanel.setLayout(new GridLayout(1, 6));
		addBoxButton = new JButton(Resources.getIcon(Icon.ADD_TEXTBOX));
		addBoxButton.addActionListener(this);
		addBoxButton.setActionCommand(A_ADD_BOX);
		addBoxButton.setToolTipText("Add a new textbox");
		boxControlPanel.add(addBoxButton);
		insertBoxBeforeButton = new JButton(Resources.getIcon(Icon.INSERT_TEXTBOX_BEFORE));
		insertBoxBeforeButton.addActionListener(this);
		insertBoxBeforeButton.setActionCommand(A_INSERT_BOX_BEFORE);
		insertBoxBeforeButton.setToolTipText("Insert a textbox before the currently selected one");
		boxControlPanel.add(insertBoxBeforeButton);
		insertBoxAfterButton = new JButton(Resources.getIcon(Icon.INSERT_TEXTBOX_AFTER));
		insertBoxAfterButton.addActionListener(this);
		insertBoxAfterButton.setActionCommand(A_INSERT_BOX_AFTER);
		insertBoxAfterButton.setToolTipText("Insert a textbox after the currently selected one");
		boxControlPanel.add(insertBoxAfterButton);
		moveBoxUpButton = new JButton(Resources.getIcon(Icon.MOVE_TEXTBOX_UP));
		moveBoxUpButton.addActionListener(this);
		moveBoxUpButton.setActionCommand(A_MOVE_BOX_UP);
		moveBoxUpButton.setToolTipText("Move the currently selected textbox up");
		boxControlPanel.add(moveBoxUpButton);
		moveBoxDownButton = new JButton(Resources.getIcon(Icon.MOVE_TEXTBOX_DOWN));
		moveBoxDownButton.addActionListener(this);
		moveBoxDownButton.setActionCommand(A_MOVE_BOX_DOWN);
		moveBoxDownButton.setToolTipText("Move the currently selected textbox down");
		boxControlPanel.add(moveBoxDownButton);
		removeBoxButton = new JButton(Resources.getIcon(Icon.REMOVE_TEXTBOX));
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
		JScrollPane scroll = new JScrollPane(boxSelect);
		boxSelectPanel.add(scroll, BorderLayout.CENTER);
		add(boxSelectPanel, BorderLayout.LINE_START);
		Dimension bsps = boxSelectPanel.getPreferredSize();
		boxSelectPanel.setPreferredSize(new Dimension(240, bsps.height));
	}

	private void initBoxEditPanel() {
		JPanel boxEditPanel = new JPanel();
		boxEditPanel.setLayout(new BorderLayout());
		boxEditPanel.setBorder(BorderFactory.createEmptyBorder(0, 4, 0, 0));
		JPanel faceSelectPanel = new JPanel();
		faceSelectPanel.setLayout(new BoxLayout(faceSelectPanel, BoxLayout.LINE_AXIS));
		faceSelectPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 4, 0));
		faceSelect = new JComboBox<String>();
		updateFaces();
		faceSelect.addItemListener(this);
		faceSelect.setBackground(COLOR_TEXTBOX);
		faceSelect.setForeground(Color.WHITE);
		faceSelect.setRenderer(new FacesComboBoxRenderer());
		faceSelect.setToolTipText("Select a facepic to add to the textbox");
		faceSelectPanel.add(faceSelect);
		JPanel faceControlPanel = new JPanel();
		faceControlPanel.setLayout(new BoxLayout(faceControlPanel, BoxLayout.PAGE_AXIS));
		openFaceFolderButton = new JButton(Resources.getIcon(Icon.FACE_FOLDER));
		openFaceFolderButton.addActionListener(this);
		openFaceFolderButton.setActionCommand(A_FACE_FOLDER);
		openFaceFolderButton.setToolTipText("Open the facepic folder");
		openFaceFolderButton.setPreferredSize(new Dimension(24, 24));
		faceControlPanel.add(openFaceFolderButton);
		customFaceButton = new JButton(Resources.getIcon(Icon.ADD_FACE));
		customFaceButton.addActionListener(this);
		customFaceButton.setActionCommand(A_CUSTOM_FACE);
		customFaceButton.setToolTipText("Add a custom facepic");
		customFaceButton.setPreferredSize(new Dimension(24, 24));
		faceControlPanel.add(customFaceButton);
		faceSelectPanel.add(faceControlPanel);
		boxEditPanel.add(faceSelectPanel, BorderLayout.PAGE_START);
		JPanel centerPanel = new JPanel();
		centerPanel.setLayout(new BorderLayout());
		textPane = new TextboxEditorPane(this, boxes.get(currentBox).text);
		textPane.setBackground(COLOR_TEXTBOX);
		textPane.setCaretColor(Color.WHITE);
		centerPanel.add(new JScrollPane(textPane), BorderLayout.CENTER);
		modifierHelpButton = new JButton("Modifier Help");
		modifierHelpButton.addActionListener(this);
		modifierHelpButton.setActionCommand(A_MODIFIER_HELP);
		centerPanel.add(modifierHelpButton, BorderLayout.PAGE_END);
		boxEditPanel.add(centerPanel, BorderLayout.CENTER);
		JPanel bottomPanel = new JPanel();
		bottomPanel.setLayout(new GridLayout(1, 2));
		makeTextboxButton = new JButton("Generate Textbox(es)!");
		makeTextboxButton.addActionListener(this);
		makeTextboxButton.setActionCommand(A_MAKE_BOXES);
		bottomPanel.add(makeTextboxButton);
		makeTextboxAnimButton = new JButton("Generate Animated Textbox(es)!");
		makeTextboxAnimButton.addActionListener(this);
		makeTextboxAnimButton.setActionCommand(A_MAKE_BOXES_ANIM);
		bottomPanel.add(makeTextboxAnimButton);
		boxEditPanel.add(bottomPanel, BorderLayout.PAGE_END);
		add(boxEditPanel, BorderLayout.CENTER);
	}

	public void updateCurrentBoxFace(Textbox box) {
		box.face = (String) faceSelect.getSelectedItem();
		boxSelect.repaint();
	}

	public void updateCurrentBoxFace() {
		updateCurrentBoxFace(boxes.get(currentBox));
	}

	public void updateCurrentBox() {
		Textbox box = boxes.get(currentBox);
		box.text = textPane.getText();
		updateCurrentBoxFace(box);
	}

	public void updateBoxComponents(boolean updateFaceSelect) {
		Textbox box = boxes.get(currentBox);
		if (updateFaceSelect)
			faceSelect.setSelectedItem(box.face);
		textPane.setText(box.text);
	}

	public void updateBoxComponents() {
		updateBoxComponents(true);
	}

	public void updateBoxList() {
		DefaultListModel<Textbox> boxSelectModel = new DefaultListModel<>();
		for (Textbox b : boxes)
			boxSelectModel.addElement(b);
		boxSelect.setModel(boxSelectModel);
		boxSelect.setSelectedIndex(currentBox);
		boxSelect.ensureIndexIsVisible(currentBox);
	}

	public void updateFaces() {
		faceSelectModel = new DefaultComboBoxModel<>(Resources.getFaces());
		faceSelect.setModel(faceSelectModel);
		for (Textbox box : boxes)
			if (faceSelectModel.getIndexOf(box.face) == -1)
				box.face = Resources.FACE_BLANK;
		faceSelect.setSelectedItem(boxes.get(currentBox).face);
	}

	public boolean isProjectEmpty() {
		updateCurrentBox();
		boolean emptyProject = true;
		for (Textbox box : boxes) {
			if (!box.text.isEmpty() || !Resources.FACE_BLANK.equals(box.face)) {
				emptyProject = false;
				break;
			}
		}
		return emptyProject;
	}

	public File getProjectFile() {
		return projectFile;
	}

	public void newProjectFile() throws IOException {
		updateCurrentBox();
		boolean emptyProject = isProjectEmpty();
		if (!emptyProject) {
			int result = JOptionPane.showConfirmDialog(this,
					"Do you want to save the current project before creating a new project?",
					"Save before creating new project?", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
			if (result == JOptionPane.CANCEL_OPTION)
				return;
			else if (result == JOptionPane.YES_OPTION)
				saveProjectFile(null);
		}
		projectFile = null;
		boxes.clear();
		boxes.add(new Textbox());
		currentBox = 0;
		updateBoxComponents();
		updateBoxList();
	}

	public void saveProjectFile(File dest) throws IOException {
		updateCurrentBox();
		int hasCustomFace = -1;
		for (int i = 0; i < boxes.size(); i++) {
			if (Resources.getFace(boxes.get(i).face).isCustom()) {
				hasCustomFace = i;
				break;
			}
		}
		if (hasCustomFace != -1) {
			int result = JOptionPane.showConfirmDialog(this, "Textbox " + (hasCustomFace + 1)
					+ " has a custom face!\nThis will prevent a user without the custom from opening the project file.\nSave anyway?",
					"Textbox has custom face", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
			if (result == JOptionPane.NO_OPTION)
				return;
		}
		if (dest == null) {
			if (projectFile == null) {
				File sel = Main.openFileDialog(true, this, "Save project file", TBPROJ_FILTER);
				if (sel == null)
					return;
				projectFile = sel;
			}
			dest = projectFile;
		}
		if (dest.exists() && !dest.equals(projectFile)) {
			int result = JOptionPane.showConfirmDialog(this, "File \"" + dest + "\" already exists.\nOverwrite it?",
					"Destination file exists", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
			if (result == JOptionPane.NO_OPTION)
				return;
			dest.delete();
			dest.createNewFile();
		}
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		try (FileWriter fw = new FileWriter(dest); BufferedWriter writer = new BufferedWriter(fw)) {
			gson.toJson(boxes, writer);
		} catch (IOException e) {
			throw e;
		}
		projectFile = dest;
	}

	public void loadProjectFile(File src) throws IOException {
		boolean emptyProject = isProjectEmpty();
		if (!emptyProject) {
			int result = JOptionPane.showConfirmDialog(this,
					"Do you want to save the current project before loading another project?",
					"Save before loading other project?", JOptionPane.YES_NO_CANCEL_OPTION,
					JOptionPane.WARNING_MESSAGE);
			if (result == JOptionPane.CANCEL_OPTION)
				return;
			else if (result == JOptionPane.YES_OPTION)
				saveProjectFile(null);
		}
		if (src == null) {
			src = Main.openFileDialog(false, this, "Load project file", TBPROJ_FILTER);
			if (src == null)
				return;
		}
		Gson gson = new GsonBuilder().create();
		try (FileReader fr = new FileReader(src); BufferedReader reader = new BufferedReader(fr)) {
			Type type = new TypeToken<List<Textbox>>() {
			}.getType();
			List<Textbox> temp = gson.fromJson(reader, type);
			for (int i = 0; i < temp.size(); i++) {
				Textbox box = temp.get(i);
				if (Resources.getFace(box.face) == null) {
					JOptionPane.showMessageDialog(this,
							"Textbox " + (i + 1) + " specifies a facepic that isn't currently loaded: \"" + box.face
									+ "\"\nPlease make sure there is a loaded facepic with that name, then try again.",
							"Missing facepic", JOptionPane.ERROR_MESSAGE);
					return;
				}
			}
			boxes.clear();
			boxes.addAll(temp);
			boxSelect.setSelectedIndex(-1);
			updateBoxList();
		} catch (IOException e) {
			throw e;
		}
		projectFile = src;
		updateBoxComponents();
		updateBoxList();
	}

	public List<Textbox> getBoxes() {
		return new LinkedList<>(boxes);
	}

	public int getCurrentBox() {
		return currentBox;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		String a = e.getActionCommand();
		JFileChooser fc;
		int ret;
		Textbox box, copyBox;
		JDialog previewFrame;
		int extraHeight, maxHeight;
		Dimension size;
		switch (a) {
		case A_FACE_FOLDER:
			if (!Desktop.isDesktopSupported())
				return;
			try {
				Desktop.getDesktop().browse(new File("res/faces").toURI());
			} catch (IOException e2) {
				Main.LOGGER.error("Error while browsing to face folder!", e2);
			}
			break;
		case A_CUSTOM_FACE:
			fc = new JFileChooser();
			fc.setMultiSelectionEnabled(true);
			fc.setAcceptAllFileFilterUsed(false);
			fc.setDialogTitle("Open face image(s)");
			fc.setFileFilter(new FileNameExtensionFilter("PNG files", "png"));
			fc.setCurrentDirectory(new File(System.getProperty("user.dir")));
			ret = fc.showOpenDialog(this);
			if (ret == JFileChooser.APPROVE_OPTION) {
				File[] sels = fc.getSelectedFiles();
				for (File sel : sels) {
					String faceName = sel.getName();
					faceName = faceName.substring(0, faceName.lastIndexOf('.'));
					try {
						BufferedImage image = ImageIO.read(sel);
						if (image.getWidth() != 96 || image.getHeight() != 96) {
							JOptionPane.showMessageDialog(this, "Face must be 96 by 96!", "Bad face dimensions!",
									JOptionPane.ERROR_MESSAGE);
							return;
						}
						Resources.addFace(faceName, sel, image);
					} catch (IOException e1) {
						Main.LOGGER.error("Error while loading facepic!", e1);
						JOptionPane.showMessageDialog(this, "An exception occured while loading the facepic:\n" + e1,
								"Couldn't load facepic!", JOptionPane.ERROR_MESSAGE);
					}
				}
			}
			String[] faces = Resources.getFaces();
			faceSelect.setModel(new DefaultComboBoxModel<>(faces));
			faceSelect.setSelectedIndex(faces.length - 1);
			break;
		case A_ADD_BOX:
			updateCurrentBox();
			copyBox = boxes.get(boxes.size() - 1);
			box = new Textbox();
			box.face = copyBox.face;
			boxes.add(box);
			currentBox = boxes.size() - 1;
			updateBoxComponents();
			updateBoxList();
			break;
		case A_INSERT_BOX_BEFORE:
			updateCurrentBox();
			copyBox = boxes.get(currentBox);
			box = new Textbox();
			box.face = copyBox.face;
			boxes.add(currentBox, box);
			updateBoxComponents();
			updateBoxList();
			break;
		case A_INSERT_BOX_AFTER:
			updateCurrentBox();
			copyBox = boxes.get(currentBox);
			box = new Textbox();
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
				int result = JOptionPane.showConfirmDialog(this,
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
			previewFrame = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Modifier Help", true);
			previewFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
			previewFrame.setResizable(false);
			previewFrame.add(new ModifierHelpPanel());
			previewFrame.pack();
			previewFrame.setLocationRelativeTo(null);
			previewFrame.setVisible(true);
			break;
		case A_MAKE_BOXES:
			updateCurrentBox();
			BufferedImage boxesImage = new BufferedImage(608, 128 * boxes.size() + 2 * (boxes.size() - 1),
					BufferedImage.TYPE_4BYTE_ABGR);
			Graphics big = boxesImage.getGraphics();
			if (checkTextboxes())
				for (int i = 0; i < boxes.size(); i++) {
					Textbox b = boxes.get(i);
					drawTextbox(big, b.face, b.text, 0, 130 * i, i < boxes.size() - 1);
				}
			else
				return;
			previewFrame = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Textbox(es) preview", true);
			previewFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
			extraHeight = 82;
			size = new Dimension(652, boxesImage.getHeight() + extraHeight);
			maxHeight = 128 * 5 + 2 * 4 + extraHeight;
			if (size.height > maxHeight)
				size.height = maxHeight + 16;
			previewFrame.setMinimumSize(size);
			previewFrame.setPreferredSize(size);
			previewFrame.setMaximumSize(size);
			previewFrame.setResizable(false);
			previewFrame.add(new PreviewPanel(boxesImage));
			previewFrame.pack();
			previewFrame.setLocationRelativeTo(null);
			previewFrame.setVisible(true);
			break;
		case A_MAKE_BOXES_ANIM:
			updateCurrentBox();
			if (!checkTextboxes())
				return;
			List<BufferedImage> boxFrames = makeTextboxAnimation(boxes);
			byte[] data = null;
			Image image = null;
			try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
					ImageOutputStream ios = ImageIO.createImageOutputStream(baos)) {
				ImageWriter writer = ImageIO.getImageWritersByFormatName("gif").next();
				IIOMetadata meta = writer.getDefaultImageMetadata(
						ImageTypeSpecifier.createFromBufferedImageType(boxFrames.get(0).getType()),
						writer.getDefaultWriteParam());
				String metaFormatName = meta.getNativeMetadataFormatName();
				IIOMetadataNode root = (IIOMetadataNode) meta.getAsTree(metaFormatName);
				IIOMetadataNode graphicsControl = getNode(root, "GraphicControlExtension");
				graphicsControl.setAttribute("disposalMethod", "none");
				graphicsControl.setAttribute("userInputFlag", "FALSE");
				graphicsControl.setAttribute("transparentColorFlag", "FALSE");
				graphicsControl.setAttribute("delayTime", "5");
				graphicsControl.setAttribute("transparentColorIndex", "0");
				IIOMetadataNode comments = getNode(root, "CommentExtensions");
				comments.setAttribute("CommentExtension", "Animated OneShot Textbox");
				IIOMetadataNode application = getNode(root, "ApplicationExtensions");
				IIOMetadataNode child = new IIOMetadataNode("ApplicationExtension");
				child.setAttribute("applicationID", "NETSCAPE");
				child.setAttribute("authenticationCode", "2.0");
				child.setUserObject(new byte[] { 0x1, (byte) (0 & 0xFF), (byte) ((0 >> 8) & 0xFF) });
				application.appendChild(child);
				meta.setFromTree(metaFormatName, root);
				writer.setOutput(ios);
				writer.prepareWriteSequence(null);
				for (int i = 0; i < boxFrames.size(); i++) {
					BufferedImage frame = boxFrames.get(i);
					writer.writeToSequence(new IIOImage(frame, null, meta), writer.getDefaultWriteParam());
				}
				writer.endWriteSequence();
				ios.flush();
				data = baos.toByteArray();
				image = Toolkit.getDefaultToolkit().createImage(data);
			} catch (IOException e1) {
				Main.LOGGER.error("Error while generating animation!", e1);
				JOptionPane.showMessageDialog(this, "An exception occured while generating the animation:\n" + e1,
						"Couldn't generate animation!", JOptionPane.ERROR_MESSAGE);
			}
			ImageIcon preview = new ImageIcon(image);
			previewFrame = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Textbox(es) preview", true);
			previewFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
			extraHeight = 82;
			size = new Dimension(652, boxFrames.get(0).getHeight() + extraHeight);
			maxHeight = 128 * 5 + 2 * 4 + extraHeight;
			if (size.height > maxHeight)
				size.height = maxHeight + 16;
			previewFrame.setMinimumSize(size);
			previewFrame.setPreferredSize(size);
			previewFrame.setMaximumSize(size);
			previewFrame.setResizable(false);
			final byte[] fdata = data;
			previewFrame.add(new PreviewPanel(preview, (ActionEvent ae) -> {
				String cmd = ae.getActionCommand();
				switch (cmd) {
				case PreviewPanel.A_SAVE_BOXES:
					File sel = Main.openFileDialog(true, this, "Save textbox(es) animation",
							new FileNameExtensionFilter("GIF files", "gif"));
					if (sel == null)
						return;
					if (sel.exists()) {
						int confirm = JOptionPane.showConfirmDialog(this,
								"File \"" + sel.getName() + "\" already exists.\nOverwrite it?",
								"Overwrite existing file?", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
						if (confirm != JOptionPane.YES_OPTION)
							return;
						sel.delete();
					}
					try (ImageOutputStream out = ImageIO.createImageOutputStream(sel)) {
						out.write(fdata);
					} catch (IOException e1) {
						Main.LOGGER.error("Error while saving animation!", e1);
						JOptionPane.showMessageDialog(this, "An exception occured while saving the animation:\n" + e1,
								"Couldn't save animation!", JOptionPane.ERROR_MESSAGE);
					}
					break;
				case PreviewPanel.A_COPY_BOXES:
					File tmp = new File(System.getProperty("java.io.tmpdir"), "textbox.gif");
					try (ImageOutputStream out = ImageIO.createImageOutputStream(tmp)) {
						out.write(fdata);
					} catch (IOException e1) {
						Main.LOGGER.error("Error while copying animation to clipboard!", e1);
						JOptionPane.showMessageDialog(this,
								"An exception occured while copying the animation to the clipboard:\n" + e1,
								"Couldn't copy animation to clipboard!", JOptionPane.ERROR_MESSAGE);
					}
					Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new TransferableFileList(tmp),
							new ClipboardOwner() {
								@Override
								public void lostOwnership(Clipboard clipboard, Transferable contents) {
									tmp.delete();
								}
							});
					break;
				default:
					Main.LOGGER.debug("Undefined action: " + cmd);
					break;
				}
			}));
			previewFrame.pack();
			previewFrame.setLocationRelativeTo(null);
			previewFrame.setVisible(true);
			break;
		default:
			Main.LOGGER.debug("Undefined action: " + a);
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

	private IIOMetadataNode getNode(IIOMetadataNode rootNode, String nodeName) {
		int nNodes = rootNode.getLength();
		for (int i = 0; i < nNodes; i++) {
			if (rootNode.item(i).getNodeName().compareToIgnoreCase(nodeName) == 0) {
				return ((IIOMetadataNode) rootNode.item(i));
			}
		}
		IIOMetadataNode node = new IIOMetadataNode(nodeName);
		rootNode.appendChild(node);
		return (node);
	}

	private boolean checkTextboxes() {
		final String[] columnNames = new String[] { "Box #", "Line#", "Description" };
		Vector<String[]> errors = new Vector<>();
		for (int i = 0; i < boxes.size(); i++) {
			Textbox b = boxes.get(i);
			TextboxParseData tpd = parseTextbox(b.text);
			String text = tpd.strippedText;
			String[] lines = text.split("\n");
			int linesNum = lines.length;
			if (linesNum > 4) {
				errors.add(new String[] { Integer.toString(i + 1), "N/A",
						"Too many lines: has " + linesNum + " lines, but can only fit 4 lines" });
				break;
			}
			int maxLen = 47;
			String errorBit = "with face";
			if (Resources.FACE_BLANK.equals(b.face)) {
				maxLen += 10;
				errorBit = "without face";
			}
			for (int l = 0; l < lines.length; l++) {
				int lineLen = lines[l].length();
				if (lineLen > maxLen) {
					errors.add(new String[] { Integer.toString(i + 1), Integer.toString(l + 1),
							"Line too long: has " + lineLen + " characters, but textboxes " + errorBit
									+ " can only fit " + maxLen + " characters" });
					break;
				}
			}
			if (errors.isEmpty())
				return true;
		}
		JTable errorTable = new JTable(new AbstractTableModel() {

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
			public String getColumnName(int column) {
				return columnNames[column];
			}

			@Override
			public Object getValueAt(int rowIndex, int columnIndex) {
				String[] row = errors.get(rowIndex);
				return row[columnIndex];
			}

			@Override
			public boolean isCellEditable(int rowIndex, int columnIndex) {
				return false;
			}

		});
		JTableHeader header = errorTable.getTableHeader();
		header.setResizingAllowed(false);
		header.setReorderingAllowed(false);
		TableColumnAdjuster tca = new TableColumnAdjuster(errorTable);
		tca.adjustColumns();
		JDialog errFrame = new JDialog((Frame) SwingUtilities.getWindowAncestor(this),
				"Errors while generating textbox(es)", true);
		errFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		final Dimension size = new Dimension(652, 280);
		errFrame.setMinimumSize(size);
		errFrame.setPreferredSize(size);
		errFrame.setMaximumSize(size);
		errFrame.setResizable(false);
		JPanel errPanel = new JPanel();
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
	public void valueChanged(ListSelectionEvent e) {
		if (e.getSource().equals(boxSelect)) {
			updateCurrentBox();
			int sel = boxSelect.getSelectedIndex();
			if (sel < 0)
				return;
			currentBox = sel;
			updateBoxComponents();
		}
	}

	@Override
	public void itemStateChanged(ItemEvent e) {
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
		public Component getListCellRendererComponent(JList<? extends Textbox> list, Textbox value, int index,
				boolean isSelected, boolean cellHasFocus) {
			if (isSelected)
				setBackground(COLOR_TEXTBOX_B);
			else
				setBackground(COLOR_TEXTBOX);
			String text = "<html><p style=\"color:white;\">Textbox " + (index + 1) + "</p><p style=\"color:gray;\"><i>"
					+ value.toString() + "</i></p></html>";
			setText(text);
			ImageIcon icon = Resources.getFace(value.face).getIcon();
			setIcon(icon);
			// getGraphics() returns null here so we need to make our own Graphics object
			Graphics g = IMAGE.getGraphics();
			g.setFont(getFont());
			Rectangle2D bounds = g.getFontMetrics().getStringBounds(text, g);
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
		public Component getListCellRendererComponent(JList<? extends String> list, String value, int index,
				boolean isSelected, boolean cellHasFocus) {
			if (isSelected)
				setBackground(COLOR_TEXTBOX_B);
			else
				setBackground(COLOR_TEXTBOX);
			Facepic face = Resources.getFace(value);
			ImageIcon faceIcon = face.getIcon();
			if (faceIcon == null)
				setIcon(Resources.getFace(Resources.FACE_BLANK).getIcon());
			else
				setIcon(faceIcon);
			String text = "<html><p style=\"color:white;\">" + face.getName();
			if (face.isCustom())
				text += "<b>*</b>";
			File faceFile = face.getFile();
			if (faceFile != null)
				text += "</p><p style=\"color:gray;\"><i>" + faceFile.getPath() + "</i>";
			text += "</p></html>";
			setText(text);
			return this;
		}

	}

	static class TextboxEditorPane extends JEditorPane {

		static class TextboxEditorKit extends DefaultEditorKit {

			private static final long serialVersionUID = 1L;

			@Override
			public ViewFactory getViewFactory() {
				return new TextboxViewFactory();
			}

			static class TextboxViewFactory implements ViewFactory {

				static class TextboxParagraphView extends ParagraphView {

					public TextboxParagraphView(Element elem) {
						super(elem);
					}

					@Override
					protected void layout(int width, int height) {
						super.layout(Short.MAX_VALUE, height);
					}

					@Override
					public float getMinimumSpan(int axis) {
						return super.getPreferredSpan(axis);
					}

				}

				@Override
				public View create(Element elem) {
					String kind = elem.getName();
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

			}

		}

		private static final long serialVersionUID = 1L;

		private MakerPanel panel;
		private SimpleAttributeSet styleNormal, styleMod, styleOver;
		private Map<Color, SimpleAttributeSet> colorStyleCache = new HashMap<>();

		public TextboxEditorPane(MakerPanel panel, String text) {
			super();
			this.panel = panel;
			setEditorKit(new TextboxEditorKit());
			StyledDocument doc = new DefaultStyledDocument() {
				private static final long serialVersionUID = 1L;

				@Override
				public void insertString(int offs, String str, AttributeSet a) throws BadLocationException {
					str = str.replaceAll("\t", "    ");
					super.insertString(offs, str, a);
				}
			};
			setDocument(doc);
			Font font = Resources.getTextboxFont();
			styleNormal = new SimpleAttributeSet();
			StyleConstants.setFontFamily(styleNormal, font.getFamily());
			StyleConstants.setFontSize(styleNormal, font.getSize());
			StyleConstants.setForeground(styleNormal, Color.WHITE);
			StyleConstants.setBold(styleNormal, font.isBold());
			colorStyleCache.put(Color.WHITE, styleNormal);
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
				public void keyReleased(KeyEvent e) {
					panel.updateCurrentBox();
					highlight();
				}
			});
		}

		@Override
		public void setText(String t) {
			super.setText(t);
			highlight();
		}

		private void highlight() {
			Document doc = getDocument();
			if (doc instanceof StyledDocument) {
				StyledDocument stylDoc = (StyledDocument) doc;
				AttributeSet curStyle = styleNormal;
				stylDoc.setParagraphAttributes(0, doc.getLength(), curStyle, true);
				stylDoc.setCharacterAttributes(0, doc.getLength(), curStyle, true);
				int maxLen = 57;
				if (!Resources.FACE_BLANK.equals(panel.faceSelect.getSelectedItem()))
					maxLen -= 10;
				TextboxParseData tpd = parseTextbox(getText());
				if (tpd.strippedText.isEmpty()) {
					if (tpd.mods.containsKey(0)) {
						List<TextboxModifier> list = tpd.mods.get(0);
						for (TextboxModifier mod : list) {
							stylDoc.setCharacterAttributes(mod.position, mod.length, styleMod, true);
						}
					}
				} else {
					String[] lines = tpd.strippedText.split("\n");
					int currentChar = 0, length = 0, ignoreOff = 0;
					for (int i = 0; i < lines.length; i++) {
						char[] chars = lines[i].toCharArray();
						for (int j = 0; j < chars.length; j++) {
							if (tpd.mods.containsKey(currentChar)) {
								List<TextboxModifier> list = tpd.mods.get(currentChar);
								for (TextboxModifier mod : list) {
									if (mod.type == TextboxModifier.ModType.FACE)
										if (mod.args.length == 0)
											maxLen = 57;
										else if (Resources.FACE_BLANK.equals(mod.args[0]))
											maxLen = 57;
										else
											maxLen = 47;
									else if (mod.type == TextboxModifier.ModType.COLOR) {
										Color col = getColorModValue(mod, StyleConstants.getForeground(styleNormal));
										SimpleAttributeSet colorStyle = colorStyleCache.get(col);
										if (colorStyle == null) {
											colorStyle = new SimpleAttributeSet();
											Font font = Resources.getTextboxFont();
											StyleConstants.setFontFamily(colorStyle, font.getFamily());
											StyleConstants.setFontSize(colorStyle, font.getSize());
											StyleConstants.setForeground(colorStyle, col);
											StyleConstants.setBold(colorStyle, font.isBold());
											colorStyleCache.put(col, colorStyle);
										}
										curStyle = colorStyle;
									}
									stylDoc.setCharacterAttributes(mod.position, mod.length, styleMod, true);
									ignoreOff += mod.length;
								}
							}
							stylDoc.setCharacterAttributes(currentChar + ignoreOff, 1, curStyle, true);
							length++;
							currentChar++;
						}
						int start = 0, end = 0;
						Element line = doc.getDefaultRootElement().getElement(i);
						start = line.getStartOffset();
						end = line.getEndOffset();
						if (i > 3) {
							stylDoc.setParagraphAttributes(start, end - start, styleOver, true);
							stylDoc.setCharacterAttributes(start, end - start, styleOver, true);
						} else if (length > maxLen) {
							final int pos = start + ignoreOff + maxLen;
							stylDoc.setCharacterAttributes(pos, end - pos, styleOver, true);
						}
					}
				}
			}
		}

	}

}
