library(DatabaseConnector)

dbms <- Sys.getenv("DBMS_TYPE")
connectionString <- Sys.getenv("CONNECTION_STRING")
user <- Sys.getenv("DBMS_USERNAME")
pw <- Sys.getenv("DBMS_PASSWORD")
cdmSchema <- Sys.getenv("DBMS_SCHEMA")

if ("impala" == dbms){
    driverPath <- Sys.getenv("IMPALA_DRIVER_PATH")
    if (missing(driverPath) || is.null(driverPath) || driverPath == ''){
        driverPath <- "/impala"
    }
    connectionDetails <- createConnectionDetails(dbms=dbms,
    connectionString=connectionString,
    user=user,
    password=pw,
    schema=cdmSchema,
    pathToDriver=driverPath)
} else {
  connectionDetails <- createConnectionDetails(dbms=dbms,
    connectionString=connectionString,
    user=user,
    password=pw,
    schema=cdmSchema)
}

conn <- connect(connectionDetails)