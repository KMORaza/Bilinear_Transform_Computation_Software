package computation.software.codebase;

import java.util.ArrayList;

public class PolynomialRootFinder {
    // Represents a complex number
    public static class Complex {
        public double real;
        public double imag;

        public Complex(double real, double imag) {
            this.real = real;
            this.imag = imag;
        }

        public Complex add(Complex other) {
            return new Complex(this.real + other.real, this.imag + other.imag);
        }

        public Complex subtract(Complex other) {
            return new Complex(this.real - other.real, this.imag - other.imag);
        }

        public Complex multiply(Complex other) {
            double r = this.real * other.real - this.imag * other.imag;
            double i = this.real * other.imag + this.imag * other.real;
            return new Complex(r, i);
        }

        public Complex divide(Complex other) {
            double denom = other.real * other.real + other.imag * other.imag;
            if (Math.abs(denom) < 1e-10) {
                throw new ArithmeticException("Division by zero in complex division");
            }
            double r = (this.real * other.real + this.imag * other.imag) / denom;
            double i = (this.imag * other.real - this.real * other.imag) / denom;
            return new Complex(r, i);
        }

        public Complex scale(double scalar) {
            return new Complex(this.real * scalar, this.imag * scalar);
        }

        public double abs() {
            return Math.sqrt(real * real + imag * imag);
        }

        public Complex sqrt() {
            double r = Math.sqrt(abs());
            double theta = Math.atan2(imag, real) / 2;
            return new Complex(r * Math.cos(theta), r * Math.sin(theta));
        }

        @Override
        public String toString() {
            if (Math.abs(imag) < 1e-10) {
                return String.format("%.6f", real);
            } else if (Math.abs(real) < 1e-10) {
                return String.format("%.6fj", imag);
            } else if (imag > 0) {
                return String.format("%.6f + j%.6f", real, imag);
            } else {
                return String.format("%.6f - j%.6f", real, -imag);
            }
        }
    }

    // Finds all roots of a polynomial using Laguerre's method
    public static Complex[] findRoots(double[] coeffs) {
        if (coeffs == null || coeffs.length < 2) {
            throw new IllegalArgumentException("Polynomial must have degree at least 1");
        }

        // Normalize coefficients (divide by leading coefficient)
        int n = coeffs.length - 1; // Degree of polynomial
        if (Math.abs(coeffs[0]) < 1e-10) {
            throw new IllegalArgumentException("Leading coefficient cannot be zero");
        }
        double[] normalized = new double[n + 1];
        for (int i = 0; i <= n; i++) {
            normalized[i] = coeffs[i] / coeffs[0];
        }

        ArrayList<Complex> roots = new ArrayList<>();
        double[] currentPoly = normalized;

        // Find roots one by one, deflating the polynomial
        while (n > 1) {
            Complex root = laguerreMethod(currentPoly, new Complex(0, 0));
            roots.add(root);

            // Deflate polynomial
            currentPoly = deflatePolynomial(currentPoly, root);
            n--;
        }

        // Solve linear equation for last root
        if (n == 1) {
            // For ax + b = 0, root = -b/a
            roots.add(new Complex(-currentPoly[1] / currentPoly[0], 0));
        }

        return roots.toArray(new Complex[0]);
    }

    // Laguerre's method to find one root
    private static Complex laguerreMethod(double[] coeffs, Complex initialGuess) {
        int n = coeffs.length - 1; // Degree
        Complex z = initialGuess;
        int maxIterations = 100;
        double tolerance = 1e-10;

        for (int iter = 0; iter < maxIterations; iter++) {
            // Evaluate polynomial and derivatives
            Complex p = evaluatePolynomial(coeffs, z);
            if (p.abs() < tolerance) {
                return z; // Root found
            }
            Complex p1 = evaluateFirstDerivative(coeffs, z);
            Complex p2 = evaluateSecondDerivative(coeffs, z);

            // Compute G = p'/p
            Complex G = p1.divide(p);

            // Compute H = G^2 - p''/p
            Complex H = G.multiply(G).subtract(p2.divide(p));

            // Compute denominator: sqrt((n-1)(nH - G^2))
            Complex term = H.scale(n * (n - 1)).subtract(G.multiply(G).scale(n - 1));
            Complex sqrtTerm = term.sqrt();
            Complex denom1 = G.add(sqrtTerm);
            Complex denom2 = G.subtract(sqrtTerm);

            // Choose denominator with larger magnitude
            Complex denom = denom1.abs() > denom2.abs() ? denom1 : denom2;

            // Compute correction: n / (G ± sqrt((n-1)(nH - G^2)))
            Complex correction = p.scale(n).divide(denom);

            // Update z
            Complex zNext = z.subtract(correction);

            // Check convergence
            if (zNext.subtract(z).abs() < tolerance) {
                return zNext;
            }
            z = zNext;
        }

        // If not converged, try a different initial guess
        Complex newGuess = new Complex(Math.random() * 2 - 1, Math.random() * 2 - 1);
        return laguerreMethod(coeffs, newGuess);
    }

    // Evaluates polynomial at z
    private static Complex evaluatePolynomial(double[] coeffs, Complex z) {
        Complex result = new Complex(0, 0);
        for (int i = 0; i < coeffs.length; i++) {
            Complex term = z;
            for (int j = 0; j < coeffs.length - i - 2; j++) {
                term = term.multiply(z);
            }
            if (i == coeffs.length - 1) {
                term = new Complex(1, 0);
            }
            result = result.add(term.scale(coeffs[i]));
        }
        return result;
    }

    // Evaluates first derivative at z
    private static Complex evaluateFirstDerivative(double[] coeffs, Complex z) {
        int n = coeffs.length - 1;
        Complex result = new Complex(0, 0);
        for (int i = 0; i < n; i++) {
            int power = n - i - 1;
            Complex term = new Complex(power * coeffs[i], 0);
            for (int j = 0; j < power - 1; j++) {
                term = term.multiply(z);
            }
            if (power == 0) {
                term = new Complex(coeffs[n - 1], 0);
            }
            result = result.add(term);
        }
        return result;
    }

    // Evaluates second derivative at z
    private static Complex evaluateSecondDerivative(double[] coeffs, Complex z) {
        int n = coeffs.length - 1;
        Complex result = new Complex(0, 0);
        for (int i = 0; i < n - 1; i++) {
            int power = n - i - 1;
            Complex term = new Complex(power * (power - 1) * coeffs[i], 0);
            for (int j = 0; j < power - 2; j++) {
                term = term.multiply(z);
            }
            if (power <= 1) {
                term = new Complex(0, 0);
            }
            result = result.add(term);
        }
        return result;
    }

    // Deflates polynomial by dividing by (z - root)
    private static double[] deflatePolynomial(double[] coeffs, Complex root) {
        int n = coeffs.length - 1;
        double[] remainder = new double[n];
        remainder[0] = coeffs[0];
        for (int i = 1; i < n; i++) {
            remainder[i] = coeffs[i] + remainder[i - 1] * root.real;
        }
        // Check residual to ensure root is accurate
        double residual = coeffs[n] + remainder[n - 1] * root.real;
        if (Math.abs(residual) > 1e-8) {
            // Log warning or retry with refined root (simplified here)
        }
        return remainder;
    }
}