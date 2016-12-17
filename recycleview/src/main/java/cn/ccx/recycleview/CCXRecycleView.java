package cn.ccx.recycleview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by chenchangxing on 2016/10/17.
 */

public class CCXRecycleView extends RecyclerView {

    private static final String CCXRECYCLEVIEW_LOG = "ccxRecyclerView";
    public static final int LINEARLAYOUT_MANAGER = 0;
    public static final int GRIDLAYOUT_MANAGER = 1;

    private int layoutManager;
    private int rowCount;

    private int dividerColor;
    private float dividerWidth;

    private String text;
    private float textSize;
    private int textColor;

    private String emptyText;
    private float emptyTextSize;
    private int emptyTextColor;

    private OnLoadMoreListener onLoadMoreListener;
    private OnDeleteListener onDeleteListener;

    private List<Integer> ignorePositions;

    public CCXRecycleView(Context context) {
        super(context);

        ignorePositions = new ArrayList<>();
    }

    public CCXRecycleView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);

        ignorePositions = new ArrayList<>();
    }

    private void init(Context context, AttributeSet attrs) {
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.CCXRecycleView);

        dividerColor = typedArray.getColor(
                R.styleable.CCXRecycleView_divider_color,
                0);
        dividerWidth = typedArray.getDimension(
                R.styleable.CCXRecycleView_divider_width,
                0);

        text = typedArray.getString(
                R.styleable.CCXRecycleView_text);

        textSize = typedArray.getDimension(
                R.styleable.CCXRecycleView_text_size,
                dip2px(14));

        textColor = typedArray.getColor(
                R.styleable.CCXRecycleView_text_color,
                getResources().getColor(R.color.black));

        emptyText = typedArray.getString(
                R.styleable.CCXRecycleView_empty_text);

        emptyTextSize = typedArray.getDimension(
                R.styleable.CCXRecycleView_empty_text_size,
                dip2px(20));

        emptyTextColor = typedArray.getColor(
                R.styleable.CCXRecycleView_empty_text_color,
                getResources().getColor(R.color.black));


        typedArray.recycle();
    }

    public void setLayoutManager(int layoutManager) {
        this.layoutManager = layoutManager;
        if (layoutManager == LINEARLAYOUT_MANAGER) {
            setLayoutManager(new LinearLayoutManager(getContext()));
            return;
        }

        if (layoutManager == GRIDLAYOUT_MANAGER) {
            rowCount = 2;
            setLayoutManager(new GridLayoutManager(getContext(), 2));
        }
    }

    public void setLayoutManager(int layoutManager, int count) {
        this.layoutManager = layoutManager;
        if (layoutManager == LINEARLAYOUT_MANAGER) {
            setLayoutManager(new LinearLayoutManager(getContext()));
            return;
        }

        if (layoutManager == GRIDLAYOUT_MANAGER) {
            rowCount = count;
            setLayoutManager(new GridLayoutManager(getContext(), count));
        }
    }

    public void setDeleteEnable(boolean enable) {
        if (enable) {
            ItemTouchHelper helper = new ItemTouchHelper(callback);
            helper.attachToRecyclerView(this);
        }
    }

    public void setDeleteEnable(boolean enable, int ignorePosition) {
        if (enable) {
            ignorePositions.add(ignorePosition);
            setDeleteEnable(true);
        }
    }

    public void setDeleteEnable(boolean enable, int[] ignorePosition) {
        if (enable) {
            for (int i = 0; i < ignorePosition.length; i++) {
                ignorePositions.add(ignorePosition[i]);
            }
            setDeleteEnable(true);
        }
    }

    public void setDeleteEnable(boolean enable, List<Integer> ignorePositions) {
        if (enable) {
            this.ignorePositions.addAll(ignorePositions);
            setDeleteEnable(true);
        }
    }

    public void setDivideEnable(boolean enable) {
        if (enable) {
            addItemDecoration(dividerDecoration);
        }
    }

    public void setLoadMoreEnable(boolean enable) {
        if (enable) {
            addOnScrollListener(onScrollListener);
            addItemDecoration(loadMoreDecoration, this.getChildCount());
        }
    }

    public void setEmptyViewEnable(boolean enable) {
        if (enable && super.getAdapter().getItemCount() == 0) {
            addItemDecoration(emptyDecoration);
            removeItemDecoration(loadMoreDecoration);
            removeItemDecoration(dividerDecoration);
            return;
        }

        if (super.getAdapter().getItemCount() > 0) {
            Log.e(CCXRECYCLEVIEW_LOG, "your item is not empty");
        }
    }

    public void setNoMoreEnable(boolean enable) {
        if (enable) {
            super.removeItemDecoration(loadMoreDecoration);
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
                        Log.e(CCXRECYCLEVIEW_LOG, "you forget the initialize OnLoadMoreListener");
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
            if (!ignorePositions.isEmpty()) {
                for (int i : ignorePositions) {
                    if (i == viewHolder.getAdapterPosition()) {
                        return;
                    }
                }
            }
            if (onDeleteListener != null) {
                onDeleteListener.delete(viewHolder.getAdapterPosition());
            } else {
                Log.e(CCXRECYCLEVIEW_LOG, "you forget the initialize OnLoadMoreListener");
            }
        }

        @Override
        public void onChildDraw(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
            if (!ignorePositions.isEmpty()) {
                for (int i : ignorePositions) {
                    if (i == viewHolder.getAdapterPosition()) {
                        return;
                    }
                }
            }
            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
            if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
                //滑动时改变Item的透明度
                final float alpha = 1 - Math.abs(dX) / (float) viewHolder.itemView.getWidth();
                viewHolder.itemView.setAlpha(alpha);
                viewHolder.itemView.setTranslationX(dX);
            }
        }
    };

    RecyclerView.ItemDecoration dividerDecoration = new ItemDecoration() {
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

                paint.setStrokeWidth(dip2px(dividerWidth));
                paint.setColor(dividerColor);
                c.drawLine(left, top, right, top, paint);
            }
        }

        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, State state) {
            super.getItemOffsets(outRect, view, parent, state);
            outRect.set(0, 0, 0, dip2px(dividerWidth));
        }
    };

    RecyclerView.ItemDecoration loadMoreDecoration = new ItemDecoration() {
        private int left;

        @Override
        public void onDrawOver(Canvas c, RecyclerView parent, State state) {
            super.onDrawOver(c, parent, state);

            int width = parent.getWidth() + parent.getPaddingLeft();
            int layoutSize = dip2px(50);

            Paint paint = new Paint();
            paint.setTextSize(textSize);


            View view = parent.getChildAt(parent.getChildCount() - 1);

            String content = "加载更多";

            int left = (int) (width - content.length() * textSize) / 2;

            Log.e("width", "width = " + width);
            Log.e("text", "text = " + content.length() + " textsize = " + textSize);
            Log.e("left", "left = " + left);

            if (layoutManager == LINEARLAYOUT_MANAGER) {

                LinearLayoutManager manager = (LinearLayoutManager) parent.getLayoutManager();

                if (parent.getChildCount() < manager.getItemCount() && parent.getChildCount() - 1 <= manager.findLastCompletelyVisibleItemPosition()) {
                    c.drawText(content, left, view.getBottom() + layoutSize / 2 + textSize / 2, paint);
                }
            } else {
                GridLayoutManager manager = (GridLayoutManager) parent.getLayoutManager();

                if (parent.getChildCount() < manager.getItemCount() && parent.getChildCount() - 1 <= manager.findLastCompletelyVisibleItemPosition()) {
                    c.drawText(content, left, view.getBottom() + layoutSize / 2 + textSize / 2, paint);
                }
            }
        }

        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, State state) {
            super.getItemOffsets(outRect, view, parent, state);

            int layoutSize = dip2px(50);
            if (layoutManager == LINEARLAYOUT_MANAGER) {
                LinearLayoutManager manager = (LinearLayoutManager) parent.getLayoutManager();

                if (parent.getChildCount() < manager.getItemCount() && manager.getItemCount() - 1 == manager.getPosition(view)) {
                    outRect.set(0, 0, 0, layoutSize);
                }
                return;
            }

            if (layoutManager == GRIDLAYOUT_MANAGER) {
                GridLayoutManager manager = (GridLayoutManager) parent.getLayoutManager();
                if (parent.getChildCount() < manager.getItemCount() && manager.getItemCount() - left-- == manager.getPosition(view)) {
                    outRect.set(0, 0, 0, layoutSize);
                } else {
                    left = manager.getItemCount() % rowCount == 0 ? rowCount : manager.getItemCount() % rowCount ;
                }
            }
        }
    };

    RecyclerView.ItemDecoration emptyDecoration = new ItemDecoration() {
        @Override
        public void onDraw(Canvas c, RecyclerView parent, State state) {
            super.onDraw(c, parent, state);


            BitmapDrawable bitmapDrawable = (BitmapDrawable) getResources().getDrawable(R.drawable.no_data);
            Bitmap bitmap = bitmapDrawable.getBitmap();
            int iconX = parent.getWidth() / 2 - bitmap.getWidth() / 2;
            int iconY = (int) (parent.getHeight() / 2 - bitmap.getHeight() / 2 - emptyTextSize);

            String content = TextUtils.isEmpty(emptyText) ? emptyText = "暂无数据" : emptyText;
            int textX = (int) (parent.getWidth() / 2 - content.length() / 2 * emptyTextSize);
            int textY = (int) (parent.getHeight() / 2 + bitmap.getHeight() / 2 - emptyTextSize) + dip2px(20);

            Paint paint = new Paint();
            paint.setTextSize(emptyTextSize);
            paint.setColor(emptyTextColor);

            c.drawBitmap(bitmap, iconX, iconY, new Paint());
            c.drawText(emptyText, textX, textY, paint);
        }

        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, State state) {
            super.getItemOffsets(outRect, view, parent, state);
            outRect.set(parent.getWidth() / 2, parent.getHeight() / 2, 0, 0);
        }
    };

    public void addLoadMoreListener(OnLoadMoreListener onLoadMoreListener) {
        this.onLoadMoreListener = onLoadMoreListener;
    }

    public void addDeleteListener(OnDeleteListener onDeleteListener) {
        this.onDeleteListener = onDeleteListener;
    }

    public int dip2px(double d) {
        final float scale = getContext().getResources().getDisplayMetrics().density;
        return (int) (d * scale + 0.5f);
    }

    public interface OnLoadMoreListener {
        void loadMore();
    }

    public interface OnDeleteListener {
        void delete(int position);
    }
}
