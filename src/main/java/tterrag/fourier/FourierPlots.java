package tterrag.fourier;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.LayoutFocusTraversalPolicy;
import javax.swing.UIManager;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import net.miginfocom.swing.MigLayout;

import com.google.common.collect.Lists;

import de.erichseifert.gral.data.DataTable;
import de.erichseifert.gral.plots.XYPlot;
import de.erichseifert.gral.plots.lines.DefaultLineRenderer2D;
import de.erichseifert.gral.ui.DrawablePanel;
import de.erichseifert.gral.util.Tuple;

public class FourierPlots extends JFrame {
    @RequiredArgsConstructor
    private class CoeftListener implements DocumentListener {
        private final JTextField owner;

        @Override
        public void insertUpdate(DocumentEvent e) {
            FourierPlots.this.onBoxEdited(owner);
        }

        @Override
        public void removeUpdate(DocumentEvent e) {
            FourierPlots.this.onBoxEdited(owner);
        }

        @Override
        public void changedUpdate(DocumentEvent e) {
            // Don't care
        }
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    private static class CoeftFilter extends DocumentFilter {
        public static final CoeftFilter INSTANCE = new CoeftFilter();

        @Override
        public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr) throws BadLocationException {
            if (isNumber(string)) {
                super.insertString(fb, offset, string, attr);
            } else {
                Toolkit.getDefaultToolkit().beep();
            }
        }

        @Override
        public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs) throws BadLocationException {
            if (isNumber(text)) {
                super.replace(fb, offset, length, text, attrs);
            } else {
                Toolkit.getDefaultToolkit().beep();
            }
        }

        private boolean isNumber(String string) {
            for (char c : string.toCharArray()) {
                if (!Character.isDigit(c) && c != '.' && c != '-') {
                    return false;
                }
            }
            return true;
        }

        public static void filter(JTextField f) {
            ((AbstractDocument) f.getDocument()).setDocumentFilter(CoeftFilter.INSTANCE);
        }
    }

    private static final CoefficientTable defaults = new CoefficientTable(10);
    private static final double defaultA0 = 0.25;

    static {
        defaults.addCoefts(1, .31831, .31831);
        defaults.addCoefts(2, 0, .31831);
        defaults.addCoefts(3, -.10610, .10610);
        defaults.addCoefts(4, 0, 0);
        defaults.addCoefts(5, .06366, .06366);
        defaults.addCoefts(6, 0, .10610);
        defaults.addCoefts(7, -.04547, .04547);
        defaults.addCoefts(8, 0, 0);
        defaults.addCoefts(9, .03537, .03537);
        defaults.addCoefts(10, 0, .06366);
    }

    private static final long serialVersionUID = -5947732987140329400L;

    private JScrollPane userInputScr;
    private JPanel userInput, graph, graphButtons;

    private DataTable dataOne, dataTwo;
    private XYPlot plotOne, plotTwo;
    private boolean lock = true;
    private JTextField txtA, txtN, txtRange;
    private List<JLabel> coeftLabels;
    private List<JTextField> aFields;
    private List<JTextField> bFields;

    public FourierPlots() {
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        getContentPane().setBackground(Color.BLACK);

        userInput = new JPanel(new MigLayout("", "[grow][][][10px:50px:50px][][][][10px:50px:50px][][grow]", "[][][][][]"));
        userInputScr = new JScrollPane(userInput, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        userInput.setSize(800, 600);

        JLabel lblA_2 = new JLabel("a");
        userInput.add(lblA_2, "cell 4 0,alignx center");

        JLabel lblB = new JLabel("b");
        userInput.add(lblB, "cell 6 0,alignx center");

        JLabel lblA = new JLabel("a0:");
        userInput.add(lblA, "cell 0 1,alignx trailing");

        txtA = new JTextField();
        CoeftFilter.filter(txtA);
        txtA.setToolTipText("a0");
        userInput.add(txtA, "cell 1 1,alignx left");
        txtA.setColumns(14);

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
        CoeftFilter.filter(rootAField);
        aFields.add(rootAField);

        JTextField rootBField = new JTextField();
        userInput.add(rootBField, "cell 6 1,growx");
        rootBField.setColumns(10);
        rootBField.getDocument().addDocumentListener(new CoeftListener(rootBField));
        CoeftFilter.filter(rootBField);
        bFields.add(rootBField);

        JLabel lblN = new JLabel("n:");
        userInput.add(lblN, "cell 8 1,alignx trailing");

        JLabel lblRange = new JLabel("f0:");
        userInput.add(lblRange, "cell 8 2,alignx trailing");

        txtN = new JTextField();
        CoeftFilter.filter(txtN);
        txtN.setToolTipText("The amount of harmonics to calculate. Blank for all.");
        userInput.add(txtN, "cell 9 1,alignx left");
        txtN.setColumns(10);

        txtRange = new JTextField();
        userInput.add(txtRange, "cell 9 2,alignx left");
        txtRange.setColumns(10);

        JButton btnPlot = new JButton("Plot!");
        btnPlot.setFocusable(false);
        btnPlot.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                int limit = txtN.getText().isEmpty() ? aFields.size() - 1 : Integer.parseInt(txtN.getText());
                limit = Math.min(limit, aFields.size() - 1);
                if (txtA.getText().isEmpty()) {
                    Toolkit.getDefaultToolkit().beep();
                    return;
                }
                for (JTextField t : aFields.subList(0, limit)) {
                    if (t.getText().isEmpty()) {
                        Toolkit.getDefaultToolkit().beep();
                        return;
                    }
                }
                for (JTextField t : bFields.subList(0, limit)) {
                    if (t.getText().isEmpty()) {
                        Toolkit.getDefaultToolkit().beep();
                        return;
                    }
                }
                userInput.setVisible(false);
                getContentPane().remove(userInputScr);
                getContentPane().add(graphButtons);
                getContentPane().add(graph);
                graph.setVisible(true);
                graphButtons.setVisible(true);
                lock = false;
                setExtendedState(getExtendedState() | JFrame.MAXIMIZED_BOTH);
            }
        });
        userInput.add(btnPlot, "cell 1 2,alignx center,aligny center");

        JButton btnDefaults = new JButton("Load defaults");
        btnDefaults.setFocusable(false);
        btnDefaults.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                for (int i = 0; i < defaults.getSize(); i++) {
                    aFields.get(i).setText("" + defaults.getAFor(i + 1));
                    bFields.get(i).setText("" + defaults.getBFor(i + 1));
                }
                txtA.setText("" + defaultA0);
            }
        });
        userInput.add(btnDefaults, "cell 1 3,alignx center,aligny center");

        getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));
        getContentPane().add(userInputScr);

        graph = new JPanel();
        graph.setLayout(new BoxLayout(graph, BoxLayout.X_AXIS));

        graphButtons = new JPanel();
        graphButtons.setLayout(new BorderLayout());
        graphButtons.setOpaque(true);
        graphButtons.setBackground(Color.BLACK);
        graphButtons.setMaximumSize(new Dimension(Integer.MAX_VALUE, 20));

        JButton backButton = new JButton("Go back");
        backButton.setBackground(Color.BLACK);
        backButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                getContentPane().remove(graph);
                getContentPane().remove(graphButtons);
                getContentPane().add(userInputScr);
                userInput.setVisible(true);
                graph.setVisible(false);
                zoomOut = 0;
                maxZoom = 0.5;
                initGraph();
                revalidate();
                repaint();
            }
        });

        graphButtons.add(backButton, BorderLayout.NORTH);

        initGraph();
    }

    @SuppressWarnings("unchecked")
    private void initGraph() {
        graph.removeAll();
        values.clear();
        lock = true;
        iterator = null;

        dataOne = new DataTable(Double.class, Double.class);
        dataTwo = new DataTable(Double.class, Double.class);
        dataOne.add(0D, 0D);
        dataTwo.add(0D, 0D);

        plotOne = new XYPlot(dataOne);
        plotTwo = new XYPlot(dataTwo);

        plotOne.setLineRenderer(dataOne, new DefaultLineRenderer2D());
        plotTwo.setLineRenderer(dataTwo, new DefaultLineRenderer2D());

        plotOne.getPointRenderer(dataOne).setColor(Color.WHITE);
        plotOne.getPointRenderer(dataOne).setShape(new Rectangle());

        plotTwo.getPointRenderer(dataTwo).setColor(Color.WHITE);
        plotTwo.getPointRenderer(dataTwo).setShape(new Rectangle());

        plotOne.getLineRenderer(dataOne).setColor(Color.ORANGE);
        plotTwo.getLineRenderer(dataTwo).setColor(Color.GRAY);

        plotOne.getPlotArea().setBackground(Color.BLACK);
        plotOne.getAxisRenderer(XYPlot.AXIS_X).setShapeColor(Color.WHITE);
        plotOne.getAxisRenderer(XYPlot.AXIS_X).setTickColor(Color.WHITE);
        plotOne.getAxisRenderer(XYPlot.AXIS_X).setMinorTickColor(Color.WHITE);
        plotOne.getAxisRenderer(XYPlot.AXIS_Y).setTickLabelsOutside(false);
        plotOne.getAxisRenderer(XYPlot.AXIS_Y).setShapeColor(Color.WHITE);
        plotOne.getAxisRenderer(XYPlot.AXIS_Y).setTickSpacing(Integer.MAX_VALUE);

        plotTwo.getPlotArea().setBackground(Color.BLACK);
        plotTwo.getAxisRenderer(XYPlot.AXIS_X).setShapeColor(Color.WHITE);
        plotTwo.getAxisRenderer(XYPlot.AXIS_X).setTickColor(Color.WHITE);
        plotTwo.getAxisRenderer(XYPlot.AXIS_X).setMinorTickColor(Color.WHITE);
        plotTwo.getAxisRenderer(XYPlot.AXIS_Y).setTickLabelsOutside(false);
        plotTwo.getAxisRenderer(XYPlot.AXIS_Y).setShapeColor(Color.WHITE);
        plotTwo.getAxisRenderer(XYPlot.AXIS_Y).setTickSpacing(Integer.MAX_VALUE);

        graph.add(new DrawablePanel(plotOne));
        graph.add(new DrawablePanel(plotTwo));
    }

    public void onBoxEdited(JTextField field) {
        int idx = aFields.indexOf(field);
        if (idx < 0) {
            idx = bFields.indexOf(field);
        }

        JTextField an = aFields.get(idx);
        JTextField bn = bFields.get(idx);

        if (idx == aFields.size() - 2 && an.getText().isEmpty() && bn.getText().isEmpty()) {
            userInput.remove(coeftLabels.get(idx + 1));
            userInput.remove(aFields.get(idx + 1));
            userInput.remove(bFields.get(idx + 1));
            aFields.remove(idx + 1);
            bFields.remove(idx + 1);

            revalidate();
            repaint();
        } else if (idx < 29 && idx == aFields.size() - 1 && !(an.getText().isEmpty() && bn.getText().isEmpty())) {
            JLabel lbl = new JLabel("" + (idx + 2));
            userInput.add(lbl, "cell 3 " + (idx + 2) + ",alignx trailing");
            coeftLabels.add(lbl);

            JTextField aField = new JTextField();
            userInput.add(aField, "cell 4 " + (idx + 2) + ",growx");
            aField.setColumns(10);
            aField.getDocument().addDocumentListener(new CoeftListener(aField));
            CoeftFilter.filter(aField);
            aFields.add(aField);

            JTextField bField = new JTextField();
            userInput.add(bField, "cell 6 " + (idx + 2) + ",growx");
            bField.setColumns(10);
            bField.getDocument().addDocumentListener(new CoeftListener(bField));
            CoeftFilter.filter(bField);
            bFields.add(bField);

            revalidate();
            repaint();
        }
    }

    private List<Tuple> values = Lists.newArrayList();
    private Iterator<Tuple> iterator = null;
    private double minY1, maxY1, minY2, maxY2;
    private double zoomOut = 0, maxZoom = 0.5;

    public void update() {
        int limit = txtN.getText().isEmpty() ? aFields.size() - 1 : Integer.parseInt(txtN.getText());
        limit = Math.min(limit, aFields.size() - 1);
        if (lock && graph.isVisible() && values.isEmpty()) {
            CoefficientTable table = new CoefficientTable(limit);
            // XXX Outer loop
            for (int i = 0; i < table.getSize(); i++) {
                table.addCoefts(i + 1, Double.parseDouble(aFields.get(i).getText()), Double.parseDouble(bFields.get(i).getText()));
            }

            double f0 = txtRange.getText().isEmpty() ? 1 : Double.parseDouble(txtRange.getText());
            FourierFunction func = new FourierFunction(Double.parseDouble(txtA.getText()), f0, table);

            double range = f0 * 3;
            double incr = Math.PI / 250;
            incr *= range / (Math.PI * 2);
            for (double x = 0; x <= range; x += incr) {
                double y = func.compute(x, table.getSize());
                values.add(new Tuple(x, y, func.compute(x, table.getSize() - 1)));
            }

            iterator = values.iterator();
        }

        if (iterator != null && iterator.hasNext()) {
            Tuple data = iterator.next();
            dataOne.add((Double) data.get(0), (Double) data.get(1));
            dataTwo.add((Double) data.get(0), (Double) data.get(2));
            if (!iterator.hasNext()) {
                minY1 = plotOne.getAxis(XYPlot.AXIS_Y).getMin().doubleValue();
                maxY1 = plotOne.getAxis(XYPlot.AXIS_Y).getMax().doubleValue();
                minY2 = plotTwo.getAxis(XYPlot.AXIS_Y).getMin().doubleValue();
                maxY2 = plotTwo.getAxis(XYPlot.AXIS_Y).getMax().doubleValue();
            }

            repaint();
        } else if (iterator != null && zoomOut < maxZoom) {
            plotOne.getAxis(XYPlot.AXIS_Y).setMin(minY1 - zoomOut);
            plotOne.getAxis(XYPlot.AXIS_Y).setMax(maxY1 + zoomOut);
            plotTwo.getAxis(XYPlot.AXIS_Y).setMin(minY2 - zoomOut);
            plotTwo.getAxis(XYPlot.AXIS_Y).setMax(maxY2 + zoomOut);
            zoomOut += 0.01;

            repaint();
        }

        if (!lock) {
            lock = true;
            plotOne.getNavigator().setDefaultState();
            plotOne.getAxisRenderer(XYPlot.AXIS_Y).setTickSpacing(0.5);
            plotOne.getAxisRenderer(XYPlot.AXIS_Y).setMinorTicksCount(5);
            plotOne.getAxisRenderer(XYPlot.AXIS_Y).setTickColor(Color.WHITE);
            plotOne.getAxisRenderer(XYPlot.AXIS_Y).setMinorTickColor(Color.WHITE);
            plotTwo.getNavigator().setDefaultState();
            plotTwo.getAxisRenderer(XYPlot.AXIS_Y).setTickSpacing(0.5);
            plotTwo.getAxisRenderer(XYPlot.AXIS_Y).setMinorTicksCount(5);
            plotTwo.getAxisRenderer(XYPlot.AXIS_Y).setTickColor(Color.WHITE);
            plotTwo.getAxisRenderer(XYPlot.AXIS_Y).setMinorTickColor(Color.WHITE);

            repaint();
        }
    }

    @SuppressWarnings("serial")
    @SneakyThrows
    public static void main(String[] args) {
        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

        final FourierPlots plots = new FourierPlots();
        plots.setSize(800, 600);
        plots.setLocationRelativeTo(null);
        plots.graph.setVisible(false);
        plots.userInput.setVisible(true);
        plots.userInput.setSize(new Dimension(800, 600));
        plots.setVisible(true);
        plots.setIconImage(ImageIO.read(FourierPlots.class.getResourceAsStream("/icon.png")));
        plots.setFocusCycleRoot(true);
        plots.setFocusTraversalPolicy(new LayoutFocusTraversalPolicy() {

            @Override
            protected boolean accept(Component aComponent) {
                return super.accept(aComponent) && aComponent != plots.txtN && aComponent != plots.txtRange;
            }
        });

        while (true) {
            Thread.sleep(5);
            if (plots.graph.isVisible()) {
                plots.update();
            }
        }
    }
}
