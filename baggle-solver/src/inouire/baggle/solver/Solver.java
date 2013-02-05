 /* Copyright 2009-2012 Edouard Garnier de Labareyre
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

package inouire.baggle.solver;

import java.util.ArrayList;
import java.util.LinkedList;

/**
 *
 * @author Edouard de Labareyre
 */
public class Solver {

    public Dawg dictionnary = new Dawg();
    private Dawg parentalFilter = new Dawg();

    private ArrayList<Letter> letters;

    private LinkedList<Letter> already_seen=new LinkedList<Letter>();
    private ArrayList<String> found = new ArrayList<String>();

    private boolean PARENTAL_FILTER=false;
    private int MIN_LENGTH=3;

    public Solver(String language,boolean with_parental_filter){
        PARENTAL_FILTER=with_parental_filter;
        try {
            System.out.print("Building "+language+" dictionnary...");
            //dictionnary.createDictionnary(language);
            dictionnary.createDawg("dawg_dic_"+language+".dat");
            System.out.println("done.");
        } catch (Exception ex) {
            System.out.println("Error while building dictionnary:"+ ex);
            System.out.println("Exiting");
            System.exit(1);
        }
        try {
            if(PARENTAL_FILTER){
                System.out.print("Building "+language+" parental filter...");
                parentalFilter.createDawg("dawg_blacklist_"+language+".dat");
                System.out.println("done.");
            }
        } catch (Exception ex) {
            System.out.println("Error while building parental filter dictionnary:"+ ex);
        }
    }

    public synchronized ArrayList<String> solveGrid(GameBoard board,int min_length){
        System.out.print("Computing and solving generated grid...");

        MIN_LENGTH=min_length;

        //build special structure used by solver
        try {
            letters = board.exportLetters();
        } catch (Exception ex) {
            System.out.println("Error while preparing grid !");
            return null;
        }
        
        //solve the grid
        double duration=System.currentTimeMillis();
        ArrayList<String> solutions=solvePrefix();
        duration=System.currentTimeMillis()-duration;
        System.out.println("Grid computed and solved in "+duration+" ms");

        //return solutions
        return solutions;
    }

    public synchronized ArrayList<String> solveGrid(String grid , int min_length){
        System.out.println("Computing and solving received grid...");

        MIN_LENGTH=min_length;
        
        //guess grid size
        int SIZE=(int) Math.floor(Math.sqrt(grid.length()));
        
        char[][] board=new char[SIZE][SIZE];
        for(int i=0 ; i < SIZE ; i++){
            for(int j=0 ; j <  SIZE ; j++){
                board[i][j] = grid.charAt(i+SIZE*j);
            }
        }

        //build special structure used by solver
        this.letters = new ArrayList<Letter>();
        for(int i = 0 ; i < SIZE; i++){
            for(int j = 0 ; j < SIZE; j++){
                this.letters.add(new Letter(board[i][j],i,j));
            }
        }
        for(Letter l :  this.letters){
            l.neighbors.clear();
            l.findNear( this.letters);
        }
        
        //solve the grid
        double duration=System.currentTimeMillis();
        ArrayList<String> solutions=solvePrefix();
        duration=System.currentTimeMillis()-duration;
        System.out.println("Grid computed and solved in "+duration+" ms");

        //return solutions
        return solutions;
    }

    private ArrayList<String> solvePrefix(){
        found.clear();
        for(Letter L:letters){
            usolvePrefix(L,"");
        }
        return found;
    }

    private void usolvePrefix(Letter L, String word){
        already_seen.addFirst(L);
        word+=L.name;
        if(word.length()>=MIN_LENGTH){
            if(!dictionnary.containsPrefix(word)){
                already_seen.removeFirst();
                return;
            }
            if(dictionnary.contains(word)){
                if(!found.contains(word)){
                    if(PARENTAL_FILTER){
                        if(!parentalFilter.contains(word)){
                            found.add(word);
                        }
                    }else{
                        found.add(word);
                    }
                }
            }
        }
        if(word.length() > 16){
            already_seen.removeFirst();
            return;
        }
        for(Letter V : L.neighbors){
            if(!already_seen.contains(V)){
                usolvePrefix(V,word);
            }
        }
        already_seen.removeFirst();
    }

    public static int getNbPoints(ArrayList<String> solutions){
        int r=0;
        for(String s:solutions){
            r+=getPoints(s);
        }
        return r;
    }

    /**
     * Get the nb of point that a word size worth
     * @param word_size the size of the word
     * @return the nb of points that the word worth
     */
    public static int getPoints(int word_size){
        switch(word_size){
            case 3:
            case 4:
                return 1;
            case 5:
                return 2;
            case 6:
                return 3;
            case 7:
                return 5;
            case 8:
            case 9:
            case 10:
            case 11:
            case 12:
            case 13:
            case 14:
            case 15:
            case 16:
                return 11;
        }
        return 0;
    }
    
    public static int getPoints(String a){
        return getPoints(a.length());
    }

}

class Letter{
	
    char name;
    int i;
    int j;
    LinkedList<Letter> neighbors=new LinkedList<Letter>();
	
    public Letter(char name,int i,int j){
         this.name = name;
         this.i = i;
         this.j = j;
    }

    public void findNear(ArrayList<Letter> L){
        for(Letter l : L){
            int x = l.i;
            int y = l.j;
            if((Math.abs(x-i)<=1) && (Math.abs(y-j)<=1) && !(i==x && j==y)){
                neighbors.add(l);
            }
        }
    }

}
