package computation.software.codebase;

public class PreWarpingCapability {
    private final double T; // Sampling period

    public PreWarpingCapability(double T) {
        if (T <= 0) throw new IllegalArgumentException("Sampling period must be positive");
        this.T = T;
    }

    /**
     * Computes the pre-warped analog frequency using the formula: ω_a = (2/T) * tan(ω_d * T / 2)
     * @param omega_d Digital frequency (rad/sample)
     * @return Pre-warped analog frequency (rad/s)
     */
    public double computePreWarpedFrequency(double omega_d) {
        if (omega_d < 0) throw new IllegalArgumentException("Digital frequency must be non-negative");
        return (2.0 / T) * Math.tan(omega_d * T / 2.0);
    }

    /**
     * Applies pre-warping to the analog transfer function by scaling coefficients
     * to adjust for the pre-warped frequency.
     * @param analogTf Original analog transfer function
     * @param omega_d Digital frequency (rad/sample)
     * @param omega_a Pre-warped analog frequency (rad/s)
     * @return Pre-warped analog transfer function
     */
    public SymbolicTransferFunction applyPreWarping(SymbolicTransferFunction analogTf, double omega_d, double omega_a) {
        if (omega_d <= 0 || omega_a <= 0) throw new IllegalArgumentException("Frequencies must be positive");

        // Compute scaling factor: ω_a / ω_d
        double scale = omega_a / omega_d;

        // Scale coefficients of numerator and denominator
        double[] num = analogTf.getNumerator();
        double[] den = analogTf.getDenominator();
        double[] newNum = new double[num.length];
        double[] newDen = new double[den.length];

        // Scale numerator: each coefficient is multiplied by (1/scale)^k where k is the power
        for (int i = 0; i < num.length; i++) {
            int power = num.length - 1 - i;
            newNum[i] = num[i] * Math.pow(1.0 / scale, power);
        }

        // Scale denominator: each coefficient is multiplied by (1/scale)^k where k is the power
        for (int i = 0; i < den.length; i++) {
            int power = den.length - 1 - i;
            newDen[i] = den[i] * Math.pow(1.0 / scale, power);
        }

        return new SymbolicTransferFunction(newNum, newDen, "s");
    }
}