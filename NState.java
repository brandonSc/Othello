import java.util.ArrayList;

public class NState 
{
    public static String heuristic = "main";
    public boolean[][] p_tiles;
    public boolean[][] o_tiles;

    public NState ( boolean[][] p_tiles, boolean[][] o_tiles ) { 
        this.p_tiles = p_tiles;
        this.o_tiles = o_tiles;
    }

    public int getHeuristicValue() { 
        if ( heuristic.equals("main") ) 
            return getPlayerScore() - getOpponentScore();
        else 
            return getPlayerScore();
    }

    public int getPlayerScore() {
        int count = 0;
        for ( int i=0; i<p_tiles.length; i++ ) {
            for ( int j=0; j<p_tiles[i].length; j++ ) { 
                if ( p_tiles[i][j] ) 
                    count++;
            }
        }
        return count;
    }
    
    public int getOpponentScore() {
        int count = 0;
        for ( int i=0; i<o_tiles.length; i++ ) {
            for ( int j=0; j<o_tiles[i].length; j++ ) { 
                if ( o_tiles[i][j] ) 
                    count++;
            }
        }
        return count;
    }

    /** 
     * Takes the player's list of opponent pieces to flip
     */ 
    public void update ( ArrayList<Pos> changes ) { 
        for ( Pos p : changes ) {
            p_tiles[p.x][p.y] = true;
            o_tiles[p.x][p.y] = false;
        }
    }
}
