package com.example.application.views;

import com.example.application.data.entity.CLTV_HW_Measures;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.details.Details;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Article;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.progressbar.ProgressBar;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MemoryBuffer;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.jdbc.core.JdbcTemplate;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Route(value="Tech_KPI", layout = MainLayout.class)
@PageTitle("Tech KPI | TEF-Control")
public class Tech_KPIView extends VerticalLayout {

    private JdbcTemplate jdbcTemplate;
    Article article = new Article();

    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");
    Div textArea = new Div();
    Div message = new Div();

    Button importButton = new Button("Import");
    MemoryBuffer memoryBuffer = new MemoryBuffer();
    Upload singleFileUpload = new Upload(memoryBuffer);

    InputStream fileData;
    String fileName = "";
    long contentLength = 0;
    String mimeType = "";
    Grid<KPI_Fact> gridFact;
    Grid<KPI_Actuals> gridActuals;
    Grid<KPI_Fact> gridPlan;

    H3 h3_Fact= new H3();
    H3 h3_Actuals= new H3();
    H3 h3_Plan= new H3();

    ProgressBar progressBar = new ProgressBar();

    private List<KPI_Fact> listOfKPI_Fact = new ArrayList<KPI_Fact>();
    private List<KPI_Actuals> listOfKPI_Actuals = new ArrayList<KPI_Actuals>();

    public Tech_KPIView(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;

        importButton.setEnabled(false);
        importButton.addClickListener(e->{
            System.out.println("Import Button gedrückt");
            progressBar.setVisible(true);
            progressBar.setValue(0);
            saveEntities();
        });

        setupKPIActualsGrid();
        setupKPIFactGrid();

        setupUploader();

        message.setText("1. Datei hochladen.");

        Details details = new Details("details", textArea);
        details.setOpened(false);

        HorizontalLayout hl = new HorizontalLayout();
        hl.setAlignItems(Alignment.CENTER);
        hl.add(singleFileUpload,importButton,message);

        h3_Fact.add("Fact 0 rows");
        h3_Actuals.add("Actuals 0 rows");
        h3_Plan.add("Plan 0 rows");

        add(hl, progressBar, details, h3_Fact, gridFact, h3_Actuals, gridActuals, h3_Plan ); //, gridPlan );

    //    progressBar.setVisible(false);



    }

    private void saveEntities() {
        UI ui=UI.getCurrent();
        ui.setPollInterval(500);
        int totalRows = listOfKPI_Fact.size();
        progressBar.setVisible(true);
        progressBar.setMin(0);
        progressBar.setMax(totalRows);
        progressBar.setValue(0);


        message.setText(LocalDateTime.now().format(formatter) + ": Info: saving to database...");

        new Thread(() -> {

                    // Do some long running task
                    try {
                        System.out.println("Upload Data to DB");

                        int batchSize = 1000; // Die Anzahl der Zeilen, die auf einmal verarbeitet werden sollen


                        for (int i = 0; i < totalRows; i += batchSize) {

                            int endIndex = Math.min(i + batchSize, totalRows);

                            List<KPI_Fact> batchData = listOfKPI_Fact.subList(i, endIndex);

                            System.out.println("Verarbeitete Zeilen: " + endIndex + " von " + totalRows);

                            saveBlock(batchData);


                            int finalI = i;
                            ui.access(() -> {
                                progressBar.setValue((double) finalI);
                                System.out.println("Fortschritt aktualisiert auf: " + finalI);
                                message.setText(LocalDateTime.now().format(formatter) + ": Info: saving to database (" + endIndex + "/" + totalRows +")");
                            });

                        }


                    } catch (Exception e) {
                        e.printStackTrace();
                    }
            ui.access(() -> {
                ui.setPollInterval(-1);
                progressBar.setVisible(false);
                message.setText(LocalDateTime.now().format(formatter) + ": Info: saved " + totalRows + " rows");
                importButton.setEnabled(false);
                Notification notification = Notification.show(totalRows + " Rows uploaded");
                notification.setPosition(Notification.Position.MIDDLE);
                notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            });

        }).start();

    }

    private void saveBlock(List<KPI_Fact> batchData) {

        String sql = "INSERT INTO [PIT].[Stage_Tech_KPI].[KPI_Fact] (NT_ID, Scenario,[Date],Wert) VALUES (?, ?, ?, ?)";

        jdbcTemplate.batchUpdate(sql, batchData, batchData.size(), (ps, entity) -> {
                ps.setString(1, entity.getNT_ID());
                ps.setString(2, entity.getScenario());
                //  ps.setDate(3, new java.sql.Date(2023,01,01));
                ps.setDate(3, new java.sql.Date(entity.getDate().getTime() ));
                ps.setDouble (4, entity.getWert());
            });

    }

    private void setupKPIActualsGrid() {
        gridActuals = new Grid<>(KPI_Actuals.class, false);

        gridActuals.setHeight("300px");

        gridActuals.addColumn(KPI_Actuals::getRow).setHeader("Zeile");

        gridActuals.addColumn(KPI_Actuals::getNT_ID).setHeader("NT ID");
        gridActuals.addColumn(KPI_Actuals::getSort).setHeader("Sort");
        gridActuals.addColumn(KPI_Actuals::getM2_Area).setHeader("M2_Area");
        gridActuals.addColumn(KPI_Actuals::getM1_Network).setHeader("M1_Network");


    }
    private void setupKPIFactGrid() {
        gridFact = new Grid<>(KPI_Fact.class, false);

        gridFact.setHeight("300px");

        gridFact.addColumn(KPI_Fact::getRow).setHeader("Zeile");

        gridFact.addColumn(KPI_Fact::getNT_ID).setHeader("NT ID");
        gridFact.addColumn(KPI_Fact::getScenario).setHeader("Scenario");
        gridFact.addColumn(KPI_Fact::getDate).setHeader("Date");
        gridFact.addColumn(KPI_Fact::getWert).setHeader("Wert");


    }

    private void setupUploader() {
        System.out.println("setup uploader................start");
        singleFileUpload.setWidth("600px");
        singleFileUpload.addSucceededListener(event -> {
            // Get information about the uploaded file
            fileData = memoryBuffer.getInputStream();
            fileName = event.getFileName();
            contentLength = event.getContentLength();
            mimeType = event.getMIMEType();
            importButton.setEnabled(true);



            //listOfKPI_Fact = parseExcelFile_Fact(fileData, fileName);
            listOfKPI_Actuals = parseExcelFile_Actuals(fileData, fileName);

            gridFact.setItems(listOfKPI_Fact);

            gridActuals.setItems(listOfKPI_Actuals);

            singleFileUpload.clearFileList();
            importButton.setEnabled(true);
            message.setText("2. Button >Import< for upload to Database");
            h3_Fact.removeAll();
            h3_Fact.add("Fact (" + listOfKPI_Fact.size() + " rows)");

            h3_Actuals.removeAll();
            h3_Actuals.add("Actuals (" + listOfKPI_Actuals.size() + " rows)");

        });
        System.out.println("setup uploader................over");
    }
    public List<KPI_Fact> parseExcelFile_Fact(InputStream fileData, String fileName) {


        List<KPI_Fact> listOfKPI_Fact = new ArrayList<>();
        try {
            if(fileName.isEmpty() || fileName.length()==0)
            {
                article=new Article();
                article.setText(LocalDateTime.now().format(formatter) + ": Error: Keine Datei angegeben!");
                textArea.add(article);
            }

            if(!mimeType.contains("openxmlformats-officedocument"))
            {
                article=new Article();
                article.setText(LocalDateTime.now().format(formatter) + ": Error: ungültiges Dateiformat!");
                textArea.add(article);
            }

            System.out.println("Excel import: "+  fileName + " => Mime-Type: " + mimeType  + " Größe " + contentLength + " Byte");
            textArea.setText(LocalDateTime.now().format(formatter) + ": Info: Verarbeite Datei: " + fileName + " (" + contentLength + " Byte)");

            message.setText(LocalDateTime.now().format(formatter) + ": Info: reading file: " + fileName);

            //addRowsBT.setEnabled(false);
            //replaceRowsBT.setEnabled(false);
            //spinner.setVisible(true);

            //  HSSFWorkbook my_xls_workbook = new HSSFWorkbook(fileData);
            XSSFWorkbook my_xls_workbook = new XSSFWorkbook(fileData);
            //   HSSFSheet my_worksheet = my_xls_workbook.getSheetAt(0);
            XSSFSheet my_worksheet = my_xls_workbook.getSheet("KPI_Fact");
            Iterator<Row> rowIterator = my_worksheet.iterator();

            Integer RowNumber=0;
            Boolean isError=false;

            while(rowIterator.hasNext() && !isError)
            {
                KPI_Fact kPI_Fact = new KPI_Fact();
                Row row = rowIterator.next();
                RowNumber++;

                //if (RowNumber>200){ break; }

                kPI_Fact.setRow(RowNumber);

                Iterator<Cell> cellIterator = row.cellIterator();
                while(cellIterator.hasNext()) {

                    if(RowNumber==1 ) //Überschrift nicht betrachten
                    {
                        break;
                    }


                    Cell cell = cellIterator.next();

                    try {
                        kPI_Fact.setNT_ID(checkCellString(cell, RowNumber,"NT ID"));
                    }
                    catch(Exception e)
                    {
                        article=new Article();
                        article.setText(LocalDateTime.now().format(formatter) + ": Error: Zeile " + RowNumber.toString() + ", Spalte NT ID nicht vorhanden!");
                        textArea.add(article);
                        isError=true;
                        break;
                    }

                    try {
                        cell = cellIterator.next();
                        kPI_Fact.setScenario(checkCellString(cell, RowNumber,"Scenario"));
                    }
                    catch(Exception e)
                    {
                        article=new Article();
                        article.setText(LocalDateTime.now().format(formatter) + ": Error: Zeile " + RowNumber.toString() + ", Spalte Scenario nicht vorhanden!");
                        textArea.add(article);
                        isError=true;
                        break;
                    }

                    try {
                        cell = cellIterator.next();
                        kPI_Fact.setDate(checkCellDate(cell, RowNumber,"Date"));
                    }
                    catch(Exception e)
                    {
                        article=new Article();
                        article.setText(LocalDateTime.now().format(formatter) + ": Error: Zeile " + RowNumber.toString() + ", Spalte Date nicht vorhanden! " + e.getMessage());
                        textArea.add(article);
                        isError=true;
                        break;
                    }

                    try {
                        cell = cellIterator.next();
                        kPI_Fact.setWert(checkCellDouble(cell, RowNumber,"Wert"));
                    }
                    catch(Exception e)
                    {
                        article=new Article();
                        article.setText(LocalDateTime.now().format(formatter) + ": Error: Zeile " + RowNumber.toString() + ", Spalte Wert nicht vorhanden!");
                        textArea.add(article);
                        //isError=true;
                        //break;
                        kPI_Fact.setWert(0.0);
                    }



                    listOfKPI_Fact.add(kPI_Fact);
                    System.out.println(listOfKPI_Fact.size()+".............parse");
                }

            }

            if(isError)
            {
                //    button.setEnabled(true);
                // spinner.setVisible(false);
                fileName="";
            }

            article=new Article();
            article.getStyle().set("white-space","pre-line");
            article.add(LocalDateTime.now().format(formatter) + ": Info: Anzahl Zeilen: " + listOfKPI_Fact.size());
            article.add("\n");

            System.out.println("Anzahl Zeilen im Excel: " + listOfKPI_Fact.size());


            return listOfKPI_Fact;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

    }
    public List<KPI_Actuals> parseExcelFile_Actuals(InputStream fileData, String fileName) {


        List<KPI_Actuals> listOfKPI_Actuals = new ArrayList<>();
        try {
            if(fileName.isEmpty() || fileName.length()==0)
            {
                article=new Article();
                article.setText(LocalDateTime.now().format(formatter) + ": Error: Keine Datei angegeben!");
                textArea.add(article);
            }

            if(!mimeType.contains("openxmlformats-officedocument"))
            {
                article=new Article();
                article.setText(LocalDateTime.now().format(formatter) + ": Error: ungültiges Dateiformat!");
                textArea.add(article);
            }


            XSSFWorkbook my_xls_workbook = new XSSFWorkbook(fileData);
            XSSFSheet my_worksheet = my_xls_workbook.getSheet("KPI_Actuals");
            Iterator<Row> rowIterator = my_worksheet.iterator();

            Integer RowNumber=0;
            Boolean isError=false;

            while(rowIterator.hasNext() && !isError)
            {
                KPI_Actuals kPI_Actuals = new KPI_Actuals();
                Row row = rowIterator.next();
                RowNumber++;

               // if (RowNumber>20){ break; }

                kPI_Actuals.setRow(RowNumber);

                Iterator<Cell> cellIterator = row.cellIterator();
                while(cellIterator.hasNext()) {

                    if(RowNumber==1 ) //Überschrift nicht betrachten
                    {
                        break;
                    }


                    Cell cell = cellIterator.next();

                    if(cell.getColumnIndex()==1)
                    {
                         try {
                            kPI_Actuals.setNT_ID(checkCellString(cell, RowNumber,"NT ID"));
                             }
                        catch(Exception e)
                            {
                                article=new Article();
                                article.setText(LocalDateTime.now().format(formatter) + ": Error: Zeile " + RowNumber.toString() + ", Spalte NT ID nicht vorhanden!");
                                textArea.add(article);
                                isError=true;
                                break;
                            }
                    }
/*
                   try {
                        cell = cellIterator.next();
                        kPI_Actuals.setWTAC_ID(checkCellString(cell, RowNumber,"WTAC ID"));
                    }
                    catch(Exception e)
                    {
                        article=new Article();
                        article.setText(LocalDateTime.now().format(formatter) + ": Error: Zeile " + RowNumber.toString() + ", Spalte WTAC ID nicht vorhanden!");
                        textArea.add(article);
                        isError=true;
                        break;
                    }

                    try {
                        cell = cellIterator.next();
                        kPI_Actuals.setSort(checkCellString(cell, RowNumber,"sort"));
                    }
                    catch(Exception e)
                    {
                        article=new Article();
                        article.setText(LocalDateTime.now().format(formatter) + ": Error: Zeile " + RowNumber.toString() + ", Spalte sort nicht vorhanden! " + e.getMessage());
                        textArea.add(article);
                        isError=true;
                        break;
                    }

                    try {
                        cell = cellIterator.next();
                        kPI_Actuals.setM2_Area(checkCellString(cell, RowNumber,"M2_Area"));
                    }
                    catch(Exception e)
                    {
                        article=new Article();
                        article.setText(LocalDateTime.now().format(formatter) + ": Error: Zeile " + RowNumber.toString() + ", Spalte M2_Area nicht vorhanden!");
                        textArea.add(article);
                        isError=true;
                        break;
                        //kPI_Fact.setWert(0.0);
                    }
*/


                }

                listOfKPI_Actuals.add(kPI_Actuals);
                System.out.println(listOfKPI_Actuals.size()+".............parse");
            }

            if(isError)
            {
                //    button.setEnabled(true);
               // spinner.setVisible(false);
                fileName="";
            }

            article=new Article();
            article.getStyle().set("white-space","pre-line");
            article.add(LocalDateTime.now().format(formatter) + ": Info: Anzahl Zeilen: " + listOfKPI_Actuals.size());
            article.add("\n");


            System.out.println("Anzahl Zeilen im Excel: " + listOfKPI_Actuals.size());


            return listOfKPI_Actuals;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

    }

    private Double checkCellDouble(Cell cell, Integer zeile, String spalte) {

        if (cell.getCellType()!=Cell.CELL_TYPE_NUMERIC)
        {
            System.out.println("Zeile " + zeile.toString() + ", Spalte " + spalte + " konnte nicht gelesen werden, da ExcelTyp nicht numerisch!");
            //     textArea.setValue(textArea.getValue() + "\n" + LocalDateTime.now().format(formatter) + ": Error: Zeile " + zeile.toString() + ", Spalte " + spalte + " konnte nicht gelesen werden, da ExcelTyp nicht Numeric!");
            article.add("\nZeile " + zeile.toString() + ", Spalte " + spalte + "  konnte nicht gelesen werden, da ExcelTyp nicht Numeric!");
            textArea.add(article);
            return 0.0;
        }
        else
        {
            //System.out.println("Spalte: " + spalte + " Zeile: " + zeile.toString() + " Wert: " + cell.getNumericCellValue());
            return  (double) cell.getNumericCellValue();
        }

    }

    private String checkCellString(Cell cell, Integer zeile, String spalte) {

        try{

            if (cell.getCellType()!=Cell.CELL_TYPE_STRING && !cell.getStringCellValue().isEmpty())
            {
                System.out.println("Zeile " + zeile.toString() + ", Spalte " + spalte + " konnte nicht gelesen werden, da ExcelTyp Numeric!");
                //detailsText.setValue(detailsText.getValue() + "\nZeile " + zeile.toString() + ", Spalte " + spalte + "  konnte nicht gelesen werden, da ExcelTyp Numeric!");

                article.add("\nZeile " + zeile.toString() + ", Spalte " + spalte + "  konnte nicht gelesen werden, da ExcelTyp Numeric!");
                textArea.add(article);

                return "";
            }
            else
            {
                if (cell.getStringCellValue().isEmpty())
                {
                    //System.out.println("Info: Zeile " + zeile.toString() + ", Spalte " + spalte + " ist leer");
                    //detailsText.setValue(detailsText.getValue() + "\nZeile " + zeile.toString() + ", Spalte " + spalte + " ist leer");
                    article.add("\nZeile " + zeile.toString() + ", Spalte " + spalte + " ist leer");
                    textArea.add(article);
                }
                return  cell.getStringCellValue();

            }
        }
        catch(Exception e) {
            System.out.println("Exception: " + e.getMessage());
            //detailsText.setValue(detailsText.getValue() + "\nZeile " + zeile.toString() + ", Spalte " + spalte + "  konnte nicht gelesen werden, da ExcelTyp Numeric!");
            article.add("\nZeile " + zeile.toString() + ", Spalte " + spalte + "  konnte nicht gelesen werden, da ExcelTyp Numeric!");
            textArea.add(article);
            return "";
        }
    }

    private Date checkCellDate(Cell cell, Integer zeile, String spalte) {
        Date date=null;
        try{


            switch (cell.getCellType()) {
                case Cell.CELL_TYPE_NUMERIC:
                    if (cell.getNumericCellValue() != 0) {
                        //Get date
                        date = (Date) cell.getDateCellValue();



                        //Get datetime
                        cell.getDateCellValue();


                        System.out.println(date.getTime());
                    }
                    break;
            }


        return date;

         /*   if (cell.getCellType()!=Cell.CELL_TYPE_STRING && !cell.getStringCellValue().isEmpty())
            {
                System.out.println("Zeile " + zeile.toString() + ", Spalte " + spalte + " konnte nicht gelesen werden, da ExcelTyp Numeric!");
                //detailsText.setValue(detailsText.getValue() + "\nZeile " + zeile.toString() + ", Spalte " + spalte + "  konnte nicht gelesen werden, da ExcelTyp Numeric!");

                article.add("\nZeile " + zeile.toString() + ", Spalte " + spalte + "  konnte nicht gelesen werden, da ExcelTyp Numeric!");
                textArea.add(article);

                return "";
            }
            else
            {
                if (cell.getStringCellValue().isEmpty())
                {
                    //System.out.println("Info: Zeile " + zeile.toString() + ", Spalte " + spalte + " ist leer");
                    //detailsText.setValue(detailsText.getValue() + "\nZeile " + zeile.toString() + ", Spalte " + spalte + " ist leer");
                    article.add("\nZeile " + zeile.toString() + ", Spalte " + spalte + " ist leer");
                    textArea.add(article);
                }
                return  cell.getStringCellValue();

            }*/
        }
        catch(Exception e) {
            System.out.println("Exception" + e.getMessage());
            //detailsText.setValue(detailsText.getValue() + "\nZeile " + zeile.toString() + ", Spalte " + spalte + "  konnte nicht gelesen werden, da ExcelTyp Numeric!");
            article.add("\nZeile " + zeile.toString() + ", Spalte " + spalte + "  konnte nicht gelesen werden, da ExcelTyp Numeric!");
            textArea.add(article);
            return null;
        }
    }

    private Integer checkCellNumeric(Cell cell, Integer zeile, String spalte) {

        if (cell.getCellType()!=Cell.CELL_TYPE_NUMERIC)
        {
            System.out.println("Zeile " + zeile.toString() + ", Spalte " + spalte + " konnte nicht gelesen werden, da ExcelTyp nicht numerisch!");
            //     textArea.setValue(textArea.getValue() + "\n" + LocalDateTime.now().format(formatter) + ": Error: Zeile " + zeile.toString() + ", Spalte " + spalte + " konnte nicht gelesen werden, da ExcelTyp nicht Numeric!");
            article.add("\nZeile " + zeile.toString() + ", Spalte " + spalte + "  konnte nicht gelesen werden, da ExcelTyp nicht Numeric!");
            textArea.add(article);
            return 0;
        }
        else
        {
            //System.out.println("Spalte: " + spalte + " Zeile: " + zeile.toString() + " Wert: " + cell.getNumericCellValue());
            return  (int) cell.getNumericCellValue();
        }

    }


    public class KPI_Fact {

        private Integer row;

        public Integer getRow() {
            return row;
        }

        public void setRow(Integer row) {
            this.row = row;
        }

        private String NT_ID ;

        private String Scenario = "";

        private Date Date;

        private Double Wert;


        public String getNT_ID() {
            return NT_ID;
        }

        public void setNT_ID(String NT_ID) {
            this.NT_ID = NT_ID;
        }

        public String getScenario() {
            return Scenario;
        }

        public void setScenario(String scenario) {
            Scenario = scenario;
        }

        public Date getDate() {
            return Date;
        }

        public void setDate(Date date) {
            Date = date;
        }

        public Double getWert() {
            return Wert;
        }

        public void setWert(Double wert) {
            Wert = wert;
        }
    }

    public class KPI_Actuals {

        private Integer row;
        private String NT_ID ;

        private String WTAC_ID ;

        private String sort;
        private String M2_Area ;

        private String M1_Network ;

        private String M3_Service;

        private String M4_Dimension;

        private String M5_Tech;

        private String M6_Detail;

        private String KPI_long;

        private String Runrate;

        private String Unit;
        private String Description;
        private String SourceReport;
        private String SourceInput;
        private String SourceComment;
        private String SourceContact;
        private String SourceLink;

        public Integer getRow() {
            return row;
        }

        public void setRow(Integer row) {
            this.row = row;
        }

        public String getNT_ID() {
            return NT_ID;
        }

        public void setNT_ID(String NT_ID) {
            this.NT_ID = NT_ID;
        }

        public String getWTAC_ID() {
            return WTAC_ID;
        }

        public void setWTAC_ID(String WTAC_ID) {
            this.WTAC_ID = WTAC_ID;
        }

        public String getSort() {
            return sort;
        }

        public void setSort(String sort) {
            this.sort = sort;
        }

        public String getM2_Area() {
            return M2_Area;
        }

        public void setM2_Area(String m2_Area) {
            M2_Area = m2_Area;
        }

        public String getM1_Network() {
            return M1_Network;
        }

        public void setM1_Network(String m1_Network) {
            M1_Network = m1_Network;
        }

        public String getM3_Service() {
            return M3_Service;
        }

        public void setM3_Service(String m3_Service) {
            M3_Service = m3_Service;
        }

        public String getM4_Dimension() {
            return M4_Dimension;
        }

        public void setM4_Dimension(String m4_Dimension) {
            M4_Dimension = m4_Dimension;
        }

        public String getM5_Tech() {
            return M5_Tech;
        }

        public void setM5_Tech(String m5_Tech) {
            M5_Tech = m5_Tech;
        }

        public String getM6_Detail() {
            return M6_Detail;
        }

        public void setM6_Detail(String m6_Detail) {
            M6_Detail = m6_Detail;
        }

        public String getKPI_long() {
            return KPI_long;
        }

        public void setKPI_long(String KPI_long) {
            this.KPI_long = KPI_long;
        }

        public String getRunrate() {
            return Runrate;
        }

        public void setRunrate(String runrate) {
            Runrate = runrate;
        }

        public String getUnit() {
            return Unit;
        }

        public void setUnit(String unit) {
            Unit = unit;
        }

        public String getDescription() {
            return Description;
        }

        public void setDescription(String description) {
            Description = description;
        }

        public String getSourceReport() {
            return SourceReport;
        }

        public void setSourceReport(String sourceReport) {
            SourceReport = sourceReport;
        }

        public String getSourceInput() {
            return SourceInput;
        }

        public void setSourceInput(String sourceInput) {
            SourceInput = sourceInput;
        }

        public String getSourceComment() {
            return SourceComment;
        }

        public void setSourceComment(String sourceComment) {
            SourceComment = sourceComment;
        }

        public String getSourceContact() {
            return SourceContact;
        }

        public void setSourceContact(String sourceContact) {
            SourceContact = sourceContact;
        }

        public String getSourceLink() {
            return SourceLink;
        }

        public void setSourceLink(String sourceLink) {
            SourceLink = sourceLink;
        }
    }



}
