package paper


object Analyze {
  def main(args : Array[String]): Unit = {

    // create analyse
    val A : Analyzer = new Analyzer()

    // Check that a directory is supplied
    if (args.length == 0) println("You really need to supply a directory as argument");

    // Then go ahead
    else A.analyze(args(0))
  }
}

class Analyzer extends Object with LoadPaper
                              with ParsePaper 
                              with ExtendPaper
                              with ComparePaper
                              with bagOfWords
                              with XMLScheduleParser
                              with Graphs {

  // Set a limit in percent for when papers get an edge between them
  val limit : Int = 1

  // Get cached papers in this order
  val cache : List[String] = List(Cache.extended, Cache.linked, Cache.scheduled, Cache.parsed)

  // Set sources we want to extend with
  //val sources : List[PaperSource] = List(TalkDates, TalkRooms, PdfLink)
  val sources : List[PaperSource] = List(PdfLink)

  // Analyze a paper
  def analyze(paperPos: String): Unit = {

    // Get a list of parsed papers
    val papers : List[Paper] = load(paperPos, cache, Isit)
   

    // Mix in the schedule XML data
    val xmlPapers : List[Paper] = getXMLSchedule(paperPos, papers)

    // Compare the papers individually
    val comparedPapers : List[Paper] = compare(xmlPapers, limit)
    print(comparedPapers)
    //Compare papers with bagOfWords
    
   // val comparedPapers2 : List[Paper] = bagOfWords(...)
    // Extend papers with tertiary data
    val extendedPapers : List[Paper] = extend(comparedPapers, sources) 
    
    // Create graph
    val graph : Graph = getGraph(extendedPapers)

    // Print graph to file 'data.json'
    graph.save
    
    // Export in cvs for import in Gephi
    graph.exportToGephi

  }
}
