package cn.ccx.recycleview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

/**
 * Created by chenchangxing on 2016/10/17.
 */

public class CCXRecycleView extends RecyclerView {

    private int dividerColor;
    private float dividerWidth;

    private String text;
    private float textSize;
    private int textColor;


    private OnLoadMoreListener onLoadMoreListener;
    private OnDeleteListener onDeleteListener;
    private ItemTouchHelper helper;


    public CCXRecycleView(Context context) {
        super(context);
        init();

    }

    public CCXRecycleView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        TypedArray typedArray = getContext().obtainStyledAttributes(R.styleable.CCXRecycleView);

        dividerColor = typedArray.getColor(
                R.styleable.CCXRecycleView_divider_color,
                getResources().getColor(R.color.black));
        dividerWidth = typedArray.getDimension(
                R.styleable.CCXRecycleView_divider_width,
                dip2px(getContext(), 0.5));

        text = typedArray.getString(
                R.styleable.CCXRecycleView_text);

        textSize = typedArray.getDimension(
                R.styleable.CCXRecycleView_text_size,
                dip2px(getContext(), 14));

        textColor = typedArray.getColor(
                R.styleable.CCXRecycleView_text_color,
                getResources().getColor(R.color.black));

        typedArray.recycle();
    }

    public void setDeleteEnable(boolean enable) {
        if (enable) {
            helper = new ItemTouchHelper(callback);
            helper.attachToRecyclerView(this);
        }
    }

    public void setDivideEnable(boolean enable) {
        if (enable) {
            super.addItemDecoration(itemDecoration);
        }
    }

    public void setLoadMoreEnable(boolean enable) {
        if (enable) {
            super.addOnScrollListener(onScrollListener);
            super.addItemDecoration(loadMoreDecoration, this.getChildCount());
        }
    }



    OnScrollListener onScrollListener = new OnScrollListener() {
        boolean isSlidingToLast;

        @Override
        public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
            super.onScrollStateChanged(recyclerView, newState);
            LinearLayoutManager manager = (LinearLayoutManager) recyclerView.getLayoutManager();
            if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                //获取最后一个完全显示的ItemPosition
                int lastVisibleItem = manager.findLastCompletelyVisibleItemPosition();
                int totalItemCount = manager.getItemCount();

                // 判断是否滚动到底部，并且是向右滚动
                if (lastVisibleItem == (totalItemCount - 1) && isSlidingToLast) {
                    if (onLoadMoreListener != null) {
                        onLoadMoreListener.loadMore();
                    } else {
                        Log.e("CCXRecycleView", "you forget the initialize OnLoadMoreListener");
                    }
                }
            }
        }

        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);
            isSlidingToLast = dy > 0;
        }
    };

    ItemTouchHelper.Callback callback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {

        @Override
        public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
            return false;
        }

        @Override
        public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
            if (onDeleteListener != null) {
                onDeleteListener.delete(viewHolder.getAdapterPosition());
            } else {
                Log.e("CCXRecycleView", "you forget the initialize OnLoadMoreListener");
            }
        }

        @Override
        public void onChildDraw(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
            if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
                //滑动时改变Item的透明度
                final float alpha = 1 - Math.abs(dX) / (float) viewHolder.itemView.getWidth();
                viewHolder.itemView.setAlpha(alpha);
                viewHolder.itemView.setTranslationX(dX);
            }
        }
    };

    RecyclerView.ItemDecoration itemDecoration = new ItemDecoration() {
        @Override
        public void onDraw(Canvas c, RecyclerView parent, State state) {
            super.onDraw(c, parent, state);

            int left = parent.getPaddingLeft();
            int right = parent.getWidth() + parent.getPaddingLeft();

            Paint paint = new Paint();

            for (int i = 0; i < parent.getChildCount(); i++) {
                View child = parent.getChildAt(i);
                RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child.getLayoutParams();
                int top = child.getBottom() + params.bottomMargin;

                paint.setStrokeWidth(dip2px(getContext(), dividerWidth));
                paint.setColor(getContext().getResources().getColor(R.color.black));
                c.drawLine(left, top, right, top, paint);
            }
        }

        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, State state) {
            super.getItemOffsets(outRect, view, parent, state);
            outRect.set(0, 0, 0, dip2px(getContext(), dividerWidth));
        }
    };

    RecyclerView.ItemDecoration loadMoreDecoration = new ItemDecoration() {
        @Override
        public void onDraw(Canvas c, RecyclerView parent, State state) {
            super.onDraw(c, parent, state);

            LinearLayoutManager manager = (LinearLayoutManager) parent.getLayoutManager();

            int width = parent.getWidth() + parent.getPaddingLeft();
            int layoutSize = dip2px(getContext(), 50);

            Paint paint = new Paint();
            paint.setTextSize(textSize);
            paint.setColor(textColor);

            View view = parent.getChildAt(parent.getChildCount() - 1);

            String content = TextUtils.isEmpty(text) ? text = "加载更多" : text;

            int left = (int) (width / 2 - textSize * (content.length() / 2));

            if (parent.getChildCount() < manager.getItemCount() && parent.getChildCount() - 1 <= manager.findLastCompletelyVisibleItemPosition()) {
                c.drawText(content, left, view.getBottom() + layoutSize / 2, paint);
            }
        }

        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, State state) {
            super.getItemOffsets(outRect, view, parent, state);
            LinearLayoutManager manager = (LinearLayoutManager) parent.getLayoutManager();

            int layoutSize = dip2px(getContext(), 50);
            if (parent.getChildAdapterPosition(view) == manager.getItemCount() - 1) {
                outRect.set(0, 0, 0, layoutSize);
            }
        }
    };

    public void addLoadMoreListener(OnLoadMoreListener onLoadMoreListener) {
        this.onLoadMoreListener = onLoadMoreListener;
    }

    public void addDeleteListener(OnDeleteListener onDeleteListener) {
        this.onDeleteListener = onDeleteListener;
    }

    public static int dip2px(Context context, double d) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (d * scale + 0.5f);
    }

    public interface OnLoadMoreListener {
        void loadMore();
    }

    public interface OnDeleteListener {
        void delete(int position);
    }
}
