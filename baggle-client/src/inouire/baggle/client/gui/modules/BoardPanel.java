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
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.AffineTransform;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.JComponent;
import inouire.baggle.client.Language;
import inouire.baggle.client.Main;
import inouire.baggle.client.gui.ColorFactory;


/**
 *
 * @author Edouard de Labareyre
 */
public class BoardPanel extends JComponent{

    int S=300;//taille d'un coté du plateau
    boolean vertical_resize;
    int X0,Y0;
    int w,h;
    int offset_x_mem, offset_y_mem;
    int b=2;//taille de bordure
    int border_ratio=5;//pourcentage de la taille de la bordure par rapport à la taille totale
    static int i=1;//taille inter-dé
    
    boolean surlign=false;
    int rotation=0;
    int zone=0;
    static int sb=8;
    static int style=Font.BOLD;
    
    int SIZE = 4;
    gDe[][] des=new gDe[SIZE][SIZE];
    
    int select_mode = 0;//0->idle, 1->start,2->incertain,3->click,4->drag
    boolean resizing=false;
    int[] previous_dice={-1,-1};
    
    Color boardColor = ColorFactory.BROWN_BOARD;//0->marron, 1-> bleu , 2-> vert
        
    /*
     * Change the size of the board between big or normal board
     */
    public void setBigBoard(boolean bigBoard){
        if(bigBoard){
            SIZE=5;
        }else{
            SIZE=4;
        }
        des=new gDe[SIZE][SIZE];
        for(int k=0;k<SIZE;k++){
            for(int l=0;l<SIZE;l++){
                des[k][l]=new gDe(0, 0, 0,"-",0);
            }
        }
    }
    
    public BoardPanel(){
        
        this.S=Main.configuration.BOARD_WIDTH;
        setSize(400, 200);
        
        des=new gDe[SIZE][SIZE];
        for(int k=0;k<SIZE;k++){
            for(int l=0;l<SIZE;l++){
                des[k][l]=new gDe(0, 0, 0,"-",0);
            }
        }
        
        this.addMouseListener(new MouseListener() {

            @Override
            public void mouseClicked(MouseEvent arg0) {
                mouseClickedAction(arg0);
            }

            @Override
            public void mousePressed(MouseEvent arg0) {
                mousePressedAction(arg0);
            }

            @Override
            public void mouseReleased(MouseEvent arg0) {
                mouseReleasedAction(arg0);
            }

            @Override
            public void mouseEntered(MouseEvent arg0) {
                //do nothing
            }

            @Override
            public void mouseExited(MouseEvent arg0) {
                zone=0;
                setCursor(Cursor.getDefaultCursor());
            }
        });
        
        this.addMouseMotionListener(new MouseMotionListener() {
            @Override
            public void mouseDragged(MouseEvent arg0) {
                mouseDraggedAction(arg0);
            }

            @Override
            public void mouseMoved(MouseEvent arg0) {
                mouseMovedAction(arg0);
            }
        });
    }

    /**
     * Action when a click is detected on board
     * @param arg0 
     */
    private void mouseClickedAction(MouseEvent arg0){
        if(zone==2){ 
            rotateGrid(false);
        }else if(zone==3){
            rotateGrid(true);
        }
        Main.mainFrame.roomPane.wordEntryPane.giveFocus();
    }
    
    /**
     * Action when mouse is pressed
     * @param arg0 
     */
    private void mousePressedAction(MouseEvent arg0){
        if(zone==1){//resize button
            offset_x_mem=arg0.getX()-X0-S;
            offset_y_mem=arg0.getY()-Y0-S;
            resizing=true;
            int a = Math.abs(arg0.getX()-(w/2));
            if(a<(S/2)-2){
                vertical_resize=true;
            }else{
                vertical_resize=false;
            }
        }else if(zone==4 && Main.connection.in_game && select_mode==0){//on a dice
            this.select_mode=1;//start mode: we don't know which type of selection yet
            
            //get which dice has been pressed          
            int X = arg0.getX();
            int Y = arg0.getY();
            int k=SIZE*(X-(X0+b))/(S-2*b);
            int l=SIZE*(Y-(Y0+b))/(S-2*b);
            if(k>=SIZE || k<0 || l>=SIZE || l<0){
                return;
            }
            
            //Set the dice as selected, and all the dices around as highlighted
            des[k][l].setSelected();
            for(int m=0 ; m<SIZE ; m++){
                for(int n=0;n<SIZE;n++){
                    if(m<=k+1 && m>=k-1 && n<=l+1 && n>=l-1){
                       des[m][n].setHighlighted();
                    }
                }
            }
            previous_dice[0]=k;
            previous_dice[1]=l;
            Main.mainFrame.roomPane.wordEntryPane.addText(""+Main.connection.grid.charAt(SIZE*l+k));
            repaint();
        }
    }
    
    /**
     * Action when mouse boutton is released on the grid
     * @param arg0 
     */
    private void mouseReleasedAction(MouseEvent arg0){
        if(resizing){//if we were in rezise of the grid, stop resizing
            resizing=false;
        }else if(select_mode==1 || select_mode==2){//we were in start mode or uncertain mode, now we know that we should go in click mode
            select_mode=3;//click mode
        }else if(select_mode==3 || select_mode==4){
            select_mode=0;//go back to idle mode
            Main.mainFrame.roomPane.wordEntryPane.sendWord(true);//send word
            resetDicesStatus();
        }
        repaint();
    }
    
    /**
     * Action when mouse is dragged
     * @param arg0 
     */
    private void mouseDraggedAction(MouseEvent arg0){
        if(resizing){//if we are resizing the grid
            if(vertical_resize){
                S=Math.abs(2*(arg0.getY()-(h/2)))-offset_y_mem;
            }else{
                S=Math.abs(2*(arg0.getX()-(w/2)))-offset_x_mem;
            }
            repaint();
        }else if(select_mode==1 || select_mode==2 || select_mode==4){
            //track dice changing
            int X = arg0.getX();
            int Y = arg0.getY();
            int k=SIZE*(X-(X0+b))/(S-2*b);
            int l=SIZE*(Y-(Y0+b))/(S-2*b);
            if(trackDiceChanging(X,Y,k,l)){
                des[k][l].setSelected();
                for(int m=0;m<SIZE;m++){
                    for(int n=0;n<SIZE;n++){
                       des[m][n].setUnhighlighted();
                    }
                }
                for(int m=0;m<SIZE;m++){
                    for(int n=0;n<SIZE;n++){
                        if(m<=k+1 && m>=k-1 && n<=l+1 && n>=l-1){
                           des[m][n].setHighlighted();
                        }
                    }
                }
                previous_dice[0]=k;
                previous_dice[1]=l;
                Main.mainFrame.roomPane.wordEntryPane.addText(""+Main.connection.grid.charAt(SIZE*l+k));
                repaint();
                if(select_mode==1){
                    select_mode=2;
                }else if(select_mode==2){
                    select_mode=4;
                }else if(select_mode==4){
                    //do nothing
                }
            }
        }
    }
    
    /**
     * Action when mouse is moved
     * @param arg0 
     */
    private void mouseMovedAction(MouseEvent arg0){
        //track the zone where the pointer is
        if(!resizing){
            zone = getZone(arg0.getX(),arg0.getY());
            if(zone==4){
                zone=4;//on one of the square
                setToolTipText(null);
            }else if(zone==1){
                setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR) );
                setToolTipText(Language.getString(59));
            }else if(zone==2){
                setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR) );
                setToolTipText(Language.getString(58));//turn grid
            }else if(zone==3){
                setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR) );
                setToolTipText(Language.getString(57));//turn grid
            }else{
                setCursor(Cursor.getDefaultCursor());
                setToolTipText(null);
                repaint();
            }
        }
        if(select_mode==3){//we are in click mode, track a dice changing
            int X = arg0.getX();
            int Y = arg0.getY();
            int k=SIZE*(X-(X0+b))/(S-2*b);
            int l=SIZE*(Y-(Y0+b))/(S-2*b);
            if(trackDiceChanging(X,Y,k,l)){
                des[k][l].setSelected();
                for(int m=0;m<SIZE;m++){
                    for(int n=0;n<SIZE;n++){
                       des[m][n].setUnhighlighted();
                    }
                }
                for(int m=0;m<SIZE;m++){
                    for(int n=0;n<SIZE;n++){
                        if(m<=k+1 && m>=k-1 && n<=l+1 && n>=l-1){
                           des[m][n].setHighlighted();
                        }
                    }
                }
                previous_dice[0]=k;
                previous_dice[1]=l;
                Main.mainFrame.roomPane.wordEntryPane.addText(""+Main.connection.grid.charAt(SIZE*l+k));
                repaint();
            }
        }
    }
        
    private boolean trackDiceChanging(int X,int Y,int k, int l){
        if(previous_dice[0]!=k || previous_dice[1]!=l){
            if(k>=SIZE || k<0 || l>=SIZE || l<0){
                return false;
            }
            if(des[k][l].state!=2){
                return false;
            }
            int divisor=250;
            if(SIZE==5){
                divisor=200;
            }
            //handle the diagonal stuff
            int ax=(1000*(X-(X0+b))/(S-2*b))%divisor;
            int bx=(1000*(Y-(Y0+b))/(S-2*b))%divisor;
            int T=80;
            if((ax+bx<T)||(bx-ax>divisor-T)||(ax+bx>divisor*2-T)||(bx-ax<T-divisor*2)){
                return false;
            }else{
                return true;
            }
        }else{
            return false;
        }
    }
    
    private int getZone(int X, int Y){
        int Z;
        if(inZone(X,Y,X0+b,Y0+b,X0+S-b,Y0+S-b)){
            Z=4;//on one of the square
            setToolTipText(null);
        }else if(inZone(X,Y,X0+S,Y0+S,X0+S+16,Y0+S+16)){
            Z=1;
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR) );
            setToolTipText(Language.getString(59));
        }else if(inZone(X,Y,X0-15,Y0-10,X0+4,Y0+6)){
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR) );
            setToolTipText(Language.getString(58));//turn grid
            Z=2;
        }else if(inZone(X,Y,X0+S-3,Y0-10,X0+S+16,Y0+6)){
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR) );
            setToolTipText(Language.getString(57));//turn grid
            Z=3;
        }else{
            Z=0;
            setCursor(Cursor.getDefaultCursor());
            setToolTipText(null);
            repaint();
        }
        return Z;
    }
    
    public void setBoardColor(Color board_color){
        boardColor=board_color;
        repaint();
    }
        
    /**
     * Makes the grid rotate, clockwise or counter clockwise
     * @param clockwise
     */
    public void rotateGrid(final boolean clockwise){
        Thread s = new Thread() {
            @Override
            public void run() {
                try {
                    if(clockwise){
                        for(int k=0;k<90;k+=10){
                            rotation=k;
                            repaint();
                            sleep(30);
                        }
                    }else{
                        for(int k=0;k>-90;k-=10){
                            rotation=k;
                            repaint();
                            sleep(30);
                        }
                    }
                    rotation = 0;
                    repaint();
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }finally{
                    turnGrid(clockwise);
                    repaint();
                }
            }
        };
        s.start();
    }

    /**
     * Reset the status of all dices (selected, disable...)
     */
    public void resetDicesStatus(){
        for(int k=0;k<SIZE;k++){
            for(int l=0;l<SIZE;l++){
                des[k][l].resetStatus();
            }
        }
        select_mode=0;
        repaint();
    }

    public void disableAll() {
        for(int k=0;k<SIZE;k++){
            for(int l=0;l<SIZE;l++){
                des[k][l].setSelected();
            }
        }
        repaint();
    }

    public void enableAll(){
        resetDicesStatus();
        repaint();
    }

    private boolean inZone(int a,int b,int x,int y,int X,int Y){
        if(a>=(x) && a<=(X) && b>=(y) && b<=(Y)){
            return true;
        }else{
            return false;
        }
    }

    public void turnGrid(boolean clockwise){
        
        int [][] bkp= new int[SIZE][SIZE];
        
        for(int k=0;k<SIZE;k++){
           for(int l=0;l<SIZE;l++){
               bkp[k][l]=this.des[k][l].state;
           }
        }
        
        int next_at_x_is_now_at,next_at_y_is_now_at;
        String new_grid_string="";
            
        for(int y=0;y<SIZE;y++){
            for(int x=0;x<SIZE;x++){
                if(clockwise){
                    next_at_x_is_now_at=y;
                    next_at_y_is_now_at=SIZE-1-x;
                }else{
                   next_at_x_is_now_at=SIZE-1-y;
                   next_at_y_is_now_at=x;
                }
               
                new_grid_string+=Main.connection.grid.charAt( SIZE*next_at_y_is_now_at + next_at_x_is_now_at );
                des[x][y].state=bkp[next_at_x_is_now_at][next_at_y_is_now_at];
            }
        }

        Main.connection.grid = new_grid_string;
        Main.mainFrame.roomPane.refreshCenter();
    }

    public void animateShuffle(){
        Thread s = new Thread() {
            @Override
            public void run() {
                try {
                    for(int k=0;k<180;k+=10){
                        rotation=k;
                        repaint();
                        sleep(20);
                    }
                    rotation = 0;
                    repaint();
                } catch (InterruptedException ex) {
                }finally{
                    repaint();
                }
            }
        };
        s.start();
    }
    
    @Override
    public void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D)g;
        RenderingHints rh = new RenderingHints(
        RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHints(rh);
        super.paintComponent(g);

        int H=0;
        w=this.getWidth();
        h=this.getHeight();

        if(w<h){
            H=w;
        }else{
            H=h;
        }
        if(S>H-(30)){
            S=H-(30);
        }else if(S<=75){
            S=75;
        }

        b=S*border_ratio/100;
        X0=(w-S)/2;
        Y0=(h-S)/2;

        if(rotation!=0){
            AffineTransform saveXform = g2.getTransform();
            AffineTransform AA = new AffineTransform();
            AA.rotate(Math.toRadians(rotation), X0+(S/2), Y0+(S/2));
            g2.transform(AA);
            g2.setColor(boardColor);
            g2.fillRect(X0, Y0, S,S);
            int d=(S-2*b-(SIZE-1)*i)/SIZE;
            for(int k=0;k<SIZE;k++){
                for(int l=0;l<SIZE;l++){
                    if(!resizing){
                        des[k][l].reAssign(X0+b+k*(d+i),Y0+b+l*(d+i),d, "",0);
                        des[k][l].paintDe(g);
                    }
                }
            }
            g2.transform(saveXform);
            return;
        }
        g.setColor(boardColor);
        g.fillRect(X0, Y0, S,S);
        g.setColor(Color.BLACK);
        int d=(S-2*b-(SIZE-1)*i)/SIZE;

        int m=10;
        int target=d;
        Font F=new Font("Serial", style, m);
        while(g.getFontMetrics(F).getHeight()<target){
            m+=1;
            F=new Font("Serial", style, m);
        }

        String grid = Main.connection.grid;

        for(int k=0;k<SIZE;k++){
            for(int l=0;l<SIZE;l++){
                if(!resizing){
                    des[k][l].reAssign(X0+b+k*(d+i),Y0+b+l*(d+i),d, grid.charAt(SIZE*l+k)+"",m);
                    des[k][l].paintDe(g);
                }
            }
        }

        Image image;
        try {
            image = ImageIO.read(getClass().getResource("/inouire/baggle/client/icons/resize.png"));
            g.drawImage(image, X0+S, Y0+S, null);
            image = ImageIO.read(getClass().getResource("/inouire/baggle/client/icons/turn_counterclockwise.png"));
            g.drawImage(image, X0-15, Y0-10, null);
            image = ImageIO.read(getClass().getResource("/inouire/baggle/client/icons/turn_clockwise.png"));
            g.drawImage(image, X0+S-3, Y0-10, null);
        } catch (IOException ex) {

        }
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(400,400);
    }

    public void setBoardSize(int width){
        this.S=width;
    }
    
    public int getBoardSize(){
        return S;
    }
}


class gDe {

    int size=100;
    int x;
    int y;
    String letter="-";
    int font_size;
    int font_occupation;
    int state=0;//0: standard , 1: grisé, 2: mis en valeur

    static Color C1=new Color(240,240,240);
    static Color C2=new Color(230,230,230);

    gDe(int x, int y, int size,String letter,int font_size){
        this.size=size;
        this.letter=letter;
        this.x=x;
        this.y=y;
        this.font_size=font_size;
        this.state=0;
    }

    void reAssign(int x, int y, int size,String letter,int font_size){
        this.size=size;
        this.letter=letter;
        this.x=x;
        this.y=y;
        this.font_size=font_size;
    }

    void resetStatus(){
        state=0;
    }

    void setSelected(){
        state=1;
    }

    void setHighlighted(){
        if(state!=1){
            state=2;
        }
    }

    void setUnhighlighted(){
        if(state==2){
            state=0;
        }
    }

    void paintDe(Graphics g){
        int percent_20=size/5;
        int percent_5=size/20;
        Font F=new Font("Serial", BoardPanel.style, font_size);
        font_occupation=g.getFontMetrics(F).stringWidth(letter);
        switch(state){
            case 0:
                g.setColor(C2);
                g.fillRoundRect(x, y, size, size, percent_20, percent_20);
                g.setColor(C1);
                g.fillOval(x+percent_5, y+percent_5, size-2*percent_5, size-2*percent_5);
                if(Main.connection.in_game){
                   g.setColor(Color.BLACK);
                }else{
                   g.setColor(Color.LIGHT_GRAY);
                }
                g.setFont(F);
                g.drawString(letter, x+((size/2)-(font_occupation/2)), y+((size)-percent_20));
                break;
            case 1:
                g.setColor(C2);
                g.fillRoundRect(x, y, size, size, percent_20, percent_20);
                g.setColor(C1);
                g.fillOval(x+percent_5, y+percent_5, size-2*percent_5, size-2*percent_5);
                g.setColor(Color.LIGHT_GRAY);
                g.setFont(F);
                g.drawString(letter, x+((size/2)-(font_occupation/2)), y+((size)-percent_20));
                break;
            case 2:
                g.setColor(C2);
                g.setColor(new Color(180,200,200));
                g.fillRoundRect(x, y, size, size, percent_20, percent_20);
                g.setColor(C1);
                g.setColor(new Color(180,200,200));
                g.fillOval(x+percent_5, y+percent_5, size-2*percent_5, size-2*percent_5);
                if(Main.connection.in_game){
                   g.setColor(Color.BLACK);
                }else{
                   g.setColor(Color.LIGHT_GRAY);
                }
                g.setFont(F);
                g.drawString(letter, x+((size/2)-(font_occupation/2)), y+((size)-percent_20));
                break;
        }
    }

}
