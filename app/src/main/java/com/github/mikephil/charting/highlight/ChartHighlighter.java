package com.github.mikephil.charting.highlight;

import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.interfaces.BarLineScatterCandleBubbleDataProvider;

/**
 * Created by Philipp Jahoda on 21/07/15.
 */
public class ChartHighlighter<T extends BarLineScatterCandleBubbleDataProvider> {

	/** instance of the data-provider */
	protected T mChart;

	public ChartHighlighter(T chart) {
		this.mChart = chart;
	}

	/**
	 * Returns a Highlight object corresponding to the given x- and y- touch positions in pixels.
	 * 
	 * @param x
	 * @param y
	 * @return
	 */
	public Highlight getHighlight(float x, float y) {

		int xIndex = getXIndex(x);
		if (xIndex == -Integer.MAX_VALUE)
			return null;

		int dataSetIndex = getDataSetIndex(xIndex, x, y);
		if (dataSetIndex == -Integer.MAX_VALUE)
			return null;

		return new Highlight(xIndex, dataSetIndex);
	}

	/**
	 * Returns the corresponding x-index for a given touch-position in pixels.
	 * 
	 * @param x
	 * @return
	 */
	protected int getXIndex(float x) {

		// create an array of the touch-point
		float[] pts = new float[2];
		pts[0] = x;

		// take any transformer to determine the x-axis value
		mChart.getTransformer(YAxis.AxisDependency.LEFT).pixelsToValue(pts);

		return (int) Math.round(pts[0]);
	}

	/**
	 * Returns the corresponding dataset-index for a given xIndex and xy-touch position in pixels.
	 * 
	 * @param xIndex
	 * @param x
	 * @param y
	 * @return
	 */
	protected int getDataSetIndex(int xIndex, float x, float y) {

		java.util.List<com.github.mikephil.charting.utils.SelectionDetail> valsAtIndex = getSelectionDetailsAtIndex(xIndex);

		float leftdist = com.github.mikephil.charting.utils.Utils.getMinimumDistance(valsAtIndex, y, YAxis.AxisDependency.LEFT);
		float rightdist = com.github.mikephil.charting.utils.Utils.getMinimumDistance(valsAtIndex, y, YAxis.AxisDependency.RIGHT);

		YAxis.AxisDependency axis = leftdist < rightdist ? YAxis.AxisDependency.LEFT : YAxis.AxisDependency.RIGHT;

		int dataSetIndex = com.github.mikephil.charting.utils.Utils.getClosestDataSetIndex(valsAtIndex, y, axis);

		return dataSetIndex;
	}

	/**
	 * Returns a list of SelectionDetail object corresponding to the given xIndex.
	 * 
	 * @param xIndex
	 * @return
	 */
	protected java.util.List<com.github.mikephil.charting.utils.SelectionDetail> getSelectionDetailsAtIndex(int xIndex) {

		java.util.List<com.github.mikephil.charting.utils.SelectionDetail> vals = new java.util.ArrayList<com.github.mikephil.charting.utils.SelectionDetail>();

		float[] pts = new float[2];

		for (int i = 0; i < mChart.getData().getDataSetCount(); i++) {

			com.github.mikephil.charting.data.DataSet<?> dataSet = mChart.getData().getDataSetByIndex(i);

			// dont include datasets that cannot be highlighted
			if (!dataSet.isHighlightEnabled())
				continue;

			// extract all y-values from all DataSets at the given x-index
			final float yVal = dataSet.getYValForXIndex(xIndex);
			if (yVal == Float.NaN)
				continue;

			pts[1] = yVal;

			mChart.getTransformer(dataSet.getAxisDependency()).pointValuesToPixel(pts);

			if (!Float.isNaN(pts[1])) {
				vals.add(new com.github.mikephil.charting.utils.SelectionDetail(pts[1], i, dataSet));
			}
		}

		return vals;
	}
}
