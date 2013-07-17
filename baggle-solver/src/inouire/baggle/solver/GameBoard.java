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
import java.util.Random;
/**
 *
 * @author Edouard de Labareyre
 */
public class GameBoard {

    private final static Random r = new Random();
    private final Dice[][] Board;

    private static Dice[] Dices;

    private BoardType type;
    private int size;

    //dices distribution for french 4x4 grid
    final static Dice[] dices_fr_4=new Dice[]{
        new Dice("V","E","Z","A","N","D"),
        new Dice("Y","N","G","L","E","U"),
        new Dice("A","A","O","I","E","T"),
        new Dice("R","S","N","H","E","I"),
        new Dice("K","O","T","U","E","N"),
        new Dice("E","L","R","W","I","U"),
        new Dice("S","F","E","H","I","E"),
        new Dice("M","A","R","O","S","I"),
        new Dice("R","A","F","X","I","O"),
        new Dice("S","A","C","E","L","R"),
        new Dice("I","T","E","V","G","N"),
        new Dice("E","D","O","N","S","T"),
        new Dice("B","A","J","O","M","Q"),
        new Dice("L","E","P","U","S","T"),
        new Dice("A","P","E","C","M","D"),
        new Dice("L","I","B","A","T","R")
    };
    
    //dices distribution for french 5x5 grid
    final static Dice[] dices_fr_5=new Dice[]{
        new Dice("N","D","H","S","N","M"),
        new Dice("M","D","N","S","N","H"),
        new Dice("G","F","S","T","E","Y"),
        new Dice("L","M","T","R","X","S"),
        new Dice("T","T","R","S","C","H"),
        new Dice("B","M","L","N","D","L"),
        new Dice("T","M","R","D","B","T"),
        new Dice("E","I","U","E","A","O"),
        new Dice("R","L","X","S","S","B"),
        new Dice("N","A","A","T","E","Q"),
        new Dice("T","C","J","F","S","H"),
        new Dice("I","E","E","A","O","A"),
        new Dice("I","A","A","I","E","O"),
        new Dice("O","E","U","E","I","A"),
        new Dice("L","C","P","R","J","S"),
        new Dice("D","S","T","L","S","M"),
        new Dice("N","K","L","P","F","N"),
        new Dice("D","W","R","N","L","P"),
        new Dice("R","Z","N","N","T","Q"),
        new Dice("R","G","L","R","V","F"),
        new Dice("R","V","C","G","R","T"),
        new Dice("I","I","O","E","A","E"),
        new Dice("E","U","I","A","E","O"),
        new Dice("U","I","A","E","O","A"),
        new Dice("N","S","E","V","A","E")
    };
    
    //dices distribution for english 4x4 grid
    final static Dice[] dices_en=new Dice[]{
        new Dice("P","C","H","O","A","S"),
        new Dice("O","A","T","T","O","W"),
        new Dice("L","R","Y","T","T","E"),
        new Dice("V","T","H","R","W","E"),
        new Dice("E","G","H","W","N","E"),
        new Dice("S","E","O","T","I","S"),
        new Dice("A","N","A","E","E","G"),
        new Dice("I","D","S","Y","T","T"),
        new Dice("M","T","O","I","C","U"),
        new Dice("A","F","P","K","F","S"),
        new Dice("X","L","D","E","R","I"),
        new Dice("E","N","S","I","E","U"),
        new Dice("Y","L","D","E","V","R"),
        new Dice("Z","N","R","N","H","L"),
        new Dice("N","M","I","Q","H","U"),
        new Dice("O","B","B","A","O","J")
    };
    
    //TODO add english dices for 5x5 grid

    /**
     * Create a board with real dices
     */
    public GameBoard(BoardType board_type , String language){
        this.type = board_type;
        this.size = board_type.getSize();
        
        if(language.equals("fr")){
            if(this.size == 4){
                Dices=dices_fr_4;
            }else{
                Dices=dices_fr_5;
            }
        }else if(language.equals("en")){
            Dices=dices_en;
        }
        this.Board = new Dice[this.size][this.size];
    }

    public String mixBoard(){
        for(int i=0 ; i < size ; i++){
            for(int j=0 ; j <  size ; j++){
                Board[i][j] = null;
            }
        }
        int x ,y;
        for(Dice d : Dices ){
            d.rollDice();
            do{
                x = r.nextInt(size);
                y = r.nextInt(size);
            }while(Board[x][y]!=null);
            Board[x][y] = d;
            //SimpleLog.
        }
        return this.toString();
    }

    private char getDice(int i , int j){
        return this.Board[i][j].getDisplayedFace().charAt(0);
    }


    ArrayList<Letter> exportLetters(){
        ArrayList<Letter> L = new ArrayList<Letter>();
        for(int i = 0 ; i < size; i++){
            for(int j = 0 ; j < size; j++){
                L.add(new Letter(getDice(i, j),i,j));
            }
        }
        for(Letter l : L){
            l.neighbors.clear();
            l.findNear(L);
        }
        return L;
    }

    @Override
    public String toString(){
        String s="";
        for(int i=0 ; i < size ; i++){
            for(int j=0 ; j < size ; j++){
                s += getDice(i, j);
            }
        }
        return s;
    }
}

class Dice {

    private static Random r=new Random();
    private String[] faces;
    private int displayed_face=-1;//pas de face affichee

    Dice(String l1 , String l2 , String l3 , String l4 , String l5 , String l6){
        this.faces = new String[6];
        this.faces[0] = l1;
        this.faces[1] = l2;
        this.faces[2] = l3;
        this.faces[3] = l4;
        this.faces[4] = l5;
        this.faces[5] = l6;
    }

    String getDisplayedFace(){
        if(displayed_face!=-1){
            return this.faces[displayed_face];
        }else{
            return "#";
        }
    }

    void rollDice(){
        this.displayed_face = r.nextInt(6);
    }

}

