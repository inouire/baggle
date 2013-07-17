 /* Copyright 2009-2013 Edouard Garnier de Labareyre
  *
  * This file is part of B@ggle.
  *
  * B@ggle is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * B@ggle is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with B@ggle.  If not, see <http://www.gnu.org/licenses/>.
  */
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
import inouire.baggle.solver.BoardType;
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
        
        miniboard.setGrid(grid);
        miniboard.setMode(Main.connection.GAME_MODE);
        Solver solver;
        solver = new Solver(Main.connection.LANG,Main.connection.PARENTAL_FILTER,BoardType.fromGrid(grid));
        solver.setMinLength(Main.connection.MIN_LENGTH);
        ArrayList<String> solutions =  solver.solveGrid(grid);
        Collections.sort(solutions,new WordComparator());
        
        all_results.addComponentListener(new ComponentListener() {
            public void componentResized(ComponentEvent e) {
                all_results.notifyResize();
            }
            public void componentMoved(ComponentEvent e) {}

            public void componentShown(ComponentEvent e) {}

            public void componentHidden(ComponentEvent e) {}
        });
        
        LinkedList<String> current = new LinkedList<String>();
        int length=solutions.get(0).length();
        for(String word : solutions){
            if(word.length()<length){
               int total_points=current.size()*Solver.getPoints(length);
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

