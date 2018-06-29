package com.robinhood.ticker.sample;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.robinhood.ticker.test.TestTickerView;

import java.util.Random;

public class TestActivity extends AppCompatActivity {
    TestTickerView testTickerView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
        testTickerView = findViewById(R.id.textView);
    }

    public void test(View view) {
        int num = new Random().nextInt(1000);
        testTickerView.setNumber(num,true);
    }
}
