import java.util.LinkedList;
import java.util.ArrayList;

public class MiniMax 
{
    public static int maxPly = 6;
    private Board env;

    public MiniMax ( Board env ) {
        this.env = env;
    }

    public void solve ( boolean[][] p_tiles, boolean[][] o_tiles ) {

        System.out.println("Search Depth = "+maxPly);
        System.out.println("Using heuristic: "+NState.heuristic);

        LinkedList<Node> open = new LinkedList<Node>();
        ArrayList<Node> closed = new ArrayList<Node>();


        Node root = new Node(-1, -1, null);
        boolean[][] r_p_tiles = new boolean[8][8];
        boolean[][] r_o_tiles = new boolean[8][8];
        for ( int k=0; k<p_tiles.length; k++ ) {
            for ( int l=0; l<p_tiles[k].length; l++ ) {
                r_p_tiles[k][l] = p_tiles[k][l];
                r_o_tiles[k][l] = o_tiles[k][l];
            }
        }
        root.state = new NState(r_p_tiles, r_o_tiles);
        open.push(root);

        int nodeCount = 0;

        System.out.println("solving...");
        int curHeight = 0;
        while ( !open.isEmpty() ) {
            Node cur = open.pop();
            nodeCount++;

            boolean min = ((cur.height % 2) == 0);

            boolean[][] _p_tiles = cur.state.p_tiles;
            boolean[][] _o_tiles = cur.state.o_tiles;

            for ( int i=0; i<_p_tiles.length; i++ ) {
                for ( int j=0; j<_p_tiles[i].length; j++ ) {
                    
                    ArrayList<Pos> changes 
                        = env.searchArea(i, j, _p_tiles, _o_tiles);
                    
                    if ( !changes.isEmpty() 
                      && cur.height < maxPly-1 ) { 
                      
                        Node n = new Node(i,j,cur);

                        boolean[][] __p_tiles = new boolean[8][8];
                        boolean[][] __o_tiles = new boolean[8][8];
                        for ( int k=0; k<p_tiles.length; k++ ) {
                            for ( int l=0; l<p_tiles[k].length; l++ ){
                                __p_tiles[k][l] = _p_tiles[k][l];
                                __o_tiles[k][l] = _o_tiles[k][l];
                            }
                        }

                        n.state = new NState(__p_tiles,__o_tiles);
                        n.state.update(changes);
                        cur.calculateValue();
                        open.push(n);
                    } 
                }
            }
            closed.add(cur);
        }
        root.calculateValue();
        System.out.println("Node Count: "+nodeCount);

        // case that no move available
        if ( root.children.isEmpty() ) 
            env.nextTurn();

        for ( Node n : root.children ) {
            if ( n.alpha == root.alpha ) {
                System.out.println("making turn at "+n);
                env.makeTurn(n.x, n.y);
                break;
            }
        }
        //printTree(closed);
    }

    public void printTree( ArrayList<Node> array ) { 
        int h = 0;
        boolean end = false;
        while ( !end ) {
            end = true;
            for ( int i=0; i<array.size(); i++ ) {
                Node n = array.get(i);
                if ( n.height == h ) {
                    System.out.println(n);
                    end = false;
                }
            }
            h++;
        }
    }
}
