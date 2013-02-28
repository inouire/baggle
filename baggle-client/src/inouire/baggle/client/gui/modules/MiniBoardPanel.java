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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import javax.swing.JComponent;
import inouire.baggle.client.gui.ColorFactory;

/**
 * TODO refactor this class...
 * @author edouard
 */
public class MiniBoardPanel extends JComponent{

    Color boardColor = ColorFactory.BROWN_BOARD;
    private String grid="BAGGLEBAGGLEBAGG";

    boolean is_highlighted=false;

    final static private int b=4;//taille du bord
    final static private int i=1;//taille de l'inter-dé

    public int SIZE=4;

    final static int STYLE=Font.BOLD;
    private MiniDice[][] des=new MiniDice[SIZE][SIZE];

    public MiniBoardPanel(){
        for(int k=0;k<SIZE;k++){
            for(int l=0;l<SIZE;l++){
                des[k][l]=new MiniDice(0, 0, 0,"-",0);
            }
        }
    }

    public void setMode(String mode){
        if(mode.equalsIgnoreCase("all")){
            boardColor=ColorFactory.BLUE_BOARD;
        }else if(mode.equalsIgnoreCase("trad")){
            boardColor=ColorFactory.GREEN_BOARD;
        }else{
            boardColor=ColorFactory.BROWN_BOARD;
        }
        repaint();
    }
    
    /**
     * Set grid size and update dices array if needed
     * @param grid 
     */
    public void setGrid(String grid){
        this.grid=grid;
        
        int previous_size=SIZE;
        if(grid.length()>16){
            SIZE=5;
        }else{
            SIZE=4;
        }
        if(SIZE != previous_size){
            des=new MiniDice[SIZE][SIZE];
            for(int k=0;k<SIZE;k++){
                for(int l=0;l<SIZE;l++){
                    des[k][l]=new MiniDice(0, 0, 0,"-",0);
                }
            }
        }
        repaint();
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(120,120);
    }

    @Override
    public void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D)g;
        RenderingHints rh = new RenderingHints(
        RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHints(rh);
        super.paintComponent(g);

        int H=0;
        int w=this.getWidth();
        int h=this.getHeight();

        if(w<h){
            H=w;
        }else{
            H=h;
        }

        int S=H-(2*b);//size of rectangle

        int d=(S-2*b-(SIZE-1)*i)/SIZE;//size of des

        //recalc rect size
        S=(2*b)+(SIZE*d)+((SIZE-1)*i);

        //offset
        int X0=(w-S)/2;
        int Y0=(h-S)/2;

        if(is_highlighted){
            g.setColor(new Color(216,255,172));
            g.fillRect(0, 0, w,h);
        }
         
        g.setColor(boardColor);

        g.fillRect(X0, Y0, S,S);

        int m=10;
        int target=d;
        Font F=new Font("Serial", STYLE, m);
        while(g.getFontMetrics(F).getHeight()<target){
            m+=1;
            F=new Font("Serial", STYLE, m);
        }

        if( grid.length() < 16 ){
            grid = "BAGGLEBAGGLEBAGG";
        }

        for(int k=0;k<SIZE;k++){
            for(int l=0;l<SIZE;l++){
                des[k][l].reAssign(X0+b+k*(d+i),Y0+b+l*(d+i),d, grid.charAt(SIZE*l+k)+"",m);
                des[k][l].paintDe(g);
            }
        }

    }

    public void setHighlight(boolean highlight) {
        is_highlighted=highlight;
        repaint();
    }

}

class MiniDice {

    int size=100;
    int x;
    int y;
    String letter="A";
    int font_size;
    int font_occupation;


    Color C1=new Color(240,240,240);
    Color C2=new Color(230,230,230);

    MiniDice(int x, int y, int size,String letter,int font_size){
        this.size=size;
        this.letter=letter;
        this.x=x;
        this.y=y;
        this.font_size=font_size;
    }

    void reAssign(int x, int y, int size,String letter,int font_size){
        this.size=size;
        this.letter=letter;
        this.x=x;
        this.y=y;
        this.font_size=font_size;
    }

    void paintDe(Graphics g){
        int percent_20=size/5;
        int percent_5=size/20;
        Font F=new Font("Serial", MiniBoardPanel.STYLE, font_size);
        font_occupation=g.getFontMetrics(F).stringWidth(letter);

        g.setColor(C2);
        g.fillRoundRect(x, y, size, size, percent_20, percent_20);

        g.setColor(C1);
        g.fillOval(x+percent_5, y+percent_5, size-2*percent_5, size-2*percent_5);

        g.setColor(Color.DARK_GRAY);
        g.setFont(F);
        g.drawString(letter, x+((size/2)-(font_occupation/2)), y+((size)-percent_20));

    }

}

