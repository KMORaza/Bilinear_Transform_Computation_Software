package computation.software.codebase;

import javax.swing.*;
import java.awt.*;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Arrays;
import javax.swing.border.TitledBorder;

public class InvBilinearTransform extends JFrame {
    private final SymbolicTransferFunction discreteTf;
    private JTextField samplingPeriodField;
    private JComboBox<Integer> precisionCombo;
    private JTextArea outputArea;
    private AnalogFrequencyResponsePanel freqResponsePanel;
    private int precision;
    private double T;
    private SymbolicTransferFunction analogTf;
    private final Font bahnschriftFont = new Font("Bahnschrift", Font.PLAIN, 14);

    public InvBilinearTransform(SymbolicTransferFunction tf, int precision, double T) {
        this.discreteTf = tf;
        this.precision = precision;
        this.T = T;
        setTitle("Inverse Bilinear Transform");
        setSize(900, 600);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));
        getContentPane().setBackground(new Color(30, 30, 30));

        JPanel controlPanel = new JPanel(new GridBagLayout());
        controlPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY), "Control Panel", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, bahnschriftFont, Color.WHITE));
        controlPanel.setBackground(new Color(30, 30, 30));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Sampling Period
        gbc.gridx = 0; gbc.gridy = 0;
        JLabel samplingLabel = new JLabel("Sampling Period (T):");
        samplingLabel.setFont(bahnschriftFont);
        samplingLabel.setForeground(Color.WHITE);
        controlPanel.add(samplingLabel, gbc);
        samplingPeriodField = new JTextField(String.valueOf(T), 10);
        samplingPeriodField.setFont(bahnschriftFont);
        samplingPeriodField.setForeground(Color.WHITE);
        samplingPeriodField.setBackground(new Color(50, 50, 50));
        gbc.gridx = 1; gbc.gridy = 0;
        controlPanel.add(samplingPeriodField, gbc);

        // Precision Control
        gbc.gridx = 0; gbc.gridy = 1;
        JLabel precisionLabel = new JLabel("Display Precision (decimals):");
        precisionLabel.setFont(bahnschriftFont);
        precisionLabel.setForeground(Color.WHITE);
        controlPanel.add(precisionLabel, gbc);
        Integer[] precisionOptions = {1, 2, 3, 4, 5, 6};
        precisionCombo = new JComboBox<>(precisionOptions);
        precisionCombo.setSelectedItem(precision);
        precisionCombo.setFont(bahnschriftFont);
        precisionCombo.setForeground(Color.WHITE);
        precisionCombo.setBackground(new Color(50, 50, 50));
        precisionCombo.addActionListener(e -> {
            this.precision = (Integer) precisionCombo.getSelectedItem();
            computeInverseTransform();
        });
        gbc.gridx = 1; gbc.gridy = 1;
        controlPanel.add(precisionCombo, gbc);

        // Compute Button
        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.NONE;
        JButton computeButton = new JButton("Compute");
        computeButton.setBackground(new Color(0, 80, 100));
        computeButton.setForeground(Color.WHITE);
        computeButton.setFont(bahnschriftFont);
        computeButton.addActionListener(e -> computeInverseTransform());
        controlPanel.add(computeButton, gbc);

        outputArea = new JTextArea(12, 30);
        outputArea.setEditable(false);
        outputArea.setFont(new Font("Bahnschrift", Font.PLAIN, 12));
        outputArea.setForeground(Color.WHITE);
        outputArea.setBackground(new Color(50, 50, 50));
        JScrollPane outputScroll = new JScrollPane(outputArea);
        outputScroll.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY), "Analog Transfer Function and Coefficients", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, bahnschriftFont, Color.WHITE));
        outputScroll.setBackground(new Color(30, 30, 30));

        freqResponsePanel = new AnalogFrequencyResponsePanel();
        freqResponsePanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY), "Analog Frequency Response", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, bahnschriftFont, Color.WHITE));
        freqResponsePanel.setBackground(new Color(30, 30, 30));

        add(controlPanel, BorderLayout.WEST);
        add(outputScroll, BorderLayout.SOUTH);
        add(freqResponsePanel, BorderLayout.CENTER);

        computeInverseTransform();
    }

    private void computeInverseTransform() {
        try {
            T = Double.parseDouble(samplingPeriodField.getText());
            if (T <= 0) throw new IllegalArgumentException("Sampling period must be positive");

            // Perform inverse bilinear transform: z = (2 + sT)/(2 - sT)
            double[] numZ = discreteTf.getNumerator();
            double[] denZ = discreteTf.getDenominator();
            int numDegree = numZ.length - 1;
            int denDegree = denZ.length - 1;

            // Compute new polynomial degrees
            int maxDegree = Math.max(numDegree, denDegree);
            double[] numS = new double[maxDegree + 1];
            double[] denS = new double[maxDegree + 1];

            // Substitute z = (2 + sT)/(2 - sT)
            for (int k = 0; k <= numDegree; k++) {
                double coeff = numZ[numDegree - k];
                for (int i = 0; i <= k; i++) {
                    double binom = binomial(k, i);
                    numS[maxDegree - (k - i)] += coeff * binom * Math.pow(2, k - i) * Math.pow(-2, i) * Math.pow(T, k - 2 * i);
                }
            }
            for (int k = 0; k <= denDegree; k++) {
                double coeff = denZ[denDegree - k];
                for (int i = 0; i <= k; i++) {
                    double binom = binomial(k, i);
                    denS[maxDegree - (k - i)] += coeff * binom * Math.pow(2, k - i) * Math.pow(-2, i) * Math.pow(T, k - 2 * i);
                }
            }

            // Normalize by highest denominator coefficient
            double leadingDen = denS[0];
            if (Math.abs(leadingDen) > 1e-10) {
                for (int i = 0; i <= maxDegree; i++) {
                    numS[i] /= leadingDen;
                    denS[i] /= leadingDen;
                }
            }

            analogTf = new SymbolicTransferFunction(numS, denS, "s");

            String format = "%." + precision + "f";
            StringBuilder output = new StringBuilder();
            output.append("Discrete Transfer Function H(z):\n").append(discreteTf.toString()).append("\n\n");
            output.append("Analog Transfer Function H(s):\n").append(analogTf.toString()).append("\n\n");
            output.append("Numeric Coefficients:\n");
            output.append("Numerator: [");
            for (int i = 0; i < numS.length; i++) {
                output.append(String.format(format, numS[i]));
                if (i < numS.length - 1) output.append(", ");
            }
            output.append("]\n");
            output.append("Denominator: [");
            for (int i = 0; i < denS.length; i++) {
                output.append(String.format(format, denS[i]));
                if (i < denS.length - 1) output.append(", ");
            }
            output.append("]\n");
            outputArea.setText(output.toString());

            freqResponsePanel.setTransferFunction(analogTf);
            freqResponsePanel.startAnimation();
            freqResponsePanel.repaint();

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Invalid sampling period format!", "Error", JOptionPane.ERROR_MESSAGE);
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private double binomial(int n, int k) {
        if (k < 0 || k > n) return 0;
        double result = 1;
        for (int i = 0; i < k; i++) {
            result *= (n - i);
            result /= (i + 1);
        }
        return result;
    }

    private class AnalogFrequencyResponsePanel extends JPanel {
        private SymbolicTransferFunction tf;
        private double[] frequencies;
        private double[] magnitude;
        private double[] phase;
        private int currentPoint;
        private Timer timer;
        private static final int NUM_POINTS = 100;
        private static final int ANIMATION_DURATION = 50; // ms per point

    private AnalogFrequencyResponsePanel() {
        super();
        setBackground(new Color(30, 30, 30));
        currentPoint = 0;
    }

    public void setTransferFunction(SymbolicTransferFunction tf) {
        this.tf = tf;
        if (tf != null) {
            frequencies = new double[NUM_POINTS];
            magnitude = new double[NUM_POINTS];
            phase = new double[NUM_POINTS];
            double maxOmega = 10.0; // Analog frequency up to 10 rad/s
            for (int i = 0; i < NUM_POINTS; i++) {
                double omega = i * maxOmega / (NUM_POINTS - 1);
                frequencies[i] = omega;
                double[] response = computeFrequencyResponse(omega);
                magnitude[i] = response[0];
                phase[i] = response[1];
            }
        }
        currentPoint = 0;
        if (timer != null) {
            timer.cancel();
        }
        startAnimation();
    }

    private void startAnimation() {
        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (currentPoint < NUM_POINTS) {
                    currentPoint++;
                    repaint();
                } else {
                    timer.cancel();
                }
            }
        }, 0, ANIMATION_DURATION);
    }

    private double[] computeFrequencyResponse(double omega) {
        double[] num = tf.getNumerator();
        double[] den = tf.getDenominator();
        double numReal = 0, numImag = 0, denReal = 0, denImag = 0;

        // Evaluate H(s) at s = j*omega
        for (int i = 0; i < num.length; i++) {
            double power = Math.pow(omega, i);
            if (i % 2 == 0) {
                numReal += num[num.length - 1 - i] * power;
            } else {
                numImag += num[num.length - 1 - i] * power * (i % 4 == 1 ? 1 : -1);
            }
        }
        for (int i = 0; i < den.length; i++) {
            double power = Math.pow(omega, i);
            if (i % 2 == 0) {
                denReal += den[den.length - 1 - i] * power;
            } else {
                denImag += den[den.length - 1 - i] * power * (i % 4 == 1 ? 1 : -1);
            }
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
        double maxFreq = frequencies[NUM_POINTS - 1];

        // Determine label format
        boolean useScientificMag = Math.abs(maxMag) > 1000 || Math.abs(minMag) > 1000;
        String magFormat = useScientificMag ? "%." + precision + "e" : "%." + precision + "f";
        String phaseFormat = "%." + precision + "f";
        String freqFormat = "%." + precision + "f";

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
        g2.drawString("Frequency (rad/s)", width - margin - 60, height - margin + 20);
        g2.drawString("Magnitude (dB)", margin - 50, margin - 20);
        g2.drawString("Phase (degrees)", margin - 50, height / 2 - 20);
        for (int i = 0; i <= 5; i++) {
            double freq = i * maxFreq / 5;
            double mag = minMag + i * (maxMag - minMag) / 5;
            double phase = minPhase + i * (maxPhase - minPhase) / 5;
            int xPos = margin + i * (width - 2 * margin) / 5;
            int magPos = height - margin - i * (height - 2 * margin) / 5;
            int phasePos = height / 2 - margin - i * (height / 2 - 2 * margin) / 5;
            g2.drawString(String.format(freqFormat, freq), xPos - 10, height - margin + 15);
            g2.drawString(String.format(magFormat, mag), margin - 50, magPos + 5);
            g2.drawString(String.format(phaseFormat, phase), margin - 50, phasePos + 5);
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
}}