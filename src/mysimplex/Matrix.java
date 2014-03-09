/* An class implements a matrix to be tableau. 
 * Also, some elementary linear transformation on it is impelemented.
 */

package mysimplex;

import java.util.ArrayList;

public class Matrix {
    public Matrix(int row, int col, ArrayList<Fraction> flist) {
        m = new ArrayList<ArrayList<Fraction> >();
        max_row = row;
        max_col = col;
        for(int i=0; i<row; ++i) {
            ArrayList arow = new ArrayList<Fraction>();
            for(int j=0; j<col; ++j) {
                arow.add(flist.get(i*col+j));
            }
            m.add(arow);
        }
    }
    
    public Matrix(ArrayList<ArrayList<Fraction> > a) {
        if(a.size() == 0) {
            max_row = 0;
            max_col = 0;
        } else {
            max_row = a.size();
            max_col = a.get(0).size();
            m = a;
        }
    }
    
    public Fraction get(int row, int col) {
        return (m.get(row)).get(col);
    }
    
    public ArrayList<ArrayList<String> > toStringArray() {
        ArrayList<ArrayList<String> > sa = new ArrayList<ArrayList<String> >();
        for(int i=0; i<max_row; ++i) {
            ArrayList row = new ArrayList<String>();
            for(int j=0; j<max_col; ++j) {
                row.add(get(i, j).toString());
            }
            sa.add(row);
        }
        return sa;
    }
    
    public String toString() {
        return toStringArray().toString();
    }
    
    /* Elementary linear transformation: change a row */
    public void changeRow(int srow, Fraction par) throws Exception {
        ArrayList<Fraction> row = m.get(srow);
        for(int i=0; i<max_col; ++i) {
            Fraction f = row.get(i);
            f.update(Fraction.mul(f, par));
        }
    }
    
    /* Elementary linear transformation: add one row timing a constant to another row */
    public void rowTransform(int srow, Fraction par, int trow) throws Exception {
        ArrayList<Fraction> row1 = m.get(srow);
        ArrayList<Fraction> row2 = m.get(trow);
        for(int i=0; i<max_col; ++i) {
            Fraction f1 = row1.get(i);
            Fraction f2 = row2.get(i);
            f2.update(Fraction.add(f2, Fraction.mul(f1, par)));
        }
    }
    
    public void deleteRow(int row) {
        if(row >= max_row)
            return;
        m.remove(row);
        max_row -= 1;
    }
    
    public void regRedundantRow(int index) {
        redundantRows.add(m.get(index));
    }
    
    public void reduce() {
        for(int i=0; i<redundantRows.size(); ++i) {
            m.remove(redundantRows.get(i));
        }
        redundantRows.clear();
        max_row = m.size();
        max_col = (max_row == 0) ? 0 : m.get(0).size();
    }
    
    public void toDetail(ArrayList<String> r) {
        for(int i=0; i<m.size(); ++i) {
            ArrayList<Fraction> row = m.get(i);
            String s = "<tr>";
            for(int j=0; j<row.size(); ++j) {
                Fraction f = row.get(j);
                s += "<td>" + f.toString() + "</td>";
            }
            s += "</tr>";
            r.add(s);
        }
    }

    private ArrayList<ArrayList<Fraction> > m;
    public int max_row;
    public int max_col;
    private ArrayList<ArrayList<Fraction> > redundantRows = new ArrayList<ArrayList<Fraction> >();
    
    // Testing funtion for this part
    public static void main(String[] args) throws Exception {
        ArrayList<Fraction> flist = new ArrayList<Fraction>();
        flist.add(new Fraction(1, 1));
        flist.add(new Fraction(2, 1));
        flist.add(new Fraction(3, 1));
        flist.add(new Fraction(4, 1));
        flist.add(new Fraction(5, 1));
        flist.add(new Fraction(7, 1));
        
        Matrix ma = new Matrix(2, 3, flist);
        System.out.println(ma.toStringArray().toString());
        ma.changeRow(0, new Fraction(1, 2));
        System.out.println(ma.toStringArray().toString());
        ma.rowTransform(0, new Fraction(-5, 1), 1);
        System.out.println(ma.toStringArray().toString());
        ma.deleteRow(0);
        System.out.println(ma.toStringArray().toString());
        ma.regRedundantRow(0);
        ma.reduce();
        System.out.println(ma.toStringArray().toString());
    }
}
