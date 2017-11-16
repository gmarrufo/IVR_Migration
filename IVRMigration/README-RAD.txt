############################################################################
Installation
1. In the Package Explorer view, right click and select "Import"
2. Select Project Interchange from EUSMigration.zip
3. Click Finish
4. EUSMigration uses the ANTLR parser generator which is not distributed 
   with the source code. 
	- Download http://www.antlr.org/download/antlr-3.2.jar to the 
	  EUSMigration folder
	- Right click on the jar file and select "Add To Build Path"
	- The project is a java project and will automatically re-build 

Configuration
1. Edit the migration.properties default configuration file 
2. Most importantly specify which ini file you want to migrate
3. The ini.zip file contains the ini files. Remove the ini folder and ini.zip 
   will be unpacked again
4. Set developmentMode to true so that the parser stub files are rebuilt   

Execution 
1. Right click on the EUSMigration project and select Run as->java app
	- Select "Migration" halfway down the list
	- The Migration will run and put the results into 
	EUSMigration/CallRoutingConfig

See README.txt to run outside of the RAD development environment
############################################################################


 
