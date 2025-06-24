package computation.software.codebase;

import javax.swing.*;
import java.awt.*;
import java.util.Arrays;

public class FrequencyResponse extends JFrame {
    private final SymbolicTransferFunction tf;
    private final Font bahnschriftFont = new Font("Bahnschrift", Font.PLAIN, 12);
    private static final int NUM_POINTS = 512;
    private static final double EPSILON = 1e-10;

    public FrequencyResponse(SymbolicTransferFunction tf) {
        this.tf = tf;
        setTitle("Frequency Response Analysis");
        setSize(900, 600);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));
        getContentPane().setBackground(new Color(30, 30, 30));

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(bahnschriftFont);
        tabbedPane.setForeground(Color.WHITE);
        tabbedPane.setBackground(new Color(50, 50, 50));

        tabbedPane.addTab("Magnitude", createMagnitudePanel());
        tabbedPane.addTab("Phase", createPhasePanel());
        tabbedPane.addTab("Group Delay", createGroupDelayPanel());
        tabbedPane.addTab("Impulse Response", createImpulseResponsePanel());
        tabbedPane.addTab("Step Response", createStepResponsePanel());

        add(tabbedPane, BorderLayout.CENTER);
    }

    private JPanel createMagnitudePanel() {
        return new ResponsePanel("Magnitude Response (dB)", "Frequency (rad/sample)", "Magnitude (dB)") {
            @Override
            protected double[] computeResponse() {
                double[] response = new double[NUM_POINTS];
                for (int i = 0; i < NUM_POINTS; i++) {
                    double omega = i * Math.PI / (NUM_POINTS - 1);
                    double[] h = evaluateTransferFunction(omega);
                    response[i] = 20 * Math.log10(Math.sqrt(h[0] * h[0] + h[1] * h[1]) + EPSILON);
                }
                return response;
            }
        };
    }

    private JPanel createPhasePanel() {
        return new ResponsePanel("Phase Response", "Frequency (rad/sample)", "Phase (degrees)") {
            @Override
            protected double[] computeResponse() {
                double[] response = new double[NUM_POINTS];
                for (int i = 0; i < NUM_POINTS; i++) {
                    double omega = i * Math.PI / (NUM_POINTS - 1);
                    double[] h = evaluateTransferFunction(omega);
                    response[i] = Math.toDegrees(Math.atan2(h[1], h[0]));
                }
                return response;
            }
        };
    }

    private JPanel createGroupDelayPanel() {
        return new ResponsePanel("Group Delay", "Frequency (rad/sample)", "Group Delay (samples)") {
            @Override
            protected double[] computeResponse() {
                double[] response = new double[NUM_POINTS];
                double deltaOmega = Math.PI / (NUM_POINTS - 1);
                for (int i = 0; i < NUM_POINTS; i++) {
                    double omega = i * deltaOmega;
                    double[] h1 = evaluateTransferFunction(omega);
                    double[] h2 = evaluateTransferFunction(omega + deltaOmega * 0.01);
                    double phase1 = Math.atan2(h1[1], h1[0]);
                    double phase2 = Math.atan2(h2[1], h2[0]);
                    response[i] = -(phase2 - phase1) / (0.01 * deltaOmega);
                }
                return response;
            }
        };
    }

    private JPanel createImpulseResponsePanel() {
        return new ResponsePanel("Impulse Response", "Sample Index", "Amplitude") {
            @Override
            protected double[] computeResponse() {
                double[] response = new double[50]; // 50 samples
                double[] num = tf.getNumerator();
                double[] den = tf.getDenominator();
                double[] x = new double[response.length];
                x[0] = 1.0; // Impulse input
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
                return response;
            }
        };
    }

    private JPanel createStepResponsePanel() {
        return new ResponsePanel("Step Response", "Sample Index", "Amplitude") {
            @Override
            protected double[] computeResponse() {
                double[] response = new double[50]; // 50 samples
                double[] num = tf.getNumerator();
                double[] den = tf.getDenominator();
                double[] x = new double[response.length];
                Arrays.fill(x, 1.0); // Step input
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
                return response;
            }
        };
    }

    private double[] evaluateTransferFunction(double omega) {
        double[] num = tf.getNumerator();
        double[] den = tf.getDenominator();
        double realNum = 0, imagNum = 0, realDen = 0, imagDen = 0;

        for (int i = 0; i < num.length; i++) {
            double angle = -i * omega;
            realNum += num[num.length - 1 - i] * Math.cos(angle);
            imagNum += num[num.length - 1 - i] * Math.sin(angle);
        }

        for (int i = 0; i < den.length; i++) {
            double angle = -i * omega;
            realDen += den[den.length - 1 - i] * Math.cos(angle);
            imagDen += den[den.length - 1 - i] * Math.sin(angle);
        }

        double denom = realDen * realDen + imagDen * imagDen;
        if (Math.abs(denom) < EPSILON) return new double[]{0, 0};
        return new double[]{
                (realNum * realDen + imagNum * imagDen) / denom,
                (imagNum * realDen - realNum * imagDen) / denom
        };
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
            int nPoints = response.length;

            double xMin[] = new double[]{0, Double.MAX_VALUE};
            for (int i = 0; i < response.length; i++) {
                if (!Double.isNaN(response[i]) && !Double.isInfinite(response[i])) {
                    xMin[1] = Math.min(xMin[1], response[i]);
                    xMin[0] = Math.max(xMin[0], response[i]);
                }
            }
            double maxVal = xMin[0];
            double minVal = xMin[1];

            if (title.contains("Magnitude")) {
                maxVal = Math.max(maxVal, 0);
                minVal = Math.min(minVal, -100);
            } else if (title.contains("Phase")) {
                maxVal = 180;
                minVal = -180;
            } else if (title.contains("Group Delay")) {
                minVal = Math.min(minVal, 0);
            }

            double xMax = title.contains("Impulse") || title.contains("Step") ? nPoints - 1 : Math.PI;

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
                g2.drawString(String.format("%.2f", x), xPos - 10, height - margin + 15);
                g2.drawString(String.format("%.2f", y), margin - 40, yPos + 5);
            }

            // Plot response
            g2.setColor(new Color(0, 120, 215));
            for (int i = 1; i < nPoints; i++) {
                double x1 = (i - 1) * (width - 2 * margin) / (nPoints - 1) + margin;
                double x2 = i * (width - 2 * margin) / (nPoints - 1) + margin;
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