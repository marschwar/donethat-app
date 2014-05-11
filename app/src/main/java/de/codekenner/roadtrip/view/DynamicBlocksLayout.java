package de.codekenner.roadtrip.view;

import android.content.Context;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.HorizontalScrollView;
import android.widget.ScrollView;

public class DynamicBlocksLayout extends ViewGroup {

    private transient DisplayMetrics dm = null;
    private static final int GRID_WIDTH = 450;
    private static final int GRID_HEIGHT = 350;

    /**
     * number of visible columns on screen
     */
    private int visibleColumnCount;
    /**
     * number of overall columns available via scrolling
     */
    private int currentColumnCount;
    private int currentRowCount;
    private int currentColumnWidth;
    private int currentRowHeight;

    private DynamicLayoutHelper layoutHelper;

    public DynamicBlocksLayout(Context context) {
        super(context);
    }

    public DynamicBlocksLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public DynamicBlocksLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        // At this time we need to call setMeasuredDimensions(). Lets just call
        // the
        // parent View's method (see
        // https://github.com/android/platform_frameworks_base/blob/master/core/java/android/view/View.java)
        // that does:
        // setMeasuredDimension(getDefaultSize(getSuggestedMinimumWidth(),
        // widthMeasureSpec),
        // getDefaultSize(getSuggestedMinimumHeight(), heightMeasureSpec));
        //

        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        long start = System.currentTimeMillis();
        actualOnMeasure();
        Log.i(getClass().getSimpleName() + "#onMeasure", String.format("execution time: %dms", System.currentTimeMillis() - start));
    }

    private void actualOnMeasure() {
        final int measuredWidth = getMeasuredWidth();
        final int measuredHeight = getMeasuredHeight();
        final int calculatedWidth = (measuredWidth > 0) ? measuredWidth
                : getScreenWidth() - guessHorizontalMargins();

        // first Time around calculate with available space
        currentColumnCount = calcGridCount(calculatedWidth, GRID_WIDTH);
        currentRowCount = calcGridCount(measuredHeight, GRID_HEIGHT);
        currentColumnWidth = calcGridSize(calculatedWidth, GRID_WIDTH,
                currentColumnCount);
        currentRowHeight = calcGridSize(measuredHeight, GRID_HEIGHT,
                currentRowCount);
        visibleColumnCount = currentColumnCount;

        if (isScrollable()) {
            // in this case we use the calculated grid width and height
            // to figure out the number of columns

            final int featured = calcMaxFeatureCount(currentColumnCount,
                    currentRowCount);
            currentColumnCount = (int) Math.ceil((getChildCount() + featured)
                    / (double) currentRowCount);
            setMeasuredDimension(
                    calcSize(currentColumnWidth, currentColumnCount),
                    measuredHeight);
        }

        for (int i = 0; i < getChildCount(); i++) {
            View v = getChildAt(i);
            final SpanDefinition span = calcGridSpan(i, currentColumnCount,
                    currentRowCount);

            int wspec = MeasureSpec.makeMeasureSpec(
                    calcSize(currentColumnWidth, span.horizontal),
                    MeasureSpec.EXACTLY);
            int hspec = MeasureSpec.makeMeasureSpec(
                    calcSize(currentRowHeight, span.vertical),
                    MeasureSpec.EXACTLY);

            v.measure(wspec, hspec);
        }
        layoutHelper = new DynamicLayoutHelper(currentColumnCount,
                currentRowCount, visibleColumnCount);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {

        long start = System.currentTimeMillis();
        actualOnLayout();
        Log.i(getClass().getSimpleName() + "#onLayout", String.format("execution time: %dms", System.currentTimeMillis() - start));
    }

    private void actualOnLayout() {
        for (int i = 0; i < getChildCount(); i++) {
            View v = getChildAt(i);
            final SpanDefinition span = calcGridSpan(i, currentColumnCount,
                    currentRowCount);

            final Pair<Integer, Integer> position = layoutHelper.occupy(span);
            if (position != null) {
                final int col = position.first;
                final int row = position.second;

                final int padding = getChildPadding();
                final int left = col * currentColumnWidth + col * padding;
                final int top = row * currentRowHeight + row * padding;
                final int right = left + span.horizontal * currentColumnWidth
                        + (span.horizontal - 1) * padding;
                final int bottom = top + span.vertical * currentRowHeight
                        + (span.vertical - 1) * padding;
                v.layout(left, top, right, bottom);
            }
        }
    }

    private int getScreenWidth() {
        return getDisplayMetrics().widthPixels;
    }

    private int calcGridSize(final int size, int maxGridSize, int count) {
        final double netWidth = (double) size
                - ((count - 1) * getChildPadding());
        return (int) (netWidth / count);
    }

    private int calcGridCount(final int size, int maxGridSize) {
        return (int) Math.ceil(size
                / ((double) maxGridSize * getDisplayMetrics().density));
    }

    private DisplayMetrics getDisplayMetrics() {
        if (dm == null) {
            dm = new DisplayMetrics();
            final WindowManager wm = (WindowManager) getContext()
                    .getApplicationContext().getSystemService(
                            Context.WINDOW_SERVICE);
            wm.getDefaultDisplay().getMetrics(dm);
        }
        return dm;
    }

    public int getChildPadding() {
        return (int) (10 * getDisplayMetrics().density);
    }

    private int calcSize(int gridSize, int span) {
        return span * gridSize + (span - 1) * getChildPadding();
    }

    private SpanDefinition calcGridSpan(int childIndex, int columns, int rows) {
        final int childCount = getChildCount();
        boolean allowsFeatured = visibleColumnCount > 1;

        if (childCount < visibleColumnCount * rows) {
            // Special Layout since we do not have enough children
            int maxFeatured = (allowsFeatured) ? calcMaxFeatureCount(columns,
                    rows) : 0;

            return (allowsFeatured && maxFeatured > childIndex) ? SpanDefinition.TWO_BY_ONE
                    : SpanDefinition.ONE_BY_ONE;
        } else {
            final boolean featured = allowsFeatured && childIndex == 0;
            return (featured && allowsFeatured) ? SpanDefinition.TWO_BY_ONE
                    : SpanDefinition.ONE_BY_ONE;
        }
    }

    private int calcMaxFeatureCount(int columns, int rows) {
        // return Math.max(columns / 2, rows / 2);
        return columns > 1 ? 1 : 0;
    }

    private static class DynamicLayoutHelper {

        final boolean[][] occupied;
        final int columnCount;
        final int rowCount;
        final int columnsPerPage;

        public DynamicLayoutHelper(int cols, int rows, int columnsPerPage) {
            occupied = new boolean[cols][rows];
            columnCount = cols;
            rowCount = rows;
            this.columnsPerPage = columnsPerPage;
        }

        public Pair<Integer, Integer> occupy(SpanDefinition span) {
            Pair<Integer, Integer> result = null;
            int pageCount = (int) Math.ceil((double) columnCount / columnsPerPage);
            int colOnPage = 0;
            int row = 0;
            int page = 0;
            do {
                final int absoluteCol = page * columnsPerPage + colOnPage;
                if (isAvailable(absoluteCol, row, span)) {
                    result = new Pair<Integer, Integer>(absoluteCol, row);
                    markOccupied(absoluteCol, row, span);
                }
                if (++colOnPage >= columnsPerPage) {
                    // last column of page -> next row, first column
                    colOnPage = 0;
                    row++;
                }
                if (row >= rowCount) {
                    // end of page -> first row of next page;
                    page++;
                    colOnPage = 0;
                    row = 0;
                }
            } while (result == null && row < rowCount && page < pageCount);

            return result;
        }

        private boolean isAvailable(int x, int y, SpanDefinition span) {
            for (int i = 0; i < span.horizontal; i++) {
                if (x + i >= columnCount) {
                    // not enough columns
                    return false;
                }
                for (int j = 0; j < span.vertical; j++) {
                    if (y + j >= rowCount) {
                        // not enough rows
                        return false;
                    }
                    if (occupied[x + i][y + j]) {
                        return false;
                    }
                }
            }
            return true;
        }

        private void markOccupied(int x, int y, SpanDefinition span) {
            for (int i = 0; i < span.horizontal; i++) {
                for (int j = 0; j < span.vertical; j++) {
                    occupied[x + i][y + j] = true;
                }
            }
        }
    }

    private static class SpanDefinition {

        public final int horizontal;
        public final int vertical;

        public static final SpanDefinition ONE_BY_ONE = new SpanDefinition(1, 1);
        public static final SpanDefinition TWO_BY_ONE = new SpanDefinition(2, 1);

        public SpanDefinition(int horizontal, int vertical) {
            super();
            this.horizontal = horizontal;
            this.vertical = vertical;
        }

        public static SpanDefinition createHorizontalSpan(int of) {
            return new SpanDefinition(of, 1);
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + horizontal;
            result = prime * result + vertical;
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            SpanDefinition other = (SpanDefinition) obj;
            if (horizontal != other.horizontal)
                return false;
            if (vertical != other.vertical)
                return false;
            return true;
        }
    }

    public boolean isScrollable() {
        return getParent() instanceof HorizontalScrollView
                || getParent() instanceof ScrollView;
    }

    /**
     * Guess the margin around this layout. We use this to calculate the grid
     * width in a scrollable environment. This will only work if the parent is
     * the top level element or there is no more padding all the way to the top.
     *
     * @return
     */
    private int guessHorizontalMargins() {
        final View parent = (View) getParent();
        return parent.getPaddingLeft() + parent.getPaddingRight();
    }

    /**
     * Guess the margin around this layout. We use this to calculate the grid
     * width in a scrollable environment. This will only work if the parent is
     * the top level element or there is no more padding all the way to the top.
     *
     * @return
     */
    private int guessVerticalMargins() {
        final View parent = (View) getParent();
        return parent.getPaddingTop() + parent.getPaddingBottom();
    }
}
