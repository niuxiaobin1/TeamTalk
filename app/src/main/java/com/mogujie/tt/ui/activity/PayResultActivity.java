package com.mogujie.tt.ui.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.mogujie.tt.R;
import com.mogujie.tt.ui.base.TTBaseActivity;

public class PayResultActivity extends TTBaseActivity {

    private Button btn_done;
    private TextView amountTv;
    private TextView nameTv;
    private TextView resultTv;
    private ImageView resultImage;

    public static final String TRANSFOR_NAME = "_name";
    public static final String TRANSFOR_RESULT = "_result";
    public static final String TRANSFOR_AMOUNT = "_amount";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LayoutInflater.from(this).inflate(R.layout.activity_pay_result, topContentView);

        btn_done = findViewById(R.id.btn_done);
        amountTv = findViewById(R.id.amountTv);
        nameTv = findViewById(R.id.nameTv);
        resultTv = findViewById(R.id.resultTv);
        resultImage = findViewById(R.id.resultImage);

        setTitle("");
        setLeftButton(R.mipmap.ic_back_black);
        setRightButton(0);


        Intent it = getIntent();
        if (it.getBooleanExtra(TRANSFOR_RESULT, false)) {
            resultImage.setImageResource(R.drawable.ic_zfcg);
            resultTv.setText(getResources().getString(R.string.transfer_result_success));
            nameTv.setText(it.getStringExtra(TRANSFOR_NAME));
            amountTv.setText(getResources().getString(R.string.transfer_unit)
                    + it.getStringExtra(TRANSFOR_AMOUNT));
        }

        btn_done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }
}
