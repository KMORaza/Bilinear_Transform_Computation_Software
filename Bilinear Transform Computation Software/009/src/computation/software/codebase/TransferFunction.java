package computation.software.codebase;

public class TransferFunction {
    private final double[] numerator;
    private final double[] denominator;

    public TransferFunction(double[] numerator, double[] denominator) {
        if (numerator == null || numerator.length == 0 || denominator == null || denominator.length == 0) {
            throw new IllegalArgumentException("Numerator and denominator must be non-empty");
        }
        this.numerator = normalize(numerator);
        this.denominator = normalize(denominator);
    }

    public double[] getNumerator() {
        return numerator.clone();
    }

    public double[] getDenominator() {
        return denominator.clone();
    }

    private double[] normalize(double[] coeffs) {
        // Remove leading zeros
        int start = 0;
        while (start < coeffs.length - 1 && Math.abs(coeffs[start]) < 1e-10) {
            start++;
        }
        double[] result = new double[coeffs.length - start];
        System.arraycopy(coeffs, start, result, 0, result.length);
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("H(z) = ");
        appendPolynomial(sb, numerator, true);
        sb.append("\n").append("       ");
        for (int i = 0; i < numerator.length; i++) sb.append("-");
        sb.append("\n       ");
        appendPolynomial(sb, denominator, false);
        return sb.toString();
    }

    private void appendPolynomial(StringBuilder sb, double[] coeffs, boolean isNumerator) {
        boolean first = true;
        for (int i = 0; i < coeffs.length; i++) {
            double coeff = coeffs[i];
            if (Math.abs(coeff) < 1e-10) continue;
            if (!first && coeff > 0) sb.append(" + ");
            else if (!first) sb.append(" - ");
            else if (coeff < 0) sb.append("-");
            if (Math.abs(Math.abs(coeff) - 1) > 1e-10 || coeffs.length - 1 - i == 0) {
                sb.append(String.format("%.4f", Math.abs(coeff)));
            }
            int power = coeffs.length - 1 - i;
            if (power > 0) {
                sb.append("z");
                if (power > 1) sb.append("^").append(power);
            }
            first = false;
        }
        if (first) sb.append("0");
    }
}