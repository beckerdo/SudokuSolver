package info.danbecker.ss.graph;

import info.danbecker.ss.RowCol;
import org.jgrapht.Graph;
import org.jgrapht.GraphPath;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A class to help find edge patterns in graph paths using regular expressions.
 * <p>
 * A simplified edge label encoding makes the patterns easier to write.
 * This encoding described in method {@link #labelEncode(String) Encode}
 * <p>
 * Many problems with Java Regular Expressions, especially with group
 * captures. Lots of fix-ups and checks in  {@link #getMatches()}.
 * <p>
 * Note that the patterns might be precompil-able. The Matchers
 * might be created from a compiled Pattern once a new graph is given.
 */
public class EdgePatternFinder {
    // Lots of deficiencies in the Java regex grouping and naming.
    public static final String XX_NAME = "xx";
    // protected static final String XX_PATTERN = "(?<!\\k<x>)(?<x>[1-9]{1})\\k<x>{1}(?!\\k<x>)"; // no preceding following x
    // protected static final String XX_PATTERN = "(?:[^123456789])(?<x>[1-9]{1})\\k<x>{1}(?!\\k<x>)"; // no preceding following x
    protected static final String XX_PATTERN = "(?<x>[1-9]{1})\\k<x>{1,}"; // 2 or more x
    // protected static final String XX_PATTERN = "(?<x>[1-9]{1})\\k<x>{1}(?!\\k<x>)"; // no following x
    // protected static final String XX_PATTERN = "(?<x>[1-9]{1})\\k<x>{1}(?<y>[1-9]*)\\z"; // finds 11
    // protected static final String XX_PATTERN = "(?<x>[11|22|33|44|55|66|77|88|99])(?<y>[1-9])"; // finds 11
    // protected static final String XX_PATTERN = "(?<x>[(1{2}2{2}3{2}4{2}5{2}6{2}7{2}8{2}9{2}])(?<y>[1-9])"; // finds 11

    public static final String XYX_NAME = "xyx";
    // What XYX works at https://regex101.com
    // public static final String XX_MATCH = "/(?'x'[1-9]{2})/g";
    // public static final String XYX_MATCH = "/(?'x'[1-9]{1})(?'y'[1-9]{1})(?P=x)/g";
    protected static final String XYX_PATTERN = "(?<x>[1-9])(?<y>[1-9])\\k<x>"; // finds 555

    public static final Map<String, String> PatternMap = Map.of(XX_NAME, XX_PATTERN, XYX_NAME, XYX_PATTERN);

    protected final GraphPath<RowCol,LabelEdge> gp;

    protected final String pathStr;
    protected final List<Integer> pathStrEdgeis;
    protected final String patternName;
    protected final Pattern pattern;
    protected final Matcher matcher;

    public EdgePatternFinder( final GraphPath<RowCol,LabelEdge> gp, String patternName ) {
        this.gp = gp;
        if ( !PatternMap.containsKey(patternName))
            throw new IllegalArgumentException( "Pattern name \"" + patternName + "\"is not one of the supported regex pattern names" );

        this.pathStr = pathString( gp, patternName.length() );
        this.pathStrEdgeis = labelPosToEdgei( this.pathStr );
        this.patternName = patternName;
        this.pattern = Pattern.compile( PatternMap.get( patternName ) );
        this.matcher = pattern.matcher( pathStr );
    }

    public String pathString() {
        return pathStr;
    }

    /**
     * Encodes and concatenates the labels in the given GraphPath.
     * This encoding described in method {@link #labelEncode(String) Encode}
     * <p>
     * This encoding makes a long graph path sequence easier to scan.
     * For example []-1-[]-18-[]-5-[]-6-[]-5-[]-56-[]
     * becomes "1Ai565Ef"
     * <p>
     * For cycles, the string is prepended a portion of the end of the pattern
     * to help detect wrap-around patterns.
     * @param gp the GraphPath to encode
     * @param patternSize the number of end edges to prepend  on cycles;
     * @return
     */
    public static String pathString( final GraphPath<RowCol,LabelEdge> gp, int patternSize ) {
        // labels are 1,2,2,3,4,5,11,2,223,5,6,5,1,2
        // cycle should include last edge
        // path match for "xyx" should be 345,565.
        // cycle match for "xyx" should be 212,345,565.
        // Regex /(?'x'[1-9]{1})(?'y'[1-9]{1})(?P=x)/g finds 345,565
        List<RowCol> vertices = gp.getVertexList();
        Graph<RowCol,LabelEdge> g = gp.getGraph();
        // List<LabelEdge> edges = gp.getEdgeList();
        boolean cycle = vertices.get( 0 ) == vertices.get( vertices.size() - 1 );
        StringBuilder sb = new StringBuilder();
        if (cycle) {
            // Preload extra labels before end for wraparound
            for ( int vi = patternSize; vi > 1; vi-- )
                sb.append( labelEncode(g.getEdge( vertices.get(vertices.size() - vi), vertices.get(vertices.size() - vi + 1 ))));
        }

        RowCol prevVertex = vertices.get( 0 );
        for ( int verti = 1; verti < vertices.size(); verti++) {
            RowCol thisVertex = vertices.get( verti );
            sb.append( labelEncode(g.getEdge( prevVertex, thisVertex )));
            prevVertex = thisVertex;
        }
        return sb.toString();
    }

    public final static String DIGIT1 = "ABCDEFGHI";
    public final static String DIGITN = "abcdefghi";

    public static String labelEncode(LabelEdge edge) {
        if ( null == edge )
            return "?";
        return labelEncode( edge.getLabel() );
    }

    /**
     * A succinct encoding of LabelEdge text to help make pattern matching easier.
     * <ul>
     * <li>Single digit labels are encoded as [1-9] chars (i.e. 1 -> "1", 9 -> "9")</li>
     * <li>Multi digit labels are encoded as [A-I][a-i]+ chars
     * (i.e. 12 -> "Ab", 19 -> "Aj", 129 -> "Abj")</li>
     * </ul>
     * @param edgeLabel
     * @return
     */
    public static String labelEncode(String edgeLabel) {
        if (null == edgeLabel || 0 == edgeLabel.length()) {
            return "-";
        } else if (1 == edgeLabel.length()) {
            return edgeLabel;
        } else {
            StringBuffer sb = new StringBuffer();
            for (int i = 0; i < edgeLabel.length(); i++) {
                int digit = Integer.parseInt( edgeLabel.substring( i, i+1 ));
                if (0 == i) {
                    sb.append(DIGIT1.charAt(digit-1));
                } else {
                    sb.append(DIGITN.charAt(digit-1));
                }
            }
            return sb.toString();
        }
    }

    /**
     * Decoding of succinct encoding of LabelEdge text to original multi-digit labels.
     * @param encodeStr for example "1Bc4
     * @return decoded string for example 1,23,4
     */
    public static List<String> labelDecode(String encodeStr) {
        List<String> labels = new ArrayList<>();
        if (null == encodeStr || 0 == encodeStr.length()) {
            return labels;
        } else {
            for ( int i = 0; i < encodeStr.length(); i++) {
                String subStr = encodeStr.substring(i,i+1);
                if ( "123456789".contains( subStr ) ) {
                    labels.add( subStr );
                } else if ( DIGIT1.contains( subStr )) {
                    // Convert letters to numerals
                    String multi = new String( String.valueOf( subStr.charAt(0) - "A".charAt(0) + 1 ));
                    while (i+1 <  encodeStr.length() && DIGITN.contains( encodeStr.substring(i+1,i+2) ) ) {
                        subStr = encodeStr.substring(i+1,i+2);
                        multi += String.valueOf(subStr.charAt(0) - "a".charAt(0) + 1 );
                        i++;
                    }
                    labels.add( multi );
                } else {
                    throw new RuntimeException( "Parse exception in string \"" + encodeStr + "\" at position" + i );
                }
            }
        }
        return labels;
    }

    /**
     * Given an encoded String of labels, return vertex source positions for each String position.
     * <p>
     * Could possibly get pos from unencoded LabelEdge.listToString( grapPath.getEdgeList() )
     * </p>
     * @param encodeStr for example "1Bc4"
     * @return vertex position example 0,1,1,2
     */
    public static List<Integer> labelPosToEdgei(String encodeStr) {
        List<Integer> vertPos = new ArrayList<>();
        if (null == encodeStr || 0 == encodeStr.length()) {
            return vertPos;
        } else {
            int verti = -1;
            for ( int posi = 0; posi < encodeStr.length(); posi++) {
                String subStr = encodeStr.substring(posi,posi+1);
                if ( "123456789".contains( subStr ) ) {
                    vertPos.add( ++verti );
                } else if ( DIGIT1.contains( subStr )) {
                    vertPos.add( ++verti );
                } else if ( DIGITN.contains( subStr )) {
                    vertPos.add( verti );
                } else {
                    throw new RuntimeException( "Parse exception in string \"" + encodeStr + "\" at position" + posi );
                }
            }
        }
        return vertPos;
    }

    /**
     * Returns a map with
     * <ul></ul>
     * <li>key of pattern</li>
     * <li>value of list matching RowCols</li>
     * </ul>
     * <p>
     * A single pattern can return one or more locations.
     * For example pattern "88" and path []-8-[]-1-[]-8-[]-8-[]-5-[]-8-[]
     * should return vertices 0 and 3.
     * <p>
     * Multiple patterns can each return one or more locations.
     * For example pattern "66" and "77" and path []-6-[]-6-[]-4-[]-7-[]-7-[]-8-[]
     * should return vertex 0 and 4.
     * <p>
     * Pattern lengths of N return N-1 vertices in the list.
     * For example pattern "565" and path []-1-[]-18-[]-5-[]-6-[]-5-[]-56-[]
     * should return vertices 3 and 4.
     * <p>
     * Note that some techniques such as XX digit repeat require exactly
     * one match in a path, so it is up to those rules to throw XXs, XX/YY out.
     * </p>
     * @return map of patterns and the vertices they encompass.
     */
    public Map<String,List<RowCol>> getMatches() {
        Map<String,List<RowCol>> finds = new HashMap<>();
        List<RowCol> vertices = gp.getVertexList();
        boolean cycle = vertices.get( 0 ) == vertices.get( vertices.size() - 1 );
        List<LabelEdge> edges = gp.getEdgeList();

        // Beware of cycle boundaries
        // For example
        // Cycle=[1,0]-6-[1,8]-5-[8,8]-3-[8,0]-2-[2,0]-5-[1,0]
        // naked pathStr=65325, groupStr match=565
        // cycle pathStr=2565325, groupStr match=565
        // pathStr=2565325, groupStr match=565

        // Beware of repeated patterns
        // cycle pathStr=2565356525, groupStr match=565,565
        // cycle pathStr=2565378725, groupStr match=565,787
        // Matcher matcher = pattern.matcher( pathStr );

        // Beware of path edge sources and targets.
        // You cannot rely on order in edge lists.
        while (matcher.find()) {
            String match = matcher.group();
            // Hack because of lame Java regex group handling
            if ( XYX_NAME.equals( patternName ) ) {
                if ( !matcher.group( "x" ).equals( matcher.group("y" ))) {
                    int adds = addMatchToList( finds, matcher, match,
                        pathStr, pathStrEdgeis, vertices, edges );
                }
            } else if ( XX_NAME.equals( patternName ) ) {
                // Ignore triples
                if ( patternName.length() == match.length() ) {
                    // Ignore wrap-around
                    if ( !pathStr.startsWith( match ) || !pathStr.endsWith( match )) {
                        int adds = addMatchToList( finds, matcher, match,
                            pathStr, pathStrEdgeis, vertices, edges );
                    }
                }
            } else {
                throw new IllegalArgumentException( "Add match code for " + patternName );
            }
        }
        return finds;
    }

    public static int addMatchToList( Map<String,List<RowCol>> finds, Matcher matcher, String match,
        final String pathStr, final List<Integer> pathStrEdgeis,
        final List<RowCol> vertices, final List<LabelEdge> edges ) {
        int adds = 0;
        // System.out.printf( "Pattern=%s,match=%s@%d:%d,labels=%s%n",
        //     pathStr, match, matcher.start(), matcher.end(), LabelEdge.listToString( edges ));
        finds.putIfAbsent( match, new ArrayList<>());
        List<RowCol> locs = finds.get( match );
        // Decode match locations to vertices
        for ( int posi = matcher.start(); posi < matcher.end() - 1; posi++) {
            boolean cycle = vertices.get( 0 ) == vertices.get( vertices.size() - 1 );
            int prefixLen = cycle ? match.length()-1 : 0;
            int edgei = pathStrEdgeis.get(posi) - prefixLen;  // Adjust posi by cycle prefix length
            if (0 > edgei) edgei += edges.size();
            LabelEdge aEdge = edges.get( edgei );
            LabelEdge bEdge = edges.get( (edgei + 1) % edges.size() );
            RowCol shared = getMatchingVertex( aEdge, bEdge );
            // System.out.printf( "Edgei=%d, aEdge=%s (%s,%s), bEdge=%s (%s,%s), shared=%s%n", edgei,
            //         aEdge.getLabel(), aEdge.getSource(), aEdge.getTarget(),
            //         bEdge.getLabel(), bEdge.getSource(), bEdge.getTarget(), shared );
            if ( null != shared )
                adds += RowCol.addUnique( locs, shared );
        }
        return adds;
    }

    /** Return matching vertex of two edges.
     * <p>
     * You cannot count on source or target position
     * going to next edge in edgeList. It depends
     * on the graph path direction.
     * </p>
     * @param aEdge
     * @param bEdge
     * @return matching edge or null if no match
     */
    public static RowCol getMatchingVertex( LabelEdge aEdge, LabelEdge bEdge ) {
        if ( null == aEdge || null == bEdge ) return null;
        RowCol aSrc = aEdge.getSource();
        RowCol aTgt = aEdge.getTarget();
        if ( null == aSrc || null == aTgt ) return null;
        RowCol bSrc = bEdge.getSource();
        RowCol bTgt = bEdge.getTarget();
        if ( null == bSrc || null == bTgt ) return null;

        if ( aSrc.equals( bTgt ) ) return aSrc;
        if ( aSrc.equals( bSrc ) ) return aSrc;
        if ( aTgt.equals( bSrc ) ) return aTgt;
        if ( aTgt.equals( bTgt ) ) return aTgt;
        return null;
    }

    /**
     * A utility method for listing numbered and optional named groups.
     * The string shows all numbered and named groups with
     * a position "@x" where x is the position in the String.
     * @param namesStr comma delimited optional named groups to list
     * @return
     */
    public String matchGroupsToString(String namesStr ) {
        // Warning, problems with matcher reuse.
        // this.matcher = matcher.reset(pathStr);
        // this.matcher = pattern.matcher( pathStr );
        // Matcher matcher = pattern.matcher( pathStr );
        StringBuilder sb = new StringBuilder( );
        for ( int groupi = 0; groupi < matcher.groupCount(); groupi++) {
            if ( 0 < groupi ) sb.append(",");
            sb.append(groupi).append("@").append(matcher.start(groupi)).append("=").append(matcher.group(groupi));
        }
        if (null != namesStr  && 0 < namesStr.length() ) {
            String [] names = namesStr.split( "," );
            for ( int namei = 0; namei < names.length; namei++) {
                sb.append( "," + names[ namei ] + "@" + matcher.start( names[ namei ]) + "=" + matcher.group( names[ namei ] ));
            }
        }
        return sb.toString();
    }
}