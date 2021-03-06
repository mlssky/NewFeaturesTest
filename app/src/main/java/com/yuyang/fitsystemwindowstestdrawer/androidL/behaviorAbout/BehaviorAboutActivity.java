package com.yuyang.fitsystemwindowstestdrawer.androidL.behaviorAbout;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.yuyang.fitsystemwindowstestdrawer.R;
import com.yuyang.fitsystemwindowstestdrawer.androidL.behaviorAbout.nestedScrollBehavior.simple.NestedScrollBehaviorActivity;

import java.util.Arrays;
import java.util.List;

/**
 * behavior使用
 */
public class BehaviorAboutActivity extends AppCompatActivity {
    private List<String> items = Arrays.asList("系统的BottomSheet","系统的SwipeDismissBehavior","自定义Behavior_DependBehavior","自定义Behavior_LayoutBehavior",
            "只关心滑动的自定义Behavior");

    private Toolbar toolbar;
    private ListView listView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_just_list);

        setToolbar();

        listView = (ListView) findViewById(R.id.list_view);
        listView.setAdapter(new ArrayAdapter<String>(this, R.layout.item_just_text, R.id.id_info, items));
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = null;
                switch (position){
                    case 0:
                        intent = new Intent(BehaviorAboutActivity.this, BottomSheetActivity.class);
                        break;
                    case 1:
                        intent = new Intent(BehaviorAboutActivity.this, SwipeDismissBehaviorActivity.class);
                        break;
                    case 2:
                        intent = new Intent(BehaviorAboutActivity.this, BehaviorActivity1.class);
                        break;
                    case 3:
                        intent = new Intent(BehaviorAboutActivity.this, BehaviorActivity2.class);
                        break;
                    case 4:
                        intent = new Intent(BehaviorAboutActivity.this, NestedScrollBehaviorActivity.class);
                        break;
                }
                startActivity(intent);
            }
        });
    }

    private void setToolbar() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle("Behavior");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }
}
