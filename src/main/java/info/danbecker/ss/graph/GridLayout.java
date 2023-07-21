package info.danbecker.ss.graph;

import java.util.ArrayList;
import java.util.List;

import com.mxgraph.layout.mxGraphLayout;
import com.mxgraph.model.mxCell;
import com.mxgraph.model.mxIGraphModel;
import com.mxgraph.util.mxRectangle;
import com.mxgraph.view.mxGraph;
import info.danbecker.ss.RowCol;

public class GridLayout extends mxGraphLayout {
    /**
     * Boolean specifying if the circle should be moved to the top,
     * left corner specified by x0 and y0. Default is false.
     */
    protected boolean moveCircle = true;

    /**
     * Integer specifying the left coordinate of the grid.
     * Default is 0.
     */
    protected double x0 = 10;

    /**
     * Integer specifying the top coordinate of the grid.
     * Default is 0.
     */
    protected double y0 = 10;

    /**
     * Specifies if all edge points of traversed edges should be removed.
     * Default is true.
     */
    protected boolean resetEdges = false;

    /**
     * Specifies if the STYLE_NOEDGESTYLE flag should be set on edges that are
     * modified by the result. Default is true.
     */
    protected boolean disableEdgeStyle = true;

    /**
     * Constructs a new layout for the specified graph,
     * spacing, orientation and offset.
     */
    public GridLayout(mxGraph graph) {
        super(graph);
    }

    /**
     * @return the x0
     */
    public double getX0() {
        return x0;
    }

    /**
     * @param x0 the x0 to set
     */
    public void setX0(double x0) {
        this.x0 = x0;
    }

    /**
     * @return the y0
     */
    public double getY0() {
        return y0;
    }

    /**
     * @param y0 the y0 to set
     */
    public void setY0(double y0) {
        this.y0 = y0;
    }

    /**
     * @return the resetEdges
     */
    public boolean isResetEdges() {
        return resetEdges;
    }

    /**
     * @param resetEdges the resetEdges to set
     */
    public void setResetEdges(boolean resetEdges) {
        this.resetEdges = resetEdges;
    }

    /**
     * @return the disableEdgeStyle
     */
    public boolean isDisableEdgeStyle() {
        return disableEdgeStyle;
    }

    /**
     * @param disableEdgeStyle the disableEdgeStyle to set
     */
    public void setDisableEdgeStyle(boolean disableEdgeStyle) {
        this.disableEdgeStyle = disableEdgeStyle;
    }

    /*
     * (non-Javadoc)
     * @see com.mxgraph.layout.mxIGraphLayout#execute(java.lang.Object)
     */
    public void execute(Object parent) {
        mxIGraphModel model = graph.getModel();

        // Moves the vertices to grid like a chess board.
        // Makes sure the radius is large enough for the vertices to not overlap
        model.beginUpdate();
        try {
            // Gets all vertices inside the parent and finds
            // the maximum dimension of the largest vertex
            double maxDimension = 0;
            Double top = null;
            Double left = null;
            List<Object> moveVertices = new ArrayList<Object>();
            int childCount = model.getChildCount(parent);

            for (int i = 0; i < childCount; i++) {
                Object cell = model.getChildAt(parent, i);

                if (!isVertexIgnored(cell)) {
                    moveVertices.add(cell);
                    mxRectangle bounds = getVertexBounds(cell);

                    if (top == null) {
                        top = bounds.getY();
                    } else {
                        top = Math.min(top, bounds.getY());
                    }

                    if (left == null) {
                        left = bounds.getX();
                    } else {
                        left = Math.min(left, bounds.getX());
                    }

                    maxDimension = Math.max(maxDimension,
                            Math.max(bounds.getWidth(), bounds.getHeight()));
                } else if (!isEdgeIgnored(cell)) {
                    if (isResetEdges()) {
                        graph.resetEdge(cell);
                    }

                    if (isDisableEdgeStyle()) {
                        setEdgeStyleEnabled(cell, false);
                    }
                }
            }

//            int vertexCount = vertices.size();
//            double r = Math.max(vertexCount * maxDimension / Math.PI, radius);
//
//            // Moves the circle to the specified origin
//            if (moveCircle) {
//                left = x0;
//                top = y0;
//            }

            // mxGraph mxgraph = getGraph();
            // mxRectangle bounds = mxgraph.getMaximumGraphBounds();
            mxRectangle bounds = new mxRectangle( 0,0, 1000.0, 1000.0);

            // Move the moveable vertices
            for (int i = 0; i < moveVertices.size(); i++) {
                Object vertex = moveVertices.get(i);
                if (isVertexMovable(vertex)) {
                    if (vertex instanceof mxCell) {
                        mxCell cell = (mxCell) vertex;
                        mxRectangle cellBounds = getVertexBounds(cell);
                        Object value = cell.getValue();
                        if (value instanceof RowCol) {
                            RowCol rowCol = (RowCol) value;
                            double cellHeight = bounds.getHeight() / 9.0;
                            double rowOffset = cellBounds.getHeight() * (rowCol.col() % 2);
                            double cellWidth = bounds.getWidth() / 9.0;
                            double colOffset = cellBounds.getWidth() * (rowCol.row() % 2);
                            setVertexLocation(vertex,
                                    x0 + left + rowCol.col() * cellWidth + colOffset,
                                    y0 + top + rowCol.row() * cellHeight + rowOffset);
                        }
                    }
                }
            }
        } finally {
            model.endUpdate();
        }
    }
}