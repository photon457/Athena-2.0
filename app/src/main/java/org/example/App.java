package org.example;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicScrollBarUI;
import javax.swing.text.html.HTMLEditorKit;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.awt.image.BufferedImage;

public class App extends JFrame {
    private JPanel chatPanel;
    private JScrollPane scrollPane;
    private JTextField inputField;
    private JPanel topPanel;

    private JButton sendButton;
    private Requests requests = new Requests();
    private DatabaseManager dbManager = new DatabaseManager();
    private java.util.List<Map<String, Object>> conversationHistory = new ArrayList<>();
    private JLabel typingIndicator;
    private boolean isDarkMode = true;
    private JButton themeToggle;
    private JComboBox<String> aiFlavorCombo;
    private final Color LIGHT_BG = new Color(245, 245, 220);
    private final Color DARK_BG = new Color(31, 31, 31);
    private final Color LIGHT_SECONDARY = new Color(245, 245, 220);
    private final Color DARK_SECONDARY = new Color(40, 40, 40);
    private final Color LIGHT_TEXT = Color.BLACK;
    private final Color DARK_TEXT = new Color(255, 255, 255);

    // Gradients for chat bubbles
    private final GradientPaint USER_GRADIENT = new GradientPaint(0, 0, new Color(64, 93, 236), 0, 60,
            new Color(88, 81, 219));
    private final GradientPaint AI_GRADIENT = new GradientPaint(0, 0, new Color(52, 53, 65), 0, 60,
            new Color(64, 65, 79));
    private final GradientPaint USER_GRADIENT_LIGHT = new GradientPaint(0, 0, new Color(173, 216, 230), 0, 60,
            new Color(135, 206, 235));
    private final GradientPaint AI_GRADIENT_LIGHT = new GradientPaint(0, 0, new Color(240, 248, 255), 0, 60,
            new Color(224, 255, 255));

    // Gradients for backgrounds
    private final GradientPaint DARK_SECONDARY_GRADIENT = new GradientPaint(
            0, 0, DARK_SECONDARY,
            0, 50, new Color(35, 35, 35));
    private final GradientPaint LIGHT_SECONDARY_GRADIENT = new GradientPaint(
            0, 0, LIGHT_SECONDARY,
            0, 50, new Color(235, 240, 245));

    // === Rounded Chat Bubble Class ===
    // Modern button with hover effects
    static class ModernButton extends JButton {
        private boolean isHovered = false;

        public ModernButton(String text) {
            super(text);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setFont(new Font("Segoe UI", Font.PLAIN, 14));
            setCursor(new Cursor(Cursor.HAND_CURSOR));

            addMouseListener(new java.awt.event.MouseAdapter() {
                public void mouseEntered(java.awt.event.MouseEvent evt) {
                    isHovered = true;
                    repaint();
                }

                public void mouseExited(java.awt.event.MouseEvent evt) {
                    isHovered = false;
                    repaint();
                }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // Draw background
            if (isHovered) {
                g2.setColor(new Color(0, 0, 0, 20));
            }
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);

            super.paintComponent(g);
            g2.dispose();
        }
    }

    static class ChatBubble extends JPanel {
        private final GradientPaint gradient;
        private final JEditorPane messagePane;
        private final JLabel timestampLabel;
        private final boolean isUser;
        private final App parent;
        private final String originalText;

        public ChatBubble(String text, boolean isUser, App parent) {
            this.isUser = isUser;
            this.parent = parent;
            this.originalText = text;
            this.gradient = parent.isDarkMode
                    ? (isUser ? parent.USER_GRADIENT : parent.AI_GRADIENT)
                    : (isUser ? parent.USER_GRADIENT_LIGHT : parent.AI_GRADIENT_LIGHT);
            setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
            setOpaque(false);

            // Message pane with HTML support and copy functionality
            messagePane = new JEditorPane();
            messagePane.setContentType("text/html");
            messagePane.setEditorKit(new HTMLEditorKit());
            messagePane.setText("<html><body style='font-family: Segoe UI; font-size: 14px; color: " +
                    (parent.isDarkMode ? "#FFFFFF" : "#000000")
                    + "; width: 220px; margin: 0; padding: 12px 16px 6px 16px;'>" +
                    text + "</body></html>");
            messagePane.setEditable(false);
            messagePane.setOpaque(false);
            messagePane.setBorder(null);

            // Add right-click context menu for copy
            JPopupMenu popupMenu = new JPopupMenu();
            JMenuItem copyItem = new JMenuItem("Copy");
            copyItem.addActionListener(e -> {
                String selectedText = messagePane.getSelectedText();
                if (selectedText != null && !selectedText.isEmpty()) {
                    Toolkit.getDefaultToolkit().getSystemClipboard().setContents(
                            new java.awt.datatransfer.StringSelection(selectedText), null);
                } else {
                    // Copy entire message if nothing selected
                    Toolkit.getDefaultToolkit().getSystemClipboard().setContents(
                            new java.awt.datatransfer.StringSelection(originalText), null);
                }
            });
            popupMenu.add(copyItem);
            messagePane.setComponentPopupMenu(popupMenu);

            // Timestamp label
            String time = String.format("%tR", System.currentTimeMillis());
            timestampLabel = new JLabel(time);
            timestampLabel.setFont(new Font("Arial", Font.PLAIN, 10));
            timestampLabel.setForeground(new Color(200, 200, 200));
            timestampLabel.setBorder(new EmptyBorder(0, 14, 5, 14));
            timestampLabel.setAlignmentX(isUser ? RIGHT_ALIGNMENT : LEFT_ALIGNMENT);

            add(messagePane);
            add(timestampLabel);
        }

        public void updateTheme(boolean isDarkMode) {
            String color = isDarkMode ? "#FFFFFF" : "#000000";
            messagePane.setText("<html><body style='font-family: Segoe UI; font-size: 14px; color: " + color +
                    "; width: 220px; margin: 0; padding: 12px 16px 6px 16px;'>" +
                    messagePane.getText()
                            .replaceAll("<body[^>]*>",
                                    "<body style='font-family: Segoe UI; font-size: 14px; color: " + color
                                            + "; width: 220px; margin: 0; padding: 12px 16px 6px 16px;'>")
                    + "</body></html>");
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setPaint(gradient);

            // Create bubble shape with more pronounced rounding
            int arc = 24;
            int shadowOffset = 4;
            int width = getWidth() - shadowOffset;
            int height = getHeight() - 15 - shadowOffset;

            // Draw shadow
            g2.setColor(new Color(0, 0, 0, 30));
            g2.fillRoundRect(shadowOffset, shadowOffset, width, height, arc, arc);

            // Draw gradient bubble
            g2.setPaint(gradient);
            g2.fillRoundRect(0, 0, width, height, arc, arc);

            // Add subtle highlight for 3D effect
            g2.setColor(new Color(255, 255, 255, 30));
            g2.drawRoundRect(0, 0, width, height, arc, arc);

            g2.dispose();
            super.paintComponent(g);
        }
    }

    static class GradientPanel extends JPanel {
        private final App parent;
        private final boolean isSecondary;

        public GradientPanel(App parent, boolean isSecondary, LayoutManager layout) {
            this.parent = parent;
            this.isSecondary = isSecondary;
            setOpaque(false);
            if (layout != null) {
                setLayout(layout);
            }
        }

        @Override
        protected void paintComponent(Graphics g) {
            if (getWidth() > 0 && getHeight() > 0) {
                BufferedImage img = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_RGB);
                Graphics2D g2img = img.createGraphics();
                g2img.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2img.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                g2img.setRenderingHint(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_ENABLE);
                g2img.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
                g2img.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
                g2img.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION,
                        RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
                GradientPaint gradient = isSecondary
                        ? (parent.isDarkMode ? new GradientPaint(0, 0, parent.DARK_SECONDARY, getWidth(), 0,
                                new Color(Math.max(0, parent.DARK_SECONDARY.getRed() - 10),
                                        Math.max(0, parent.DARK_SECONDARY.getGreen() - 10),
                                        Math.max(0, parent.DARK_SECONDARY.getBlue() - 10)))
                                : new GradientPaint(0, 0, parent.LIGHT_SECONDARY, getWidth(), 0,
                                        new Color(Math.max(0, parent.LIGHT_SECONDARY.getRed() - 20),
                                                Math.max(0, parent.LIGHT_SECONDARY.getGreen() - 20),
                                                Math.max(0, parent.LIGHT_SECONDARY.getBlue() - 20))))
                        : (parent.isDarkMode
                                ? new GradientPaint(0, 0, parent.DARK_BG, 0, getHeight(), new Color(1, 1, 1))
                                : new GradientPaint(0, 0, parent.LIGHT_BG, 0, getHeight(), new Color(252, 61, 3)));
                g2img.setPaint(gradient);
                g2img.fillRect(0, 0, getWidth(), getHeight());
                g2img.dispose();

                Graphics2D g2 = (Graphics2D) g.create();
                g2.drawImage(img, 0, 0, null);
                g2.dispose();
            }
            super.paintComponent(g);
        }
    }

    public App() {

        // Frame setup
        setTitle("Athena");
        setSize(600, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // Chat panel inside scroll pane
        chatPanel = new GradientPanel(this, false, null);
        chatPanel.setLayout(new BoxLayout(chatPanel, BoxLayout.Y_AXIS));

        scrollPane = new JScrollPane(chatPanel);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        add(scrollPane, BorderLayout.CENTER);

        // Top panel with theme toggle and AI flavor selector
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        topPanel.setBackground(Color.BLACK);
        topPanel.setBorder(new EmptyBorder(5, 10, 5, 10));

        // AI Flavor dropdown
        String[] flavors = { "Normal", "Happy", "Sad", "Angry", "Sarcastic", "Professional" };
        aiFlavorCombo = new JComboBox<>(flavors);
        aiFlavorCombo.setSelectedItem("Normal");
        aiFlavorCombo.setForeground(Color.WHITE);
        aiFlavorCombo.setBackground(Color.BLACK);
        aiFlavorCombo.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        aiFlavorCombo.setToolTipText("Select AI Personality");
        aiFlavorCombo.putClientProperty("JComponent.roundRect", true);
        aiFlavorCombo.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.WHITE, 1),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)));

        // Theme toggle button
        themeToggle = new ModernButton(isDarkMode ? "Light" : "Dark");
        themeToggle.addActionListener(e -> toggleTheme());
        themeToggle.setToolTipText("Toggle Dark/Light Mode");
        themeToggle.setForeground(Color.WHITE);

        topPanel.add(aiFlavorCombo);
        topPanel.add(themeToggle);

        add(topPanel, BorderLayout.NORTH);

        // Typing indicator
        typingIndicator = new JLabel("AI is typing...");
        typingIndicator.setForeground(new Color(200, 200, 200));
        typingIndicator.setVisible(false);

        // Input panel
        GradientPanel inputPanel = new GradientPanel(this, true, null);
        inputPanel.setLayout(new BoxLayout(inputPanel, BoxLayout.X_AXIS));
        inputPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        // Message input field (pill shaped & shorter)
        inputField = new JTextField();
        inputField.setForeground(Color.WHITE);
        inputField.setCaretColor(Color.WHITE);
        inputField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.WHITE, 1),
                BorderFactory.createEmptyBorder(10, 15, 10, 15)));
        inputField.putClientProperty("JComponent.roundRect", true);

        // Send button (rounded)
        sendButton = new JButton("Send");
        sendButton.putClientProperty("JButton.buttonType", "roundRect");
        sendButton.setBackground(new Color(173, 216, 230));
        sendButton.setOpaque(true);
        sendButton.setBorderPainted(false);
        sendButton.setContentAreaFilled(false);
        sendButton.setFocusPainted(false);
        sendButton.setForeground(Color.BLACK);
        sendButton.setFocusPainted(false);

        // Add to panel
        inputPanel.add(inputField);
        inputPanel.add(Box.createHorizontalGlue());
        inputPanel.add(sendButton);

        add(inputPanel, BorderLayout.SOUTH);

        // Modern scrollbar styling
        scrollPane.getVerticalScrollBar().setUI(new BasicScrollBarUI() {
            @Override
            protected void configureScrollBarColors() {
                this.thumbColor = new Color(64, 93, 236, 100);
                this.trackColor = isDarkMode ? DARK_SECONDARY : LIGHT_SECONDARY;
            }

            @Override
            protected JButton createDecreaseButton(int orientation) {
                return createZeroButton();
            }

            @Override
            protected JButton createIncreaseButton(int orientation) {
                return createZeroButton();
            }

            private JButton createZeroButton() {
                JButton button = new JButton();
                button.setPreferredSize(new Dimension(0, 0));
                button.setMinimumSize(new Dimension(0, 0));
                button.setMaximumSize(new Dimension(0, 0));
                return button;
            }
        });

        updateTheme();

        // Action when pressing Send or Enter
        ActionListener sendAction = e -> {
            String userText = inputField.getText().trim();
            if (!userText.isEmpty()) {
                sendButton.setEnabled(false);
                inputField.setEnabled(false);
                addMessage(userText, true);
                inputField.setText("");

                String selectedFlavor = (String) aiFlavorCombo.getSelectedItem();
                String flavorPrompt = getFlavorPrompt(selectedFlavor);

                String promptToSend = userText;
                if (conversationHistory.isEmpty()) {
                    promptToSend = flavorPrompt + userText;
                }

                Map<String, Object> userMessage = new HashMap<>();
                userMessage.put("role", "user");
                userMessage.put("content", promptToSend);
                conversationHistory.add(userMessage);

                // Show typing indicator
                typingIndicator.setVisible(true);
                chatPanel.add(typingIndicator);
                chatPanel.revalidate();

                // Asynchronous API call using explicit threading
                new Thread(() -> {
                    try {
                        Thread.sleep(2000);
                        String aiResponse = requests.getAIMessage(conversationHistory);
                        SwingUtilities.invokeLater(() -> {
                            if (aiResponse.startsWith("IOException:")) {
                                conversationHistory.remove(conversationHistory.size() - 1);
                            }
                            addMessage(aiResponse, false);
                            typingIndicator.setVisible(false);
                            sendButton.setEnabled(true);
                            inputField.setEnabled(true);
                            inputField.requestFocus();

                            Map<String, Object> aiMessage = new HashMap<>();
                            aiMessage.put("role", "assistant");
                            aiMessage.put("content", aiResponse);
                            conversationHistory.add(aiMessage);

                            // Save chat pair to database
                            dbManager.saveChatPair(userText, aiResponse);
                        });
                    } catch (Exception ex) {
                        SwingUtilities.invokeLater(() -> {
                            addMessage("Error: " + ex.getMessage(), false);
                            typingIndicator.setVisible(false);
                            sendButton.setEnabled(true);
                            inputField.setEnabled(true);
                            inputField.requestFocus();
                        });
                    }
                }).start();
            }
        };

        sendButton.addActionListener(sendAction);
        inputField.addActionListener(sendAction); // Enter key shortcut

        setVisible(true);
    }

    // Add message with bubble
    private void addMessage(String message, boolean isUser) {
        JPanel messagePanel = new JPanel(new FlowLayout(isUser ? FlowLayout.RIGHT : FlowLayout.LEFT));
        messagePanel.setOpaque(false);

        ChatBubble bubble = new ChatBubble(message, isUser, this);

        messagePanel.add(bubble);
        chatPanel.add(messagePanel);
        chatPanel.revalidate();
        chatPanel.repaint();

        // Auto scroll
        JScrollBar vertical = scrollPane.getVerticalScrollBar();
        vertical.setValue(vertical.getMaximum());
    }

    private void toggleTheme() {
        isDarkMode = !isDarkMode;
        updateTheme();
    }

    private void updateTheme() {
        // Update main components
        getContentPane().setBackground(isDarkMode ? DARK_BG : LIGHT_BG);

        // Update input field
        inputField.setBackground(isDarkMode ? DARK_SECONDARY : LIGHT_SECONDARY);
        inputField.setForeground(isDarkMode ? DARK_TEXT : LIGHT_TEXT);
        inputField.setCaretColor(isDarkMode ? DARK_TEXT : LIGHT_TEXT);
        inputField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(isDarkMode ? Color.WHITE : Color.BLACK, 1),
                BorderFactory.createEmptyBorder(10, 15, 10, 15)));

        // Update all panels
        for (Component c : getContentPane().getComponents()) {
            if (c instanceof JPanel && !(c instanceof GradientPanel)) {
                JPanel panel = (JPanel) c;
                panel.setBackground(isDarkMode ? DARK_SECONDARY : LIGHT_SECONDARY);
                panel.setBorder(isDarkMode ? null : BorderFactory.createLineBorder(Color.BLACK, 1));
                for (Component inner : panel.getComponents()) {
                    if (inner instanceof JLabel) {
                        inner.setForeground(isDarkMode ? DARK_TEXT : LIGHT_TEXT);
                    } else if (inner instanceof JButton) {
                        inner.setForeground(isDarkMode ? DARK_TEXT : LIGHT_TEXT);
                        if (inner == sendButton) {
                            ((JButton) inner).setBorderPainted(!isDarkMode);
                            ((JButton) inner)
                                    .setBorder(isDarkMode ? null : BorderFactory.createLineBorder(Color.BLACK, 1));
                        }
                    }
                }
            }
        }

        // Update scrollPane
        scrollPane.getVerticalScrollBar().setUI(new BasicScrollBarUI() {
            @Override
            protected void configureScrollBarColors() {
                this.thumbColor = new Color(64, 93, 236, 100);
                this.trackColor = isDarkMode ? DARK_SECONDARY : LIGHT_SECONDARY;
            }
        });

        // Update all message panels and chat bubbles
        for (Component c : chatPanel.getComponents()) {
            if (c instanceof JPanel) {
                c.setBackground(isDarkMode ? DARK_BG : LIGHT_BG);
                for (Component inner : ((JPanel) c).getComponents()) {
                    if (inner instanceof ChatBubble) {
                        ChatBubble bubble = (ChatBubble) inner;
                        bubble.updateTheme(isDarkMode);
                    }
                }
            }
        }

        // Keep topPanel black
        if (topPanel != null) {
            topPanel.setBackground(Color.BLACK);
        }

        // Repaint everything
        SwingUtilities.invokeLater(() -> {
            repaint();
            revalidate();
        });
    }

    private String getFlavorPrompt(String flavor) {
        switch (flavor) {
            case "Happy":
                return "You're a cheerful and optimistic AI assistant. Always respond with enthusiasm and positivity. Answer concisely. ";
            case "Sad":
                return "You're a melancholic AI assistant. Respond with a somber, reflective tone. Answer concisely. ";
            case "Angry":
                return "You're a frustrated AI assistant. Respond with irritation and strong opinions. Answer concisely. ";
            case "Sarcastic":
                return "You're a witty and sarcastic AI assistant. Use irony and clever remarks. Answer concisely. ";
            case "Professional":
                return "You're a formal and professional AI assistant. Respond with expertise and precision. Answer concisely. ";
            default:
                return "You're a helpful AI assistant. Answer concisely. ";
        }
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ex) {
            System.err.println("Failed to init LaF");
        }

        SwingUtilities.invokeLater(App::new);
    }
}
