package tterrag.fourier;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class FourierFunction
{
    private final double a0;
    private final CoefficientTable table;

    public double compute(double x, int n)
    {
        return (a0 / 2) + calcA(x, n) + calcB(x, n);
    }

    private double calcA(double x, int n)
    {
        double ret = 0;
        for (int i = 1; i <= n; i++)
        {
            ret += table.getAFor(i) * Math.cos(i * x);
        }
        return ret;
    }

    private double calcB(double x, int n)
    {
        double ret = 0;
        for (int i = 1; i <= n; i++)
        {
            ret += table.getBFor(i) * Math.sin(i * x);
        }
        return ret;
    }
}
