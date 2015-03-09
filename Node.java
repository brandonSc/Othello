import java.util.ArrayList;

public class Node
{
    int x, y;
    Node parent;
    ArrayList<Node> children;
    int mm_value;
    int alpha, beta;
    int height;
    NState state;

    public Node ( int x, int y, Node parent ) { 
        this.parent = parent;
        this.x = x;
        this.y = y;
        if ( parent == null ) 
            height = 0;
        else { 
            height = parent.height+1;
            parent.children.add(this);
        }
        this.alpha = -10000;
        this.beta = 10000;
        children = new ArrayList<Node>(4);
    }

    public int calculateValue() { 
        if ( children.size() == 0 ) {
            mm_value = state.getHeuristicValue();
            if ( isCorner(x,y) ) mm_value+=2;
            if ( isWall(x,y) ) mm_value+=1;
            alpha = beta = mm_value;
            return mm_value;
        } else { 
            
            if ( (height % 2) == 0 ) {
                // MAX node, children are MIN
                int v = -10000;
                ArrayList<Node> rem = new ArrayList<Node>();
                for ( Node n : children ) {
                    v = Math.max(v, n.calculateValue());
                    alpha = Math.max(alpha, v); 
                    if ( beta <= alpha ) {
                        //System.out.println("beta pruning: "+n);
                        rem.add(n);
                    }
                }
                for ( Node n : rem ) 
                    children.remove(n);
                return v;
            } else { 
                // MIN node
                int v = 10000;
                ArrayList<Node> rem = new ArrayList<Node>();
                for ( Node n : children ) {
                    v = Math.min(v, n.calculateValue());
                    beta = Math.min(beta, v);
                    if ( beta <= alpha ) { 
                        //System.out.println("alpha pruning: "+n);
                        rem.add(n);
                    }
                }
                for ( Node n : rem ) 
                    children.remove(n);
                return v;
            }
        }
    }

    private boolean isCorner ( int x, int y ) {
        return (x == 0 && y == 0) 
            || (x == 0 && y == 7)
            || (x == 7 && y == 0)
            || (x == 7 && y == 7);
    }

    private boolean isWall ( int x, int y ) { 
        return (x == 0) || (y == 0)
            || (x == 7) || (y == 7);
    }

    @Override
    public String toString() { 
        return "{("+x+","+y+"), h:"
            +height+", "
            +(((height%2)==0)?"MAX":"MIN")
            +", a:"+alpha+", b:"+beta+"}";
    }
    @Override 
    public boolean equals ( Object other ) { 
        Node n = (Node)other;
        return (n.x==x) && (n.y==y);
    }

}
