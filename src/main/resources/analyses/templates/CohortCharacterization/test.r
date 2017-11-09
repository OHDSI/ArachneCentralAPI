dbms <- "postgresql"
connectionString <- "jdbc:postgresql://odysseusovh02.odysseusinc.com:5432/cdm_v500_synpuf_v101_110k"
user <- "ohdsi"
password <- "ohdsi"

install.packages("drat")
drat::addRepo("OHDSI")
install.packages("rJava")
install.packages("DatabaseConnector")

# Solves issue with x64 (https://stackoverflow.com/questions/7019912/using-the-rjava-package-on-win7-64-bit-with-r)
if (Sys.getenv("JAVA_HOME")!="")
  Sys.setenv(JAVA_HOME="")

library(DatabaseConnector)

connectionDetails <- createConnectionDetails(dbms=dbms,
                                             connectionString=connectionString,
                                             user=user,
                                             password=password)
connection <- connect(connectionDetails)

disconnect(connection)