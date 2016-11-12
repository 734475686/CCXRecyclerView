package cn.ccx.mymodule;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import cn.ccx.recycleview.CCXRecycleView;

public class MainActivity extends AppCompatActivity {

    List<String> list = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        for (int i= 0; i < 9; i++) {
            list.add("aaa");
        }

        final BaseAdapter adapter = new BaseAdapter(list);

        CCXRecycleView ccxRecycleView = (CCXRecycleView) findViewById(R.id.recycler);

        ccxRecycleView.setDivideEnable(true);
        ccxRecycleView.setLoadMoreEnable(true);

        ccxRecycleView.setLayoutManager(new LinearLayoutManager(this));
        ccxRecycleView.setAdapter(adapter);
        ccxRecycleView.addLoadMoreListener(new CCXRecycleView.OnLoadMoreListener() {
            @Override
            public void loadMore() {
//                for (int i= 0; i < 10; i++) {
//                    list.add("aaa");
//                }
//                adapter.notifyDataSetChanged();
            }
        });
        ccxRecycleView.addDeleteListener(new CCXRecycleView.OnDeleteListener() {
            @Override
            public void delete(int position) {
                list.remove(position);
                adapter.notifyItemRemoved(position);
            }
        });
    }

    class BaseAdapter extends RecyclerView.Adapter {

        List<String> list;

        public BaseAdapter(List<String> list) {
            this.list = list;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_main, parent, false);
            return new MyViewHolder(view);
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

        }

        @Override
        public int getItemCount() {
            return list.size();
        }

        class MyViewHolder extends RecyclerView.ViewHolder {

            MyViewHolder(View itemView) {
                super(itemView);
            }
        }
    }
}
