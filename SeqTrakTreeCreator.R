args=(commandArgs(TRUE))
library(adegenet)
spearmanmat <- as.matrix(read.csv(file= file.path(args,"matrix.csv",sep="") , sep=",", header=FALSE))
date <- read.csv(file.path(args,"dates.csv",sep=""))
dates <- as.Date(date$collec.dates)
id <- date$id
res <- seqTrack(spearmanmat,x.names=id,x.dates=dates)
write.csv(res,file=file.path(args,"res.csv",sep=""))