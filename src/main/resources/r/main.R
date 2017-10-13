install.packages("import")
import::from(dbConnection.R, conn) # https://github.com/smbache/import
library(DatabaseConnector)

# List all tables in the PostgreSQL database
res <- querySql(conn, paste("SELECT table_name",
    "FROM information_schema.tables",
    "WHERE table_type = 'BASE TABLE'",
    "AND table_schema NOT IN ",
    "('pg_catalog', 'information_schema')"))

file.create("output.txt")
fileConn <- file("output.txt")
writeLines(paste(res), fileConn)
close(fileConn)
dbDisconnect(conn)