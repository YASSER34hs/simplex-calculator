/* This is the main class implementing the simplex algorithm. Basically it 
 * implements the two-phase method with Bland's anticycling rule. It utilizes 
 * the Matrix class to maintaining the tableaus.
 */

package mysimplex;

import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;

public class Simplex {
    /* The constructiong method for class Simplex. It requires two parameters: 
     * a matrix and a column name array.
     *
     * The matrix is a tableau generated by the Problem class who parse the 
     * inputs and normalize it. In other words, the matrix provided here is in 
     * standard form, with surplus and slack variables introduced and the 
     * artificial variables and row also introduced. 
     *
     * The column name array is just an array records all name of variables of 
     * columns. It is in following format
     * "b x0 x1 ... u0 ... s0 ... a0 a1 ..."
     * The first column is always b, followed by variables x0, x1 and so on. 
     * There may be surplus variables (u0) and slack variables (s0). Finally 
     * there are aritifical variables a0, a1 and so on.
     */
    public Simplex(Matrix t, ArrayList<String> cn) {
        tableau = t;
        colName = cn;
        // search for the index where artificial vars start
        for(int i=0; i<cn.size(); ++i) {
            String name = cn.get(i);
            if(name.matches("a\\d+")) {
                artificialStart = i;
                break;
            }
        }
        // search for the index where surplus and slack vars start
        for(int i=0; i<cn.size(); ++i) {
            String name = cn.get(i);
            if(!name.matches("(b)|(x\\d+)")) {
                givenVarEnd = i;
                break;
            }
        }
    }
    
    /* The main method for two-phase algorithm for simplex. It is almost in the 
     * same shape to the algorithm given in slides of 
     * The Simplex Algorithm, Part III, p.p. 18
     *
     * All MyUI.detail.*** clauses are for detail outputing.
     */
    public void run() throws Exception {
        // do initial perparing. This is to zero columns of artificial variables.
        initTableau();
        // phase I start
        phase = 0;
        // in phase I we should search all columns of the tableau to pivot
        phase_max_column = tableau.max_col;
        MyUI.detail.phase1Tableaus.add(toDetail());
        runSimplex();
        Fraction r = getResult().getNegative();
        if(r.toDecimal() > 0)
            infeasible = true;
        else {
            // try to drive artificial column still in basis out, which may 
            // remove some rows of the tableau. Instead of inmediately do that, 
            // we only mark the rows in driveArtificialOut(), and later use a 
            // reduce method of matrix to really delete rows.
            if(!driveArtificialOut()) {
                tableau.reduce();
            }
            // phase II start
            phase = 1;
            // in phase II we only need to search the given variable columns to pivot
            phase_max_column = artificialStart;
            MyUI.detail.phase2Tableaus.add(toDetail());
            runSimplex();
        }
        
        if(!infeasible && !unbounded) {
            // not infeasible and not unbounded, so we have a final optimal and a BFS
            finalOptimal = getResult().getNegative();
            calculateBFS();
        }
    }
    
    /* The method for ordinary algorithm of simplex, the part iterating pivoting.
     * Also, it almost follows the algorithm given in slides of 
     * The Simplex Algorithm, Part III, p.p. 5
     *
     * All MyUI.detail.*** clauses are for detail outputing.
     */
    public void runSimplex() throws Exception {
        boolean opt = false;
        // remember that unbounded is a class member
        while(true) {
            int j = chooseColumn(); // try to choose a column to pivot
            if(j != -1) {
                int i = chooseRow(j); // try to choose a row to pivot
                if(i == -1) {
                    // -1 means no cell to be chose to pivot, means unbounded
                    unbounded = true;
                    calculateBFS();
                    break;
                }
                if(phase == 0)
                    MyUI.detail.phase1Pivot.add(String.format("%d,%d", i, j));
                else if(phase == 1)
                    MyUI.detail.phase2Pivot.add(String.format("%d,%d", i, j));
                // pivot the tableau on cell (i,j)
                pivot(i, j);
                if(phase == 0)
                    MyUI.detail.phase1Tableaus.add(toDetail());
                else if(phase == 1)
                    MyUI.detail.phase2Tableaus.add(toDetail());
            } else
                // -1 means no column to choose, means optimal reached
                break;
        }
    }
    
    /* A method to extract the result (z0) for each phase. */
    public Fraction getResult() {
        if(phase == 0)
            return tableau.get(0,0);
        else if(phase == 1)
            return tableau.get(1,0);
        return null;
    }
    
    /* This is to zero columns of artificial variables. */
    private void initTableau() throws Exception {
        for(int i=2,j=artificialStart; j<tableau.max_col; ++j, ++i) {
            tableau.rowTransform(i, new Fraction(-1, 1), 0);
        }
    }
    
    /*
     * try to drive artificial column still in basis out, which may remove some 
     * rows of the tableau. Instead of inmediately do that, we only mark the 
     * rows in driveArtificialOut(), and later use a reduce method of matrix to 
     * really delete rows.
     */
    private boolean driveArtificialOut() throws Exception {
        // set phase = 2 to make fake pivot possible
        phase = 2;
        
        int r = 0;
        for(int j=artificialStart; j<tableau.max_col; ++j) {
            // check weather current column is a identity
            boolean isIdentity = true;
            int findOne = 0;
            for(int i=2; i<tableau.max_row; ++i) {
                Fraction f = tableau.get(i, j);
                if(f.toDecimal() == 0) continue;
                else if(f.toDecimal() == 1.0) {
                    if(findOne>0) {
                        isIdentity = false;
                        break;
                    } else {
                        findOne = i;
                        continue;
                    }
                } else {
                    isIdentity = false;
                    break;
                }
            }
            // try to drive each artificial column in basis out
            if(isIdentity) {
                Fraction f = tableau.get(findOne, 0);
                if(f.toDecimal() == 0) {
                    // now we are sure it is in basis
                    boolean pivotOk = false;
                    for(int k=1; k<artificialStart; ++k) {
                        f = tableau.get(findOne, k);
                        if(f.toDecimal() != 0) {
                            pivot(findOne, k);
                            pivotOk = true;
                            break;
                        }
                    }
                    if(!pivotOk) {
                        // mark in the tableau for later deletion
                        tableau.regRedundantRow(findOne);
                        r += 1;
                    }
                }
            }
        }
        
        // remember to restore the phase variable
        phase = 0;
        
        return r==0; // if r != 0, means that there are rows marked to be lately deleted.
    }
    
    /* Try to choose one column to pivot later. */
    private int chooseColumn() {
        // phase 1 we look the row zero, phase 2 we look the row one, just little trick.
        int i = (phase == 0) ? 0 : 1; 
        int selj = -1; // if -1 is returned, means no column can be selected.
        double min = 0;
        for(int j=1; j<phase_max_column; ++j) {
            Fraction f = tableau.get(i, j);
            if(f.toDecimal() < 0 && min > f.toDecimal()) {
                /* Notice that the part I of Bland's anticycling rule is 
                 * implemented implicitly here. Because we always search from 
                 * left to right in the columns, and only when strict smaller f 
                 * is discovered we change the selection, we can ensure that we 
                 * always choose the lowest numbered column to leave the basis.
                 */
                selj = j;
                min = f.toDecimal();
            }
        }
        return selj;
    }
    
    /* Try to choose one row to pivot later */
    private int chooseRow(int col) throws Exception{
        int seli = -1; // if -1 is returned, means no row can be selected.
        int b = Integer.MAX_VALUE;
        double min = Double.MAX_VALUE;
        for(int i=2; i<tableau.max_row; ++i) {
            Fraction f1 = tableau.get(i, col);
            Fraction f2 = tableau.get(i, 0);
            if(f1.toDecimal() > 0) {
                Fraction f3 = Fraction.div(f2, f1);
                if(f3.toDecimal() <= min) { // There may be a tie                    
                    int b2 = 1;
                    
                    // this loop will find the index of basis to be replaced
                    for(; b2<phase_max_column; ++b2) {
                        if(b2 == col) continue;
                        if(tableau.get(i, b2).toDecimal() == 1.0) {
                            boolean isIdentity = true;
                            for(int j=2; j<tableau.max_row; ++j) {
                                if(j == i) continue;
                                if(tableau.get(j, b2).toDecimal() != 0) {
                                    isIdentity = false;
                                    break;
                                }
                            }
                            if(isIdentity)
                                break;
                        }
                    }
                    
                    if(f3.toDecimal() < min) {
                        // no tie, no worry
                        seli = i;
                        b = b2;
                        min = f3.toDecimal();
                    } else if(b2 < b) {
                        // tie, so only when lower basis column to be replaced we change the selection
                        // This is the second part of Bland's anticycling rule.'
                        seli = i;
                        b = b2;
                        min = f3.toDecimal();
                    }
                }
            }
        }
        return seli;
    }
    
    /* pivot action on tableau. */
    private void pivot(int row, int col) throws Exception {
        Fraction f = tableau.get(row, col);
        tableau.changeRow(row, f.getReverse());
        // we will update row[phase] up to row[max_row]
        // when phase == 0, it means we update all rows.
        // when phase == 1, it means we will not update artificial row 
        //   (remember that we place artificial as row 0).
        // when phase == 2, this is the fake 'pivot' action used in 
        //   driveArtificialOut, so we will not update objection row and 
        //   artificial row.
        for(int i=phase; i<tableau.max_row; ++i) {
            if(i==row) continue;
            Fraction f2 = tableau.get(i, col);
            f2 = f2.getNegative();
            tableau.rowTransform(row, f2, i);
        }
    }
    
    /* Calculating BFS method. 
     * Calculating BFS is easy. 
     * For identity columns, x_k = b_k. For non-identity columns, x_k = 0.
     */
    private void calculateBFS() throws Exception {
        for(int j=1; j<givenVarEnd; ++j) {
            // wether current col is a identity
            boolean isIdentity = true;
            int findOne = 0;
            for(int i=2; i<tableau.max_row; ++i) {
                Fraction f = tableau.get(i, j);
                if(f.toDecimal() == 0) continue;
                else if(f.toDecimal() == 1.0) {
                    if(findOne>0) {
                        isIdentity = false;
                        break;
                    } else {
                        findOne = i;
                        continue;
                    }
                } else {
                    isIdentity = false;
                    break;
                }
            }
            
            if(isIdentity) {
                BFS.add(tableau.get(findOne, 0));
            } else {
                BFS.add(new Fraction(0, 1));
            }
        }
    }

    public String BFSToString(boolean fract) {
        String r = "";
        for(int i=0; i<BFS.size(); ++i) {
            if(fract) r += BFS.get(i).toString() + " ";
            else r+= String.format("%.3f ", BFS.get(i).toDecimal());
        }
        return r;
    }
    
    public ArrayList<String> toDetail() {
        ArrayList<String> r = new ArrayList<String>();
        String s = "<tr>";
        for(int j=0; j<colName.size(); ++j) {
            s += "<td>" + colName.get(j) + "</td>";
        }
        s += "</tr>";
        r.add(s);
        tableau.toDetail(r);
        return r;
    }

    private Matrix tableau;
    public ArrayList<String> colName;
    public boolean infeasible = false; // if this is true, means problem is infeasible
    public boolean unbounded = false; // is this is true, means problem is unbounded
    public Fraction finalOptimal; // final result
    public ArrayList<Fraction> BFS = new ArrayList<Fraction>(); // BFS of final result

    private int givenVarEnd; // the index where surplus and slack vars start
    private int artificialStart; // the index where artificial vars start
    private int phase = 0; // a flag indicating which phase is now. 0 = phase 1, 1 = phase 2, 2 = for special purpose
    private int phase_max_column = 0; // a flag indicating the max column we should search for pivoting in each phase
    
    // Testing funtion for this part
    public static void main(String[] string) throws Exception {
        ArrayList<Fraction> flist = new ArrayList<Fraction>();
        flist.add(new Fraction(0, 1));
        flist.add(new Fraction(0, 1));
        flist.add(new Fraction(0, 1));
        flist.add(new Fraction(1, 1));
        flist.add(new Fraction(1, 1));

        flist.add(new Fraction(0, 1));
        flist.add(new Fraction(1, 1));
        flist.add(new Fraction(1, 1));
        flist.add(new Fraction(0, 1));
        flist.add(new Fraction(0, 1));
        
        flist.add(new Fraction(1, 1));
        flist.add(new Fraction(-1, 2));
        flist.add(new Fraction(1, 1));
        flist.add(new Fraction(1, 1));
        flist.add(new Fraction(0, 1));
        
        flist.add(new Fraction(1, 1));
        flist.add(new Fraction(2, 1));
        flist.add(new Fraction(-1, 1));
        flist.add(new Fraction(0, 1));
        flist.add(new Fraction(1, 1));
        
        Matrix ma = new Matrix(4, 5, flist);
        ArrayList<String> colName = new ArrayList<String>();
        colName.add("b");
        colName.add("x1");
        colName.add("x2");
        colName.add("a1");
        colName.add("a2");
        
        Simplex s = new Simplex(ma, colName);
        System.out.println(s.colName.toString());
        System.out.println(s.tableau.toStringArray());
        System.out.println(s.artificialStart);
        
        s.run();
        System.out.println(String.format("infeasible = %b", s.infeasible));
        System.out.println(String.format("unbounded = %b", s.unbounded));
        System.out.println(String.format("BFS = %s", s.BFS.toString()));
        System.out.println(String.format("Result = %s", s.finalOptimal.toString()));
    }
}
