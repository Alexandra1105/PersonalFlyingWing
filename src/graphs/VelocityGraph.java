package graphs;

import java.util.HashMap;
import java.util.Observable;
import java.util.Observer;

import org.jfree.data.time.*;
import org.jfree.ui.ApplicationFrame;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.ui.RefineryUtilities;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.data.time.Second;

import java.awt.BorderLayout;

import javax.swing.JPanel;

/**
 * Created by david on 24/04/16.
 */
public class VelocityGraph extends ApplicationFrame implements Observer  {

    TimeSeries series;

    public VelocityGraph() {

        super("Velocity over time");
        this.series = new TimeSeries("Velocity over time");
        TimeSeriesCollection dataset = new TimeSeriesCollection(this.series);

        final JFreeChart chart = ChartFactory.createTimeSeriesChart(
                "Velocity over time",
                "Time(s)",
                "Velocity(m/s)",
                dataset
        );

        final XYPlot plot = chart.getXYPlot();
        ValueAxis axis = plot.getDomainAxis();
        axis.setAutoRange(true);
        axis.setFixedAutoRange(10000.0); // 10 seconds

        axis = plot.getRangeAxis();
        axis.setRange(0.0, 200.0); // Velocity

        final ChartPanel chartPanel = new ChartPanel(chart);
        final JPanel content = new JPanel(new BorderLayout());
        content.add(chartPanel);

        chartPanel.setPreferredSize(new java.awt.Dimension(500, 270));
        setContentPane(content);

        this.pack();
        RefineryUtilities.centerFrameOnScreen(this);
        this.setVisible(true);

    }

    @Override
    public void update(Observable o, Object arg) {

        HashMap<String, Float> currentSimParams = (HashMap<String, Float>) arg;
        float currentVelocity = currentSimParams.get("velocity");

        this.series.add(new Second(), currentVelocity);

    }

}