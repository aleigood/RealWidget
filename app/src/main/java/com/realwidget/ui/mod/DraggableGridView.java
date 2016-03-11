package com.realwidget.ui.mod;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.os.Vibrator;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import com.realwidget.R;
import com.realwidget.db.Button;
import com.realwidget.util.Utils;

import java.util.List;

public class DraggableGridView extends GridView {
    private final float ALPHA_VALUE = 0.8F;
    // 滑动的距离,scroll的时候会用到
    // scaledTouchSlop定义了拖动的偏差位(一般+-10)
    // 表示滑动的时候，手的移动要大于这个距离才开始移动控件。
    private int mTouchSlop;
    // 被拖拽项的影像
    private ImageView mDragImage;
    // 被拖拽项的真实View
    private View mDragView;
    // 手指拖动的时候，当前拖动项在列表中的位置
    private int mPosition;
    // dragPoint点击位置在点击View内的相对位置
    private int mDragPointY;
    private int mDragPointX;
    private int mYOffset;
    private int mXOffset;
    // windows窗口控制类
    private WindowManager mWindowManager;
    private WindowManager mWindowManager2;
    // 用于控制拖拽项的显示的参数
    private WindowManager.LayoutParams mWindowParams;
    // 拖动的时候，开始向上滚动的边界
    private int mUpperBound;
    // 拖动的时候，开始向下滚动的边界
    private int mLowerBound;
    private int mMoveX;

    private int mMoveY;
    private View mSelectedView;

    private WidgetConfigureActivity mActivity;
    private DraggableGridAdapter mAdapter;

    public DraggableGridView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public DraggableGridView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
    }

    public DraggableGridView(Context context) {
        super(context);
    }

    /**
     * onTouchEvent() 用于处理事件，返回值决定当前控件是否消费（consume）了这个事件
     */
    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (mDragImage != null && mPosition != INVALID_POSITION) {
            int action = ev.getAction();

            switch (action) {
                case MotionEvent.ACTION_UP:
                    // 释放拖动影像
                    stopDrag();
                    break;
                case MotionEvent.ACTION_MOVE:
                    // 拖动影像
                    onDrag((int) ev.getX(), (int) ev.getY());
                    break;
                default:
                    break;
            }
            return true;
        }

        return super.onTouchEvent(ev);
    }

    /**
     * 返回值为false时事件会传递给子控件的onInterceptTouchEvent()；
     * 返回值为true时事件会传递给当前控件的onTouchEvent()，而不再传递给子控件
     *
     * @see android.widget.AbsListView#onInterceptTouchEvent(android.view.MotionEvent)
     */
    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        int x = (int) ev.getX();
        int y = (int) ev.getY();

        // dragOffset屏幕位置和当前ListView位置的偏移量
        mYOffset = (int) (ev.getRawY() - y);
        mXOffset = (int) (ev.getRawX() - x);

        // 拦截DOWN事件
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            setOnItemLongClickListener(x, y);
            setOnItemClickListener(x, y);
        }

        return super.onInterceptTouchEvent(ev);
    }

    public void setOnItemClickListener(final int x, final int y) {
        if (mSelectedView != null) {
            mWindowManager2.removeView(mSelectedView);
            mSelectedView = null;
        }

        setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> paramAdapterView, View paramView, int paramInt, long paramLong) {
                // 选中的数据项位置，使用ListView自带的pointToPosition(x, y)方法
                mPosition = pointToPosition(x, y);

                // 如果是无效位置(超出边界，分割线等位置)，返回
                if (mPosition == AdapterView.INVALID_POSITION) {
                    return;
                }

                ViewGroup itemView = (ViewGroup) getChildAt(mPosition - getFirstVisiblePosition());
                ViewGroup item1 = (ViewGroup) itemView.findViewById(R.id.pos1);
                ViewGroup item2 = (ViewGroup) itemView.findViewById(R.id.pos2);
                ViewGroup selectedView = item1;

                int dragPointY = y - itemView.getTop();
                int dragPointX = 0;

                if (item2 != null && x > item2.getLeft()) {
                    dragPointX = x - item2.getLeft();
                    selectedView = item2;
                } else {
                    dragPointX = x - item1.getLeft();
                    selectedView = item1;
                }

                final Button selButton = (Button) selectedView.getTag();

                // 选择的按钮不存在
                if (selButton == null) {
                    return;
                }

                final int selId = selButton.btnId;

                // selectedView.getY()是相对于父vieW坐标，所以要使用itemView
                WindowManager.LayoutParams windowParams = new WindowManager.LayoutParams();
                // 从上到下计算y方向上的相对位置，
                windowParams.gravity = Gravity.TOP | Gravity.LEFT;

                windowParams.x = x - dragPointX + mXOffset;
                windowParams.y = y - dragPointY + mYOffset;

                windowParams.width = selectedView.getWidth();
                windowParams.height = selectedView.getHeight();

                // 下面这些参数能够帮助准确定位到选中项点击位置，照抄即可
                windowParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                        | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                        | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN;
                windowParams.format = PixelFormat.TRANSLUCENT;
                windowParams.windowAnimations = 0;

                // 把影像ImagView添加到当前视图中
                LayoutInflater inflater = LayoutInflater.from(mActivity);
                mSelectedView = inflater.inflate(R.layout.view_button_config, null);
                mSelectedView.findViewById(R.id.delete).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (mSelectedView != null) {
                            mWindowManager2.removeView(mSelectedView);
                            mSelectedView = null;
                        }

                        List<Button> data = mAdapter.getSourceData();

                        for (int i = selId + 1; i < data.size(); i++) {
                            data.get(i).btnId--;
                        }
                        data.remove(selId);
                        mAdapter.refresh();
                    }
                });
                mSelectedView.findViewById(R.id.config).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent();
                        intent.putExtra("button", selButton);
                        intent.setClass(mActivity, ButtonConfActivity.class);
                        mActivity.startActivityForResult(intent, WidgetConfigureActivity.REQUEST_CODE_CONFIG_BUTTON);
                    }
                });

                mWindowManager2 = (WindowManager) mActivity.getSystemService("window");
                mWindowManager2.addView(mSelectedView, windowParams);
            }
        });
    }

    public void setOnItemLongClickListener(final int x, final int y) {
        setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                // 选中的数据项位置，使用ListView自带的pointToPosition(x, y)方法
                mPosition = pointToPosition(x, y);

                // 如果是无效位置(超出边界，分割线等位置)，返回
                if (mPosition == AdapterView.INVALID_POSITION) {
                    return false;
                }

                // 获取选中项View
                // getChildAt(int position)显示display在界面的position位置的View
                // getFirstVisiblePosition()返回第一个display在界面的view在adapter的位置position，可能是0，也可能是4
                ViewGroup itemView = (ViewGroup) getChildAt(mPosition - getFirstVisiblePosition());
                ViewGroup item1 = (ViewGroup) itemView.findViewById(R.id.pos1);
                ViewGroup item2 = (ViewGroup) itemView.findViewById(R.id.pos2);

                mDragPointY = y - itemView.getTop();

                if (item2 != null && x > item2.getLeft()) {
                    mDragPointX = x - item2.getLeft();
                    mDragView = item2;
                } else {
                    mDragPointX = x - item1.getLeft();
                    mDragView = item1;
                }

                if (mDragView != null && x > mDragView.getLeft()) {
                    // 向上滚动边界
                    mUpperBound = Math.min(y - mTouchSlop, getHeight() / 5);
                    // 向下滚动边界
                    mLowerBound = Math.max(y + mTouchSlop, getHeight() * 4 / 5);

                    // 设置Drawingcache为true，获得选中项的图片
                    mDragView.setDrawingCacheEnabled(true);
                    Bitmap bm = Bitmap.createBitmap(mDragView.getDrawingCache());

                    // 拖动影像(把影像加入到当前窗口，并没有拖动，拖动操作我们放在onTouchEvent()的move中执行)
                    startDrag(bm, x, y);
                    mDragView.setVisibility(View.INVISIBLE);
                }

                // 震动
                Vibrator vibrator = (Vibrator) mActivity.getSystemService("vibrator");
                vibrator.vibrate(25);
                return false;
            }
        });
    }

    /**
     * 准备拖动，初始化拖动项的图像
     */
    private void startDrag(Bitmap bm, int x, int y) {
        // 释放影像，在准备影像的时候，防止影像没释放，每次都执行一下
        refreshData();

        mWindowParams = new WindowManager.LayoutParams();
        // 从上到下计算y方向上的相对位置，
        mWindowParams.gravity = Gravity.TOP | Gravity.LEFT;
        mWindowParams.x = x - mDragPointX + mXOffset;
        mWindowParams.y = y - mDragPointY + mYOffset;

        mWindowParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
        mWindowParams.height = WindowManager.LayoutParams.WRAP_CONTENT;

        // 下面这些参数能够帮助准确定位到选中项点击位置，照抄即可
        mWindowParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN;
        mWindowParams.format = PixelFormat.TRANSLUCENT;
        mWindowParams.windowAnimations = 0;

        // 把影像ImagView添加到当前视图中
        mDragImage = new ImageView(mActivity);
        mDragImage.setImageBitmap(bm);
        mDragImage.setPadding(0, 0, 0, 0);
        mDragImage.setAlpha(ALPHA_VALUE);

        mWindowManager = (WindowManager) mActivity.getSystemService("window");
        mWindowManager.addView(mDragImage, mWindowParams);
    }

    /**
     * 停止拖动
     */
    public void stopDrag() {
        if (mDragImage != null) {
            mWindowManager.removeView(mDragImage);
            mDragImage = null;
        }
    }

    /**
     * 拖动执行，在Move方法中执行
     */
    public void onDrag(final int x, final int y) {
        if (mDragImage != null) {
            // 设置一点点的透明度
            mWindowParams.alpha = ALPHA_VALUE;
            // 更新坐标位置
            mWindowParams.y = y - mDragPointY + mYOffset;
            mWindowParams.x = x - mDragPointX + mXOffset;
            // 更新界面
            mWindowManager.updateViewLayout(mDragImage, mWindowParams);
        }

        // 为了避免滑动到分割线的时候，返回-1的问题
        int position = pointToPosition(x, y);

        if (position != INVALID_POSITION) {
            mPosition = position;
        }

        // 滚动高度
        int scrollHeight = 0;

        if (y < mUpperBound) {
            // 定义向上滚动8个像素，如果可以向上滚动的话
            scrollHeight = -8;
        } else if (y > mLowerBound) {
            scrollHeight = 8;
        }

        mMoveX = x;
        mMoveY = y;

        if (scrollHeight != 0) {
            smoothScrollByOffset(scrollHeight);
        } else {
            postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (x == mMoveX && y == mMoveY) {
                        onStop(x, y);
                    }
                }
            }, 250);
        }
    }

    /**
     * 停止拖动的时候
     */
    public void onStop(int x, int y) {
        Button dragBtn = (Button) mDragView.getTag();

        if (dragBtn == null) {
            return;
        }

        // 获取某位置在数据position
        int position = pointToPosition(0, y);

        if (position != INVALID_POSITION) {
            mPosition = position;
        }

        if (y < getChildAt(0).getTop()) {
            mPosition = 0;
        } else if (y > getChildAt(getChildCount() - 1).getBottom()) {
            mPosition = getAdapter().getCount() - 1;
        }

        ViewGroup itemView = (ViewGroup) getChildAt(mPosition - getFirstVisiblePosition());
        ViewGroup item1 = (ViewGroup) itemView.findViewById(R.id.pos1);
        ViewGroup item2 = (ViewGroup) itemView.findViewById(R.id.pos2);
        int item1X = (int) item1.getX() + item1.getWidth();
        List<Button> data = mAdapter.getSourceData();
        int newBtnId = dragBtn.btnId;

        // 交换ID
        if (x > (int) item1.getX() && x < item1X) {
            newBtnId = ((Button) item1.getTag()).btnId;
        } else if (x > item1X && item2 != null) {
            // pos2不为空，是小按钮
            if (item2.getTag() != null) {
                int item2X = (int) item2.getX() + item2.getWidth();

                if (x < item2X) {
                    newBtnId = ((Button) item2.getTag()).btnId;
                }
            }
            // pos2为空按钮
            else {
                newBtnId = ((Button) item1.getTag()).btnId + 1;
            }
        }

        // 往后移
        if (dragBtn.btnId < newBtnId) {
            for (int i = dragBtn.btnId + 1; i <= newBtnId; i++) {
                if (i < data.size()) {
                    data.get(i).btnId--;
                }
            }

            dragBtn.btnId = newBtnId;
        } else if (dragBtn.btnId > newBtnId) {
            for (int i = newBtnId; i < dragBtn.btnId; i++) {
                if (i < data.size()) {
                    data.get(i).btnId++;
                }
            }

            dragBtn.btnId = newBtnId;
        } else {
            return;
        }

        mAdapter.refresh();
    }

    public void init(WidgetConfigureActivity widgetConfigure, List<Button> btns) {
        mAdapter = new DraggableGridAdapter(widgetConfigure, btns);
        super.setAdapter(mAdapter);
        mActivity = widgetConfigure;
    }

    public void refreshData() {
        // 清除编辑状态
        if (mSelectedView != null) {
            mWindowManager2.removeView(mSelectedView);
            mSelectedView = null;
        }
        mAdapter.refresh();
    }

    public void clean() {
        Utils.deleteAllButtonImage(mActivity, mAdapter.getSortedData());
    }
}
