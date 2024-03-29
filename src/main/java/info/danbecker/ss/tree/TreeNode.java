package info.danbecker.ss.tree;

import info.danbecker.ss.Candidates;
import info.danbecker.ss.RowCol;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import static info.danbecker.ss.Candidates.FULL_COMBI_MATCH;
import static info.danbecker.ss.Candidates.NAKED;

/**
 * TreeNode - an implementation of a tree.
 * TreeNodes have data, parent, and children.
 * The implementation can be a general tree 0* number of children,
 * or an N-ary tree with exactly N children which can be null.
 * See TreeIter for iteration, TreeNodeTest for searching.
 * <p>
 * Implementation largely taken from 
 * https://stackoverflow.com/questions/3522454/how-to-implement-a-tree-data-structure-in-java
 * https://github.com/gt4dev/yet-another-tree-structure
 * <p>
 * Original implementation had an index on every node for search, but
 * this implementation removes that and has tree traversal.
 */
public class TreeNode<T> implements Iterable<TreeNode<T>>, Cloneable {
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
		this.children = new LinkedList<>();
	}

	public TreeNode(T data, int nAry) {
		// Creates the N-ary tree with linked list.
		this.data = data;
		if (nAry < 1)
			throw new IllegalArgumentException( "TreeNode with nAry=" + nAry );
		this.nAry = nAry;
		this.children = new ArrayList<>( nAry );
	}

	/** Makes an initialized copy of the given data.
	 * The user must clone/deepCopy the list if desired.
	 * @param data
	 * @param nAry
	 * @param children
	 */
	public TreeNode(T data, int nAry, List<TreeNode<T>> children ) {
		// Creates the N-ary tree with linked list.
		this.data = data;
		if (nAry < 0)
			throw new IllegalArgumentException( "TreeNode with nAry=" + nAry );
		this.nAry = nAry;
		this.children = children;
	}

	/** Clones a TreeNode
	 *
	 * @return
	 */
	@Override
	public Object clone() {
		// See https://stackoverflow.com/questions/1138769/why-is-the-clone-method-protected-in-java-lang-object
		// This use of TreeNode should be used with Cloneable data.
		T cData = data; // for String
		try {
			// Will do NoSuchMethodException for String
			// Will call clone for classes that implement that.
			// Method clone = Object.class.getMethod("clone");
			// cData = (T) clone.invoke( this.data );
			super.clone();
		} catch (CloneNotSupportedException e) {
			System.out.println( "Clone exception=" + e );
		}
		if ( 0 == nAry )
			return new TreeNode<>( cData, this.nAry, new LinkedList<>( this.children ) );
		else
			return new TreeNode<>( cData, this.nAry, new ArrayList<>( this.children ) );
		// TODO Recurse and make deep copy.
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
	 * For nAry tree, this includes self, children, and null child nodes.
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
	 * For nAry tree, this includes self, children, and null child nodes.
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
		// for ( int i = 0; i < children.size(); i++) {
			int nodeLevel = node.getLevel();
			String indent = "   ".repeat(Math.max(0, nodeLevel - startLevel));
			// String nodeInfo = (null == node.data ) ? "no " + Utils.Unit.values()[i].toString().toLowerCase(): node.data.toString();
			System.out.printf("%s%d-%d-%s%n", indent, nodeLevel, nodeCount++, node.data);
		}
	}

	/**
	 * A node is a leaf when ALL children are null or null data.
	 * @return
	 */
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

	/**
	 * This searches the given node and below,
	 * and returns a list of ALL leaf nodes visible from this node.
	 */
	public List<TreeNode<T>> findLeafNodes() {
		List<TreeNode<T>> list = new ArrayList<>();
		if ( isLeaf() ) {
			list.add(this);
			return list;
		}
		for ( int childi = 0; childi < children.size(); childi++ ) {
			TreeNode<T> child = this.getChild(childi);
			if ( null != child && null != child.data) {
				child.findLeafNodes( list );
			}
		}
		return list;
	}

	/**
	 * This searches the given node and below,
	 * and returns a list of ALL leaf nodes visible from this node.
	 */
	protected void findLeafNodes( List<TreeNode<T>> list ) {
		if ( isLeaf() ) {
			list.add(this);
			return;
		}
		// Recurse children
		for ( int childi = 0; childi < children.size(); childi++ ) {
			TreeNode<T> child = this.getChild(childi);
			if ( null != child && null != child.data) {
				child.findLeafNodes( list);
			}
		}
	}

	/** A node level is 0 for the root, or
	 * the number of hops to get to the root.
	 * @return
	 */
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
				TreeNode<T> childNode = new TreeNode<>(null, nAry);
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
				TreeNode<T> childNode = new TreeNode<>(null, nAry);
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
		TreeNode<T> childNode = new TreeNode<>(childData);
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
				TreeNode<T> childNode = new TreeNode<>(null, nAry);
				childNode.parent = this;
				children.add(childi, childNode);
				// this.registerChildForSearch(childNode);
			}
		}
		// Remove previous node from tree (set parent null)
		TreeNode<T> prevChildNode = children.get( i );
		prevChildNode.parent = null;

		// Add childNode to children
		TreeNode<T> childNode = new TreeNode<>(childData, nAry);
		childNode.parent = this;
		if ( !children.contains(childNode) ) {
			children.set(i, childNode);
		}
		return childNode;
	}

	/** This searches the given node and below,
	 * and returns the FIRST non-null node with matching data.
	 * <p></p>
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
	 * and returns a list of ALL non-null nodes with matching data.
	 */ 
	public List<TreeNode<T>> findTreeNodes(Comparable<T> cmp) {
		List<TreeNode<T>> list = new LinkedList<>();
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

	/**
	 * Report a unique set of path ids from root to here
	 * @return
	 */
	public int[] getPath() {
		if ( this.isRoot() ) {
			return new int[]{ 0 };
		} else {
			int level = getLevel();
			// Create path based on my level
			int[] pathIds = new int[level + 1];
			pathIds[ level ] = getParent().getChildPath( this );
			getParent().getPath( pathIds );
			return pathIds;
		}
	}

	protected void getPath( int [] pathIds ) {
		if (null == parent) {
			pathIds[0] = 0;
		} else {
			// Fill in my level.
			int level = getLevel();
			pathIds[ level ] = getParent().getChildPath( this );
			// Have parent fill in their level.
			getParent().getPath( pathIds );
		}
	}

	public int getChildPath( TreeNode<T> child ) {
		for ( int childi = 0; childi < children.size(); childi++ ) {
			TreeNode<T> potential = this.getChild(childi);
			if ( child.equals( potential )) {
				return childi;
			}
		}
		return -1;
	}

	public String toString( List<TreeNode<T>> tree ) {
		if ( null == tree )
			return "null";
		StringBuilder sb = new StringBuilder( "(" + tree.size() + " nodes)");
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
		return new TreeNodeIter<>(this);
	}

	/**
	 * Looks for an endpoint to a chain, which sees only one location.
	 * Returns the location index, or -1 if one does not match (a loop).
	 * @param candidates
	 * @param zeroBasedDigits
	 * @param locs
	 * @return index in locs which sees one other location, -1 otherwise
	 */
	public static int pickMinSights(Candidates candidates, int[] zeroBasedDigits, List<RowCol> locs ) {
		List<Integer> sees = new LinkedList<>();
		for ( int loci = 0; loci < locs.size(); loci++) {
			RowCol loc = locs.get( loci );
			int locSightCount =
					candidates.candidateComboRowLocations( loc.row(), zeroBasedDigits, NAKED, FULL_COMBI_MATCH ).size() - 1; // subtract self loc
			locSightCount +=
					candidates.candidateComboColLocations( loc.col(), zeroBasedDigits, NAKED, FULL_COMBI_MATCH ).size() - 1; // subtract self loc
			// do not count box if unit was counted in same row or same col (
			List<RowCol> boxLocs = candidates.candidateComboBoxLocations( loc.box(), zeroBasedDigits, NAKED, FULL_COMBI_MATCH );
			for ( RowCol boxLoc : boxLocs) {
				if ( !loc.equals( boxLoc ) && loc.row() != boxLoc.row() && loc.col() != boxLoc.col())  {
					locSightCount++;
				}
			}
			sees.add( locSightCount );
		}
		int min = Integer.MAX_VALUE;
		int mini = -1;
		// Find mininum sees count and index of this count.
		for ( int seei = 0; seei < sees.size(); seei++) {
			int units = sees.get( seei );
			if ( units < min ) {
				min = units;
				mini = seei;
			}

		}
		if ( -1 != mini ) {
			if ( 1 == min) {
				// System.out.println("   tree end min loc=" + pairLocs.get(mini) + ", sees=" + sees.get(mini));
				return mini;
			} else {
				// System.out.println("   no end min loc=" + pairLocs.get(mini) + ", sees=" + sees.get(mini));
				return -1;
			}
		}
		return -1;
	}

	/** A String comparator for trees of String data. */
	public static class StringContains implements Comparable<String> {
		String searchStr;

		public StringContains( String searchStr ) {
			this.searchStr = searchStr;
		}

		@Override
		public int compareTo(String treeData) {
			if (treeData == null)
				return 1;
			return treeData.contains(searchStr) ? 0 : 1;
		}
	}
}
