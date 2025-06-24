package computation.software.codebase;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;

public class BilinearTransformMain extends JFrame {
    private JTextField numField, denField, samplingPeriodField, criticalFreqField, orderField, cutoffFreqField, rippleField, stopbandAttenField;
    private JCheckBox autoPreWarpCheckBox;
    private JComboBox<String> filterTypeCombo;
    private JTextArea outputArea;
    private FrequencyResponsePanel freqResponsePanel;
    private JButton feedbackButton, freqResponseButton, timeDomainButton;
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
        JLabel criticalFreqLabel = new JLabel("Critical Digital Input (ω_d, rad/s):");
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

        gbc.gridx = 0; gbc.gridy = 13; gbc.gridwidth = 2;
        timeDomainButton = new JButton("Time Domain Simulation");
        timeDomainButton.setBackground(new Color(0, 80, 100));
        timeDomainButton.setForeground(Color.WHITE);
        timeDomainButton.setFont(bahnschriftFont);
        timeDomainButton.setEnabled(false);
        inputPanel.add(timeDomainButton, gbc);

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
        timeDomainButton.addActionListener(e -> showTimeDomainSimulationWindow());
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
            output.append("Pre-Warped Frequency: ").append(String.format("%.4f", omega_a)).append(" rad/s (for ω_d = ").append(String.format("%.4f", omega_d)).append(" rad/sample)\n\n");
            output.append("Discrete Transfer Function H(z):\n").append(discreteTf.toString()).append("\n\n");
            output.append("Numeric Coefficients:\n");
            output.append("Numerator: ").append(Arrays.toString(numCoeffs)).append("\n");
            output.append("Denominator: ").append(Arrays.toString(denCoeffs)).append("\n\n");
            output.append("Stability: ").append(isStable ? "Stable" : "Unstable");
            outputArea.setText(output.toString());

            freqResponsePanel.setTransferFunction(discreteTf);
            freqResponsePanel.startAnimation();
            freqResponsePanel.repaint();

            feedbackButton.setEnabled(true);
            freqResponseButton.setEnabled(true);
            timeDomainButton.setEnabled(true);

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

    private void showTimeDomainSimulationWindow() {
        if (discreteTf != null) {
            TimeDomainSimulation timeDomainWindow = new TimeDomainSimulation(discreteTf);
            timeDomainWindow.setVisible(true);
        } else {
            JOptionPane.showMessageDialog(this, "No discrete transfer function available!", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private class FrequencyResponsePanel extends JPanel {
        private SymbolicTransferFunction tf;
        private double[] frequencies;
        private double[] magnitude;
        private double[] phase;
        private int currentPoint;
        private Timer animationTimer;
        private static final int NUM_POINTS = 100;
        private static final int ANIMATION_DELAY = 50; // ms per point

        public FrequencyResponsePanel() {
            setBackground(new Color(30, 30, 30));
            currentPoint = 0;
        }

        public void setTransferFunction(SymbolicTransferFunction tf) {
            this.tf = tf;
            if (tf != null) {
                frequencies = new double[NUM_POINTS];
                magnitude = new double[NUM_POINTS];
                phase = new double[NUM_POINTS];
                for (int i = 0; i < NUM_POINTS; i++) {
                    double omega = Math.PI * i / (NUM_POINTS - 1);
                    frequencies[i] = omega;
                    double[] response = computeFrequencyResponse(omega);
                    magnitude[i] = response[0];
                    phase[i] = response[1];
                }
            }
            currentPoint = 0;
            if (animationTimer != null) {
                animationTimer.cancel();
            }
            startAnimation();
        }

        private void startAnimation() {
            animationTimer = new Timer();
            animationTimer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    if (currentPoint < NUM_POINTS) {
                        currentPoint++;
                        repaint();
                    } else {
                        animationTimer.cancel();
                    }
                }
            }, 0, ANIMATION_DELAY);
        }

        private double[] computeFrequencyResponse(double omega) {
            double[] num = tf.getNumerator();
            double[] den = tf.getDenominator();
            double numReal = 0, numImag = 0, denReal = 0, denImag = 0;

            for (int i = 0; i < num.length; i++) {
                double angle = -i * omega;
                numReal += num[num.length - 1 - i] * Math.cos(angle);
                numImag += num[num.length - 1 - i] * Math.sin(angle);
            }
            for (int i = 0; i < den.length; i++) {
                double angle = -i * omega;
                denReal += den[den.length - 1 - i] * Math.cos(angle);
                denImag += den[den.length - 1 - i] * Math.sin(angle);
            }

            double mag = Math.sqrt(numReal * numReal + numImag * numImag) /
                         Math.sqrt(denReal * denReal + denImag * denImag);
            double phase = Math.atan2(numImag, numReal) - Math.atan2(denImag, denReal);
            return new double[]{20 * Math.log10(mag), Math.toDegrees(phase)};
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setFont(new Font("Bahnschrift", Font.PLAIN, 12));

            if (tf == null || frequencies == null || magnitude == null || phase == null) {
                g2.setColor(Color.WHITE);
                g2.drawString("No data to display", getWidth() / 2 - 50, getHeight() / 2);
                return;
            }

            int width = getWidth();
            int height = getHeight();
            int margin = 60;

            double maxMag = Arrays.stream(magnitude).filter(v -> !Double.isNaN(v) && !Double.isInfinite(v)).max().orElse(0.0) + 10;
            double minMag = Arrays.stream(magnitude).filter(v -> !Double.isNaN(v) && !Double.isInfinite(v)).min().orElse(-100.0) - 10;
            double maxPhase = 180;
            double minPhase = -180;
            double maxFreq = Math.PI;

            // Draw grid
            g2.setColor(new Color(30, 30, 30));
            for (int i = 0; i <= 10; i++) {
                int x = margin + i * (width - 2 * margin) / 10;
                int y = margin + i * (height - 2 * margin) / 10;
                g2.drawLine(x, margin, x, height - margin);
                g2.drawLine(margin, y, width - margin, y);
            }

            // Draw axes
            g2.setColor(Color.WHITE);
            g2.drawLine(margin, height - margin, width - margin, height - margin); // x-axis
            g2.drawLine(margin, height - margin, margin, margin); // y-axis

            // Label axes
            g2.drawString("Frequency (rad/sample)", width - margin - 60, height - margin + 20);
            g2.drawString("Magnitude (dB)", margin - 50, margin - 20);
            g2.drawString("Phase (degrees)", margin - 50, height / 2 - 20);
            for (int i = 0; i <= 5; i++) {
                double freq = i * maxFreq / 5;
                double mag = minMag + i * (maxMag - minMag) / 5;
                double phase = minPhase + i * (maxPhase - minPhase) / 5;
                int xPos = margin + i * (width - 2 * margin) / 5;
                int magPos = height - margin - i * (height - 2 * margin) / 5;
                int phasePos = height / 2 - margin - i * (height / 2 - 2 * margin) / 5;
                g2.drawString(String.format("%.2f", freq), xPos - 10, height - margin + 15);
                g2.drawString(String.format("%.0f", mag), margin - 50, magPos + 5);
                g2.drawString(String.format("%.0f", phase), margin - 50, phasePos + 5);
            }

            // Plot magnitude up to currentPoint
            g2.setColor(new Color(0, 120, 215));
            for (int i = 1; i <= currentPoint && i < NUM_POINTS; i++) {
                double x1 = (i - 1) * (width - 2 * margin) / (NUM_POINTS - 1) + margin;
                double x2 = i * (width - 2 * margin) / (NUM_POINTS - 1) + margin;
                double mag1 = magnitude[i - 1];
                double mag2 = magnitude[i];
                if (!Double.isNaN(mag1) && !Double.isInfinite(mag1) && !Double.isNaN(mag2) && !Double.isInfinite(mag2)) {
                    int magY1 = height - margin - (int) ((mag1 - minMag) * (height - 2 * margin) / (maxMag - minMag));
                    int magY2 = height - margin - (int) ((mag2 - minMag) * (height - 2 * margin) / (maxMag - minMag));
                    g2.drawLine((int) x1, magY1, (int) x2, magY2);
                }
            }

            // Plot phase up to currentPoint
            g2.setColor(new Color(0, 200, 100));
            for (int i = 1; i <= currentPoint && i < NUM_POINTS; i++) {
                double x1 = (i - 1) * (width - 2 * margin) / (NUM_POINTS - 1) + margin;
                double x2 = i * (width - 2 * margin) / (NUM_POINTS - 1) + margin;
                double phase1 = phase[i - 1];
                double phase2 = phase[i];
                if (!Double.isNaN(phase1) && !Double.isInfinite(phase1) && !Double.isNaN(phase2) && !Double.isInfinite(phase2)) {
                    int phaseY1 = height / 2 - margin - (int) ((phase1 - minPhase) * (height / 2 - 2 * margin) / (maxPhase - minPhase));
                    int phaseY2 = height / 2 - margin - (int) ((phase2 - minPhase) * (height / 2 - 2 * margin) / (maxPhase - minPhase));
                    g2.drawLine((int) x1, phaseY1, (int) x2, phaseY2);
                }
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new BilinearTransformMain().setVisible(true));
    }
}