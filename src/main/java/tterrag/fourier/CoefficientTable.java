package tterrag.fourier;

import lombok.ToString;
import lombok.Value;


@ToString
public class CoefficientTable
{
    @Value
    public static class Pair
    {
        private final double an, bn;
    }

    private Pair[] coefts;

    public CoefficientTable(int m)
    {
        coefts = new Pair[m];
    }

    public void addCoefts(int n, double an, double bn)
    {
        coefts[n - 1] = new Pair(an, bn);
    }

    public Pair getDataForM(int n)
    {
        return coefts[n - 1];
    }

    public double getAFor(int n)
    {
        return getDataForM(n).getAn();
    }

    public double getBFor(int n)
    {
        return getDataForM(n).getBn();
    }
}
