/*** BEGIN META {
  "name" : "Build availability",
  "comment" : "Check's to see which builds are available at certain hours",
  "parameters" : [],
  "core": "1.300",
  "authors" : [
    { name : "Ryan Lehman" },
  ]
} END META**/

import groovy.xml.MarkupBuilder
import  java.util.concurrent.TimeUnit
import jenkins.model.Jenkins
import hudson.model.*
import hudson.FilePath
import groovy.time.*
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.File;
import java.io.Writer;
import java.io.FileNotFoundException;
import java.io.IOException;


def inputFile = new File("/var/lib/jenkins/jobs/Groovy Script/workspace/index.html")


//Init. varaibles

//Have more then enough colors to match with each machine node
def allColors = ["AliceBlue","Yellow","Turquoise","SkyBlue","Red","Pink","PapayaWhip","BlanchedAlmond","BurlyWood", "Cornsilk","Cyan", "FloralWhite","Gainsboro","GhostWhite","Gold","GoldenRod","HoneyDew","HotPink","Ivory","Khaki","Lavender","LavenderBlush","LemonChiffon","LightBlue","LightCyan","LightGoldenRodYellow", "LightGreen","LightPink","LightSalmon","LightSeaGreen","LightSkyBlue","LightSteelBlue","LightYellow","Linen","MintCream","MistyRose","Moccasin","NavajoWhite","OldLace","Orange","Orchid","PaleGoldenRod","PaleGreen","PaleTurquoise","PaleVioletRed","PapayaWhip","PeachPuff","Peru","Pink","Plum","PowderBlue","Red","RoyalBlue","Salmon","SandyBrown","SeaGreen","SeaShell","Sienna","Silver","SkyBlue","SlateBlue","SlateGray","SlateGrey","Snow","SpringGreen","SteelBlue","Tan","Teal","Thistle","Tomato","Turquoise","Violet","Wheat","White","WhiteSmoke","Yellow","YellowGreen"]
//Starts a list that will contain all machine names
List allNodeNames = []
//A placeholder value for storing each nodeName for short term memory
def nodeName2 = ""
//A counter to make sure each machine has its own colors and no to are the same
def nodeCounter = 0
//Starts a list to organize each build with certain criteria 
List list = []
//Begins each hour with no builds for any hours
def hours = ["--","--","--","--","--","--","--","--","--","--","--","--","--","--","--","--","--","--","--","--","--","--","--","--","?"]
//Is a placeholder for the backround color choosen for each machine and stores it for short term memory while the for loop runs below in html
def backroundColor = ""
def date = ""

//Obtains each machine
Jenkins.instance.slaves.each{
 allNodeNames.add(nodeName: it.getNodeName(), color: allColors[nodeCounter])
  nodeCounter++
}


//Obtains each build from every node
Jenkins.instance.getAllItems(Job).each{
  
  def jobBuilds=it.getLastBuild()
  
  //For each of such jobs we can get all the builds (or you can limit the number at your convenience)
    jobBuilds.each { build ->

      //Retrieves the cause of each build and stores that into a temporary value
      def triggerCause = build.getCauses()

      //Only choose from the builds that are triggered (no manual builds)
      if ((triggerCause.toString()).contains("trigger")){
        date = build.getTime().format("YYYY-MMM-dd")
        //Generates the duration of each build and then formats into frames we can manipulate
        def currentStatus = build.getDuration()
        
        duration = String.format("%d hr, %d min, %d sec",
                TimeUnit.MILLISECONDS.toHours(currentStatus), TimeUnit.MILLISECONDS.toMinutes(currentStatus) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(currentStatus)),
            TimeUnit.MILLISECONDS.toSeconds(currentStatus) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(currentStatus)))

        //Able to get Hours minutes and seconds from url -> (http://docs.oracle.com/javase/8/docs/api/java/util/Date.html?is-external=true)
        def runTimeSeconds = (build.getTime()).getSeconds()
        def runTimeMinutes = (build.getTime()).getMinutes()
        def runTimeHours = (build.getTime()).getHours()

        //If the build name is long it needs to be cut and put on the line below the first half
        def longName = false
        def newtest = (runTimeHours + TimeUnit.MILLISECONDS.toHours(currentStatus))%24
        def buildStringSize = build.toString()
        def counter = 0;
        def var = 0

        //Runs through the duration of each build and marks it under each hour in the array
        if (TimeUnit.MILLISECONDS.toHours(currentStatus) != 0){

          //Goes 1 hour past the duration to round up
          while (counter < (TimeUnit.MILLISECONDS.toHours(currentStatus) + 2)){

            var = ((runTimeHours + counter))%24

            if (var == 0){

                var = 24
                hours[var-1] = var.toString()
            }

            hours[var-1] = var.toString()
            counter++
          }

        } else {

          //The builds with less then an hour in duration only have the time they start plus and hour for rounding
          var = runTimeHours
          hours[var-1] = var.toString()
          //Added the extra hour since every one of them goes over - used for rounding

          if (((var + 1)%24) == 0){
            hours[var] = "24"
          } else {
            hours[var] = ((var + 1)%24).toString()
          }
        }
        
    //Obtains the machine that directly correlates with the current build
    if (build.getBuiltOn() != null){

          //If the machine is from the standard jenkins then it just gets put into a placeholder for it to be stored in a list
          if ((build.getBuiltOn().toString()).contains("model")){
            nodeName2 = "Jenkins"
           
            
          //If the machine is not then all the extra data is parsed out and only the machine name is then stored in the remote variable 
          } else {
            def stringStart = (build.getBuiltOn().toString()).indexOf("[")
            def stringEnd = (build.getBuiltOn().toString()).indexOf("]")
            
            nodeName2 = (build.getBuiltOn().toString()).substring(stringStart + 1,stringEnd)
            
          }
        }

    //At the end of each build it is then stored in a list with all of its current values
    list.add(buildName: buildStringSize, nodeName: nodeName2, buildStatus: build.getResult(), buildHours: hours, buildDate: date)
        //The values for hours are then reset to begin the start of reading the next build
        hours = ["--","--","--","--","--","--","--","--","--","--","--","--","--","--","--","--","--","--","--","--","--","--","--","--","?"]

      }
    }
}



//Initializes a character stream to be used to form a final string
StringWriter writer = new StringWriter()
//Defines a MarkupBuilder variable to begin contructing the data in html
def build = new MarkupBuilder(writer)
//Sorts the build list by machine name to organize it in a html table
list.sort{it.nodeName}

//Begins to form the html format
build.html{
  head{
    style(type:"text/css", '''
    .bigHeader {
        text-align: center;
        
        padding: 2px;
        
        padding-left: 10px;
        padding-right: 50px;
        background-color: #F0F8FF
    }
  .notBuilds {
        text-align: center;
        width: 90px;
        margin: 10px;
       padding: 2px;
       padding-left: 30px;
  }
  .buildNames {
        width: 100px;
        margin: 10px;
       padding: 2px;
        background-color: #F0FFFF
    }
  .buildNames2 {
		text-align: center;
        width: 100px;
        margin: 10px;
       padding: 2px;
        background-color: Violet
    }
  .busyBuilds {
        text-align: center;
        width: 100px;
        margin: 10px;
        padding: 5px;
        background-color: #80ff00
    }
   .busyBuildsRed {
        text-align: center;
        width: 100px;
        margin: 10px;
        padding: 5px;
        background-color: #FF3333
    }
    .busyBuildsYellow {
        text-align: center;
        width: 100px;
        margin: 10px;
        padding: 5px;
        background-color: #FFFC33
    }


    ''')
  }
  body{
    table{
      tr{
        //Forms the rows in the table with the title of the data needed to be inputted
        th('class':'bigHeader', "Machine Names:")
        th('class':'bigHeader', "Build Names:")
        th('class':'bigHeader', "Build Date:")
        th('class':'bigHeader', "1 am")
        th('class':'bigHeader', "2 am")
        th('class':'bigHeader', "3 am")
        th('class':'bigHeader', "4 am")
        th('class':'bigHeader', "5 am")
        th('class':'bigHeader', "6 am")
        th('class':'bigHeader', "7 am")
        th('class':'bigHeader', "8 am")
        th('class':'bigHeader', "9 am")
        th('class':'bigHeader', "10 am")
        th('class':'bigHeader', "11 am")
        th('class':'bigHeader', "12 am")
        th('class':'bigHeader', "1 pm")
        th('class':'bigHeader', "2 pm")
        th('class':'bigHeader', "3 pm")
        th('class':'bigHeader', "4 pm")
        th('class':'bigHeader', "5 pm")
        th('class':'bigHeader', "6 pm")
        th('class':'bigHeader', "7 pm")
        th('class':'bigHeader', "8 pm")
        th('class':'bigHeader', "9 pm")
        th('class':'bigHeader', "10 pm")
        th('class':'bigHeader', "11 pm")
        th('class':'bigHeader', "12 pm")
        
        
            
      }
      
        //Loops through each build and finds the machine allocated with the same machine name to allow each build and machine name to be organized by color 
        for (i = 0; i < list.size(); i++) {
          
            for (p = 0; p < allNodeNames.size(); p++){
              
              if ((list[i]["nodeName"]) == allNodeNames[p]["nodeName"]){
                backroundColor = allNodeNames[p]["color"]
                
              }
            }
          
          tr{
            td('class': 'buildNames2', 'style': "background-color:" + backroundColor.toString(), list[i]["nodeName"])
            td('class': 'buildNames2', 'style': "background-color:" + backroundColor.toString(), list[i]["buildName"])
            td('class': 'buildNames2', 'style': "background-color:" + backroundColor.toString(), list[i]["buildDate"])
            
            //Runs through each hour for each build and marks in on the html table as current build time and status
            for (j = 0; j < 24; j++) {
                
                if ((list[i]["buildHours"][j]).contains("--")){
                  td('class': 'notBuilds', "----")
                } else if ((list[i]["buildStatus"].toString()).contains("SUCCESS")) {
                  
                  td('class': 'busyBuilds', list[i]["buildStatus"])
                  
                } else if ((list[i]["buildStatus"].toString()).contains("FAILURE")) {
                  
                  td('class': 'busyBuildsRed', list[i]["buildStatus"])
                  
                } else if ((list[i]["buildStatus"].toString()).contains("ABORTED")) {
                  
                  td('class': 'busyBuildsYellow', list[i]["buildStatus"])
                  
                }
            }
          }
        
        }
    }
  }
}
//Stores the html inside a file to export onto its own custom Jenkins dashboard
inputFile.write (writer.toString())

