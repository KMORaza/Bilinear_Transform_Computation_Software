package computation.software.codebase;

import java.util.Arrays;

public class ADFilterMapping {
    private final double T; // Sampling period
    private static final double EPSILON = 1e-10;

    public ADFilterMapping(double T) {
        if (T <= 0) throw new IllegalArgumentException("Sampling period must be positive");
        this.T = T;
    }

    public enum FilterType {
        BUTTERWORTH, CHEBYSHEV_I, CHEBYSHEV_II, ELLIPTIC, BESSEL
    }

    public SymbolicTransferFunction designFilter(FilterType type, int order, double cutoffFreq, double ripple, double stopbandAtten) {
        if (order < 1) throw new IllegalArgumentException("Filter order must be positive");
        if (cutoffFreq <= 0) throw new IllegalArgumentException("Cutoff frequency must be positive");
        if (type == FilterType.CHEBYSHEV_I || type == FilterType.CHEBYSHEV_II || type == FilterType.ELLIPTIC) {
            if (ripple <= 0) throw new IllegalArgumentException("Passband ripple must be positive for Chebyshev/Elliptic");
        }
        if (type == FilterType.CHEBYSHEV_II || type == FilterType.ELLIPTIC) {
            if (stopbandAtten <= 0) throw new IllegalArgumentException("Stopband attenuation must be positive for Chebyshev II/Elliptic");
        }

        double[] num, den;
        switch (type) {
            case BUTTERWORTH:
                den = butterworthPoles(order, cutoffFreq);
                num = new double[]{Math.pow(cutoffFreq, order)}; // Unity gain
                break;
            case CHEBYSHEV_I:
                den = chebyshevIPoles(order, cutoffFreq, ripple);
                num = new double[]{chebyshevIGain(order, ripple)};
                break;
            case CHEBYSHEV_II:
                double[] polesII = chebyshevIIPoles(order, cutoffFreq, stopbandAtten);
                num = chebyshevIINumerator(order, cutoffFreq, stopbandAtten);
                den = polesII;
                break;
            case ELLIPTIC:
                double[] ellipticPoles = ellipticPoles(order, cutoffFreq, ripple, stopbandAtten);
                num = ellipticNumerator(order, cutoffFreq, ripple, stopbandAtten);
                den = ellipticPoles;
                break;
            case BESSEL:
                den = besselPoles(order, cutoffFreq);
                num = new double[]{Math.pow(cutoffFreq, order)}; // Unity gain
                break;
            default:
                throw new IllegalArgumentException("Unsupported filter type");
        }

        SymbolicTransferFunction analogTf = new SymbolicTransferFunction(num, den, "s");
        BilinearTransform bt = new BilinearTransform(T);
        return bt.apply(analogTf);
    }

    private double[] butterworthPoles(int order, double cutoffFreq) {
        double[] den = new double[order + 1];
        den[0] = 1.0; // Leading coefficient
        for (int k = 1; k <= order; k++) {
            double theta = Math.PI * (2 * k - 1) / (2 * order);
            double poleReal = -cutoffFreq * Math.sin(theta);
            double poleImag = cutoffFreq * Math.cos(theta);
            // For simplicity, assume real poles or conjugate pairs combine into real coefficients
            den[order - k] = (k % 2 == 0) ? (poleReal * poleReal + poleImag * poleImag) : -2 * poleReal;
        }
        return den;
    }

    private double[] chebyshevIPoles(int order, double cutoffFreq, double ripple) {
        double epsilon = Math.sqrt(Math.pow(10, ripple / 10) - 1);
        double v = (1.0 / order) * Math.log(1 / epsilon + Math.sqrt(1 / (epsilon * epsilon) + 1));
        double[] den = new double[order + 1];
        den[0] = 1.0;
        for (int k = 1; k <= order; k++) {
            double theta = Math.PI * (2 * k - 1) / (2 * order);
            double poleReal = -cutoffFreq * Math.sinh(v) * Math.sin(theta);
            double poleImag = cutoffFreq * Math.cosh(v) * Math.cos(theta);
            den[order - k] = (k % 2 == 0) ? (poleReal * poleReal + poleImag * poleImag) : -2 * poleReal;
        }
        return den;
    }

    private double chebyshevIGain(int order, double ripple) {
        double epsilon = Math.sqrt(Math.pow(10, ripple / 10) - 1);
        return Math.pow(10, -ripple / 20) / (order % 2 == 0 ? 1 : Math.sqrt(1 + epsilon * epsilon));
    }

    private double[] chebyshevIIPoles(int order, double cutoffFreq, double stopbandAtten) {
        double epsilon = 1 / Math.sqrt(Math.pow(10, stopbandAtten / 10) - 1);
        double v = (1.0 / order) * Math.log(1 / epsilon + Math.sqrt(1 / (epsilon * epsilon) + 1));
        double[] den = new double[order + 1];
        den[0] = 1.0;
        for (int k = 1; k <= order; k++) {
            double theta = Math.PI * (2 * k - 1) / (2 * order);
            double poleReal = -cutoffFreq / Math.sinh(v) * Math.sin(theta);
            double poleImag = cutoffFreq / Math.cosh(v) * Math.cos(theta);
            den[order - k] = (k % 2 == 0) ? (poleReal * poleReal + poleImag * poleImag) : -2 * poleReal;
        }
        return den;
    }

    private double[] chebyshevIINumerator(int order, double cutoffFreq, double stopbandAtten) {
        double[] num = new double[order + 1];
        num[order] = 1.0 / Math.sqrt(Math.pow(10, stopbandAtten / 10));
        return num;
    }

    private double[] ellipticPoles(int order, double cutoffFreq, double ripple, double stopbandAtten) {
        // Simplified elliptic filter pole placement (approximation)
        double epsilon = Math.sqrt(Math.pow(10, ripple / 10) - 1);
        double xi = 1 / Math.sqrt(Math.pow(10, stopbandAtten / 10) - 1);
        double[] den = new double[order + 1];
        den[0] = 1.0;
        for (int k = 1; k <= order; k++) {
            double theta = Math.PI * (2 * k - 1) / (2 * order);
            double poleReal = -cutoffFreq * epsilon * Math.sin(theta);
            double poleImag = cutoffFreq * xi * Math.cos(theta);
            den[order - k] = (k % 2 == 0) ? (poleReal * poleReal + poleImag * poleImag) : -2 * poleReal;
        }
        return den;
    }

    private double[] ellipticNumerator(int order, double cutoffFreq, double ripple, double stopbandAtten) {
        double[] num = new double[order + 1];
        num[order] = Math.pow(10, -ripple / 20);
        // Add zeros for elliptic filter (simplified)
        for (int k = 1; k < order; k++) {
            num[k] = 0.0; // Zeros at infinity or computed via elliptic functions
        }
        return num;
    }

    private double[] besselPoles(int order, double cutoffFreq) {
        // Simplified Bessel filter (using Bessel polynomial coefficients)
        double[] den = new double[order + 1];
        for (int k = 0; k <= order; k++) {
            den[k] = besselCoefficient(order, k);
        }
        // Scale to cutoff frequency
        for (int i = 0; i < den.length; i++) {
            den[i] *= Math.pow(cutoffFreq, den.length - 1 - i);
        }
        return den;
    }

    private double besselCoefficient(int n, int k) {
        // Compute Bessel polynomial coefficient: (2n-k)! / (2^(n-k) * k! * (n-k)!)
        long numerator = factorial(2 * n - k);
        long denominator = (long) Math.pow(2, n - k) * factorial(k) * factorial(n - k);
        return (double) numerator / denominator;
    }

    private long factorial(int n) {
        if (n < 0) return 0;
        long result = 1;
        for (int i = 1; i <= n; i++) {
            result *= i;
        }
        return result;
    }
}