package com.leo.ostbm;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.LinkedList;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.Highlighter;
import javax.swing.text.Highlighter.HighlightPainter;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.leo.ostbm.Resources.Facepic;
import com.leo.ostbm.Resources.Icon;

public class MakerPanel extends JPanel implements ActionListener, ListSelectionListener {

	private static final long serialVersionUID = 1L;

	public static final FileNameExtensionFilter TBPROJ_FILTER = new FileNameExtensionFilter("Project files", "tbproj");

	public static final Color COLOR_TEXTBOX = Color.decode("0x180C1E");
	public static final Color COLOR_TEXTBOX_B = COLOR_TEXTBOX.brighter().brighter();

	public static final String A_FACE_FOLDER = "faceFolder";
	public static final String A_CUSTOM_FACE = "customFace";
	public static final String A_REMOVE_BOX = "removeBox";
	public static final String A_ADD_BOX = "addBox";
	public static final String A_MAKE_BOXES = "makeBoxes";

	class Textbox {
		public String face;
		public String text;

		public Textbox(String face, String text) {
			this.face = face;
			this.text = text;
		}

		public Textbox(String text) {
			this(Resources.FACE_BLANK, text);
		}

		public Textbox() {
			this(Resources.FACE_BLANK, "");
		}

		@Override
		public String toString() {
			String t = text;
			t = t.trim().replace('\n', ' ');
			final int maxLen = 26;
			if (t.length() > maxLen)
				t = t.substring(0, maxLen) + "...";
			if (t.isEmpty())
				t = "(empty)";
			return t;
		}
	}

	private File projectFile;
	private int currentBox;
	private List<Textbox> boxes;

	private JList<Textbox> boxSelect;
	private DefaultListModel<Textbox> boxSelectModel;
	private JButton addBoxButton, removeBoxButton;

	private JComboBox<String> faceSelect;
	private JButton openFaceFolderButton, customFaceButton;
	private JTextArea textArea;
	private JButton makeTextboxButton;

	public MakerPanel() {
		currentBox = 0;
		boxes = new LinkedList<>();
		boxes.add(new Textbox());
		setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
		setLayout(new BorderLayout());
		JPanel boxSelectPanel = new JPanel();
		boxSelectPanel.setLayout(new BorderLayout());
		JPanel boxControlPanel = new JPanel();
		boxControlPanel.setLayout(new BoxLayout(boxControlPanel, BoxLayout.LINE_AXIS));
		addBoxButton = new JButton(Resources.getIcon(Icon.ADD_TEXTBOX));
		addBoxButton.addActionListener(this);
		addBoxButton.setActionCommand(A_ADD_BOX);
		addBoxButton.setToolTipText("Add a new textbox");
		addBoxButton.setMaximumSize(new Dimension(24, 24));
		boxControlPanel.add(addBoxButton);
		removeBoxButton = new JButton(Resources.getIcon(Icon.REMOVE_TEXTBOX));
		removeBoxButton.addActionListener(this);
		removeBoxButton.setActionCommand(A_REMOVE_BOX);
		removeBoxButton.setToolTipText("Remove the current textbox");
		removeBoxButton.setMaximumSize(new Dimension(24, 24));
		boxControlPanel.add(removeBoxButton);
		boxSelectPanel.add(boxControlPanel, BorderLayout.PAGE_START);
		boxSelect = new JList<>();
		boxSelect.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		boxSelect.addListSelectionListener(this);
		boxSelect.setCellRenderer(new TextboxListRenderer());
		boxSelectModel = new DefaultListModel<>();
		updateBoxList();
		JScrollPane scroll = new JScrollPane(boxSelect);
		boxSelectPanel.add(scroll, BorderLayout.CENTER);
		add(boxSelectPanel, BorderLayout.LINE_START);
		JPanel boxEditPanel = new JPanel();
		boxEditPanel.setLayout(new BorderLayout());
		boxEditPanel.setBorder(BorderFactory.createEmptyBorder(0, 4, 0, 0));
		JPanel faceSelectPanel = new JPanel();
		faceSelectPanel.setLayout(new BoxLayout(faceSelectPanel, BoxLayout.LINE_AXIS));
		faceSelectPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 4, 0));
		faceSelect = new JComboBox<String>();
		faceSelect.setModel(new DefaultComboBoxModel<>(Resources.getFaces()));
		faceSelect.setSelectedItem(boxes.get(currentBox).face);
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
		textArea = new TextboxTextArea(boxes.get(currentBox).text);
		textArea.setFont(Resources.getFontBox());
		textArea.setBackground(COLOR_TEXTBOX);
		textArea.setForeground(Color.WHITE);
		textArea.setCaretColor(Color.WHITE);
		boxEditPanel.add(new JScrollPane(textArea), BorderLayout.CENTER);
		JPanel bottomPanel = new JPanel();
		bottomPanel.setLayout(new BorderLayout());
		makeTextboxButton = new JButton("Make a Textbox!");
		makeTextboxButton.addActionListener(this);
		makeTextboxButton.setActionCommand(A_MAKE_BOXES);
		bottomPanel.add(makeTextboxButton, BorderLayout.PAGE_END);
		boxEditPanel.add(bottomPanel, BorderLayout.PAGE_END);
		add(boxEditPanel, BorderLayout.CENTER);
	}

	private void updateCurrentBox() {
		Textbox box = boxes.get(currentBox);
		box.face = faceSelect.getItemAt(faceSelect.getSelectedIndex());
		box.text = textArea.getText();
		boxSelect.repaint();
	}

	private void updateBoxComponents() {
		Textbox box = boxes.get(currentBox);
		faceSelect.setSelectedItem(box.face);
		textArea.setText(box.text);
	}

	private void updateBoxList() {
		boxSelectModel = new DefaultListModel<>();
		for (Textbox b : boxes)
			boxSelectModel.addElement(b);
		boxSelect.setModel(boxSelectModel);
		boxSelect.setSelectedIndex(currentBox);
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

	@Override
	public void actionPerformed(ActionEvent e) {
		String a = e.getActionCommand();
		JFileChooser fc;
		int ret;
		Textbox box;
		switch (a) {
		case A_FACE_FOLDER:
			if (!Desktop.isDesktopSupported())
				return;
			try {
				Desktop.getDesktop().browse(new File("res/faces").toURI());
			} catch (IOException e2) {
				e2.printStackTrace();
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
						e1.printStackTrace();
						JOptionPane.showMessageDialog(this, "An exception occured while loading the face:\n" + e1,
								"Couldn't load face!", JOptionPane.ERROR_MESSAGE);
					}
				}
			}
			String[] faces = Resources.getFaces();
			faceSelect.setModel(new DefaultComboBoxModel<>(faces));
			faceSelect.setSelectedIndex(faces.length - 1);
			break;
		case A_REMOVE_BOX:
			updateCurrentBox();
			if (!boxes.get(currentBox).text.trim().isEmpty()) {
				int result = JOptionPane.showConfirmDialog(this,
						"Are you sure you want to delete textbox " + (currentBox + 1) + "?", "Confirm deleting textbox",
						JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
				if (result != JOptionPane.YES_OPTION)
					break;
			}
			if (boxes.size() == 1) {
				box = boxes.get(currentBox);
				box.face = Resources.FACE_BLANK;
				box.text = "";
			} else
				boxes.remove(currentBox);
			if (currentBox == boxes.size())
				currentBox--;
			if (currentBox < 0)
				currentBox = 0;
			updateBoxComponents();
			updateBoxList();
			break;
		case A_ADD_BOX:
			updateCurrentBox();
			Textbox lastBox = boxes.get(boxes.size() - 1);
			box = new Textbox();
			box.face = lastBox.face;
			boxes.add(box);
			currentBox = boxes.size() - 1;
			updateBoxComponents();
			updateBoxList();
			break;
		case A_MAKE_BOXES:
			updateCurrentBox();
			BufferedImage boxesImage = new BufferedImage(608, 128 * boxes.size() + 2 * (boxes.size() - 1),
					BufferedImage.TYPE_4BYTE_ABGR);
			Graphics big = boxesImage.getGraphics();
			for (int i = 0; i < boxes.size(); i++) {
				Textbox b = boxes.get(i);
				String text = b.text.trim();
				if (text.isEmpty()) {
					JOptionPane.showMessageDialog(this,
							"Textbox " + (i + 1) + " is blank!\nPlease write something there.", "Text cannot be blank!",
							JOptionPane.ERROR_MESSAGE);
					return;
				}
				String[] lines = text.split("\n");
				int linesNum = lines.length;
				if (linesNum > 4) {
					JOptionPane.showMessageDialog(this,
							"Textbox " + (i + 1) + " has too many lines!\nIt has " + linesNum
									+ " lines, but a single textbox can only fit 4 lines.",
							"Too many lines!", JOptionPane.ERROR_MESSAGE);
					return;
				}
				int maxLen = 47;
				String errorBit = "with a face";
				if (Resources.FACE_BLANK.equals(b.face)) {
					maxLen += 10;
					errorBit = "without a face";
				}
				for (int l = 0; l < lines.length; l++) {
					int lineLen = lines[l].length();
					if (lineLen > maxLen) {
						JOptionPane.showMessageDialog(this,
								"Line " + (l + 1) + " in textbox " + (i + 1) + " is too long!\nIt has " + lineLen
										+ " characters, but textboxes " + errorBit + " can only fit " + maxLen
										+ " characters per line.",
								"Line too long!", JOptionPane.ERROR_MESSAGE);
						return;
					}
				}
				drawTextbox(big, b.face, text, 0, 130 * i, i < boxes.size() - 1);
			}
			JFrame previewFrame = new JFrame();
			previewFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
			final int extraHeight = 82;
			final Dimension size = new Dimension(652, boxesImage.getHeight() + extraHeight);
			final int maxHeight = 128 * 5 + 2 * 4 + extraHeight;
			if (size.height > maxHeight)
				size.height = maxHeight + 16;
			previewFrame.setMinimumSize(size);
			previewFrame.setPreferredSize(size);
			previewFrame.setMaximumSize(size);
			previewFrame.setResizable(false);
			previewFrame.add(new PreviewPanel(boxesImage));
			previewFrame.pack();
			previewFrame.setLocationRelativeTo(null);
			previewFrame.setTitle("Textbox(es) preview");
			previewFrame.setVisible(true);
			break;
		default:
			System.out.println("Undefined action: " + a);
			break;
		}
	}

	@Override
	public void valueChanged(ListSelectionEvent e) {
		if (e.getSource().equals(boxSelect)) {
			if (faceSelect != null && textArea != null)
				updateCurrentBox();
			int sel = boxSelect.getSelectedIndex();
			if (sel < 0)
				return;
			currentBox = sel;
			if (faceSelect != null && textArea != null)
				updateBoxComponents();
		}
	}

	public static BufferedImage drawTextbox(String face, String text) {
		BufferedImage ret = new BufferedImage(608, 128, BufferedImage.TYPE_INT_ARGB);
		Graphics g = ret.getGraphics();
		drawTextbox(g, face, text, 0, 0, false);
		return ret;
	}

	public static void drawTextbox(Graphics g, String face, String text, int x, int y, boolean drawArrow) {
		g.drawImage(Resources.getBox(), x, y, null);
		BufferedImage faceImage = Resources.getFace(face).getImage();
		if (faceImage != null)
			g.drawImage(faceImage, x + 496, y + 16, null);
		if (drawArrow)
			g.drawImage(Resources.getArrow(), x + 299, y + 118, null);
		drawTextboxString(g, text, x + 20, y + 10);
	}

	private static void drawTextboxString(Graphics g, String str, int x, int y) {
		g.setFont(Resources.getFontBox());
		g.setColor(Color.WHITE);
		final int lineSpace = g.getFontMetrics().getHeight() + 1;
		for (String line : str.split("\n")) {
			y += lineSpace;
			g.drawString(line, x, y);
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
			if (isSelected) {
				setBackground(list.getSelectionBackground());
				setForeground(list.getSelectionForeground());
			} else {
				setBackground(list.getBackground());
				setForeground(list.getForeground());
			}
			String text = "<html><b>Textbox " + (index + 1) + "</b><br>" + value.toString() + "</html>";
			setText(text);
			// getGraphics() returns null here so we need to make our own Graphics object
			GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
			Graphics2D g = ge.createGraphics(IMAGE);
			g.setFont(getFont());
			Rectangle2D bounds = g.getFontMetrics().getStringBounds(text, g);
			setPreferredSize(new Dimension(200, (int) bounds.getHeight() + g.getFontMetrics().getHeight()));
			return this;
		}

		@Override
		protected void paintComponent(Graphics g) {
			super.paintComponent(g);
		}

	}

	class FacesComboBoxRenderer extends JLabel implements ListCellRenderer<String> {

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
			String text = "<html><font color=white>" + face.getName();
			if (face.isCustom())
				text += "<b>*</b>";
			File faceFile = face.getFile();
			if (faceFile != null)
				text += "</font><br><font color=gray><i>" + faceFile.getPath() + "</i>";
			text += "</font></html>";
			setText(text);
			return this;
		}

	}

	class TextboxTextArea extends JTextArea {

		private static final long serialVersionUID = 1L;

		public TextboxTextArea(String text) {
			super(text);
			addKeyListener(new KeyAdapter() {
				@Override
				public void keyReleased(KeyEvent e) {
					int maxLen = 57;
					if (faceSelect.getSelectedItem() != Resources.FACE_BLANK)
						maxLen -= 10;
					Highlighter hl = getHighlighter();
					hl.removeAllHighlights();
					HighlightPainter p = new DefaultHighlighter.DefaultHighlightPainter(Color.RED);
					String[] lines = getText().split("\n");
					for (int i = 0; i < lines.length; i++) {
						int start = 0, end = 0;
						try {
							start = getLineStartOffset(i);
							end = getLineEndOffset(i);
						} catch (BadLocationException e2) {
							e2.printStackTrace();
						}
						try {
							if (i > 3) {
								hl.addHighlight(start, end, p);
							} else if (lines[i].length() > maxLen) {
								hl.addHighlight(start + maxLen, end, p);
							}
						} catch (BadLocationException e1) {
							e1.printStackTrace();
						}
					}
					repaint();
				}
			});
		}

	}

}
