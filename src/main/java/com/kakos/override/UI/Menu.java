package com.kakos.override.UI;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.LineBorder;

import com.kakos.override.editor.Console;
import com.kakos.override.engine.Core.Project;
import com.kakos.override.engine.Core.gameLoader.Crypter;

public class Menu {
    private static final String APP_DATA_DIR = determineAppDataDir();
    private static final String HISTORY_FILE = APP_DATA_DIR + File.separator + "projects.txt";
    private static final String SETTINGS_FILE = APP_DATA_DIR + File.separator + "settings.txt";

    private Color C_YELLOW = new Color(251, 237, 154);
    private final Color C_DARK = new Color(27, 27, 27);
    private final Color C_CARD = new Color(53, 53, 55);
    private final Color C_PINK = new Color(237, 127, 133);
    private final Font F_BIG = new Font("Arial", Font.BOLD, 22);
    private final Font F_MID = new Font("Arial", Font.BOLD, 14);

    private String projectPath = null;
    private final List<String> savedProjects = new ArrayList<>();
    private String currentAuthor = "Overrider";
    private String currentTheme = "Override";
    private String currentBuildType = "Release";
    private boolean currentAllowTelemetry = false;

    private JFrame frame;
    private JPanel homeTab;
    private JPanel docTab;
    private JPanel settingsTab;
    private JPanel settingsContent;
    private JPanel pGrid;
    private JPanel communityTab;
    private JPanel headerPanel;

    private JLabel lbl;
    private JLabel settingsTitle;
    private JLabel lblAuthor;
    private JLabel lblTheme;
    private JLabel lblApi;

    private JComboBox<String> comboTheme;
    private JTextField txtAuthor;
    private JRadioButton rbRelease;
    private JRadioButton rbBeta;
    private JCheckBox chkAutoSave;
    private JButton btnCreate;
    private JButton btnSaveSettings;
    private JButton[] navButtons;

    private static String determineAppDataDir() {
        String os = System.getProperty("os.name").toLowerCase();

        if (os.contains("win")) {
            String appData = System.getenv("APPDATA");
            if (appData != null) {
                return appData + File.separator + "OverrideEngine";
            }
        } else if (os.contains("nix") || os.contains("nux") || os.contains("mac")) {
            String xdgConfig = System.getenv("XDG_CONFIG_HOME");
            if (xdgConfig != null && !xdgConfig.isEmpty()) {
                return xdgConfig + File.separator + "OverrideEngine";
            }
            return System.getProperty("user.home") + File.separator + ".config" + File.separator + "OverrideEngine";
        }

        return System.getProperty("user.home") + File.separator + ".override_engine";
    }

    private void ensureAppDataDirExists() {
        File dir = new File(APP_DATA_DIR);
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }

    private void executeProjectLaunch(String path) {
        new Thread(() -> {
            try {
                Project.init(path);
                Project.start("main.lua");
            } catch (Exception ex) {
                Console.logError(Console.Modifier.OVERRIDE, "Engine crashed: " + ex.getMessage());
            }
        }, "Override-Engine-Thread").start();
    }

    private void createNewProjectAction() {
        JFileChooser ch = new JFileChooser();
        ch.setDialogTitle("Select folder");
        ch.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

        if (ch.showOpenDialog(frame) != JFileChooser.APPROVE_OPTION) {
            return;
        }

        String name = JOptionPane.showInputDialog(frame, "Name:", "New project", JOptionPane.QUESTION_MESSAGE);
        if (name == null || name.trim().isEmpty()) {
            return;
        }

        File newProjectDir = new File(ch.getSelectedFile(), name.trim());
        if (!newProjectDir.exists() && newProjectDir.mkdirs()) {
            for (String sub : List.of("sprites","fonts", "scripts", "music", "scenes", "resources")) {
                new File(newProjectDir, sub).mkdir();
            }

            String configTemplate = """
                    conf = {
                        title = "%s",
                        width = 1280,
                        height = 720,
                        fps = 60,
                        vsync = false,
                        fullscreen = false,
                        resizable = true,
                        decorated = true,
                        autoIconify = true,
                        maximized = false,
                        samples = 0,
                        useGL30 = false,
                        audioDeviceSimultaneousSources = 32,
                        iconPath = "icons/icon.png"
                    }
                    """.formatted(name.trim());

            writeFile(new File(newProjectDir, "conf.lua"), configTemplate);

            writeFile(new File(newProjectDir, "main.lua"), String.format(
                    "-- Base script/scene for %s\nprint(\"Project initialized successfully!\")",
                    name.trim()));

            projectPath = newProjectDir.getAbsolutePath();
            saveProjectToHistory(projectPath);
            if (frame != null) {
                frame.setVisible(false);
                frame.dispose();
            }
            executeProjectLaunch(projectPath);
        } else {
            JOptionPane.showMessageDialog(frame, "Error: folder exists.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public String showMenu() {
        loadProjectsHistory();
        loadSettings();

        frame = new JFrame("Override Engine build 0.0.1");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setSize(1000, 580);
        frame.setLocationRelativeTo(null);

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        split.setDividerSize(0);
        split.setDividerLocation(200);
        split.setEnabled(false);

        CardLayout cardLayout = new CardLayout();
        JPanel mainContentPanel = new JPanel(cardLayout);

        JPanel side = new JPanel();
        side.setBackground(C_DARK);
        side.setLayout(new BoxLayout(side, BoxLayout.Y_AXIS));
        side.setBorder(BorderFactory.createEmptyBorder(25, 20, 25, 20));
        JLayeredPane logoContainer = new JLayeredPane();
        logoContainer.setAlignmentX(Component.LEFT_ALIGNMENT);
        logoContainer.setPreferredSize(new Dimension(170, 90));
        logoContainer.setMinimumSize(new Dimension(170, 90));
        logoContainer.setMaximumSize(new Dimension(170, 90));

        JLabel logo = new JLabel("Override", SwingConstants.CENTER);
        logo.setFont(new Font("Arial", Font.BOLD, 20));
        logo.setForeground(Color.WHITE);
        logo.setBounds(0, 25, 170, 30);
        logoContainer.add(logo, JLayeredPane.PALETTE_LAYER);

        String imageResourcePath = "/overriders/" + ThreadLocalRandom.current().nextInt(1, 8 + 1) + ".png";

        java.net.URL imgURL = Menu.class.getResource(imageResourcePath);
        ImageIcon logoIcon;

        if (imgURL != null) {
            logoIcon = new ImageIcon(imgURL);
        } else {
            System.err.println("Resource not found: " + imageResourcePath);
            logoIcon = new ImageIcon();
        }

        Image scaledImage = logoIcon.getImage().getScaledInstance(75, 75, Image.SCALE_SMOOTH);
        JLabel logoImageLabel = new JLabel(new ImageIcon(scaledImage));
        logoImageLabel.setBounds(-12, 5, 75, 75);
        logoContainer.add(logoImageLabel, JLayeredPane.DEFAULT_LAYER);

        side.add(logoContainer);
        side.add(Box.createRigidArea(new Dimension(0, 25)));

        String[] nav = { "My projects", "Community", "Documentation", "Settings" };
        navButtons = new JButton[nav.length];

        for (int i = 0; i < nav.length; i++) {
            final int index = i;
            JButton btn = new JButton(nav[i]);
            btn.setFont(F_MID);
            btn.setFocusPainted(false);
            btn.setBorderPainted(false);
            btn.setMaximumSize(new Dimension(170, 35));
            btn.addActionListener(e -> {
                cardLayout.show(mainContentPanel, "Tab" + index);
                updateNavButtons(index);
            });
            navButtons[i] = btn;
            side.add(btn);
            side.add(Box.createRigidArea(new Dimension(0, 5)));
        }
        updateNavButtons(0);
        split.setLeftComponent(side);

        homeTab = new JPanel(new BorderLayout());
        headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBorder(BorderFactory.createEmptyBorder(30, 40, 10, 40));

        lbl = new JLabel("My projects");
        lbl.setFont(F_BIG);
        headerPanel.add(lbl, BorderLayout.WEST);

        btnCreate = new JButton("+ Create project");
        btnCreate.setFont(F_MID);
        btnCreate.setBackground(C_PINK);
        btnCreate.setForeground(Color.WHITE);
        btnCreate.setFocusPainted(false);
        btnCreate.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        headerPanel.add(btnCreate, BorderLayout.EAST);
        homeTab.add(headerPanel, BorderLayout.NORTH);

        pGrid = new JPanel();
        pGrid.setLayout(new BoxLayout(pGrid, BoxLayout.Y_AXIS));
        pGrid.setBorder(BorderFactory.createEmptyBorder(10, 40, 30, 40));

        JPanel cSel = createAndroidCard("Select new...", "Open project", true, true);
        cSel.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                JFileChooser ch = new JFileChooser();
                ch.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                if (ch.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION) {
                    projectPath = ch.getSelectedFile().getAbsolutePath();
                    saveProjectToHistory(projectPath);
                    if (frame != null) {
                        frame.setVisible(false);
                        frame.dispose();
                    }
                    executeProjectLaunch(projectPath);
                }
            }
        });
        pGrid.add(cSel);
        pGrid.add(Box.createRigidArea(new Dimension(0, 12)));
        btnCreate.addActionListener(e -> createNewProjectAction());
        renderProjectList();
        JScrollPane scroll = new JScrollPane(pGrid);
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        homeTab.add(scroll, BorderLayout.CENTER);
        JPanel browseTab = new JPanel();
        docTab = new JPanel();
        JButton browseButton = new JButton("Browse documentation");
        browseButton.addActionListener(e -> openWebpage("https://ilia89706-byte.github.io/overrideDoc/"));
        JButton offlineButton = new JButton("Offline documentation");
        offlineButton.setEnabled(false);
        docTab.add(browseButton);
        docTab.add(offlineButton);
        communityTab = new JPanel();
        JButton community = new JButton("Telegram");
        community.addActionListener(e -> openWebpage("https://t.me/overrideCommunity"));
        communityTab.add(community);
        settingsTab = new JPanel(new BorderLayout());
        settingsContent = new JPanel(new GridBagLayout());
        settingsContent.setBorder(BorderFactory.createEmptyBorder(30, 40, 30, 40));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(10, 0, 10, 15);
        gbc.anchor = GridBagConstraints.NORTHWEST;
        settingsTitle = new JLabel("Settings");
        settingsTitle.setFont(F_BIG);
        gbc.gridwidth = 2;
        gbc.gridx = 0;
        gbc.gridy = 0;
        settingsContent.add(settingsTitle, gbc);
        gbc.gridwidth = 1;
        gbc.gridy = 1;
        lblAuthor = new JLabel("Author:");
        lblAuthor.setFont(F_MID);
        settingsContent.add(lblAuthor, gbc);
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        txtAuthor = new JTextField(currentAuthor);
        txtAuthor.setFont(F_MID);
        settingsContent.add(txtAuthor, gbc);
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weightx = 0;
        lblTheme = new JLabel("Interface theme:");
        lblTheme.setFont(F_MID);
        settingsContent.add(lblTheme, gbc);
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        comboTheme = new JComboBox<>(new String[] { "Override", "White", "Black", "SystemTheme" });
        comboTheme.setSelectedItem(currentTheme);
        comboTheme.setFont(F_MID);
        comboTheme.setBackground(Color.WHITE);
        comboTheme.addActionListener(e -> applyThemeColors((String) comboTheme.getSelectedItem()));
        settingsContent.add(comboTheme, gbc);
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.weightx = 0;
        lblApi = new JLabel("Build type:");
        lblApi.setFont(F_MID);
        settingsContent.add(lblApi, gbc);
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        JPanel radioPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        radioPanel.setOpaque(false);
        rbRelease = new JRadioButton("Override alpha [windows,linux]", currentBuildType.equals("pre alpha,alpha"));
        rbBeta = new JRadioButton("Override java builder [.exe,.jar,compilie .exe]",
                currentBuildType.equals("release"));
        rbBeta.setEnabled(false);
        rbRelease.setOpaque(false);
        rbBeta.setOpaque(false);
        rbRelease.setFont(F_MID);
        rbBeta.setFont(F_MID);
        ButtonGroup apiGroup = new ButtonGroup();
        apiGroup.add(rbRelease);
        apiGroup.add(rbBeta);
        radioPanel.add(rbRelease);
        radioPanel.add(Box.createRigidArea(new Dimension(15, 0)));
        radioPanel.add(rbBeta);
        settingsContent.add(radioPanel, gbc);
        gbc.gridy = 4;
        chkAutoSave = new JCheckBox("-Allow sending data about PC", currentAllowTelemetry);
        chkAutoSave.setFont(F_MID);
        chkAutoSave.setOpaque(false);
        settingsContent.add(chkAutoSave, gbc);
        gbc.gridy = 5;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.SOUTHEAST;
        btnSaveSettings = new JButton("Save changes");
        btnSaveSettings.setFont(F_MID);
        btnSaveSettings.setBackground(C_PINK);
        btnSaveSettings.setForeground(Color.WHITE);
        btnSaveSettings.setFocusPainted(false);
        btnSaveSettings.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnSaveSettings.addActionListener(e -> {
            currentAuthor = txtAuthor.getText();
            currentTheme = (String) comboTheme.getSelectedItem();
            currentBuildType = rbRelease.isSelected() ? "Release" : "Beta";
            currentAllowTelemetry = chkAutoSave.isSelected();
            saveSettings();
            JOptionPane.showMessageDialog(frame, "Settings successfully saved!", "Success",
                    JOptionPane.INFORMATION_MESSAGE);
        });
        settingsContent.add(btnSaveSettings, gbc);
        JScrollPane settingsScroll = new JScrollPane(settingsContent);
        settingsScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        settingsScroll.setBorder(null);
        settingsScroll.getVerticalScrollBar().setUnitIncrement(16);
        settingsScroll.setOpaque(false);
        settingsScroll.getViewport().setOpaque(false);
        settingsTab.add(settingsScroll, BorderLayout.CENTER);
        mainContentPanel.add(homeTab, "Tab0");
        mainContentPanel.add(docTab, "Tab2");
        mainContentPanel.add(communityTab, "Tab1");
        mainContentPanel.add(settingsTab, "Tab3");
        split.setRightComponent(mainContentPanel);
        frame.add(split);
        applyThemeColors(currentTheme);
        frame.setVisible(true);
        while (frame != null && frame.isShowing() && frame.isVisible()) {
            try {
                Thread.sleep(100);
            } catch (Exception e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
        return projectPath;
    }

    private void updateNavButtons(int activeIndex) {
        for (int j = 0; j < navButtons.length; j++) {
            boolean isActive = (j == activeIndex);
            navButtons[j].setContentAreaFilled(isActive);
            navButtons[j].setBackground(isActive ? Color.WHITE : C_DARK);
            navButtons[j].setForeground(isActive ? C_DARK : new Color(180, 180, 180));
        }
    }

    private void setupHoverEffect(JButton button, Color normalColor, Color hoverColor) {
        button.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                button.setForeground(hoverColor);
            }

            public void mouseExited(MouseEvent e) {
                button.setForeground(normalColor);
            }
        });
    }

    private void renderProjectList() {
        Component[] comps = pGrid.getComponents();
        for (int i = comps.length - 1; i >= 2; i--) {
            pGrid.remove(comps[i]);
        }
        if (savedProjects.isEmpty()) {
            pGrid.add(Box.createVerticalGlue());
            JLabel emptyLabel = new JLabel("No projects");
            emptyLabel.setFont(F_BIG);
            emptyLabel.setForeground(new Color(40, 40, 42, 100));
            emptyLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            JPanel emptyPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
            emptyPanel.setOpaque(false);
            emptyPanel.add(emptyLabel);
            emptyPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
            pGrid.add(emptyPanel);
            pGrid.add(Box.createVerticalGlue());
        } else {
            for (String path : savedProjects) {
                File dir = new File(path);
                boolean isValid = dir.exists() && dir.isDirectory();
                JPanel card = createAndroidCard(isValid ? dir.getName() : "Disabled project", path, false, isValid);
                if (isValid) {
                    card.addMouseListener(new MouseAdapter() {
                        public void mouseClicked(MouseEvent e) {
                            if (frame != null) {
                                frame.setVisible(false);
                                frame.dispose();
                            }
                            executeProjectLaunch(path);
                        }
                    });
                } else {
                    card.setToolTipText("The path to the project does not exist, or the folder has been deleted!");
                }
                pGrid.add(card);
                pGrid.add(Box.createRigidArea(new Dimension(0, 12)));
            }
        }
    }

    private JPanel createAndroidCard(String title, String desc, boolean isAction, boolean isValid) {
        JPanel c = new JPanel(new BorderLayout(15, 0));
        boolean isDark = comboTheme != null && ("Black".equals(comboTheme.getSelectedItem()));
        c.setBackground(isAction ? C_YELLOW : (isDark ? new Color(60, 60, 63) : Color.WHITE));
        c.setPreferredSize(new Dimension(600, 65));
        c.setMinimumSize(new Dimension(600, 65));
        c.setMaximumSize(new Dimension(Short.MAX_VALUE, 65));
        c.setAlignmentX(Component.LEFT_ALIGNMENT);
        Color borderColor = isAction ? C_CARD
                : (isValid ? (isDark ? new Color(90, 90, 95) : new Color(220, 220, 220)) : new Color(230, 80, 80));
        c.setBorder(
                BorderFactory.createCompoundBorder(new LineBorder(borderColor, (isAction || !isValid) ? 2 : 1, true),
                        BorderFactory.createEmptyBorder(12, 15, 12, 15)));
        JPanel textPanel = new JPanel();
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.setOpaque(false);
        JLabel t = new JLabel(title);
        t.setFont(F_MID);
        t.setForeground(isDark ? Color.WHITE : C_CARD);
        JLabel d = new JLabel(desc);
        d.setFont(new Font("Arial", Font.PLAIN, 12));
        d.setForeground(isDark ? new Color(180, 180, 180) : Color.GRAY);
        textPanel.add(t);
        textPanel.add(Box.createRigidArea(new Dimension(0, 2)));
        textPanel.add(d);
        c.add(textPanel, BorderLayout.CENTER);
        if (!isAction) {
            JPanel actionButtonsPanel = new JPanel(new GridBagLayout());
            actionButtonsPanel.setOpaque(false);
            GridBagConstraints gbcBtn = new GridBagConstraints();
            gbcBtn.gridy = 0;
            gbcBtn.insets = new Insets(0, 5, 0, 5);
            if (isValid) {
                JButton btnExport = new JButton("Build");
                btnExport.setFont(new Font("Arial", Font.BOLD, 14));
                btnExport.setForeground(new Color(120, 180, 120));
                btnExport.setBorderPainted(false);
                btnExport.setContentAreaFilled(false);
                btnExport.setFocusPainted(false);
                btnExport.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                btnExport.setToolTipText("Build the project into a finished game");
                setupHoverEffect(btnExport, new Color(120, 180, 120), C_PINK);
                btnExport.addActionListener(e -> {
                    JFileChooser ch = new JFileChooser();
                    ch.setDialogTitle("Select a folder to save the finished build.");
                    ch.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                    if (ch.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION) {
                        exportProject(desc, ch.getSelectedFile().getAbsolutePath(), title);
                    }
                });
                gbcBtn.gridx = 0;
                actionButtonsPanel.add(btnExport, gbcBtn);
            }
            JButton btnDelete = new JButton("Delete");
            btnDelete.setFont(new Font("Arial", Font.BOLD, 14));
            btnDelete.setForeground(new Color(180, 180, 180));
            btnDelete.setBorderPainted(false);
            btnDelete.setContentAreaFilled(false);
            btnDelete.setFocusPainted(false);
            btnDelete.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            btnDelete.setToolTipText("Delete the project from the list");
            setupHoverEffect(btnDelete, new Color(180, 180, 180), new Color(230, 80, 80));
            btnDelete.addActionListener(e -> {
                int choice = JOptionPane.showConfirmDialog(SwingUtilities.getWindowAncestor(c),
                        "Do you really want to delete the project '" + title
                                + "' from the list?\n(Files on the disk will remain intact)",
                        "Deleting the project", JOptionPane.YES_NO_OPTION);
                if (choice == JOptionPane.YES_OPTION) {
                    removeProjectFromHistory(desc);
                    renderProjectList();
                    pGrid.revalidate();
                    pGrid.repaint();
                }
            });
            gbcBtn.gridx = 1;
            actionButtonsPanel.add(btnDelete, gbcBtn);
            c.add(actionButtonsPanel, BorderLayout.EAST);
        }
        if (isAction || isValid) {
            c.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        }
        return c;
    }

    private void applyThemeColors(String themeName) {
        Color bgTheme, textTheme, cardTextTheme, componentBg;
        switch (themeName) {
            case "White" -> {
                bgTheme = new Color(245, 245, 247);
                textTheme = new Color(30, 30, 30);
                cardTextTheme = new Color(50, 50, 50);
                componentBg = Color.WHITE;
            }
            case "Black" -> {
                bgTheme = new Color(40, 40, 42);
                textTheme = new Color(240, 240, 240);
                cardTextTheme = new Color(220, 220, 220);
                componentBg = new Color(60, 60, 63);
            }
            case "SystemTheme" -> {
                bgTheme = UIManager.getColor("Panel.background");
                textTheme = UIManager.getColor("Label.foreground");
                cardTextTheme = textTheme;
                componentBg = UIManager.getColor("TextField.background");
            }
            default -> {
                C_YELLOW = new Color(251, 237, 154);
                bgTheme = C_YELLOW;
                textTheme = new Color(53, 53, 55);
                cardTextTheme = textTheme;
                componentBg = Color.WHITE;
            }
        }
        C_YELLOW = bgTheme;
        for (JPanel p : List.of(homeTab, settingsTab, docTab, settingsContent, communityTab, pGrid, headerPanel)) {
            if (p != null)
                p.setBackground(bgTheme);
        }
        for (JLabel l : List.of(lbl, settingsTitle)) {
            if (l != null)
                l.setForeground(cardTextTheme);
        }
        for (JLabel l : List.of(lblAuthor, lblTheme, lblApi)) {
            if (l != null)
                l.setForeground(textTheme);
        }
        for (JToggleButton b : List.of(rbRelease, rbBeta, chkAutoSave)) {
            if (b != null)
                b.setForeground(textTheme);
        }
        if (txtAuthor != null) {
            txtAuthor.setBackground(componentBg);
            txtAuthor.setForeground(textTheme);
            txtAuthor.setBorder(BorderFactory.createCompoundBorder(new LineBorder(cardTextTheme, 1, true),
                    BorderFactory.createEmptyBorder(5, 8, 5, 8)));
        }
        if (comboTheme != null) {
            comboTheme.setBackground(componentBg);
            comboTheme.setForeground(textTheme);
        }
        if (btnCreate != null) {
            btnCreate.setBorder(BorderFactory.createCompoundBorder(new LineBorder(cardTextTheme, 1, true),
                    BorderFactory.createEmptyBorder(8, 15, 8, 15)));
        }
        if (btnSaveSettings != null) {
            btnSaveSettings.setBorder(BorderFactory.createCompoundBorder(new LineBorder(cardTextTheme, 1, true),
                    BorderFactory.createEmptyBorder(10, 20, 10, 20)));
        }
        if (pGrid != null) {
            renderProjectList();
            pGrid.revalidate();
            pGrid.repaint();
        }
    }

    private void exportProject(String srcProjectPath, String destBuildPath, String gameName) {
        Crypter.crypt(srcProjectPath, destBuildPath, gameName, frame);
    }

    private void writeFile(File file, String content) {
        try {
            Files.writeString(file.toPath(), content, StandardCharsets.UTF_8);
        } catch (IOException ex) {
            System.err.println("error file " + file.getName() + ": " + ex.getMessage());
        }
    }

    public void openWebpage(String urlString) {
        String osName = System.getProperty("os.name").toLowerCase();
        if (osName.contains("nix") || osName.contains("nux")) {
            try {
                new ProcessBuilder("xdg-open", urlString).start();
                return;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
            Desktop desktop = Desktop.getDesktop();
            if (desktop.isSupported(Desktop.Action.BROWSE)) {
                desktop.browse(new URI(urlString));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadProjectsHistory() {
        savedProjects.clear();
        File file = new File(HISTORY_FILE);
        if (!file.exists())
            return;
        try {
            savedProjects.addAll(Files.readAllLines(file.toPath(), StandardCharsets.UTF_8));
        } catch (IOException e) {
            System.err.println("Error reading project history: " + e.getMessage());
        }
    }

    private void saveProjectToHistory(String path) {
        ensureAppDataDirExists();
        LinkedHashSet uniqueProjects = new LinkedHashSet<>(savedProjects);
        uniqueProjects.remove(path);
        uniqueProjects.add(path);
        try {
            Files.write(Paths.get(HISTORY_FILE), uniqueProjects, StandardCharsets.UTF_8);
        } catch (IOException e) {
            System.err.println("Error saving the project to history: " + e.getMessage());
        }
        loadProjectsHistory();
    }

    private void removeProjectFromHistory(String path) {
        savedProjects.remove(path);
        try {
            Files.write(Paths.get(HISTORY_FILE), savedProjects, StandardCharsets.UTF_8);
        } catch (IOException e) {
            System.err.println("Error deleting project from history: " + e.getMessage());
        }
    }

    private void saveSettings() {
        ensureAppDataDirExists();
        List lines = List.of(
                "author=" + currentAuthor,
                "theme=" + currentTheme,
                "buildType=" + currentBuildType,
                "telemetry=" + currentAllowTelemetry);
        try {
            Files.write(Paths.get(SETTINGS_FILE), lines, StandardCharsets.UTF_8);
        } catch (IOException e) {
            System.err.println("Error saving settings: " + e.getMessage());
        }
    }

    private void loadSettings() {
        File file = new File(SETTINGS_FILE);
        if (!file.exists())
            return;
        try {
            for (String line : Files.readAllLines(file.toPath(), StandardCharsets.UTF_8)) {
                String[] parts = line.split("=", 2);
                if (parts.length < 2)
                    continue;
                String key = parts[0].trim();
                String value = parts[1].trim();
                switch (key) {
                    case "author" -> currentAuthor = value;
                    case "theme" -> currentTheme = value;
                    case "buildType" -> currentBuildType = value;
                    case "telemetry" -> currentAllowTelemetry = Boolean.parseBoolean(value);
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading settings: " + e.getMessage());
        }
    }
}
