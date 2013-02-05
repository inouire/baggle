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
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;


/**
 * 
 * @author Edouard de Labareyre
 */
public class TrieDictionnary {
    
    private Node rootnode=null;

    public TrieDictionnary () {
    }

    public void createParentalFilterDictionnary(String language) throws IOException{
        if(!language.equals("fr")){
            System.out.println("No parental filter for language "+language);
            rootnode = new Node(':',new Node(':'));
            return;
        }
        InputStream in;
        URL f = getClass().getResource("/inouire/baggle/solver/blacklist_"+language);
        in = f.openStream();
        BufferedReader Reader = new BufferedReader(new InputStreamReader(in));
        Node root = new Node(':',new Node(':'));
        String line = Reader.readLine();
        while( line != null){
            root.addWord(line);
            line = Reader.readLine();
        }
        in.close();
        rootnode=root;
    }

    public void createDictionnary(String language) throws IOException{
        InputStream in;
        URL f = getClass().getResource("/inouire/baggle/solver/dic_"+language);
        in = f.openStream();
        BufferedReader Reader = new BufferedReader(new InputStreamReader(in));
        Node root = new Node(':',new Node(':'));
        String line = Reader.readLine();
        while( line != null){
            root.addWord(line);
            line = Reader.readLine();
        }
        in.close();
        this.rootnode=root;
    }

    public boolean contains(String s) {
        char [] word=s.toLowerCase().toCharArray();
        Node Ncur = this.rootnode.left;
        for(char c : word){
            while(Ncur.letter != c){
                if (Ncur.right != null){
                    Ncur = Ncur.right;
                }else{
                    return false;
                }
            }
            Ncur = Ncur.left;
        }
        if (Ncur.letter == '.' ){
            return true;
        }else{
            return false;
        }
    }

    public boolean containsPrefix(String s){
        char [] word=s.toLowerCase().toCharArray();
        Node Ncur = this.rootnode.left;
        for(char c : word){
            while(Ncur.letter != c){
                if (Ncur.right != null){
                    Ncur = Ncur.right;
                }else{
                    return false;
                }
            }
            Ncur = Ncur.left;
        }
        return true;
    }
}
class Node {

    char letter;
    Node left;
    Node right;

    Node (char letter , Node left , Node right) {
        this.letter = letter;
        this.left = left;
        this.right = right;
    }

    Node (char letter , Node left) {
        this.letter = letter;
        this.left = left;
        this.right = null;
    }

    Node (char letter) {
        this.letter = letter;
    }

    Node () {
        this.letter = '.';
    }

    private Node addLetter (char c) {
        if (left == null) {
            left = new Node(c);
            return left;
        }
        else if (left.letter == c) {
            return this.left;
        }
        else {
            Node cur = left;
            if (cur.right == null) {
                cur.right = new Node(c);
                return cur.right;
            }
            while (cur.right.letter != c) {
                cur = cur.right;
                if (cur.right == null) {
                    cur.right = new Node(c);
                    return cur.right;
                }
            }
            return cur.right;
        }
    }

    void addWord(String s) {
        Node N = this;
        char[] word = s.toCharArray();

        for (char c : word) {
            N = N.addLetter(c);
        }
        if(N.left == null){
            N.left = new Node();
        }else if (N.left.letter=='.'){
            return;
        }else {
            Node M = N.left;
            N.left = new Node ('.', null, M);
            return;
        }
    }
}

