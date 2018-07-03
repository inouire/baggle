 /* Copyright 2009-2018 Edouard Garnier de Labareyre
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
    
    private static DawgDictionnary test_dict_4x4;
    private static DawgDictionnary test_dict_5x5;
    
    public DawgDictionnaryTest() {
    }
    
    @BeforeClass
    public static void setUpClass() throws Exception {
        test_dict_4x4 = new DawgDictionnary();
        test_dict_4x4.createDawg("dawg_dict_fr.dat");
        
        test_dict_5x5 = new DawgDictionnary();
        test_dict_5x5.createDawg("dawg_dict_fr_5x5.dat");
    }
    
    @AfterClass
    public static void tearDownClass() {
        test_dict_4x4 = null;
        test_dict_5x5 = null;
    }

    /**
     * Test 4x4 grid dictionnary content
     */
    @Test
    public void testDictContent4x4() throws Exception
    {
        System.out.println("Test content of DAWG french dictionnary for 4x4 grids");
        
        //check real words
        String[] real_words={"do","bonjour","ciel","maitrise","pigeon","zebre"};
        for(String word : real_words){
            if(!test_dict_4x4.contains(word)){
                fail(word+" should be in the dictionnary");
            }
        }
        
        //check false words
        String[] false_words={"plop","ferrari","racro","abraca","bonjo"};
        for(String word : false_words){
            if(test_dict_4x4.contains(word)){
                fail(word+" should not be in the dictionnary");
            }
        }
    }
    
        /**
     * Test 5x5 grid dictionnary content
     */
    @Test
    public void testDictContent5x5() throws Exception
    {
        System.out.println("Test content of DAWG french dictionnary for 5x5 grids");
        
        //check real words
        String[] real_words={"zebre","bonjour","handballeur","maitrise","nomenclature","abasourdissement"};
        for(String word : real_words){
            if(!test_dict_5x5.contains(word)){
                fail(word+" should be in the dictionnary");
            }
        }
        
        //check false words
        String[] false_words={"ete","ciel","racro","abraca","bonjo"};
        for(String word : false_words){
            if(test_dict_5x5.contains(word)){
                fail(word+" should not be in the dictionnary");
            }
        }
    }
}
