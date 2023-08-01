package info.danbecker.ss.graph;

import com.mxgraph.util.mxConstants;
import com.mxgraph.view.mxStylesheet;
import info.danbecker.ss.RowCol;

import javax.swing.*;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.util.Iterator;
import java.util.Map;

import org.jgrapht.*;
import org.jgrapht.ext.*;
import org.jgrapht.graph.DefaultListenableGraph;
import org.jgrapht.graph.SimpleGraph;
import com.mxgraph.layout.*;
import com.mxgraph.swing.*;

/** Draws a graph of JGraphT
 * Graph<RowCol,LabelEdge> graph or GraphPath<RowCol,LabelEdge> graphPath
 * using
 * AWT, Swing, and com.mxgraph.
 * <p>
 * DisplayGraphs hang around until all graph windows are close
 * or the parent thread is gone, so you probably do not want
 * these in automated unit tests.
 * </p>
 */
public class GraphDisplay {
    private static final Dimension DEFAULT_SIZE = new Dimension(1000, 1000);
    protected ListenableGraph<RowCol,LabelEdge> graph;
    protected GraphPath<RowCol,LabelEdge> graphPath;

    public String name;
    public final int indent;
    public GraphDisplay(String name, int indent, GraphPath<RowCol,LabelEdge> graphPath ) {
        this.name = name;
        this.indent = indent;
        this.graph = pathToGraph( graphPath );
        this.graphPath = graphPath;

        mxGraphComponent component = makeComponent( this.graph );
        launchAWT( component );
    }

    public GraphDisplay(String name, int indent, Graph<RowCol,LabelEdge> graph ) {
        this.name = name;
        this.indent = indent;
        this.graph = new DefaultListenableGraph<>(graph);

        mxGraphComponent component = makeComponent( this.graph );
        launchAWT( component );
    }

    public void launchAWT( JComponent component ) {
        // AWT event queue
        EventQueue.invokeLater(new Runnable() { // AWT-EventQuueue
            public void run() {
                // System.out.println("Threads=" + Thread.activeCount() + ", current=" + Thread.currentThread().getName());
                JFrame frame = new JFrame( name );
                frame.setLocation( 10 * indent, 30 * indent );
                frame.setSize(DEFAULT_SIZE );
                frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); // Parent thread calls exit
                // frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // Any thread will cause exit
                frame.getContentPane().add( component );
                frame.setVisible(true);
            }
        });
    }

    public static mxGraphComponent makeComponent(ListenableGraph<RowCol,LabelEdge> graph) {
        // create a JGraphT graph
        JGraphXAdapter<RowCol,LabelEdge> jgxAdapter = new JGraphXAdapter<>(graph);
        //  ListenableGraph<String, DefaultEdge> g = createGraph();
        // Graph<RowCol,BiLocCycleDigitRepeat.LabelEdge> g = createBiLocGraph();
        mxGraphComponent component = new mxGraphComponent(jgxAdapter);
        component.setConnectable(false);
        component.getGraph().setAllowDanglingEdges(false);
        // Muck with edge style
        mxStylesheet styleSheet = component.getGraph().getStylesheet();
        Map<String,Object> edgeStyle = styleSheet.getDefaultEdgeStyle();
        edgeStyle.put( mxConstants.STYLE_ROUNDED, "true" );

        mxGraphLayout layout = new GridLayout(jgxAdapter);
        layout.execute(jgxAdapter.getDefaultParent());
        return component;
    }

    public static ListenableGraph<RowCol,LabelEdge> pathToGraph( GraphPath<RowCol,LabelEdge> gp ) {
        // System.out.println( "Cycle vertices=" + RowCol.toString(gp.getVertexList()));
        Graph<RowCol,LabelEdge> graph = gp.getGraph();
        ListenableGraph<RowCol,LabelEdge> lGraph =
                new DefaultListenableGraph<>(new SimpleGraph<>(LabelEdge.class));
        RowCol lastLoc = null;
        Iterator<RowCol> viterator = gp.getVertexList().iterator();
        while (viterator.hasNext()) {
            RowCol loc = viterator.next();
            if ( null != lastLoc ) {
                lGraph.addVertex( lastLoc );
                lGraph.addVertex( loc );
                LabelEdge edge = graph.getEdge( lastLoc, loc );
                if ( null != edge )
                    lGraph.addEdge( lastLoc, loc, new LabelEdge( edge.getLabel() ));
            }
            lastLoc = loc;
        }
        return lGraph;
    }
}