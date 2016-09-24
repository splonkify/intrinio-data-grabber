
package com.picarious;

import com.github.rcaller.exception.ExecutionException;
import com.github.rcaller.rStuff.RCaller;
import com.github.rcaller.rStuff.RCode;
import org.springframework.stereotype.Service;

import java.io.FileNotFoundException;

@Service
public class RClassifier {
    public int classify(String trainingSet, String testSet, String workingDirectory, boolean append) {
        RCaller rCaller = new RCaller();
        rCaller.setRscriptExecutable("/usr/local/bin/RScript");
        RCode rCode = rCaller.getRCode();
        rCode.addRCode("training <- read.csv(\"" + trainingSet + "\")");
        rCode.addRCode("testing <- read.csv(\"" + testSet + "\")");
        rCode.addRCode("normalize <- function(x) {");
        rCode.addRCode("factor<-max(c(max(x), abs(min(x))))");
        rCode.addRCode("return (x/factor)");
        rCode.addRCode("}");
        rCode.addRCode("training_norm <- as.data.frame(c(training[1],lapply(training[2:length(training)], normalize)))");
        rCode.addRCode("testing_norm <- as.data.frame(c(testing[1],lapply(testing[2:length(testing)], normalize)))");
        rCode.addRCode("training_labels <- training_norm[, 1]");
        rCode.addRCode("library(class)");
        rCode.addRCode("predictions <- knn(train = training_norm, test = testing_norm, cl = training_labels, k=5)");
        rCode.addRCode("predictions[0,]");
        try {
            rCaller.redirectROutputToFile(workingDirectory + "RClassifier.txt", append);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        try {
            rCaller.runAndReturnResult("failures");
        } catch(ExecutionException e) {
            e.printStackTrace();
            return Integer.MAX_VALUE;
        }
        return rCaller.getParser().getAsIntArray("failures")[0];
    }
}
