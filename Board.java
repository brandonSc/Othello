import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseListener;
import java.awt.event.MouseEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.Timer;

import java.util.ArrayList;

public class Board extends JPanel 
    implements ActionListener, MouseListener, KeyListener
{
    public final int WIDTH = 600;
    public final int HEIGHT = 400;
    public final int TILE_PX = 50;
    public final int TILE_COL = 8;
    public final int TILE_ROW = 8;
    
    private Timer timer;

    private Image[][] tiles;
    private boolean[][] used_tiles;
    private boolean[][] w_tiles; 
    private boolean[][] b_tiles;

    private Image empty_tile;
    private Image white_piece;
    private Image black_piece;

    private boolean p1_turn;
    private boolean p1_AI, p2_AI;
    private int p1_score;
    private int p2_score;
    private boolean inGame;
    private int diff1, diff2; // difficulty of CPU

    private String ai_type;

    private MiniMax ai_search;


    public Board ( String ai_type, int diff1, int diff2 ) { 
        this.ai_type = ai_type;
        this.diff1 = diff1;
        this.diff2 = diff2;
        if ( ai_type.equals("Player vs Player") ) {
            p1_AI = false;
            p2_AI = false;
        } else if ( ai_type.equals("Player vs CPU") ) {
            p1_AI = false;
            p2_AI = true;
        } else if ( ai_type.equals("CPU vs CPU") ) { 
            p1_AI = true;
            p2_AI = true;
        } else { 
            System.err.println("unrecognized game type: "+ai_type);
            System.exit(-1);
        }

        setBackground(Color.GRAY);
        setFocusable(true);
        setPreferredSize(new Dimension(WIDTH, HEIGHT));

        addMouseListener(this);
        addKeyListener(this);

        startGame();
    }

    public boolean[][] getWhiteTiles() { 
        return this.w_tiles;
    } 

    public boolean[][] getBlackTiles() { 
        return this.b_tiles;
    }

    private void startGame() { 
        initImages();
        timer = new Timer(60, this);
        timer.start();
        p1_score = 0;
        p2_score = 0;
        p1_turn = true;
        inGame = true;
    }

    private synchronized void aiMakeTurn() {
        try { Thread.sleep(500); } 
        catch ( Exception e ) { } 
        boolean[][] _p_tiles = new boolean[8][8];
        boolean[][] _o_tiles = new boolean[8][8];
        for ( int i=0; i<w_tiles.length; i++ ) {
            for ( int j=0; j<w_tiles[i].length; j++ ) {
                _p_tiles[i][j] 
                    = (p1_turn) 
                        ? w_tiles[i][j]
                        : b_tiles[i][j];
                _o_tiles[i][j] 
                    = (p1_turn)
                    ? b_tiles[i][j]
                    : w_tiles[i][j];
            }
        }
        ai_search.solve(_p_tiles,_o_tiles); 
        ai_search = null;
    }

    private synchronized void setTile ( int x, int y, char c ) {
        ImageIcon wicon = new ImageIcon("img/tile-piece-white.png");
        ImageIcon bicon = new ImageIcon("img/tile-piece-black.png");

        if ( c == 'w' ) { 
            w_tiles[x][y] = true;
            b_tiles[x][y] = false;
            tiles[x][y] = wicon.getImage();
        } else if ( c == 'b' ) { 
            b_tiles[x][y] = true;
            w_tiles[x][y] = false;
            tiles[x][y] = bicon.getImage();
        }
    }

    public boolean makeTurn ( int x, int y ) {
        if ( p1_turn ) 
            return makeTurn(x,y,'w');
        else 
            return makeTurn(x,y,'b');
    }

    /**
     * Place a piece and flip any opponent pieces
     * @return true on success false if invalid move
     */
    public synchronized boolean makeTurn ( int x, int y, char c ) {
        if ( !inGame || w_tiles[x][y] || b_tiles[x][y] ) 
            return false;
      
        ArrayList<Pos> list = null;
        if ( c == 'w' ) 
            list = searchArea(x,y,w_tiles,b_tiles);
        else 
            list = searchArea(x,y,b_tiles,w_tiles);

        if ( list.isEmpty() ) 
            return false;

        setTile(x, y, c);
        flipPieces(list, c);

        updateScore();

        checkEndConditions();

        p1_turn = !p1_turn;

        return true;
    }

    /**
     * searches the surronding area of a piece 
     * for pieces of the opposite color to flip
     * @return a list containing all pieces to flip
     */
    public ArrayList<Pos> searchArea ( int x, int y, 
            boolean[][] p_tiles, boolean[][] o_tiles ) { 
        
        ArrayList<Pos> list = new ArrayList<Pos>();

        if ( p_tiles[x][y] || o_tiles[x][y] ) 
            return list;

        list.addAll(directedSearch(
                    x,y,"right","none",p_tiles,o_tiles));
        list.addAll(directedSearch(
                    x,y,"left","none",p_tiles,o_tiles));
        list.addAll(directedSearch(
                    x,y,"none","up",p_tiles,o_tiles));
        list.addAll(directedSearch(
                    x,y,"none","down",p_tiles,o_tiles));
        list.addAll(directedSearch(
                    x,y,"right","up",p_tiles,o_tiles));
        list.addAll(directedSearch(
                    x,y,"left","up",p_tiles,o_tiles));
        list.addAll(directedSearch(
                    x,y,"left","down",p_tiles,o_tiles));
        list.addAll(directedSearch(
                    x,y,"right","down",p_tiles,o_tiles));

        return list;   
    }

    public ArrayList<Pos> searchArea ( int x, int y, char c ) { 
        boolean[][] p_tiles = (c=='w') ? w_tiles : b_tiles;
        boolean[][] o_tiles = (c=='w') ? b_tiles : w_tiles;
        return searchArea(x,y,p_tiles,o_tiles);
    }

    public boolean isFeasible ( int x, int y, char c ) { 
        return !searchArea(x,y,c).isEmpty();
    }
    
    public boolean isFeasible ( int x, int y, 
            boolean[][] p_tiles, boolean[][] o_tiles ) { 
        return !searchArea(x,y,p_tiles,o_tiles).isEmpty();
    }

    public ArrayList<Pos> directedSearch ( 
            int x, int y, char c, String dirx, String diry ) { 
        
        boolean[][] p_tiles = (c == 'w') ? w_tiles : b_tiles;
        boolean[][] o_tiles = (c == 'w') ? b_tiles : w_tiles;

        return directedSearch(x,y,dirx,diry,p_tiles,o_tiles);
    }


    public ArrayList<Pos> directedSearch ( 
            int x, int y, String dirx, String diry, 
            boolean[][] p_tiles, boolean[][] o_tiles ) {
       
        ArrayList<Pos> list = new ArrayList<Pos>();

        int dx = 0;
        if ( dirx.equals("right") )
            dx = 1;
        else if ( dirx.equals("left") )
            dx = -1;
            
        int dy = 0;
        if ( diry.equals("down") )
            dy = 1;
        else if ( diry.equals("up") ) 
            dy = -1;

        while ( inBounds((x+=dx), (y+=dy)) ) {
            if ( o_tiles[x][y] )
                list.add(new Pos(x,y));
            else if ( p_tiles[x][y] ) 
                return list;
            else 
                break;
        }

        list.clear();
        return list;
    }

    private boolean inBounds ( int x, int y ) {
       return x >= 0 && x < TILE_ROW 
           && y >= 0 && y < TILE_COL;
    }

    private synchronized void flipPieces( ArrayList<Pos> list, char c ) {
        for ( Pos p : list )  
            setTile(p.x, p.y, c);
    }

    private boolean clearTile ( int x, int y ) { 
        if ( !used_tiles[x][y] )
            return false;

        ImageIcon icon = new ImageIcon("img/tile-white.png");
        used_tiles[x][y] = false;
        b_tiles[x][y] = false;
        w_tiles[x][y] = false;
        tiles[x][y] = icon.getImage();
        return true;
    }

    private void initImages() { 
        tiles = new Image[TILE_ROW][TILE_COL];
        w_tiles = new boolean[TILE_ROW][TILE_COL];
        b_tiles = new boolean[TILE_ROW][TILE_COL];
        used_tiles = new boolean[TILE_ROW][TILE_COL];
        
        ImageIcon icon = new ImageIcon("img/tile-white.png");
        ImageIcon wicon = new ImageIcon("img/tile-piece-white.png");
        ImageIcon bicon = new ImageIcon("img/tile-piece-black.png");
        empty_tile = icon.getImage();
        white_piece = wicon.getImage();
        black_piece = bicon.getImage();

        for ( int i=0; i<tiles.length; i++ ) {
            for ( int j=0; j<tiles[i].length; j++ ) {
                tiles[i][j] = icon.getImage();
                w_tiles[i][j] = false;
                b_tiles[i][j] = false;
                used_tiles[i][j] = false;
            }
        }

        setTile(3,3,'w');
        setTile(3,4,'b');
        setTile(4,3,'b');
        setTile(4,4,'w');
    }

    public void checkEndConditions() { 
        for ( int i=0; i<w_tiles.length; i++ ) {
            for ( int j=0; j<w_tiles[i].length; j++ ) { 
                if ( !w_tiles[i][j] && !b_tiles[i][j] ) {
                    if ( isFeasible(i,j,'w') 
                      || isFeasible(i,j,'b') ) {
                        inGame = true;
                        return;
                    }
                }
            }
        }
        inGame = false;
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        doDrawing(g);
    }

    private void doDrawing(Graphics g) {

        for ( int i=0; i<tiles.length; i++ ) {
            for ( int j=0; j<tiles[i].length; j++ ) {
                g.drawImage(tiles[i][j], i*TILE_PX, j*TILE_PX, this);
            }
        }
        
        drawText(g);
        
        if ( inGame ) { 
            if ( p1_turn && p1_AI && ai_search == null ) {
                ai_search = new MiniMax(this);
                ai_search.maxPly = diff1;
                NState.heuristic = "alt";

                new Thread(new Runnable() {
                    @Override public void run() {
                        aiMakeTurn();
                    }
                }).start();
            } 
            if ( !p1_turn && p2_AI && ai_search == null ) {
                ai_search = new MiniMax(this);
                NState.heuristic = "main";
                if ( ai_type.equals("CPU vs CPU") )
                    ai_search.maxPly = diff2;
                else 
                    ai_search.maxPly = diff1;
                

                new Thread(new Runnable() {
                    @Override public void run() {
                        aiMakeTurn();
                    }
                }).start();
            }
        }
    }

    private void drawText ( Graphics g ) { 
        Image im = (p1_turn) ? white_piece : black_piece;
        Font f = new Font(Font.MONOSPACED, Font.BOLD, 16);
        g.setFont(f);
        g.setColor(Color.BLACK);
        g.drawString("Turn: ", WIDTH-150, 40);
        g.drawImage(im, WIDTH-100, 10, this);
        g.drawImage(white_piece, WIDTH-160, 150,this);
        g.drawImage(black_piece, WIDTH-80, 150, this);
        int o = (p1_score > 9) ? 5 : 0;
        g.drawString(""+p1_score, WIDTH-140-o, 182);
        g.setColor(Color.WHITE);
        o = (p2_score > 9) ? 5 : 0;
        g.drawString(""+p2_score, WIDTH-60-o, 182);

        if ( !inGame ) {  
            g.setColor(Color.BLACK);
            g.setFont(new Font(Font.MONOSPACED, Font.BOLD, 36));
            g.drawString("GAME OVER", (WIDTH/2)-198, HEIGHT/2-8);
            g.setColor(Color.RED);
            g.drawString("GAME OVER", (WIDTH/2)-200, HEIGHT/2-10);
            g.setFont(new Font(Font.MONOSPACED, Font.BOLD, 14));
            
            if (p1_score > p2_score )
                g.drawString("White wins!",
                        WIDTH-130, HEIGHT-40);
            else if (p1_score < p2_score ) 
                g.drawString("Black wins!",
                        WIDTH-130, HEIGHT-40);
            else 
                g.drawString("It's a draw!",
                        WIDTH-130, HEIGHT-40);
        } else {
            g.setFont(new Font(Font.MONOSPACED, Font.BOLD, 14));
            g.drawString("To skip your turn,",
                    WIDTH-180, HEIGHT-40);
            g.drawString("press the space key.",
                    WIDTH-180, HEIGHT-20);
        }
    }

    private void updateScore() { 
        p1_score = p2_score = 0;
        for ( int i=0; i<w_tiles.length; i++ ) {
            for ( int j=0; j<w_tiles[i].length; j++ ) {
                if ( w_tiles[i][j] ) 
                    p1_score++;
                if ( b_tiles[i][j] ) 
                    p2_score++;
            }
        }
    }

    public void nextTurn() { 
        System.out.println("next turn");
        p1_turn = !p1_turn;
    }

    @Override 
    public void mouseReleased ( MouseEvent e ) {
        if ( p1_turn && p1_AI ) return;
        if ( !p1_turn && p2_AI ) return;
        if ( !inGame ) { startGame(); return; }

        int tx = (int)((double)e.getX()/TILE_PX);
        int ty = (int)((double)e.getY()/TILE_PX);

        if ( !inBounds(tx, ty) )
            return;
        
        boolean b = false;
        if ( p1_turn ) 
            b = makeTurn(tx,ty,'w');
        else 
            b = makeTurn(tx,ty,'b');

    }
    
    @Override 
    public void keyReleased ( KeyEvent e ) { 
        //System.out.println(e);
        if ( e.getKeyChar() == ' ' ) { 
            // space pressed 
            // skip move
            p1_turn = !p1_turn;
        }

        if ( !inGame ) 
            startGame();
    }

    @Override
    public void keyTyped ( KeyEvent e ) { 
        //System.out.println(e);
    }

    @Override 
    public void keyPressed ( KeyEvent e  ) { 
        //System.out.println(e);
    }

    @Override 
    public void mouseClicked ( MouseEvent e ) { 
        //System.out.println(e);
    }

    @Override 
    public void mousePressed ( MouseEvent e ) { 
        //System.out.println(e);
    }
    
    @Override 
    public void mouseEntered ( MouseEvent e ) { 
        //System.out.println(e);
    }
    
    @Override 
    public void mouseExited ( MouseEvent e ) { 
        //System.out.println(e);
    }

    @Override
    public void actionPerformed ( ActionEvent e ) {
        repaint();
    }
}
