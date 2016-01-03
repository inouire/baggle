package inouire.baggle.client.gui.modules;

import java.awt.BorderLayout;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import javax.swing.JFrame;
import inouire.baggle.client.Main;
import inouire.baggle.solver.Solver;

/**
 *
 * @author edouard
 */
public class SolutionsFrame extends JFrame{
    
    private MiniBoardPanel miniboard = new MiniBoardPanel();
    
    //détournement de l'usage original pour y placer les mots de différentes longueur
    private PlayersResultsPanel all_results = new PlayersResultsPanel(true);
    
    public SolutionsFrame(String grid) throws Exception{
        super();
        
        miniboard.updateGrid(grid)
                 .updateMode(Main.connection.GAME_MODE);
        Solver solver;
        solver = new Solver(Main.connection.LANG,Main.connection.PARENTAL_FILTER,Main.connection.BIG_BOARD);
        solver.setMinLength(Main.connection.MIN_LENGTH);
        ArrayList<String> solutions =  solver.solveGrid(grid);
        Collections.sort(solutions,new WordComparator());
        
        all_results.addComponentListener(new ComponentListener() {
            @Override
            public void componentResized(ComponentEvent e) {
                all_results.notifyResize();
            }
            @Override
            public void componentMoved(ComponentEvent e) {}

            @Override
            public void componentShown(ComponentEvent e) {}

            @Override
            public void componentHidden(ComponentEvent e) {}
        });
        
        LinkedList<String> current = new LinkedList<String>();
        int length=solutions.get(0).length();
        for(String word : solutions){
            if(word.length()<length){
               int total_points=current.size()*Solver.getPoints(word,Main.connection.REWARD_BIG_WORDS);
               String intitule = length+" lettres: "+current.size()+" ("+total_points+" points)";
               all_results.addResult(intitule , current);
               length=word.length();
               current = new LinkedList<String>();
            }
            current.add(word);
        }
        
        
        this.setSize(600,600);
        this.setLayout(new BorderLayout());
        this.add(miniboard,BorderLayout.NORTH);
        this.add(all_results,BorderLayout.CENTER);
        this.setTitle("Toutes les solutions");
        this.setLocationRelativeTo(Main.mainFrame);
    }
    

    
}

class WordComparator implements Comparator<String> {

    @Override
    public int compare(String w1, String w2) {
        if (w1.length()>w2.length()){
            return -1;
        }else if (w1.length()<w2.length()){
            return 1;
        }else{
            return 0;
        }
    }
    
}

