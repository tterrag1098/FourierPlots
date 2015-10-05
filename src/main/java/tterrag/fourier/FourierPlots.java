package tterrag.fourier;

import java.awt.Color;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import net.miginfocom.swing.MigLayout;
import de.erichseifert.gral.data.DataTable;
import de.erichseifert.gral.plots.XYPlot;
import de.erichseifert.gral.plots.lines.DefaultLineRenderer2D;
import de.erichseifert.gral.ui.DrawablePanel;

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

    private JPanel userInput, graph;
    
    private DataTable dataOne, dataTwo;
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
        
        userInput = new JPanel(new MigLayout("", "20[][][][10px:50px:50px][][][][][]", "[][][]"));
        userInput.setSize(500, 300);
        
        JLabel lblA_2 = new JLabel("a");
        userInput.add(lblA_2, "cell 4 0,alignx center");

        JLabel lblB = new JLabel("b");
        userInput.add(lblB, "cell 6 0,alignx center");

        JLabel lblA = new JLabel("a0:");
        userInput.add(lblA, "cell 0 1,alignx trailing");

        JButton btnPlot = new JButton("Plot!");
        btnPlot.setFocusable(false);
        btnPlot.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				userInput.setVisible(false);
				graph.setVisible(true);
				lock = false;
			}
		});
        
        userInput.add(btnPlot, "cell 1 2,alignx center,aligny center");

        txtA = new JTextField();
        txtA.setToolTipText("a0");
        userInput.add(txtA, "cell 1 1,alignx left");
        txtA.setColumns(10);

        JLabel rootLabel = new JLabel("1");
        userInput.add(rootLabel, "cell 3 1,alignx trailing");
        coeftLabels = new ArrayList<>();
        coeftLabels.add(rootLabel);
        
        aFields = new ArrayList<>();
        bFields = new ArrayList<>();

        JTextField rootAField = new JTextField();
        userInput.add(rootAField, "cell 4 1,growx");
        rootAField.setColumns(10);
        rootAField.getDocument().addDocumentListener(new CoeftListener(rootAField));
        aFields.add(rootAField);

        JTextField rootBField = new JTextField();
        userInput.add(rootBField, "cell 6 1,growx");
        rootBField.setColumns(10);
        rootBField.getDocument().addDocumentListener(new CoeftListener(rootBField));
        bFields.add(rootBField);
        
        getContentPane().add(userInput);
        
        graph = new JPanel();
        graph.setLayout(new BoxLayout(graph, BoxLayout.X_AXIS));

        dataOne = new DataTable(Double.class, Double.class);
        dataTwo = new DataTable(Double.class, Double.class);
        dataOne.add(0D, 0D);
        dataTwo.add(0D, 0D);
        
        plot = new XYPlot(dataOne);

        plot.add(dataTwo);
        plot.add(dataOne);
        plot.getAxis(XYPlot.AXIS_X).setRange(-1, Math.PI * 2);

        plot.setLineRenderer(dataOne, new DefaultLineRenderer2D());
        plot.setLineRenderer(dataTwo, new DefaultLineRenderer2D());

        plot.getPointRenderer(dataOne).setColor(Color.WHITE);
        plot.getPointRenderer(dataOne).setShape(new Rectangle());

        plot.getLineRenderer(dataOne).setColor(Color.ORANGE);
        plot.getLineRenderer(dataTwo).setColor(Color.GRAY);

        plot.getPlotArea().setBackground(Color.BLACK);
        plot.getAxisRenderer(XYPlot.AXIS_X).setShapeColor(Color.WHITE);
        plot.getAxisRenderer(XYPlot.AXIS_X).setTickColor(Color.WHITE);
        plot.getAxisRenderer(XYPlot.AXIS_X).setMinorTickColor(Color.WHITE);
        plot.getAxisRenderer(XYPlot.AXIS_Y).setShapeColor(Color.WHITE);
        plot.getAxisRenderer(XYPlot.AXIS_Y).setTickSpacing(Integer.MAX_VALUE);

        plot.setBounds(0, 0, 100, 100);
        
        graph.add(new DrawablePanel(plot));
        getContentPane().add(graph);
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
            userInput.remove(coeftLabels.get(idx + 1));
            userInput.remove(aFields.get(idx + 1));
            userInput.remove(bFields.get(idx + 1));
            aFields.remove(idx + 1);
            bFields.remove(idx + 1);

            revalidate();
            repaint();
        }
        else if (idx == aFields.size() - 1 && !(an.getText().isEmpty() && bn.getText().isEmpty()))
        {
            JLabel lbl = new JLabel("" + (idx + 2));
            userInput.add(lbl, "cell 3 " + (idx + 2) + ",alignx trailing");
            coeftLabels.add(lbl);
            
            JTextField aField = new JTextField();
            userInput.add(aField, "cell 4 " + (idx + 2) + ",growx");
            aField.setColumns(10);
            aField.getDocument().addDocumentListener(new CoeftListener(aField));
            aFields.add(aField);

            JTextField bField = new JTextField();
            userInput.add(bField, "cell 6 " + (idx + 2) + ",growx");
            bField.setColumns(10);
            bField.getDocument().addDocumentListener(new CoeftListener(bField));
            bFields.add(bField);

            revalidate();
            repaint();
        }
    }

    public void update()
    {
        pct = Math.max(5, Math.pow(pct, 0.95) - 0.1);

        if (lock && graph.isVisible() && dataOne.getColumn(0).size() == 1)
        {        	
            CoefficientTable table = new CoefficientTable(aFields.size() - 1);
            for (int i = 0; i < aFields.size() - 1; i++)
            {
            	table.addCoefts(i + 1, Double.parseDouble(aFields.get(i).getText()), Double.parseDouble(bFields.get(i).getText()));
            }
            
            FourierFunction func = new FourierFunction(Double.parseDouble(txtA.getText()), table);

            for (double x = 0; x <= Math.PI * 8; x += Math.PI / 150)
            {
                dataOne.add(x, func.compute(x, table.getSize()));
//                dataTwo.add(x, func.compute(x, table.getSize() - 1));
            }
            
            plot.getAxis(XYPlot.AXIS_Y).setRange(-pct + 4, pct + 2);
        }
        
        if (!lock)
        {
            plot.getAxis(XYPlot.AXIS_Y).setRange(-pct + 4, pct + 2);
            
            if (pct == 5)
            {
                lock = true;
                plot.getNavigator().setDefaultState();
                plot.getAxisRenderer(XYPlot.AXIS_Y).setTickSpacing(1);
                plot.getAxisRenderer(XYPlot.AXIS_Y).setTickColor(Color.WHITE);
                plot.getAxisRenderer(XYPlot.AXIS_Y).setMinorTickColor(Color.WHITE);
            }
        }

        repaint();
    }

    @SneakyThrows
    public static void main(String[] args)
    {
        FourierPlots plots = new FourierPlots();
        plots.setSize(plots.userInput.getSize());
        plots.userInput.setVisible(true);
        plots.graph.setVisible(false);
        plots.setVisible(true);
//        plots.setExtendedState(plots.getExtendedState() | JFrame.MAXIMIZED_BOTH);
        plots.setIconImage(ImageIO.read(FourierPlots.class.getResourceAsStream("/icon.png")));
        
        //
        // DrawablePanel panel = new DrawablePanel(plots.plot);
        // plots.getContentPane().add(panel);
        //
        // plots.plot.setBounds(0, 0, 500, 500);

        while (true)
        {
            Thread.sleep(20);
            if (plots.graph.isVisible())
            {
            	plots.update();
            }
        }
    }
}
