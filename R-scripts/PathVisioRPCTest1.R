
# Installing XMLRPC package
install.packages("RCurl")
install.packages("XMLRPC", repos="http://R-Forge.R-project.org")

library("XMLRPC")

# start PathVisioRPC server (e.g. in PathVisio with the plugin)
server = "http://localhost:7777"

dir <- "C:/Users/martina.kutmon/Downloads/"
author <- "Anwesha"
pathwayName <- "Test_Pathway"
species <- "Homo Sapiens"

# create a new pathway and add two datanodes
xml.rpc(server,"PathVisio.test")
xml.rpc(server,"PathVisio.createPathway",pathwayName,author,species,dir)
xml.rpc(server,"PathVisio.addDataNode", paste(dir, pathwayName, ".gpml", sep=""), "Glucose","Metabolite"," HMDB00122","HMDB")
xml.rpc(server,"PathVisio.addDataNode", paste(dir, pathwayName, ".gpml", sep=""), "Glucose-6-P","Metabolite"," HMDB01401","HMDB")

# export pathway png file
xml.rpc(server,"PathVisio.exportPathway",paste(dir, pathwayName, ".gpml", sep=""), "png",dir)
