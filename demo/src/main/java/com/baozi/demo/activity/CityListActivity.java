package com.baozi.demo.activity;

import android.graphics.Rect;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.baozi.demo.R;
import com.baozi.demo.moudle.citylist.ProvinceItemParent;
import com.baozi.demo.moudle.citylist.bean.CityBean;
import com.baozi.demo.moudle.citylist.event.DeleteEvent;
import com.baozi.demo.moudle.citylist.event.DownEvent;
import com.baozi.demo.moudle.citylist.event.UpEvent;
import com.baozi.treerecyclerview.adpater.TreeRecyclerAdapter;
import com.baozi.treerecyclerview.adpater.TreeRecyclerType;
import com.baozi.treerecyclerview.adpater.wrapper.LoadingWrapper;
import com.baozi.treerecyclerview.adpater.wrapper.SwipeWrapper;
import com.baozi.treerecyclerview.base.BaseRecyclerAdapter;
import com.baozi.treerecyclerview.base.ViewHolder;
import com.baozi.treerecyclerview.factory.ItemHelperFactory;
import com.baozi.treerecyclerview.item.TreeItem;
import com.baozi.treerecyclerview.item.TreeItemGroup;
import com.baozi.treerecyclerview.manager.ItemManager;
import com.hwangjr.rxbus.RxBus;
import com.hwangjr.rxbus.annotation.Subscribe;

import java.util.List;

public class CityListActivity extends AppCompatActivity {

    private List<TreeItem> treeItemList;
    private TreeRecyclerAdapter treeRecyclerAdapter;
    private SwipeWrapper swipeWrapper;
    private RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_city);
        RxBus.get().register(this);
        recyclerView = (RecyclerView) findViewById(R.id.rl_content);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
                super.getItemOffsets(outRect, view, parent, state);
                outRect.top = 10;
                if (view.getLayoutParams() instanceof GridLayoutManager.LayoutParams) {
                    GridLayoutManager.LayoutParams layoutParams = (GridLayoutManager.LayoutParams) view.getLayoutParams();
                    int spanIndex = layoutParams.getSpanIndex();//在一行中所在的角标，第几列
                    if (spanIndex != ((GridLayoutManager) parent.getLayoutManager()).getSpanCount() - 1) {
                        outRect.right = 10;
                    }
                }
            }
        });
        List<CityBean> cityBeen = JSON.parseArray(getResources().getString(R.string.location), CityBean.class);
        treeItemList = ItemHelperFactory.createTreeItemList(cityBeen, ProvinceItemParent.class, null);
        treeRecyclerAdapter = new TreeRecyclerAdapter(TreeRecyclerType.SHOW_EXPAND);
        swipeWrapper = new SwipeWrapper(treeRecyclerAdapter);
        LoadingWrapper wrapper = new LoadingWrapper(swipeWrapper);
        wrapper.setEmptyView(R.layout.layout_empty);
        wrapper.setLoadingView(R.layout.layout_loading);

        recyclerView.setAdapter(wrapper);
        ((TreeItemGroup) treeItemList.get(0)).setExpand(true);//default expand first group
        wrapper.setDatas(treeItemList);
        wrapper.setType(LoadingWrapper.Type.SUCCESS);

        treeRecyclerAdapter.setOnItemClickListener(new BaseRecyclerAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(ViewHolder viewHolder, int position) {
                //拿到BaseItem
                final TreeItem item = treeRecyclerAdapter.getDatas().get(position);
                if (treeRecyclerAdapter.getType() != TreeRecyclerType.SHOW_ALL && item instanceof TreeItemGroup && ((TreeItemGroup) item).isCanExpand()) {
                    //展开,折叠
                    swipeWrapper.getSwipeManger().closeAllItems();
                    recyclerView.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            treeRecyclerAdapter.expandOrCollapse(((TreeItemGroup) item));
                        }
                    },200);
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        RxBus.get().unregister(this);
    }

    @Subscribe
    public void subscribeDeleteEvent(final DeleteEvent event) {
        Toast.makeText(this, "删除" + event.position, Toast.LENGTH_SHORT).show();
        recyclerView.postDelayed(new Runnable() {
            @Override
            public void run() {
                treeRecyclerAdapter.getItemManager().removeItem(event.position);
            }
        }, 200);
    }

    @Subscribe
    public void subscribeUpEvent(final UpEvent event) {

        recyclerView.postDelayed(new Runnable() {
            @Override
            public void run() {
                upMoveItem(event.position);
            }
        }, 200);
    }

    private void upMoveItem(int position) {
        //        Toast.makeText(this, "上移"+event.position, Toast.LENGTH_SHORT).show();
        ItemManager<TreeItem> itemManager = treeRecyclerAdapter.getItemManager();
        TreeItem targetItem = itemManager.getItem(position);
        if (targetItem.position == 0) {
//            swipeWrapper.getSwipeManger().closeItem(event.position);
            Toast.makeText(this, "已经到顶了", Toast.LENGTH_SHORT).show();
        } else {
            itemManager.removeItemWithoutNotify(position);
            targetItem.position -= 1;
            itemManager.addItem(position - 1, targetItem);
        }

    }

    @Subscribe
    public void subscribeDownEvent(final DownEvent event) {
        recyclerView.postDelayed(new Runnable() {
            @Override
            public void run() {
                downMoveItem(event.position);
            }
        }, 200);
    }

    private void downMoveItem(int position) {
        //        Toast.makeText(this, "下移"+event.position, Toast.LENGTH_SHORT).show();
        ItemManager<TreeItem> itemManager = treeRecyclerAdapter.getItemManager();
        TreeItem targetItem = itemManager.getItem(position);
        if (targetItem.position == targetItem.getParentItem().getChildCount() - 1) {
            Toast.makeText(this, "已经到底了", Toast.LENGTH_SHORT).show();
        } else {
            itemManager.removeItemWithoutNotify(position);
            targetItem.position += 1;
            itemManager.addItem(position + 1, targetItem);
        }
    }
}
