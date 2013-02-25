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

package inouire.baggle.solver;

import java.util.ArrayList;
import java.util.LinkedList;

/**
 *
 * @author Edouard de Labareyre
 */
public class Solver {

    private DawgDictionnary dictionnary = new DawgDictionnary();
    private DawgDictionnary parentalFilter = new DawgDictionnary();

    private boolean PARENTAL_FILTER=false;
    private int MIN_LENGTH=3;
    private boolean BIG_GRID=false;
    private int MAX_LENGTH=16;
    
    private ArrayList<Letter> letters;
    private LinkedList<Letter> already_seen=new LinkedList<Letter>();
    private ArrayList<String> found = new ArrayList<String>();

    /**
     * Default constructor for Solver
     * @param language
     * @param with_parental_filter
     * @param big_grid 
     */
    public Solver(String language,boolean with_parental_filter,boolean big_grid) throws Exception
    {
        BIG_GRID = big_grid;
        PARENTAL_FILTER=with_parental_filter;
        if(BIG_GRID){
            MAX_LENGTH = 25;
        }
        //build dict name from args
        String dict_reference="dawg_dict_";
        dict_reference+=language;
        if(big_grid){
            dict_reference+="_5x5";
        }
        dict_reference+=".dat";
        
        //create dawg stucture from file
        dictionnary.createDawg(dict_reference);

        //load parental filter if necessary
        if(PARENTAL_FILTER){
            parentalFilter.createDawg("dawg_blacklist_"+language+".dat");
        }
    }

    /**
     * Set a min length for the words that shall be found by the solver.
     * @param min_length 
     */
    public void setMinLength(int min_length)
    {
        this.MIN_LENGTH = min_length;
    }
    
    /**
     * Solve a boggle grid based on a GameBoard structure
     * @param board
     * @return the list of words found in the grid by the solver
     */
    public synchronized ArrayList<String> solveGrid(GameBoard board)
    {    
        //build special structure used by solver
        try {
            letters = board.exportLetters();
        } catch (Exception ex) {
            System.out.println("Error while preparing grid !");
            return null;
        }
        
         //solve the grid and return solutions
        return solveGrid();
    }

    /**
     * Solve a boggle grid based on a string
     * @param grid
     * @return  the list of words found in the grid by the solver
     */
    public synchronized ArrayList<String> solveGrid(String grid)
    {
        //guess grid size
        int SIZE=(int) Math.floor(Math.sqrt(grid.length()));

        //build special structure needed by solver
        char[][] board=new char[SIZE][SIZE];
        for(int i=0 ; i < SIZE ; i++){
            for(int j=0 ; j <  SIZE ; j++){
                board[i][j] = grid.charAt(i+SIZE*j);
            }
        }
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
        
        //solve the grid and return solutions
        return solveGrid();
    }

    private ArrayList<String> solveGrid()
    {
        found.clear();
        for(Letter L:letters){
            usolvePrefix(L,"");
        }
        return found;
    }

    private void usolvePrefix(Letter L, String word)
    {
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
        if(word.length() > MAX_LENGTH){
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

    /**
     * Get the total number of points of a given set of solutions
     * @param solutions
     * @return the nb of points that the grid worth
     */
    public static int getNbPoints(ArrayList<String> solutions)
    {
        int r=0;
        for(String s:solutions){
            r+=getPoints(s);
        }
        return r;
    }

    /**
     * Get the number of points that a word worth
     * @param word
     * @return the nb of points that the word wort
     */
    public static int getPoints(String word)
    {
        return getPoints(word.length());
    }
        
    /**
     * Get the number of points that a word size worth
     * @param word_size the size of the word
     * @return the nb of points that the word worth
     */
    public static int getPoints(int word_size)
    {
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
}

class Letter{
	
    char name;
    int i;
    int j;
    LinkedList<Letter> neighbors=new LinkedList<Letter>();
	
    public Letter(char name,int i,int j)
    {
         this.name = name;
         this.i = i;
         this.j = j;
    }

    public void findNear(ArrayList<Letter> L)
    {
        for(Letter l : L){
            int x = l.i;
            int y = l.j;
            if((Math.abs(x-i)<=1) && (Math.abs(y-j)<=1) && !(i==x && j==y)){
                neighbors.add(l);
            }
        }
    }

}
