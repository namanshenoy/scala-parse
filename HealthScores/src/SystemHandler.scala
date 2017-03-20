

object SystemHandler {

  case class System(val id: Int) {
    var group: GroupHandler.Group = null;
    //Storing these values in an array by type would probably save a lot of code....
    //Summary variables
    var company: String = "None";
    var model: String = "None";
    var freePct: Float = -1; //percent of free space

    //Performance variables
    var patch: String = "None"; //TODO this
    var delAcksPct: Float = -1; //Delayed acknowledgement percent
    //    var totalsPct: Array[Float] = Array[Float](21); //read/write speed buckets SPECIAL CASE! TODO this also
    var readBandwith: Float = -1; //Read bandwidth
    var writeBandwith: Float = -1; //Write bandwidth
    var cpuAvgPct: Float = -1; //Average CPU use
    var cpuMaxPct: Float = -1; //Max CPU use
    var nodesOffline: Float = -1; //number of nodes offline
    var nodesMissing: Float = -1; //number of nodes missing
    var ddSize: Float = -1; //Deduplication store size in TB

    var healthScore: Float = -1;

    override def equals(other: Any): Boolean = other match { //compare systems only by their id so its faster
      case System(otherId) => id == otherId
      case _               => false;
    }

    override def toString(): String = { //behold the majesty
      s"id: ${id}, company: ${company}, model: ${model}, freePct: ${freePct}, patch: ${patch}, delAcksPct: ${delAcksPct}, readBandwith: ${readBandwith}, writeBandwith: ${writeBandwith}, cpuAvgPct: ${cpuAvgPct}, cpuMaxPct: ${cpuMaxPct}, nodesOffline: ${nodesOffline}, nodesMissing: ${nodesMissing}, ddSize: ${ddSize}";
    }

    def calculateHealthScore() { //The magic sauce
      val average = group.averageSystem; //get the average system
      healthScore = 0; //reset the health score
      healthScore += calc(freePct, average.freePct, 100, 200, true);
      healthScore += calc(delAcksPct, average.delAcksPct, 100, 200, false);
      healthScore += calc(readBandwith, average.readBandwith, 300, 20, true);
      healthScore += calc(writeBandwith, average.writeBandwith, 300, 20, true);
      healthScore += calc(cpuAvgPct, average.cpuAvgPct, 100, 300, false);
      healthScore += calc(cpuMaxPct, average.cpuMaxPct, 100, 200, false);
      healthScore += calc(nodesOffline, average.nodesOffline, 5, 100, false);
      healthScore += calc(nodesMissing, average.nodesMissing, 5, 100, false);
      healthScore += calc(ddSize, average.ddSize, 10, 50, false);
      healthScore = 100 - healthScore;
      //TODO some normalization here so that there aren't negative scores
    }

    private def calc(value: Float, average: Float, range: Double, weight: Double, higherBetter: Boolean): Float = { //lower is better
      if (higherBetter) {
        if (value > average) //if the value is better than the average it shouldn't improve the health score, we only want bad things
          return 0;
        else {
          val r = (average - value)/(range/10)-5;
          return ((1 / (1 + Math.exp(-r))) * weight).toFloat; //sigmoid function so that larger differences mean more to a point
        }
      } else {
        if (average > value) //Same here
          return 0;
        else {
          val r = (average - value)/(range/10)-5;
          return ((1 / (1 + Math.exp(-r))) * weight).toFloat;
        }
      }
    }
  }

  //  def createSystems(csvName: String): Array[System] = createSystems(new CSVHandler.SummaryCSV(csvName));

  def createSystems(csv: CSVHandler.SummaryCSV): Array[System] = { //Creates systems from summary CSV. Doesn't fill in info from performance CSV
    val idCol = csv.getFieldCol("serialNumberInserv"); //get columns for data
    val companyCol = csv.getFieldCol("system_companyName");
    val modelCol = csv.getFieldCol("system_model");
    val freePctCol = csv.getFieldCol("capacity_total_freePct");

    var systems: Array[System] = Array[System]();
    do { //For each line
      val id = csv.getCol(idCol).toInt; //get data from csv and put it into a new system
      val system = new System(id);
      system.company = csv.getCol(companyCol);
      system.model = csv.getCol(modelCol);
      system.freePct = csv.getCol(freePctCol).toFloat;
      systems = systems :+ system; //add the new system to collection
    } while (csv.nextLine()) //I use a do while so that I don't skip the first line
    return systems;
  }

  //Behold the spaghetti code
  def parseCSV(csv: CSVHandler.PerformCSV, systems: Array[System]) = { //Fills in information from performance CSV
    //FIND THE COLUMNS OF THE DATA
    val idFieldCol = csv.getFieldCol("systemId")
    val id = csv.getCol(idFieldCol).toInt; //Read systemID
    val system = SystemHandler.getSystem(id, systems); //find the system this CSV describes from its systemID

    //    val patchCol = csv.getFieldCol("tpdKernelPatch"); TODO Need list of patches to determine max patch
    val delAcksPctCol = csv.getFieldCol("delAcksPct");
    var delAcksPctSum: Float = 0; //Calculating average
    var delAcksPctCount: Int = 0;
    //    val totalsPctCol = csv.getFieldCol(""); TODO READ TO ARRAYS
    val readBandwithCol = csv.getFieldCol("portReadBandwidthMBPS");
    var readBandwithSum: Float = 0; //Calculating average
    var readBandwithCount: Int = 0;
    val writeBandwithCol = csv.getFieldCol("portWriteBandwidthMBPS");
    var writeBandwithSum: Float = 0; //Calculating average
    var writeBandwithCount: Int = 0;
    val cpuAvgPctCol = csv.getFieldCol("cpuLatestTotalAvgPct");
    var cpuAvgPctSum: Float = 0; //Calculating average
    var cpuAvgPctCount: Int = 0;
    val cpuMaxPctCol = csv.getFieldCol("cpuLatestTotalMaxPct");
    var cpuMaxPctSum: Float = 0; //Calculating average
    var cpuMaxPctCount: Int = 0;
    val nodesOfflineCol = csv.getFieldCol("nodeCountOffline");
    var nodesOfflineSum: Float = 0; //Calculating average
    var nodesOfflineCount: Int = 0;
    val nodesMissingCol = csv.getFieldCol("nodeCountMissing");
    var nodesMissingSum: Float = 0; //Calculating average
    var nodesMissingCount: Int = 0;
    val ddSizeCol = csv.getFieldCol("ddsSizeUsedTiB");
    var ddSizeSum: Float = 0; //Calculating average
    var ddSizeCount: Int = 0;

    //TAKE SUMS OF ALL THE ROWS
    do { //For each line in the CSV
      var value = csv.getCol(delAcksPctCol).toFloat;
      if (value != 0) { //Dont add it if it's zero
        delAcksPctSum += value;
        delAcksPctCount += 1;
      }

      value = csv.getCol(readBandwithCol).toFloat;
      if (value != 0) { //Dont add it if it's zero
        readBandwithSum += value;
        readBandwithCount += 1;
      }

      value = csv.getCol(writeBandwithCol).toFloat;
      if (value != 0) { //Dont add it if it's zero
        writeBandwithSum += value;
        writeBandwithCount += 1;
      }

      value = csv.getCol(cpuAvgPctCol).toFloat;
      if (value != 0) { //Dont add it if it's zero
        cpuAvgPctSum += value;
        cpuAvgPctCount += 1;
      }

      value = csv.getCol(cpuMaxPctCol).toFloat;
      if (value != 0) { //Dont add it if it's zero
        cpuMaxPctSum += value;
        cpuMaxPctCount += 1;
      }

      value = csv.getCol(nodesOfflineCol).toFloat;
      if (value != 0) { //Dont add it if it's zero
        nodesOfflineSum += value;
        nodesOfflineCount += 1;
      }

      value = csv.getCol(nodesMissingCol).toFloat;
      if (value != 0) { //Dont add it if it's zero
        nodesMissingSum += value;
        nodesMissingCount += 1;
      }

      value = csv.getCol(ddSizeCol).toFloat;
      if (value != 0) { //Dont add it if it's zero
        ddSizeSum += value;
        ddSizeCount += 1;
      }

    } while (csv.nextLine());
    //CALCULATE AVERAGES AND STORE THEM
    system.delAcksPct = div(delAcksPctSum, delAcksPctCount);
    system.readBandwith = div(readBandwithSum, readBandwithCount);
    system.writeBandwith = div(writeBandwithSum, writeBandwithCount);
    system.cpuAvgPct = div(cpuAvgPctSum, cpuAvgPctCount);
    system.cpuMaxPct = div(cpuMaxPctSum, cpuMaxPctCount);
    system.nodesOffline = div(nodesOfflineSum, nodesOfflineCount);
    system.nodesMissing = div(nodesMissingSum, nodesMissingCount);
    system.ddSize = div(ddSizeSum, ddSizeCount);
  }

  def div(x: Float, y: Int): Float = {
    if (x != 0 && y != 0) //Required to stop all zero systems from making averages NaN
      x / y;
    else
      0;
  }

  def getSystem(id: Int, systems: Array[System]): System = { //find a system from its ID
    systems.find(_.id == id) match {
      case Some(x) => x
      case None =>
        println("HOW DID THIS HAPPEN? SYSTEM NOT RECOGNIZED: " + id);
        null;
    }
  }
}