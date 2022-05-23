package com.zokudo.sor.util;


import com.zokudo.sor.enums.BizErrors;
import com.zokudo.sor.enums.Quater;
import com.zokudo.sor.exceptions.BizException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.xssf.usermodel.*;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.http.MediaType;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.bind.DatatypeConverter;
import javax.xml.soap.*;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Slf4j
public class CommonUtil {

    public static SimpleDateFormat dateFormate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    public static SimpleDateFormat dateFormate2 = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
    public static SimpleDateFormat dateFormateDdMmYyyy = new SimpleDateFormat("dd-MM-yyyy");
    public static SimpleDateFormat dateFormate_yyyy_MM_dd = new SimpleDateFormat("yyyy-MM-dd");
    public static SimpleDateFormat dateFormateDdMmYyyyHhMmSs = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
    public static SimpleDateFormat dateFormate3 = new SimpleDateFormat("dd/MM/yyyy");

    public static final String startTime = " 00:00:00";
    public static final String endTime = " 23:59:59";

    public static final String FIRST_QUATER_START_TIME = " 00:00:00";
    public static final String FIRST_QUATER_END_TIME = " 05:59:59";
    public static final String SECOND_QUATER_START_TIME = " 06:00:00";
    public static final String SECOND_QUATER_END_TIME = " 11:59:59";
    public static final String THIRD_QUATER_START_TIME = " 12:00:00";
    public static final String THIRD_QUATER_END_TIME = " 17:59:59";
    public static final String FOURTH_QUATER_START_TIME = " 18:00:00";
    public static final String FOURTH_QUATER_END_TIME = " 23:59:59";


    public static String[] getProgramAndRequestUrl(HttpServletRequest request) {
        return request.getRequestURI().split("/");
    }

    public static String getBasicAuthorization(String applicationLevelUserName, String applicationLevelUserPassword) {
        String result = "Basic ";
        String credentials = applicationLevelUserName + ":" + applicationLevelUserPassword;
        result = result + DatatypeConverter.printBase64Binary(credentials.getBytes(StandardCharsets.UTF_8));
        return result;
    }

    public static String getString(String description) {
        if (description != null) {
            return description.replaceAll("\\W", " ");
        }
        return null;
    }

    public static boolean isValidMobileNumber(String str) {
        try {
            return str.trim().matches("^(?=(?:[6-9]){1})(?=[0-9]{10}).*");
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean isValidTitle(String str) {
        try {
            switch (str) {
                case "Mr":
                case "Ms":
                case "Mrs":
                    return true;
            }
        } catch (Exception e) {
            throw new BizException(BizErrors.NULL_ERROR.getValue(), "title should not be empty");
        }
        return false;
    }

    public static boolean isValidGender(String str) {
        try {
            switch (str) {
                case "M":
                case "F":
                    return true;
            }
        } catch (Exception e) {
            throw new BizException(BizErrors.NULL_ERROR.getValue(), "Gender should not be empty");
        }
        return false;
    }

    // date format should be like YYYY-MM-DD
    /*public static boolean dobValidate(String date) {
        try {
            Constants.dateFormat.parse(date);
        } catch (ParseException e) {
            return false;
        }
        return true;
    }*/

    public static boolean validIdType(String idType) {
        switch (idType) {
            case "aadhaar":
            case "pan":
            case "driver_id":
                return true;
        }
        return false;
    }


    public static boolean validCountry(String countryOfIssue) {

        return "India".equals(countryOfIssue);
    }

    public static boolean isValidId(final double id) {
        return id > 0;
    }


    private static Pageable getPageable(final int pageNo, final int pageSize, final String direction, final String property) {
        final int pageSizeLocal = pageSize != 0 ? pageSize : 20;
        final Direction directionLocal = StringUtils.isNotEmpty(direction) ? Direction.valueOf(direction) : Direction.DESC;
        final String propertyLocal = StringUtils.isNotEmpty(property) ? property : "createdAt";
        return PageRequest.of(pageNo, pageSizeLocal, directionLocal, propertyLocal);
    }

    public static boolean containsScriptElements(String str) {
        if (str != null) {
            str = str.trim();
            if (str.contains("<") || str.contains(">") || str.contains("script")) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    //Validation for Sql Injection
    public static boolean containsSqlKeywords(String str) {
        String[] sqlKeywords = {"select", "drop", "insert", "delete", "table", "merge", "union", "all", "dual", "group by", "having", "insert", "update", "join", "left join", "right join", "inner join"};
        String[] inputWords = str.split(" ");
        for (String inputWord : inputWords) {
            for (String sqlKeyword : sqlKeywords) {
                if (inputWord.equalsIgnoreCase(sqlKeyword)) {
                    return true;
                }
            }
        }
        return false;
    }

    public static SOAPConnection getNewConnection() {
        SOAPConnection soapConnection = null;
        try {
            SOAPConnectionFactory soapConnectionFactory = SOAPConnectionFactory.newInstance();
            soapConnection = soapConnectionFactory.createConnection();
        } catch (SOAPException e) {
            log.error("Exception occurred while setting the new soap connection");
            log.error(e.getMessage(), e);
        }
        return soapConnection;
    }

    public static SOAPMessage createSOAPMessage() {
        SOAPMessage soapMessage = null;
        try {
            MessageFactory messageFactory = MessageFactory.newInstance();
            soapMessage = messageFactory.createMessage();

        } catch (SOAPException e) {
            log.error("Exception occurred while creating the SOAP Message");
            log.error(e.getMessage(), e);
        }
        return soapMessage;
    }

    public static void generateExcelSheet(final List<String> headers, final List<Map<String, String>> dataList, String resultantFileName, HttpServletResponse response) {
        try {
            if (headers == null || headers.size() == 0) {
                return;
            }
            if (dataList == null || dataList.size() == 0) {
                return;
            }
            final XSSFWorkbook workbook = new XSSFWorkbook();
            XSSFCellStyle cellStyle = workbook.createCellStyle();
            XSSFFont font = workbook.createFont();
            font.setBold(true);
            cellStyle.setFont(font);

            final XSSFSheet sheet = workbook.createSheet();
            XSSFRow xssfRow = sheet.createRow(0);
            for (int i = 0; i < headers.size(); i++) {
                XSSFCell xssfCell = xssfRow.createCell(i);
                xssfCell.setCellStyle(cellStyle);
                xssfCell.setCellValue(headers.get(i));
            }
            for (int j = 0; j < dataList.size(); j++) {
                XSSFRow row = sheet.createRow((j + 1));
                Map<String, String> eachDataMap = dataList.get(j);
                for (int k = 0; k < headers.size(); k++) {
                    XSSFCell cell = row.createCell(k);
                    String key = headers.get(k);
                    String value = eachDataMap.get(key);
                    cell.setCellValue(value);
                }
            }
            response.setHeader("Content-Type", MediaType.APPLICATION_OCTET_STREAM_VALUE);
            response.setHeader("Content-Disposition", "attachment; filename=" + resultantFileName);

            final ServletOutputStream outputStream = response.getOutputStream();
            workbook.write(outputStream);
            workbook.close();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return;
        }
    }

    public static InputStream generateExcelReport(final List<String> headers, final List<Map<String, String>> dataList, String resultantFileName, HttpServletResponse response) {
        try {
            if (headers == null || headers.size() == 0) {
                throw new BizException(BizErrors.APPLICATION_ERROR.getValue(), "Error while generating EOD report, headers can not be null!");
            }
            if (dataList == null || dataList.size() == 0) {
                throw new BizException(BizErrors.APPLICATION_ERROR.getValue(), "Error while generating EOD report, file upload operation failed!");
            }
            final XSSFWorkbook workbook = new XSSFWorkbook();
            XSSFCellStyle cellStyle = workbook.createCellStyle();
            XSSFFont font = workbook.createFont();
            font.setBold(true);
            cellStyle.setFont(font);

            final XSSFSheet sheet = workbook.createSheet();
            XSSFRow xssfRow = sheet.createRow(0);
            for (int i = 0; i < headers.size(); i++) {
                XSSFCell xssfCell = xssfRow.createCell(i);
                xssfCell.setCellStyle(cellStyle);
                xssfCell.setCellValue(headers.get(i));
            }
            for (int j = 0; j < dataList.size(); j++) {
                XSSFRow row = sheet.createRow((j + 1));
                Map<String, String> eachDataMap = dataList.get(j);
                for (int k = 0; k < headers.size(); k++) {
                    XSSFCell cell = row.createCell(k);
                    String key = headers.get(k);
                    String value = eachDataMap.get(key);
                    cell.setCellValue(value);
                }
            }
            /*FileOutputStream fop = null;
            File file;
            try {
                file = new File(resultantFileName);
                fop = new FileOutputStream(file);

                // get the content in bytes
                byte[] contentInBytes = content.getBytes();
            }catch (IOException e) {
                e.printStackTrace();
            }*/

            //response.setHeader("Content-Type", MediaType.APPLICATION_OCTET_STREAM_VALUE);
            //response.setHeader("Content-Disposition", "attachment; filename=" + resultantFileName);


            //final ServletOutputStream outputStream = response.getOutputStream();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            //FileInputStream inputStream = new FileInputStream(
            //       new File(resultantFileName+"-.xlsx"));

            workbook.write(baos);
            workbook.close();

            // Create the input stream (do not forget to close the inputStream after use)
            InputStream inStream = new ByteArrayInputStream(baos.toByteArray());

            return inStream;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new BizException(BizErrors.APPLICATION_ERROR.getValue(), "Error while generating EOD report!");
        }
    }

    /**
     * @param dateStr
     * @return return the date object
     * @throws ParseException
     */
    public static String convertDateForPrefund(final String dateStr) throws ParseException {
        log.info("Convert Date from str :{}", dateStr);
        String date = dateFormate.format(dateFormateDdMmYyyyHhMmSs.parse(dateStr));
        log.info("Converted format :{}", date);
        return date;
    }

    public static String generateFileKey(String id) {
        try {
            String systemMillis = String.valueOf(System.currentTimeMillis());
            return id + "_" + systemMillis;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Date convertDateInSQLDateFormate(final String dateStr) throws ParseException {
        log.info("Input dateStr : {}", dateStr);
        Date date = dateFormate2.parse(dateStr);
        log.info("Converted formate : {}", date);
        return date;
    }

    public static Date getPastDateFromCurrentDate() throws ParseException {
        log.info("Inside Get Past Date from Current Date");
        String pastDateStr = String.valueOf(LocalDate.now().minusDays(1));
//        String pastDateStr = "2021-04-05";
        log.info("PastDateStr :{}", pastDateStr);
        String pastEndDateStr = pastDateStr.concat(endTime);
        log.info("PastEndDateStr :{}", pastEndDateStr);
        return dateFormate.parse(pastEndDateStr);
    }

    /**
     * This method gives quater start date-time based on quater enum in String.
     * For Example : If its FIRST_QUATER of 29/04/2021 then it must return 29/04/2021 00:00:00 "
     *
     * @param quarter
     * @return
     * @throws ParseException
     */
    public static String getQuarterStartTime(Quater quarter, Date previousDate) throws ParseException {
        log.info("** Previous Date params {}", previousDate);
        String parsedPreviousDate = CommonUtil.dateFormate_yyyy_MM_dd.format(previousDate);
        log.info("** Date after formatting {} ", parsedPreviousDate);
        String quarterDateTime;
        if (quarter == Quater.FIRST_QUARTER) {
            quarterDateTime = CommonUtil.dateFormate.format(CommonUtil.dateFormate.parse(parsedPreviousDate + CommonUtil.FIRST_QUATER_START_TIME));
            return quarterDateTime;
        }
        if (quarter == Quater.SECOND_QUARTER) {
            quarterDateTime = CommonUtil.dateFormate.format(CommonUtil.dateFormate.parse(parsedPreviousDate + CommonUtil.SECOND_QUATER_START_TIME));
            return quarterDateTime;
        }
        if (quarter == Quater.THIRD_QUARTER) {
            quarterDateTime = CommonUtil.dateFormate.format(CommonUtil.dateFormate.parse(parsedPreviousDate + CommonUtil.THIRD_QUATER_START_TIME));
            return quarterDateTime;
        }
        if (quarter == Quater.FOURTH_QUARTER) {
            quarterDateTime = CommonUtil.dateFormate.format(CommonUtil.dateFormate.parse(parsedPreviousDate + CommonUtil.FOURTH_QUATER_START_TIME));
            return quarterDateTime;
        }
        throw new BizException("Invalid Quater Input");
    }


    /**
     * This method gives quater end date-time based on quater enum in String.
     * For Example : If its FIRST_QUATER of 29/04/2021 then it must return 29/04/2021 05:59:59 "
     *
     * @param quarter
     * @return
     * @throws ParseException
     */
    public static String getQuarterEndTime(Quater quarter, Date previousDate) throws ParseException {
        String parsedPreviousDate = CommonUtil.dateFormate_yyyy_MM_dd.format(previousDate);
        String quarterDateTime;
        if (quarter == Quater.FIRST_QUARTER) {
            quarterDateTime = CommonUtil.dateFormate.format(CommonUtil.dateFormate.parse(parsedPreviousDate + CommonUtil.FIRST_QUATER_END_TIME));
            return quarterDateTime;
        }
        if (quarter == Quater.SECOND_QUARTER) {
            quarterDateTime = CommonUtil.dateFormate.format(CommonUtil.dateFormate.parse(parsedPreviousDate + CommonUtil.SECOND_QUATER_END_TIME));
            return quarterDateTime;
        }
        if (quarter == Quater.THIRD_QUARTER) {
            quarterDateTime = CommonUtil.dateFormate.format(CommonUtil.dateFormate.parse(parsedPreviousDate + CommonUtil.THIRD_QUATER_END_TIME));
            return quarterDateTime;
        }
        if (quarter == Quater.FOURTH_QUARTER) {
            quarterDateTime = CommonUtil.dateFormate.format(CommonUtil.dateFormate.parse(parsedPreviousDate + CommonUtil.FOURTH_QUATER_END_TIME));
            return quarterDateTime;
        }
        throw new BizException("Invalid Quater Input");
    }

    public static void main(String[] args) {
       /* Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DATE, cal.get(Calendar.DATE) - 1);
        Date fromDate = cal.getTime();
        Date toDate = new Date();
        log.info("FromDate : {}, ToDate :{}", fromDate, toDate);*/

        Calendar now = Calendar.getInstance();
        now.set(Calendar.DATE, now.get(Calendar.DATE) + 5);
        log.info("Year :{}, Month: {}, Date :{}", now.get(Calendar.YEAR), now.get(Calendar.MONTH) + 1, now.getTime());
    }
    
    public static Date addDays(Date date, int days)
    {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.add(Calendar.DATE, days); //minus number would decrement the days
        return cal.getTime();
    }
}