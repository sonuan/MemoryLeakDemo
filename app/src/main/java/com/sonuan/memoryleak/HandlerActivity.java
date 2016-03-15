package com.sonuan.memoryleak;

import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

public class HandlerActivity extends AppCompatActivity {

    private TextView mTVLeak;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_handle);
        mTVLeak = (TextView) findViewById(R.id.tv_leak);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                mTVLeak.setText("内存泄露");
            }
        }, 5000000);//延迟5000秒后执行，为了检测内存泄漏比较明显，故意设置这么大，实际开发中，应该不会出现设置如此大

    }
}
