package computation.software.codebase;

public class BilinearTransform {
    private final double T; // Sampling period

    public BilinearTransform(double T) {
        if (T <= 0) throw new IllegalArgumentException("Sampling period must be positive");
        this.T = T;
    }

    public SymbolicTransferFunction apply(SymbolicTransferFunction analogTf) {
        // Bilinear transform: s = (2/T) * (z-1)/(z+1)
        int numDegree = analogTf.getNumerator().length - 1;
        int denDegree = analogTf.getDenominator().length - 1;

        // Resulting polynomial degrees
        int maxDegree = Math.max(numDegree, denDegree);
        double[] newNum = new double[maxDegree + 1];
        double[] newDen = new double[maxDegree + 1];

        // Compute coefficients for (z+1)^k and (z-1)^k
        for (int k = 0; k <= maxDegree; k++) {
            double numCoeff = 0, denCoeff = 0;
            for (int i = 0; i <= k; i++) {
                // Numerator contribution
                if (i <= numDegree) {
                    numCoeff += analogTf.getNumerator()[numDegree - i] *
                            binomial(k, i) * Math.pow(2 / T, i) * Math.pow(-1, k - i);
                }
                // Denominator contribution
                if (i <= denDegree) {
                    denCoeff += analogTf.getDenominator()[denDegree - i] *
                            binomial(k, i) * Math.pow(2 / T, i) * Math.pow(-1, k - i);
                }
            }
            newNum[maxDegree - k] = numCoeff;
            newDen[maxDegree - k] = denCoeff;
        }

        return new SymbolicTransferFunction(newNum, newDen, "z");
    }

    private long binomial(int n, int k) {
        if (k < 0 || k > n) return 0;
        long result = 1;
        for (int i = 1; i <= k; i++) {
            result *= (n - i + 1);
            result /= i;
        }
        return result;
    }
}