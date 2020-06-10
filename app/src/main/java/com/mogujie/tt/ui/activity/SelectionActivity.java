package com.mogujie.tt.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.mogujie.tt.R;
import com.mogujie.tt.config.TUIKitConstants;
import com.mogujie.tt.ui.base.TTBaseActivity;
import com.mogujie.tt.utils.DividerItemDecoration;
import com.mogujie.tt.utils.SoftKeyBoardUtil;
import com.mogujie.tt.utils.ToastUtil;

import java.util.ArrayList;
import java.util.List;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static com.mogujie.tt.utils.DividerItemDecoration.VERTICAL_LIST;

public class SelectionActivity extends TTBaseActivity {

    private static OnResultReturnListener sOnResultReturnListener;
    private static OnResultReturnListener1 sOnResultReturnListener1;

    private RecyclerView selectList;
    private EditText input;
    private RelativeLayout edit_content_layout;
    private ImageView cleartextImage;
    private int mSelectionType;
    private TextView sureTv;
    private FrameLayout searchLayout;

    private LinearLayout normalLayout;
    private LinearLayout inputLayout;
    private TextView cancleTv;
    private EditText searchEt;
    private ImageView cleartextImage1;
    private MySelectAdapter mySelectAdapter;
    private ArrayList<String> data;
    private String key = "";

    public static void startTextSelection(Context context, Bundle bundle, OnResultReturnListener listener) {
        bundle.putInt(TUIKitConstants.Selection.TYPE, TUIKitConstants.Selection.TYPE_TEXT);
        startSelection(context, bundle, listener);
    }

    public static void startListSelection(Context context, Bundle bundle, OnResultReturnListener listener) {
        bundle.putInt(TUIKitConstants.Selection.TYPE, TUIKitConstants.Selection.TYPE_LIST);
        startSelection(context, bundle, listener);
    }
    public static void startListSelection(Context context, Bundle bundle, OnResultReturnListener1 listener) {
        bundle.putInt(TUIKitConstants.Selection.TYPE, TUIKitConstants.Selection.TYPE_LIST);
        startSelection(context, bundle, listener);
    }

    private static void startSelection(Context context, Bundle bundle, OnResultReturnListener listener) {
        Intent intent = new Intent(context, SelectionActivity.class);
        intent.putExtra(TUIKitConstants.Selection.CONTENT, bundle);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
        sOnResultReturnListener = listener;
    }
    private static void startSelection(Context context, Bundle bundle, OnResultReturnListener1 listener) {
        Intent intent = new Intent(context, SelectionActivity.class);
        intent.putExtra(TUIKitConstants.Selection.CONTENT, bundle);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
        sOnResultReturnListener1 = listener;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LayoutInflater.from(this).inflate(R.layout.activity_edit_alisa, topContentView);

        setLeftButton(R.mipmap.ic_back_black);
        input = findViewById(R.id.edit_content_et);
        edit_content_layout = findViewById(R.id.edit_content_layout);
        selectList = findViewById(R.id.selectList);
        cleartextImage = findViewById(R.id.cleartextImage);
        sureTv = findViewById(R.id.sureTv);
        searchLayout = findViewById(R.id.searchLayout);


        normalLayout = findViewById(R.id.normalLayout);
        inputLayout = findViewById(R.id.inputLayout);
        cancleTv = findViewById(R.id.cancleTv);
        searchEt = findViewById(R.id.searchEt);
        cleartextImage1 = findViewById(R.id.cleartextImage1);

        Bundle bundle = getIntent().getBundleExtra(TUIKitConstants.Selection.CONTENT);
        switch (bundle.getInt(TUIKitConstants.Selection.TYPE)) {
            case TUIKitConstants.Selection.TYPE_TEXT:
                selectList.setVisibility(GONE);
                String defaultString = bundle.getString(TUIKitConstants.Selection.INIT_CONTENT);
                String hintString = bundle.getString(TUIKitConstants.Selection.INIT_HINT);
                int limit = bundle.getInt(TUIKitConstants.Selection.LIMIT);
                if (!TextUtils.isEmpty(hintString)) {
                    input.setHint(hintString);
                }
                if (!TextUtils.isEmpty(defaultString)) {
                    input.setText(defaultString);
                    input.setSelection(defaultString.length());
                }

                if (limit > 0) {
                    input.setFilters(new InputFilter[]{new InputFilter.LengthFilter(limit)});
                }
                break;
            case TUIKitConstants.Selection.TYPE_LIST:
                edit_content_layout.setVisibility(GONE);
                boolean isCanSearch = bundle.getBoolean(TUIKitConstants.Selection.CAN_SEARCH);
                data = bundle.getStringArrayList(TUIKitConstants.Selection.LIST);
                if (data == null || data.size() == 0) {
                    return;
                }
                if (isCanSearch) {
                    searchLayout.setVisibility(VISIBLE);
                } else {
                    searchLayout.setVisibility(GONE);
                }
                int checked = bundle.getInt(TUIKitConstants.Selection.DEFAULT_SELECT_ITEM_INDEX);
                mySelectAdapter=new MySelectAdapter(data, checked);
                selectList.setAdapter(mySelectAdapter);
                break;
            default:
                finish();
                return;
        }
        mSelectionType = bundle.getInt(TUIKitConstants.Selection.TYPE);

        final String title = bundle.getString(TUIKitConstants.Selection.TITLE);
        setTitle(title);
        sureTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                echoClick(title);
            }
        });

        input.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
//                if (TextUtils.isEmpty(editable.toString())){
//                    cleartextImage.setVisibility(View.GONE);
//                }else{
//                    cleartextImage.setVisibility(View.VISIBLE);
//                }
            }
        });

        selectList.setLayoutManager(new LinearLayoutManager(this));
        selectList.addItemDecoration(new DividerItemDecoration(this, VERTICAL_LIST, 1,
                R.color.text_tips_color));


        cleartextImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                input.setText("");
            }
        });


        normalLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                normalLayout.setVisibility(GONE);
                inputLayout.setVisibility(VISIBLE);
                SoftKeyBoardUtil.showSoftInputFromWindow(SelectionActivity.this, searchEt);

            }
        });


        searchEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (TextUtils.isEmpty(editable.toString())) {
                    cleartextImage1.setVisibility(GONE);
                    key="";
                } else {
                    cleartextImage1.setVisibility(VISIBLE);
                    key = editable.toString();
                }
                mySelectAdapter.screenData();
            }
        });

        cleartextImage1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                searchEt.setText("");
            }
        });


        cancleTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                normalLayout.setVisibility(VISIBLE);
                inputLayout.setVisibility(GONE);
                SoftKeyBoardUtil.hideKeyBoard(searchEt);
            }
        });

    }

    private void echoClick(String title) {
        switch (mSelectionType) {
            case TUIKitConstants.Selection.TYPE_TEXT:
                if (TextUtils.isEmpty(input.getText().toString()) && title.equals(getResources().getString(R.string.modify_group_name))) {
                    ToastUtil.toastLongMessage(getResources().getString(R.string.tuikit_againFillInNickName));
                    return;
                }

                if (sOnResultReturnListener != null) {
                    sOnResultReturnListener.onReturn(input.getText().toString());
                }
                break;
            case TUIKitConstants.Selection.TYPE_LIST:
                if (sOnResultReturnListener != null) {
                    sOnResultReturnListener.onReturn(mySelectAdapter.getCurSelectP());
                }
                if (sOnResultReturnListener1 != null) {
                    sOnResultReturnListener1.onReturn(mySelectAdapter.getCurSelectName());
                }
                break;
        }
        finish();
    }

    @Override
    protected void onStop() {
        super.onStop();
        sOnResultReturnListener = null;
        sOnResultReturnListener1 = null;
    }

    public interface OnResultReturnListener {
        void onReturn(Object res);
    }
    public interface OnResultReturnListener1 {
        void onReturn(String name);
    }


    class MySelectAdapter extends RecyclerView.Adapter<MySelectHolder> {

        private List<String> list=new ArrayList<>();
        private int curSelectP = 0;

        public MySelectAdapter(List<String> mData, int curSelectP) {
            this.list.addAll(mData);
            this.curSelectP = curSelectP;
        }

        public void screenData(){
            list.clear();
            if (TextUtils.isEmpty(key)){
                list.addAll(data);
            }else{
                for (int i = 0; i <data.size() ; i++) {
                    String content=data.get(i).toUpperCase();
                    if (!TextUtils.isEmpty(content)&&content.contains(key.toUpperCase())){
                        list.add(data.get(i));
                    }
                }
            }
            notifyDataSetChanged();
        }

        public int getCurSelectP() {
            return curSelectP;
        }
        public String getCurSelectName() {
            return list.get(curSelectP);
        }

        @NonNull
        @Override
        public MySelectHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.select_item, parent, false);
            return new MySelectHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull MySelectHolder holder, final int position) {

            holder.select_content.setText(list.get(position));
            if (position == curSelectP) {
                holder.select_content.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
                holder.select_check.setVisibility(VISIBLE);
            } else {
                holder.select_content.setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL));
                holder.select_check.setVisibility(GONE);
            }

            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    curSelectP = position;
                    notifyDataSetChanged();
                }
            });
        }

        @Override
        public int getItemCount() {
            return list.size();
        }
    }

    class MySelectHolder extends RecyclerView.ViewHolder {

        private TextView select_content;
        private ImageView select_check;

        public MySelectHolder(View itemView) {
            super(itemView);
            select_content = itemView.findViewById(R.id.select_content);
            select_check = itemView.findViewById(R.id.select_check);
        }
    }
}
