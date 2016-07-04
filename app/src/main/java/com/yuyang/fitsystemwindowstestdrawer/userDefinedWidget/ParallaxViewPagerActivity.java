package com.yuyang.fitsystemwindowstestdrawer.userDefinedWidget;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.yuyang.fitsystemwindowstestdrawer.R;
import com.yuyang.fitsystemwindowstestdrawer.userDefinedWidget.ParallaxViewPager.CustomViewPager;

/**
 * 背景视差效果的ViewPager
 */
public class ParallaxViewPagerActivity extends AppCompatActivity {
    private CustomViewPager viewPager;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_parallax_view_pager);

        viewPager = (CustomViewPager) findViewById(R.id.custom_view_pager);
        viewPager.setAdapter(new FragmentPagerAdapter(getSupportFragmentManager()) {
            @Override
            public Fragment getItem(int position) {
                return SimpleFragment.newInstance("HelloWorld"+position);
            }

            @Override
            public int getCount() {
                return 3;
            }
        });
    }

    private static class SimpleFragment extends Fragment{
        public static SimpleFragment newInstance(String title) {
            SimpleFragment simpleFragment = new SimpleFragment();
            Bundle bundle = new Bundle();
            bundle.putString("key_title", title);
            simpleFragment.setArguments(bundle);
            return simpleFragment;
        }

        private String mTitle = "helloworld";

        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            mTitle = getArguments().getString("key_title");
        }

        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            TextView tv = new TextView(getActivity());
            tv.setTextSize(20);
            tv.setTextColor(Color.WHITE);
            tv.setText(mTitle);
            return tv;
        }
    }
}
