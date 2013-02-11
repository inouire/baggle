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

package inouire.baggle.server.bean;

/**
 *
 * @author edouard
 */
public class OneScore {
    
    private String name;
    private int score;
    private int won=0;
    private int beat=0;
    private int sixp=0;
    
    public OneScore(String name,int score){    
        this.name=name;
        this.score=score;
    }

    public int getBeat() {
        return beat;
    }
    public void setBeat(int beat) {
        this.beat = beat;
    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public int getScore() {
        return score;
    }
    public void setScore(int score) {
        this.score = score;
    }

    public int getSixp() {
        return sixp;
    }
    public void setSixp(int sixp) {
        this.sixp = sixp;
    }

    public int getWon() {
        return won;
    }
    public void setWon(int won) {
        this.won = won;
    }

    
    public String toURL(){
        String url="&name="+name.replaceAll(" ", "%20");
        url+="&score="+score;
        url+="&won="+won;
        url+="&beat="+beat;
        url+="&sixp="+sixp;
        return url;
    }
    
}
