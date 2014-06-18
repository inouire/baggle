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
 * @author Edouard de Labareyre
 */
public class MiniBoardPanel extends JComponent{

    private MiniDice[][] des;
    private Color boardColor;
    private String grid;
    public int size;
    boolean is_highlighted=false;

    final static private int b=4;//taille du bord
    final static private int i=1;//taille de l'inter-dé
    final static int STYLE=Font.BOLD;
    
    public MiniBoardPanel(){
        this.size = 0;
        this.boardColor = ColorFactory.BROWN_BOARD;
        this.grid = "BAGGLEBAGGLEBAGG";
    }
    
    public MiniBoardPanel(int board_size){
        this.size = board_size;
        for(int k=0;k<size;k++){
            for(int l=0;l<size;l++){
                des[k][l]=new MiniDice(0, 0, 0,"-",0);
            }
        }
        this.boardColor = ColorFactory.BROWN_BOARD;
    }
 
    public MiniBoardPanel updateGrid(String grid){
        this.grid=grid;
        int previous_size=size;
        if(grid.length()>16){
            this.size=5;
        }else{
            this.size=4;
        }
        if(size != previous_size){
            des=new MiniDice[size][size];
            for(int k=0;k<size;k++){
                for(int l=0;l<size;l++){
                    des[k][l]=new MiniDice(0, 0, 0,"-",0);
                }
            }
        }
        repaint();
        return this;
    }
        
    public MiniBoardPanel updateMode(String mode){
        this.boardColor=ColorFactory.getBoardColor((size==5), mode);
        repaint();
        return this;
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

        int d=(S-2*b-(size-1)*i)/size;//size of des

        //recalc rect size
        S=(2*b)+(size*d)+((size-1)*i);

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

        for(int k=0;k<size;k++){
            for(int l=0;l<size;l++){
                des[k][l].reAssign(X0+b+k*(d+i),Y0+b+l*(d+i),d, grid.charAt(size*l+k)+"",m);
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

