package computation.software.codebase;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.Arrays;

public class BilinearTransformMain extends JFrame {
    private JTextField numField, denField, samplingPeriodField, criticalFreqField, orderField, cutoffFreqField, rippleField, stopbandAttenField;
    private JCheckBox autoPreWarpCheckBox;
    private JComboBox<String> filterTypeCombo;
    private JTextArea outputArea;
    private FrequencyResponsePanel freqResponsePanel;
    private JButton feedbackButton, freqResponseButton;
    private SymbolicTransferFunction discreteTf;
    private final Font bahnschriftFont = new Font("Bahnschrift", Font.PLAIN, 14);

    public BilinearTransformMain() {
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

        JPanel inputPanel = new JPanel();
        inputPanel.setLayout(new GridBagLayout());
        inputPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY), "Input Parameters", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, bahnschriftFont, Color.WHITE));
        inputPanel.setBackground(new Color(30, 30, 30));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Filter Type Selection
        gbc.gridx = 0; gbc.gridy = 0;
        JLabel filterTypeLabel = new JLabel("Filter Type:");
        filterTypeLabel.setFont(bahnschriftFont);
        filterTypeLabel.setForeground(Color.WHITE);
        inputPanel.add(filterTypeLabel, gbc);
        String[] filterOptions = {"Manual Input", "Butterworth", "Chebyshev I", "Chebyshev II", "Elliptic", "Bessel"};
        filterTypeCombo = new JComboBox<>(filterOptions);
        filterTypeCombo.setFont(bahnschriftFont);
        filterTypeCombo.setForeground(Color.WHITE);
        filterTypeCombo.setBackground(new Color(50, 50, 50));
        gbc.gridx = 1; gbc.gridy = 0;
        inputPanel.add(filterTypeCombo, gbc);

        // Filter Order
        gbc.gridx = 0; gbc.gridy = 1;
        JLabel orderLabel = new JLabel("Filter Order:");
        orderLabel.setFont(bahnschriftFont);
        orderLabel.setForeground(Color.WHITE);
        inputPanel.add(orderLabel, gbc);
        orderField = new JTextField("2", 10);
        orderField.setFont(bahnschriftFont);
        orderField.setForeground(Color.WHITE);
        orderField.setBackground(new Color(50, 50, 50));
        orderField.setEnabled(false);
        gbc.gridx = 1; gbc.gridy = 1;
        inputPanel.add(orderField, gbc);

        // Cutoff Frequency
        gbc.gridx = 0; gbc.gridy = 2;
        JLabel cutoffFreqLabel = new JLabel("Cutoff Frequency (rad/s):");
        cutoffFreqLabel.setFont(bahnschriftFont);
        cutoffFreqLabel.setForeground(Color.WHITE);
        inputPanel.add(cutoffFreqLabel, gbc);
        cutoffFreqField = new JTextField("1.0", 10);
        cutoffFreqField.setFont(bahnschriftFont);
        cutoffFreqField.setForeground(Color.WHITE);
        cutoffFreqField.setBackground(new Color(50, 50, 50));
        cutoffFreqField.setEnabled(false);
        gbc.gridx = 1; gbc.gridy = 2;
        inputPanel.add(cutoffFreqField, gbc);

        // Passband Ripple
        gbc.gridx = 0; gbc.gridy = 3;
        JLabel rippleLabel = new JLabel("Passband Ripple (dB):");
        rippleLabel.setFont(bahnschriftFont);
        rippleLabel.setForeground(Color.WHITE);
        inputPanel.add(rippleLabel, gbc);
        rippleField = new JTextField("1.0", 10);
        rippleField.setFont(bahnschriftFont);
        rippleField.setForeground(Color.WHITE);
        rippleField.setBackground(new Color(50, 50, 50));
        rippleField.setEnabled(false);
        gbc.gridx = 1; gbc.gridy = 3;
        inputPanel.add(rippleField, gbc);

        // Stopband Attenuation
        gbc.gridx = 0; gbc.gridy = 4;
        JLabel stopbandAttenLabel = new JLabel("Stopband Attenuation (dB):");
        stopbandAttenLabel.setFont(bahnschriftFont);
        stopbandAttenLabel.setForeground(Color.WHITE);
        inputPanel.add(stopbandAttenLabel, gbc);
        stopbandAttenField = new JTextField("30.0", 10);
        stopbandAttenField.setFont(bahnschriftFont);
        stopbandAttenField.setForeground(Color.WHITE);
        stopbandAttenField.setBackground(new Color(50, 50, 50));
        stopbandAttenField.setEnabled(false);
        gbc.gridx = 1; gbc.gridy = 4;
        inputPanel.add(stopbandAttenField, gbc);

        // Manual Numerator/Denominator Input
        gbc.gridx = 0; gbc.gridy = 5;
        JLabel numLabel = new JLabel("Numerator Coefficients (s, comma-separated):");
        numLabel.setFont(bahnschriftFont);
        numLabel.setForeground(Color.WHITE);
        inputPanel.add(numLabel, gbc);
        numField = new JTextField("1", 20);
        numField.setFont(bahnschriftFont);
        numField.setForeground(Color.WHITE);
        numField.setBackground(new Color(50, 50, 50));
        gbc.gridx = 1; gbc.gridy = 5;
        inputPanel.add(numField, gbc);

        gbc.gridx = 0; gbc.gridy = 6;
        JLabel denLabel = new JLabel("Denominator Coefficients (s, comma-separated):");
        denLabel.setFont(bahnschriftFont);
        denLabel.setForeground(Color.WHITE);
        inputPanel.add(denLabel, gbc);
        denField = new JTextField("1, 1", 20);
        denField.setFont(bahnschriftFont);
        denField.setForeground(Color.WHITE);
        denField.setBackground(new Color(50, 50, 50));
        gbc.gridx = 1; gbc.gridy = 6;
        inputPanel.add(denField, gbc);

        // Sampling Period
        gbc.gridx = 0; gbc.gridy = 7;
        JLabel samplingLabel = new JLabel("Sampling Period (T):");
        samplingLabel.setFont(bahnschriftFont);
        samplingLabel.setForeground(Color.WHITE);
        inputPanel.add(samplingLabel, gbc);
        samplingPeriodField = new JTextField("0.1", 10);
        samplingPeriodField.setFont(bahnschriftFont);
        samplingPeriodField.setForeground(Color.WHITE);
        samplingPeriodField.setBackground(new Color(50, 50, 50));
        gbc.gridx = 1; gbc.gridy = 7;
        inputPanel.add(samplingPeriodField, gbc);

        // Pre-Warping
        gbc.gridx = 0; gbc.gridy = 8; gbc.gridwidth = 2;
        autoPreWarpCheckBox = new JCheckBox("Enable Automatic Pre-Warping", true);
        autoPreWarpCheckBox.setFont(bahnschriftFont);
        autoPreWarpCheckBox.setForeground(Color.WHITE);
        autoPreWarpCheckBox.setBackground(new Color(30, 30, 30));
        inputPanel.add(autoPreWarpCheckBox, gbc);

        gbc.gridx = 0; gbc.gridy = 9; gbc.gridwidth = 1;
        JLabel criticalFreqLabel = new JLabel("Critical Digital Frequency (ω_d, rad/sample):");
        criticalFreqLabel.setFont(bahnschriftFont);
        criticalFreqLabel.setForeground(Color.WHITE);
        inputPanel.add(criticalFreqLabel, gbc);
        criticalFreqField = new JTextField("1.0", 10);
        criticalFreqField.setFont(bahnschriftFont);
        criticalFreqField.setForeground(Color.WHITE);
        criticalFreqField.setBackground(new Color(50, 50, 50));
        criticalFreqField.setEnabled(false);
        gbc.gridx = 1; gbc.gridy = 9;
        inputPanel.add(criticalFreqField, gbc);

        // Buttons
        gbc.gridx = 0; gbc.gridy = 10; gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.NONE;
        JButton computeButton = new JButton("Compute");
        computeButton.setBackground(new Color(0, 80, 100));
        computeButton.setForeground(Color.WHITE);
        computeButton.setFont(bahnschriftFont);
        inputPanel.add(computeButton, gbc);

        gbc.gridx = 0; gbc.gridy = 11; gbc.gridwidth = 2;
        feedbackButton = new JButton("Feedback");
        feedbackButton.setBackground(new Color(0, 80, 100));
        feedbackButton.setForeground(Color.WHITE);
        feedbackButton.setFont(bahnschriftFont);
        feedbackButton.setEnabled(false);
        inputPanel.add(feedbackButton, gbc);

        gbc.gridx = 0; gbc.gridy = 12; gbc.gridwidth = 2;
        freqResponseButton = new JButton("Frequency Response");
        freqResponseButton.setBackground(new Color(0, 80, 100));
        freqResponseButton.setForeground(Color.WHITE);
        freqResponseButton.setFont(bahnschriftFont);
        freqResponseButton.setEnabled(false);
        inputPanel.add(freqResponseButton, gbc);

        outputArea = new JTextArea(12, 30);
        outputArea.setEditable(false);
        outputArea.setFont(new Font("Bahnschrift", Font.PLAIN, 12));
        outputArea.setForeground(Color.WHITE);
        outputArea.setBackground(new Color(50, 50, 50));
        JScrollPane outputScroll = new JScrollPane(outputArea);
        outputScroll.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY), "Transfer Functions and Coefficients", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, bahnschriftFont, Color.WHITE));
        outputScroll.setBackground(new Color(30, 30, 30));

        freqResponsePanel = new FrequencyResponsePanel();
        freqResponsePanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY), "Frequency Response", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, bahnschriftFont, Color.WHITE));
        freqResponsePanel.setBackground(new Color(30, 30, 30));

        add(inputPanel, BorderLayout.WEST);
        add(outputScroll, BorderLayout.SOUTH);
        add(freqResponsePanel, BorderLayout.CENTER);

        autoPreWarpCheckBox.addActionListener(e -> {
            criticalFreqField.setEnabled(!autoPreWarpCheckBox.isSelected());
            updateInputFields();
        });
        filterTypeCombo.addActionListener(e -> updateInputFields());
        computeButton.addActionListener((ActionEvent e) -> computeTransform());
        feedbackButton.addActionListener(e -> showFeedbackWindow());
        freqResponseButton.addActionListener(e -> showFrequencyResponseWindow());
    }

    private void updateInputFields() {
        String selectedType = (String) filterTypeCombo.getSelectedItem();
        boolean isManual = "Manual Input".equals(selectedType);
        numField.setEnabled(isManual);
        denField.setEnabled(isManual);
        orderField.setEnabled(!isManual);
        cutoffFreqField.setEnabled(!isManual);
        rippleField.setEnabled(!isManual && ("Chebyshev I".equals(selectedType) || 
                                            "Chebyshev II".equals(selectedType) || 
                                            "Elliptic".equals(selectedType)));
        stopbandAttenField.setEnabled(!isManual && ("Chebyshev II".equals(selectedType) || 
                                                   "Elliptic".equals(selectedType)));
    }

    private void computeTransform() {
        try {
            double T = Double.parseDouble(samplingPeriodField.getText());
            boolean autoPreWarp = autoPreWarpCheckBox.isSelected();
            double omega_d = autoPreWarp ? 1.0 : Double.parseDouble(criticalFreqField.getText());
            SymbolicTransferFunction analogTf;

            String selectedType = (String) filterTypeCombo.getSelectedItem();
            if ("Manual Input".equals(selectedType)) {
                double[] num = Arrays.stream(numField.getText().split(","))
                        .map(String::trim).mapToDouble(Double::parseDouble).toArray();
                double[] den = Arrays.stream(denField.getText().split(","))
                        .map(String::trim).mapToDouble(Double::parseDouble).toArray();
                analogTf = new SymbolicTransferFunction(num, den, "s");
            } else {
                int order = Integer.parseInt(orderField.getText());
                double cutoffFreq = Double.parseDouble(cutoffFreqField.getText());
                double ripple = rippleField.isEnabled() ? Double.parseDouble(rippleField.getText()) : 0.0;
                double stopbandAtten = stopbandAttenField.isEnabled() ? Double.parseDouble(stopbandAttenField.getText()) : 0.0;
                ADFilterMapping filterMapper = new ADFilterMapping(T);
                ADFilterMapping.FilterType filterType;
                switch (selectedType) {
                    case "Butterworth":
                        filterType = ADFilterMapping.FilterType.BUTTERWORTH;
                        break;
                    case "Chebyshev I":
                        filterType = ADFilterMapping.FilterType.CHEBYSHEV_I;
                        break;
                    case "Chebyshev II":
                        filterType = ADFilterMapping.FilterType.CHEBYSHEV_II;
                        break;
                    case "Elliptic":
                        filterType = ADFilterMapping.FilterType.ELLIPTIC;
                        break;
                    case "Bessel":
                        filterType = ADFilterMapping.FilterType.BESSEL;
                        break;
                    default:
                        throw new IllegalArgumentException("Invalid filter type");
                }
                discreteTf = filterMapper.designFilter(filterType, order, cutoffFreq, ripple, stopbandAtten);
                analogTf = new SymbolicTransferFunction(discreteTf.getNumerator(), discreteTf.getDenominator(), "s");
            }

            PreWarpingCapability preWarp = new PreWarpingCapability(T);
            double omega_a = preWarp.computePreWarpedFrequency(omega_d);
            SymbolicTransferFunction preWarpedTf = preWarp.applyPreWarping(analogTf, omega_d, omega_a);

            if ("Manual Input".equals(selectedType)) {
                BilinearTransform bt = new BilinearTransform(T);
                discreteTf = bt.apply(preWarpedTf);
            }

            StabilityVerification stability = new StabilityVerification(discreteTf);
            boolean isStable = stability.isStable();

            double[] numCoeffs = discreteTf.getNumerator();
            double[] denCoeffs = discreteTf.getDenominator();

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

            freqResponsePanel.setTransferFunction(discreteTf);
            freqResponsePanel.repaint();

            feedbackButton.setEnabled(true);
            freqResponseButton.setEnabled(true);

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Invalid input format!", "Error", JOptionPane.ERROR_MESSAGE);
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showFeedbackWindow() {
        if (discreteTf != null) {
            StabilityFeedbackWindow feedbackWindow = new StabilityFeedbackWindow(discreteTf);
            feedbackWindow.setTransferFunction(discreteTf);
            feedbackWindow.setVisible(true);
        } else {
            JOptionPane.showMessageDialog(this, "No discrete transfer function available!", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showFrequencyResponseWindow() {
        if (discreteTf != null) {
            FrequencyResponse freqResponseWindow = new FrequencyResponse(discreteTf);
            freqResponseWindow.setVisible(true);
        } else {
            JOptionPane.showMessageDialog(this, "No discrete transfer function available!", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new BilinearTransformMain().setVisible(true));
    }
}