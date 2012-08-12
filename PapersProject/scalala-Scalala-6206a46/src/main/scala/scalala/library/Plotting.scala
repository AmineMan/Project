/*
 * Distributed as part of Scalala, a linear algebra library.
 *
 * Copyright (C) 2008- Daniel Ramage
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.

 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.

 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110 USA
 */
package scalala;
package library;

import generic.collection.{CanViewAsTensor1,CanViewAsTensor2};

import plotting._;

import java.awt.Color;

/**
 * Matlab-like plotting routines.
 *
 * @author dramage
 */
trait Plotting {

  /** Returns the default figure set. */
  def figures = Figures.global;

  /** Selects and returns the requested figure by index (1-based). */
  def figure(select:Int = -1)(implicit figures : Figures = figures) : Figure = {
    if (select >= 0) {
      figures.figure = select;
      figures.figure.refresh;
    }
    return figures.figure
  }

  /** Returns the current figure. */
  def figure : Figure =
    figure();

  /** Clears the current figure */
  def clf()(implicit figure : Figure = figures.figure) = {
    figure.clear;
  }

  /** Returns the current plot in the current figure. */
  def plot : XYPlot =
    figure.plot;

  /** Selects the given subplot.  Note that select is 1-based for compatibility with matlab. */
  def subplot(rows:Int,cols:Int,select:Int)(implicit figure : Figure = figure) : XYPlot = {
    figure.rows = rows;
    figure.cols = cols;
    figure.plot = select - 1;
    figure.refresh;
    return figure.plot;
  }

  /** Sets the title of this figure. */
  def title(title : String)(implicit xyplot : XYPlot = plot) {
    xyplot.title = title;
  }

  /** Sets the label of the x axis */
  def xlabel(text : String)(implicit xyplot : XYPlot = plot) {
    xyplot.xaxis.setLabel(text);
  }

  /** Sets the label of the y axis */
  def ylabel(text : String)(implicit xyplot : XYPlot = plot) {
    xyplot.yaxis.setLabel(text);
  }

  /** Sets the lower and upper bounds of the current plot. */
  def xlim(xmin : Double, xmax : Double)(implicit xyplot : XYPlot = figures.figure.plot) {
    xyplot.plot.getDomainAxis.setLowerBound(xmin);
    xyplot.plot.getDomainAxis.setUpperBound(xmax);
  }

  /** Sets the lower and upper bounds of the current plot. */
  def ylim(ymin : Double, ymax : Double)(implicit xyplot : XYPlot = figures.figure.plot) {
    xyplot.plot.getRangeAxis.setLowerBound(ymin);
    xyplot.plot.getRangeAxis.setUpperBound(ymax);
  }

  /** For re-plotting to same figure */
  def hold(state : Boolean)(implicit xyplot : XYPlot = figures.figure.plot) : Unit = {
    plot.hold = state;
  }

  /** Saves the current figure at the requested dpi to the given filename. */
  def saveas(filename : String, dpi : Int = 72)(implicit figure : Figure = figure) : Unit = {
    // make sure figure is visible or saved image will come up empty
    figure.refresh();
    
    plotting.ExportGraphics.writeFile(
      new java.io.File(filename),
      draw = figure.drawPlots _,
      width = figure.contents.getWidth,
      height = figure.contents.getHeight,
      dpi = dpi);
  }

  /**
   * Plots the given y versus the given x with the given style.
   *
   * @param x X-coordinates, co-indexed with y (and indexed by keys of type K).
   * @param y Y-coordinates, co-indexed with x (and indexed by keys of type K).
   * @param style Matlab-like style spec of the series to plot.
   * @param name Name of the series to show in the legend.
   * @param labels Optional in-graph labels for some points.
   * @param tips Optional mouse-over tooltips for some points.
   */
  def plot[K,X,XV,Y,YV]
  (x : X, y : Y, style : Char = '-', colorcode : String = null, name : String = null,
  lines : Boolean = true, shapes : Boolean = false)
   // labels : PartialFunction[K,String] = null.asInstanceOf[PartialFunction[K,String]],
   // tips : PartialFunction[K,String] = null.asInstanceOf[PartialFunction[K,String]])
  (implicit xyplot : XYPlot = figures.figure.plot,
   xtv : CanViewAsTensor1[X,K,XV], ytv : CanViewAsTensor1[Y,K,YV]) : Unit = {

    val series = xyplot.nextSeries;

    val xt = xtv(x);
    val yt = ytv(y);

    val labels : PartialFunction[K,String] = null;
    val tips : PartialFunction[K,String] = null;

    require(xt.domain == yt.domain, "x and y must have same domain");

    val items = xt.domain.toIndexedSeq;

    // initialize dataset
    val dataset = XYDataset(
      items = items,
      name = if (name == null) "Series "+series else name,
      x = (k : K) => xt.scalar.toDouble(xt(k)),
      y = (k : K) => yt.scalar.toDouble(yt(k)),
      label = (k : K) => if (labels != null && labels.isDefinedAt(k)) labels(k) else null,
      tip = (k : K) => if (tips != null && tips.isDefinedAt(k)) tips(k) else null
    );

    // initialize the series renderer
    val renderer = new org.jfree.chart.renderer.xy.XYLineAndShapeRenderer();

    if (colorcode != null) {
      val color = PaintScale.convertToColor(colorcode);
      renderer.setSeriesPaint(0, color);
      renderer.setSeriesFillPaint(0, color);
      renderer.setSeriesOutlinePaint(0, color);
    } else {
      renderer.setSeriesPaint(0, XYPlot.paint(series));
      renderer.setSeriesFillPaint(0, XYPlot.fillPaint(series));
      renderer.setSeriesOutlinePaint(0, XYPlot.outlinePaint(series));
    }
    renderer.setSeriesStroke(0, XYPlot.stroke(series));
    renderer.setSeriesShape(0, XYPlot.shape(series));
    renderer.setSeriesOutlineStroke(0, XYPlot.outlineStroke(series));

    val tooltipGenerator = new org.jfree.chart.labels.XYToolTipGenerator() {
      override def generateToolTip(dataset : org.jfree.data.xy.XYDataset,
        series : Int, item : Int) : String = {
        dataset.asInstanceOf[XYDataset[_]].getTip(series, item);
      }
    }
    renderer.setSeriesToolTipGenerator(series, tooltipGenerator);

    val labelGenerator = new org.jfree.chart.labels.XYItemLabelGenerator() {
      override def generateLabel(dataset : org.jfree.data.xy.XYDataset,
        series : Int, item : Int) : String = {
        dataset.asInstanceOf[XYDataset[_]].getLabel(series, item);
      }
    }
    renderer.setSeriesItemLabelGenerator(series, labelGenerator);
    renderer.setSeriesItemLabelsVisible(series, labels != null);

    style match {
      case '-' =>
        // default line type
        renderer.setSeriesLinesVisible(0,lines);
        renderer.setSeriesShapesVisible(0,shapes);
      case '.' =>
        renderer.setSeriesLinesVisible(0,false);
        renderer.setSeriesShapesVisible(0,true);
        renderer.setSeriesShape(0,XYPlot.dot);
      case '+' =>
        renderer.setSeriesLinesVisible(0,false);
        renderer.setSeriesShapesVisible(0,true);
        renderer.setSeriesShape(0,XYPlot.plus);
      case _ =>
        throw new IllegalArgumentException("Expected style to be one of - . or +")
    }

    // set standard tick unit back to default (non-int) if we see non-ints
    if (!xt.scalar.zero.isInstanceOf[Int]) xyplot.setXAxisDecimalTickUnits();
    if (!yt.scalar.zero.isInstanceOf[Int]) xyplot.setYAxisDecimalTickUnits();

    // add dataset and renderer to plot
    xyplot.plot.setDataset(series, dataset);
    xyplot.plot.setRenderer(series, renderer);
    xyplot.refresh();
  }

  /**
   * Displays a scatter plot of x versus y, each point drawn at the given
   * size and mapped with the given color.
   *
   * Example usage: https://gist.github.com/1288473
   *
   * @param x The x coordinates to draw as provided by anything that can be seen as a Tensor1.
   * @param y The y coordinates to draw as provided by anything that can be seen as a Tensor1.
   * @param s The size of each circle (on the same scale as the domain axis);
   * @param c A partial function (e.g. a Map) from item ids to the color to draw the bubble.
   *   Missing colors are drawn with a hashed pattern.
   * @param labels Labels to draw next to each point.
   * @param tips Tooltips to show on mouseover for each point.
   * @param name Series name for legend
   */
  def scatter[K,X,XV,Y,YV,S,SV]
  (x : X, y : Y, size : S,
   colors : PartialFunction[K,java.awt.Paint],
   labels : PartialFunction[K,String] = null.asInstanceOf[PartialFunction[K,String]],
   tips : PartialFunction[K,String] = null.asInstanceOf[PartialFunction[K,String]],
   name : String = null)
  (implicit xyplot : XYPlot = figures.figure.plot,
   xtv : CanViewAsTensor1[X,K,XV], ytv : CanViewAsTensor1[Y,K,YV],
   stv : CanViewAsTensor1[S,K,SV])
  : Unit = {

    val series = xyplot.nextSeries;

    val (xt,yt,st) = (xtv(x), ytv(y), stv(size));
    require(xt.domain == yt.domain, "x and y must have same domain");

    val items = xt.domain.toIndexedSeq;

    val paintScale = CategoricalPaintScale[K](colors);

    // initialize dataset
    val dataset = XYZDataset(
      items = items,
      name = if (name == null) "Series "+series else name,
      x = (k : K) => xt.scalar.toDouble(xt(k)),
      y = (k : K) => yt.scalar.toDouble(yt(k)),
      z = (k : K) => st.scalar.toDouble(st(k)),
      label = (k : K) => if (labels != null && labels.isDefinedAt(k)) labels(k) else null,
      tip = (k : K) => if (tips != null && tips.isDefinedAt(k)) tips(k) else null
    );

    // initialize the series renderer
    import org.jfree.chart.renderer.xy.XYBubbleRenderer;
    val renderer = new XYBubbleRenderer(XYBubbleRenderer.SCALE_ON_DOMAIN_AXIS) {;
      val stroke = new java.awt.BasicStroke(0f);
      override def getItemPaint(series : Int, item : Int) : java.awt.Paint =
        paintScale(items(item));
      override def getItemStroke(series : Int, item : Int) = stroke;
    }

    val tooltipGenerator = new org.jfree.chart.labels.XYToolTipGenerator() {
      override def generateToolTip(dataset : org.jfree.data.xy.XYDataset, series : Int, item : Int) : String = {
        dataset.asInstanceOf[XYZDataset[_]].getTip(0, item);
      }
    }
    renderer.setSeriesToolTipGenerator(series, tooltipGenerator);

    val labelGenerator = new org.jfree.chart.labels.BubbleXYItemLabelGenerator() {
      override def generateLabel(dataset : org.jfree.data.xy.XYDataset, series : Int, item : Int) : String = {
        dataset.asInstanceOf[XYZDataset[_]].getLabel(0, item);
      }
    }
    renderer.setSeriesItemLabelGenerator(series, labelGenerator);
    renderer.setSeriesItemLabelsVisible(series, labels != null);

    // set standard tick unit back to default (non-int) if we see non-ints
    if (!xt.scalar.zero.isInstanceOf[Int]) xyplot.setXAxisDecimalTickUnits();
    if (!yt.scalar.zero.isInstanceOf[Int]) xyplot.setYAxisDecimalTickUnits();

    // add dataset and renderer to plot
    xyplot.plot.setDataset(series, dataset);
    xyplot.plot.setRenderer(series, renderer);
    xyplot.refresh();
  }

//  /** An XY stacked area chart. */
//  def stacked(x : Vector, y : Seq[Vector], names : Seq[String])(implicit xyplot : XYPlot = _figures.figure.plot) {
//    import org.jfree.data.xy.{AbstractXYDataset, TableXYDataset};
//    val dataset = new AbstractXYDataset with TableXYDataset {
//      override def getX(series : Int, item : Int) = x(item);
//      override def getY(series : Int, item : Int) = y(series)(item);
//      override def getSeriesKey(series : Int) = names(series);
//      override def getSeriesCount = y.size;
//      override def getItemCount = x.size;
//      override def getItemCount(series : Int) = y(series).size;
//    };
//
//    import org.jfree.chart.renderer.xy.StackedXYAreaRenderer2;
//
//    val renderer = new StackedXYAreaRenderer2(null,null);
//    renderer.setOutline(true);
//
//    // add dataset and renderer
//    val series = xyplot.nextSeries;
//    xyplot.plot.setDataset(series,dataset);
//    xyplot.plot.setRenderer(series, renderer);
//    xyplot.refresh;
//  }
//
//  /** An XY stacked area chart with default names. */
//  def stacked(x : Vector, y : Seq[Vector])(implicit xyplot : XYPlot = _figures.figure.plot) {
//    stacked(x, y, y.iterator.zipWithIndex.map(_._2.toString).toSeq)(xyplot);
//  }

  /** Plots a histogram of the given data into the given number of bins */
  def hist[D,K,V](data : D, bins : HistogramBins = 10, name : String = "Histogram")
  (implicit xyplot : XYPlot = plot, dtv : CanViewAsTensor1[D,K,V]) : Unit = {
    val dt = dtv(data);
    implicit def dvToDouble(v : V) = dt.scalar.toDouble(v);

    val binner : StaticHistogramBins = bins match {
      case static : StaticHistogramBins => static;
      case dynamic : DynamicHistogramBins =>
        dynamic(dt.min, dt.max);
    }

    val counts = new Array[Int](binner.splits.length + 1);
    for (value <- dt.valuesIterator) {
      counts(binner.bin(value)) += 1;
    }

    val width = (binner.splits.iterator zip binner.splits.iterator.drop(1)).map(tup => tup._2 - tup._1).min;

    val dataset = new org.jfree.data.xy.XYBarDataset(
      XYDataset(
        name = name,
        items = IndexedSeq.range(0,counts.length),
        x = (i : Int) =>
          if (i == binner.splits.length) {
            binner.splits(i - 1) + width / 2.0
          } else {
            binner.splits(i) - width / 2.0
          },
        y = (i : Int) => counts(i),
        label = (i : Int) => null,
        tip = (i : Int) => null
      ), width);

    val series = xyplot.nextSeries;

    val renderer = new org.jfree.chart.renderer.xy.XYBarRenderer;
    renderer.setSeriesPaint(0, XYPlot.paint(series));
    renderer.setSeriesShape(0, XYPlot.shape(series));
    renderer.setSeriesStroke(0, XYPlot.stroke(series));
    renderer.setSeriesFillPaint(0, XYPlot.fillPaint(series));
    renderer.setSeriesOutlinePaint(0, XYPlot.outlinePaint(series));
    renderer.setSeriesOutlineStroke(0, XYPlot.outlineStroke(series));
    renderer.setShadowVisible(false);
    renderer.setBarPainter(new org.jfree.chart.renderer.xy.StandardXYBarPainter());

    xyplot.plot.getDomainAxis.setLowerBound(binner.splits(0)-width/2.0);
    xyplot.plot.getDomainAxis.setUpperBound(binner.splits(binner.splits.length-1)+width/2.0);

    // set standard tick unit back to default (non-int)
    xyplot.setXAxisDecimalTickUnits();
    xyplot.setYAxisDecimalTickUnits();

    xyplot.plot.setDataset(series,dataset);
    xyplot.plot.setRenderer(series,renderer);
    xyplot.refresh();
  }

  /**
   * Displays an image in the current figure, where each cell in the matrix
   * provides color for one square of the image.
   *
   * @param img A matrix containing the colors to plot
   * @param scale Scale used for converting matrix values to colors.
   * @param showScale If true, show the paint scale legend
   * @param name Series name
   * @param offset Offset for indexing the top-left corner of the matrix
   * @param labels Labels for some subset of the data points
   * @param tips Tooltip popups for some subset of the data points
   */
  def image[M,V]
  (img : M,
   scale : GradientPaintScale[Double] = null,
   showScale : Boolean = true,
   name : String = null,
   offset : (Int,Int) = (0,0),
   labels : PartialFunction[(Int,Int), String] = null.asInstanceOf[PartialFunction[(Int,Int), String]],
   tips : PartialFunction[(Int,Int), String] = null.asInstanceOf[PartialFunction[(Int,Int), String]])
  (implicit xyplot : XYPlot = figures.figure.plot,
   mtv : CanViewAsTensor2[M,Int,Int,V]) {

    val mt = mtv(img);

    val series = xyplot.nextSeries;

    val domain = mt.domain;
    val (minx,maxx) = (domain._2.min, domain._2.max);
    val (miny,maxy) = (domain._1.min, domain._1.max);

    val items = domain.toIndexedSeq;

    // initialize dataset
    val dataset = XYZDataset(
      items = items,
      name = if (name == null) "Series "+series else name,
      x = (k : (Int,Int)) => k._2 + offset._2,
      y = (k : (Int,Int)) => k._1 + offset._1,
      z = (k : (Int,Int)) => mt.scalar.toDouble(mt(k._1, k._2)),
      label = (k : (Int,Int)) => if (labels != null && labels.isDefinedAt(k)) labels(k) else null,
      tip = (k : (Int,Int)) => if (tips != null && tips.isDefinedAt(k)) tips(k) else null
    );

    // initialize renderer
    import org.jfree.chart.renderer.xy.XYBlockRenderer
    val renderer = new XYBlockRenderer();
    renderer.setSeriesPaint(0, XYPlot.paint(series));
    renderer.setSeriesShape(0, XYPlot.shape(series));
    renderer.setSeriesStroke(0, XYPlot.stroke(series));
    renderer.setSeriesFillPaint(0, XYPlot.fillPaint(series));
    renderer.setSeriesOutlinePaint(0, XYPlot.outlinePaint(series));
    renderer.setSeriesOutlineStroke(0, XYPlot.outlineStroke(series));

    val tooltipGenerator = new org.jfree.chart.labels.XYToolTipGenerator() {
      override def generateToolTip(dataset : org.jfree.data.xy.XYDataset, series : Int, item : Int) : String = {
        dataset.asInstanceOf[XYZDataset[_]].getTip(series, item);
      }
    }
    renderer.setSeriesToolTipGenerator(series, tooltipGenerator);

    val labelGenerator = new org.jfree.chart.labels.XYItemLabelGenerator() {
      override def generateLabel(dataset : org.jfree.data.xy.XYDataset, series : Int, item : Int) : String = {
        dataset.asInstanceOf[XYZDataset[_]].getLabel(series, item);
      }
    }
    renderer.setSeriesItemLabelGenerator(series, labelGenerator);
    renderer.setSeriesItemLabelsVisible(series, labels != null);

    val staticScale = {
      if (scale == null)
        GradientPaintScaleFactory[Double]().apply(mt.values.toList.map(mt.scalar.toDouble)).asInstanceOf[GradientPaintScale[Double]]
      else
        scale
    }
    
//    val staticScale : StaticPaintScale = scale match {
//      case static : StaticPaintScale =>
//        static;
//
//      case dynamic : DynamicPaintScale => {
//        val values = items.view.map(item =>
//          mt.scalar.toDouble(mt(item._1, item._2))).filter(!_.isNaN);
//        if (values.isEmpty) {
//          dynamic(lower = Double.NaN, upper = Double.NaN);
//        } else {
//          dynamic(lower = values.min, upper = values.max);
//        }
//      }
//    }

    val paintScale = new org.jfree.chart.renderer.PaintScale {
      override def getLowerBound = staticScale.lower;
      override def getUpperBound = staticScale.upper;
      override def getPaint(value : Double) =
        staticScale(value);
    }

    renderer.setPaintScale(paintScale);
    renderer.setBlockAnchor(org.jfree.ui.RectangleAnchor.BOTTOM_LEFT);
    renderer.setBlockWidth(1);
    renderer.setBlockHeight(1);

    xyplot.plot.getRangeAxis.setInverted(true);
    xyplot.plot.getRangeAxis.setLowerBound(miny+offset._2);
    xyplot.plot.getRangeAxis.setUpperBound(maxy+1+offset._2);
    xyplot.plot.getRangeAxis.setStandardTickUnits(XYPlot.integerTickUnits);
    
    xyplot.plot.getDomainAxis.setLowerBound(minx+offset._1);
    xyplot.plot.getDomainAxis.setUpperBound(maxx+1+offset._1);
    xyplot.plot.getDomainAxis.setStandardTickUnits(XYPlot.integerTickUnits);

    // set legend
    if (showScale) {
      val legendAxis = new org.jfree.chart.axis.NumberAxis();
      legendAxis.setLowerBound(staticScale.lower);
      legendAxis.setUpperBound(staticScale.upper);

      val legend = new org.jfree.chart.title.PaintScaleLegend(paintScale, legendAxis);
      import org.jfree.chart.title._;
      import org.jfree.ui._;
      import org.jfree.chart.block._;
      legend.setMargin(new RectangleInsets(40, 2, 40, 2));
      legend.setPadding(new RectangleInsets(10, 2, 10, 2));
      legend.setBorder(1,1,1,1);
      legend.setBackgroundPaint(Color.white);
      legend.setPosition(RectangleEdge.RIGHT);
      xyplot.chart.addSubtitle(legend);
    }

    // add dataset and renderer to plot
    xyplot.plot.setDataset(series, dataset);
    xyplot.plot.setRenderer(series, renderer);
    xyplot.refresh();
  }
}

/**
 * An object with access to the Plotting trait members.
 *
 * @author dramage
 */
object Plotting extends Plotting {
}

