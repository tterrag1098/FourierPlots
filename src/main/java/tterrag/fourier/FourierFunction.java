package tterrag.fourier;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class FourierFunction {
    @Getter
    private final double a0, f0;
    private final CoefficientTable table;

    private double t;

    public double compute(double x, int n) {
        t = 1 / f0;
        return (a0 / 2) + calcA(x, n) + calcB(x, n);
    }

    private double calcA(double x, int n) {
        double ret = 0;
        for (int i = 1; i <= n; i++) {
            ret += table.getAFor(i) * Math.cos(i * Math.PI * 2 * t * x);
        }
        return ret;
    }

    private double calcB(double x, int n) {
        double ret = 0;
        for (int i = 1; i <= n; i++) {
            ret += table.getBFor(i) * Math.sin(i * Math.PI * 2 * t * x);
        }
        return ret;
    }
}
