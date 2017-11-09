toCamelCase <- function(value) {
  return(gsub("(?<=[^\\W_])_+([^\\W_])|([^\\W_]+)|_+", "\\U\\1\\L\\2", value, perl = TRUE))
}

transformColnamesToCamelCase <- function(dataframe) {
  colnames(dataframe) <- lapply(
    colnames(dataframe),
    function(header) {
      return(toCamelCase(header))
    }
  )
  return(dataframe)
}