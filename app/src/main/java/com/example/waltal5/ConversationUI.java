package com.example.waltal5;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Queue;

public class ConversationUI extends View {
    private Canvas canvas;
    private int myVoiceAmplitude = 0;
    private int friendsVoiceAmplitude = 0;
    private int voiceAnimGate = 96;
    ArrayDeque<JSONObject> arcsQueue = new ArrayDeque<>();
    int elementsToRemove = 0;
    public ConversationUI(Context context) {
        super(context);
        init(null);
    }
    public void init(@Nullable AttributeSet set){   }

    public ConversationUI(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public ConversationUI(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public ConversationUI(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(attrs);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        Paint voiceCircle = new Paint();
        Paint voiceArc = new Paint();
        Paint voiceLabelPaint = new Paint();

        voiceCircle.setColor(Color.rgb(200,255,200));
        voiceArc.setStrokeWidth(40);
        voiceArc.setStyle(Paint.Style.STROKE);
        voiceLabelPaint.setColor(Color.rgb(150,150,150));
        voiceLabelPaint.setTextSize(50);





        if (myVoiceAmplitude>=voiceAnimGate)
        {
            JSONObject arc = new JSONObject();
            try {
                arc.put("rad", 0);
                arc.put("tran", myVoiceAmplitude*2-2);
                arc.put("who","me");
                arc.put("amp",myVoiceAmplitude);

            } catch (JSONException e) {
                e.printStackTrace();
            }

            arcsQueue.add(arc);
            Log.d("voice", "300+");
            myVoiceAmplitude=0;
        }

        if(friendsVoiceAmplitude>=voiceAnimGate)
        {
            JSONObject arc = new JSONObject();
            try {
                arc.put("rad", 0);
                arc.put("tran", friendsVoiceAmplitude*2-2);
                arc.put("who","friend");
                arc.put("amp",friendsVoiceAmplitude);

            } catch (JSONException e) {
                e.printStackTrace();
            }

            arcsQueue.add(arc);
            friendsVoiceAmplitude=0;
        }

        //ArrayDeque<JSONObject> clone = new ArrayDeque<JSONObject>(arcsQueue);
        for (JSONObject arc: new ArrayDeque<JSONObject>(arcsQueue)) {
            try {
                voiceArc.setColor(Color.argb(arc.getInt("tran"),200,255,200));
                if(arc.getString("who").equals("friend"))
                {

                    canvas.drawArc(((this.getWidth()/2)-70)-arc.getInt("rad"),-70-arc.getInt("rad"),((this.getWidth()/2)+70)+arc.getInt("rad"),70+arc.getInt("rad"),0,360,false,voiceArc);
                }
                else
                {

                    canvas.drawArc(((this.getWidth()/2)-70)-arc.getInt("rad"),(this.getHeight()-70)-arc.getInt("rad"),((this.getWidth()/2)+70)+arc.getInt("rad"),(this.getHeight()+70)+arc.getInt("rad"),0,360,false,voiceArc);
                }
                arc.put("rad", (arc.getInt("rad"))+10);
                arc.put("tran", (arc.getInt("tran"))-10);
                if (arc.getInt("tran")<=0)
                {

                    arcsQueue.remove(arc);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        //Log.d("deque", arcsQueue.toString());
        canvas.drawCircle(this.getWidth()/2,0,100,voiceCircle);
        canvas.drawCircle(this.getWidth()/2,this.getHeight(),100,voiceCircle);
        canvas.drawText("friend",this.getWidth()/2-50,30,voiceLabelPaint);
        canvas.drawText("me",this.getWidth()/2-40,this.getHeight()-20,voiceLabelPaint);


        invalidate();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        setMyVoiceAmplitude(255);
        return super.onTouchEvent(event);

    }

    void setMyVoiceAmplitude(int vA){
        this.myVoiceAmplitude = vA;
    }

    void setFriendsVoiceAmplitude(int vA){
        this.friendsVoiceAmplitude = vA;
    }
}
