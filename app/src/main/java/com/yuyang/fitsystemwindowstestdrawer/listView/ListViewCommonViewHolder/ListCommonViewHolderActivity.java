package com.yuyang.fitsystemwindowstestdrawer.listView.listViewCommonViewHolder;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ListView;

import com.yuyang.fitsystemwindowstestdrawer.R;

import java.util.ArrayList;
import java.util.List;

/**
 * 使用通用的ViewHolder
 */
public class ListCommonViewHolderActivity extends AppCompatActivity {
    private Toolbar toolbar;
    private ListView listView;
    private ItemAdapter itemAdapter;
    private List<Item> items;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_list);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("使用通用的ListView－ViewHolder");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        listView = (ListView) findViewById(R.id.list_view);
        initDatas();
        itemAdapter = new ItemAdapter(this, items);
        listView.setAdapter(itemAdapter);
    }

    private void initDatas() {
        items = new ArrayList<>();
        Item item1 = new Item();
        item1.setmTitle("item1");
        item1.setmDescription("item1Description");
        item1.setmImgId(R.drawable.ic_menu_send);
        Item item2 = new Item();
        item2.setmTitle("item2");
        item2.setmDescription("item2Description");
        item2.setmImgId(R.drawable.ic_menu_camera);
        Item item3 = new Item();
        item3.setmTitle("item3");
        item3.setmDescription("item3Description");
        item3.setmImgId(R.drawable.ic_menu_gallery);
        items.add(item1);
        items.add(item2);
        items.add(item3);
    }
}
