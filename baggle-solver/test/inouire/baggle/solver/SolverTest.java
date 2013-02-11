/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package inouire.baggle.solver;

import java.util.ArrayList;
import org.junit.AfterClass;
import static org.junit.Assert.*;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author edouard
 */
public class SolverTest {
    
    private static ArrayList<String> test_results;
    
    public SolverTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
        Solver test_solver = new Solver("fr",false);
        test_results = test_solver.solveGrid("HIODLCIOTTERLNON", 3);
    }
    
    @AfterClass
    public static void tearDownClass() {
        test_results = null;
    }

    /**
     * Test the number of points that the grid worth 
     */
    @Test
    public void testGridNbPoints() {
        System.out.println("Test grid number of points");
        assertEquals(Solver.getNbPoints(test_results),237);
    }
    
     /**
     * Test the words foudn by the solver
     */
    @Test
    public void testWordsFound(){
        System.out.println("Test words found by solver");
        
        String[] are_solutions = {"LICITE","TENOR","OIE","DORIEN","ROTIE"};
        String[] are_not_solutions = {"A","TES","SET"};
        
        for(String s : are_solutions){
            if(!test_results.contains(s)){
                fail("Results should contain "+s);
            }
        }
        
        for(String ns : are_not_solutions){
            if(test_results.contains(ns)){
                fail("Results should not contain "+ns);
            }
        }
        
        
    }

}
