if (!require("packrat")) {
  install.packages("packrat")
}
packratRun <- function (packratPath, workDir, func){
  packratFolder <- substr(packratPath, 0, nchar(packratPath)-nchar(".tar.gz"))
  tryCatch(
    {
      tryCatch(
        {
          setwd(workDir)
          packrat::unbundle(bundle = packratPath, where = workDir)
          setwd(packratFolder)
        },
        error = function(e) {
          setwd(packratFolder)
          install.packages("./windows_binaries/XML_3.98-1.6.zip", repos = NULL, type = "binary", lib = "./packrat/lib/x86_64-w64-mingw32/3.2.3")
          install.packages("./windows_binaries/curl_2.5.zip", repos = NULL, type = "binary", lib = "./packrat/lib/x86_64-w64-mingw32/3.2.3")
          install.packages("./windows_binaries/git2r_0.18.0.zip", repos = NULL, type = "binary", lib = "./packrat/lib/x86_64-w64-mingw32/3.2.3")
          install.packages("./windows_binaries/openssl_0.9.6.zip", repos = NULL, type = "binary", lib = "./packrat/lib/x86_64-w64-mingw32/3.2.3")
          install.packages("./windows_binaries/rJava_0.9-8.zip", repos = NULL, type = "binary", lib = "./packrat/lib/x86_64-w64-mingw32/3.2.3")
          install.packages("./windows_binaries/stringi_1.1.5.zip", repos = NULL, type = "binary", lib = "./packrat/lib/x86_64-w64-mingw32/3.2.3")
          install.packages("./windows_binaries/RCurl_1.95-4.8.zip", repos = NULL, type = "binary", lib = "./packrat/lib/x86_64-w64-mingw32/3.2.3")
          packrat::restore()
        },
        finally = {
          packrat::packrat_mode(on=TRUE)
          writeLines("Session library paths:")
          print(.libPaths())
          do.call(func, list(workDir))
        }
      )
    },
    finally = {
      setwd(workDir)
      writeLines("Session info:")
      print(sessionInfo())
      if (file.exists(packratFolder)) {
        unlink(packratFolder, recursive = TRUE)
      }
    }
  )
}
