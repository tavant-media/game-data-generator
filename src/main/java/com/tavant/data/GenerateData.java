package com.tavant.data;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

import org.apache.poi.hpsf.Array;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class GenerateData {

    Map<Integer, String[]> gamesmap;

    Map<Integer, String[]> playersmap;

    Map<Integer, String[]> devicemap;

    Map<Integer, String[]> osmap;

    Map<Integer, String[]> itemsmap;

    Map<Integer, String[]> locationmap;

    Map<String, List> playerInfo;

    public static void main(String[] args) throws IOException {

        new GenerateData().prepareDimentionsData();

//        Integer i = new Double("1.0").intValue();
    }

    private void prepareDimentionsData() throws IOException {
        File file = new File("out-file.txt");
        PrintStream stream = new PrintStream(file);
        System.setOut(stream);
        //obtaining input bytes from a file
        FileInputStream fis = new FileInputStream(new File("C:\\work\\gaming\\sega\\Game Data.xlsx"));
//creating workbook instance that refers to .xls file
        XSSFWorkbook wb = new XSSFWorkbook(fis);
        FormulaEvaluator formulaEvaluator = wb.getCreationHelper().createFormulaEvaluator();
//creating a Sheet object to retrieve the object
        XSSFSheet gamesheet = wb.getSheetAt(0);
        gamesmap = readsheet(gamesheet, formulaEvaluator);

        XSSFSheet playersheet = wb.getSheetAt(1);
        playersmap = readsheet(playersheet, formulaEvaluator);

        XSSFSheet devicesheet = wb.getSheetAt(2);
        devicemap = readsheet(devicesheet, formulaEvaluator);

        XSSFSheet ossheet = wb.getSheetAt(3);
        osmap = readsheet(ossheet, formulaEvaluator);

        XSSFSheet itemssheet = wb.getSheetAt(4);
        itemsmap = readsheet(itemssheet, formulaEvaluator);

        XSSFSheet locationsheet = wb.getSheetAt(5);
        locationmap = readsheet(locationsheet, formulaEvaluator);


        preparePlayerData();

        createSessionData();

//        createIAPData();

    }

    private void preparePlayerData() {

        playerInfo = new HashMap<String, List>();

        for (String[] player : playersmap.values()) {
            String playerId = player[0];
            ArrayList<String[]> list = new ArrayList<String[]>();

            Integer numOfOS = getRandomDoubleBetweenRange(1,3);
            String[] osarray = new String[numOfOS];
            for(int i=0;i < numOfOS;i++){
                osarray [i] = osmap.get(getRandomDoubleBetweenRange(0, osmap.size()-1))[0];
            }
            list.add(osarray);

            Integer numOfGames = getRandomDoubleBetweenRange(1,4);
            String[] gamesarray = new String[numOfGames];
            for(int i=0;i < numOfGames;i++){
                gamesarray [i] = gamesmap.get(getRandomDoubleBetweenRange(0,gamesmap.size()-1))[0];
            }
            list.add(gamesarray);

            Integer numOfLocations = getRandomDoubleBetweenRange(1,20);
            String[] locationarray = new String[numOfLocations];
            for(int i=0;i < numOfLocations;i++){
                locationarray [i] = locationmap.get(getRandomDoubleBetweenRange(0,locationmap.size()-1))[0];
            }
            list.add(locationarray);

            playerInfo.put(playerId, list);
        }

    }

    private void createIAPData() {

        Integer numOfRows = 0;

        //Create 10 player buckets
        List<String> randomPlayers = new ArrayList<String>();
        for (String playerId : playerInfo.keySet()) {

            randomPlayers.add(playerId);
        }
        Collections.shuffle(randomPlayers);

        //Prepare paying users collection
        List<String> payingUsers = new ArrayList<String>();
        for (int i = 0; i < (randomPlayers.size() * 0.1); i ++) {
            payingUsers.add(randomPlayers.get(i));
        }

        List<String> IAPBucket = new ArrayList<>();
//        IAPBucket.add("1");

        int payingPlayerCount = payingUsers.size();
        List<Integer> payingPattern = new ArrayList<Integer>();
        payingPattern.add(payingPlayerCount/17); IAPBucket.add("1");
        payingPattern.add(payingPlayerCount/8); IAPBucket.add("5");
        payingPattern.add(payingPlayerCount/5); IAPBucket.add("10");
        payingPattern.add(payingPlayerCount/3); IAPBucket.add("25");
        payingPattern.add(payingPlayerCount/5); IAPBucket.add("50");
        payingPattern.add(payingPlayerCount/12); IAPBucket.add("75");
        payingPattern.add(payingPlayerCount/23); IAPBucket.add("100");

        int loop = 0;
        for (Integer i:payingPattern){
            for (int j=0; j<i;j++){
                String playerId = payingUsers.get(j);
                List<String[]> list = playerInfo.get(playerId);
                String[] osarray = list.get(0);
                String[] gamearray = list.get(1);

                LocalDate startDate = LocalDate.of(2020, 1, 1); //start date
                long start = startDate.toEpochDay();
                LocalDate endDate = LocalDate.now(); //end date
                long end = endDate.toEpochDay();
                LocalDate dateIAP = LocalDate.ofEpochDay((ThreadLocalRandom.current().nextLong(start, end)));

                Integer gamesPlayedToday = getRandomDoubleBetweenRange(0,gamearray.length-1);
                String gameIdIAP = gamearray[gamesPlayedToday];

//                Integer IAPAmount = getRandomDoubleBetweenRange(Double.valueOf(IAPBucket.get(loop)),Double.valueOf(IAPBucket.get(loop+1)));
                Integer IAPAmount = Integer.valueOf(IAPBucket.get(loop));

                List<Integer[]> gameIAPLIst = getIAPList(gameIdIAP);

                Integer[][] gameList2Array = new Integer[gameIAPLIst.size()][2];//Initialize array conversion

                List<List<Integer[]>> iapComboList = combinationSum(gameIAPLIst.toArray(gameList2Array),IAPAmount);

                List<Integer[]> finalIAPList = iapComboList.get(getRandomDoubleBetweenRange(0,iapComboList.size()-1));

                for (Integer[] APIs : finalIAPList){

                    System.out.println(numOfRows++ + "\t" + playerId + "\t" + gameIdIAP + "\t" + dateIAP + "\t" + APIs[0] + "\t" + APIs[1]);
                }
//                System.out.println(numOfRows++ + "\t" + playerId + "\t" + gameIdIAP + "\t" + dateIAP + "\t" + IAPAmount);

            }
            loop++;
        }

    }

    private List<Integer[]> getIAPList(String gameId){
        List<Integer[]> results = new ArrayList<Integer[]>();

        for (String[] row : itemsmap.values()){
            if(new Double(row[2]).intValue() == new Double(gameId).intValue()) {
                Integer[] iap = {new Double(row[0]).intValue(),new Double(row[3]).intValue()};
                results.add(iap);
            }
        }
        return results;
    }

    private void createSessionData() {
        Integer numOfRows = 0;
        //Creating Session Data
        for (String playerId : playerInfo.keySet()){
            List<String[]> list = playerInfo.get(playerId);
            String[] osarray = list.get(0);
            String[] gamearray = list.get(1);
            String[] locationarray = list.get(2);

            LocalDate startDate = LocalDate.of(2020, 1, 1); //start date
            long start = startDate.toEpochDay();
            LocalDate endDate = LocalDate.now(); //end date
            long end = endDate.toEpochDay();

            long random1 = ThreadLocalRandom.current().nextLong(start, end);

            long random2 = ThreadLocalRandom.current().nextLong(start, end);

            LocalDate dateStart, dateEnd;
            if (random2 > random1) {
                dateStart = LocalDate.ofEpochDay(random1);
                dateEnd = LocalDate.ofEpochDay(random2);
            } else {
                dateStart = LocalDate.ofEpochDay(random2);
                dateEnd = LocalDate.ofEpochDay(random1);
            }

            Map<Integer, Integer> firstSession = new HashMap<Integer, Integer>();
            for (LocalDate date = dateStart; date.isBefore(dateEnd); date = date.plusDays(1)) {
                Integer NoOfGamesPlayedToday = getRandomDoubleBetweenRange(0,gamearray.length-1);

                for (int i = 0; i < NoOfGamesPlayedToday; i++){
                    Integer gamesPlayedToday = getRandomDoubleBetweenRange(0,gamearray.length-1);
                    String gameId = gamearray[gamesPlayedToday];

                    Integer OSToday = getRandomDoubleBetweenRange(0,osarray.length-1);
                    String osId = osarray[OSToday];

                    Integer locationToday = getRandomDoubleBetweenRange(0,locationarray.length-1);
                    String locationId = locationarray[locationToday];

                    int gameIdInt = new Double(gameId).intValue();
                    if(!firstSession.containsKey(gameIdInt)){
                        firstSession.put(gameIdInt, 1);
                    }

                    System.out.println(numOfRows++ + "\t" + playerId + "\t" + gameId + "\t" + osId + "\t" + date + "\t" +
                            locationId + "\t" + getRandomDoubleBetweenRange(0,75) + "\t" + firstSession.get(gameIdInt));
                    firstSession.put(gameIdInt, 0);
                }
            }

        }


    }

    private Map readsheet(Sheet sheet, FormulaEvaluator formulaEvaluator) {
        Map map = new HashMap<Integer, String[]>();
        for (Row row : sheet)     //iteration over row using for each loop
        {
            String[] rowStr = new String[row.getLastCellNum()];
            for (Cell cell : row)    //iteration over cell using for each loop
            {
//                String val = (String) cell.getStringCellValue();

                switch (formulaEvaluator.evaluateInCell(cell).getCellType()) {
                    case _NONE:
                        rowStr[cell.getColumnIndex()] = "";
                        break;
                    case NUMERIC:
                        rowStr[cell.getColumnIndex()] = Double.toString(cell.getNumericCellValue());
                        break;
                    case STRING:
                        rowStr[cell.getColumnIndex()] = cell.getStringCellValue();
                        break;
                    }
                }
            map.put(row.getRowNum(), rowStr);
        }
        //System.out.println(map.toString());
        return map;
    }

    public static Integer getRandomDoubleBetweenRange(double min, double max){
        double x = (Math.random()*((max-min)+1))+min;
        return (int)x;

    }



    public List<List<Integer[]>> combinationSum(Integer[][] candidates, int target) {
        List<List<Integer[]>> result = new ArrayList<>();
        List<Integer[]> temp = new ArrayList<>();
        helper(candidates, 0, target, 0, temp, result);
//        System.out.println(result);
        return result;
    }

    private void helper(Integer[][] candidates, int start, int target, int sum,
                        List<Integer[]> list, List<List<Integer[]>> result){
        if(sum>target){
            return;
        }

        if(sum==target){
            result.add(new ArrayList<Integer[]>(list));
            return;
        }

        for(int i=start; i<candidates.length; i++){
            list.add(candidates[i]);
            helper(candidates, i, target, sum+candidates[i][1], list, result);
            list.remove(list.size()-1);
        }
    }

    private void sample() throws IOException {
        //obtaining input bytes from a file
        FileInputStream fis = new FileInputStream(new File("C:\\work\\gaming\\sega\\Game Data.xlsx"));
//creating workbook instance that refers to .xls file
        XSSFWorkbook wb = new XSSFWorkbook(fis);
//creating a Sheet object to retrieve the object
//        XSSFSheet sheet = wb.getSheetAt(0);
        for (Sheet sheet : wb)     //iteration over row using for each loop
        {
//evaluating cell type
            System.out.println("\n\nSheet - " + sheet.getSheetName() );
            FormulaEvaluator formulaEvaluator = wb.getCreationHelper().createFormulaEvaluator();
            for (Row row : sheet)     //iteration over row using for each loop
            {
                for (Cell cell : row)    //iteration over cell using for each loop
                {
                    switch (formulaEvaluator.evaluateInCell(cell).getCellType()) {
                        case _NONE:
                            break;
                        case NUMERIC:   //field that represents numeric cell type
//getting the value of the cell as a number
                            System.out.print(cell.getNumericCellValue() + "\t\t");
                            break;
                        case STRING:    //field that represents string cell type
//getting the value of the cell as a string
                            System.out.print(cell.getStringCellValue() + "\t\t");
                            break;
//                    default:
//                        break;
//                        case FORMULA:
//                            System.out.print(cell.getStringCellValue() + "\t\t");
//                            break;
//                        case BLANK:
//                            System.out.print(cell.getStringCellValue() + "\t\t");
//                            break;
//                        case BOOLEAN:
//                            System.out.print(cell.getStringCellValue() + "\t\t");
//                            break;
//                        case ERROR:
//                            break;
                    }
                }
                System.out.println();
            }
        }

    }

}
