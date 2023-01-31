package info.danbecker.ss.tree;

import static java.lang.String.format;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * TreeNode - an implementation of a tree.
 * TreeNodes have data, parent, and children.
 * The implementation can be a general tree 0* number of children,
 * or an N-ary tree with exactly N children which can be null.
 * See TreeIter for iteration, TreeNodeTest for searching.
 *  
 * Implementation largely taken from 
 * https://stackoverflow.com/questions/3522454/how-to-implement-a-tree-data-structure-in-java
 * https://github.com/gt4dev/yet-another-tree-structure
 * 
 * Original implementation had an index on every node for search , but
 * this implementation removes that and has tree traversal.
 */
public class TreeNode<T> implements Iterable<TreeNode<T>>{

	public T data;
	public int nAry;
	public TreeNode<T> parent;
	public List<TreeNode<T>> children; // used for general tree
	// Do not use TreeNode<T> as Java does not allow. See getIndex for typing.
	// public Object [] nChildren; // used for nAry tee, length is nAry.

	public TreeNode(T data) {
		// Creates the general tree with linked list.
		this.data = data;
		this.nAry = 0;
		this.children = new LinkedList<TreeNode<T>>();
	}
	
	public TreeNode(T data, int nAry) {
		// Creates the N-ary tree with linked list.
		this.data = data;
		if (nAry < 1)
			throw new IllegalArgumentException( "TreeNode with nAry=" + nAry );
		this.nAry = nAry;
		this.children = new ArrayList<TreeNode<T>>( nAry );
	}
	
	public boolean isRoot() {
		return parent == null;
	}

	public TreeNode<T> getRoot() {
		if ( null == parent )
		   return this;
		return parent.getRoot();
	}

	public TreeNode<T> getParent() {
		// Consider making defensive copy
		return parent;
	}

	/** Size of this branch/node of the tree. Includes self and all non-null children.
	 * @return
	 */
	public int size() {
		int size = 0;
		if ( null != data )
			size += 1;
		for ( int i = 0; i < children.size(); i++) {
			TreeNode<T> child = children.get(i);
			if ( null != child )
				size += child.size();
		}
		return size;
	}

	/** For general tree, this equals this node self and all children.
	 * For nAry tree, this include self, children, and null child nodes.
	 * @return
	 */
	public int nodeCount() {
		int nodeCount = 0;
		for (@SuppressWarnings("unused") TreeNode<T> node : this) {
			nodeCount++;
		}
		return nodeCount;
	}

	/** For general tree, this equals this node self and all children.
	 * For nAry tree, this include self, children, and null child nodes.
	 * @return
	 */
	public int deepestLevel() {
		int deepestLevel = -1;
		for (TreeNode<T> node : this) {
			int nodeLevel = node.getLevel();
			if ( nodeLevel > deepestLevel)
				deepestLevel = nodeLevel;
		}
		return deepestLevel;
	}

	/** Prints an indented version of tree. */
	public void printTree() {
		int nodeCount = 0;
		int startLevel = this.getLevel();
		for (TreeNode<T> node : this) {
			int nodeLevel = node.getLevel();
			StringBuilder indent = new StringBuilder();
			for (int i = startLevel; i < nodeLevel; i++) {
				indent.append("   ");
			}
			System.out.println(format("%s%d-%d-%s", indent.toString(), nodeLevel, nodeCount++, node.data));
		}
	}
	
	/** Returns a string of parent info. */
	public String parentString() {
		StringBuilder sb = new StringBuilder();
		TreeNode<T> parent = this.parent;
		while ( null != parent ) {
			sb.append( ", ");
			sb.append( parent.data );
			parent = parent.parent;			
		}
		return sb.toString();
	}

	public boolean isLeaf() {
		if ( 0 == nAry )
			return 0 == children.size();
		for (int childi = 0; childi < nAry; childi++) {
			TreeNode<T> child = getChild(childi);
			if ( null != child && null != child.data )
			   return false;
		}
		return true;
	}

	public int getLevel() {
		if (this.isRoot())
			return 0;
		else
			return parent.getLevel() + 1;
	}

	public List<TreeNode<T>> getChildren() {
		// Populate null children
		if ( nAry > 0 && 0 == children.size()) {
			for ( int childi = 0; childi < nAry; childi++ ) {
				TreeNode<T> childNode = new TreeNode<T>(null, nAry);
				childNode.parent = this;
				children.add(childi, childNode);
				// this.registerChildForSearch(childNode);
			}
		}

		// Consider making defensive copy
		return children;
	}

	public int getChildCount() {
		if ( 0 == nAry)
			return children.size();
		// Count non-null children
		int count = 0;
		for ( int childi = 0; childi < nAry; childi++ ) {
			TreeNode<T> childNode = getChild( childi );
			if ( null != childNode && null != childNode.data)
		       count++;
		}
		return count;
	}

	public TreeNode<T> getChild(int index) {		
		if ( 0 == nAry && index >= children.size())
			throw new ArrayIndexOutOfBoundsException( "Child list length=" + children.size() + ", access index=" + index);
		
		// Populate null children
		if ( nAry > 0 && 0 == children.size()) {
			for ( int childi = 0; childi < nAry; childi++ ) {
				TreeNode<T> childNode = new TreeNode<T>(null, nAry);
				childNode.parent = this;
				children.add(childi, childNode);
				// this.registerChildForSearch(childNode);
			}
		}

		// Consider making defensive copy
		return children.get( index );
	}

	public TreeNode<T> addChild(T childData) {
		if (nAry > 0) 
			throw new IllegalArgumentException( "Use setChild for n-Ary tree");
		TreeNode<T> childNode = new TreeNode<T>(childData);
		childNode.parent = this;
		this.children.add(childNode);
		return childNode;
	}

	public TreeNode<T> setChild(T childData, int i) {
		if (0 == nAry ) 
			throw new IllegalArgumentException( "Use addChild for general tree");
		if ( i >= nAry )
			throw new ArrayIndexOutOfBoundsException( "Child list length=" + children.size() + ", access index=" + i);

		// Populate null children
		if ( nAry > 0 && 0 == children.size()) {
			for ( int childi = 0; childi < nAry; childi++ ) {
				TreeNode<T> childNode = new TreeNode<T>(null, nAry);
				childNode.parent = this;
				children.add(childi, childNode);
				// this.registerChildForSearch(childNode);
			}
		}
		TreeNode<T> childNode = new TreeNode<T>(childData, nAry);
		childNode.parent = this;
		if ( !children.contains(childNode) ) {
			children.set(i, childNode);
		}
		return childNode;
	}

	/** This searches the given node and below,
	 * and returns the first non-null non-null node with matching data. 
	 * If this is a node in the middle of the tree,
	 * it will not find the higher nodes. 
	 * Use root to search entire tree. 
	 * @param cmp
	 * @return
	 */
	public TreeNode<T> findTreeNode(Comparable<T> cmp) {
		// Test self
		if ( 0 == cmp.compareTo( this.data ) )
			return this;

		// Recurse children
		for ( int i = 0; i < children.size(); i++) {
			TreeNode<T> child = children.get(i);			
			if ( null != child ) {
				TreeNode<T> found = child.findTreeNode(cmp);
				if ( null != found) 
					return found;
			}
		}
		return null;
	}

	/** This searches the given node and below,
	 * and returns a list of all non-null nodes with matching data.
	 */ 
	public List<TreeNode<T>> findTreeNodes(Comparable<T> cmp) {
		List<TreeNode<T>> list = new LinkedList<TreeNode<T>> ();
		if (cmp.compareTo(this.data) == 0) {
			list.add(this);
		}
		// Recurse children
		for ( int childi = 0; childi < children.size(); childi++ ) {
			TreeNode<T> child = this.getChild(childi);
			if ( null != child && null != child.data ) {
				List<TreeNode<T>> nodes = child.findTreeNodes( cmp );
				list.addAll( nodes );
			}

		}
		return list;
	}

	public String toString( List<TreeNode<T>> tree ) {
		if ( null == tree )
			return "null tree";
		StringBuffer sb = new StringBuffer( "(" + tree.size() + " nodes)");
		for ( int i = 0; i < tree.size(); i++ ) {
			if ( i > 0 ) sb.append(",");
			TreeNode<T> item = tree.get( i );
			sb.append( item.toString());
		}
		return sb.toString();
	}
	
	@Override
	public String toString() {
		return data != null ? data.toString() : "[data null]";
	}

	@Override
	public Iterator<TreeNode<T>> iterator() {
		return new TreeNodeIter<T>(this);
	}
}