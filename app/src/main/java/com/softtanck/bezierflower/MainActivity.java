package com.softtanck.bezierflower;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.softtanck.bezierflower.view.BezierFlowerView;


public class MainActivity extends AppCompatActivity {

    private BezierFlowerView bezierFlowerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        bezierFlowerView = (BezierFlowerView) findViewById(R.id.bfv);

    }

    public void test(View view) {
        for (int i = 0; i < 10; i++) {
            bezierFlowerView.addFlower(MainActivity.this);
        }
    }

}
