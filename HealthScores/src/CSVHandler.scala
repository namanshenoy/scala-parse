import scala.io.BufferedSource
import scala.io.Source
import javax.swing.filechooser.FileNameExtensionFilter
import javax.swing.JFileChooser
import javax.swing.filechooser.FileFilter
import java.io.File

object CSVHandler {

  class CSV(val fileName: String) {

    var source: BufferedSource = Source.fromFile(fileName); //the file buffer

    var lines: Iterator[String] = source.getLines(); //an iterator of all the lines in the file
    //Calling length() iterates through the whole thing (so don't call it)

    private var line: Array[String] = lines.next().split(","); //The current line

    var fields: Array[Field] = { //The fields in this csv
      var count = 0;
      var headers = Set[Field]();
      for (title: String <- line) { //for each header
        headers = headers + (new Field(clean(title), count)); //add it to the array
        count += 1;
      }
      nextLine(); //go to the next line so that the headers dont get analyzed like data
      headers.toArray;
    }

    def close() = { //closes the file buffer
      source.close();
    }

    def nextLine(): Boolean = { //goes to the next line of the buffer
      if (lines.hasNext) {
        line = lines.next().split(",")
        return true; //returns true if there are more lines, and false if there aren't
      }
      return false;
    }

    def getCol(col: Int): String = { //returns data from the csv for a certain column in the current row
      clean(line(col)); //the data
    }
    
    def clean(r: String): String = {
      if (r.length() == 0)  //removes quotes and replaces empties with zeroes
        "0"
      else if (r.charAt(0) == '"') {
        if (r.length() == 2)
          "0"
        else
          r.subSequence(1, r.length - 1).toString()
      } else r
    }

    def getField(title: String): Option[Field] = fields.find(_.title.equals(title)); //returns the field for a header

    def getFieldCol(title: String): Int = getField(title) match {  //same as above, but it gives the column
      case Some(field) => field.col;
      case None => {
        println("FIELD NOT RECOGNIZED: " + title);
        -1
      }
    }

  }

  case class PerformCSV(fn: String) extends CSV(fn);  //These exists to differentiate between the two types of CSVs
  case class SummaryCSV(fn: String) extends CSV(fn);

  class CSVGroup(directory: String) { //represents a directory of CSVs

    var count: Integer = 0;

    def next(): PerformCSV = {
      count += 1;
      return new PerformCSV(directory + count + "-perform.csv");
    }

    def getSummary(): SummaryCSV = {
      return new SummaryCSV(directory + "perform-summary.csv");
    }

    def hasNext(): Boolean = new File(directory + (count + 1) + "-perform.csv").exists();
  }

  case class Field(title: String, col: Int);  //represents a column in a CSV

  def choseDirectory(): Option[CSVGroup] = {
    val p: JFileChooser = new JFileChooser(); //Use the java libraries to show a file picker
    p.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
    p.setCurrentDirectory(new File("C:\\"));  //this is where I saved my files, makes it faster to test
    if (p.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
      return Some(new CSVGroup(p.getSelectedFile().getCanonicalPath() + System.getProperty("file.separator")));
    }
    return None;
  }

}