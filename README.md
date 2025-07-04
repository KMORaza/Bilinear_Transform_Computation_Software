# Software zur Berechnung der Bilinearen-Transform (Bilinear Transform Computation Software)

## _Core Functionality_

### Bilinear Transform Engine
- Implements the standard bilinear transform: $s = (2/T)*(z-1)/(z+1)$  
  where T is the sampling period
- Handles polynomial substitution for both numerator and denominator
- Maintains coefficient normalization during transformation

### Inverse Bilinear Transform
- Implements the inverse mapping: $z = (2 + sT)/(2 - sT)$
- Computes analog poles/zeros from digital poles/zeros
- Handles special cases (poles at $z = -1$ mapping to $s = ∞$)

### Filter Design Methods
- Butterworth: Maximally flat magnitude response
- Chebyshev Type I: Equiripple in passband
- Chebyshev Type II: Equiripple in stopband
- Elliptic: Equiripple in both passband and stopband
- Bessel: Maximally flat group delay

## _Simulation Logic_

### Frequency Response Calculation
- Evaluates $H(e^{jω}) = (Σb_{k}e^{-jωk})/(Σa_{k}e^{-jωk})$
- Computes magnitude $20log10|H(e^{jω})|$ and phase $∠H(e^{jω}))$
- Uses uniform sampling from $ω=0$ to $ω=π$

### Time Domain Simulation
- Solves difference equation: $y[n] = (Σb_k x[n-k] - Σa_k y[n-k])/a_{0}$
  
  ```
  y[n] = (Σb_k x[n-k] - Σa_k y[n-k])/a₀
  ```
- Implements:
  - Impulse response ($x[0]=1$, $x[n]=0$ for $n>0$)
  - Step response ($x[n]=1$ for all $n$)
  - Custom input sequences

### Pole-Zero Analysis
- Finds roots of numerator (zeros) and denominator (poles)
- Uses Laguerre's method for polynomial root finding
- Stability determined by |poles| < 1

### Group Delay Calculation
- Computes as negative derivative of phase: $τ(ω) = -d∠H(e^{jω})/dω$
  ```
  τ(ω) = -d∠H(e^jω)/dω
  ```
- Numerically approximated using finite differences

## _Code Structure_

### Mathematical Core
- Polynomial operations (root finding, multiplication)
- Complex number arithmetic
- Bilinear transform implementations
- Filter coefficient generation

### Analysis Modules
- Frequency response evaluator
- Time domain simulator
- Stability analyzer
- Pole-zero plot generator

### Data Flow
1. User specifies filter parameters
2. System generates analog prototype
3. Applies bilinear transform
4. Performs requested analyses
5. Returns numerical and graphical results

## _Physics/Mathematical Models_

### Analog Filter Prototypes
- Butterworth: $|H(jΩ)|^{2} = 1/(1+(Ω/Ω_c)^{2N})$
  
  ```
  |H(jΩ)|² = 1/(1 + (Ω/Ω_c)^2N)
  ```
- Chebyshev I: $|H(jΩ)|^{2} = 1/(1+ε^{2}*T_{N}^{2}(Ω/Ω_{c}))$
  
  ```
  |H(jΩ)|² = 1/(1 + ε²T_N²(Ω/Ω_c))
  ```
- Chebyshev II: $|H(jΩ)|^{2} = 1/(1 + 1/(ε^{2}*T_{N}^{2}(Ω_{s}/Ω)))$
  
  ```
  |H(jΩ)|² = 1/(1 + 1/(ε²T_N²(Ω_s/Ω)))
  ```
- Elliptic: Uses Jacobi elliptic functions
- Bessel: Polynomials with maximally flat delay

### Bilinear Transform Mathematics
- Frequency warping relationship: $Ω=(2/T)*tan(ω/2)$
  ```
  Ω=(2/T)*tan(ω/2)
  ```
- Pre-warping correction:  $Ω_c=(2/T)*tan(ω_c/2)$
  ```
  Ω_c=(2/T)*tan(ω_c/2)
  ```

### Difference Equations
- Standard form: $Σa_k y[n-k] = Σb_k x[n-k]$
  ```
  Σa_k y[n-k] = Σb_k x[n-k]
  ```
- Implemented with direct form I structure

### Pole-Zero Analysis
- Stability criterion: $|z_i|$ < 1 for all poles
- Transfer function factorization: $H(z) = K Π(z - z_i)/Π(z - p_i)$
  ```
  H(z) = K Π(z - z_i)/Π(z - p_i)
  ```

### Frequency Response
- Magnitude: $|H(e^{jω})| = sqrt(Re^{2} + Im^{2})$
  ```
  |H(exp(jω))| = sqrt(Re² + Im²)
  ```
- Phase: $∠H(e^{jω}) = atan2(Im, Re)$
  ```
  ∠H(exp(jω)) = atan2(Im, Re)
  ```

---

## ① Analog to Digital Filter Mapping

### _Bilinear Transform Method_
**Algorithm Steps:**
1. Receive analog coefficients ($a_{n}, b_{n}$) and sampling period T
2. For each coefficient in numerator and denominator:
   - Apply substitution: $s ← (2/T)(z-1)/(z+1)$
     ```
     s ← (2/T)(z-1)/(z+1)
     ```
   - Expand polynomial terms
3. Combine like terms to form digital transfer function
4. Normalize coefficients so $a_{0}$ = 1

**Key Equations:**
- Transform equation: $s = (2/T)*(z-1)/(z+1)$
  ```
  s = (2/T)*(z-1)/(z+1)
  ```
- Resulting digital transfer function: $H(z) = H(s)|_{s}=(2/T)(z-1)/(z+1)$
  ```
  H(z) = H(s)|ₛ=(2/T)(z-1)/(z+1)
  ```
### _Pre-warping Correction_
**Frequency Mapping:**
- Analog frequency (Ω) to digital frequency (ω) relationship: $Ω = (2/T)tan(ωT/2)$
- Critical frequency adjustment: $Ω_{corrected} = (2/T)*tan((ω_{desired})*T/2)$
```
Ω_corrected = (2/T) * tan(ω_desired*T/2)
```

**Implementation:**
1. Compute pre-warped analog cutoff: $Ω_a = (2/T)tan(ω_{n}T/2)$
   ```
   Ωₐ = (2/T)tan(ωₙT/2)
   ```
3. Design analog filter at Ωₐ
4. Apply bilinear transform

### _Butterworth Filters_
**Analog Prototype:** $|H(jΩ)|^{2} = 1 / [1 + (Ω/Ω_{c})^{2N}]$
```
|H(jΩ)|² = 1 / [1 + (Ω/Ω_c)^(2N)]
```
**Design Steps:**
1. Calculate required order N from specifications
2. Determine analog poles: $s_k = Ω_c * e^{(j[π/2 + (2k+1)π/2N])}$, , $k=0,1,...N-1$
   ```
   s_k = Ω_c * exp(j[π/2 + (2k+1)π/2N]), k=0,1,...N-1
   ```
4. Convert to digital via bilinear transform

### _Chebyshev Type I Filters_
**Analog Prototype:** $|H(jΩ)|^{2} = 1 / [1 + ε^{2}T_{N}^{2}(Ω/Ω_{c})]$
```
|H(jΩ)|² = 1 / [1 + ε²T_N²(Ω/Ω_c)]
```
where $T_N$ is Chebyshev polynomial of 1st kind

**Design Steps:**
1. Compute ε from ripple specification
2. Calculate poles on ellipse in s-plane
3. Apply bilinear transform

### _Chebyshev Type II Filters_
**Analog Prototype:** $|H(jΩ)|^{2} = 1 / [1 + 1/(ε^{2}T_{N}^{2}(Ω_s/Ω))]$
```
|H(jΩ)|² = 1 / [1 + 1/(ε²T_N²(Ω_s/Ω))]
```
**Design Steps:**
1. Determine stopband frequency $Ω_s$
2. Compute poles and zeros
3. Transform to digital domain

### _Elliptic Filters_
**Analog Prototype:** $|H(jΩ)|^{2} = 1 / [1 + ε^{2}R_{N}^{2}(Ω,L)]$
```
|H(jΩ)|² = 1 / [1 + ε²R_N²(Ω,L)]
```

**Special Characteristics:**
- Uses Jacobi elliptic functions
- Equiripple in both passband and stopband
- Requires calculation of elliptic integrals

### _Bessel Filters_
**Analog Prototype:**
```
H(s) = 1/B_N(s)
```
where $B_N$ is Bessel polynomial

**Key Feature:**
- Maximally flat group delay
- Nonlinear phase to digital conversion

### _Polynomial Transformation_
**Numerical Method:**
1. Initialize arrays for numerator/denominator
2. For each term $a_{n}s^{n}$:
   - Expand $[(z-1)/(z+1)]^{n}$
   - Multiply by $(2/T)^{n}$ coefficient
   - Distribute across polynomial
3. Combine like terms

**Example for 2nd Order:** $a_{2}s^{2} → a_{2}(2/T)^{2}(z-1)^{2}/(z+1)^{2} → a^{2}(4/T^{2})(z^{2}-2z+1)/(z^{2}+2z+1)$
```
a₂s² → a₂(2/T)²(z-1)²/(z+1)² → a₂(4/T²)(z²-2z+1)/(z²+2z+1)
```

### _Stability Preservation_
**Verification Steps:**
1. Map analog poles (s-plane left half-plane)
   → Digital poles (inside unit circle)
2. Check all poles satisfy $|z_i|$ < 1
3. Verify no pole-zero cancellations outside unit circle

### _Frequency Warping Compensation_

**Algorithm**
1. Input desired digital frequency $ω_d$
2. Compute pre-warped analog frequency: $Ω_a = (2/T)tan((ω_d)*T/2)$
3. Design analog filter at $Ω_a$
4. Apply bilinear transform
**Mathematical Justification:**
- Bilinear transform creates nonlinear frequency mapping
- Pre-warping ensures critical frequencies align correctly

### _Numerical Stability_
- Uses normalized polynomial forms
- Implements careful root finding with:
  - Laguerre's method
  - Polynomial deflation
  - Residual checking

### _Coefficient Scaling_
- Normalizes transfer function so $a_0$ = 1
- Prevents numerical overflow/underflow
- Maintains precision in fixed-point implementations

### _Frequency Response Verification_
1. Compare analog prototype response at key frequencies
2. Verify digital response matches at:
   - DC (ω=0)
   - Nyquist (ω=π/T)
   - Critical frequencies

### _Time Domain Validation_
1. Impulse response invariance check
2. Step response steady-state verification
3. Comparison with known stable filters

---

## ② Core Bilinear Transform 

### _Bilinear Transform Definition_
The bilinear transform converts an analog transfer function H(s) to a digital transfer function H(z) using the substitution: $s = (2/T)*(z - 1)/(z + 1)$
```
s = (2/T) * (z - 1)/(z + 1)
```
where
- T = sampling period (seconds)
- s = Laplace domain complex frequency
- z = Z-transform variable

### _Frequency Warping Relationship_
The transform creates a non-linear frequency mapping between analog (Ω) and digital (ω) frequencies: $Ω = (2/T)*tan(ωT/2)$

1. Preserves stability (maps left-half s-plane to unit z-circle)
2. Avoids aliasing through non-linear frequency compression
3. One-to-one mapping between analog and digital domains
### _Direct Transformation Method_
1. **Input**: Analog transfer function coefficients $[a_{n}, a_{n-1},...a_{0}]$, $[b_{m}, b_{m-1},...b_{0}]$
2. **Substitution**:
   - Replace each 's' term with $(2/T)(z-1)/(z+1)$
   - Multiply through by $(z+1)^{N}$ to clear denominators
3. **Normalization**:
   - Collect like terms in z
   - Divide all coefficients by leading denominator coefficient
### _Polynomial Transformation_
**Numerical Method:**
1. Initialize arrays for numerator/denominator
2. For each term $a_{n}s^{n}$:
   - Expand $[(z-1)/(z+1)]^{n}$
   - Multiply by $(2/T)^{n}$ coefficient
   - Distribute across polynomial
3. Combine like terms

**Example for 2nd Order:** $a_{2}s^{2} → a_{2}(2/T)^{2}(z-1)^{2}/(z+1)^{2} → a^{2}(4/T^{2})(z^{2}-2z+1)/(z^{2}+2z+1)$

```
a₂s² → a₂(2/T)²(z-1)²/(z+1)² → a₂(4/T²)(z²-2z+1)/(z²+2z+1)
```

---

## ③ Pre-Warping 

Pre-warping compensates for the non-linear frequency mapping that occurs during the bilinear transform. The bilinear transform maps the entire analog frequency range (0 to ∞) into the digital frequency range (0 to π) in a non-linear fashion.

### _Frequency Warping_
The fundamental frequency mapping equation: $ω_{d} = 2*arctan(Ω_{a}*T/2)$
```
ω_d = 2 * arctan(Ω_a * T/2)
```
where:
- $ω_d$ = digital frequency (radians/sample)
- $Ω_a$ = analog frequency (radians/second)
- $T$ = sampling period (seconds)

### _Pre-Warping Correction Formula_
To maintain critical frequencies, we use: $Ω_{a} = (2/T)*tan(ω_{d}/2)$
```
Ω_a = (2/T) * tan(ω_d/2)
```
This ensures the analog filter's cutoff frequency $Ω_c$ maps exactly to the desired digital cutoff frequency $ω_c$.

### _Critical Frequency Pre-Warping_
```
function computePreWarpedFrequency(ω_d, T):
    if ω_d < 0:
        throw error "Frequency must be non-negative"
    return (2.0 / T) * tan(ω_d * T / 2.0)
```

### _Completion of Pre-Warping Process_
```
function applyPreWarping(analogTF, ω_d, T):
    Ω_a = (2.0 / T) * tan(ω_d / 2.0)
    num = analogTF.getNumerator()
    den = analogTF.getDenominator()
    scale = Ω_a / ω_d
    newNum = scalePolynomial(num, scale)
    newDen = scalePolynomial(den, scale)    
    return new SymbolicTransferFunction(newNum, newDen, "s")
function scalePolynomial(coeffs, scale):
    newCoeffs = new array[coeffs.length]
    for i from 0 to coeffs.length-1:
        power = coeffs.length - 1 - i
        newCoeffs[i] = coeffs[i] * (1.0/scale)^power
    return newCoeffs
```

### _Frequency Scaling Principle_
Each term in the transfer function H(s) is scaled according to its power of s: $H'(s) = H(s/α)$
```
H'(s) = H(s/α)
```
where α is the scaling factor $Ω_a/ω_d$

### _Coefficient Transformation_
For a general polynomial term: $a_{n}s^{n} → a_{n}*(s/α)^{n} = (a_{n}/α^{n})*s^{n}$
```
a_n*s^n → a_n*(s/α)^n = (a_n/α^n)*s^n
```

### _Transfer Function Transformation_
Given original analog transfer function: $H_{a}(s) = (Σb_{k}*s^{k})/(Σa_{k}*s^{k})$
```
H_a(s) = (Σb_k*s^k)/(Σa_k*s^k)
```
The pre-warped version becomes $(Σ(b_{k}/α^{k})*s^{k})/(Σ(a_{k}/α^{k})*s^{k})$
```
H'ₐ(s) = (Σ(bₖ/αᵏ)*sᵏ)/(Σ(aₖ/αᵏ)*sᵏ)
```

### _Considerations_
1. Normalization Handling
- All coefficients are scaled relative to the highest order term
- Maintains numerical stability during transformation
2. Special Cases
- DC (ω=0): No pre-warping needed as tan(0)=0
- Nyquist (ω=π): tan(π/2)→∞, handled as edge case
3. Numerical Stability
- Uses direct polynomial scaling rather than root manipulation
- Preserves original polynomial structure
- Avoids compounding numerical errors from multiple transforms

### _Verification_
- Frequency Verification
Check that: $H_{d}(e^{jω_{c}}) ≈ H_{a}(jΩ_{c})$
```
H_d(e^(jω_c)) ≈ H_a(jΩ_c)
```
where $ω_c$ is the desired digital cutoff frequency and $Ω_c$ is the pre-warped analog frequency.
- Boundary Cases
  - Verify DC gain remains unchanged
  - Check behavior as $ω → π$

### _Usage Flow_
1. User specifies desired digital cutoff frequency $ω_d$
2. System computes pre-warped analog frequency $Ω_a$
3. Original analog filter is scaled using polynomial transformation
4. Bilinear transform is applied to the pre-warped analog filter
5. Resulting digital filter has exact response at $ω_d$

---

## ④ Frequency Response Analysis

The frequency response analysis module computes the magnitude and phase response of digital filters derived via the bilinear transform. It evaluates the transfer function H(z) along the unit circle (z = e^(jω)) to determine how the filter affects different frequency components.  

### _Evaluate Transfer Function on Unit Circle_
Given a discrete-time transfer function: $H(z) = (b_{0} + b_{1}z^{-1} + ... + b_{n}z^{-n}))/(a_{0} + a_{1}z^{-1} + ... + a_{m}z^{-m})$
```
H(z) = (b₀ + b₁z⁻¹ + ... + bₙz⁻ⁿ) / (a₀ + a₁z⁻¹ + ... + aₘz⁻ᵐ)  
```
Substitute $z = e^{jω}$:  $H(e^{jω}) = (Σb_{k}e^{-jωk})/(Σa_{k}e^{-jωk})$
```
H(e^(jω)) = (Σbₖ e^(-jωk))/(Σaₖ e^(-jωk))  
```
where:  
- ω = 0 to π (Nyquist frequency)  
- k = 0 to N (filter order)  

### _Compute Real and Imaginary Components_  
Decompose numerator and denominator into real/imaginary parts:  
- $Re_{num} = Σb_{k}*cos(ωk), Im_{num} = -Σb_{k}*sin(ωk)$
- $Re_{den} = Σa_{k}*cos(ωk), Im_{den} = -Σa_{k}*sin(ωk)$
```
Re_num = Σbₖ*cos(ωk), Im_num = -Σbₖ*sin(ωk) (Numerator)
Re_den = Σaₖ*cos(ωk), Im_den = -Σaₖ*sin(ωk) (Denominator)  
```

### _Calculate Magnitude Response_  
$|H(e^{jω})| = sqrt((Re_{num}^{2} + Im_{num}^{2})/(Re_{den}^{2} + Im_{den}^{2}))$
```
|H(e^(jω))| = sqrt((Re_num² + Im_num²)/(Re_den² + Im_den²))  
```
Convert to decibels (dB): $20*log10(|H(e^{jω})|)$
```
Magnitude (dB) = 20 log10(|H(e^(jω))|)  
```

### _Calculate Phase Response_
$∠H(e^{jω}) = atan2(Im_{num}, Re_{num}) - atan2(Im_{den}, Re_{den})$
```
∠H(e^(jω)) = atan2(Im_num, Re_num) - atan2(Im_den, Re_den)  
```
Phase is unwrapped to avoid 2π jumps.  

### _Compute Group Delay_  
Group delay measures phase distortion: $τ(ω) = -d∠H(e^{jω})/dω$
```
τ(ω) = -d∠H(e^(jω)) / dω  
```
Numerically approximated using finite differences: $τ(ω) ≈ -[∠H(ω + Δω) - ∠H(ω - Δω)]/(2Δω)$
```
τ(ω) ≈ -[∠H(ω + Δω) - ∠H(ω - Δω)] / (2Δω)  
```

### _Bilinear Transform Pre-Warping_  
To counteract frequency warping, critical frequencies are pre-warped: $Ω_{analog} = (2/T)tan(ω_{digital}/2)$
```
Ω_analog = (2/T) tan(ω_digital / 2)  
```
where:  
- $Ω_{analog}$ = analog frequency (rad/s)  
- $ω_{digital}$ = digital frequency (rad/sample)  
- $T$ = sampling period  

### _Frequency Response of Analog Prototypes_
- **Butterworth**: $|H(jΩ)| = 1/sqrt(1+(Ω/Ω_{c})^{2N})$
  ```
  |H(jΩ)| = 1 / sqrt(1 + (Ω/Ω_c)^(2N))  
  ```
- **Chebyshev Type I**: $|H(jΩ)| = 1/sqrt(1 + ε^{2}T_{N}^{2}(Ω/Ω_{c)})$
  ```
  |H(jΩ)| = 1 / sqrt(1 + ε² T_N²(Ω/Ω_c))  
  ```
  where $T_N$ = Chebyshev polynomial of order N.  
- **Chebyshev Type II**: $|H(jΩ)| = 1/sqrt(1 + 1/(ε^{2}T_{N}^{2}(Ω_{s}/Ω)))$
  ```
  |H(jΩ)| = 1 / sqrt(1 + 1/(ε² T_N²(Ω_s/Ω)))  
  ```
- **Elliptic**: Uses Jacobi elliptic functions.  
- **Bessel**: Designed for maximally flat group delay.  

### _Implementation Details_

1. **Frequency Sampling**  
- Linear spacing from ω = 0 to ω = π.  
- Logarithmic spacing optional for wideband analysis.  

2. **Numerical Stability Handling**  
- Near-zero denominators:  
  $If |Re_{den}| + |Im_{den}| < ε → H(e^{jω}) ≈ 0$  
- High-frequency roll-off detection for FIR/IIR filters.  
3. **Fast Evaluation via Horner’s Method**  
Optimized polynomial evaluation: $H(e^{jω}) = b_{0} + e^{-jω}(b_{1} + e^{-jω}(b_{2} + ... ))/(a_{0} + e^{-jω}(a_{1} + ... ))$
Reduces computational complexity from $O(N^{2})$ to $O(N)$.  

---

## ⑤ Inverse Bilinear Transform
The inverse bilinear transform in the software converts a digital transfer function (in the z-domain) back to an analog transfer function (in the s-domain). It allows users to input a digital transfer function, specify the sampling period (T), and compute the corresponding analog transfer function, along with its poles, zeros, and frequency response. 

- **Input Validation**:
   - The sampling period T is validated to be positive (T > 0).
   - The precision is used to format numerical outputs (e.g., coefficients, poles, zeros).
   - The digital transfer function’s numerator and denominator coefficients are retrieved and normalized (removing leading zeros).
- **Inverse Bilinear Transform**:
   - The transform uses the substitution $z = (2 + sT)/(2 - sT)$, where s is the analog domain variable and T is the sampling period.
   - The digital transfer function $H(z) = N(z) / D(z)$ is transformed by substituting z with the above expression to obtain $H(s) = N(s)/D(s)$.
- **Polynomial Computation**:
   - The numerator $N(z)$ and denominator $D(z)$ are polynomials in $z^{-1}$ (e.g., $N(z) = b_0 + b_1*z^{-1} + ...$).
   - Substituting $z = (2 + sT)/(2 - sT)$ into each term $z^{-k}$ requires computing the resulting s-domain polynomial.
   - For a term $z^{-k}$, the substitution becomes:
     - $z^{-k} = ((2 - sT) / (2 + sT))^{k}$
     - This is expanded using the binomial theorem to express $(2 - sT)^{k}$ and $(2 + sT)^{-k}$ as polynomials in s.
   - The software computes the coefficients of the resulting numerator and denominator polynomials in s.
- **Pole-Zero Calculation**:
   - The roots of the numerator (zeros) and denominator (poles) of H(s) are computed using `PolynomialRootFinder.java`.
   - Special handling is applied for poles/zeros at z = -1, which map to s = infinity in the analog domain.
- **Frequency Response**:
   - The analog frequency response is computed by evaluating H(s) at $s = jω$, where w is the angular frequency (rad/s).
   - The magnitude (in dB) and phase are plotted in the `AnalogFrequencyResponsePanel`.
    
### _Model_
The inverse bilinear transform is based on the following key equation:

- **Bilinear Transform (Forward)**: $s = (2/T) * (z - 1) / (z + 1)$
- **Inverse Bilinear Transform**: Solving for z in terms of s:
  $- z = (2 + sT) / (2 - sT)$

For a digital transfer function $H(z) = N(z) / D(z)$, where:
- $N(z) = b_0 + b_{1}z^{-1} + b_{2}z^{-2} + ... + b_{M}z^{-M}$
- $D(z) = a_0 + a_{1}z^{-1} + a_{2}z^{-2} + ... + a_{N}z^{-N}$

The inverse transform substitutes $z = (2 + sT)/(2 - sT)$ into H(z):

1. **Substitution for $z^{-k}$**:
   - $z^{-k} = ((2 - sT) / (2 + sT))^{k}$
   - Let $u = sT$, so $z = (2 + u) / (2 - u)$, and $z^{-1} = (2 - u) / (2 + u)$.
   - For $z^{-k}$, compute $((2 - u) / (2 + u))^{k}$:
     - Numerator: $(2 - u)^{k} = Σ_{i=0}^{k} binomial(k, i) * 2^{k-i)} * (-u)^{i}$
     - Denominator: $(2 + u)^{-k} = (2 + u)^{k} = Σ_{j=0}^{k} binomial(k, j) * 2^{k-j} * u^j$ 
     - Combine to form a polynomial in u (and thus s).
2. **Polynomial Expansion**:
   - Each term $b_{k}*z^{-k}$ in N(z) becomes a polynomial in s after substitution.
   - Similarly, each term $a_{k}*z^{-k}$ in D(z) becomes a polynomial in s.
   - The software aggregates these terms to form the numerator and denominator polynomials of H(s).
3. **Normalization**:
   - The resulting polynomials are normalized by dividing by the leading coefficient of the denominator to ensure a monic denominator (leading coefficient = 1).
   - Leading zeros are removed (coefficients < 1e-10).
4. **Pole-Zero Mapping**:
   - A pole at $z = p$ in the z-domain maps to $s = (2/T)*(p - 1)/(p + 1)$.
   - If $p = -1$, the pole maps to s = infinity, which is handled by reducing the polynomial degree.
   - Zeros are mapped similarly.
5. **Frequency Response**:
   - For H(s), evaluate at s = j*ω:
     - Magnitude = $|H(jω)| = sqrt(Re[H(jω)]^{2} + Im[H(jω)]^{2})$
     - Magnitude in dB = $20 * log10(|H(ω)|)$
     - Phase = $atan2(Im[H(jω)], Re[H(jω)])$
6. **Example**
For a digital transfer function $H(z) = (1 + 0.5z^{-1}) / (1 - 0.3z^{-1})$ with $T = 1$:
- Substitute $z = (2 + s)/(2 - s)$.
- Numerator: $1 + 0.5 * (2 - s) / (2 + s) = (2 + s + 0.5 * (2 - s)) / (2 + s) = (3 + 0.5s) / (2 + s)$.
- Denominator: $1 - 0.3 * (2 - s) / (2 + s) = (2 + s - 0.3 * (2 - s)) / (2 + s) = (1.4 + 1.3s) / (2 + s)$.
- $H(s) = (3 + 0.5s)/(1.4 + 1.3s)$ after normalization.
- Poles and zeros are computed, and the frequency response is plotted.

---

## ⑥ Stability Feedback
The stability feedback evaluates the stability of a digital filter by analyzing its pole locations and Nyquist plot. It determines whether a digital filter is stable (all poles inside the unit circle) and provides visual feedback through a pole-zero plot and a Nyquist plot.

### _Stability Verification_
   - The `StabilityVerification` class is instantiated with the input `SymbolicTransferFunction`.
   - The `isStable()` method checks if all poles have magnitude less than 1 - EPSILON (EPSILON = 1e-10).
   - Poles and zeros are computed using `computePoles()` and `computeZeros()`, which call `findRoots()`.
### _Pole and Zero Computation_
   - For a polynomial $P(z) = a_{0}z^{n} + a_{1}z^{n-1} + ... + an$, find roots by solving $P(z) = 0$.
   - Laguerre’s method iteratively refines a root estimate x using:
     - $P(x)$ = evaluate polynomial at x
     - $P'(x)$ = first derivative: $P'(x) = na_{0}z^{n-1} + (n-1)a_{1}z^{n-2} + ... + a_{n-1}$
     - $P''(x)$ = second derivative: $P''(x) = n(n-1)a_{0}z^{n-2} + (n-1)(n-2)a_{1}z^{n-3} +$ $...$
     - $G = P'(x) / P(x)$
     - $H = G^2 - P''(x) / P(x)$
     - Correction = $n/(G ± sqrt((n-1)*(nH - G^2)))$
     - Update x = x - Correction
   - After finding a root, deflate the polynomial: $P(z) = (z - r) * Q(z)$, where Q(z) is computed via synthetic division (though the current implementation has issues with complex roots).
### _Nyquist Plot_
   - Evaluate $H(z)$ at $z = e^{jω}$:
     - $H(e^{jω}) = N(e^{jω}) / D(e^{jω})$
     - $N(e^{jω}) = Σ(b_k * cos(ωk) - j*sin(ωk))$
     - $D(e^{jω}) = Σ(a_k * cos(ωk) - j*sin(ωk))$
   - Plot $Re[H(e^{jω})]$ vs. $Im[H(e^{jω})]$ for ω from 0 to 2π.
   - Stability is assessed by checking if the curve encircles the point (-1, 0). For a stable system with no open-loop poles outside the unit circle, the curve should not encircle (-1, 0).
### _Pole-Zero Plot_
   - Poles are roots of $D(z) = 0$, zeros are roots of $N(z) = 0$.
   - Plot each pole/zero as a point $(Re[z], Im[z])$ in the z-plane.
   - The unit circle is drawn as $x^2 + y^2 = 1$.
### _Stability Criterion_
   - A digital filter is stable if all poles of $H(z) = N(z) / D(z)$ lie inside the unit circle, i.e., $|p_i| < 1$ for all poles $p_i$.
   - The software uses $|p_i| < 1 - EPSILON$ to ensure strict stability, where `EPSILON = 1e-10`.

---

## ⑦ Direct Bilinear Mapping Engine

The `DirectBilinearMappingEngine` class in the provides a graphical interface to compute and display the results of applying the bilinear transform to an analog transfer function. The bilinear transform converts an analog filter (in the s-domain) to a digital filter (in the z-domain) using the substitution $s = (2/T)*(z-1)/(z+1)$, where `T` is the sampling period. This class differs from `BilinearTransform.java` by using an explicit polynomial expansion approach to compute the digital transfer function.

The class operates by transforming the analog transfer function $H(s) = N(s)/D(s)$ into a digital transfer function $H(z) = N(z)/D(z)$ using the bilinear transform. 

### _Input Validation_
   - Parses the coefficient strings into `double` arrays, handling invalid formats via try-catch blocks.
   - Ensures $T > 0$ to avoid division by zero in the bilinear transform formula.
   - Creates a `SymbolicTransferFunction` object with the parsed coefficients and variable `s` (analog domain).

### _Bilinear Transform Application_
   - Substitutes $s = (2/T)*(z-1)/(z+1)$ into the transfer function.
   - Expands the resulting rational function by computing the numerator and denominator polynomials in the z-domain.
   - Uses binomial expansion to handle powers of $(z-1)$ and $(z+1)$ in the substitution.

### _Polynomial Computation_
   - For a polynomial $P(s) = a_n * s^n + ... + a_0$, substitute $s = (2/T) * (z-1)/(z+1)$.
   - This results in $P(z) = a_n * ((2/T) * (z-1)/(z+1))^{n} + ... + a_0$.
   - Expand each term $((z-1)/(z+1))^{k}$ using binomial coefficients:
     - $(z-1)^{k} = sum_{i=0}^{k} [binomial(k, i) * z^i * (-1)^{k-i}]$
     - $(z+1)^{-k} = (1/(z+1))^{k}$, approximated via polynomial division or direct expansion.
   - Combine terms to form the final numerator $N(z)$ and denominator $D(z)$.

### _Output Form
   - Normalizes the resulting coefficients (removes leading zeros).
   - Displays the digital transfer function with coefficients formatted to a specified precision (default 4 decimal places).
   - Includes a string representation of the transfer function in the form $H(z) = (b_{m}z^{m} + ... + b_{0})/(a_{n}z^{n} + ... + a_{0})$.

### _Model_
- The bilinear transform is defined as: $s = (2/T)*(z-1)/(z+1)$
where:
  - $s$ is the complex frequency in the analog domain.
  - $z$ is the complex variable in the digital domain.
  - $T$ is the sampling period (in seconds).

- For an analog transfer function: $H(s) = N(s)/D(s) = (b_{m}s^{m}+b_{m-1}s^{m-1} + ... + b_{0})/(a_{n}s^{n} + a_{n-1}s^{n-1} + ... + a_{0})$

```
H(s) = N(s) / D(s) = (b_m s^m + b_(m-1) s^(m-1) + ... + b_0) / (a_n s^n + a_(n-1) s^(n-1) + ... + a_0)
```
the digital transfer function is $H(z) = N(z)/D(z)$ where $N(z)$ and $D(z)$ are polynomials in $z$ derived by substituting $s = (2/T) * (z-1)/(z+1)$ into $N(s)$ and $D(s)$.

- For a term $s^k$ in the polynomial: $s^{k} = ((2/T)(z-1)/(z+1))^{k} = (2/T)^{k}*(z-1)^{k}/(z+1)^{k}$

```
s^k = ((2/T) * (z-1)/(z+1))^k = (2/T)^k * (z-1)^k / (z+1)^k
```
  - **Numerator of the term**: $(z-1)^{k} = Σ_{i=0}^{k} [binomial(k, i) * z^{i}*(-1)^{k-i}]$.
  - **Denominator of the term**: $(z+1)^{k}$, which contributes to the common denominator.
  - **Binomial coefficient**: $binomial(n, k) = n!/(k!*(n-k)!)$.

- The final $N(z)$ and $D(z)$ are computed by:
  1. Expanding each term in $N(s)$ and $D(s)$ after substitution.
  2. Collecting like terms (same powers of $z$) across all expansions.
  3. Normalizing the resulting polynomials to remove leading zeros.

- Normalization: Coefficients with magnitude less than `EPSILON` (typically $1e-10$) are treated as zero to avoid numerical noise.

## ⑧ Time Domain Simulation in Bilinear Transform Computation Software

### _Functionality_
The Time Domain Simulation serves the following purposes:
1. **Compute Responses**: Calculates the filter's output for impulse, step, or custom inputs using the difference equation derived from the transfer function.
2. **Visualize Responses**: Displays the response as a plot of amplitude versus sample index, with dynamic scaling and animation.
3. **User Interaction**: Allows users to input custom sequences, control animation (play/pause), and reset the plot.
4. **Precision Control**: Formats numerical labels based on a user-specified precision parameter.

### _Logic_
- **Key Parameters**
  - `tf`: The `SymbolicTransferFunction` defining the filter’s numerator and denominator polynomials.
  - `precision`: Controls the number of decimal places for y-axis labels.
  - `NUM_SAMPLES = 50`: Fixed number of samples for the response (could be made configurable).
  - `ANIMATION_DELAY = 100`: Time between animation frames (in milliseconds).
  - `EPSILON = 1e-10`: Threshold for numerical comparisons (e.g., avoiding division by zero).
- **Response Computation**
   - Each `ResponsePanel` computes its response using the `computeDifferenceEquation` method, which implements the difference equation based on the transfer function’s numerator and denominator.
   - The input sequence (`x`) varies by panel:
     - **Impulse**: `x[0] = 1`, `x[n] = 0` for n > 0.
     - **Step**: `x[n] = 1` for all n.
     - **Custom Input**: Parsed from a comma-separated text field (e.g., "1, 0.5, 0.25, 0").
   - The output sequence (`response`) is computed for `NUM_SAMPLES` points and stored for plotting.
- **Animation**
   - A `Timer` updates the plot every `ANIMATION_DELAY` (100ms), incrementing the `currentSample` index to display the response progressively.
   - Users can pause/resume animation using a "Play/Pause" button or reset it to the start with a "Reset" button.
   - If `currentSample` reaches `NUM_SAMPLES`, the animation pauses automatically.
- **Visualization**
   - The `paintComponent` method in `ResponsePanel` draws the plot:
     - **Axes**: X-axis (sample index, 0 to 49), Y-axis (amplitude, scaled dynamically).
     - **Grid**: 10x10 grid for reference.
     - **Data**: Plots the response up to `currentSample` as a line graph.
     - **Labels**: X-axis labeled with sample indices, Y-axis with amplitude values formatted to `precision` decimal places (or scientific notation for large/small values).
     - **Title**: Displays the response type (e.g., "Impulse Response").

### _Models_
- The computation is based on the difference equation for a digital filter defined by its transfer function $H(z) = B(z)/A(z)$, where:
  - $B(z)$ is the numerator polynomial: $b(M) * z^M + b(M-1) * z^{M-1} + ... + b(0)$.
  - $A(z)$ is the denominator polynomial: $a(N) * z^N + a(N-1) * z^{N-1} + ... + a(0)$.
  - $b(k)$ and $a(k)$ are the coefficients, and $a(N)$ is the leading denominator coefficient (assumed non-zero).
- The filter’s output $y(n)$ at time step n is calculated as: $y[n] = (1/a_{0}) * (Σ_{k=0}^{M} b_{k} * x[n-k] - Σ_{k=1}^{N} a_{k} * y[n-k])$
  - $a_{0}$ is the constant term of the denominator (leading coefficient after normalization).
  - $b_{k}$ are the numerator coefficients.
  - $a_{k}$ are the denominator coefficients (excluding $a_{0}$).
  - $x[n-k]$ are past and current input samples.
  - $y[n-k]$ are past output samples.
- For each sample $n$ (`0` to `NUM_SAMPLES-1`):
  - Feedforward term: $Σ_{k=0}^M b_{k} * x[n-k]$
  - Feedback term: $Σ_{k=1}^N a_{k} * y[n-k]$
  - $y[n] = (feedforward - feedback) / a_{0}$

---

## Screenshots
![](https://raw.githubusercontent.com/KMORaza/Bilinear_Transform_Computation_Software/refs/heads/main/Bilinear%20Transform%20Computation%20Software/009/screenshots/screenshot.png)

---

_Ich fühlte ein Licht des Glücks, nachdem ich die Entwicklung der Software abgeschlossen hatte. Es war keine leichte Aufgabe, die Software von Grund auf neu zu schreiben und die notwendigen mathematischen und physikalischen Modelle, Berechnungen und Algorithmen zu integrieren. Während der Entwicklung war ich ziemlich gestresst, hatte Kopfschmerzen und überanstrengte Augen, aber trotzdem habe ich es geschafft.
Diese Software könnte Fehler aufweisen und verbesserungsbedürftig sein, daher ist sie möglicherweise nicht 100% perfekt. Aber wissen Sie was? Kein von Menschenhand geschaffenes Ding ist vollkommen perfekt._

_I felt a sense of happiness after I finished developing the software. It wasn't an easy work to write the software from scratch and integration of necessary mathematics & physics models, calculations, and algorithms. While developing this software, I felt quite stressed alongside headache and strained eyes, despite this I managed to develop the software.
This software might have flaws and may need further enhancements, so this software may not be 100% perfect. But guess what? No man-made thing is completely perfect._














