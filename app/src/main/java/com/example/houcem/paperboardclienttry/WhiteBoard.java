package com.example.houcem.paperboardclienttry;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.Path.Direction;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by houcem on 15/08/16.
 */
public class WhiteBoard extends View implements OnTouchListener {

    private DrawNotifier tracker;
    private List<Point> points = new ArrayList<Point>();
    private List<Point> newPoints = new ArrayList<>();
    public void setPoints(  List<Point> points){
        this.points = points;
    }
    private Paint mLinePaint;

    public WhiteBoard(Context context) {
        super(context);
        init();
    }

    public WhiteBoard(Context context, AttributeSet attrs, List<Point> Points) {
        this(context, attrs);
        this.points = Points;



    }






    public WhiteBoard(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray a = context.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.WhiteBoard,
                0, 0);
        try {

        } finally {
            a.recycle();
        }
        setFocusable(true);
        setFocusableInTouchMode(true);
        this.setOnTouchListener(this);
        init();


    }

    private void init() {
        mLinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mLinePaint.setColor(getResources().getColor(R.color.Black));
        mLinePaint.setStyle(Style.FILL);


    }




    @Override
    protected void onDraw(Canvas canvas) {
        Path path = new Path();
        for (Point point : points) {

            path.addCircle(point.x, point.y, 10, Direction.CW);
        }
        canvas.drawPath(path, mLinePaint);

    }

    public boolean onTouch(View view, MotionEvent event) {
        switch(event.getAction()){
            case MotionEvent.ACTION_DOWN:{
                newPoints.clear();
                Point point = new Point();
                point.x = event.getX();
                point.y = event.getY();
                newPoints.add(point);
                points.add(point);



            }
            case MotionEvent.ACTION_UP : {
                Point point = new Point();
                point.x = event.getX();
                point.y = event.getY();
                points.add(point);
                newPoints.add(point);
                tracker.DrawComplete(newPoints);

            }
            default:{
                Point point = new Point();
                point.x = event.getX();
                point.y = event.getY();
                points.add(point);
                newPoints.add(point);

            }



        }

        return true;
    }


    public void register(DrawNotifier tracker){
        this.tracker = tracker;
    }

}



class Point {
    public float x, y;
}

