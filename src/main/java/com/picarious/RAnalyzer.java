
package com.picarious;

import com.github.rcaller.exception.ExecutionException;
import com.github.rcaller.rStuff.RCaller;
import com.github.rcaller.rStuff.RCode;
import org.springframework.stereotype.Service;

import java.io.FileNotFoundException;

@Service
public class RAnalyzer {
    public void analyze(String corpusPathAndFile, String workingDirectory) {
        RCaller rCaller = new RCaller();
        rCaller.setRscriptExecutable("/usr/local/bin/RScript");
        RCode rCode = rCaller.getRCode();
        rCode.addRCode("corpus <- read.csv(\"" + corpusPathAndFile + "\")");
//        rCode.addRCode("normalize <- function(x) {");
//        rCode.addRCode("num <- x - min(x)");
//        rCode.addRCode("denom <- max(x) - min(x)");
//        rCode.addRCode(" return (num/denom)");
//        rCode.addRCode("}");
        rCode.addRCode("normalize <- function(x) {");
        rCode.addRCode("factor<-max(c(max(x), abs(min(x))))");
        rCode.addRCode("return (x/factor)");
        rCode.addRCode("}");
        rCode.addRCode("corpus_norm <- as.data.frame(c(corpus[1],lapply(corpus[2:length(corpus)], normalize)))");
        rCode.addRCode("set.seed(4321)");
        rCode.addRCode("library(gmodels)");
        rCode.addRCode("library(class)");
        rCode.addRCode("ind <- sample(2, nrow(corpus_norm), replace=TRUE, prob=c(0.67, 0.33))");
        rCode.addRCode("corpus.training <- corpus_norm[ind==1, 2:length(corpus_norm)]");
        rCode.addRCode("corpus.test <- corpus_norm[ind==2, 2:length(corpus_norm)]");
        rCode.addRCode("corpus.trainLabels <- corpus_norm[ind==1, 1]");
        rCode.addRCode("corpus.testLabels <- corpus_norm[ind==2, 1]");
        rCode.addRCode("corpus_pred <- knn(train = corpus.training, test = corpus.test, cl = corpus.trainLabels, k=5)");
        rCode.addRCode("corpus[0,]");
        rCode.addRCode("cat(\"failures =\", length(which(corpus_pred != corpus.testLabels)))");
        rCode.addRCode("CrossTable(x = corpus.testLabels, y = corpus_pred, prop.chisq=FALSE)");
        try {
            rCaller.redirectROutputToFile(workingDirectory + "Routput.txt", true);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        try {
            rCaller.runOnly();
        } catch(ExecutionException e) {
            e.printStackTrace();

        }
    }
}
