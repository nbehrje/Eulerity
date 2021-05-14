package com.example.eulerity;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.Nullable;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;

public class EditView extends View {
    Paint paint = new Paint();
    Bitmap img;
    int imgWidth, imgHeight;
    Filter filter = Filter.NONE;
    String textOverlay;
    int textColor = Color.WHITE;
    int brushColor = Color.WHITE;
    Path mPath = new Path();
    ArrayList<Path> paths = new ArrayList<>();
    boolean drawing = false;

    public EditView(Context context) {
        super(context);
    }

    public EditView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public EditView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public EditView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    public void onDraw(Canvas canvas){
        super.onDraw(canvas);
        if(img == null) {
            return;
        }
        ColorMatrixColorFilter colorFilter;
        int start = (getRight()/2)-(imgWidth)/2 ;
        switch(filter){
            case GRAYSCALE:
                ColorMatrix colorMatrix = new ColorMatrix();
                colorMatrix.setSaturation(0);
                colorFilter = new ColorMatrixColorFilter(colorMatrix);
                paint = new Paint();
                paint.setColorFilter(colorFilter);
                break;
            case SEPIA:
                ColorMatrix bw = new ColorMatrix();
                bw.setSaturation(0);
                ColorMatrix rgb = new ColorMatrix();
                rgb.setScale(1f, 0.9f,0.8f,1f);
                bw.setConcat(rgb, bw);
                colorFilter = new ColorMatrixColorFilter(bw);
                paint = new Paint();
                paint.setColorFilter(colorFilter);
                break;
            case INVERT:
                ColorMatrix inverted = new ColorMatrix();
                inverted.set(new float[]
                    {      -1.0f, 0.0f, 0.0f, 0.0f, 255.0f,
                            0.0f, -1.0f, 0.0f, 0.0f, 255.0f,
                            0.0f, 0.0f, -1.0f, 0.0f, 255.0f,
                            0.0f, 0.0f, 0.0f, 1.0f, 0.0f
                    });

                ColorMatrixColorFilter filter = new ColorMatrixColorFilter(inverted);
                paint.setColorFilter(filter);
                break;
            default:
                paint = new Paint();
                break;
        }
        canvas.drawBitmap(img,start,0,paint);

        if(textOverlay != null){
            paint = new Paint();
            paint.setTextSize(160);
            paint.setColor(textColor);
            canvas.drawText(textOverlay,start, 150,paint);
        }

        if(paths.size() > 0) {
            paint = new Paint();
            paint.setColor(brushColor);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(20);
            paint.setStrokeCap(Paint.Cap.ROUND);
            for (int i = 0; i < paths.size(); i++) {
                canvas.drawPath(paths.get(i), paint);
            }
        }
    }

    public void setImage(Bitmap image){
        Bitmap copy = image.copy(Bitmap.Config.ARGB_8888, true);
        int maxWidth = getWidth()-getPaddingLeft()-getPaddingEnd();
        int maxHeight = getTop()-getBottom();
        float widthRatio = copy.getWidth() / maxWidth;
        float heightRatio = copy.getHeight() / maxHeight;

        if (widthRatio >= heightRatio) {
            imgWidth = maxWidth;
            imgHeight = (int)(((float)imgWidth / copy.getWidth()) * copy.getHeight());
        } else {
            imgHeight = maxHeight;
            imgWidth = (int)(((float)imgHeight / copy.getHeight()) * copy.getWidth());
        }

        widthRatio = (float) imgWidth / copy.getWidth();
        heightRatio = (float) imgHeight / copy.getHeight();
        float centerX = imgWidth / 2.0f;
        float centerY = imgHeight / 2.0f;
        Matrix scale = new Matrix();
        scale.setScale(widthRatio,heightRatio,centerX,centerY);

        Bitmap scaled = Bitmap.createBitmap(imgWidth, imgHeight, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(scaled);
        canvas.setMatrix(scale);
        canvas.drawBitmap(copy,
                centerX - copy.getWidth() / 2,
                centerY - copy.getHeight() / 2,
                paint);
        img = scaled;
        invalidate();
    }

    public void setText(String txt, String color){
        textOverlay = txt;
        switch(color){
            case "White":
                textColor = Color.WHITE;
                break;
            case "Black":
                textColor = Color.BLACK;
                break;
            case "Red":
                textColor = Color.RED;
                break;
            case "Blue":
                textColor = Color.BLUE;
                break;
            default:
                textColor = Color.WHITE;
                break;

        }
        invalidate();
    }

    public void setFilter(Filter f){
        filter = f;
        invalidate();
    }

    public void reset(){
        filter = Filter.NONE;
        textOverlay = null;
        paths = new ArrayList<>();
        invalidate();
    }

    public void setBrushColor(String color){
        if(color == null){
            drawing = false;
        }
        switch(color){
            case "White":
                brushColor = Color.WHITE;
                break;
            case "Black":
                brushColor = Color.BLACK;
                break;
            case "Red":
                brushColor = Color.RED;
                break;
            case "Blue":
                brushColor = Color.BLUE;
                break;
            default:
                brushColor = Color.WHITE;
                break;

        }
        drawing = true;
        invalidate();
    }

    @Override
    public boolean onTouchEvent(MotionEvent motionEvent){
        if(!drawing){
            return true;
        }
        float x = Math.max(Math.min(motionEvent.getX(), imgWidth+getPaddingStart()),getPaddingStart());
        float y = Math.min(motionEvent.getY(), imgHeight);

        switch(motionEvent.getAction()){
            case MotionEvent.ACTION_DOWN:
                mPath.moveTo(x,y);
                invalidate();
                break;
            case MotionEvent.ACTION_MOVE:
                mPath.lineTo(x, y);
                paths.add(mPath);
                mPath = new Path();
                mPath.moveTo(x,y);
                paint = new Paint();
                paint.setStyle(Paint.Style.STROKE);
                paint.setStrokeCap(Paint.Cap.ROUND);
                paint.setStrokeWidth(20);
                paint.setColor(brushColor);
                invalidate();
                break;
            case MotionEvent.ACTION_UP:
                paths.add(mPath);
                mPath = new Path();
                paint = new Paint();
                paint.setStyle(Paint.Style.STROKE);
                paint.setStrokeCap(Paint.Cap.ROUND);
                paint.setStrokeWidth(20);
                paint.setColor(brushColor);
                break;
        }
        return true;
    }

    public byte[] getBytes(){
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        img.compress(Bitmap.CompressFormat.PNG, 100, stream);
        return stream.toByteArray();
    }
}