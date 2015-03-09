import javax.swing.JFrame;
import javax.swing.JOptionPane;
import java.awt.EventQueue;
import java.util.ArrayList;

public class Othello extends JFrame
{
    public Othello ( String ai_type ) {
        Object[] possibleValues = { 
            "Player vs Player", 
            "Player vs CPU", 
            "CPU vs CPU" 
        };
        String selectedValue 
            = (String)JOptionPane.showInputDialog(null,
                "Select Game Type:", "Othello",
                JOptionPane.INFORMATION_MESSAGE, null,
                possibleValues, possibleValues[0]);

        int diff1 = 0, diff2 = 0;

        if ( !selectedValue.equals("Player vs Player") ) {
            Object[] possibleDifficulties = {
                "2", "3", "4", "5", "6", "7", "8", "9"
            };
            String s1 = (String)JOptionPane.showInputDialog(null,
                "Select CPU1 difficulty:", "Othello",
                JOptionPane.INFORMATION_MESSAGE, null,
                possibleDifficulties, possibleDifficulties[4]);
            diff1 = Integer.parseInt(s1); 
            if ( selectedValue.equals("CPU vs CPU") ) {
                String s2 = (String)JOptionPane.showInputDialog(null,
                    "Select CPU2 difficulty:", "Othello",
                    JOptionPane.INFORMATION_MESSAGE, null,
                    possibleDifficulties, possibleDifficulties[4]);
                diff2 = Integer.parseInt(s2);
            }
        }

        add(new Board(selectedValue,diff1,diff2));

        setResizable(false);
        pack();

        setTitle("Othello");
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    public static void main ( String args[] ) { 
        String type = "DLS";

        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {                
                JFrame ex = new Othello(type);
                ex.setVisible(true);                
            }
        });
    }
}
