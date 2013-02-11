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
import java.awt.Color;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import inouire.baggle.client.Language;

/**
 * Panel for time progress display
 * @author edouard
 */
public class TimePanel extends JPanel{

    TimeInterpolator interpolator=null;

    JProgressBar timeProgress = new JProgressBar();
    JLabel timeLabel = new JLabel();
    
    public Icon[] clocks = {new ImageIcon(getClass().getResource("/inouire/baggle/client/icons/clock.png")),
                            new ImageIcon(getClass().getResource("/inouire/baggle/client/icons/clock_nb.png"))};

    private JLabel clock;
            
    public TimePanel(){
        
        timeLabel.setText("0:00");
        timeLabel.setForeground(Color.WHITE);

        timeProgress.setMaximum(200);
        timeProgress.setToolTipText(Language.getString(20));
        timeProgress.setOpaque(false);
        clock=new JLabel();
        clock.setIcon(clocks[0]);

        this.setLayout(new BorderLayout());
        this.add(clock,BorderLayout.WEST);
        this.add(timeProgress,BorderLayout.CENTER);
        this.add(timeLabel,BorderLayout.EAST);
     
        this.setOpaque(false);
    }

    /**
     * Recall the time with the one received from the server
     * @param rem remaining time
     * @param total total time
     */
    public void setServerTime(int rem,int total){
        clock.setIcon(clocks[0]);
        if(interpolator != null){
            interpolator.stopInterpolation();
        }
        interpolator=new TimeInterpolator(this,rem,total);
        interpolator.start();
    }

    /**
     * Set the time and progress
     * @param rem remaining time
     * @param total total time
     */
    void setTime(int rem,int total){
        int percent=200*rem/total;
        timeProgress.setValue(percent);
        int min = rem/60;
        int sec = rem%60;
        String time="";
        if(sec<10){
            time=min+":0"+sec;
        }else{
            time=min+":"+sec;
        }
        timeLabel.setText(time);
    }

    /**
     * Reset display by showing 0:00, stop interpolation and display a nb clock
     */
    public void resetTimer(){
        if(interpolator != null){
            interpolator.stopInterpolation();
        }
        timeLabel.setText("0:00");
        timeProgress.setValue(0);
        clock.setIcon(clocks[1]);
    }

}

/**
 * Thread to interpolate time progress between two information from the server (~every 5 sec)
 * @author edouard
 */
class TimeInterpolator extends Thread{

    private boolean stopInterpolation=false;
    private int rem;
    private int total;
    private TimePanel hourglassPane;

    public TimeInterpolator(TimePanel hourglassPane , int rem,int total){
        this.hourglassPane=hourglassPane;
        this.rem=rem;
        this.total=total;
    }

    @Override
    public void run(){
        hourglassPane.setTime(rem, total);
        while(rem>0){
            try{
                sleep(1000);
            }catch(Exception e){}
            if(stopInterpolation){
                return;
            }else{
                rem-=1;
                hourglassPane.setTime(rem, total);
            }
        }
    }

    /**
     * Stop time interpolation
     */
    public void stopInterpolation(){
        stopInterpolation=true;
    }
}
