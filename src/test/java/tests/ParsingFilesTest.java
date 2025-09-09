package tests;

import com.codeborne.pdftest.PDF;
import static org.assertj.core.api.Assertions.assertThat;

import com.codeborne.xlstest.XLS;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.zeroturnaround.zip.ZipUtil;

import java.io.*;
import java.net.URISyntaxException;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class ParsingFilesTest {

    private File getResource(String fileName) throws URISyntaxException {
        var url = ParsingFilesTest.class.getClassLoader().getResource(fileName);
        if (url == null)
            throw new AssertionError(String.format("Файл %s не найден", fileName));
        return new File(url.toURI());
    }

    @DisplayName("Проверка PDF-файла из ZIP архива")
    @Test
    void pdfFileParsingTest() throws Exception {
        File file = getResource("Файлы.zip");
        AtomicBoolean pdfFound = new AtomicBoolean(false);
        ZipUtil.iterate(file, (entryStream, entry) -> {
            if (pdfFound.get()) return;

            if (entry.getName().toLowerCase().endsWith(".pdf")) {
                pdfFound.set(true);

                System.out.println("PDF найден: " + entry.getName());

                assertThat(entry.getSize())
                    .as("Найденный PDF-файл пуст")
                    .isGreaterThan(0);

                PDF pdf = new PDF(entryStream);

                assertThat(pdf.text).contains("Антуан де Сент-Экзюпери", "Маленький принц");
                assertThat(pdf.numberOfPages).isBetween(1, 5);

                assertThat(pdf.text.trim()).isNotEmpty();
            }
        });

        assertThat(pdfFound.get())
                .as("PDF-файл не найден в архиве")
                .isTrue();
    }

    @DisplayName("Проверка excel-файла из ZIP архива")
    @Test
    void excelFileParsingTest() throws Exception {
        File file = getResource("Файлы.zip");
        AtomicBoolean xlsxFound = new AtomicBoolean(false);
        ZipUtil.iterate(file, (entryStream, entry) -> {
            if (xlsxFound.get()) return;

            if (entry.getName().toLowerCase().endsWith(".xlsx")) {
                xlsxFound.set(true);

                System.out.println("XLSX найден: " + entry.getName());

                assertThat(entry.getSize())
                        .as("XLSX-файл найден, но он пуст")
                        .isGreaterThan(0);
                XLS xls = new XLS(entryStream);

                String actualCellValue0 = xls.excel.getSheetAt(0).getRow(0).getCell(0).getStringCellValue();
                String actualCellValue1 = xls.excel.getSheetAt(0).getRow(0).getCell(1).getStringCellValue();
                String actualCellValue2 = xls.excel.getSheetAt(0).getRow(0).getCell(2).getStringCellValue();

                Assertions.assertTrue(actualCellValue0.contains("ФИО"));
                Assertions.assertTrue(actualCellValue1.contains("Должность"));
                Assertions.assertTrue(actualCellValue2.contains("Дата оформления"));

            }
        });

        assertThat(xlsxFound.get())
                .as("XLSX-файл не найден в архиве")
                .isTrue();
    }

    @DisplayName("Проверка csv-файла из ZIP архива")
    @Test
    void csvFileParsingTest() throws Exception {
        File file = getResource("Файлы.zip");
        AtomicBoolean csvFound = new AtomicBoolean(false);
        ZipUtil.iterate(file, (entryStream, entry) -> {
            if (csvFound.get()) return;
            if (entry.getName().toLowerCase().endsWith(".csv")) {
                csvFound.set(true);

                System.out.println("csv найден: " + entry.getName());
                try (Reader reader = new InputStreamReader(entryStream)) {
                    try (CSVReader csvReader = new CSVReader(reader)) {
                        List<String[]> data = csvReader.readAll();

                        Assertions.assertFalse(data.isEmpty(), "CSV-файл пуст");

                        Assertions.assertArrayEquals(new String[]{"Александр", "водитель", "89261111111"}, data.get(0));
                        Assertions.assertArrayEquals(new String[]{"Петр", "грузчик", "89262222222"}, data.get(1));
                    } catch (CsvException ex) {
                        Assertions.fail(ex);
                    }
                }
            }
        });

        assertThat(csvFound.get())
                .as("CSV-файл не найден в архиве")
                .isTrue();
    }
}
