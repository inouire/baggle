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
import inouire.basics.SimpleLog;
import inouire.basics.Value;
import java.awt.Polygon;

/**
 *
 * @author Edouard de Labareyre
 */
public class BoardPanel extends JComponent{

    
    //internal constants, rebuilt only on board resize
    int board_size;
    int component_width;
    int component_height;
    int dices_size=0;
    int dice_width;
    int x0,xf,y0,yf;
    int X0,Y0;
    int board_margin;
    int radius_pow2;
    
    boolean dimensions_init=false;

    private final static int border_ratio=5;//pourcentage de la taille de la bordure par rapport à la taille totale
    private final static int i=1;//taille inter-dé
    
    boolean surlign=false;
    int rotation=0;
    int zone=0;
    static int sb=8;
    static int style=Font.BOLD;
    
    int SIZE = 4;
    gDe[][] des=new gDe[SIZE][SIZE];
    
    int select_mode = 0;//0->idle, 1->start,2->incertain,3->click,4->drag
    boolean resizing=false;
    boolean vertical_resize;
    int[] previous_dice={-1,-1};
    int offset_x_mem, offset_y_mem;
    
    Color boardColor = ColorFactory.BROWN_BOARD;//0->marron, 1-> bleu , 2-> vert
    
    private Image[] icons = new Image[3];

    public BoardPanel(){
        this.board_size = Main.configuration.BOARD_WIDTH;
        setSize(400, 200);
        
        try{
            icons[0] = ImageIO.read(getClass().getResource("/inouire/baggle/client/icons/resize.png"));
            icons[1] = ImageIO.read(getClass().getResource("/inouire/baggle/client/icons/turn_counterclockwise.png"));
            icons[2] = ImageIO.read(getClass().getResource("/inouire/baggle/client/icons/turn_clockwise.png"));
        }catch(IOException ioe){
            SimpleLog.logger.warn("Impossible to load rotate and resize icons on grid");
        }
        
        des=new gDe[SIZE][SIZE];
        for(int k=0;k<SIZE;k++){
            for(int l=0;l<SIZE;l++){
                des[k][l]=new gDe(0, 0,"-",0);
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
                
    public void updateConstants(int new_board_size){
        updateConstants(new_board_size, this.getWidth(), this.getHeight());
    }
    
    public void updateConstants(int new_component_width, int new_component_height){
        updateConstants(this.board_size, new_component_width, new_component_height);
    }
    
    public void updateConstants(int new_board_size,int new_component_width, int new_component_height){
        
        int component_size = Math.min(new_component_width, new_component_height);
        board_size = Value.bound(new_board_size, 75, component_size - 30);
        component_width = new_component_width;
        component_height = new_component_height;
        
        X0 = (this.component_width  - board_size)/2;
        Y0 = (this.component_height - board_size)/2;
        
        board_margin = board_size * border_ratio / 100;
        
        x0 = X0 + board_margin;
        y0 = Y0 + board_margin;
        xf = X0 + board_size - board_margin;
        yf = Y0 + board_size - board_margin;
        
        dices_size = xf - x0;
        dice_width = (dices_size / SIZE)-2;
        
        int radius_ratio=75;
        radius_pow2 = (int) Math.pow(radius_ratio * dice_width / 200,2);
        
        gDe.updateConstants(dice_width);
    }
    
    private int[] discretizePoint(int x, int y){
        //check that we are in the grid zone
        if( x>xf || x < x0 || y > yf || y < y0){
            return null;
        }
        //get grid coordinate
        int ix = SIZE * (x - x0)/dices_size;
        int jy = SIZE * (y - y0)/dices_size;

        return new int[]{ix,jy};
    }
    
    private int[] roundDiscretizePoint(int x, int y){
        
        // get discrete reference
        int[] ref = discretizePoint(x, y);
        
        if(ref!=null){
            // compute corresponding coordinates
            int xref = x0 + ref[0] * dice_width + dice_width/2;
            int yref = y0 + ref[1] * dice_width + dice_width/2;

            // compute distante^2 between ref and actual point
            int distance_2 = (int)(Math.pow(x-xref,2) + Math.pow(y-yref,2));

            // check that distance is lower than radius
            if(distance_2 < radius_pow2){
                return ref;
            }
        }
        return null;
    }
        
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
                des[k][l]=new gDe(0, 0,"-",0);
            }
        }
        updateConstants(board_size);
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
        
        int x = arg0.getX();
        int y = arg0.getY();
        
        if(zone==1){//resize button
            offset_x_mem = x - X0-board_size;
            offset_y_mem = y - Y0-board_size;
            resizing=true;
            int a = Math.abs(x-(component_width/2));
            if(a<(board_size/2)-2){
                vertical_resize=true;
            }else{
                vertical_resize=false;
            }
        }else if(zone==4 && Main.connection.in_game && select_mode==0){//on a dice
            this.select_mode=1;//start mode: we don't know which type of selection yet
            
            int[] coord = discretizePoint(x, y);
            if(coord == null){
                return;
            }
            int k = coord[0];
            int l = coord[1];
            
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
        int x = arg0.getX();
        int y = arg0.getY();
        if(resizing){//if we are resizing the grid
            int new_board_size;
            if(vertical_resize){
                new_board_size=Math.abs(2*(y-(component_height/2)))-offset_y_mem;
            }else{
                new_board_size=Math.abs(2*(x-(component_width/2)))-offset_x_mem;
            }
            updateConstants(new_board_size);
            repaint();
        }else if(select_mode==1 || select_mode==2 || select_mode==4){

            int[] coord = roundDiscretizePoint(x, y);
            if(coord == null){
                return;
            }
            int k = coord[0];
            int l = coord[1];

            if(trackDiceChanging(k,l)){
                des[k][l].setSelected();
                des[k][l].setConnector(gDe.computeConnector(previous_dice[0],previous_dice[1],k,l));
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

        int x = arg0.getX();
        int y = arg0.getY();
        
        //track the zone where the pointer is
        if(!resizing){
            zone = getZone(x,y);
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
            
            int[] coord = roundDiscretizePoint(x, y);
            if(coord == null){
                return;
            }
            int k = coord[0];
            int l = coord[1];

            if(trackDiceChanging(k,l)){
                des[k][l].setSelected();
                des[k][l].setConnector(gDe.computeConnector(previous_dice[0],previous_dice[1],k,l));
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
        
    private boolean trackDiceChanging(int k, int l){
        if(previous_dice[0]!=k || previous_dice[1]!=l){
            if(k>=SIZE || k<0 || l>=SIZE || l<0){
                return false;
            }
            if(des[k][l].state!=2){
                return false;
            }
            return true;
        }else{
            return false;
        }
    }
    
    private int getZone(int x, int y){
        int Z;
        if(inZone(x, y, x0, y0, xf, yf)){
            Z=4;//on one of the square
            setToolTipText(null);
        }else if(inZone(x,y,
                        X0+board_size, Y0+board_size,
                        X0+board_size+16, Y0+board_size+16)){
            Z=1;
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR) );
            setToolTipText(Language.getString(59));
        }else if(inZone(x,y,
                        X0-15, Y0-10,
                        X0+4, Y0+6)){
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR) );
            setToolTipText(Language.getString(58));//turn grid
            Z=2;
        }else if(inZone(x, y,
                        X0+board_size-3, Y0-10,
                        X0+board_size+16, Y0+6)){
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
                    SimpleLog.logger.warn("Error while rotating grid");
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
                des[k][l].setDisabled();
            }
        }
        repaint();
    }

    public void enableAll(){
        resetDicesStatus();
        repaint();
    }

    private boolean inZone(int x,int y,int A,int B,int C,int D){
        if( x>= A && x <= C && y >= B && y<= D){
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
        
        int actual_width  = this.getWidth();
        int actual_height = this.getHeight();
        
        // check first init
        if(!dimensions_init){
            updateConstants(board_size, actual_width, actual_height);
            dimensions_init = true;
        }

        // check if component size has changed
        if( actual_width != component_width || actual_height != component_height){
            updateConstants(actual_width, actual_height);
        }
                
        // init graphical component
        Graphics2D g2 = (Graphics2D)g;
        RenderingHints rh = new RenderingHints(
        RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHints(rh);
        super.paintComponent(g);

        // handle rotation cases
        if(rotation!=0){
            AffineTransform saveXform = g2.getTransform();
            AffineTransform AA = new AffineTransform();
            AA.rotate(Math.toRadians(rotation), X0+(board_size/2), Y0+(board_size/2));
            g2.transform(AA);
            g2.setColor(boardColor);
            g2.fillRect(X0, Y0, board_size,board_size);
            int d=(board_size-2*board_margin-(SIZE-1)*i)/SIZE;
            for(int k=0;k<SIZE;k++){
                for(int l=0;l<SIZE;l++){
                    if(!resizing){
                        des[k][l].reAssign(X0+board_margin+k*(d+i),Y0+board_margin+l*(d+i),"",0);
                        des[k][l].paintDe(g);
                    }
                }
            }
            g2.transform(saveXform);
            return;
        }
        
        // paint board
        g.setColor(boardColor);
        g.fillRect(X0, Y0, board_size,board_size);
        
        //compute font size
        // TODO do it in update constants
        int m=10;
        int d=(board_size-2*board_margin-(SIZE-1)*i)/SIZE;
        int target=d;
        Font F=new Font("Serial", style, m);
        while(g.getFontMetrics(F).getHeight()<target){
            m+=1;
            F=new Font("Serial", style, m);
        }

        // draw dices
        String grid = Main.connection.grid;
        for(int k=0;k<SIZE;k++){
            for(int l=0;l<SIZE;l++){
                if(!resizing){
                    des[k][l].reAssign(X0+board_margin+k*(d+i),Y0+board_margin+l*(d+i),grid.charAt(SIZE*l+k)+"",m);
                    des[k][l].paintDe(g);
                }
            }
        }
        for(int k=0;k<SIZE;k++){
            for(int l=0;l<SIZE;l++){
                if(!resizing){
                    des[k][l].paintConnector(g);
                }
            }
        }

        //draw icons
        g.drawImage(icons[0], X0+board_size, Y0+board_size, null);
        g.drawImage(icons[1], X0-15, Y0-10, null);
        g.drawImage(icons[2], X0+board_size-3, Y0-10, null);

    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(400,400);
    }
    
    public int getBoardSize(){
        return board_size;
    }
}


class gDe {

    static int size=100;
    int x;
    int y;
    String letter="-";
    int font_size;
    int font_occupation;
    int state=0;//0: standard , 1: sékectionné, 2: mis en valeur, 3: disbabled

    int connector;
    
    static Color C1=new Color(240,240,240);
    static Color C2=new Color(230,230,230);

    static int margin,bold,inter,diameter,radius,straight,diagonal,cosradius,cosbold,cosdiagonal;
        
    static void updateConstants(int new_size){
        size=new_size;
        margin=size/20;
        bold = 5;
        inter = 1;
        
        diameter = size-(2*margin);
        radius = diameter/2;
        straight = 2 * margin +inter;
        diagonal = (int)(Math.sqrt(2 * Math.pow(size+1, 2))) - 2*radius + 2;
        
        cosradius = (int) (radius * Math.cos(Math.PI/4));
        cosbold = (int) (bold * Math.cos(Math.PI/4));
        cosdiagonal = (int)(diagonal * Math.cos(Math.PI/4));
    }
    
    gDe(int x, int y, String letter,int font_size){
        this.connector = 0;
        this.letter=letter;
        this.x=x;
        this.y=y;
        this.font_size=font_size;
        this.state=0;
    }

    void reAssign(int x, int y, String letter,int font_size){
        this.letter=letter;
        this.x=x;
        this.y=y;
        this.font_size=font_size;
    }

    void resetStatus(){
        state=0;
        connector=0;
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
    
    void setDisabled(){
        state=3;
    }
    
    void paintDe(Graphics g){
        int percent_20=size/5;
        int percent_5=size/20;
        Font F=new Font("Serial", BoardPanel.style, font_size);
        font_occupation=g.getFontMetrics(F).stringWidth(letter);
        switch(state){
            case 0:
            case 2:
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
                g.setColor(new Color(180,200,200));
                g.fillOval(x+percent_5, y+percent_5, size-2*percent_5, size-2*percent_5);
                g.setColor(Color.BLACK);
                g.drawOval(x+percent_5, y+percent_5, size-2*percent_5, size-2*percent_5);
                g.setColor(Color.BLACK);
                g.setFont(F);
                g.drawString(letter, x+((size/2)-(font_occupation/2)), y+((size)-percent_20));
                break;
            case 3:
                g.setColor(C2);
                g.fillRoundRect(x, y, size, size, percent_20, percent_20);
                g.setColor(C1);
                g.fillOval(x+percent_5, y+percent_5, size-2*percent_5, size-2*percent_5);
                g.setColor(new Color(189,163,170));
                g.fillOval(x+percent_5, y+percent_5, size-2*percent_5, size-2*percent_5);
                g.setColor(Color.LIGHT_GRAY);
                g.setFont(F);
                g.drawString(letter, x+((size/2)-(font_occupation/2)), y+((size)-percent_20));
                break;
        }
        
        
    }

    void setConnector(int connector){
        this.connector=connector;
    }
    
    static int computeConnector(int i, int j, int k, int l){
        return 3*(i-k)+(j-l);
    }
    
    void paintConnector(Graphics g){
        int xc = x + (size/2);
        int yc = y + (size/2);

        g.setColor(new Color(255,106,36));
        
        //draw connector related to this dice
        Polygon p = new Polygon();
       /*                      
        *   -4  -1   2           
        *                      
        *   -3   0   3            
        *                        
        *   -2   1   4           
        */                           
        switch(connector){
            case -4:
                p.addPoint(xc-cosradius+cosbold, yc-cosradius-cosbold);
                p.addPoint(xc-cosradius-cosbold, yc-cosradius+cosbold);
                p.addPoint(xc-cosradius-cosdiagonal-cosbold, yc-cosradius-cosdiagonal+cosbold);
                p.addPoint(xc-cosradius-cosdiagonal+cosbold, yc-cosradius-cosdiagonal-cosbold);
                break;
            case -3:
                p.addPoint(xc-radius, yc+bold);
                p.addPoint(xc-radius, yc-bold);
                p.addPoint(xc-radius-straight, yc-bold);
                p.addPoint(xc-radius-straight, yc+bold);
                break;
            case -2:
                p.addPoint(xc-cosradius-cosbold, yc+cosradius-cosbold);
                p.addPoint(xc-cosradius+cosbold, yc+cosradius+cosbold);
                p.addPoint(xc-cosradius-cosdiagonal+cosbold, yc+cosradius+cosdiagonal+cosbold);
                p.addPoint(xc-cosradius-cosdiagonal-cosbold, yc+cosradius+cosdiagonal-cosbold);
                break;
            case -1: 
                p.addPoint(xc-bold, yc-radius);
                p.addPoint(xc+bold, yc-radius);
                p.addPoint(xc+bold, yc-radius-straight);
                p.addPoint(xc-bold, yc-radius-straight);
                break;
            case 0:
                //draw nothing
                break;
            case 1:
                p.addPoint(xc-bold, yc+radius+1);
                p.addPoint(xc+bold, yc+radius+1);
                p.addPoint(xc+bold, yc+radius+straight+1);
                p.addPoint(xc-bold, yc+radius+straight+1);
                break;
            case 2:
                p.addPoint(xc+cosradius+cosbold+1, yc-cosradius+cosbold+1);
                p.addPoint(xc+cosradius-cosbold+1, yc-cosradius-cosbold+1);
                p.addPoint(xc+cosradius+cosdiagonal-cosbold, yc-cosradius-cosdiagonal-cosbold);
                p.addPoint(xc+cosradius+cosdiagonal+cosbold, yc-cosradius-cosdiagonal+cosbold);
                break;
            case 3:
                p.addPoint(xc+radius+1, yc+bold);
                p.addPoint(xc+radius+1, yc-bold);
                p.addPoint(xc+radius+straight+1, yc-bold);
                p.addPoint(xc+radius+straight+1, yc+bold);
                break;
            case 4:
                p.addPoint(xc+cosradius+cosbold+1, yc+cosradius-cosbold+1);
                p.addPoint(xc+cosradius-cosbold+1, yc+cosradius+cosbold+1);
                p.addPoint(xc+cosradius+cosdiagonal-cosbold, yc+cosradius+cosdiagonal+cosbold);
                p.addPoint(xc+cosradius+cosdiagonal+cosbold, yc+cosradius+cosdiagonal-cosbold);
                break;

        }
        if(p.npoints>=4){
            g.fillPolygon(p);
        }
    
    }

}
