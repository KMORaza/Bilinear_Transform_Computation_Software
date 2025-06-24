package computation.software.codebase;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.Arrays;

public class BilinearTransformMain extends JFrame {
    private JTextField numField, denField, samplingPeriodField, criticalFreqField;
    private JCheckBox autoPreWarpCheckBox;
    private JTextArea outputArea;
    private FrequencyResponsePanel freqResponsePanel;
    private JButton feedbackButton;
    private SymbolicTransferFunction discreteTf;
    private final Font bahnschriftFont = new Font("Bahnschrift", Font.PLAIN, 14);

    public BilinearTransformMain() {
        // Set Windows Classic Look and Feel
        try {
            UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsClassicLookAndFeel");
        } catch (Exception e) {
            System.err.println("Failed to set Windows Classic Look and Feel: " + e.getMessage());
        }

        setTitle("Bilinear Transform Computation");
        setSize(900, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));
        getContentPane().setBackground(new Color(30, 30, 30));

        // Input Panel
        JPanel inputPanel = new JPanel();
        inputPanel.setLayout(new GridBagLayout());
        inputPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY), "Input Parameters", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, bahnschriftFont, Color.WHITE));
        inputPanel.setBackground(new Color(30, 30, 30));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Numerator Coefficients
        gbc.gridx = 0; gbc.gridy = 0;
        JLabel numLabel = new JLabel("Numerator Coefficients (s, comma-separated):");
        numLabel.setFont(bahnschriftFont);
        numLabel.setForeground(Color.WHITE);
        inputPanel.add(numLabel, gbc);
        numField = new JTextField("1", 20);
        numField.setFont(bahnschriftFont);
        numField.setForeground(Color.WHITE);
        numField.setBackground(new Color(50, 50, 50));
        gbc.gridx = 1; gbc.gridy = 0;
        inputPanel.add(numField, gbc);

        // Denominator Coefficients
        gbc.gridx = 0; gbc.gridy = 1;
        JLabel denLabel = new JLabel("Denominator Coefficients (s, comma-separated):");
        denLabel.setFont(bahnschriftFont);
        denLabel.setForeground(Color.WHITE);
        inputPanel.add(denLabel, gbc);
        denField = new JTextField("1, 1", 20);
        denField.setFont(bahnschriftFont);
        denField.setForeground(Color.WHITE);
        denField.setBackground(new Color(50, 50, 50));
        gbc.gridx = 1; gbc.gridy = 1;
        inputPanel.add(denField, gbc);

        // Sampling Period
        gbc.gridx = 0; gbc.gridy = 2;
        JLabel samplingLabel = new JLabel("Sampling Period (T):");
        samplingLabel.setFont(bahnschriftFont);
        samplingLabel.setForeground(Color.WHITE);
        inputPanel.add(samplingLabel, gbc);
        samplingPeriodField = new JTextField("0.1", 10);
        samplingPeriodField.setFont(bahnschriftFont);
        samplingPeriodField.setForeground(Color.WHITE);
        samplingPeriodField.setBackground(new Color(50, 50, 50));
        gbc.gridx = 1; gbc.gridy = 2;
        inputPanel.add(samplingPeriodField, gbc);

        // Auto Pre-Warp Checkbox
        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2;
        autoPreWarpCheckBox = new JCheckBox("Enable Automatic Pre-Warping", true);
        autoPreWarpCheckBox.setFont(bahnschriftFont);
        autoPreWarpCheckBox.setForeground(Color.WHITE);
        autoPreWarpCheckBox.setBackground(new Color(30, 30, 30));
        inputPanel.add(autoPreWarpCheckBox, gbc);

        // Critical Frequency (Manual)
        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 1;
        JLabel criticalFreqLabel = new JLabel("Critical Digital Frequency (ω_d, rad/sample):");
        criticalFreqLabel.setFont(bahnschriftFont);
        criticalFreqLabel.setForeground(Color.WHITE);
        inputPanel.add(criticalFreqLabel, gbc);
        criticalFreqField = new JTextField("1.0", 10);
        criticalFreqField.setFont(bahnschriftFont);
        criticalFreqField.setForeground(Color.WHITE);
        criticalFreqField.setBackground(new Color(50, 50, 50));
        criticalFreqField.setEnabled(false);
        gbc.gridx = 1; gbc.gridy = 4;
        inputPanel.add(criticalFreqField, gbc);

        // Compute Button
        gbc.gridx = 0; gbc.gridy = 5; gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.NONE;
        JButton computeButton = new JButton("Compute");
        computeButton.setBackground(new Color(0, 80, 100));
        computeButton.setForeground(Color.WHITE);
        computeButton.setFont(bahnschriftFont);
        inputPanel.add(computeButton, gbc);

        // Feedback Button
        gbc.gridx = 0; gbc.gridy = 6; gbc.gridwidth = 2;
        feedbackButton = new JButton("Feedback");
        feedbackButton.setBackground(new Color(0, 80, 100));
        feedbackButton.setForeground(Color.WHITE);
        feedbackButton.setFont(bahnschriftFont);
        feedbackButton.setEnabled(false);
        inputPanel.add(feedbackButton, gbc);

        // Output Area
        outputArea = new JTextArea(12, 30);
        outputArea.setEditable(false);
        outputArea.setFont(new Font("Bahnschrift", Font.PLAIN, 12));
        outputArea.setForeground(Color.WHITE);
        outputArea.setBackground(new Color(50, 50, 50));
        JScrollPane outputScroll = new JScrollPane(outputArea);
        outputScroll.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY), "Transfer Functions and Coefficients", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, bahnschriftFont, Color.WHITE));
        outputScroll.setBackground(new Color(30, 30, 30));

        // Frequency Response Panel
        freqResponsePanel = new FrequencyResponsePanel();
        freqResponsePanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY), "Frequency Response", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, bahnschriftFont, Color.WHITE));
        freqResponsePanel.setBackground(new Color(30, 30, 30));

        // Layout Setup
        add(inputPanel, BorderLayout.WEST);
        add(outputScroll, BorderLayout.SOUTH);
        add(freqResponsePanel, BorderLayout.CENTER);

        // Action Listeners
        autoPreWarpCheckBox.addActionListener(e -> criticalFreqField.setEnabled(!autoPreWarpCheckBox.isSelected()));
        computeButton.addActionListener((ActionEvent e) -> computeTransform());
        feedbackButton.addActionListener(e -> showFeedbackWindow());
    }

    private void computeTransform() {
        try {
            // Parse inputs
            double[] num = Arrays.stream(numField.getText().split(","))
                    .map(String::trim).mapToDouble(Double::parseDouble).toArray();
            double[] den = Arrays.stream(denField.getText().split(","))
                    .map(String::trim).mapToDouble(Double::parseDouble).toArray();
            double T = Double.parseDouble(samplingPeriodField.getText());
            boolean autoPreWarp = autoPreWarpCheckBox.isSelected();
            double omega_d = autoPreWarp ? 1.0 : Double.parseDouble(criticalFreqField.getText());

            // Create analog transfer function
            SymbolicTransferFunction analogTf = new SymbolicTransferFunction(num, den, "s");

            // Apply pre-warping
            PreWarpingCapability preWarp = new PreWarpingCapability(T);
            double omega_a = preWarp.computePreWarpedFrequency(omega_d);
            SymbolicTransferFunction preWarpedTf = preWarp.applyPreWarping(analogTf, omega_d, omega_a);

            // Perform bilinear transform
            BilinearTransform bt = new BilinearTransform(T);
            discreteTf = bt.apply(preWarpedTf);

            // Verify stability
            StabilityVerification stability = new StabilityVerification(discreteTf);
            boolean isStable = stability.isStable();

            // Compute numeric coefficients
            double[] numCoeffs = discreteTf.getNumerator();
            double[] denCoeffs = discreteTf.getDenominator();

            // Display output
            StringBuilder output = new StringBuilder();
            output.append("Analog Transfer Function H(s):\n").append(analogTf.toString()).append("\n\n");
            output.append("Pre-Warped Analog Transfer Function H(s):\n").append(preWarpedTf.toString()).append("\n\n");
            output.append("Pre-Warped Frequency: ω_a = ").append(String.format("%.4f", omega_a)).append(" rad/s (for ω_d = ").append(String.format("%.4f", omega_d)).append(" rad/sample)\n\n");
            output.append("Discrete Transfer Function H(z):\n").append(discreteTf.toString()).append("\n\n");
            output.append("Numeric Coefficients:\n");
            output.append("Numerator: ").append(Arrays.toString(numCoeffs)).append("\n");
            output.append("Denominator: ").append(Arrays.toString(denCoeffs)).append("\n\n");
            output.append("Stability: ").append(isStable ? "Stable" : "Unstable");
            outputArea.setText(output.toString());

            // Update frequency response plot
            freqResponsePanel.setTransferFunction(discreteTf);
            freqResponsePanel.repaint();

            // Enable feedback button
            feedbackButton.setEnabled(true);

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Invalid input format!", "Error", JOptionPane.ERROR_MESSAGE);
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showFeedbackWindow() {
        if (discreteTf != null) {
            new StabilityFeedbackWindow(discreteTf).setVisible(true);
        } else {
            JOptionPane.showMessageDialog(this, "No discrete transfer function available!", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new BilinearTransformMain().setVisible(true));
    }
}