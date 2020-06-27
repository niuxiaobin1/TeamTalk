package com.mogujie.tt.ui.base;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextPaint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.mogujie.tt.R;
import com.mogujie.tt.ui.activity.SearchActivity;
import com.mogujie.tt.ui.widget.SearchEditText;
import com.mogujie.tt.utils.Logger;

public abstract class TTBaseFragment extends Fragment {
    protected ImageView topLeftBtn;
    protected ImageView topRightBtn;
    protected TextView topTitleTxt;
    protected TextView topRightTitleTxt;
    protected ViewGroup topBar;
    protected ViewGroup topContentView;
    /**
     * 搜索相关
     */

    protected TextView topSearchTv;
    protected SearchEditText searchEt;
    protected TextView tvCancel;
    protected LinearLayout mLinSearchEdit;

    protected float x1, y1, x2, y2 = 0;
    protected static Logger logger = Logger.getLogger(TTBaseFragment.class);

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        topContentView = (ViewGroup) LayoutInflater.from(getActivity()).inflate(R.layout.tt_fragment_base, null);

        topBar = (ViewGroup) topContentView.findViewById(R.id.topbar);
        topTitleTxt = (TextView) topContentView.findViewById(R.id.base_fragment_title);
        topRightTitleTxt = (TextView) topContentView.findViewById(R.id.right_txt);
        topLeftBtn = (ImageView) topContentView.findViewById(R.id.left_btn);
        topRightBtn = (ImageView) topContentView.findViewById(R.id.right_btn);
        topSearchTv = topContentView.findViewById(R.id.chat_title_search);
        tvCancel = topContentView.findViewById(R.id.tv_cancel);
        mLinSearchEdit = topContentView.findViewById(R.id.lin_Search_edit);
        searchEt = topContentView.findViewById(R.id.searchEt);

        topTitleTxt.setVisibility(View.GONE);
        topRightBtn.setVisibility(View.GONE);
        topLeftBtn.setVisibility(View.GONE);
        topRightTitleTxt.setVisibility(View.INVISIBLE);
        topSearchTv.setVisibility(View.GONE);

        topLeftBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().finish();
            }
        });

        tvCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                topSearchTv.setVisibility(View.VISIBLE);
                mLinSearchEdit.setVisibility(View.GONE);
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup vg,
                             Bundle bundle) {
        if (null != topContentView) {
            ((ViewGroup) topContentView.getParent()).removeView(topContentView);
            return topContentView;
        }
        return topContentView;
    }

    protected View getTitleBar() {
        return topContentView;
    }

    protected void setTopTitleBold(String title) {
        if (title == null) {
            return;
        }
        if (title.length() > 12) {
            title = title.substring(0, 11) + "...";
        }
        // 设置字体为加粗
        TextPaint paint = topTitleTxt.getPaint();
        paint.setFakeBoldText(true);

        topTitleTxt.setText(title);
        topTitleTxt.setVisibility(View.VISIBLE);

    }

    protected void setTopTitle(String title) {
        if (title == null) {
            return;
        }
        if (title.length() > 12) {
            title = title.substring(0, 11) + "...";
        }
        topTitleTxt.setText(title);
        topTitleTxt.setVisibility(View.VISIBLE);
    }

    protected void setTopLeftButton(int resID) {
        if (resID <= 0) {
            return;
        }

        topLeftBtn.setImageResource(resID);
        topLeftBtn.setVisibility(View.VISIBLE);
    }

    protected void setTopLeftText(String text) {
        if (null == text) {
            return;
        }
    }

    protected void setTopRightText(String text) {
        if (null == text) {
            return;
        }
        topRightTitleTxt.setText(text);
        topRightTitleTxt.setVisibility(View.VISIBLE);
    }

    protected void setTopRightButton(int resID, View.OnClickListener onClickListener) {
        if (resID <= 0) {
            return;
        }

        topRightBtn.setImageResource(resID);
        topRightBtn.setVisibility(View.VISIBLE);
        topRightBtn.setOnClickListener(onClickListener);
    }

    protected void hideTopRightButton() {
        topRightBtn.setVisibility(View.GONE);
    }

    protected void setTopBar(int resID) {
        if (resID <= 0) {
            return;
        }
        topBar.setBackgroundResource(resID);
    }

    protected void hideTopBar() {
        topBar.setVisibility(View.GONE);
    }

    protected void showTopSearchBar() {
        topSearchTv.setVisibility(View.VISIBLE);
        topSearchTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                topSearchTv.setVisibility(View.GONE);
                mLinSearchEdit.setVisibility(View.VISIBLE);
            }
        });
    }

    protected void hideTopSearchBar() {
        topSearchTv.setVisibility(View.GONE);
    }

    protected void showSearchFrameLayout() {
        //下面的历史代码
        //tryHandleSearchAction(action);
    }

    protected abstract void initHandler();

    @Override
    public void onActivityCreated(Bundle bundle) {
        logger.d("Fragment onActivityCreate:" + getClass().getName());
        super.onActivityCreated(bundle);
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    protected void initSearch() {
        setTopRightButton(R.drawable.tt_top_search, null);
        topRightBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                showSearchView();
            }
        });
    }

    public void showSearchView() {
        startActivity(new Intent(getActivity(), SearchActivity.class));
    }

    protected void onSearchDataReady() {
        initSearch();
    }
}
