
package com.github.mikephil.charting.renderer;

import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.XAxis.XAxisPosition;

public class XAxisRenderer extends AxisRenderer {

    protected XAxis mXAxis;

    public XAxisRenderer(com.github.mikephil.charting.utils.ViewPortHandler viewPortHandler, XAxis xAxis, com.github.mikephil.charting.utils.Transformer trans) {
        super(viewPortHandler, trans);

        this.mXAxis = xAxis;

        mAxisLabelPaint.setColor(android.graphics.Color.BLACK);
        mAxisLabelPaint.setTextAlign(android.graphics.Paint.Align.CENTER);
        mAxisLabelPaint.setTextSize(com.github.mikephil.charting.utils.Utils.convertDpToPixel(10f));
    }

    public void computeAxis(float xValAverageLength, java.util.List<String> xValues) {

        mAxisLabelPaint.setTypeface(mXAxis.getTypeface());
        mAxisLabelPaint.setTextSize(mXAxis.getTextSize());

        StringBuilder widthText = new StringBuilder();

        int max = Math.round(xValAverageLength
                + mXAxis.getSpaceBetweenLabels());

        for (int i = 0; i < max; i++) {
            widthText.append('h');
        }

        final com.github.mikephil.charting.utils.FSize labelSize = com.github.mikephil.charting.utils.Utils.calcTextSize(mAxisLabelPaint, widthText.toString());

        final float labelWidth = labelSize.width;
        final float labelHeight = com.github.mikephil.charting.utils.Utils.calcTextHeight(mAxisLabelPaint, "Q");

        final com.github.mikephil.charting.utils.FSize labelRotatedSize = com.github.mikephil.charting.utils.Utils.getSizeOfRotatedRectangleByDegrees(
                labelWidth,
                labelHeight,
                mXAxis.getLabelRotationAngle());

        mXAxis.mLabelWidth = Math.round(labelWidth);
        mXAxis.mLabelHeight = Math.round(labelHeight);
        mXAxis.mLabelRotatedWidth = Math.round(labelRotatedSize.width);
        mXAxis.mLabelRotatedHeight = Math.round(labelRotatedSize.height);

        mXAxis.setValues(xValues);
    }

    @Override
    public void renderAxisLabels(android.graphics.Canvas c) {

        if (!mXAxis.isEnabled() || !mXAxis.isDrawLabelsEnabled())
            return;

        float yoffset = mXAxis.getYOffset();

        mAxisLabelPaint.setTypeface(mXAxis.getTypeface());
        mAxisLabelPaint.setTextSize(mXAxis.getTextSize());
        mAxisLabelPaint.setColor(mXAxis.getTextColor());

        if (mXAxis.getPosition() == XAxisPosition.TOP) {

            drawLabels(c, mViewPortHandler.contentTop() - yoffset,
                    new android.graphics.PointF(0.5f, 1.0f));

        } else if (mXAxis.getPosition() == XAxisPosition.TOP_INSIDE) {

            drawLabels(c, mViewPortHandler.contentTop() + yoffset + mXAxis.mLabelRotatedHeight,
                    new android.graphics.PointF(0.5f, 1.0f));

        } else if (mXAxis.getPosition() == XAxisPosition.BOTTOM) {

            drawLabels(c, mViewPortHandler.contentBottom() + yoffset,
                    new android.graphics.PointF(0.5f, 0.0f));

        } else if (mXAxis.getPosition() == XAxisPosition.BOTTOM_INSIDE) {

            drawLabels(c, mViewPortHandler.contentBottom() - yoffset - mXAxis.mLabelRotatedHeight,
                    new android.graphics.PointF(0.5f, 0.0f));

        } else { // BOTH SIDED

            drawLabels(c, mViewPortHandler.contentTop() - yoffset,
                    new android.graphics.PointF(0.5f, 1.0f));
            drawLabels(c, mViewPortHandler.contentBottom() + yoffset,
                    new android.graphics.PointF(0.5f, 0.0f));
        }
    }

    @Override
    public void renderAxisLine(android.graphics.Canvas c) {

        if (!mXAxis.isDrawAxisLineEnabled() || !mXAxis.isEnabled())
            return;

        mAxisLinePaint.setColor(mXAxis.getAxisLineColor());
        mAxisLinePaint.setStrokeWidth(mXAxis.getAxisLineWidth());

        if (mXAxis.getPosition() == XAxisPosition.TOP
                || mXAxis.getPosition() == XAxisPosition.TOP_INSIDE
                || mXAxis.getPosition() == XAxisPosition.BOTH_SIDED) {
            c.drawLine(mViewPortHandler.contentLeft(),
                    mViewPortHandler.contentTop(), mViewPortHandler.contentRight(),
                    mViewPortHandler.contentTop(), mAxisLinePaint);
        }

        if (mXAxis.getPosition() == XAxisPosition.BOTTOM
                || mXAxis.getPosition() == XAxisPosition.BOTTOM_INSIDE
                || mXAxis.getPosition() == XAxisPosition.BOTH_SIDED) {
            c.drawLine(mViewPortHandler.contentLeft(),
                    mViewPortHandler.contentBottom(), mViewPortHandler.contentRight(),
                    mViewPortHandler.contentBottom(), mAxisLinePaint);
        }
    }

    /**
     * draws the x-labels on the specified y-position
     *
     * @param pos
     */
    protected void drawLabels(android.graphics.Canvas c, float pos, android.graphics.PointF anchor) {

        final float labelRotationAngleDegrees = mXAxis.getLabelRotationAngle();

        // pre allocate to save performance (dont allocate in loop)
        float[] position = new float[] {
                0f, 0f
        };

        for (int i = mMinX; i <= mMaxX; i += mXAxis.mAxisLabelModulus) {

            position[0] = i;

            mTrans.pointValuesToPixel(position);

            if (mViewPortHandler.isInBoundsX(position[0])) {

                String label = mXAxis.getValues().get(i);

                if (mXAxis.isAvoidFirstLastClippingEnabled()) {

                    // avoid clipping of the last
                    if (i == mXAxis.getValues().size() - 1 && mXAxis.getValues().size() > 1) {
                        float width = com.github.mikephil.charting.utils.Utils.calcTextWidth(mAxisLabelPaint, label);

                        if (width > mViewPortHandler.offsetRight() * 2
                                && position[0] + width > mViewPortHandler.getChartWidth())
                            position[0] -= width / 2;

                        // avoid clipping of the first
                    } else if (i == 0) {

                        float width = com.github.mikephil.charting.utils.Utils.calcTextWidth(mAxisLabelPaint, label);
                        position[0] += width / 2;
                    }
                }

                drawLabel(c, label, i, position[0], pos, anchor, labelRotationAngleDegrees);
            }
        }
    }

    protected void drawLabel(android.graphics.Canvas c, String label, int xIndex, float x, float y, android.graphics.PointF anchor, float angleDegrees) {
        String formattedLabel = mXAxis.getValueFormatter().getXValue(label, xIndex, mViewPortHandler);
        com.github.mikephil.charting.utils.Utils.drawText(c, formattedLabel, x, y, mAxisLabelPaint, anchor, angleDegrees);
    }

    @Override
    public void renderGridLines(android.graphics.Canvas c) {

        if (!mXAxis.isDrawGridLinesEnabled() || !mXAxis.isEnabled())
            return;

        // pre alloc
        float[] position = new float[] {
                0f, 0f
        };

        mGridPaint.setColor(mXAxis.getGridColor());
        mGridPaint.setStrokeWidth(mXAxis.getGridLineWidth());
        mGridPaint.setPathEffect(mXAxis.getGridDashPathEffect());

        android.graphics.Path gridLinePath = new android.graphics.Path();

        for (int i = mMinX; i <= mMaxX; i += mXAxis.mAxisLabelModulus) {

            position[0] = i;
            mTrans.pointValuesToPixel(position);

            if (position[0] >= mViewPortHandler.offsetLeft()
                    && position[0] <= mViewPortHandler.getChartWidth()) {

                gridLinePath.moveTo(position[0], mViewPortHandler.contentBottom());
                gridLinePath.lineTo(position[0], mViewPortHandler.contentTop());

                // draw a path because lines don't support dashing on lower android versions
                c.drawPath(gridLinePath, mGridPaint);
            }

            gridLinePath.reset();
        }
    }

	/**
	 * Draws the LimitLines associated with this axis to the screen.
	 *
	 * @param c
	 */
	@Override
	public void renderLimitLines(android.graphics.Canvas c) {

		java.util.List<LimitLine> limitLines = mXAxis.getLimitLines();

		if (limitLines == null || limitLines.size() <= 0)
			return;

        float[] position = new float[2];

		for (int i = 0; i < limitLines.size(); i++) {

			LimitLine l = limitLines.get(i);

            if(!l.isEnabled())
                continue;

            position[0] = l.getLimit();
            position[1] = 0.f;

            mTrans.pointValuesToPixel(position);

            renderLimitLineLine(c, l, position);
            renderLimitLineLabel(c, l, position, 2.f + l.getYOffset());
		}
	}

    float[] mLimitLineSegmentsBuffer = new float[4];
    private android.graphics.Path mLimitLinePath = new android.graphics.Path();

    public void renderLimitLineLine(android.graphics.Canvas c, LimitLine limitLine, float[] position)
    {
        mLimitLineSegmentsBuffer[0] = position[0];
        mLimitLineSegmentsBuffer[1] = mViewPortHandler.contentTop();
        mLimitLineSegmentsBuffer[2] = position[0];
        mLimitLineSegmentsBuffer[3] = mViewPortHandler.contentBottom();

        mLimitLinePath.reset();
        mLimitLinePath.moveTo(mLimitLineSegmentsBuffer[0], mLimitLineSegmentsBuffer[1]);
        mLimitLinePath.lineTo(mLimitLineSegmentsBuffer[2], mLimitLineSegmentsBuffer[3]);

        mLimitLinePaint.setStyle(android.graphics.Paint.Style.STROKE);
        mLimitLinePaint.setColor(limitLine.getLineColor());
        mLimitLinePaint.setStrokeWidth(limitLine.getLineWidth());
        mLimitLinePaint.setPathEffect(limitLine.getDashPathEffect());

        c.drawPath(mLimitLinePath, mLimitLinePaint);
    }

    public void renderLimitLineLabel(android.graphics.Canvas c, LimitLine limitLine, float[] position, float yOffset)
    {
        String label = limitLine.getLabel();

        // if drawing the limit-value label is enabled
        if (label != null && !label.equals("")) {

            mLimitLinePaint.setStyle(limitLine.getTextStyle());
            mLimitLinePaint.setPathEffect(null);
            mLimitLinePaint.setColor(limitLine.getTextColor());
            mLimitLinePaint.setStrokeWidth(0.5f);
            mLimitLinePaint.setTextSize(limitLine.getTextSize());

            float xOffset = limitLine.getLineWidth() + limitLine.getXOffset();

            final LimitLine.LimitLabelPosition labelPosition = limitLine.getLabelPosition();

            if (labelPosition == LimitLine.LimitLabelPosition.RIGHT_TOP) {

                final float labelLineHeight = com.github.mikephil.charting.utils.Utils.calcTextHeight(mLimitLinePaint, label);
                mLimitLinePaint.setTextAlign(android.graphics.Paint.Align.LEFT);
                c.drawText(label, position[0] + xOffset, mViewPortHandler.contentTop() + yOffset + labelLineHeight, mLimitLinePaint);
            } else if (labelPosition == LimitLine.LimitLabelPosition.RIGHT_BOTTOM) {

                mLimitLinePaint.setTextAlign(android.graphics.Paint.Align.LEFT);
                c.drawText(label, position[0] + xOffset, mViewPortHandler.contentBottom() - yOffset, mLimitLinePaint);
            } else if (labelPosition == LimitLine.LimitLabelPosition.LEFT_TOP) {

                mLimitLinePaint.setTextAlign(android.graphics.Paint.Align.RIGHT);
                final float labelLineHeight = com.github.mikephil.charting.utils.Utils.calcTextHeight(mLimitLinePaint, label);
                c.drawText(label, position[0] - xOffset, mViewPortHandler.contentTop() + yOffset + labelLineHeight, mLimitLinePaint);
            } else {

                mLimitLinePaint.setTextAlign(android.graphics.Paint.Align.RIGHT);
                c.drawText(label, position[0] - xOffset, mViewPortHandler.contentBottom() - yOffset, mLimitLinePaint);
            }
        }
    }

}
