package graphs;

import java.util.HashMap;
import java.util.Observable;
import java.util.Observer;

import org.jfree.data.time.*;
import org.jfree.ui.ApplicationFrame;
import java.awt.Color;
import java.awt.BasicStroke;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.ui.ApplicationFrame;
import org.jfree.ui.RefineryUtilities;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Random;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.Timer;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.time.DynamicTimeSeriesCollection;
import org.jfree.data.time.Second;
import org.jfree.data.xy.XYDataset;
import org.jfree.ui.ApplicationFrame;
import org.jfree.ui.RefineryUtilities;
import java.sql.Date;


/**
 * Created by david on 24/04/16.
 */
public class AccelerationGraph extends ApplicationFrame implements Observer  {

    TimeSeriesCollection dataset;
    TimeSeries series;

    public AccelerationGraph() {

        super("Acceleration over time");
        this.series = new TimeSeries("Acceleration over time");
        this.dataset = new TimeSeriesCollection(this.series);

        final JFreeChart chart = ChartFactory.createTimeSeriesChart(
                "Acceleration over time",
                "Time(s)",
                "Acceleration(m/s^2)",
                dataset,
                true,
                true,
                false
        );

        final XYPlot plot = chart.getXYPlot();
        ValueAxis axis = plot.getDomainAxis();
        axis.setAutoRange(true);
        axis.setFixedAutoRange(30000.0); // 30 seconds
        axis = plot.getRangeAxis();
        axis.setRange(0.0, 200.0);

        final ChartPanel chartPanel = new ChartPanel(chart);
        final JPanel content = new JPanel(new BorderLayout());
        content.add(chartPanel);

        chartPanel.setPreferredSize(new java.awt.Dimension(500, 270));
        setContentPane(content);

        this.pack();
        RefineryUtilities.centerFrameOnScreen(this);
        this.setVisible(true);

        //dataset.setTimeBase(new Second(0, 0, 0, 1, 1, 2011));
        //dataset.addSeries(gaussianData(), 0, "Gaussian data");

        /*this.xylineChart = ChartFactory.createXYLineChart(
                "Acceleration over time",
                "Time(s)",
                "Acceleration(m/s^2)",
                //new XYSeriesCollection(),
                dataset,
                PlotOrientation.VERTICAL,
                true, true, false);

        ChartPanel chartPanel = new ChartPanel( xylineChart );
        chartPanel.setPreferredSize( new java.awt.Dimension( 560 , 367 ) );
        final XYPlot plot = xylineChart.getXYPlot( );
        XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer( );
        renderer.setSeriesPaint( 0 , Color.RED );
        renderer.setSeriesStroke( 0 , new BasicStroke( 4.0f ) );
        plot.setRenderer( renderer );
        setContentPane( chartPanel );

        this.pack( );
        RefineryUtilities.centerFrameOnScreen( this );
        this.setVisible( true );*/

    }

    @Override
    public void update(Observable o, Object arg) {

        HashMap<String, Float> currentSimParams = (HashMap<String, Float>) arg;
        float currentAcceleration = currentSimParams.get("acceleration");
        float currentTime = currentSimParams.get("time");
        //long currentTimeLong = (long)currentTime;

        this.series.add(new Second(), currentAcceleration);


        //this.series.add(new Millisecond(new Date(currentTimeLong)), currentAcceleration);

       /* this.dataset.advanceTime();
        float[] newData = new float[1];
        newData[0] = 10f;
        dataset.appendData(newData);*/


    }

}