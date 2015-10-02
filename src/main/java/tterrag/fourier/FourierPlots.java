package tterrag.fourier;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import net.miginfocom.swing.MigLayout;
import de.erichseifert.gral.data.DataTable;
import de.erichseifert.gral.plots.XYPlot;
import de.erichseifert.gral.plots.lines.DefaultLineRenderer2D;

public class FourierPlots extends JFrame
{
    @RequiredArgsConstructor
    private class CoeftListener implements DocumentListener
    {
        private final JTextField owner;

        @Override
        public void insertUpdate(DocumentEvent e)
        {
            FourierPlots.this.onBoxEdited(owner);
        }

        @Override
        public void removeUpdate(DocumentEvent e)
        {
            FourierPlots.this.onBoxEdited(owner);
        }

        @Override
        public void changedUpdate(DocumentEvent e)
        {
            // Don't care
        }
    }

    private static final long serialVersionUID = -5947732987140329400L;

    private DataTable data;
    private XYPlot plot;
    private double pct = 10000;
    private boolean lock = true;
    private JTextField txtA;
    private List<JLabel> coeftLabels;
    private List<JTextField> aFields;
    private List<JTextField> bFields;

    @SuppressWarnings({ "unchecked" })
    public FourierPlots()
    {
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        getContentPane().setLayout(new MigLayout("", "[][][][10px:50px:50px][][][][][]", "[][][]"));

        JLabel lblA_2 = new JLabel("a");
        getContentPane().add(lblA_2, "cell 4 0,alignx center");

        JLabel lblB = new JLabel("b");
        getContentPane().add(lblB, "cell 6 0,alignx center");

        JLabel lblA = new JLabel("a0:");
        getContentPane().add(lblA, "cell 0 1,alignx trailing");

        JButton btnPlot = new JButton("Plot!");
        btnPlot.setFocusable(false);
        getContentPane().add(btnPlot, "cell 1 2,alignx center,aligny center");

        txtA = new JTextField();
        txtA.setToolTipText("a0");
        getContentPane().add(txtA, "cell 1 1,alignx left");
        txtA.setColumns(10);

        JLabel rootLabel = new JLabel("1");
        getContentPane().add(rootLabel, "cell 3 1,alignx trailing");
        coeftLabels = new ArrayList<>();
        coeftLabels.add(rootLabel);
        
        aFields = new ArrayList<>();
        bFields = new ArrayList<>();

        JTextField rootAField = new JTextField();
        getContentPane().add(rootAField, "cell 4 1,growx");
        rootAField.setColumns(10);
        rootAField.getDocument().addDocumentListener(new CoeftListener(rootAField));
        aFields.add(rootAField);

        JTextField rootBField = new JTextField();
        getContentPane().add(rootBField, "cell 6 1,growx");
        rootBField.setColumns(10);
        rootBField.getDocument().addDocumentListener(new CoeftListener(rootBField));
        bFields.add(rootBField);

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
    }

    public void onBoxEdited(JTextField field)
    {
        int idx = aFields.indexOf(field);
        if (idx < 0)
        {
            idx = bFields.indexOf(field);
        }

        JTextField an = aFields.get(idx);
        JTextField bn = bFields.get(idx);

        if (idx == aFields.size() - 2 && an.getText().isEmpty() && bn.getText().isEmpty())
        {
            getContentPane().remove(coeftLabels.get(idx + 1));
            getContentPane().remove(aFields.get(idx + 1));
            getContentPane().remove(bFields.get(idx + 1));
            aFields.remove(idx + 1);
            bFields.remove(idx + 1);

            revalidate();
            repaint();
        }
        else if (idx == aFields.size() - 1 && !(an.getText().isEmpty() && bn.getText().isEmpty()))
        {
            JLabel lbl = new JLabel("" + (idx + 2));
            getContentPane().add(lbl, "cell 3 " + (idx + 2) + ",alignx trailing");
            coeftLabels.add(lbl);
            
            JTextField aField = new JTextField();
            getContentPane().add(aField, "cell 4 " + (idx + 2) + ",growx");
            aField.setColumns(10);
            aField.getDocument().addDocumentListener(new CoeftListener(aField));
            aFields.add(aField);

            JTextField bField = new JTextField();
            getContentPane().add(bField, "cell 6 " + (idx + 2) + ",growx");
            bField.setColumns(10);
            bField.getDocument().addDocumentListener(new CoeftListener(bField));
            bFields.add(bField);

            revalidate();
            repaint();
        }

        System.out.println(getContentPane().getComponentCount());
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
        //
        // DrawablePanel panel = new DrawablePanel(plots.plot);
        // plots.getContentPane().add(panel);
        //
        // plots.plot.setBounds(0, 0, 500, 500);

        while (!plots.lock)
        {
            Thread.sleep(20);
            plots.update();
        }
    }
}
