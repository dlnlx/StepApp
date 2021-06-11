
package com.github.mikephil.charting.utils;

/**
 * Transformer class that contains all matrices and is responsible for
 * transforming values into pixels on the screen and backwards.
 *
 * @author Philipp Jahoda
 */
public class Transformer {

    /**
     * matrix to map the values to the screen pixels
     */
    protected android.graphics.Matrix mMatrixValueToPx = new android.graphics.Matrix();

    /**
     * matrix for handling the different offsets of the chart
     */
    protected android.graphics.Matrix mMatrixOffset = new android.graphics.Matrix();

    protected ViewPortHandler mViewPortHandler;

    public Transformer(ViewPortHandler viewPortHandler) {
        this.mViewPortHandler = viewPortHandler;
    }

    /**
     * Prepares the matrix that transforms values to pixels. Calculates the
     * scale factors from the charts size and offsets.
     *
     * @param xChartMin
     * @param deltaX
     * @param deltaY
     * @param yChartMin
     */
    public void prepareMatrixValuePx(float xChartMin, float deltaX, float deltaY, float yChartMin) {

        float scaleX = (float) ((mViewPortHandler.contentWidth()) / deltaX);
        float scaleY = (float) ((mViewPortHandler.contentHeight()) / deltaY);

        // setup all matrices
        mMatrixValueToPx.reset();
        mMatrixValueToPx.postTranslate(-xChartMin, -yChartMin);
        mMatrixValueToPx.postScale(scaleX, -scaleY);
    }

    /**
     * Prepares the matrix that contains all offsets.
     *
     * @param inverted
     */
    public void prepareMatrixOffset(boolean inverted) {

        mMatrixOffset.reset();

        // offset.postTranslate(mOffsetLeft, getHeight() - mOffsetBottom);

        if (!inverted)
            mMatrixOffset.postTranslate(mViewPortHandler.offsetLeft(),
                    mViewPortHandler.getChartHeight() - mViewPortHandler.offsetBottom());
        else {
            mMatrixOffset
                    .setTranslate(mViewPortHandler.offsetLeft(), -mViewPortHandler.offsetTop());
            mMatrixOffset.postScale(1.0f, -1.0f);
        }

        // mMatrixOffset.set(offset);

        // mMatrixOffset.reset();
        //
        // mMatrixOffset.postTranslate(mOffsetLeft, getHeight() -
        // mOffsetBottom);
    }

    /**
     * Transforms an List of Entry into a float array containing the x and
     * y values transformed with all matrices for the SCATTERCHART.
     *
     * @param entries
     * @return
     */
    public float[] generateTransformedValuesScatter(java.util.List<? extends com.github.mikephil.charting.data.Entry> entries,
                                                    float phaseY) {

        float[] valuePoints = new float[entries.size() * 2];

        for (int j = 0; j < valuePoints.length; j += 2) {

            com.github.mikephil.charting.data.Entry e = entries.get(j / 2);

            if (e != null) {
                valuePoints[j] = e.getXIndex();
                valuePoints[j + 1] = e.getVal() * phaseY;
            }
        }

        getValueToPixelMatrix().mapPoints(valuePoints);

        return valuePoints;
    }

    /**
     * Transforms an List of Entry into a float array containing the x and
     * y values transformed with all matrices for the BUBBLECHART.
     *
     * @param entries
     * @return
     */
    public float[] generateTransformedValuesBubble(java.util.List<? extends com.github.mikephil.charting.data.Entry> entries,
                                                   float phaseX, float phaseY, int from, int to) {

        final int count = (int) Math.ceil(to - from) * 2; // (int) Math.ceil((to - from) * phaseX) * 2;

        float[] valuePoints = new float[count];

        for (int j = 0; j < count; j += 2) {

            com.github.mikephil.charting.data.Entry e = entries.get(j / 2 + from);

            if (e != null) {
                valuePoints[j] = (float) (e.getXIndex() - from) * phaseX + from;
                valuePoints[j + 1] = e.getVal() * phaseY;
            }
        }

        getValueToPixelMatrix().mapPoints(valuePoints);

        return valuePoints;
    }

    /**
     * Transforms an List of Entry into a float array containing the x and
     * y values transformed with all matrices for the LINECHART.
     *
     * @param entries
     * @return
     */
    public float[] generateTransformedValuesLine(java.util.List<? extends com.github.mikephil.charting.data.Entry> entries,
                                                 float phaseX, float phaseY, int from, int to) {

        final int count = (int) Math.ceil((to - from) * phaseX) * 2;

        float[] valuePoints = new float[count];

        for (int j = 0; j < count; j += 2) {

            com.github.mikephil.charting.data.Entry e = entries.get(j / 2 + from);

            if (e != null) {
                valuePoints[j] = e.getXIndex();
                valuePoints[j + 1] = e.getVal() * phaseY;
            }
        }

        getValueToPixelMatrix().mapPoints(valuePoints);

        return valuePoints;
    }

    /**
     * Transforms an List of Entry into a float array containing the x and
     * y values transformed with all matrices for the CANDLESTICKCHART.
     *
     * @param entries
     * @return
     */
    public float[] generateTransformedValuesCandle(java.util.List<com.github.mikephil.charting.data.CandleEntry> entries,
                                                   float phaseX, float phaseY, int from, int to) {

        final int count = (int) Math.ceil((to - from) * phaseX) * 2;

        float[] valuePoints = new float[count];

        for (int j = 0; j < count; j += 2) {

            com.github.mikephil.charting.data.CandleEntry e = entries.get(j / 2 + from);

            if (e != null) {
                valuePoints[j] = e.getXIndex();
                valuePoints[j + 1] = e.getHigh() * phaseY;
            }
        }

        getValueToPixelMatrix().mapPoints(valuePoints);

        return valuePoints;
    }

    /**
     * Transforms an List of Entry into a float array containing the x and
     * y values transformed with all matrices for the BARCHART.
     *
     * @param entries
     * @param dataSet the dataset index
     * @return
     */
    public float[] generateTransformedValuesBarChart(java.util.List<? extends com.github.mikephil.charting.data.Entry> entries,
                                                     int dataSet, com.github.mikephil.charting.data.BarData bd, float phaseY) {

        float[] valuePoints = new float[entries.size() * 2];

        int setCount = bd.getDataSetCount();
        float space = bd.getGroupSpace();

        for (int j = 0; j < valuePoints.length; j += 2) {

            com.github.mikephil.charting.data.Entry e = entries.get(j / 2);
            int i = e.getXIndex();

            // calculate the x-position, depending on datasetcount
            float x = e.getXIndex() + i * (setCount - 1) + dataSet + space * i
                    + space / 2f;
            float y = e.getVal();

            valuePoints[j] = x;
            valuePoints[j + 1] = y * phaseY;
        }

        getValueToPixelMatrix().mapPoints(valuePoints);

        return valuePoints;
    }

    /**
     * Transforms an List of Entry into a float array containing the x and
     * y values transformed with all matrices for the BARCHART.
     *
     * @param entries
     * @param dataSet the dataset index
     * @return
     */
    public float[] generateTransformedValuesHorizontalBarChart(java.util.List<? extends com.github.mikephil.charting.data.Entry> entries,
                                                               int dataSet, com.github.mikephil.charting.data.BarData bd, float phaseY) {

        float[] valuePoints = new float[entries.size() * 2];

        int setCount = bd.getDataSetCount();
        float space = bd.getGroupSpace();

        for (int j = 0; j < valuePoints.length; j += 2) {

            com.github.mikephil.charting.data.Entry e = entries.get(j / 2);
            int i = e.getXIndex();

            // calculate the x-position, depending on datasetcount
            float x = i + i * (setCount - 1) + dataSet + space * i
                    + space / 2f;
            float y = e.getVal();

            valuePoints[j] = y * phaseY;
            valuePoints[j + 1] = x;
        }

        getValueToPixelMatrix().mapPoints(valuePoints);

        return valuePoints;
    }

    /**
     * transform a path with all the given matrices VERY IMPORTANT: keep order
     * to value-touch-offset
     *
     * @param path
     */
    public void pathValueToPixel(android.graphics.Path path) {

        path.transform(mMatrixValueToPx);
        path.transform(mViewPortHandler.getMatrixTouch());
        path.transform(mMatrixOffset);
    }

    /**
     * Transforms multiple paths will all matrices.
     *
     * @param paths
     */
    public void pathValuesToPixel(java.util.List<android.graphics.Path> paths) {

        for (int i = 0; i < paths.size(); i++) {
            pathValueToPixel(paths.get(i));
        }
    }

    /**
     * Transform an array of points with all matrices. VERY IMPORTANT: Keep
     * matrix order "value-touch-offset" when transforming.
     *
     * @param pts
     */
    public void pointValuesToPixel(float[] pts) {

        mMatrixValueToPx.mapPoints(pts);
        mViewPortHandler.getMatrixTouch().mapPoints(pts);
        mMatrixOffset.mapPoints(pts);
    }

    /**
     * Transform a rectangle with all matrices.
     *
     * @param r
     */
    public void rectValueToPixel(android.graphics.RectF r) {

        mMatrixValueToPx.mapRect(r);
        mViewPortHandler.getMatrixTouch().mapRect(r);
        mMatrixOffset.mapRect(r);
    }

    /**
     * Transform a rectangle with all matrices with potential animation phases.
     *
     * @param r
     * @param phaseY
     */
    public void rectValueToPixel(android.graphics.RectF r, float phaseY) {

        // multiply the height of the rect with the phase
        r.top *= phaseY;
        r.bottom *= phaseY;

        mMatrixValueToPx.mapRect(r);
        mViewPortHandler.getMatrixTouch().mapRect(r);
        mMatrixOffset.mapRect(r);
    }

    /**
     * Transform a rectangle with all matrices with potential animation phases.
     *
     * @param r
     */
    public void rectValueToPixelHorizontal(android.graphics.RectF r) {

        mMatrixValueToPx.mapRect(r);
        mViewPortHandler.getMatrixTouch().mapRect(r);
        mMatrixOffset.mapRect(r);
    }

    /**
     * Transform a rectangle with all matrices with potential animation phases.
     *
     * @param r
     * @param phaseY
     */
    public void rectValueToPixelHorizontal(android.graphics.RectF r, float phaseY) {

        // multiply the height of the rect with the phase
        r.left *= phaseY;
        r.right *= phaseY;

        mMatrixValueToPx.mapRect(r);
        mViewPortHandler.getMatrixTouch().mapRect(r);
        mMatrixOffset.mapRect(r);
    }

    /**
     * transforms multiple rects with all matrices
     *
     * @param rects
     */
    public void rectValuesToPixel(java.util.List<android.graphics.RectF> rects) {

        android.graphics.Matrix m = getValueToPixelMatrix();

        for (int i = 0; i < rects.size(); i++)
            m.mapRect(rects.get(i));
    }

    /**
     * Transforms the given array of touch positions (pixels) (x, y, x, y, ...)
     * into values on the chart.
     *
     * @param pixels
     */
    public void pixelsToValue(float[] pixels) {

        android.graphics.Matrix tmp = new android.graphics.Matrix();

        // invert all matrixes to convert back to the original value
        mMatrixOffset.invert(tmp);
        tmp.mapPoints(pixels);

        mViewPortHandler.getMatrixTouch().invert(tmp);
        tmp.mapPoints(pixels);

        mMatrixValueToPx.invert(tmp);
        tmp.mapPoints(pixels);
    }

    /**
     * Returns the x and y values in the chart at the given touch point
     * (encapsulated in a PointD). This method transforms pixel coordinates to
     * coordinates / values in the chart. This is the opposite method to
     * getPixelsForValues(...).
     *
     * @param x
     * @param y
     * @return
     */
    public PointD getValuesByTouchPoint(float x, float y) {

        // create an array of the touch-point
        float[] pts = new float[2];
        pts[0] = x;
        pts[1] = y;

        pixelsToValue(pts);

        double xTouchVal = pts[0];
        double yTouchVal = pts[1];

        return new PointD(xTouchVal, yTouchVal);
    }

    public android.graphics.Matrix getValueMatrix() {
        return mMatrixValueToPx;
    }

    public android.graphics.Matrix getOffsetMatrix() {
        return mMatrixOffset;
    }

    private android.graphics.Matrix mMBuffer1 = new android.graphics.Matrix();

    public android.graphics.Matrix getValueToPixelMatrix() {
        mMBuffer1.set(mMatrixValueToPx);
        mMBuffer1.postConcat(mViewPortHandler.mMatrixTouch);
        mMBuffer1.postConcat(mMatrixOffset);
        return mMBuffer1;
    }

    private android.graphics.Matrix mMBuffer2 = new android.graphics.Matrix();

    public android.graphics.Matrix getPixelToValueMatrix() {
        getValueToPixelMatrix().invert(mMBuffer2);
        return mMBuffer2;
    }
}
