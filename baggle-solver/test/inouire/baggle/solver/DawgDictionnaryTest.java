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

import org.junit.AfterClass;
import static org.junit.Assert.*;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author edouard
 */
public class DawgDictionnaryTest {
    
    private static DawgDictionnary test_dict;
    
    public DawgDictionnaryTest() {
    }
    
    @BeforeClass
    public static void setUpClass() throws Exception {
        test_dict = new DawgDictionnary();
        test_dict.createDawg("dawg_dict_fr.dat");
    }
    
    @AfterClass
    public static void tearDownClass() {
    }

    /**
     * Test dictionnary content
     */
    @Test
    public void testDictContent() throws Exception {
        System.out.print("Test content of DAWG french dictionnary...");
        
        //check real words
        String[] real_words={"bonjour","ciel","maitrise","pigeon","zebre"};
        for(String word : real_words){
            if(!test_dict.contains(word)){
                fail(word+" should be in the dictionnary");
            }
        }
        
        //check false words
        String[] false_words={"plop","merco","zef","abraca","bonjo"};
        for(String word : false_words){
            if(test_dict.contains(word)){
                fail(word+" should not be in the dictionnary");
            }
        }
        
        System.out.println(" done");

    }
}
