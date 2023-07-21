package info.danbecker.ss.tree;

import org.jgrapht.Graph;
import org.jgrapht.graph.*;
import org.jgrapht.traverse.DepthFirstIterator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Iterator;
import java.util.stream.Collectors;

public class GraphTest {
	@BeforeEach
    public void setup() {
	}

	@Test
    public void testBasics() {
		// Test org.jgrapht.*
		// Mostly from https://jgrapht.org/guide/HelloJGraphT
		// Graph<String, DefaultEdge> stringGraph = createStringGraph();
		Graph<String, DefaultEdge> stringGraph = createFig6Graph();

		// note undirected edges are printed as: {<v1>,<v2>}
		System.out.println( "Graph.toString()=" + stringGraph.toString());

		System.out.println( "Graph vertices=" + stringGraph
				.vertexSet()
				.stream()
				.collect(Collectors.joining(",")));

		// Print out the graph to be sure it's really complete
		Iterator<String> iter = new DepthFirstIterator<>(stringGraph);
		while (iter.hasNext()) {
			String vertex = iter.next();
			System.out.println(
					"Vertex " + vertex + " is connected to: "
							+ stringGraph.edgesOf(vertex).toString());
		}
	}


	/**
	 * Create a toy graph based on String objects.
	 *
	 * @return a graph based on String objects.
	 */
	private static Graph<String, DefaultEdge> createStringGraph() {
		Graph<String, DefaultEdge> g = new SimpleGraph<>(DefaultEdge.class);

		String v1 = "v1";
		String v2 = "v2";
		String v3 = "v3";
		String v4 = "v4";

		// add the vertices
		g.addVertex(v1);
		g.addVertex(v2);
		g.addVertex(v3);
		g.addVertex(v4);

		// add edges to create a circuit
		g.addEdge(v1, v2);
		g.addEdge(v2, v3);
		g.addEdge(v3, v4);
		g.addEdge(v4, v1);

		return g;
	}

	/**
	 * Create a toy graph based on String objects.
	 *
	 * @return a graph based on String objects.
	 */
	private static Graph<String, DefaultEdge> createFig6Graph() {
		Graph<String, DefaultEdge> g = new SimpleGraph<>(DefaultEdge.class);

		String rc10 = "rc10";
		String rc18 = "rc18";
		String rc20 = "rc20";
		String rc80 = "rc80";
		String rc88 = "rc88";

		// add the vertices
		g.addVertex(rc10);
		g.addVertex(rc18);
		g.addVertex(rc20);
		g.addVertex(rc80);
		g.addVertex(rc88);

		// add edges to create a circuit
		g.addEdge( rc10, rc18, new LabelEdge("6"));
		g.addEdge( rc10, rc20, new LabelEdge("5"));
		g.addEdge( rc10, rc20, new LabelEdge("5"));
		g.addEdge( rc18, rc88, new LabelEdge("5"));
		g.addEdge( rc20, rc80, new LabelEdge("2"));

		return g;
	}

	/**
	 * A graph edge that has a String label.
	 */
	public static class LabelEdge extends DefaultEdge {
		private String label;

		public LabelEdge(String label) {
			this.label = label;
		}

		public String setLabel() {
			this.label = label;
			return label;
		}
		public String getLabel() {
			return label;
		}

		@Override
		public String toString() {
			return "(" + getSource() + "-" + label + "-" + getTarget()  + ")";
		}
	}

}