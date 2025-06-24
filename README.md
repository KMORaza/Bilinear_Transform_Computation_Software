# Software zur Berechnung der Bilinearen-Transform (Bilinear Transform Computation Software)

## _Core Functionality_

### Bilinear Transform Engine
- Implements the standard bilinear transform:  
  `s = (2/T)*(z-1)/(z+1)`  
  where T is the sampling period
- Handles polynomial substitution for both numerator and denominator
- Maintains coefficient normalization during transformation

### Inverse Bilinear Transform
- Implements the inverse mapping:  
  `z = (2 + sT)/(2 - sT)`
- Computes analog poles/zeros from digital poles/zeros
- Handles special cases (poles at `z = -1` mapping to `s = ∞`)

### Filter Design Methods
- Butterworth: Maximally flat magnitude response
- Chebyshev Type I: Equiripple in passband
- Chebyshev Type II: Equiripple in stopband
- Elliptic: Equiripple in both passband and stopband
- Bessel: Maximally flat group delay

## _Simulation Logic_

### Frequency Response Calculation
- Evaluates `H(e^jω) = (Σbₖ e^-jωk)/(Σaₖ e^-jωk)`
- Computes magnitude (`20log10|H(e^jω)|`) and phase (`∠H(e^jω))`
- Uses uniform sampling from `ω=0` to `ω=π`

### Time Domain Simulation
- Solves difference equation:  
  $y[n] = (Σb_k x[n-k] - Σa_k y[n-k])/a₀$
- Implements:
  - Impulse response (`x[0]=1`, `x[n]=0` for `n>0`)
  - Step response (`x[n]=1` for all `n`)
  - Custom input sequences

### Pole-Zero Analysis
- Finds roots of numerator (zeros) and denominator (poles)
- Uses Laguerre's method for polynomial root finding
- Stability determined by |poles| < 1

### Group Delay Calculation
- Computes as negative derivative of phase:  
  `τ(ω) = -d∠H(e^jω)/dω`
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
- Butterworth: $|H(jΩ)|² = 1/(1 + (Ω/Ω_c)^2N)$
  ```
  |H(jΩ)|² = 1/(1 + (Ω/Ω_c)^2N)
  ```
- Chebyshev I: $|H(jΩ)|² = 1/(1 + ε²T_N²(Ω/Ω_c))$
  ```
  |H(jΩ)|² = 1/(1 + ε²T_N²(Ω/Ω_c))
  ```
- Chebyshev II: $|H(jΩ)|² = 1/(1 + 1/(ε²T_N²(Ω_s/Ω)))$
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
- Magnitude: $|H(e^{jω})| = sqrt(Re² + Im²)$
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
1. Receive analog coefficients (aₙ, bₙ) and sampling period T
2. For each coefficient in numerator and denominator:
   - Apply substitution: `s ← (2/T)(z-1)/(z+1)`
   - Expand polynomial terms
3. Combine like terms to form digital transfer function
4. Normalize coefficients so a₀ = 1

**Key Equations:**
- Transform equation: `s = (2/T) * (z-1)/(z+1)`
- Resulting digital transfer function:
  ```
  H(z) = H(s)|ₛ=(2/T)(z-1)/(z+1)
  ```
### _Pre-warping Correction_
**Frequency Mapping:**
- Analog frequency (Ω) to digital frequency (ω) relationship: `Ω = (2/T)*tan(ωT/2)`
- Critical frequency adjustment: $Ω_{corrected} = (2/T)*tan((ω_{desired})*T/2)$
```
Ω_corrected = (2/T) * tan(ω_desired*T/2)
```

**Implementation:**
1. Compute pre-warped analog cutoff: `Ωₐ = (2/T)tan(ωₙT/2)`
2. Design analog filter at Ωₐ
3. Apply bilinear transform

### _Butterworth Filters_
**Analog Prototype:** $|H(jΩ)|² = 1 / [1 + (Ω/Ω_c)^(2N)]$
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
**Analog Prototype:** $|H(jΩ)|² = 1 / [1 + ε²T_N²(Ω/Ω_c)]$
```
|H(jΩ)|² = 1 / [1 + ε²T_N²(Ω/Ω_c)]
```
where $T_N$ is Chebyshev polynomial of 1st kind

**Design Steps:**
1. Compute ε from ripple specification
2. Calculate poles on ellipse in s-plane
3. Apply bilinear transform

### _Chebyshev Type II Filters_
**Analog Prototype:** $|H(jΩ)|² = 1 / [1 + 1/(ε²T_N²(Ω_s/Ω))]$
```
|H(jΩ)|² = 1 / [1 + 1/(ε²T_N²(Ω_s/Ω))]
```
**Design Steps:**
1. Determine stopband frequency $Ω_s$
2. Compute poles and zeros
3. Transform to digital domain

### _Elliptic Filters_
**Analog Prototype:** $|H(jΩ)|² = 1 / [1 + ε²R_N²(Ω,L)]$
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
2. For each term $aₙsⁿ$:
   - Expand `[(z-1)/(z+1)]ⁿ`
   - Multiply by $(2/T)ⁿ$ coefficient
   - Distribute across polynomial
3. Combine like terms

**Example for 2nd Order:** $a₂s² → a₂(2/T)²(z-1)²/(z+1)² → a₂(4/T²)(z²-2z+1)/(z²+2z+1)$
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
- Normalizes transfer function so a₀ = 1
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
The bilinear transform converts an analog transfer function H(s) to a digital transfer function H(z) using the substitution:
```
s = (2/T) * (z - 1)/(z + 1)
```
where
- T = sampling period (seconds)
- s = Laplace domain complex frequency
- z = Z-transform variable

### _Frequency Warping Relationship_
The transform creates a non-linear frequency mapping between analog (Ω) and digital (ω) frequencies: **$Ω = (2/T)*tan(ωT/2)$**

1. Preserves stability (maps left-half s-plane to unit z-circle)
2. Avoids aliasing through non-linear frequency compression
3. One-to-one mapping between analog and digital domains
### _Direct Transformation Method_
1. **Input**: Analog transfer function coefficients `[aₙ, aₙ₋₁,...a₀]`, `[bₘ, bₘ₋₁,...b₀]`
2. **Substitution**:
   - Replace each 's' term with `(2/T)(z-1)/(z+1)`
   - Multiply through by `(z+1)^N` to clear denominators
3. **Normalization**:
   - Collect like terms in z
   - Divide all coefficients by leading denominator coefficient
### _Polynomial Transformation_
**Numerical Method:**
1. Initialize arrays for numerator/denominator
2. For each term $aₙsⁿ$:
   - Expand `[(z-1)/(z+1)]ⁿ`
   - Multiply by `(2/T)ⁿ` coefficient
   - Distribute across polynomial
3. Combine like terms

**Example for 2nd Order:**
```
a₂s² → a₂(2/T)²(z-1)²/(z+1)² → a₂(4/T²)(z²-2z+1)/(z²+2z+1)
```

---

## ③ Pre-Warping 

Pre-warping compensates for the non-linear frequency mapping that occurs during the bilinear transform. The bilinear transform maps the entire analog frequency range (0 to ∞) into the digital frequency range (0 to π) in a non-linear fashion.

### _Frequency Warping_
The fundamental frequency mapping equation:
```
ω_d = 2 * arctan(Ω_a * T/2)
```
where:
- $ω_d$ = digital frequency (radians/sample)
- $Ω_a$ = analog frequency (radians/second)
- $T$ = sampling period (seconds)

### _Pre-Warping Correction Formula_
To maintain critical frequencies, we use:
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
Each term in the transfer function H(s) is scaled according to its power of s:
```
H'(s) = H(s/α)
```
where α is the scaling factor $Ω_a/ω_d$

### _Coefficient Transformation_
For a general polynomial term:
```
a_n*s^n → a_n*(s/α)^n = (a_n/α^n)*s^n
```

### _Transfer Function Transformation_
Given original analog transfer function:
```
H_a(s) = (Σb_k*s^k)/(Σa_k*s^k)
```
The pre-warped version becomes:
```
H'_a(s) = (Σ(b_k/α^k)*s^k)/(Σ(a_k/α^k)*s^k)
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
Check that:
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
Given a discrete-time transfer function:  
```
H(z) = (b₀ + b₁z⁻¹ + ... + bₙz⁻ⁿ) / (a₀ + a₁z⁻¹ + ... + aₘz⁻ᵐ)  
```
Substitute `z = e^(jω)`:  
```
H(e^(jω)) = (Σbₖ e^(-jωk))/(Σaₖ e^(-jωk))  
```
where:  
- ω = 0 to π (Nyquist frequency)  
- k = 0 to N (filter order)  

### _Compute Real and Imaginary Components_  
Decompose numerator and denominator into real/imaginary parts:  
```
Re_num = Σbₖ*cos(ωk), Im_num = -Σbₖ*sin(ωk) (Numerator)
Re_den = Σaₖ*cos(ωk), Im_den = -Σaₖ*sin(ωk) (Denominator)  
```

### _Calculate Magnitude Response_  
```
|H(e^(jω))| = sqrt((Re_num² + Im_num²)/(Re_den² + Im_den²))  
```
Convert to decibels (dB):  
```
Magnitude (dB) = 20 log10(|H(e^(jω))|)  
```

### _Calculate Phase Response_
```
∠H(e^(jω)) = atan2(Im_num, Re_num) - atan2(Im_den, Re_den)  
```
Phase is unwrapped to avoid 2π jumps.  

### _Compute Group Delay_  
Group delay measures phase distortion:  
```
τ(ω) = -d∠H(e^(jω)) / dω  
```
Numerically approximated using finite differences:  
```
τ(ω) ≈ -[∠H(ω + Δω) - ∠H(ω - Δω)] / (2Δω)  
```

### _Bilinear Transform Pre-Warping_  
To counteract frequency warping, critical frequencies are pre-warped:  
```
Ω_analog = (2/T) tan(ω_digital / 2)  
```
where:  
- $Ω_analog$ = analog frequency (rad/s)  
- $ω_digital$ = digital frequency (rad/sample)  
- $T$ = sampling period  

### _Frequency Response of Analog Prototypes_
- **Butterworth**:  
  ```
  |H(jΩ)| = 1 / sqrt(1 + (Ω/Ω_c)^(2N))  
  ```
- **Chebyshev Type I**:  
  ```
  |H(jΩ)| = 1 / sqrt(1 + ε² T_N²(Ω/Ω_c))  
  ```
  where $T_N$ = Chebyshev polynomial of order N.  
- **Chebyshev Type II**:  
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
  ```
  If |Re_den| + |Im_den| < ε → H(e^(jω)) ≈ 0  
  ```
- High-frequency roll-off detection for FIR/IIR filters.  
3. **Fast Evaluation via Horner’s Method**  
Optimized polynomial evaluation:  
```
H(e^(jω)) = b₀ + e^(-jω)(b₁ + e^(-jω)(b₂ + ... ))/(a₀ + e^{-jω}(a₁ + ... ))  
```
Reduces computational complexity from $O(N²)$ to $O(N)$.  


