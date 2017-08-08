# Build Availability:

The entire code is written in Groovy with HTML and CSS inter webbed inside. I have never written in Groovy before but wanted to tackle a project that involved Jenkins integration. I realized that so many companies used Jenkins for running a bulk of their builds on VM’s. The problem was that there is only so much core power to run more and more builds because then at a certain point more VM’s would be created. As more VM/s went online this would cause a major issue with the speed of the Jenkins total environment capabilities. The goal of this project was to present a dashboard to every user which displayed each build in a 24 hour table. The use for this would be instead of adding a whole new VM or dealing with slow build times and utilizing there current avaivlity for builds. The user would be able to see if there are certain times where a lot of builds are clustered to space them out. Or if a new build needed to be run then they could see a good time to run this. The only builds that would appear would be the ones that run on a timer. I didn’t include any manual builds because If the user were to run a manual build they could just look at the schedule and space out when they need to run there build. 

The biggest problem I had intialiiy was understanding the mixture of Groovy along with Jenkins library. I was able to catch onto Groovy because of my Java backround but using the Jenkins documentation didn’t come as fast since a lot fo the documentation was out dated. My first objective was to find out how to go through every node, which in this case was each machine used to run the builds. After being able to find each node in the Jenkins environment, each node has builds underneath them, which can range from 1 to any possible number. Of course there could be faster ways to optimize this was of going through each and every build but at the moment optimizing for the quickest possible code wasn’t the objective. The code takes each VM machine and then begins to run through each of the builds on that machine. It begins by going through each build and uses a customized loop to extract particlar builds information. Before it even goes through the process of getting information from each build the build trigger is analyzed to see if it runs by manual command or through a scheduled activation. 

Then if the build is ran through a schedule it conintues and if not it gets discarded. The current build running through the loop is then brought on by a method to receive its last build duration which is formatted in hours, minutes, and seconds. This is then converted into only hours. The last build is also called upon by another method to find the exact start time of the build. Combing both the original start time of the last build along with the duration we now have a way to map each hour out for each build.

For the dashboard to universally used I wanted to make sure that the build names wouldn’t randomly be cut out of the html. If the build name exceeded a certain length then the second half would be put on the line below it. Then using the an array called hours I was able to mark each index as a 24 hour time manager. This would allow me to store the information until a later point. The next part was to obtain the machine name in which the current build was apart of. This name was then added to a varaible to be stored for later use. Towards the end of the loop the last part was to add each build to a data structure. A list utilizing placement variables was exactly what I used. The list stored the information stored before along with checking if the build passed, failed, or aborted. The variables were then reset for the next loop.

The next step was to present the data not only to the user but in the form of a dashboard integrated into Jenkins. By creating a character upstream I was able to then create a markupBuilder variable to begin constructing the html layout. I then created a table in html with rows to show ‘Machine names, build names, build date, and then the 24 hour layout”. After the rows were created the columns had to be constructed. This was done by integrating loops to form the machine name with all of the builds underneath. The CSS was used at first to style the rows and columns to be particular fonts. Then I went back in to organize the data into a colored coordinated system. The list created in the begiigngin begins to extract data from each section in the list to a particular row and columns in the table. After all the deconstruction and reassembly is made, the html is then exported into a file to export onto a custom dashboard in the Jenkins environment.
