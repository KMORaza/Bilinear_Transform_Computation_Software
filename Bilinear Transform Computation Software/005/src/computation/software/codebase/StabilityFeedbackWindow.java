package computation.software.codebase;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.util.Arrays;
import javax.swing.border.TitledBorder;

public class StabilityFeedbackWindow extends JFrame {
    private SymbolicTransferFunction tf;
    private final Font bahnschriftFont = new Font("Bahnschrift", Font.PLAIN, 12);
    private static final int NUM_POINTS = 512;

    public StabilityFeedbackWindow(SymbolicTransferFunction tf) {
        this.tf = tf;
        setTitle("Stability Feedback");
        setSize(900, 450);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new GridLayout(1, 2, 10, 10));
        getContentPane().setBackground(new Color(30, 30, 30));

        PoleZeroPanel poleZeroPanel = new PoleZeroPanel();
        poleZeroPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY), "Pole-Zero Plot", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, bahnschriftFont, Color.WHITE));
        add(poleZeroPanel);

        NyquistPanel nyquistPanel = new NyquistPanel();
        nyquistPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY), "Nyquist Plot", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, bahnschriftFont, Color.WHITE));
        add(nyquistPanel);
    }

    public void setTransferFunction(SymbolicTransferFunction tf) {
        this.tf = tf;
        for (Component component : getContentPane().getComponents()) {
            if (component instanceof PoleZeroPanel || component instanceof NyquistPanel) {
                component.repaint();
            }
        }
    }

    private class PoleZeroPanel extends JPanel {
        private String tooltipText = null;

        public PoleZeroPanel() {
            setBackground(new Color(30, 30, 30));
            setPreferredSize(new Dimension(450, 450));

            addMouseMotionListener(new MouseMotionAdapter() {
                @Override
                public void mouseMoved(MouseEvent e) {
                    tooltipText = getTooltipText(e.getX(), e.getY());
                    setToolTipText(tooltipText);
                    repaint(); // Ensure tooltip updates trigger repaint
                }
            });
        }

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
            int plotSize = Math.min(width, height) - 2 * margin;

            // Compute scaling factor
            StabilityVerification stability = new StabilityVerification(tf);
            StabilityVerification.Complex[] poles = stability.computePoles();
            StabilityVerification.Complex[] zeros = stability.computeZeros();
            double maxVal = 1.0; // Unit circle
            for (StabilityVerification.Complex p : poles) maxVal = Math.max(maxVal, p.magnitude());
            for (StabilityVerification.Complex z : zeros) maxVal = Math.max(maxVal, z.magnitude());
            maxVal = Math.max(maxVal, 1.2); // Ensure visibility

            // Draw grid
            g2.setColor(new Color(50, 50, 50));
            int centerX = margin + plotSize / 2;
            int centerY = margin + plotSize / 2;
            for (double i = -maxVal; i <= maxVal; i += maxVal / 4) {
                int x = centerX + (int) (i * plotSize / (2 * maxVal));
                int y = centerY - (int) (i * plotSize / (2 * maxVal));
                g2.drawLine(x, margin, x, margin + plotSize);
                g2.drawLine(margin, y, margin + plotSize, y);
            }

            // Draw axes
            g2.setColor(Color.LIGHT_GRAY);
            g2.drawLine(margin, centerY, margin + plotSize, centerY);
            g2.drawLine(centerX, margin, centerX, margin + plotSize);

            // Draw unit circle
            g2.drawOval(centerX - plotSize / 2, centerY - plotSize / 2, plotSize, plotSize);

            // Label axes
            g2.setColor(Color.WHITE);
            g2.drawString("Re", margin + plotSize, centerY + 15);
            g2.drawString("Im", centerX - 15, margin + 10);
            for (double i = -maxVal; i <= maxVal; i += maxVal / 2) {
                if (Math.abs(i) > 1e-10) {
                    int x = centerX + (int) (i * plotSize / (2 * maxVal));
                    int y = centerY - (int) (i * plotSize / (2 * maxVal));
                    g2.drawString(String.format("%.2f", i), x - 10, centerY + 15);
                    g2.drawString(String.format("%.2f", i), centerX - 25, y + 5);
                }
            }

            // Plot poles and zeros
            g2.setColor(Color.RED);
            for (StabilityVerification.Complex pole : poles) {
                int x = centerX + (int) (pole.getReal() * plotSize / (2 * maxVal));
                int y = centerY - (int) (pole.getImag() * plotSize / (2 * maxVal));
                g2.drawLine(x - 5, y - 5, x + 5, y + 5);
                g2.drawLine(x - 5, y + 5, x + 5, y - 5);
                g2.drawString(String.format("(%.2f, %.2f)", pole.getReal(), pole.getImag()), x + 5, y - 5);
            }

            g2.setColor(Color.GREEN);
            for (StabilityVerification.Complex zero : zeros) {
                int x = centerX + (int) (zero.getReal() * plotSize / (2 * maxVal));
                int y = centerY - (int) (zero.getImag() * plotSize / (2 * maxVal));
                g2.drawOval(x - 5, y - 5, 10, 10);
                g2.drawString(String.format("(%.2f, %.2f)", zero.getReal(), zero.getImag()), x + 5, y - 5);
            }

            // Draw legend
            g2.setColor(Color.WHITE);
            g2.drawString("Poles (X): Red", margin, margin + plotSize + 20);
            g2.setColor(Color.RED);
            g2.drawLine(margin + 70, margin + plotSize + 15, margin + 80, margin + plotSize + 25);
            g2.drawLine(margin + 70, margin + plotSize + 25, margin + 80, margin + plotSize + 15);
            g2.setColor(Color.WHITE);
            g2.drawString("Zeros (O): Green", margin + 100, margin + plotSize + 20);
            g2.setColor(Color.GREEN);
            g2.drawOval(margin + 170, margin + plotSize + 15, 10, 10);
        }

        private String getTooltipText(int mouseX, int mouseY) {
            if (tf == null) return null;
            int margin = 50;
            int plotSize = Math.min(getWidth(), getHeight()) - 2 * margin;
            int centerX = margin + plotSize / 2;
            int centerY = margin + plotSize / 2;
            double maxVal = 1.2;
            StabilityVerification stability = new StabilityVerification(tf);
            StabilityVerification.Complex[] poles = stability.computePoles();
            StabilityVerification.Complex[] zeros = stability.computeZeros();

            for (StabilityVerification.Complex pole : poles) {
                int x = centerX + (int) (pole.getReal() * plotSize / (2 * maxVal));
                int y = centerY - (int) (pole.getImag() * plotSize / (2 * maxVal));
                if (Math.abs(mouseX - x) < 10 && Math.abs(mouseY - y) < 10) {
                    return String.format("Pole: (%.4f, %.4fi)", pole.getReal(), pole.getImag());
                }
            }

            for (StabilityVerification.Complex zero : zeros) {
                int x = centerX + (int) (zero.getReal() * plotSize / (2 * maxVal));
                int y = centerY - (int) (zero.getImag() * plotSize / (2 * maxVal));
                if (Math.abs(mouseX - x) < 10 && Math.abs(mouseY - y) < 10) {
                    return String.format("Zero: (%.4f, %.4fi)", zero.getReal(), zero.getImag());
                }
            }
            return null;
        }
    }

    private class NyquistPanel extends JPanel {
        public NyquistPanel() {
            setBackground(new Color(30, 30, 30));
            setPreferredSize(new Dimension(450, 450));
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setFont(bahnschriftFont);

            int width = getWidth();
            int height = getHeight();
            int margin = 50;
            int plotSize = Math.min(width, height) - 2 * margin;

            // Compute Nyquist plot
            double[] real = new double[NUM_POINTS];
            double[] imag = new double[NUM_POINTS];
            double maxVal = 0;
            for (int i = 0; i < NUM_POINTS; i++) {
                double omega = -Math.PI + 2 * Math.PI * i / (NUM_POINTS - 1);
                double[] response = evaluateTransferFunction(omega);
                real[i] = response[0];
                imag[i] = response[1];
                maxVal = Math.max(maxVal, Math.sqrt(real[i] * real[i] + imag[i] * imag[i]));
            }
            maxVal = Math.max(maxVal, 1.0);

            // Draw grid
            g2.setColor(new Color(50, 50, 50));
            int centerX = margin + plotSize / 2;
            int centerY = margin + plotSize / 2;
            for (double i = -maxVal; i <= maxVal; i += maxVal / 4) {
                int x = centerX + (int) (i * plotSize / (2 * maxVal));
                int y = centerY - (int) (i * plotSize / (2 * maxVal));
                g2.drawLine(x, margin, x, margin + plotSize);
                g2.drawLine(margin, y, margin + plotSize, y);
            }

            // Draw axes
            g2.setColor(Color.LIGHT_GRAY);
            g2.drawLine(margin, centerY, margin + plotSize, centerY);
            g2.drawLine(centerX, margin, centerX, margin + plotSize);

            // Label axes
            g2.setColor(Color.WHITE);
            g2.drawString("Re", margin + plotSize, centerY + 15);
            g2.drawString("Im", centerX - 15, margin + 10);
            for (double i = -maxVal; i <= maxVal; i += maxVal / 2) {
                if (Math.abs(i) > 1e-10) {
                    int x = centerX + (int) (i * plotSize / (2 * maxVal));
                    int y = centerY - (int) (i * plotSize / (2 * maxVal));
                    g2.drawString(String.format("%.2f", i), x - 10, centerY + 15);
                    g2.drawString(String.format("%.2f", i), centerX - 25, y + 5);
                }
            }

            // Plot critical point (-1, 0)
            g2.setColor(Color.YELLOW);
            int xCrit = centerX + (int) (-1 * plotSize / (2 * maxVal));
            int yCrit = centerY;
            g2.fillOval(xCrit - 5, yCrit - 5, 10, 10);
            g2.drawString("(-1, 0)", xCrit + 5, yCrit - 5);

            // Plot Nyquist curve
            g2.setColor(new Color(0, 120, 215));
            for (int i = 1; i < NUM_POINTS; i++) {
                int x1 = centerX + (int) (real[i - 1] * plotSize / (2 * maxVal));
                int y1 = centerY - (int) (imag[i - 1] * plotSize / (2 * maxVal));
                int x2 = centerX + (int) (real[i] * plotSize / (2 * maxVal));
                int y2 = centerY - (int) (imag[i] * plotSize / (2 * maxVal));
                g2.drawLine(x1, y1, x2, y2);
            }

            // Draw legend
            g2.setColor(Color.WHITE);
            g2.drawString("Nyquist Curve: Blue", margin, margin + plotSize + 20);
            g2.setColor(new Color(0, 120, 215));
            g2.drawLine(margin + 90, margin + plotSize + 15, margin + 110, margin + plotSize + 15);
            g2.setColor(Color.WHITE);
            g2.drawString("Critical Point: Yellow", margin + 120, margin + plotSize + 20);
            g2.setColor(Color.YELLOW);
            g2.fillOval(margin + 220, margin + plotSize + 10, 10, 10);
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
            if (Math.abs(denom) < 1e-10) return new double[]{0, 0};
            return new double[]{
                    (realNum * realDen + imagNum * imagDen) / denom,
                    (imagNum * realDen - realNum * imagDen) / denom
            };
        }
    }

    private static final double EPSILON = 1e-10;
}