import javax.swing.JFileChooser
import javax.swing.filechooser.FileFilter
import javax.swing.filechooser.FileNameExtensionFilter
import java.io.File

object Main {

  def main(args: Array[String]): Unit = {

    CSVHandler.choseDirectory() match {  //let the user select the directory
      case Some(d) => {
        val before = java.lang.System.currentTimeMillis(); //measure run time
        calculate(d);
        System.out.println("millis: " + (java.lang.System.currentTimeMillis() - before)); //print run time
      }
      case None => println("Pick a file next time, bud");
    }
  }

  def calculate(csvGroup: CSVHandler.CSVGroup) {
    var systems = SystemHandler.createSystems(csvGroup.getSummary()); //Create the systems from the summary csv
    println(systems.size + " systems");
    var groups = GroupHandler.createGroups(systems); //Split them into gropus
    println(groups.size + " groups");

    val dashCount = 100.0 //for the nifty progress bar
    for (i <- Range(0, dashCount.toInt)) {
      print("-");
    }
    println();
    var doneCount = 0.0;
    var doneTo = systems.size / dashCount;

    while (csvGroup.hasNext()) { //Loop through each CSV file

      doneCount += 1; //progress bar
      if (doneCount > doneTo) {
        doneCount -= doneTo;
        print("-");
      }

      val csv = csvGroup.next(); //Open next CSV
      SystemHandler.parseCSV(csv, systems); //fill in the systems with the data
      csv.close(); //Close it
    }
    println(); //a new line so that its not writing next to the progress bar
    println("Finished Parsing");
    groups.foreach(_.calculateAverageSystem()); //calculate the average system for each group
    println("Calculated Averages");
    systems.foreach(_.calculateHealthScore()); //aaand calculate the health scores

    systems.foreach(s => {
      println("This: " + s);  //print this system
      println("Average: " + s.group.averageSystem);  //print this system's group's average 
      println(s.healthScore)  //print all the health scores
    });
    //TODO save the health scores or averages
  }

}