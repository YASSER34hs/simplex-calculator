/* A class to maintain details through running simplex (all tableaus, pivoting 
 * decisions, etc.) Also, it provides a method to format and output the details 
 * into a html file.
 */

package mysimplex;

import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class Detail {
    public String problem;
    public ArrayList<String> results = new ArrayList<String>();
    public ArrayList<String> initTableau;
    public ArrayList<ArrayList<String> > phase1Tableaus = new ArrayList<ArrayList<String> >();
    public ArrayList<String> phase1Pivot = new ArrayList<String>();
    public ArrayList<ArrayList<String> > phase2Tableaus = new ArrayList<ArrayList<String> >();
    public ArrayList<String> phase2Pivot = new ArrayList<String>();
    
    public void printDetails(PrintStream out) {
        out.println(getHTML());
        
        out.println(getHTML());
        out.println(problem);
        out.println(getHTML());
        
        out.println(getHTML());
        for(int i=0; i<results.size(); ++i)
            out.println(results.get(i));
        out.println(getHTML());
        
        out.println(getHTML());
        for(int i=0; i<initTableau.size(); ++i)
            out.println(initTableau.get(i));
        out.println(getHTML());
        
        out.println(String.format(getHTML(), phase1Tableaus.size() + phase2Tableaus.size()));
        int tabCount = 0;
        String h1 = getHTML();
        String h2 = getHTML();
        String h3 = getHTML();
        for(int i=0; i<phase1Tableaus.size(); ++i) {
            out.print(String.format(h1, tabCount, 1, i, tabCount));
            ArrayList<String> t = phase1Tableaus.get(i);
            for(int j=0; j<t.size(); ++j)
                out.print(t.get(j));
            if(i<phase1Pivot.size()) {
                String[] xy = phase1Pivot.get(i).split(",");
                int x = Integer.valueOf(xy[0]);
                int y = Integer.valueOf(xy[1]);
                out.print(String.format(h2, tabCount, x, tabCount, y));
            }
            out.println(h3);
            tabCount++;
        }
        for(int i=0; i<phase2Tableaus.size(); ++i) {
            out.print(String.format(h1, tabCount, 2, i, tabCount));
            ArrayList<String> t = phase2Tableaus.get(i);
            for(int j=0; j<t.size(); ++j)
                out.print(t.get(j));
            if(i<phase2Pivot.size()) {
                String[] xy = phase2Pivot.get(i).split(",");
                int x = Integer.valueOf(xy[0]);
                int y = Integer.valueOf(xy[1]);
                out.print(String.format(h2, tabCount, x, tabCount, y));
            }
            out.println(h3);
            tabCount++;
        }
        
        out.println(String.format(getHTML(), (new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(new Date()))));
        out.println(getHTML());
    }
    
    private int count = 0;
    private String getHTML() {
        String s = html[count];
        count++;
        return s;
    }
    
    private final static String[] html = {
            "<html><head><title>Simplex Running Details</title>"
           +"<style type=\"text/css\">body {	font-family: helvetica, sans-serif;} div p { font-weight: bold; font-family: helvetica, sans-serif;} table {	border-collapse: collapse;	border: 2px solid #3f7c5f;	font-family: helvetica, sans-serif;	color: #000;	background: #fff;} td {	border: 1px solid #e0e0e0;	padding: 0.5em;} tbody td {	vertical-align: top;	text-align: right;} hr { display:none; } h2 span {  font-style: italic;  font-weight: bold;  font-family: helvetica, sans-serif; } h4 {  display: block;  border-bottom: 2px solid #3f7c5f;  padding: 5px 0px 0px 4px;  text-decoration: none;  color: #666666; } h4 span {  border-top: 2px solid #3f7c5f;  border-left: 2px solid #3f7c5f;  border-right: 2px solid #3f7c5f;  padding: 5px 1em 0px 1em;  font-style:italic;  font-weight: bold;  font-family: helvetica, sans-serif; } pre {  border : 1px solid #e0e0e0;  padding: 1em;  margin-right: 50px;  margin-left: 50px;  font-family: Courier New; } .tableau {  display : none;} </style>"
           +"<script type=\"text/javascript\"> function previous(){ var current = parseInt(document.getElementById(\"current\").value); if(current == 0) return; document.getElementById(\"\"+current).style.display = \"none\"; deActiveMark(current); document.getElementById(\"\"+(current-1)).style.display = \"block\"; activeMark((current-1)); document.getElementById(\"current\").value = current-1; } function next(){ var current = parseInt(document.getElementById(\"current\").value); var max = parseInt(document.getElementById(\"max\").value); if(current+1 >= max) { alert(\"This is the last tableau!\"); return; } document.getElementById(\"\"+current).style.display = \"none\"; deActiveMark(current); document.getElementById(\"\"+(current+1)).style.display = \"block\"; activeMark((current+1)); document.getElementById(\"current\").value = current+1; } function activeMark(tab){ if(!document.getElementById(\"h\"+tab+\"x\")) return; var x = parseInt(document.getElementById(\"h\"+tab+\"x\").value); var y = parseInt(document.getElementById(\"h\"+tab+\"y\").value); var tabobj = document.getElementById(\"t\"+tab); var trow = tabobj.childNodes[0].childNodes[x+1]; var tcol = trow.childNodes[y]; tcol.style.backgroundColor = \"pink\"; } function deActiveMark(tab){ if(!document.getElementById(\"h\"+tab+\"x\")) return; var x = parseInt(document.getElementById(\"h\"+tab+\"x\").value); var y = parseInt(document.getElementById(\"h\"+tab+\"y\").value); var tabobj = document.getElementById(\"t\"+tab); var trow = tabobj.childNodes[0].childNodes[x+1]; var tcol = trow.childNodes[y]; tcol.style.backgroundColor = \"\"; } function onload() { document.getElementById(\"0\").style.display = \"block\"; activeMark(0); } function showall() { var f = parseInt(document.getElementById(\"showall\").value); if(!f) { var max = parseInt(document.getElementById(\"max\").value); var i; for(i=0; i<max; i++) { document.getElementById(\"\"+i).style.display = \"block\"; activeMark(i); } document.getElementById(\"previous_btn\").style.display = \"none\"; document.getElementById(\"next_btn\").style.display = \"none\"; document.getElementById(\"showall_btn\").value = \"Back to Browse Mode\"; document.getElementById(\"showall\").value = \"1\"; } else { var max = parseInt(document.getElementById(\"max\").value); var i; for(i=0; i<max; i++) { document.getElementById(\"\"+i).style.display = \"none\"; deActiveMark(i); } document.getElementById(\"previous_btn\").style.display = \"\"; document.getElementById(\"next_btn\").style.display = \"\"; document.getElementById(\"showall_btn\").value = \"Show All Tableaus\"; document.getElementById(\"showall\").value = \"0\"; onload(); } } window.setTimeout(\"onload()\", 1); </script> "
            ,
            "</head><body><h2><span>Simplex Running Details</span></h2><hr /><h4><span>Problem</span></h4><pre>",
            "</pre><hr />",
            "<h4><span>Results</span></h4><pre>",
            "</pre><hr />",
            "<h4><span>Initial Tableau</span></h4><div><table>",
            "</table></div><hr />",
            "<h4><span>Details</span></h4><input id=\"previous_btn\" onclick = \"previous()\" type=\"button\" id=\"previous\" value=\"&lt;      \" /><input id=\"next_btn\" onclick = \"next()\" type=\"button\" id=\"next\" value=\"      &gt;\" /><input id=\"showall_btn\" onclick = \"showall()\" type=\"button\" id=\"next\" value=\"Show All Tableaus\" /><input id=\"current\" type=\"hidden\" value=\"0\" /><input id=\"max\" type=\"hidden\" value=\"%d\" /><input id=\"showall\" type=\"hidden\" value=\"0\" />",
            "<div id=\"%d\" class=\"tableau\"><p>Phase %d, Tableau #%d</p><table id=\"t%d\">",
            "<input id=\"h%dx\" type=\"hidden\" value=\"%d\"><input id=\"h%dy\" type=\"hidden\" value=\"%d\">",
            "</table></div>",
            "<h4></h4><center>Generated at <i>%s</i><br>by Yet Another Simplex Resolver</center>",
            "</body></html>"
    };
}
