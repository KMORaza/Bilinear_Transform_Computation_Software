package computation.software.codebase;

import javax.swing.*;
import java.awt.*;
import java.util.Arrays;
import javax.swing.border.TitledBorder;

public class DirectBilinearMappingEngine extends JFrame {
    private final SymbolicTransferFunction analogTf;
    private final double T;
    private final int precision;
    private final Font bahnschriftFont = new Font("Arial", Font.PLAIN, 12);
    private JTextArea outputArea;

    public DirectBilinearMappingEngine(SymbolicTransferFunction tf, double T, int precision) {
        this.analogTf = tf;
        this.T = T;
        this.precision = precision;
        setTitle("Direct Bilinear Mapping Results");
        setSize(600, 400);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));
        getContentPane().setBackground(new Color(30, 30, 30));

        outputArea = new JTextArea(15, 50);
        outputArea.setEditable(false);
        outputArea.setFont(bahnschriftFont);
        outputArea.setForeground(Color.WHITE);
        outputArea.setBackground(new Color(50, 50, 50));
        JScrollPane outputScroll = new JScrollPane(outputArea);
        outputScroll.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY), "Bilinear Transformation Results", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, bahnschriftFont, Color.WHITE));
        outputScroll.setBackground(new Color(30, 30, 30));

        add(outputScroll, BorderLayout.CENTER);

        computeAndDisplayTransformation();
    }

    private void computeAndDisplayTransformation() {
        if (analogTf == null) {
            outputArea.setText("No analog transfer function provided!");
            return;
        }

        // Perform bilinear transformation: s = (2/T) * (z-1)/(z+1)
        double[] numS = analogTf.getNumerator();
        double[] denS = analogTf.getDenominator();

        // Initialize result polynomials for H(z)
        int numDegree = numS.length - 1;
        int denDegree = denS.length - 1;
        int maxDegree = Math.max(numDegree, denDegree);
        double[] numZ = new double[maxDegree + 1];
        double[] denZ = new double[maxDegree + 1];

        // Compute (z+1)^maxDegree and (z-1)^maxDegree
        double[] zPlus1 = new double[maxDegree + 1];
        double[] zMinus1 = new double[maxDegree + 1];
        zPlus1[0] = 1; // (z+1)^0 = 1
        zMinus1[0] = 1; // (z-1)^0 = 1
        for (int i = 1; i <= maxDegree; i++) {
            // Expand (z+1)^i = (z+1)^(i-1) * (z+1)
            double[] prev = zPlus1.clone();
            zPlus1 = new double[i + 1];
            for (int j = 0; j < i; j++) {
                zPlus1[j] += prev[j]; // Coefficient of z^j
                zPlus1[j + 1] += prev[j]; // Coefficient of z^(j+1)
            }
            zPlus1[0] += prev[0]; // Constant term
            // Expand (z-1)^i = (z-1)^(i-1) * (z-1)
            prev = zMinus1.clone();
            zMinus1 = new double[i + 1];
            for (int j = 0; j < i; j++) {
                zMinus1[j] += prev[j]; // Coefficient of z^j
                zMinus1[j + 1] -= prev[j]; // Coefficient of z^(j+1)
            }
            zMinus1[0] += prev[0]; // Constant term
        }

        // Compute numerator and denominator of H(z)
        for (int i = 0; i <= numDegree; i++) {
            double coeff = numS[numS.length - 1 - i] * Math.pow(2.0 / T, i);
            // Multiply by (z-1)^i * (z+1)^(maxDegree-i)
            double[] term = multiplyPolynomials(powerPolynomial(zMinus1, i), powerPolynomial(zPlus1, maxDegree - i));
            for (int j = 0; j < term.length; j++) {
                numZ[j] += coeff * term[j];
            }
        }
        for (int i = 0; i <= denDegree; i++) {
            double coeff = denS[denS.length - 1 - i] * Math.pow(2.0 / T, i);
            // Multiply by (z-1)^i * (z+1)^(maxDegree-i)
            double[] term = multiplyPolynomials(powerPolynomial(zMinus1, i), powerPolynomial(zPlus1, maxDegree - i));
            for (int j = 0; j < term.length; j++) {
                denZ[j] += coeff * term[j];
            }
        }

        // Normalize to avoid leading zeros
        numZ = normalizePolynomial(numZ);
        denZ = normalizePolynomial(denZ);

        // Create discrete transfer function
        SymbolicTransferFunction discreteTf = new SymbolicTransferFunction(numZ, denZ, "z");

        // Format output
        String format = "%." + precision + "f";
        StringBuilder output = new StringBuilder();
        output.append("Analog Transfer Function H(s):\n").append(analogTf.toString()).append("\n\n");
        output.append("Discrete Transfer Function H(z):\n").append(discreteTf.toString()).append("\n\n");
        output.append("Numeric Coefficients (with ").append(precision).append(" decimal places):\n");
        output.append("Numerator: [");
        for (int i = 0; i < numZ.length; i++) {
            output.append(String.format(format, numZ[i]));
            if (i < numZ.length - 1) output.append(", ");
        }
        output.append("]\n");
        output.append("Denominator: [");
        for (int i = 0; i < denZ.length; i++) {
            output.append(String.format(format, denZ[i]));
            if (i < denZ.length - 1) output.append(", ");
        }
        output.append("]\n");
        outputArea.setText(output.toString());
    }

    private double[] multiplyPolynomials(double[] p1, double[] p2) {
        double[] result = new double[p1.length + p2.length - 1];
        for (int i = 0; i < p1.length; i++) {
            for (int j = 0; j < p2.length; j++) {
                result[i + j] += p1[i] * p2[j];
            }
        }
        return result;
    }

    private double[] powerPolynomial(double[] poly, int power) {
        if (power == 0) return new double[]{1.0};
        double[] result = poly.clone();
        for (int i = 1; i < power; i++) {
            result = multiplyPolynomials(result, poly);
        }
        return result;
    }

    private double[] normalizePolynomial(double[] poly) {
        int leadingNonZero = 0;
        for (int i = poly.length - 1; i >= 0; i--) {
            if (Math.abs(poly[i]) > 1e-10) {
                leadingNonZero = i + 1;
                break;
            }
        }
        return Arrays.copyOf(poly, leadingNonZero);
    }
}