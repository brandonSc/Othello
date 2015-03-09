public class Pos
{
    int x, y;

    Pos ( int x, int y ) {
        this.x = x;
        this.y = y;
    }

    @Override public String toString() { 
        return "("+x+","+y+")";
    }
}

