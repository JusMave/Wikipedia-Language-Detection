import java.io.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class Wiki {

    public class Node {
        Node parent = null;
        boolean value;
        int bestAttribute = -1;
        Node trueChild = null;
        Node falseChild = null;
        Node(){}
        Node(int bestAttribute){
            this.bestAttribute = bestAttribute;
        }

    }
    public char[] charcterTable = new char[]{
            'a', 'b', 'c', 'd', 'e',
            'f', 'g', 'h', 'i', 'j',
            'k', 'l', 'm', 'n', 'o',
            'p', 'q', 'r', 's', 't',
            'u', 'v', 'w', 'x', 'y', 'z'
    };

    public static void main(String[] args) {
        int modelType;
        Wiki wIns = new Wiki();
        List<String[]> inputData = new ArrayList<>();
        List<String[]> dutchData = new ArrayList<>();
        List<String[]> italianData = new ArrayList<>();
        if(args[0].equals("train")){
            dutchData = wIns.readFile("src\\dutchData.txt");
            italianData = wIns.readFile("src\\italianData.txt");
            //Dutch data
            boolean[][] dutchDataTruthTable = wIns.calTruthTable(dutchData);
            for(int i = 0; i < dutchDataTruthTable.length; i++){
                dutchDataTruthTable[i][dutchDataTruthTable[i].length - 1] = true;
            }

            for(boolean[] lineResult : dutchDataTruthTable){
                for(boolean value : lineResult){
                    System.out.print(value + " ");
                }
                System.out.println();
            }
            System.out.println();
            //Italian data
            boolean[][] italianDataTruthTable = wIns.calTruthTable(italianData);
            for(int i = 0; i < italianDataTruthTable.length; i++){
                italianDataTruthTable[i][italianDataTruthTable[i].length - 1] = false;
            }

            for(boolean[] lineResult : italianDataTruthTable){
                for(boolean value : lineResult){
                    System.out.print(value + " ");
                }
                System.out.println();
            }
            System.out.println();
            System.out.println("Build Decision Tree: ");
            System.out.println("----Find best depth limit----");
            System.out.println("Acc:");
            boolean[][] wholeTable = new boolean[dutchDataTruthTable.length + italianDataTruthTable.length][dutchDataTruthTable[0].length];
            for(int i = 0; i < wholeTable.length; i++){
                if(i < dutchDataTruthTable.length){
                    wholeTable[i] = dutchDataTruthTable[i];
                } else {
                    wholeTable[i] = italianDataTruthTable[i - dutchDataTruthTable.length];
                }
            }
            boolean[] attributes = new boolean[dutchDataTruthTable[0].length - 1];
            Node bestTree = wIns.training(wholeTable, attributes, 0);

            System.out.println();
            System.out.println("Adaboost: ");
            System.out.println("----Find proper stump number----");
            double[] resultWeights = new double[0];
            double highestAcc = 0;
            int bestStumpNum = 0;
            for(int stumpNum = 1; stumpNum < attributes.length + 1; stumpNum++){
                attributes = new boolean[dutchDataTruthTable[0].length - 1];
                double[] weights = wIns.adaBoost(wholeTable, attributes, stumpNum);
                int index = 0;
                for(double weight : weights){
                    System.out.println("a" + index + " weight: " + weight);
                    index++;
                }
                double correctNum = 0, wrongNum = 0;
                for(int columnIndex = 0; columnIndex < wholeTable.length; columnIndex++){
                    boolean curResult = false;
                    double trueSum = 0, falseSum = 0;
                    for(int i = 0; i < weights.length; i++){
                        if(wholeTable[columnIndex][i]){
                            trueSum += 1 * weights[i];
                        } else {
                            falseSum += 1 * weights[i];
                        }
                    }
                    System.out.println("trueSum: " + trueSum + "\tfalseSum: " + falseSum);
                    if(trueSum > falseSum){
                        curResult = true;
                    } else {
                        curResult = false;
                    }
                    if(curResult == wholeTable[columnIndex][attributes.length]){
                        correctNum++;
                    } else {
                        wrongNum++;
                    }
                }
                double correctPer = correctNum / (correctNum + wrongNum);
                System.out.println("stump number: " + stumpNum + " Acc: " + correctPer);
                if(correctPer >= highestAcc){
                    highestAcc = correctPer;
                    bestStumpNum = stumpNum;
                    resultWeights = weights;
                }
            }
            System.out.println("---- Finish ----");
            System.out.println("Highest Acc: " + highestAcc + " Best Stump Number: " + bestStumpNum);
            System.out.println("Final Weights:");
            int index = 0;
            for(double weight : resultWeights){
                System.out.println("a" + index + " weight: " + weight);
                index++;
            }
        } else if(args[0].equals("predict")){
            //mode = 1;
            inputData = wIns.readFile(args[2]);
            boolean[][] inputTruthTable = wIns.calTruthTable(inputData);
            boolean[] attributes = new boolean[inputTruthTable[0].length - 1];
            Node bestTree = wIns.training(inputTruthTable, attributes, 1);
            List<Boolean> resultList = new ArrayList<>();
            for(int i = 0; i < inputTruthTable.length; i++){
                boolean result = wIns.predict(inputTruthTable[i], bestTree);
                resultList.add(result);
            }
            if(args[1].equals("tree")){
                modelType = 0;
                for(int i = 0; i < inputTruthTable.length; i++){
                    if(resultList.get(i)){
                        System.out.println("Segment " + i + ": dutch");
                    } else {
                        System.out.println("Segment " + i + ": italian");
                    }
                }
            } else if(args[1].equals("stumps")){
                modelType = 1;
                attributes = new boolean[inputTruthTable[0].length - 1];
                double[] weights = new double[]{
                        1.0, 2.1151975416349145, 1.5677471079645748, 1.7458501980808627, 1.5899318927965926, 1.0
                };
                for(int columnIndex = 0; columnIndex < inputTruthTable.length; columnIndex++){
                    boolean curResult = false;
                    double trueSum = 0, falseSum = 0;
                    for(int i = 0; i < weights.length; i++){
                        if(inputTruthTable[columnIndex][i]){
                            trueSum += 1 * weights[i];
                        } else {
                            falseSum += 1 * weights[i];
                        }
                    }
                    if(trueSum > falseSum){
                        System.out.println("Segment " + columnIndex + ": dutch");
                    } else {
                        System.out.println("Segment " + columnIndex + ": italian");
                    }
                }
            }
        }
    }
    public double[] adaBoost(boolean[][] wholeTable, boolean[] attributes, int stumpNum){
        double[] weights = new double[wholeTable[0].length - 1];
        for(int i = 0; i < weights.length; i++){
            weights[i] = (double) 1 / wholeTable.length;
            //System.out.println(weights[i]);
        }
        double[] z = new double[attributes.length];
        int[] hAttributeIndex = bestAttributeADA(wholeTable, attributes);

        double x = 0.0000001;
        for(int k = 0; k < stumpNum; k++){
            int aIndex = hAttributeIndex[k];
            double error = 0;
            for(int j = 0; j < wholeTable.length; j++){
                if(wholeTable[j][aIndex] != wholeTable[j][attributes.length]){
                    error += weights[j];
                    //System.out.println(weights[j]);
                }
            }
            if(error > 0.5){
                System.out.println("Error too much!");
                break;
            }
            if(error > 1 - x){
                error = 1 - x;
            }
            for(int j = 0; j < wholeTable.length; j++){
                if(wholeTable[j][aIndex] == wholeTable[j][attributes.length]){
                    //System.out.println("Before: " + weights[j]);
                    weights[j] = weights[j] / 2 * (1 - error);
                    //System.out.println("After: " + weights[j]);
                }
            }
            if(error == 0){
                z[aIndex] = 1;
            } else {
                z[aIndex] = 0.5 * Math.log((1 - error) / error);
            }
            //System.out.println(error + " " + z[aIndex]);
        }

        return z;
    }
    public int[] bestAttributeADA(boolean[][] wholeSet, boolean[] attributes){
        List<Double> attributeGain = new ArrayList<>();
        List<Double> attributeGainTemp = new ArrayList<>();
        List<Integer> resultList = new ArrayList<>();
        for(int i = 0; i < attributes.length; i++){
            double gain = calGain(wholeSet,i);
            attributeGain.add(gain);
            attributeGainTemp.add(gain);
        }
        attributeGainTemp.sort(Comparator.naturalOrder());
        for(int i = 0; i < attributes.length; i++){
            double num = attributeGainTemp.get(i);
            for(int j = 0; j < attributeGainTemp.size(); j++){
                if(attributeGain.get(j) == num && !attributes[j]){
                    attributes[j] = true;
                    resultList.add(j);
                    //System.out.println(i + ": " + j + " " + num);
                    break;
                }
            }
        }
        System.out.println();
        int[] resultArray = new int[resultList.size()];
        for(int i = 0; i < resultArray.length; i++){
            resultArray[i] = resultList.get(resultArray.length - 1 - i);
            //System.out.print(resultArray[i]  + " ");
        }
        //System.out.println();
        return resultArray;
    }

    public Node training(boolean[][] wholeTable, boolean[] attributes, int mode){
        Node bestTree = null;
        double highestCorrect = 0;
        int bestDepth = 0;
        for(int i = 1; i < attributes.length + 1; i++){
            boolean[] tempAttributes = new boolean[attributes.length];
            for(int j = 0; j < attributes.length; j++){
                tempAttributes[j] = attributes[j];
            }
            Node tree = buildDecisionTree(wholeTable, tempAttributes, i, 0);
            double correctNum = 0, wrongNum = 0;
            for(int j = 0; j < wholeTable.length; j++){
                if(predict(wholeTable[j], tree) == wholeTable[j][attributes.length - 1]){
                    correctNum++;
                } else {
                    wrongNum++;
                }
            }
            double correctPercent = correctNum / (correctNum + wrongNum);
            if(mode == 0){
                System.out.println(i + ": " + correctPercent);
            }
            if(correctPercent >= highestCorrect){
                highestCorrect = correctPercent;
                bestTree = tree;
                bestDepth = i;
            }
        }
        if(mode == 0){
            System.out.println("Best Depth: " + bestDepth);
            System.out.println("----Finish Decision Tree Training----");
        }
        return bestTree;
    }
    public boolean[][] calTruthTable(List<String[]> data){
        double[] dutchFrequency = new double[]{
                0.0776, 0.0131, 0.1931, 0.0503, 0.1004, 0.0588
        };
        double[] italianFrequency = new double[]{
                0.1085, 0.043, 0.1149, 0.1018, 0.0702, 0.0997
        };
        boolean[][] result = new boolean[data.size()][dutchFrequency.length + 1];
        int lineIndex = 0;
        for(String[] line : data){
            int[] charaCount = new int[26];
            double[] charaFrePercentage = new double[26];
            List<Double> chosenFre = new ArrayList<>();
            int totalChara = 0;
            for(int i = 0; i < 26; i++){
                charaCount[i] = 0;
            }
            for(int i = 0; i < line.length; i++){
                String lineTemp = line[i].toLowerCase();
                for(int j = 0; j < lineTemp.length(); j++){
                    for(int k = 0; k < 26; k++){
                        if(lineTemp.charAt(j) == charcterTable[k]){
                            totalChara++;
                            charaCount[k]++;
                        }
                    }
                }
            }
            for(int i = 0; i < 26; i++){
                charaFrePercentage[i] = (double)charaCount[i] / (double)totalChara;
                if(i == 0 || i == 2 || i == 4 || i == 8 || i == 13 || i == 14){
                    //System.out.println(i + ": " + charcterTable[i] + ": " + charaCount[i] + " " + charaFrePercentage[i]);
                    chosenFre.add(charaFrePercentage[i]);
                    //System.out.println(charcterTable[i] + ": " + charaFrePercentage[i]);
                }
            }
            boolean[] curLineResult = new boolean[chosenFre.size() + 1];
            for(int i = 0; i < chosenFre.size(); i++){
                double diffDutchFre = Math.abs(chosenFre.get(i) - dutchFrequency[i]);
                double diffItalianFre = Math.abs(chosenFre.get(i) - italianFrequency[i]);
                if(diffDutchFre > diffItalianFre){
                    curLineResult[i] = false;
                } else {
                    curLineResult[i] = true;
                }
            }
            result[lineIndex] = curLineResult;
            lineIndex++;
        }
/*
        for(boolean[] lineResult : result){
            for(boolean value : lineResult){
                System.out.print(value + " ");
            }
            System.out.println();
        }
        System.out.println();*/
        return result;
    }
    public boolean predict(boolean[] inputTrueTable, Node decisionTree){
        Node curNode = decisionTree;
        while(true){
            boolean value = inputTrueTable[curNode.bestAttribute];
            //System.out.println(value + " " + curNode.bestAttribute);
            if(value){
                //System.out.println("!");
                curNode = curNode.trueChild;
            } else {
                //System.out.println("!!");
                curNode = curNode.falseChild;
            }
            //System.out.println(curNode.trueChild + " " + curNode.falseChild);
            if(curNode == null){
                return value;
            }
            if((curNode.trueChild == null && curNode.falseChild == null)){
                if(curNode.bestAttribute == -1){
                    return curNode.value;
                } else {
                    return inputTrueTable[curNode.bestAttribute];
                }
            }
        }
    }

    public Node buildDecisionTree(boolean[][] wholeTable, boolean[] attributes, int maxDepth, int curDepth){
        //System.out.println("CurDepth: " + curDepth);
        boolean lastValue = wholeTable[0][0];
        int sameFlag = 1;
        for(boolean[] table : wholeTable){
            for (boolean v : table){
                if(v != lastValue){
                    sameFlag = 0;
                    break;
                }
            }
            if(sameFlag == 0){
                break;
            }
        }
        if(sameFlag == 1){
            //System.out.println("same!!!");
            Node result = new Node();
            result.value = lastValue;
            return result;
        }
        if(attributes.length == 0){
            //System.out.println("length == 0!!!");
            int trueNum = 0, falseNum = 0;
            for(int i = 0; i < wholeTable.length; i++){
                if(wholeTable[i][attributes.length]){
                    trueNum++;
                } else {
                    falseNum++;
                }
            }
            Node result = new Node();
            if(trueNum > falseNum){
                result.value = true;
            } else {
                result.value = false;
            }
            return result;
        }
        int bestAttribute = bestAttribute(wholeTable, attributes);
        Node rootNode = new Node();
        rootNode.bestAttribute = bestAttribute;
        attributes[bestAttribute] = true;
        //System.out.println("bestAttribute: " + bestAttribute);
        //true answer
        if(curDepth + 1 < maxDepth){
            //System.out.println("true Sub");
            int subTrueNum = 0;
            for(int i = 0; i < wholeTable.length; i++){
                if(wholeTable[i][bestAttribute]){
                    subTrueNum++;
                }
            }
            boolean[][] newSet = new boolean[subTrueNum][];
            int count = 0;
            for(int i = 0; i < wholeTable.length; i++){
                if(wholeTable[i][bestAttribute]){
                    newSet[count] = wholeTable[i];
                    count++;
                }
            }
            boolean[] newAttributes = new boolean[attributes.length];
            for(int i = 0; i < newAttributes.length; i++){
                newAttributes[i] = attributes[i];
            }
            Node subtree = new Node(bestAttribute);
            //System.out.print(subtree.bestAttribute + " ");
            if(newSet.length > 0){
                subtree = buildDecisionTree(newSet, newAttributes, maxDepth, curDepth + 1);
            }
            //System.out.println(subtree.bestAttribute);
            rootNode.trueChild = subtree;
            subtree.parent = rootNode;

            //false answer
            //System.out.println("false Sub");
            int subFalseNum = 0;
            for(int i = 0; i < wholeTable.length; i++){
                if(!wholeTable[i][bestAttribute]){
                    subFalseNum++;
                }
            }
            newSet = new boolean[subFalseNum][];
            count = 0;
            for(int i = 0; i < wholeTable.length; i++){
                if(!wholeTable[i][bestAttribute]){
                    newSet[count] = wholeTable[i];
                    count++;
                }
            }
            newAttributes = new boolean[attributes.length];
            for(int i = 0; i < newAttributes.length; i++){
                newAttributes[i] = attributes[i];
            }
            subtree = new Node(bestAttribute);
            if(newSet.length > 0){
                subtree = buildDecisionTree(newSet, newAttributes, maxDepth, curDepth + 1);
            }
            rootNode.falseChild = subtree;
            subtree.parent = rootNode;
        }
        return rootNode;
    }
    public int bestAttribute(boolean[][] wholeSet, boolean[] attributes){
        double bestGain = -1;
        int bestAttribute = -1;
        for(int i = 0; i < attributes.length; i++){
            if(!attributes[i]){
                double gain = calGain(wholeSet,i);
                if(gain > bestGain){
                    bestGain = gain;
                    bestAttribute = i;
                }
            }
        }
        return bestAttribute;
    }
    public double calGain(boolean[][] wholeSet, int columnIndex){
        double result = 0;
        int wholeSetTrueNum = 0, wholeSetFalseNum = 0;
        int totalNum = wholeSet.length;
        int trueSubTrueNum = 0, trueSubFalseNum = 0, falseSubTrueNum = 0, falseSubFalseNum = 0;
        int trueSubNum = 0, falseSubNum = 0;
        for(int i = 0; i < wholeSet.length; i++){
            if(wholeSet[i][wholeSet[i].length - 1]){
                wholeSetTrueNum++;
            } else {
                wholeSetFalseNum++;
            }
            if(wholeSet[i][columnIndex]){
                trueSubNum++;
                if(wholeSet[i][wholeSet[i].length - 1]){
                    trueSubTrueNum++;
                } else {
                    trueSubFalseNum++;
                }
            } else {
                falseSubNum++;
                if(wholeSet[i][wholeSet[i].length - 1]){
                    falseSubTrueNum++;
                } else {
                    falseSubFalseNum++;
                }
            }
        }/*
        System.out.println(wholeSetTrueNum + " " + wholeSetFalseNum);
        System.out.println(trueSubTrueNum + " " + trueSubFalseNum);
        System.out.println(falseSubTrueNum + " " + falseSubFalseNum);*/
        double wholeSetEntropy = calE(wholeSetTrueNum, wholeSetFalseNum);
        double trueSubEntropy = calE(trueSubTrueNum, trueSubFalseNum);
        double falseSubEntropy = calE(falseSubTrueNum, falseSubFalseNum);
        //System.out.println(wholeSetEntropy + " " + trueSubEntropy + " " + falseSubEntropy);
        result = wholeSetEntropy - (((double)trueSubNum / totalNum) * trueSubEntropy +
                ((double)falseSubNum / totalNum) * falseSubEntropy);
        //System.out.println(result);
        return result;
    }
    public double calE(int trueNum, int falseNum){
        if(trueNum == 0 || falseNum == 0){
            return 0;
        }
        double result = 0;
        int totalNum = trueNum + falseNum;
        double Pplus = (double)trueNum / totalNum;
        double Pminus = 1 - Pplus;
        result = -Pplus * log2(Pplus) - Pminus * log2(Pminus);
        return result;
    }

    public double log2(double value){
        return Math.log(value) / Math.log(2);
    }

    public List<String[]> readFile(String fileName){
        File file = new File(fileName);
        BufferedReader reader = null;
        List<String[]> inputData = new ArrayList<>();
        try {
            reader = new BufferedReader(new FileReader(file));
            String tempString = null;
            int line = 0;
            while ((tempString = reader.readLine()) != null) {
                String[] data = tempString.split(" ");
                inputData.add(data);
                line++;
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e1) {
                }
            }
        }
        return inputData;
    }
}
