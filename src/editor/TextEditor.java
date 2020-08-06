package editor;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.undo.UndoManager;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class TextEditor extends JFrame {

    private static final long serialVersionUID = -5374238263145714137L;

    /* Action Listeners */
    private final ActionListener loadAction;
    private final ActionListener saveAction;
    private final ActionListener searchAction;
    private final ActionListener nextAction;
    private final ActionListener previousAction;

    /* Input Fields */
    private final JTextField searchField;
    private final JCheckBox regexCheckbox;
    private final JTextArea textArea;

    /* Menus */
    private final JMenuItem menuOpen;
    private final JMenuItem menuSave;
    private final JMenuItem menuExit;
    private final JMenuItem menuStartSearch;
    private final JMenuItem menuPreviousMatch;
    private final JMenuItem menuNextMatch;
    private final JMenuItem menuUseRegex;
    private final JMenuItem menuUndo;
    private final JMenuItem menuRedo;

    /* Icons */
    private final ImageIcon saveIcon = new ImageIcon("resource/save.png");
    private final ImageIcon openIcon = new ImageIcon("resource/open.png");
    private final ImageIcon searchIcon = new ImageIcon("resource/search.png");
    private final ImageIcon nextIcon = new ImageIcon("resource/next.png");
    private final ImageIcon previousIcon = new ImageIcon("resource/previous.png");

    /* Search Fields */
    private final SearchResultList searchResultList = new SearchResultList();
    private SearchWorker searchWorker;

    /* Other Fields */
    private final UndoManager undoManager = new UndoManager();
    private String currentDirectory = System.getProperty("user.home");
    private String currentFileName = "Untitled";

    public TextEditor() {

        // set to MacOs look and feel
        if (System.getProperty("os.name").toLowerCase().startsWith("mac os x")) {
            System.setProperty("apple.laf.useScreenMenuBar", "true");
            System.setProperty("com.apple.mrj.application.apple.menu.about.name", "Text Editor");
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (ClassNotFoundException | InstantiationException | IllegalAccessException
                    | UnsupportedLookAndFeelException e1) {
                e1.printStackTrace();
            }
        }

        SpringLayout mainLayout = new SpringLayout();

        /* Define TextEditor Fields */
        searchField = new JTextField() {
            private static final long serialVersionUID = 1389996229208138346L;
            {
                setName("SearchField");
            }
        };

        regexCheckbox = new JCheckBox() {
            private static final long serialVersionUID = 2599984024948014167L;
            {
                setName("UseRegExCheckbox");
                setSelected(false);
                setText("Use regex");
            }
        };

        textArea = new JTextArea() {
            private static final long serialVersionUID = -1598133667203234710L;
            {
                setBounds(10, 10, 280, 270);
                setName("TextArea");
                getDocument().addDocumentListener(new DocumentListener() {

                    @Override
                    public void insertUpdate(DocumentEvent e) {
                        if (searchWorker != null) {
                            searchWorker.restart();
                        }
                    }

                    @Override
                    public void removeUpdate(DocumentEvent e) {
                        if (searchWorker != null) {
                            searchWorker.restart();
                        }
                    }

                    @Override
                    public void changedUpdate(DocumentEvent e) {
                    }
                });
                getDocument().addUndoableEditListener(undoManager);
            }
        };

        final JScrollPane textPane = new JScrollPane(textArea) {
            private static final long serialVersionUID = 6417841791294042219L;
            {
                setName("ScrollPane");
            }
        };

        loadAction = e -> {
            FileDialog fileDialog = new FileDialog(this, "Open File", FileDialog.LOAD);
            fileDialog.setMultipleMode(false);
            fileDialog.setDirectory(currentDirectory);
            fileDialog.setVisible(true);
            currentDirectory = fileDialog.getDirectory();
            if (fileDialog.getFile() != null) {
                currentFileName = fileDialog.getFile();
                String fileName = currentDirectory + currentFileName;
                try {
                    textArea.setText(Files.readString(Path.of(fileName)));
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
            }
        };

        saveAction = e -> {
            FileDialog fileDialog = new FileDialog(this, "Save As ...", FileDialog.SAVE);
            fileDialog.setDirectory(currentDirectory);
            fileDialog.setFile(currentFileName);
            fileDialog.setVisible(true);
            currentDirectory = fileDialog.getDirectory();
            if (fileDialog.getFile() != null) {
                currentFileName = fileDialog.getFile();
                String fileName = currentDirectory + currentFileName;
                try {
                    Files.writeString(Path.of(fileName), textArea.getText());
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
            }
        };

        searchAction = e -> {
            searchWorker = new SearchWorker(textArea, searchResultList, searchField.getText(),
                    regexCheckbox.isSelected());
            searchWorker.execute();
        };

        nextAction = e -> {
            SearchResult result = searchResultList.next();
            if (result == null) {
                return;
            }
            textArea.setCaretPosition(result.end);
            textArea.select(result.start, result.end);
            textArea.grabFocus();
        };

        previousAction = e -> {
            SearchResult result = searchResultList.previous();
            if (result == null) {
                return;
            }
            textArea.setCaretPosition(result.end);
            textArea.select(result.start, result.end);
            textArea.grabFocus();
        };

        /* Menu Bar */
        menuOpen = new JMenuItem("Open") {
            private static final long serialVersionUID = -8173925873294943575L;
            {
                setName("MenuOpen");
                setAccelerator(
                        KeyStroke.getKeyStroke(KeyEvent.VK_O, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
                addActionListener(loadAction);
            }
        };

        menuSave = new JMenuItem("Save") {
            private static final long serialVersionUID = 2134267584778050158L;
            {
                setName("MenuSave");
                setAccelerator(
                        KeyStroke.getKeyStroke(KeyEvent.VK_S, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
                addActionListener(saveAction);
            }
        };

        menuExit = new JMenuItem("Exit") {
            private static final long serialVersionUID = 3768061764679612179L;
            {
                setName("MenuExit");
                setAccelerator(
                        KeyStroke.getKeyStroke(KeyEvent.VK_Q, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
                addActionListener(e -> System.exit(0));
            }
        };

        menuStartSearch = new JMenuItem("Search") {
            private static final long serialVersionUID = -7470856224542200616L;
            {
                setName("MenuStartSearch");
                setAccelerator(
                        KeyStroke.getKeyStroke(KeyEvent.VK_F, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
                addActionListener(searchAction);
            }
        };

        menuPreviousMatch = new JMenuItem("Previous Match") {
            private static final long serialVersionUID = 1931088497377251270L;
            {
                setName("MenuPreviousMatch");
                setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_G,
                        InputEvent.SHIFT_DOWN_MASK | Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
                addActionListener(previousAction);
            }
        };

        menuNextMatch = new JMenuItem("Next Match") {
            private static final long serialVersionUID = -1834510552251944637L;
            {
                setName("MenuNextMatch");
                setAccelerator(
                        KeyStroke.getKeyStroke(KeyEvent.VK_G, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
                addActionListener(nextAction);
            }
        };

        menuUseRegex = new JCheckBoxMenuItem("Use Regex") {
            private static final long serialVersionUID = -3333639627980140952L;
            {
                setName("MenuUseRegExp");
                addActionListener(e -> regexCheckbox.setSelected(!regexCheckbox.isSelected()));
            }
        };

        regexCheckbox.addActionListener(e -> menuUseRegex.setSelected(!menuUseRegex.isSelected()));

        menuUndo = new JMenuItem("Undo") {
            {
                setName("MenuUndo");
                addActionListener(e -> {
                    if (undoManager.canUndo()) {
                        undoManager.undo();
                    }
                });
                setAccelerator(
                        KeyStroke.getKeyStroke(KeyEvent.VK_Z, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
            }
        };

        menuRedo = new JMenuItem("Redo") {
            {
                setName("MenuRedo");
                addActionListener(e -> {
                    if (undoManager.canRedo()) {
                        undoManager.redo();
                    }
                });
                setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z,
                        InputEvent.SHIFT_DOWN_MASK | Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
            }
        };

        final JMenu menuFile = new JMenu("File") {
            private static final long serialVersionUID = -843926481964895754L;
            {
                setName("MenuFile");
                add(menuOpen);
                add(menuSave);
                addSeparator();
                add(menuExit);
            }
        };

        final JMenu menuEdit = new JMenu("Edit") {
            {
                setName("MenuRedo");
                add(menuUndo);
                add(menuRedo);
            }
        };

        final JMenu menuSearch = new JMenu("Search") {
            private static final long serialVersionUID = 7631745700972370457L;
            {
                setName("MenuSearch");
                add(menuStartSearch);
                add(menuPreviousMatch);
                add(menuNextMatch);
                addSeparator();
                add(menuUseRegex);
            }
        };

        setJMenuBar(new JMenuBar() {
            private static final long serialVersionUID = 5674099574201432610L;
            {
                add(menuFile);
                add(menuEdit);
                add(menuSearch);
                setVisible(true);
                setOpaque(true);
            }
        });

        /* The Panel for Options */

        JPanel optionPanel = new JPanel() {
            private static final long serialVersionUID = -5116733378927020465L;
            {

                SpringLayout optionLayout = new SpringLayout();

                JButton saveButton = new JButton() {
                    private static final long serialVersionUID = 7183327104567601849L;
                    {
                        setName("SaveButton");
                        setIcon(saveIcon);
                        setPreferredSize(new Dimension(30, 30));
                        setBorderPainted(false);
                        addActionListener(saveAction);
                    }
                };

                JButton openButton = new JButton() {
                    private static final long serialVersionUID = -7951293119131539430L;
                    {
                        setName("OpenButton");
                        setIcon(openIcon);
                        setPreferredSize(new Dimension(30, 30));
                        setBorderPainted(false);
                        addActionListener(loadAction);
                    }
                };

                JButton searchButton = new JButton() {
                    private static final long serialVersionUID = 5992698205850273040L;
                    {
                        setName("StartSearchButton");
                        setIcon(searchIcon);
                        setPreferredSize(new Dimension(30, 30));
                        setBorderPainted(false);
                        addActionListener(searchAction);
                    }
                };

                JButton nextButton = new JButton() {
                    private static final long serialVersionUID = -8659935650088525947L;
                    {
                        setName("NextMatchButton");
                        setIcon(nextIcon);
                        setPreferredSize(new Dimension(30, 30));
                        setBorderPainted(false);
                        addActionListener(nextAction);
                    }
                };

                JButton previousButton = new JButton() {
                    private static final long serialVersionUID = -3194434895459964059L;
                    {
                        setName("PreviousMatchButton");
                        setIcon(previousIcon);
                        setPreferredSize(new Dimension(30, 30));
                        setBorderPainted(false);
                        addActionListener(previousAction);
                    }
                };

                /* Layout for toolbar */
                optionLayout.putConstraint(SpringLayout.NORTH, searchField, 0, SpringLayout.NORTH, this);
                optionLayout.putConstraint(SpringLayout.NORTH, saveButton, 0, SpringLayout.NORTH, this);
                optionLayout.putConstraint(SpringLayout.NORTH, openButton, 0, SpringLayout.NORTH, this);
                optionLayout.putConstraint(SpringLayout.NORTH, searchButton, 0, SpringLayout.NORTH, this);
                optionLayout.putConstraint(SpringLayout.NORTH, nextButton, 0, SpringLayout.NORTH, this);
                optionLayout.putConstraint(SpringLayout.NORTH, previousButton, 0, SpringLayout.NORTH, this);
                optionLayout.putConstraint(SpringLayout.NORTH, regexCheckbox, 0, SpringLayout.NORTH, this);
                optionLayout.putConstraint(SpringLayout.SOUTH, this, 0, SpringLayout.SOUTH, openButton);
                optionLayout.putConstraint(SpringLayout.SOUTH, saveButton, 0, SpringLayout.SOUTH, this);
                optionLayout.putConstraint(SpringLayout.SOUTH, searchButton, 0, SpringLayout.SOUTH, this);
                optionLayout.putConstraint(SpringLayout.SOUTH, nextButton, 0, SpringLayout.SOUTH, this);
                optionLayout.putConstraint(SpringLayout.SOUTH, previousButton, 0, SpringLayout.SOUTH, this);
                optionLayout.putConstraint(SpringLayout.SOUTH, regexCheckbox, 0, SpringLayout.SOUTH, this);
                optionLayout.putConstraint(SpringLayout.SOUTH, searchField, 0, SpringLayout.SOUTH, this);
                optionLayout.putConstraint(SpringLayout.WEST, openButton, 0, SpringLayout.WEST, this);
                optionLayout.putConstraint(SpringLayout.WEST, saveButton, 0, SpringLayout.EAST, openButton);
                optionLayout.putConstraint(SpringLayout.WEST, searchField, 0, SpringLayout.EAST, saveButton);
                optionLayout.putConstraint(SpringLayout.EAST, regexCheckbox, 0, SpringLayout.EAST, this);
                optionLayout.putConstraint(SpringLayout.EAST, nextButton, 0, SpringLayout.WEST, regexCheckbox);
                optionLayout.putConstraint(SpringLayout.EAST, previousButton, 0, SpringLayout.WEST, nextButton);
                optionLayout.putConstraint(SpringLayout.EAST, searchButton, 0, SpringLayout.WEST, previousButton);
                optionLayout.putConstraint(SpringLayout.EAST, searchField, 0, SpringLayout.WEST, searchButton);

                add(searchField);
                add(saveButton);
                add(openButton);
                add(searchButton);
                add(previousButton);
                add(nextButton);
                add(regexCheckbox);
                setLayout(optionLayout);
            }
        };

        /* JFrame Layout */
        mainLayout.putConstraint(SpringLayout.NORTH, optionPanel, 5, SpringLayout.NORTH, getContentPane());
        mainLayout.putConstraint(SpringLayout.NORTH, textPane, 5, SpringLayout.SOUTH, optionPanel);
        mainLayout.putConstraint(SpringLayout.SOUTH, textPane, -15, SpringLayout.SOUTH, getContentPane());
        mainLayout.putConstraint(SpringLayout.EAST, textPane, -15, SpringLayout.EAST, getContentPane());
        mainLayout.putConstraint(SpringLayout.WEST, textPane, 15, SpringLayout.WEST, getContentPane());
        mainLayout.putConstraint(SpringLayout.EAST, optionPanel, -15, SpringLayout.EAST, getContentPane());
        mainLayout.putConstraint(SpringLayout.WEST, optionPanel, 15, SpringLayout.WEST, getContentPane());

        /* set properties for JFrame */
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(400, 480);
        setTitle("Text Editor");
        add(optionPanel);
        add(textPane);
        setLocationRelativeTo(null);
        setLayout(mainLayout);
        setVisible(true);
    }
}
