package com.whizupp.jpims.util;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import java.io.ByteArrayOutputStream;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

public class ReportGenerator {

    public static byte[] generatePdf(String title, Map<String, Object> summary, List<Map<String, Object>> data,
            String from, String to, String generatedBy) {
        try {
            Document document = new Document(PageSize.A4, 36, 36, 36, 36);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            PdfWriter.getInstance(document, out);
            document.open();

            // Font Settings
            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, BaseColor.DARK_GRAY);
            Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, BaseColor.WHITE);
            Font boldFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, BaseColor.BLACK);
            Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, 9, BaseColor.BLACK);

            // Company Logo on Top
            try {
                java.io.InputStream is = ReportGenerator.class.getResourceAsStream("/whizupp-logo.png");
                if (is != null) {
                    byte[] bytes = is.readAllBytes();
                    Image img = Image.getInstance(bytes);
                    img.scaleToFit(120, 60);
                    img.setAlignment(Element.ALIGN_CENTER);
                    document.add(img);
                }
            } catch (Exception e) {
                System.err.println("Could not load logo for report PDF: " + e.getMessage());
            }

            // Whizupp Header
            Paragraph header = new Paragraph("Whizupp Ltd - Juice Production & Inventory System",
                    FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8, BaseColor.GRAY));
            header.setAlignment(Element.ALIGN_RIGHT);
            document.add(header);

            document.add(new Paragraph("\n"));

            // Title
            Paragraph titlePara = new Paragraph(title, titleFont);
            titlePara.setAlignment(Element.ALIGN_CENTER);
            document.add(titlePara);

            // Timestamp, date range and Prepared By
            String dateRange = (from != null && to != null) ? "Period: " + from + " to " + to : "Period: All Time";
            String prepBy = (generatedBy != null && !generatedBy.isEmpty()) ? "  |  Prepared By: " + generatedBy : "";
            Paragraph infoPara = new Paragraph(
                    dateRange + prepBy + "  |  Generated: "
                            + OffsetDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
                    normalFont);
            infoPara.setAlignment(Element.ALIGN_CENTER);
            document.add(infoPara);

            document.add(new Paragraph("\n"));

            // Detailed Data Table First
            if (data != null && !data.isEmpty()) {
                Paragraph dataTitle = new Paragraph("Detailed Records",
                        FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, BaseColor.DARK_GRAY));
                document.add(dataTitle);
                document.add(new Paragraph("\n"));

                Map<String, Object> firstRow = data.get(0);
                java.util.List<String> keys = new java.util.ArrayList<>();
                for (String key : firstRow.keySet()) {
                    if (!"id".equalsIgnoreCase(key)) {
                        keys.add(key);
                    }
                }
                int columns = keys.size();
                PdfPTable table = new PdfPTable(columns);
                table.setWidthPercentage(100);

                // Add headers
                for (String key : keys) {
                    PdfPCell cell = new PdfPCell(new Phrase(camelCaseToWords(key), headerFont));
                    cell.setBackgroundColor(new BaseColor(10, 90, 45)); // Deep green theme
                    cell.setPadding(6);
                    table.addCell(cell);
                }

                // Add data rows
                for (Map<String, Object> row : data) {
                    for (String key : keys) {
                        Object val = row.get(key);
                        String cellText = "—";
                        if (val != null) {
                            String rawStr = String.valueOf(val);
                            if ("startTime".equals(key)) {
                                try {
                                    OffsetDateTime odt = OffsetDateTime.parse(rawStr);
                                    cellText = odt.format(DateTimeFormatter.ofPattern("HH:mm"));
                                } catch (Exception e) {
                                    cellText = rawStr;
                                }
                            } else if ("completionTime".equals(key) || "testDate".equals(key)) {
                                try {
                                    OffsetDateTime odt = OffsetDateTime.parse(rawStr);
                                    cellText = odt.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
                                } catch (Exception e) {
                                    cellText = rawStr;
                                }
                            } else {
                                cellText = rawStr;
                            }
                        }
                        PdfPCell cell = new PdfPCell(new Phrase(cellText, normalFont));
                        cell.setPadding(5);
                        table.addCell(cell);
                    }
                }
                document.add(table);
                document.add(new Paragraph("\n"));
            }

            // Summary Section at the Bottom
            if (summary != null && !summary.isEmpty()) {
                Paragraph sumTitle = new Paragraph("Summary Metrics",
                        FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, BaseColor.DARK_GRAY));
                document.add(sumTitle);
                document.add(new Paragraph("\n"));

                PdfPTable sumTable = new PdfPTable(2);
                sumTable.setWidthPercentage(100);
                sumTable.setSpacingAfter(15);
                for (Map.Entry<String, Object> entry : summary.entrySet()) {
                    if (entry.getValue() instanceof Map)
                        continue; // skip complex maps
                    PdfPCell cellKey = new PdfPCell(new Phrase(camelCaseToWords(entry.getKey()), boldFont));
                    cellKey.setBackgroundColor(new BaseColor(245, 245, 245));
                    cellKey.setPadding(6);
                    PdfPCell cellVal = new PdfPCell(new Phrase(String.valueOf(entry.getValue()), normalFont));
                    cellVal.setPadding(6);
                    sumTable.addCell(cellKey);
                    sumTable.addCell(cellVal);
                }
                document.add(sumTable);
            }

            document.close();
            return out.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Error generating PDF report", e);
        }
    }

    public static byte[] generateExcel(String title, Map<String, Object> summary, List<Map<String, Object>> data, String generatedBy) {
        try (org.apache.poi.xssf.usermodel.XSSFWorkbook workbook = new org.apache.poi.xssf.usermodel.XSSFWorkbook();
                ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            org.apache.poi.ss.usermodel.Sheet sheet = workbook.createSheet("Report Data");

            int rowIdx = 0;

            // Title Row
            org.apache.poi.ss.usermodel.Row titleRow = sheet.createRow(rowIdx++);
            org.apache.poi.ss.usermodel.Cell titleCell = titleRow.createCell(0);
            titleCell.setCellValue(title);
            org.apache.poi.ss.usermodel.CellStyle titleStyle = workbook.createCellStyle();
            org.apache.poi.ss.usermodel.Font titleFont = workbook.createFont();
            titleFont.setBold(true);
            titleFont.setFontHeightInPoints((short) 16);
            titleStyle.setFont(titleFont);
            titleCell.setCellStyle(titleStyle);

            // Prepared By & Timestamp Rows
            if (generatedBy != null && !generatedBy.isEmpty()) {
                org.apache.poi.ss.usermodel.Row prepRow = sheet.createRow(rowIdx++);
                prepRow.createCell(0).setCellValue("Prepared By: " + generatedBy);
            }
            org.apache.poi.ss.usermodel.Row genRow = sheet.createRow(rowIdx++);
            genRow.createCell(0).setCellValue("Generated: " + OffsetDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));

            rowIdx++; // spacing

            // Detailed Data Table First
            int detailEndCol = 0;
            if (data != null && !data.isEmpty()) {
                org.apache.poi.ss.usermodel.Row sectionHeader = sheet.createRow(rowIdx++);
                org.apache.poi.ss.usermodel.Cell cellHeader = sectionHeader.createCell(0);
                cellHeader.setCellValue("Detailed Records");
                org.apache.poi.ss.usermodel.CellStyle sectionStyle = workbook.createCellStyle();
                org.apache.poi.ss.usermodel.Font secFont = workbook.createFont();
                secFont.setBold(true);
                secFont.setFontHeightInPoints((short) 12);
                sectionStyle.setFont(secFont);
                cellHeader.setCellStyle(sectionStyle);

                Map<String, Object> firstRow = data.get(0);
                java.util.List<String> keys = new java.util.ArrayList<>();
                for (String key : firstRow.keySet()) {
                    if (!"id".equalsIgnoreCase(key)) {
                        keys.add(key);
                    }
                }

                org.apache.poi.ss.usermodel.Row headerRow = sheet.createRow(rowIdx++);
                int colIdx = 0;

                org.apache.poi.ss.usermodel.CellStyle tableHeaderStyle = workbook.createCellStyle();
                org.apache.poi.ss.usermodel.Font tableHeaderFont = workbook.createFont();
                tableHeaderFont.setBold(true);
                tableHeaderStyle.setFont(tableHeaderFont);

                for (String key : keys) {
                    org.apache.poi.ss.usermodel.Cell cell = headerRow.createCell(colIdx++);
                    cell.setCellValue(camelCaseToWords(key));
                    cell.setCellStyle(tableHeaderStyle);
                }
                detailEndCol = colIdx;

                for (Map<String, Object> row : data) {
                    org.apache.poi.ss.usermodel.Row dataRow = sheet.createRow(rowIdx++);
                    int cIdx = 0;
                    for (String key : keys) {
                        Object val = row.get(key);
                        String cellText = "";
                        if (val != null) {
                            String rawStr = String.valueOf(val);
                            if ("startTime".equals(key)) {
                                try {
                                    OffsetDateTime odt = OffsetDateTime.parse(rawStr);
                                    cellText = odt.format(DateTimeFormatter.ofPattern("HH:mm"));
                                } catch (Exception e) {
                                    cellText = rawStr;
                                }
                            } else if ("completionTime".equals(key) || "testDate".equals(key)) {
                                try {
                                    OffsetDateTime odt = OffsetDateTime.parse(rawStr);
                                    cellText = odt.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
                                } catch (Exception e) {
                                    cellText = rawStr;
                                }
                            } else {
                                cellText = rawStr;
                            }
                        }
                        dataRow.createCell(cIdx++).setCellValue(cellText);
                    }
                }
                rowIdx++; // spacing
            }

            // Summary Section at the Bottom
            if (summary != null && !summary.isEmpty()) {
                org.apache.poi.ss.usermodel.Row sectionHeader = sheet.createRow(rowIdx++);
                org.apache.poi.ss.usermodel.Cell cellHeader = sectionHeader.createCell(0);
                cellHeader.setCellValue("Summary Metrics");
                org.apache.poi.ss.usermodel.CellStyle sectionStyle = workbook.createCellStyle();
                org.apache.poi.ss.usermodel.Font secFont = workbook.createFont();
                secFont.setBold(true);
                secFont.setFontHeightInPoints((short) 12);
                sectionStyle.setFont(secFont);
                cellHeader.setCellStyle(sectionStyle);

                for (Map.Entry<String, Object> entry : summary.entrySet()) {
                    if (entry.getValue() instanceof Map)
                        continue;
                    org.apache.poi.ss.usermodel.Row sRow = sheet.createRow(rowIdx++);
                    sRow.createCell(0).setCellValue(camelCaseToWords(entry.getKey()));
                    sRow.createCell(1).setCellValue(String.valueOf(entry.getValue()));
                }
                rowIdx++; // spacing
            }

            // Auto-fit columns
            int maxCols = Math.max(detailEndCol, 2);
            for (int i = 0; i < maxCols; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(out);
            return out.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Error generating Excel report", e);
        }
    }

    private static String camelCaseToWords(String str) {
        if (str == null || str.isEmpty())
            return "";
        StringBuilder result = new StringBuilder();
        result.append(Character.toUpperCase(str.charAt(0)));
        for (int i = 1; i < str.length(); i++) {
            char ch = str.charAt(i);
            if (Character.isUpperCase(ch)) {
                result.append(" ");
            }
            result.append(ch);
        }
        return result.toString();
    }
}
