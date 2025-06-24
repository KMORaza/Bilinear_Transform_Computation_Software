package computation.software.codebase;

import javax.swing.*;
import java.awt.*;

public class FrequencyResponsePanel extends JPanel {
    private SymbolicTransferFunction tf;
    private static final int NUM_POINTS = 512;
    private final Font bahnschriftFont = new Font("Bahnschrift", Font.PLAIN, 12);

    public FrequencyResponsePanel() {
        setPreferredSize(new Dimension(600, 300));
        setBackground(new Color(30, 30, 30));
    }

    public void setTransferFunction(SymbolicTransferFunction tf) {
        this.tf = tf;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setFont(bahnschriftFont);
        g2.setColor(Color.WHITE);

        if (tf == null) {
            g2.drawString("No data to display", getWidth() / 2 - 50, getHeight() / 2);
            return;
        }

        // Compute frequency response
        double[] freq = new double[NUM_POINTS];
        double[] mag = new double[NUM_POINTS];
        double maxMag = 0;
        for (int i = 0; i < NUM_POINTS; i++) {
            double omega = Math.PI * i / (NUM_POINTS - 1); // 0 to pi
            freq[i] = omega / Math.PI; // Normalized frequency
            double[] response = evaluateTransferFunction(omega);
            mag[i] = 20 * Math.log10(Math.sqrt(response[0] * response[0] + response[1] * response[1]));
            if (!Double.isNaN(mag[i]) && !Double.isInfinite(mag[i])) {
                maxMag = Math.max(maxMag, Math.abs(mag[i]));
            }
        }
        maxMag = Math.max(maxMag, 1e-6); // Avoid division by zero

        // Draw axes
        int margin = 50;
        int width = getWidth() - 2 * margin;
        int height = getHeight() - 2 * margin;
        g2.setColor(Color.LIGHT_GRAY);
        g2.drawLine(margin, getHeight() - margin, margin + width, getHeight() - margin); // X-axis
        g2.drawLine(margin, margin, margin, getHeight() - margin); // Y-axis
        g2.setColor(Color.WHITE);
        g2.drawString("Frequency (π rad/sample)", margin + width / 2, getHeight() - margin + 20);
        g2.drawString("Magnitude (dB)", margin - 40, margin - 10);

        // Draw ticks
        for (int i = 0; i <= 4; i++) {
            int x = margin + i * width / 4;
            g2.setColor(Color.LIGHT_GRAY);
            g2.drawLine(x, getHeight() - margin, x, getHeight() - margin + 5);
            g2.setColor(Color.WHITE);
            g2.drawString(String.format("%.2f", i * 0.25), x - 10, getHeight() - margin + 20);
        }
        for (int i = -2; i <= 2; i++) {
            int y = getHeight() - margin - i * height / 4;
            g2.setColor(Color.LIGHT_GRAY);
            g2.drawLine(margin - 5, y, margin, y);
            g2.setColor(Color.WHITE);
            g2.drawString(String.format("%d", i * (int)(maxMag / 2)), margin - 30, y + 5);
        }

        // Plot magnitude response
        g2.setColor(new Color(0, 120, 215));
        for (int i = 1; i < NUM_POINTS; i++) {
            if (Double.isNaN(mag[i]) || Double.isInfinite(mag[i])) continue;
            int x1 = margin + (i - 1) * width / (NUM_POINTS - 1);
            int x2 = margin + i * width / (NUM_POINTS - 1);
            int y1 = getHeight() - margin - (int)((mag[i - 1] + maxMag) * height / (2 * maxMag));
            int y2 = getHeight() - margin - (int)((mag[i] + maxMag) * height / (2 * maxMag));
            g2.drawLine(x1, y1, x2, y2);
        }
    }

    private double[] evaluateTransferFunction(double omega) {
        double[] num = tf.getNumerator();
        double[] den = tf.getDenominator();
        double realNum = 0, imagNum = 0, realDen = 0, imagDen = 0;

        // Evaluate numerator
        for (int i = 0; i < num.length; i++) {
            double angle = -i * omega;
            realNum += num[num.length - 1 - i] * Math.cos(angle);
            imagNum += num[num.length - 1 - i] * Math.sin(angle);
        }

        // Evaluate denominator
        for (int i = 0; i < den.length; i++) {
            double angle = -i * omega;
            realDen += den[den.length - 1 - i] * Math.cos(angle);
            imagDen += den[den.length - 1 - i] * Math.sin(angle);
        }

        // Compute magnitude: (realNum + j*imagNum) / (realDen + j*imagDen)
        double denom = realDen * realDen + imagDen * imagDen;
        if (Math.abs(denom) < 1e-10) return new double[]{0, 0};
        return new double[]{
                (realNum * realDen + imagNum * imagDen) / denom,
                (imagNum * realDen - realNum * imagDen) / denom
        };
    }
}