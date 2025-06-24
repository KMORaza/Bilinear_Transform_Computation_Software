package computation.software.codebase;

import java.util.ArrayList;
import java.util.List;

public class StabilityVerification {
    private final SymbolicTransferFunction tf;
    private static final double EPSILON = 1e-10;
    private static final int MAX_ITERATIONS = 100;

    public StabilityVerification(SymbolicTransferFunction tf) {
        this.tf = tf;
    }

    public boolean isStable() {
        Complex[] poles = computePoles();
        for (Complex pole : poles) {
            if (pole.magnitude() >= 1.0 - EPSILON) {
                return false;
            }
        }
        return true;
    }

    public Complex[] computePoles() {
        double[] den = tf.getDenominator();
        return findRoots(den);
    }

    public Complex[] computeZeros() {
        double[] num = tf.getNumerator();
        return findRoots(num);
    }

    private Complex[] findRoots(double[] coeffs) {
        // Normalize coefficients and validate
        List<Double> normalized = new ArrayList<>();
        for (double coeff : coeffs) {
            if (Math.abs(coeff) > EPSILON || normalized.isEmpty()) {
                normalized.add(coeff);
            }
        }
        if (normalized.isEmpty() || normalized.size() == 1 && Math.abs(normalized.get(0)) < EPSILON) {
            return new Complex[0]; // Degenerate case: constant zero polynomial
        }

        double[] poly = new double[normalized.size()];
        for (int i = 0; i < normalized.size(); i++) {
            poly[i] = normalized.get(i);
        }

        // Use Laguerre's method to find all roots
        List<Complex> roots = new ArrayList<>();
        double[] workingPoly = poly.clone();
        int degree = workingPoly.length - 1;

        while (degree > 0) {
            Complex root = laguerreMethod(workingPoly, new Complex(Math.random() * 0.1, Math.random() * 0.1)); // Random initial guess
            if (root != null) {
                roots.add(root);
                workingPoly = deflatePolynomial(workingPoly, root);
                degree--;
                // Re-normalize working polynomial to avoid numerical drift
                List<Double> temp = new ArrayList<>();
                for (double coeff : workingPoly) {
                    if (Math.abs(coeff) > EPSILON || temp.isEmpty()) {
                        temp.add(coeff);
                    }
                }
                workingPoly = temp.stream().mapToDouble(Double::doubleValue).toArray();
                degree = workingPoly.length - 1;
            } else {
                break; // Numerical issues, stop iteration
            }
        }

        return roots.toArray(new Complex[0]);
    }

    private Complex laguerreMethod(double[] poly, Complex x) {
        int degree = poly.length - 1;
        for (int iter = 0; iter < MAX_ITERATIONS; iter++) {
            Complex p = evaluatePolynomial(poly, x);
            if (p.magnitude() < EPSILON) {
                return x;
            }

            // Compute first and second derivatives
            Complex dp = evaluateDerivative(poly, x);
            Complex ddp = evaluateSecondDerivative(poly, x);

            // Avoid division if p is too small
            if (p.magnitude() < EPSILON) {
                return x; // Approximate root found
            }

            // Compute G = dp/p
            Complex G = dp.divideSafe(p);
            if (G == null) return null; // Numerical instability

            // Compute H = G^2 - (ddp/p)
            Complex ddpOverP = ddp.divideSafe(p);
            if (ddpOverP == null) return null;
            Complex H = G.multiply(G).subtract(ddpOverP);

            // Compute denominator terms
            Complex n = new Complex(degree, 0);
            Complex sqrtTerm = (n.subtract(new Complex(1, 0))).multiply(H).subtract(G.multiply(G)).sqrt();
            Complex denom1 = G.add(sqrtTerm);
            Complex denom2 = G.subtract(sqrtTerm);

            // Choose the larger denominator
            Complex a = denom1.magnitude() > denom2.magnitude() ? denom1 : denom2;
            if (a.magnitude() < EPSILON) {
                return null; // Avoid division by zero
            }

            // Update x
            Complex correction = n.divideSafe(a);
            if (correction == null) return null;
            x = x.subtract(correction);

            if (correction.magnitude() < EPSILON) {
                return x;
            }
        }
        return null; // Did not converge
    }

    private double[] deflatePolynomial(double[] poly, Complex root) {
        int degree = poly.length - 1;
        double[] result = new double[degree];
        result[0] = poly[0];
        for (int i = 1; i < degree; i++) {
            result[i] = poly[i] + result[i - 1] * root.getReal();
            if (Math.abs(root.getImag()) > EPSILON) {
                result[i] += result[i - 1] * root.getImag();
            }
        }
        return result;
    }

    private Complex evaluatePolynomial(double[] poly, Complex x) {
        Complex result = new Complex(poly[0], 0);
        for (int i = 1; i < poly.length; i++) {
            result = result.multiply(x).add(new Complex(poly[i], 0));
        }
        return result;
    }

    private Complex evaluateDerivative(double[] poly, Complex x) {
        int degree = poly.length - 1;
        Complex result = new Complex(degree * poly[0], 0);
        for (int i = 1; i < degree; i++) {
            result = result.multiply(x).add(new Complex((degree - i) * poly[i], 0));
        }
        return result;
    }

    private Complex evaluateSecondDerivative(double[] poly, Complex x) {
        int degree = poly.length - 1;
        if (degree < 2) return new Complex(0, 0);
        Complex result = new Complex(degree * (degree - 1) * poly[0], 0);
        for (int i = 1; i < degree - 1; i++) {
            result = result.multiply(x).add(new Complex((degree - i) * (degree - i - 1) * poly[i], 0));
        }
        return result;
    }

    public static class Complex {
        private final double real;
        private final double imag;

        public Complex(double real, double imag) {
            this.real = real;
            this.imag = imag;
        }

        public double getReal() {
            return real;
        }

        public double getImag() {
            return imag;
        }

        public double magnitude() {
            return Math.sqrt(real * real + imag * imag);
        }

        public Complex add(Complex other) {
            return new Complex(real + other.real, imag + other.imag);
        }

        public Complex subtract(Complex other) {
            return new Complex(real - other.real, imag - other.imag);
        }

        public Complex multiply(Complex other) {
            return new Complex(real * other.real - imag * other.imag, real * other.imag + imag * other.real);
        }

        public Complex divide(Complex other) {
            double denom = other.real * other.real + other.imag * other.imag;
            if (Math.abs(denom) < EPSILON) {
                throw new ArithmeticException("Division by zero");
            }
            double newReal = (real * other.real + imag * other.imag) / denom;
            double newImag = (imag * other.real - real * other.imag) / denom;
            return new Complex(newReal, newImag);
        }

        public Complex divideSafe(Complex other) {
            double denom = other.real * other.real + other.imag * other.imag;
            if (Math.abs(denom) < EPSILON) {
                return null; // Indicate numerical instability
            }
            double newReal = (real * other.real + imag * other.imag) / denom;
            double newImag = (imag * other.real - real * other.imag) / denom;
            return new Complex(newReal, newImag);
        }

        public Complex sqrt() {
            double r = Math.sqrt(magnitude());
            double theta = Math.atan2(imag, real) / 2;
            return new Complex(r * Math.cos(theta), r * Math.sin(theta));
        }
    }
}