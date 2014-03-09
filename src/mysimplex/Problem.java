/* The class to represent a linear programmming problem, though actually it only
 * does the parsing work to get some matrix and normalize it.
 */

package mysimplex;

import java.util.ArrayList;

public class Problem {
    private Problem(ArrayList<String> colName, ArrayList<ArrayList<Fraction> > matrix, boolean mmc) {
        //System.out.println(colName);
        //System.out.println(matrix);
        Matrix m = new Matrix(matrix);
        simplex = new Simplex(m, colName);
        minMaxConvert = mmc;
    }
    
    public Simplex simplex;
    private boolean minMaxConvert = false; // if it is true, means we have normalized a maximization problem into a minimization one.

    public static Problem parseProblem(String[] inputs) throws Exception {
        // first line should contain the row count and column count.
        String rowColLine = inputs[0];
        String[] rowCol = rowColLine.split("\\s");
        int row = Integer.valueOf(rowCol[0]);
        int col = Integer.valueOf(rowCol[1]);
        
        ArrayList<String> colName = new ArrayList<String>();
        colName.add("b");
        for(int i=0; i<col; ++i)
            colName.add(String.format("x%d", i));
        col += 1;
        
        // then each line is a constrain of the linear problem. While parse them, we also add surplus or slack vars.
        ArrayList<ArrayList<Fraction> > matrix = new ArrayList<ArrayList<Fraction> >();
        for(int i=1; i<inputs.length-1; ++i) {
            Object[] results = parseAndNormalize(inputs[i], col);
            int newCol = ((Integer)results[0]).intValue();
            if(newCol > col) {
                for(int j=0; j<matrix.size(); ++j) {
                    matrix.get(j).add(new Fraction(0,1));
                }
                String slackVar = (String)results[2];
                colName.add(slackVar);
                col = newCol;
            }
            matrix.add((ArrayList<Fraction>)results[1]);
        }
        
        // finally is the objection line.
        Object[] results = parseAndNormalizeObjection(inputs[inputs.length-1], col);
        ArrayList<Fraction> objectLine = (ArrayList<Fraction>)results[0];
        boolean mmc = ((Boolean)results[1]).booleanValue();
        
        // collected all things, so normalize it (actually only add artificial vars)
        normalizeForTowPhase(matrix, objectLine, colName);
        
        Problem p = new Problem(colName, matrix, mmc);
        return p;
    }

    private static Object[] parseAndNormalize(String s, int col) throws Exception {
        // this will return an array containning
        // [0] : the size of [1]
        // [1] : the arraylist corresponding a line in the tableau
        // [2] : slack variables added when doing normalization
        Object[] results = new Object[3];
        
        String[] fields = s.split("\\s");
        String predict = fields[fields.length-2];
        ArrayList<Fraction> line = new ArrayList<Fraction>();
        String slack = "";

        for(int i=0; i<fields.length-2; ++i)
            line.add(parseFraction(fields[i]));
        line.add(0, parseFraction(fields[fields.length-1]));
        
        if(line.size() < col)
            for(int i=line.size(); i<col; ++i)
                line.add(new Fraction(0, 1));
        
        if(predict.equals("<=")) {
            line.add(new Fraction(1, 1));
            slack = String.format("s%d", col+1);
        } else if(predict.equals(">=")) {
            line.add(new Fraction(-1, 1));
            slack = String.format("u%d", col+1);
        }

        results[0] = new Integer(line.size());
        results[1] = line;
        results[2] = slack;
        
        return results;
    }

    private static Object[] parseAndNormalizeObjection(String string, int col) throws Exception {
        // this will return an array containing
        // [0] : the arraylist corresponding a line in the tableau
        // [1] : a boolean flag indicate need convert finalOptimal?
        
        Object[] results = new Object[2];
        String[] fields = string.split("\\s");
        Boolean minMaxConvert = false;
        
        Fraction f = null;
        if(fields[0].equals("minimize")) {
            f = new Fraction(1, 1);
        } else if(fields[0].equals("maximize")) {
            f = new Fraction(-1, 1);
            minMaxConvert = true;
        }

        ArrayList<Fraction> line = new ArrayList<Fraction>();
        for(int i=1; i<fields.length; ++i)
            line.add(Fraction.mul(f, parseFraction(fields[i])));
        line.add(0, new Fraction(0, 1));
        int n = col-line.size();
        for(int i=0; i<n; ++i)
            line.add(new Fraction(0, 1));

        results[0] = line;
        results[1] = minMaxConvert;
        return results;
    }

    private static void normalizeForTowPhase(ArrayList<ArrayList<Fraction> > matrix, ArrayList objectLine, ArrayList colName) throws Exception {
        // the only thing did in this part is adding artificial variables (so extend each row).
        for(int i=0; i<matrix.size(); ++i) {
            ArrayList<Fraction> row = matrix.get(i);
            if(row.get(0).toDecimal() < 0) {
                for(int j=0; j<row.size(); ++j)
                    row.get(j).negate();
            }
        }

        ArrayList<Fraction> artificialLine = new ArrayList<Fraction>();
        for(int i=0; i<objectLine.size(); ++i)
            artificialLine.add(new Fraction(0, 1));

        int m = matrix.size();
        for(int i=0; i<m; ++i) {
            artificialLine.add(new Fraction(1, 1));
            objectLine.add(new Fraction(0, 1));
            colName.add(String.format("a%d", i));
            for(int j=0; j<m; ++j) {
                ArrayList<Fraction> row = matrix.get(j);
                row.add(i==j ? new Fraction(1, 1) : new Fraction(0, 1));
            }
        }
            
        matrix.add(0, objectLine);
        matrix.add(0, artificialLine);
    }

    private static Fraction parseFraction(String string) throws Exception {
        if(string.matches("-?\\d+/\\d+")) {
            String[] strs = string.split("/");
            int d = Integer.valueOf(strs[0]);
            int n = Integer.valueOf(strs[1]);
            return new Fraction(d, n);
        } else if(string.matches("-?\\d+")) {
            int i = Integer.valueOf(string);
            return Fraction.parse(i);
        } else if(string.matches("-?\\d+\\.\\d+")) {
            return Fraction.parse(string);
        }
        return null;
    }
    
    public String finalOptimalToString(boolean fract) {
        try {
            if(fract) {
                if(minMaxConvert)
                    return simplex.finalOptimal.getNegative().toString();
                else return simplex.finalOptimal.toString();
            } else {
                if(minMaxConvert)
                    return String.format("%.3f ", simplex.finalOptimal.getNegative().toDecimal());
                else return String.format("%.3f ", simplex.finalOptimal.toDecimal());
            }
        } catch(Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // Testing funtion for this part
    public static void main(String[] args) throws Exception {
        String[] inputs = {
            "3 2",
            "5.0 1.0 >= 5.0",
            "1.0 1.0 <= -3.0",
            "1.0 5.0 >= 5.0",
            "maximize 1.0 2.0"
        };
        
        Problem p = parseProblem(inputs);
    }
}
