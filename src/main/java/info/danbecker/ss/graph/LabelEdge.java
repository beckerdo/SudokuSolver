package info.danbecker.ss.graph;

import info.danbecker.ss.RowCol;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultEdge;

import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

/**
 * A special edge for graphs that has
 * a few additional label functions
 * <p>
 * Most functions are for maintining
 * an ordered listof digits for the edge.
 *
 * @author <a href="mailto://dan@danbecker.info>Dan Becker</a>
 */
public class LabelEdge extends DefaultEdge {
    private String label;

    public LabelEdge(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    public String setLabel(String label) {
        this.label = label;
        return label;
    }

    public String addToLabel(int digit) {
        Set<Integer> ints = labelToInts(getLabel());
        ints.add(digit);
        setLabel(intsToLabel(ints));
        return label;
    }

    public String removeFromToLabel(int digit) {
        Set<Integer> ints = labelToInts(getLabel());
        ints.remove(digit);
        setLabel(intsToLabel(ints));
        return label;
    }

    @Override
    public String toString() {
        return label;
    }

    public String toStringVerbose() {
        return getSource() + "-" + label + "-" + getTarget();
    }

    /**
     * Compares two labels.
     * Only match on exact label text.
     *
     * @param label
     * @param anotherLabel
     */
    public static int labelExactCompare(String label, String anotherLabel) {
        if (null == label && null == anotherLabel) return 0;
        if (null == label) return 1;
        if (null == anotherLabel) return -1;
        return label.compareTo(anotherLabel);
    }

    /**
     * Loosely compare two labels.
     * Will also match labels such as
     * 39 to 3 and 9 and
     * 9 to 29 and 49 and
     * 39 to 39.
     *
     * @param label
     * @param anotherLabel
     */
    public static int labelLooseCompare(String label, String anotherLabel) {
        if (null == label && null == anotherLabel) return 0;
        if (null == label) return 1;
        if (null == anotherLabel) return -1;
        if (label.length() == 1 && anotherLabel.contains(label)) return 0;
        if (anotherLabel.length() == 1 && label.contains(anotherLabel)) return 0;
        return label.compareTo(anotherLabel);
    }

    // FYI Utils has digitStringToList
    public static Set<Integer> labelToInts(String label) {
        Set<Integer> ints = label.chars()
                .mapToObj(code -> (char) code)
                .map(dChar -> Integer.parseInt(String.valueOf(dChar)))
                .collect(Collectors.toSet());
        return ints;
    }

    // FYI Utils has digitListToString
    public static String intsToLabel(Set<Integer> ints) {
        TreeSet<Integer> sortedInts = new TreeSet<>(ints);
        String newLabel = sortedInts.stream()
                .map(iStr -> Integer.toString(iStr))
                .collect(Collectors.joining());
        return newLabel;
    }
}