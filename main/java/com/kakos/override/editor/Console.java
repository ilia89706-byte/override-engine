package com.kakos.override.editor;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Console {

    public enum Modifier {
        OVERRIDE(new Color(155, 105, 255)),
        LUA(new Color(50, 180, 255)),
        FILESYSTEM(new Color(245, 210, 80));

        private final Color color;
        private boolean isVisible = true;

        Modifier(Color color) {
            this.color = color;
        }

        public SimpleAttributeSet getStyle() {
            SimpleAttributeSet style = new SimpleAttributeSet();
            StyleConstants.setForeground(style, this.color);
            StyleConstants.setBold(style, true);
            return style;
        }
    }

    private static class LogEntry {
        String timeStr;
        String modStr;
        Modifier modifier;
        String textStr;
        SimpleAttributeSet textStyle;
        boolean isWarn;
        boolean isError;

        LogEntry(String timeStr, String modStr, Modifier modifier, String textStr, SimpleAttributeSet textStyle,
                boolean isWarn, boolean isError) {
            this.timeStr = timeStr;
            this.modStr = modStr;
            this.modifier = modifier;
            this.textStr = textStr;
            this.textStyle = textStyle;
            this.isWarn = isWarn;
            this.isError = isError;
        }
    }

    private static JFrame frame;
    private static JTabbedPane tabbedPane;
    private static JTextField searchField;

    private static JTextPane allLogsPane;
    private static JTextPane warningsPane;
    private static JTextPane errorsPane;

    private static final List<LogEntry> logHistory = new ArrayList<>();
    private static String currentSearchQuery = "";

    private static int warnCount = 0;
    private static int errorCount = 0;

    private static final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
    private static boolean isInitialized = false;

    private static final SimpleAttributeSet STYLE_TIME = new SimpleAttributeSet();
    private static final SimpleAttributeSet STYLE_BRACKETS = new SimpleAttributeSet();
    private static final SimpleAttributeSet STYLE_INFO = new SimpleAttributeSet();
    private static final SimpleAttributeSet STYLE_WARN = new SimpleAttributeSet();
    private static final SimpleAttributeSet STYLE_ERROR = new SimpleAttributeSet();

    private static final Color BG_DARK = new Color(30, 30, 32);
    private static final Color BG_LIGHTER = new Color(40, 40, 44);
    private static final Color TEXT_MUTED = new Color(130, 130, 135);

    private Console() {
    }

    public static synchronized void init() {
        if (isInitialized)
            return;

        try {
            StyleConstants.setForeground(STYLE_TIME, TEXT_MUTED);
            StyleConstants.setForeground(STYLE_BRACKETS, new Color(160, 160, 165));
            StyleConstants.setForeground(STYLE_INFO, new Color(230, 230, 235));
            StyleConstants.setForeground(STYLE_WARN, new Color(255, 165, 30));
            StyleConstants.setForeground(STYLE_ERROR, new Color(255, 85, 85));

            SwingUtilities.invokeAndWait(() -> {
                try {
                    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                } catch (Exception ignored) {
                }

                UIManager.put("TabbedPane.contentOpaque", Boolean.FALSE);
                UIManager.put("TabbedPane.tabsOpaque", Boolean.FALSE);
                UIManager.put("TabbedPane.borderHighlightColor", BG_DARK);
                UIManager.put("TabbedPane.darkShadow", BG_DARK);
                UIManager.put("TabbedPane.shadow", BG_DARK);

                frame = new JFrame("Override Console");
                frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                frame.setSize(800, 550);
                frame.setLocationRelativeTo(null);
                frame.getContentPane().setBackground(BG_DARK);

                JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
                toolbar.setBackground(BG_LIGHTER);
                toolbar.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, BG_DARK));

                JLabel searchLabel = new JLabel("🔍 Find:");
                searchLabel.setForeground(Color.WHITE);
                toolbar.add(searchLabel);

                searchField = new JTextField(15);
                searchField.setBackground(BG_DARK);
                searchField.setForeground(Color.WHITE);
                searchField.setCaretColor(Color.WHITE);
                searchField.setBorder(BorderFactory.createLineBorder(BG_DARK, 4));
                searchField.getDocument().addDocumentListener(new DocumentListener() {
                    public void insertUpdate(DocumentEvent e) {
                        updateSearch();
                    }

                    public void removeUpdate(DocumentEvent e) {
                        updateSearch();
                    }

                    public void changedUpdate(DocumentEvent e) {
                        updateSearch();
                    }

                    private void updateSearch() {
                        currentSearchQuery = searchField.getText().toLowerCase();
                        rebuildAllPanes();
                    }
                });
                toolbar.add(searchField);

                for (Modifier mod : Modifier.values()) {
                    JCheckBox checkBox = new JCheckBox(mod.name(), true);
                    checkBox.setBackground(BG_LIGHTER);
                    checkBox.setForeground(mod.color);
                    checkBox.setFocusPainted(false);
                    checkBox.addActionListener(e -> {
                        mod.isVisible = checkBox.isSelected();
                        rebuildAllPanes();
                    });
                    toolbar.add(checkBox);
                }

                tabbedPane = new JTabbedPane();
                tabbedPane.setBackground(BG_LIGHTER);
                tabbedPane.setForeground(new Color(200, 200, 205));
                tabbedPane.setFont(new Font("Segoe UI", Font.BOLD, 12));
                tabbedPane.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
                tabbedPane.setOpaque(false);

                allLogsPane = createTextPane();
                warningsPane = createTextPane();
                errorsPane = createTextPane();

                tabbedPane.addTab("💻 All Logs", createScrollPane(allLogsPane));
                tabbedPane.addTab("⚠ Warnings (0)", createScrollPane(warningsPane));
                tabbedPane.addTab("❌ Errors (0)", createScrollPane(errorsPane));

                frame.add(toolbar, BorderLayout.NORTH);
                frame.add(tabbedPane, BorderLayout.CENTER);
                frame.setVisible(true);
            });
            isInitialized = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static JTextPane createTextPane() {
        JTextPane pane = new JTextPane();
        pane.setBackground(BG_DARK);
        pane.setCaretColor(Color.WHITE);
        pane.setFont(new Font("Consolas", Font.PLAIN, 14));
        pane.setEditable(false);
        pane.setMargin(new Insets(12, 15, 12, 15));
        return pane;
    }

    private static JScrollPane createScrollPane(JTextPane pane) {
        JScrollPane scrollPane = new JScrollPane(pane);
        scrollPane.setBorder(BorderFactory.createLineBorder(BG_LIGHTER, 1));
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.getVerticalScrollBar().setBackground(BG_DARK);
        return scrollPane;
    }

    public static void log(Modifier modifier, Object message) {
        appendMessage(modifier, message, STYLE_INFO, false, false);
    }

    public static void logWarn(Modifier modifier, Object message) {
        appendMessage(modifier, message, STYLE_WARN, true, false);
    }

    public static void logError(Modifier modifier, Object message) {
        appendMessage(modifier, message, STYLE_ERROR, false, true);
    }

    private static void appendMessage(Modifier modifier, Object message, SimpleAttributeSet textStyle, boolean isWarn,
            boolean isError) {
        if (!isInitialized)
            return;

        String timeStr = "[" + timeFormat.format(new Date()) + "] ";
        String modStr = modifier.name();
        String textStr = " " + (message == null ? "null" : message.toString()) + "\n";

        LogEntry entry = new LogEntry(timeStr, modStr, modifier, textStr, textStyle, isWarn, isError);
        synchronized (logHistory) {
            logHistory.add(entry);
        }

        SwingUtilities.invokeLater(() -> {
            if (isWarn) {
                warnCount++;
                tabbedPane.setTitleAt(1, "⚠ Warnings (" + warnCount + ")");
            }
            if (isError) {
                errorCount++;
                tabbedPane.setTitleAt(2, "❌ Errors (" + errorCount + ")");
            }

            if (shouldShow(entry)) {
                writeEntryToPane(allLogsPane, entry);
                if (isWarn)
                    writeEntryToPane(warningsPane, entry);
                if (isError)
                    writeEntryToPane(errorsPane, entry);
            }
        });
    }

    private static boolean shouldShow(LogEntry entry) {
        if (!entry.modifier.isVisible)
            return false;

        if (!currentSearchQuery.isEmpty()) {
            return entry.textStr.toLowerCase().contains(currentSearchQuery) ||
                    entry.modStr.toLowerCase().contains(currentSearchQuery);
        }
        return true;
    }

    private static void rebuildAllPanes() {
        SwingUtilities.invokeLater(() -> {
            if (allLogsPane == null || warningsPane == null || errorsPane == null)
                return;

            allLogsPane.setText("");
            warningsPane.setText("");
            errorsPane.setText("");
            synchronized (logHistory) {
                for (LogEntry entry : logHistory) {
                    if (shouldShow(entry)) {
                        writeEntryToPane(allLogsPane, entry);
                        if (entry.isWarn)
                            writeEntryToPane(warningsPane, entry);
                        if (entry.isError)
                            writeEntryToPane(errorsPane, entry);
                    }
                }
            }
        });
    }

    private static void writeEntryToPane(JTextPane pane, LogEntry entry) {
        try {
            StyledDocument doc = pane.getStyledDocument();
            doc.insertString(doc.getLength(), entry.timeStr, STYLE_TIME);
            doc.insertString(doc.getLength(), "[", STYLE_BRACKETS);
            doc.insertString(doc.getLength(), entry.modStr, entry.modifier.getStyle());
            doc.insertString(doc.getLength(), "]", STYLE_BRACKETS);
            doc.insertString(doc.getLength(), entry.textStr, entry.textStyle);
            pane.setCaretPosition(doc.getLength());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void close() {
        if (frame != null) {
            SwingUtilities.invokeLater(() -> {
                frame.dispose();
                synchronized (logHistory) {
                    logHistory.clear();
                }
                warnCount = 0;
                errorCount = 0;
                isInitialized = false;
            });
        }
    }
}