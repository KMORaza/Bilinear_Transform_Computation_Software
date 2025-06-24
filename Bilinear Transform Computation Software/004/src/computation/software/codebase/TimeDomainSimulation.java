package computation.software.codebase;

import javax.swing.*;
import java.awt.*;
import java.util.Arrays;

public class TimeDomainSimulation extends JFrame {
    private final SymbolicTransferFunction tf;
    private final Font bahnschriftFont = new Font("Bahnschrift", Font.PLAIN, 12);
    private static final int NUM_SAMPLES = 50;
    private static final double EPSILON = 1e-10;

    public TimeDomainSimulation(SymbolicTransferFunction tf) {
        this.tf = tf;
        setTitle("Time Domain Simulation");
        setSize(900, 600);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));
        getContentPane().setBackground(new Color(30, 30, 30));

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(bahnschriftFont);
        tabbedPane.setForeground(Color.WHITE);
        tabbedPane.setBackground(new Color(50, 50, 50));

        tabbedPane.addTab("Impulse Response", createImpulseResponsePanel());
        tabbedPane.addTab("Step Response", createStepResponsePanel());
        tabbedPane.addTab("Input Response", createInputResponsePanel());

        add(tabbedPane, BorderLayout.CENTER);
    }

    private JPanel createImpulseResponsePanel() {
        return new ResponsePanel("Impulse Response", "Sample Index", "Amplitude") {
            @Override
            protected double[] computeResponse() {
                double[] response = new double[NUM_SAMPLES];
                double[] num = tf.getNumerator();
                double[] den = tf.getDenominator();
                double[] x = new double[NUM_SAMPLES];
                x[0] = 1.0; // Impulse input
                computeDifferenceEquation(response, x, num, den);
                return response;
            }
        };
    }

    private JPanel createStepResponsePanel() {
        return new ResponsePanel("Step Response", "Sample Index", "Amplitude") {
            @Override
            protected double[] computeResponse() {
                double[] response = new double[NUM_SAMPLES];
                double[] num = tf.getNumerator();
                double[] den = tf.getDenominator();
                double[] x = new double[NUM_SAMPLES];
                Arrays.fill(x, 1.0); // Step input
                computeDifferenceEquation(response, x, num, den);
                return response;
            }
        };
    }

    private JPanel createInputResponsePanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBackground(new Color(50, 50, 50));

        JTextField inputField = new JTextField("1, 0.5, 0.25, 20", 20);
        inputField.setFont(bahnschriftFont);
        inputField.setForeground(Color.WHITE);
        inputField.setBackground(new Color(30, 30, 30));

        JButton computeButton = new JButton("Compute Input Response");
        computeButton.setFont(bahnschriftFont);
        computeButton.setForeground(Color.WHITE);
        computeButton.setBackground(new Color(0, 80, 100));

        ResponsePanel responsePanel = new ResponsePanel("Input Response", "Sample Index", "Amplitude") {
            @Override
            protected double[] computeResponse() {
                double[] response = new double[NUM_SAMPLES];
                double[] num = tf.getNumerator();
                double[] den = tf.getDenominator();
                double[] x = new double[NUM_SAMPLES];
                try {
                    double[] input = Arrays.stream(inputField.getText().split(","))
                            .map(String::trim).mapToDouble(Double::parseDouble).toArray();
                    for (int i = 0; i < Math.min(input.length, NUM_SAMPLES); i++) {
                        x[i] = input[i];
                    }
                } catch (NumberFormatException e) {
                    JOptionPane.showMessageDialog(this, "Invalid input format!", "Error", JOptionPane.ERROR_MESSAGE);
                    return response; // Return zeros
                }
                computeDifferenceEquation(response, x, num, den);
                return response;
            }
        };

        computeButton.addActionListener(e -> responsePanel.repaint());

        JPanel controlPanel = new JPanel(new FlowLayout());
        controlPanel.setBackground(new Color(50, 50, 50));
        controlPanel.add(new JLabel("Input Sequence (comma-separated):"));
        controlPanel.add(inputField);
        controlPanel.add(computeButton);

        panel.add(controlPanel, BorderLayout.NORTH);
        panel.add(responsePanel, BorderLayout.CENTER);
        return panel;
    }

    private void computeDifferenceEquation(double[] response, double[] x, double[] num, double[] den) {
        for (int n = 0; n < response.length; n++) {
            double y = 0;
            for (int k = 0; k < num.length && n - k >= 0; k++) {
                y += num[num.length - 1 - k] * x[n - k];
            }
            for (int k = 1; k < den.length && n - k >= 0; k++) {
                y -= den[den.length - 1 - k] * response[n - k];
            }
            if (Math.abs(den[den.length - 1]) > EPSILON) {
                y /= den[den.length - 1];
            }
            response[n] = y;
        }
    }

    private abstract class ResponsePanel extends JPanel {
        private final String title;
        private final String xLabel;
        private final String yLabel;

        public ResponsePanel(String title, String xLabel, String yLabel) {
            this.title = title;
            this.xLabel = xLabel;
            this.yLabel = yLabel;
            setBackground(new Color(50, 50, 50));
            setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        }

        protected abstract double[] computeResponse();

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setFont(bahnschriftFont);

            if (tf == null) {
                g2.setColor(Color.WHITE);
                g2.drawString("No data to display", getWidth() / 2 - 50, getHeight() / 2);
                return;
            }

            int width = getWidth();
            int height = getHeight();
            int margin = 50;

            double[] response = computeResponse();
            double maxVal = Arrays.stream(response).filter(v -> !Double.isNaN(v) && !Double.isInfinite(v)).max().orElse(1.0);
            double minVal = Arrays.stream(response).filter(v -> !Double.isNaN(v) && !Double.isInfinite(v)).min().orElse(-1.0);
            if (Math.abs(maxVal - minVal) < EPSILON) {
                maxVal += 1.0;
                minVal -= 1.0;
            }

            double xMax = response.length - 1;

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
            g2.drawLine(margin, height - margin, width - margin, height - margin);
            g2.drawLine(margin, height - margin, margin, margin);

            // Label axes
            g2.drawString(xLabel, width - margin - 30, height - margin + 20);
            g2.drawString(yLabel, margin - 40, margin - 10);
            for (int i = 0; i <= 10; i++) {
                double x = i * xMax / 10;
                double y = minVal + i * (maxVal - minVal) / 10;
                int xPos = margin + i * (width - 2 * margin) / 10;
                int yPos = height - margin - i * (height - 2 * margin) / 10;
                g2.drawString(String.format("%.0f", x), xPos - 10, height - margin + 15);
                g2.drawString(String.format("%.2f", y), margin - 40, yPos + 5);
            }

            // Plot response
            g2.setColor(new Color(0, 120, 215));
            for (int i = 1; i < response.length; i++) {
                double x1 = (i - 1) * (width - 2 * margin) / (response.length - 1) + margin;
                double x2 = i * (width - 2 * margin) / (response.length - 1) + margin;
                double y1 = response[i - 1];
                double y2 = response[i];
                if (Double.isNaN(y1) || Double.isInfinite(y1) || Double.isNaN(y2) || Double.isInfinite(y2)) {
                    continue;
                }
                int y1Pos = height - margin - (int) ((y1 - minVal) * (height - 2 * margin) / (maxVal - minVal));
                int y2Pos = height - margin - (int) ((y2 - minVal) * (height - 2 * margin) / (maxVal - minVal));
                g2.drawLine((int) x1, y1Pos, (int) x2, y2Pos);
            }

            // Draw title
            g2.setColor(Color.WHITE);
            g2.drawString(title, width / 2 - 50, margin - 10);
        }
    }
}