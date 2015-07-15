package com.softtanck.bezierflower.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.view.View;
import android.util.AttributeSet;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.AnimatorListenerAdapter;
import com.nineoldandroids.animation.AnimatorSet;
import com.nineoldandroids.animation.ObjectAnimator;
import com.nineoldandroids.animation.TypeEvaluator;
import com.nineoldandroids.animation.ValueAnimator;
import com.nineoldandroids.view.ViewHelper;
import com.softtanck.bezierflower.R;

import java.util.Random;

/**
 * @author : Tanck
 * @Description : TODO
 * @date 7/15/2015
 */
public class BezierFlowerView extends RelativeLayout {
    private int mWidth;
    private int mHeight;
    private Drawable red;
    private Drawable yellow;
    private Drawable blue;

    private Drawable[] drawables;
    private int dHeight;
    private int dWidth;
    private LayoutParams layoutParams;
    private Random rand;
    private Interpolator line = new LinearInterpolator();//线性
    private Interpolator acc = new AccelerateInterpolator();//加速
    private Interpolator dce = new DecelerateInterpolator();//减速
    private Interpolator accdec = new AccelerateDecelerateInterpolator();//先加速后减速
    // 在init中初始化
    private Interpolator[] interpolators;

    public BezierFlowerView(Context context) {
        this(context, null);
    }

    public BezierFlowerView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BezierFlowerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public BezierFlowerView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {
        drawables = new Drawable[3];

        red = getResources().getDrawable(R.mipmap.red);
        yellow = getResources().getDrawable(R.mipmap.yellow);
        blue = getResources().getDrawable(R.mipmap.blue);
        drawables[0] = red;
        drawables[1] = yellow;
        drawables[2] = blue;

        dHeight = red.getIntrinsicHeight();
        dWidth = red.getIntrinsicWidth();

        layoutParams = new LayoutParams(dWidth, dHeight);
        layoutParams.addRule(CENTER_HORIZONTAL, TRUE);
        layoutParams.addRule(ALIGN_PARENT_BOTTOM, TRUE);


        // 初始化插补器
        interpolators = new Interpolator[4];
        interpolators[0] = line;
        interpolators[1] = acc;
        interpolators[2] = dce;
        interpolators[3] = accdec;

        rand = new Random();

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        mWidth = getMeasuredWidth();
        mHeight = getMeasuredHeight();
    }


    public void addFlower(Context context) {
        // 需要一些动画.
        ImageView imageView = new ImageView(context);
        imageView.setImageDrawable(drawables[rand.nextInt(3)]);
        imageView.setLayoutParams(layoutParams);
        addView(imageView);
        setUpstartAnima(imageView).start();
        ValueAnimator valueAnimator = setUpRunAnima(imageView);
        valueAnimator.addListener(new AnimEndListener(imageView));
        valueAnimator.start();
    }

    private AnimatorSet setUpstartAnima(View tag) {
        ObjectAnimator alpha = new ObjectAnimator().ofFloat(tag, "alpha", 0.2f, 1.0f);
        ObjectAnimator scalex = new ObjectAnimator().ofFloat(tag, "scaleX", 0.2f, 1.0f);
        ObjectAnimator scaley = new ObjectAnimator().ofFloat(tag, "scaleY", 0.2f, 1.0f);
        AnimatorSet enter = new AnimatorSet();
        enter.setDuration(500);
        enter.setTarget(tag);
        enter.playTogether(alpha, scalex, scaley);
        return enter;
    }

    private ValueAnimator setUpRunAnima(View tag) {


        //初始化一个BezierEvaluator
        BeizerEvaluator evaluator = new BeizerEvaluator(getPointF(1), getPointF(2));
        ValueAnimator animator = ValueAnimator.ofObject(evaluator, new PointF(rand.nextInt(getWidth()), 0), new PointF(rand.nextInt(getWidth()), mHeight - dHeight));//随机
        animator.addUpdateListener(new BezierListenr(tag));
        animator.setInterpolator(interpolators[rand.nextInt(3)]);
        animator.setTarget(tag);
        animator.setDuration(3000);
        return animator;
    }


    private class AnimEndListener extends AnimatorListenerAdapter {
        private View target;

        public AnimEndListener(View target) {
            this.target = target;
        }

        @Override
        public void onAnimationEnd(Animator animation) {
            super.onAnimationEnd(animation);
            //因为不停的add 导致子view数量只增不减,所以在view动画结束后remove掉
            removeView((target));
        }
    }

    /**
     * 获取中间的两个 点
     *
     * @param scale
     */
    private PointF getPointF(int scale) {

        PointF pointF = new PointF();
        pointF.x = rand.nextInt((mWidth - 100));
        pointF.y = rand.nextInt((mHeight - 100)) / scale;
        return pointF;
    }

    private class BezierListenr implements ValueAnimator.AnimatorUpdateListener {

        private View target;

        public BezierListenr(View target) {
            this.target = target;
        }

        @Override
        public void onAnimationUpdate(ValueAnimator animation) {
            PointF pointF = (PointF) animation.getAnimatedValue();
            ViewHelper.setX(target, pointF.x);
            ViewHelper.setY(target, pointF.y);
            ViewHelper.setAlpha(target, 1 - animation.getAnimatedFraction());
        }
    }

    /**
     * 贝塞尔方程
     */
    private class BeizerEvaluator implements TypeEvaluator<PointF> {

        private PointF point1;
        private PointF point2;

        private PointF pointF;

        public BeizerEvaluator(PointF point1, PointF point2) {
            this.point1 = point1;
            this.point2 = point2;
        }

        @Override
        public PointF evaluate(float time, PointF start, PointF end) {
            float timeLeft = 1.0f - time;
            pointF = new PointF();//结果

            PointF point0 = start;//起点

            PointF point3 = end;//终点
            pointF.x = timeLeft * timeLeft * timeLeft * (point0.x)
                    + 3 * timeLeft * timeLeft * time * (point1.x)
                    + 3 * timeLeft * time * time * (point2.x)
                    + time * time * time * (point3.x);

            pointF.y = timeLeft * timeLeft * timeLeft * (point0.y)
                    + 3 * timeLeft * timeLeft * time * (point1.y)
                    + 3 * timeLeft * time * time * (point2.y)
                    + time * time * time * (point3.y);
            return pointF;
        }
    }
}
