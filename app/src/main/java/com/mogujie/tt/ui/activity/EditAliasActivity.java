package com.mogujie.tt.ui.activity;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import com.mogujie.tt.R;
import com.mogujie.tt.ui.base.TTBaseActivity;

public class EditAliasActivity extends TTBaseActivity {

    private EditText mEtAlisa;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LayoutInflater.from(this).inflate(R.layout.activity_edit_alisa, topContentView);

        setTitle(getIntent().getStringExtra("title"));
        setLeftButton(R.mipmap.ic_back_black);
        mEtAlisa = findViewById(R.id.et_alisa);
        mEtAlisa.setHint(getIntent().getStringExtra("hint"));

        if (getIntent().getBooleanExtra("hasCancle", true)) {
            setRightText(getResources().getString(R.string.cancel), new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    finish();
                }
            });
        }


    }
}
