

object GroupHandler {

  class Group(val id: Int, val model: String, var systems: Array[SystemHandler.System]) {  //each group is a different model of server

    var averageSystem: SystemHandler.System = new SystemHandler.System(-1);

    def calculateAverageSystem() = {
      averageSystem.group = this;
      averageSystem.model = systems(0).model;
      
      var freePctSum: Double = 0;
      var delAcksPctSum: Double = 0;
      var readBandwithSum: Double = 0;
      var writeBandwithSum: Double = 0;
      var cpuAvgPctSum: Double = 0;
      var cpuMaxPctSum: Double = 0;
      var nodesOfflineSum: Double = 0;
      var nodesMissingSum: Double = 0;
      var ddSizeSum: Double = 0;
      for (s <- systems) {
        freePctSum += s.freePct;
        delAcksPctSum += s.delAcksPct;
        readBandwithSum += s.readBandwith;
        writeBandwithSum += s.writeBandwith;
        cpuAvgPctSum += s.cpuAvgPct;
        cpuMaxPctSum += s.cpuMaxPct;
        nodesOfflineSum += s.nodesOffline;
        nodesMissingSum += s.nodesMissing;
        ddSizeSum += s.ddSize;
      }
      
      averageSystem.freePct = (freePctSum / systems.size).toFloat;
      averageSystem.delAcksPct =  (delAcksPctSum / systems.size).toFloat;
      averageSystem.readBandwith = (readBandwithSum / systems.size).toFloat;
      averageSystem.writeBandwith = (writeBandwithSum / systems.size).toFloat;
      averageSystem.cpuAvgPct = (cpuAvgPctSum / systems.size).toFloat;
      averageSystem.cpuMaxPct = (cpuMaxPctSum / systems.size).toFloat;
      averageSystem.nodesOffline = (nodesOfflineSum / systems.size).toFloat;
      averageSystem.nodesMissing = (nodesMissingSum / systems.size).toFloat;
      averageSystem.ddSize = (ddSizeSum / systems.size).toFloat;
    }
  }

  def createGroups(systems: Array[SystemHandler.System]): Array[Group] = {
    var groups: Array[Group] = Array[Group]();  //create the array of groups

    var groupMap: Map[String, Int] = Map();  //create a map from a model type to a position in the group array
    var counter: Integer = 0;  //count the number of groups

    for (s <- systems) {//for all systems
      var groupIndex = -1;  //the index in the array for this systems group
      if (!groupMap.contains(s.model)) {//if there isn't a group for this model yet
        groupMap += ((s.model, counter));  //add the group to the map
        groups = groups :+ new Group(counter, s.model, Array[SystemHandler.System]()); //create the new group
        groupIndex = counter;
        counter += 1;
      }else{
        groupIndex = groupMap.apply(s.model);
      }
      groups(groupIndex).systems = groups(groupIndex).systems :+ s; //Add the system to the group
      s.group = groups(groupMap.apply(s.model)); //Tell the system what group its in
    }
    return groups;
  }

  def getGroup(system: SystemHandler.System, groups: Array[Group]): Group = {
    groups.find(g => g.systems.contains(system)) match {
      case Some(x) => x
      case None =>
        println("HOW DID THIS HAPPEN? SYSTEM NOT RECOGNIZED: " + system.id);
        null;
    }
  }
}