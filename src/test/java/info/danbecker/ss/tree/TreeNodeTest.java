package info.danbecker.ss.tree;

import org.junit.jupiter.api.Test;

import org.junit.jupiter.api.BeforeEach;

import java.util.List;

import static info.danbecker.ss.Board.ROWCOL;
import static org.junit.jupiter.api.Assertions.*;

public class TreeNodeTest {
	@BeforeEach
    public void setup() {
	}

	// 12 nodes, 1 null, 1 ""
	public static TreeNode<String> getGeneralTree() {
		TreeNode<String> root = new TreeNode<>("root");
		{
			root.addChild("node0");
			root.addChild("node1");
			TreeNode<String> node2 = root.addChild("node2");
			{
				node2.addChild(null);
				TreeNode<String> node21 = node2.addChild("node21");
				{
					TreeNode<String> node210 = node21.addChild("node210");
					node210.addChild("node2100");
					node21.addChild("node211");
					node21.addChild("");
				}
			}
			TreeNode<String> node3 = root.addChild("node3");
			{
				node3.addChild("node30");
			}
		}

		return root;
	}

	// 8 non-null nodes, 1 null data, 1 "", null children
	public static TreeNode<String> getNaryTree() {
		TreeNode<String> root = new TreeNode<>("root", 3);
		{
			root.setChild("node0", 0);
			root.setChild("node1", 1);
			TreeNode<String> node2 = root.setChild("node2", 2);
			{
				TreeNode<String> node21 = node2.setChild("node21",1);
				{
					TreeNode<String> node210 = node21.setChild("node210",1);
					node210.setChild("node2100",1);
					node210.setChild("",2);
					node21.setChild(null, 2);
				}
			}
		}

		return root;
	}

	@Test
    public void testBasics() {
		TreeNode<String> treeRoot = getGeneralTree();
		// treeRoot.printTree();

		assertTrue( treeRoot.getChild(0).isLeaf() );
		assertFalse( treeRoot.getChild(3).isLeaf() );
		assertEquals( 12, treeRoot.nodeCount() );
		assertEquals( 11, treeRoot.size() );
		assertEquals( treeRoot, treeRoot.getChild(0).getParent() );
		assertEquals( treeRoot, treeRoot.getChild(0).getRoot() );
		assertTrue( treeRoot.toString().contains("root") );

		treeRoot = getNaryTree();
		// treeRoot.printTree();

		assertTrue( treeRoot.getChild(0).isLeaf() );
		assertFalse( treeRoot.getChild(2).isLeaf() );
		assertEquals( 16, treeRoot.nodeCount() );
		assertEquals( 8, treeRoot.size() );
		assertEquals( treeRoot, treeRoot.getChild(0).getParent() );
		assertEquals( treeRoot, treeRoot.getChild(0).getRoot() );
		assertTrue( treeRoot.toString().contains("root") );
		assertEquals( 3, treeRoot.getChildren().size() );
		assertEquals( 3, treeRoot.getChild(0).getChildren().size() ); // 3 nodes with null data
	}

	@Test
	public void testStringCloneable() {
		TreeNode<String> tree = getGeneralTree();
		TreeNode<String> treeClone = (TreeNode<String>) tree.clone();

		assertNotSame( tree, treeClone );
		assertEquals( tree.size(), treeClone.size() );
		assertEquals( tree.nodeCount(), treeClone.nodeCount() );
//		System.out.println( "Tree1=" );
//		tree.printTree();
//		System.out.println( "Clone=" );
//		treeClone.printTree();
	}

	@Test
	public void testColorDataCloneable() {
		TreeNode<DigitData> tree = new TreeNode<>(new DigitData( 3, ROWCOL[5][5], 0), 3);
		TreeNode<DigitData> treeClone = (TreeNode<DigitData>) tree.clone();

		assertNotSame( tree, treeClone );
		assertEquals( tree.size(), treeClone.size() );
		assertEquals( tree.nodeCount(), treeClone.nodeCount() );
//		System.out.println( "Tree1=" );
//		tree.printTree();
//		System.out.println( "Clone=" );
//		treeClone.printTree();
	}

	@Test
    public void testIter() {
		TreeNode<String> treeRoot = getGeneralTree();
		assertEquals( 4, treeRoot.getChildCount());
		assertEquals( 12, treeRoot.nodeCount() );
		assertEquals( 4, treeRoot.deepestLevel() );

		// Test TreeNodeIter which is depth first
		int count = 0;
		for ( TreeNode treeNode : treeRoot ) {
			switch ( count ) {
				case 0: { assertEquals( "root", treeNode.data ); break; }
				case 11: { assertEquals( "node30", treeNode.data ); break ; }
			}
			count++;
		}
    }

	@Test
    public void testNaryIter() {
		TreeNode<String> treeRoot = getNaryTree();
		// treeRoot.printTree();
		assertEquals( 3, treeRoot.getChildCount());
		assertEquals( 1, treeRoot.getChild( 2 ).getChildCount());

		assertEquals( 13, treeRoot.nodeCount() );
		assertEquals( 4, treeRoot.deepestLevel() );
	}

	@Test
	public void testSearch() {
		Comparable<String> searchCriteria = new Comparable<>() {
			@Override
			public int compareTo(String treeData) {
				if (treeData == null)
					return 1;
				boolean nodeOk = treeData.contains("210");
				return nodeOk ? 0 : 1;
			}
		};

		TreeNode<String> treeRoot = getGeneralTree();
		// treeRoot.printTree();
		TreeNode<String> found = treeRoot.findTreeNode(searchCriteria);
		found.printTree(); // leave on for code coverage
		assertNotNull( found );
		assertEquals( "node210", found.data );
		assertTrue( found.data.contains("210") );
		assertFalse( found.isLeaf() ); // has a child

		List<TreeNode<String>> list = treeRoot.findTreeNodes(searchCriteria);
		assertNotNull( list );
		assertEquals( 2, list.size());
		assertTrue( list.contains(found) );

		list = treeRoot.findTreeNodes(new MatchAll());
		assertNotNull(list);
		assertEquals( 11, list.size());

		list = treeRoot.findTreeNodes(new MatchNullNode());
		assertNotNull(list);
		assertEquals( 0, list.size());

		// Will not find null or "" nodes
		list = treeRoot.findTreeNodes(new MatchNonZeroLength());
		assertNotNull(list);
		assertEquals( 10, list.size());

		TreeNode<String> naryTreeRoot = getNaryTree();
		found = naryTreeRoot.findTreeNode(searchCriteria);
		assertTrue( found.data.contains("210") );

		list = naryTreeRoot.findTreeNodes(searchCriteria);
		assertEquals( 2, list.size());
		assertTrue( list.contains(found) );

		// nAry does not count null nodes as children can be nulls
		list = naryTreeRoot.findTreeNodes(new MatchAll());
		assertNotNull(list);
		assertEquals( 8, list.size());

		list = naryTreeRoot.findTreeNodes(new MatchNullNode());
		assertNotNull(list);
		assertEquals( 0, list.size());

		// Will not find null or "" nodes
		list = naryTreeRoot.findTreeNodes(new MatchNonZeroLength());
		assertNotNull( list );
		assertEquals( 7, list.size());
	}

	@Test
	public void testEditing() {
		TreeNode<String> r0 = new TreeNode<>("0", 3);
		TreeNode<String> c10 = r0.setChild( "1-0", 0);
		TreeNode<String> c11 = r0.setChild( "1-1", 1);

		assertEquals( 3, r0.size());
		assertEquals( 4, r0.nodeCount()); // one null node
		assertEquals( 1, r0.deepestLevel());

		// This is a chain
		TreeNode<String> s0 = new TreeNode<> ( "s0" );
		TreeNode<String> s10 = s0.addChild( "s1-0" );
		TreeNode<String> s20 = s10.addChild( "s2-0" );
		assertEquals( 3, s0.size());
		assertEquals( 3, s0.nodeCount()); // one null node
		assertEquals( 2, s0.deepestLevel());
		assertEquals( s0, s20.getRoot());

		// This is a fan
		TreeNode<String> f0 = new TreeNode<>("f0", 3);
		TreeNode<String> f10 = f0.setChild( "f1-0", 0);
		TreeNode<String> f11 = f0.setChild( "f1-1", 1);
		TreeNode<String> f12 = f0.setChild( "f1-2", 2);
		TreeNode<String> f20 = f12.setChild( "f2-0", 0);
		assertEquals( 5, f0.size());
		assertEquals( 7, f0.nodeCount()); // two null nodes
		assertEquals( 2, f0.deepestLevel());
		assertEquals( f0, f12.getRoot());

		// No way to edit nodes since setChild and addChild use data not nodes.
		// Add new data to tree, ensure tree values remain same.
		TreeNode<String> f13 = f0.setChild( "f1-3", 2);
		assertEquals( f13, f0.getChild(2));
		assertEquals( f0, f13.getRoot());

		// Assert obliterated node is no longer pointing to old parent, root.
		assertNull( f12.getParent());
		assertEquals( f12, f12.getRoot());
		// Assert child still points to disconnected node.
		assertEquals( f12, f20.getParent());
	}

	/** Finds all nodes in the tree, even if they contain null data. */
	static class MatchAll implements Comparable<String> {
		@Override
		public int compareTo(String that) {
			return 0;
		}
	}

	/** Finds all nodes in the tree that are nulls. */
	static class MatchNullNode implements Comparable<String> {
		@Override
		public int compareTo(String that) {
			//System.out.println( "compareTo");
			if (null == that) return 0;
			return 1;
		}
	}

	/** Finds all nodes in the tree that are nulls. */
	static class MatchNonZeroLength implements Comparable<String> {
		@Override
		public int compareTo(String that) {
			//System.out.println( "compareTo");
			if (null != that && that.length() > 0) return 0;
			return 1;
		}
	}
}