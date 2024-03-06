package org.graffiti4;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputConnection;

import java.io.InputStream;
import java.io.InputStreamReader;

public class Graffiti4View extends View {

    private final Graffiti4 main;
    private final Graffiti4Engine engine = new Graffiti4Engine();

    private int currentWidth = 0;
    private int currentHeight = 0;
    private int middleWidth = 0;

    private final Paint brushWhite = new Paint();
    private final Path line = new Path();

    private final DisplayMetrics displaymetrics = new DisplayMetrics();

    public Graffiti4View(Context context) {
        super(context);
        brushWhite.setAntiAlias(true);
        brushWhite.setColor(Color.WHITE);
        brushWhite.setStrokeWidth(4f);
        brushWhite.setStyle(Paint.Style.STROKE);
        brushWhite.setStrokeJoin(Paint.Join.ROUND);
        brushWhite.setTextSize(36);

        setBackgroundColor(0xFFC0C0C0);
        if (context instanceof Graffiti4) {
            main = (Graffiti4) context;
        } else {
            main = null;
        }
        try {
            InputStream is = context.getAssets().open("gestures.txt");
            engine.loadFromReader(new InputStreamReader(is));
        } catch (Exception e) {
            //should never happen
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (main == null) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            return;
        }

        getDisplay().getRealMetrics(displaymetrics);
        int width = displaymetrics.widthPixels;
        int height = displaymetrics.heightPixels;

        final int desiredWidth;
        final int desiredHeight;
        if (width < height) {
            // portrait
            desiredWidth = width;
            desiredHeight = width >> 1;
        } else {
            //landscape
            desiredWidth = width;
            desiredHeight = height >> 1;
        }

        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        //Measure Width
        if (widthMode == MeasureSpec.EXACTLY) {
            //Must be this size
            width = widthSize;
        } else if (widthMode == MeasureSpec.AT_MOST) {
            //Can't be bigger than...
            width = Math.min(desiredWidth, widthSize);
        } else {
            //Be whatever you want
            width = desiredWidth;
        }

        //Measure Height
        if (heightMode == MeasureSpec.EXACTLY) {
            //Must be this size
            height = heightSize;
        } else if (heightMode == MeasureSpec.AT_MOST) {
            //Can't be bigger than...
            height = Math.min(desiredHeight, heightSize);
        } else {
            //Be whatever you want
            height = desiredHeight;
        }

        //MUST CALL THIS
        setMeasuredDimension(width, height);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        currentWidth = w;
        currentHeight = h;
        middleWidth = currentWidth / 2;
    }

    @Override
    public boolean performClick() {
        return super.performClick();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int x = (int) event.getX();
        int y = (int) event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                engine.gestureStart(x, y);
                return true;
            case MotionEvent.ACTION_MOVE:
                engine.gestureAdd(x, y);
                return true;
            case MotionEvent.ACTION_UP:
                if (main != null) {
                    InputConnection input = main.getInputConnection();
                    if (input != null) {
                        engine.gestureEnd((int) x, (int) y);
                        char code = engine.getGraffitiChar();
                        if (code != Graffiti4Engine.NULL_CHAR) {
                            Graffiti4Engine.Dimension dimension = engine.getDimension();
                            final Graffiti4Decode.Position position;
                            if (dimension.maxX < middleWidth) {
                                position = Graffiti4Decode.Position.LOWERCASE;
                            } else if (dimension.minX > middleWidth) {
                                position = Graffiti4Decode.Position.NUMBERS;
                            } else {
                                position = Graffiti4Decode.Position.UPPERCASE;
                            }

                            char c = Graffiti4Decode.decodeGraffiti(code, dotMode, position);
                            if (c > 0) {
                                if (c == '.' && !dotMode) {
                                    dotMode = true;
                                } else {
                                    if (c == Graffiti4Decode.DELETE_LEFT) {
                                        input.deleteSurroundingText(1, 0);
                                    } else if (c == Graffiti4Decode.LEFT) {
                                        input.commitText("", -1);
                                    } else if (c == Graffiti4Decode.RIGHT) {
                                        input.commitText("", 2);
                                    } else {
                                        input.commitText(String.valueOf(c), 0);
                                    }
                                    dotMode = false;
                                }
                            }
                        }
                    }
                }
                performClick();
                invalidate();
                return true;
        }
        return true;
    }

    private boolean dotMode = false;
    private int[] point = new int[2];

    @Override
    protected void onDraw(Canvas canvas) {
        if (currentHeight > 40 && currentWidth > 80) {
            final int border = 20;
            canvas.drawRoundRect(border, border, currentWidth - border, currentHeight - border, border, border, brushWhite);
            canvas.drawText(dotMode ? "./!?" : "ABC", border * 2, border * 3, brushWhite);
            canvas.drawText("123", currentWidth - border * 5, border * 3, brushWhite);

            line.reset();
            line.moveTo(middleWidth - border, border);
            line.lineTo(middleWidth, border * 3);
            line.lineTo(middleWidth + border, border);
            canvas.drawPath(line, brushWhite);

            line.reset();
            line.moveTo(middleWidth - border, currentHeight - border);
            line.lineTo(middleWidth, currentHeight - border * 3);
            line.lineTo(middleWidth + border, currentHeight - border);
            canvas.drawPath(line, brushWhite);

            {
                int i = 0, l = engine.getPointsCount();
                if (l > 0) {
                    engine.getPoint(0, point);
                    line.reset();
                    line.moveTo(point[0], point[1]);
                }
                for (i = 1; i < l; i++) {
                    engine.getPoint(i, point);
                    line.lineTo(point[0], point[1]);
                }
                canvas.drawPath(line, brushWhite);
            }
        }
    }
}
