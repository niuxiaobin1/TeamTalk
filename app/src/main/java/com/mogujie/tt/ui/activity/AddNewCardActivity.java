package com.mogujie.tt.ui.activity;

import android.content.Intent;
import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.mogujie.tt.R;
import com.mogujie.tt.ui.base.TTBaseActivity;
import com.mogujie.tt.utils.DividerItemDecoration;

import java.util.ArrayList;
import java.util.List;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static com.mogujie.tt.utils.DividerItemDecoration.VERTICAL_LIST;

public class AddNewCardActivity extends TTBaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LayoutInflater.from(this).inflate(R.layout.activity_add_new_card, topContentView);

        setTitle(getResources().getString(R.string.app_addCardTitle));
        setLeftButton(R.mipmap.ic_back_black);

        findViewById(R.id.bankNameLayout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(AddNewCardActivity.this, SelectBankActivity.class);
                startActivity(intent);
            }
        });


    }
}