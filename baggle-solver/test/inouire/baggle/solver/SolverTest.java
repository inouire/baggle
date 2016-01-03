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
    
    private static ArrayList<String> test_results_4x4;
    private static ArrayList<String> test_results_5x5;
    
    public SolverTest() {
    }
    
    @BeforeClass
    public static void setUpClass() throws Exception
    {
        Solver test_solver_4x4 = new Solver("fr",false,false);
        test_solver_4x4.setMinLength(3);
        test_results_4x4 = test_solver_4x4.solveGrid("HIODLCIOTTERLNON");
        
        Solver test_solver_5x5 = new Solver("fr",false,true);
        test_solver_5x5.setMinLength(3);
        test_results_5x5 = test_solver_5x5.solveGrid("ABASOSIDRUSEMENEDCITERTUO");
    }
    
    @AfterClass
    public static void tearDownClass()
    {
        test_results_4x4 = null;
    }

    /**
     * Test the number of points that the grid worth
     */
    @Test
    public void testGridNbPoints()
    {
        System.out.println("Test grid number of points");
        assertEquals(351,Solver.getNbPoints(test_results_4x4, false));
        assertEquals(408,Solver.getNbPoints(test_results_4x4, true));
    }
    
     /**
     * Test the words found by the solver on a 4x4 grid
     */
    @Test
    public void testWordsFound4x4()
    {
        System.out.println("Test words found by solver on 4x4 grid");
        
        String[] are_solutions = {"LICITE","TENOR","OIE","DORIEN","ROTIE"};
        String[] are_not_solutions = {"A","TES","SET"};
        
        for(String s : are_solutions){
            if(!test_results_4x4.contains(s)){
                fail("Results should contain "+s);
            }
        }
        
        for(String ns : are_not_solutions){
            if(test_results_4x4.contains(ns)){
                fail("Results should not contain "+ns);
            }
        }
    }
    
    /**
     * Test the words found by the solver on a 5x5 grid
     */
    @Test
    public void testWordsFound5x5()
    {
        System.out.println("Test words found by solver on 5x5 grid");
        
        String[] are_solutions = {"TERMINUS","RADIS","ABASOURDISSEMENT","ADRET","DECIMER"};
        String[] are_not_solutions = {"BONJOUR","MORDRE","SET"};
        
        for(String s : are_solutions){
            if(!test_results_5x5.contains(s)){
                fail("Results should contain "+s);
            }
        }
        
        for(String ns : are_not_solutions){
            if(test_results_5x5.contains(ns)){
                fail("Results should not contain "+ns);
            }
        }
    }

}
