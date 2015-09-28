package tterrag.fourier;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Rectangle;

import javax.imageio.ImageIO;
import javax.swing.JFrame;

import lombok.SneakyThrows;
import de.erichseifert.gral.data.DataTable;
import de.erichseifert.gral.plots.XYPlot;
import de.erichseifert.gral.plots.lines.DefaultLineRenderer2D;
import de.erichseifert.gral.ui.DrawablePanel;

public class FourierPlots extends JFrame
{
    private static final long serialVersionUID = -5947732987140329400L;

    private DataTable data;
    private XYPlot plot;
    private double pct = 10000;
    private boolean lock = false;

    @SuppressWarnings({ "unchecked" })
    public FourierPlots()
    {
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        data = new DataTable(Double.class, Double.class);
        DataTable base = new DataTable(Double.class, Double.class);
        
        plot = new XYPlot(data);

        CoefficientTable table = new CoefficientTable(3);
        table.addCoefts(1, 0, -((5 / Math.PI) * (Math.cos(Math.PI) - 1)));
        table.addCoefts(2, 0, -((5 / (Math.PI * 2)) * (Math.cos(Math.PI * 2) - 1)));
        table.addCoefts(3, 0, -((5 / (Math.PI * 3)) * (Math.cos(Math.PI * 3) - 1)));

        FourierFunction func = new FourierFunction(5, table);

        for (double x = 0; x <= Math.PI * 8; x += Math.PI / 150)
        {
            data.add(x, func.compute(x, 3));
            base.add(x, func.compute(x, 2));
        }

        plot.add(base);
        plot.add(data);
        plot.getAxis(XYPlot.AXIS_X).setRange(-1, Math.PI * 2);

        plot.setLineRenderer(data, new DefaultLineRenderer2D());
        plot.setLineRenderer(base, new DefaultLineRenderer2D());

        plot.getPointRenderer(data).setColor(Color.WHITE);
        plot.getPointRenderer(data).setShape(new Rectangle());

        plot.getLineRenderer(data).setColor(Color.ORANGE);
        plot.getLineRenderer(base).setColor(Color.GRAY);

        plot.getPlotArea().setBackground(Color.BLACK);
        plot.getAxisRenderer(XYPlot.AXIS_X).setShapeColor(Color.WHITE);
        plot.getAxisRenderer(XYPlot.AXIS_X).setTickColor(Color.WHITE);
        plot.getAxisRenderer(XYPlot.AXIS_X).setMinorTickColor(Color.WHITE);
        plot.getAxisRenderer(XYPlot.AXIS_Y).setShapeColor(Color.WHITE);
        plot.getAxisRenderer(XYPlot.AXIS_Y).setTickSpacing(Integer.MAX_VALUE);
        
        plot.setBounds(0, 0, 100, 100);

        DrawablePanel panel = new DrawablePanel(plot);
        getContentPane().add(panel);
    }

    public void update()
    {
        pct = Math.max(5, Math.pow(pct, 0.95) - 0.1);

        if (!lock)
        {
            plot.getAxis(XYPlot.AXIS_Y).setRange(-pct + 4, pct + 2);
        }

        if (pct == 5)
        {
            lock = true;
            plot.getNavigator().setDefaultState();
            plot.getAxisRenderer(XYPlot.AXIS_Y).setTickSpacing(1);
            plot.getAxisRenderer(XYPlot.AXIS_Y).setTickColor(Color.WHITE);
            plot.getAxisRenderer(XYPlot.AXIS_Y).setMinorTickColor(Color.WHITE);
        }

        repaint();
    }

    @SneakyThrows
    public static void main(String[] args)
    {
        FourierPlots plots = new FourierPlots();
        plots.setMinimumSize(new Dimension(400, 200));
        plots.setVisible(true);
        plots.setExtendedState(plots.getExtendedState() | JFrame.MAXIMIZED_BOTH);
        plots.setIconImage(ImageIO.read(FourierPlots.class.getResourceAsStream("/icon.png")));

        while (true)
        {
            Thread.sleep(20);
            plots.update();
        }
    }
}
