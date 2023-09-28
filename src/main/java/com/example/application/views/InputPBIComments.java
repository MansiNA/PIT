package com.example.application.views;

import com.example.application.data.entity.CLTV_HW_Measures;
import com.example.application.data.entity.CLTV_HW_MeasuresDataProvider;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.accordion.AccordionPanel;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.crud.BinderCrudEditor;
import com.vaadin.flow.component.crud.Crud;
import com.vaadin.flow.component.crud.CrudEditor;
import com.vaadin.flow.component.crud.CrudFilter;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Article;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.TabSheet;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MemoryBuffer;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.converter.StringToIntegerConverter;
import com.vaadin.flow.data.provider.*;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.InputStream;
import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Comparator.naturalOrder;


@Route(value="InputPBIComments", layout = MainLayout.class)
@PageTitle("Tech KPI | TEF-Control")
public class InputPBIComments extends VerticalLayout {

    MemoryBuffer memoryBuffer = new MemoryBuffer();
    Upload singleFileUpload = new Upload(memoryBuffer);
    Button importButton = new Button("Freigabe");

    Article article = new Article();
    Div textArea = new Div();
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");
    InputStream fileData_Financials;
    InputStream fileData_Subscriber;
    InputStream fileData_UnitsDeepDive;
    String fileName = "";
    long contentLength = 0;
    String mimeType = "";
    private List<Financials> listOfFinancials = new ArrayList<Financials>();
    private List<Subscriber> listOfSubscriber = new ArrayList<Subscriber>();
    private List<UnitsDeepDive> listOfUnitsDeepDive = new ArrayList<UnitsDeepDive>();

    private Crud<Financials> crudFinancials;
    Grid<Financials> gridFinancials = new Grid<>(Financials.class);

    public InputPBIComments() {

        Div htmlDiv = new Div();
        htmlDiv.getElement().setProperty("innerHTML", "<h2>Input Frontend for PBI Comments");

        // Div zur Ansicht hinzufügen
        add(htmlDiv);

        HorizontalLayout hl = new HorizontalLayout();
        hl.setAlignItems(Alignment.CENTER);

        hl.add(singleFileUpload,importButton);
        add(hl);
        add(textArea);


        setupUploader();

      //  crudFinancials = new Crud<>(Financials.class, createEditor());
     //   setupFinancialsGrid();

        //crudFinancials.setHeight("600px");

        //crudFinancials.setDataProvider();

       // add(crudFinancials);
        VerticalLayout vl = new VerticalLayout();
        Article text = new Article();
        text.add("Hier möglichst noch den jeweils ausgewählten Projekt-Namen mit Pfad ausgeben...(breadcrump like)");
        vl.add(text,getTabsheet());

        vl.setHeightFull();
        vl.setSizeFull();

        add(vl);
    }

    private TabSheet getTabsheet() {
        System.out.println("tabsheet..........");
        TabSheet tabSheet = new TabSheet();

       tabSheet.add("CommentsFinancials", getFinancialsGrid());
       tabSheet.add("CommentsSubscriber", new H1("welcome"));
       tabSheet.add("CommentsUnitsDeepDive", new H1("Hello"));

        tabSheet.setSizeFull();
        tabSheet.setHeightFull();
        return tabSheet;
    }

    private Component getFinancialsGrid() {
        VerticalLayout content = new VerticalLayout();
        crudFinancials = new Crud<>(Financials.class, createEditor());
        setupFinancialsGrid();
        content.add(crudFinancials);
        return content;
    }

    private CrudEditor<Financials> createEditor() {

        IntegerField zeile = new IntegerField  ("Zeile");
        IntegerField month = new IntegerField ("Month");
        TextField category = new TextField("Category");
        TextField comment = new TextField("Comment");
        TextField scenario = new TextField("Scenario");
        TextField xtd = new TextField("XTD");

        FormLayout editForm = new FormLayout(zeile, month, category, comment, scenario, xtd);

        Binder<Financials> binder = new Binder<>(Financials.class);
        //binder.forField(month).withNullRepresentation("202301"").withConverter(new StringToIntegerConverter("Not a Number")).asRequired().bind(Financials::setMonth, Financials::setMonth);
        //  binder.forField(monat_ID).asRequired().bind(CLTV_HW_Measures::getMonat_ID, CLTV_HW_Measures::setMonat_ID);
        binder.forField(month).asRequired().bind(Financials::getMonth, Financials::setMonth);
        binder.forField(category).asRequired().bind(Financials::getCategory, Financials::setCategory);
        binder.forField(comment).asRequired().bind(Financials::getComment, Financials::setComment);
        binder.forField(scenario).asRequired().bind(Financials::getScenario, Financials::setScenario);
        binder.forField(xtd).asRequired().bind(Financials::getXtd, Financials::setXtd);
        binder.forField(zeile).asRequired().bind(Financials::getRow, Financials::setRow);

        return new BinderCrudEditor<>(binder, editForm);
    }

    private void setupUploader() {
        System.out.println("setup uploader................start");
        singleFileUpload.setWidth("600px");

        singleFileUpload.addSucceededListener(event -> {
            // Get information about the uploaded file
            fileData_Financials = memoryBuffer.getInputStream();
            fileName = event.getFileName();
            contentLength = event.getContentLength();
            mimeType = event.getMIMEType();

            listOfFinancials = parseExcelFile(fileData_Financials, fileName,"Comments Financials", Financials.class);
            listOfSubscriber = parseExcelFile(fileData_Financials, fileName,"Comments Subscriber", Subscriber.class);
             listOfUnitsDeepDive = parseExcelFile(fileData_Financials, fileName,"Comments Units Deep Dive", UnitsDeepDive.class);
            //listOfFinancials = parseExcelFile_Financials(fileData_Financials, fileName,"Comments Financials");
   //         listOfSubscriber = parseExcelFile_Subscriber(fileData_Subscriber, fileName,"Comments Subscriber");
//            listOfUnitsDeepDive = parseExcelFile_UnitsDeepDive(fileData_UnitsDeepDive, fileName, "Comments Units Deep Dive");
      //      System.out.println(listOfSubscriber.get(listOfSubscriber.size()-1).getSegment());
        //    FinancialsDataProvider dataProvider = new FinancialsDataProvider(listOfFinancials);
            GenericDataProvider dataProvider = new GenericDataProvider(listOfFinancials);
            crudFinancials.setDataProvider(dataProvider);
            setupDataProviderEvent();

            singleFileUpload.clearFileList();
            crudFinancials.setHeight("600px");

        });
        System.out.println("setup uploader................over");
    }

    private void setupFinancialsGrid() {


        String ZEILE = "row";

        String MONTH = "month";

        String COMMENT = "comment";

        String SCENARIO = "scenario";

        String CATEGORY = "category";

        String XTD = "xtd";

        String EDIT_COLUMN = "vaadin-crud-edit-column";

        gridFinancials = crudFinancials.getGrid();

        gridFinancials.getColumnByKey("row").setHeader("Zeile").setWidth("10px");

        // Reorder the columns (alphabetical by default)
        gridFinancials.setColumnOrder( gridFinancials.getColumnByKey(ZEILE)
                , gridFinancials.getColumnByKey(MONTH)
                , gridFinancials.getColumnByKey(COMMENT)
                , gridFinancials.getColumnByKey(SCENARIO)
                , gridFinancials.getColumnByKey(CATEGORY)
                , gridFinancials.getColumnByKey(XTD)
                , gridFinancials.getColumnByKey(EDIT_COLUMN));

        gridFinancials.addItemDoubleClickListener(e->{
            System.out.println("Zeile: " + e.getItem().getRow());
        });


    }

    public <T> List<T>  parseExcelFile(InputStream fileData, String fileName, String sheetName, Class<T> targetType) {


        List<T> resultList = new ArrayList<>();
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
            System.out.println(sheetName+"...........");
            //System.out.println("Excel import: "+  fileName + " => Mime-Type: " + mimeType  + " Größe " + contentLength + " Byte");
            textArea.setText(LocalDateTime.now().format(formatter) + ": Info: Verarbeite Datei: " + fileName + " (" + contentLength + " Byte)");

            XSSFWorkbook my_xls_workbook = new XSSFWorkbook(fileData);
            XSSFSheet my_worksheet = my_xls_workbook.getSheet(sheetName);
            Iterator<Row> rowIterator = my_worksheet.iterator();

            int RowNumber=0;
            Integer Error_count=0;

            while (rowIterator.hasNext()) {
                Row row = rowIterator.next();
                T entity = targetType.newInstance();
                RowNumber++;

                if (RowNumber == 1) {
                    continue;
                }

               // System.out.println(row.getLastCellNum()+" ___________________________"+row.toString());
                Field[] fields = targetType.getDeclaredFields();
                for (int index = 0; index < (fields.length -1); index++) {

                    Field field = fields[index];
                    if( index == 0) {
                        field.set(entity, RowNumber);
                    }

                    field = fields[index+1];
                    field.setAccessible(true);
                    Cell cell = row.getCell(index);
                  //  System.out.println(cell + " ......."+field.getType() + "---"+field.getName());

                    if (cell != null) {
                        if (field.getType() == int.class || field.getType() == Integer.class) {
                            field.set(entity, (int) cell.getNumericCellValue());
                        } else if (field.getType() == double.class || field.getType() == Double.class) {
                            field.set(entity, cell.getNumericCellValue());
                        } else if (field.getType() == String.class) {
                            field.set(entity, cell.getStringCellValue());
                        }
                    }
                }

                resultList.add(entity);
            }
            System.out.println(resultList.get(0).toString()+"list...."+resultList.size());
            return resultList;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

    }

    public List<Financials> parseExcelFile_Financials(InputStream fileData, String fileName, String sheetName) {


        List<Financials> listOfFinancials = new ArrayList<>();
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
            //message.setText(LocalDateTime.now().format(formatter) + ": Info: reading file: " + fileName);

            //addRowsBT.setEnabled(false);
            //replaceRowsBT.setEnabled(false);
            //spinner.setVisible(true);

            //  HSSFWorkbook my_xls_workbook = new HSSFWorkbook(fileData);
            XSSFWorkbook my_xls_workbook = new XSSFWorkbook(fileData);
            //   HSSFSheet my_worksheet = my_xls_workbook.getSheetAt(0);
            XSSFSheet my_worksheet = my_xls_workbook.getSheet(sheetName);
            Iterator<Row> rowIterator = my_worksheet.iterator();

            Integer RowNumber=0;
            Integer Error_count=0;

            while(rowIterator.hasNext() )
            {
                Financials financials = new Financials();
                Row row = rowIterator.next();
                RowNumber++;

                //if (RowNumber>200){ break; }



                Iterator<Cell> cellIterator = row.cellIterator();
                while(cellIterator.hasNext()) {

                    if(RowNumber==1 ) //Überschrift nicht betrachten
                    {
                        break;
                    }


                    Cell cell = cellIterator.next();
                    financials.setRow(RowNumber);

                    if(cell.getColumnIndex()==0)
                    {
                        String ColumnName="Month";
                        try {
                            financials.setMonth(checkCellNumeric(sheetName, cell, RowNumber,ColumnName));
                        }
                        catch(Exception e)
                        {
                            article=new Article();
                            article.setText(LocalDateTime.now().format(formatter) + " " + sheetName + ": Error: Zeile " + RowNumber.toString() + ", Spalte " + ColumnName + ": " + e.getMessage());
                            textArea.add(article);
                            Error_count++;

                        }
                    }

                    if(cell.getColumnIndex()==1)
                    {
                        String ColumnName="Category";
                        try {
                            financials.setCategory(checkCellString(sheetName, cell, RowNumber,ColumnName));
                        }
                        catch(Exception e)
                        {
                            article=new Article();
                            article.setText(LocalDateTime.now().format(formatter) + " " + sheetName + ": Error: Zeile " + RowNumber.toString() + ", Spalte " + ColumnName + ": " + e.getMessage());
                            textArea.add(article);
                            Error_count++;

                        }
                    }

                    if(cell.getColumnIndex()==2)
                    {
                        String ColumnName="Date";
                        try {
                            financials.setComment(checkCellString(sheetName, cell, RowNumber,ColumnName));
                        }
                        catch(Exception e)
                        {
                            article=new Article();
                            article.setText(LocalDateTime.now().format(formatter) + " " + sheetName + ": Error: Zeile " + RowNumber.toString() + ", Spalte " + ColumnName + ": " + e.getMessage());
                            textArea.add(article);
                            Error_count++;

                        }
                    }

                    if(cell.getColumnIndex()==3)
                    {
                        String ColumnName="Scenario";
                        try {
                            financials.setScenario(checkCellString(sheetName, cell, RowNumber,ColumnName));
                        }
                        catch(Exception e)
                        {
                            article=new Article();
                            article.setText(LocalDateTime.now().format(formatter) + " " + sheetName + ": Error: Zeile " + RowNumber.toString() + ", Spalte " + ColumnName + ": " + e.getMessage());
                            textArea.add(article);
                            Error_count++;

                        }
                    }

                    if(cell.getColumnIndex()==4)
                    {
                        String ColumnName="XTD";
                        try {
                            financials.setXtd(checkCellString(sheetName, cell, RowNumber,ColumnName));
                        }
                        catch(Exception e)
                        {
                            article=new Article();
                            article.setText(LocalDateTime.now().format(formatter) + " " + sheetName + ": Error: Zeile " + RowNumber.toString() + ", Spalte " + ColumnName + ": " + e.getMessage());
                            textArea.add(article);
                            Error_count++;

                        }
                    }

                }

                if(RowNumber != 1) {
                    listOfFinancials.add(financials);
                    System.out.println(listOfFinancials.size()+".............parse");
                }


            }

            article=new Article();
            article.getStyle().set("white-space","pre-line");
            article.add("\n");
            article.add(LocalDateTime.now().format(formatter) + " " + sheetName + ": Count Rows: " + listOfFinancials.size() + " Count Errrors: " + Error_count);
            article.add("\n");
            textArea.add(article);


            System.out.println("Anzahl Zeilen im Excel: " + listOfFinancials.size());


            return listOfFinancials;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

    }

    private Double checkCellDouble(String sheetName, Cell cell, Integer zeile, String spalte) {

        try {

            switch (cell.getCellType()){
                case Cell.CELL_TYPE_NUMERIC:
                    return  (double) cell.getNumericCellValue();
                case Cell.CELL_TYPE_STRING:
                    return 0.0;
                case Cell.CELL_TYPE_FORMULA:
                    return 0.0;
                case Cell.CELL_TYPE_BLANK:
                    return 0.0;
                case Cell.CELL_TYPE_BOOLEAN:
                    return 0.0;
                case Cell.CELL_TYPE_ERROR:
                    return 0.0;

            }
            article.add("\n" + sheetName + " Zeile " + zeile.toString() + ", column >" + spalte + "< konnte in checkCellDouble nicht aufgelöst werden. Typ=" + cell.getCellType());
            textArea.add(article);

        }
        catch(Exception e){
            switch (e.getMessage()) {
                case "Cannot get a text value from a error formula cell":

                    article = new Article();
                    article.setText("\n" + sheetName + ": Info: row >" + zeile.toString() + "<, column " + spalte + ": formula cell error => replaced to 0");
                    textArea.add(article);

                    return  0.0;

            }
            System.out.println("Zeile " + zeile.toString() + ", Spalte " + spalte + " konnte in checkCellDouble nicht aufgelöst werden. Typ=" + cell.getCellType() + e.getMessage());
        }


        return  0.0;



        /*


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

         */

    }

    private String checkCellString(String sheetName, Cell cell, Integer zeile, String spalte) {

        try {

            switch (cell.getCellType()){
                case Cell.CELL_TYPE_NUMERIC:
                    return cell.getStringCellValue();
                case Cell.CELL_TYPE_STRING:
                    return cell.getStringCellValue();
                case Cell.CELL_TYPE_FORMULA:
                    return cell.getStringCellValue();
                case Cell.CELL_TYPE_BLANK:
                    return  "";
                case Cell.CELL_TYPE_BOOLEAN:
                    return cell.getStringCellValue();
                case Cell.CELL_TYPE_ERROR:
                    return  "";

            }
            article.add("\n" + sheetName + " Zeile " + zeile.toString() + ", column >" + spalte + "< konnte in checkCellString nicht aufgelöst werden. Typ=" + cell.getCellType());
            textArea.add(article);

        }
        catch(Exception e){
            switch (e.getMessage()) {
                case "Cannot get a text value from a error formula cell":

                    article = new Article();
                    article.setText("\n" + sheetName + ": Info: row >" + zeile.toString() + "<, column " + spalte + ": formula cell error => replaced to empty cell");
                    textArea.add(article);

                    return "";

            }
            System.out.println("Zeile " + zeile.toString() + ", Spalte " + spalte + " konnte in checkCellString nicht aufgelöst werden. Typ=" + cell.getCellType() + e.getMessage());
        }


        return  "######";

    }

    private Date checkCellDate(String sheetName, Cell cell, Integer zeile, String spalte) {
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

    private Integer checkCellNumeric(String sheetName, Cell cell, Integer zeile, String spalte) {


        switch (cell.getCellType()){
            case Cell.CELL_TYPE_NUMERIC:
                return  (int) cell.getNumericCellValue();
            case Cell.CELL_TYPE_STRING:
                return 0;
            case Cell.CELL_TYPE_FORMULA:
                return 0;
            case Cell.CELL_TYPE_BLANK:
                return 0;
            case Cell.CELL_TYPE_BOOLEAN:
                return 0;
            case Cell.CELL_TYPE_ERROR:
                return 0;

        }

        return 0;


/*
        if (cell.getCellType()!=Cell.CELL_TYPE_NUMERIC)
        {
            var CellType =cell.getCellType();

            System.out.println("Zeile " + zeile.toString() + ", Spalte " + spalte + " konnte nicht gelesen werden, da ExcelTyp nicht numerisch, sonder hat Typ: " + CellType );
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

 */

    }

    private void setupDataProviderEvent() {
    //    FinancialsDataProvider dataProvider = new FinancialsDataProvider(getDataProviderAllItems());
        GenericDataProvider dataProvider = new GenericDataProvider(getDataProviderAllItems());

        article=new Article();
        article.setText(LocalDateTime.now().format(formatter) + ": Info: Download from Database");
        textArea.add(article);

        crudFinancials.addDeleteListener(
                deleteEvent -> {dataProvider.delete(deleteEvent.getItem());
                    crudFinancials.setDataProvider(dataProvider);

                });
        crudFinancials.addSaveListener(
                saveEvent -> {
                    dataProvider.persist(saveEvent.getItem());
                    crudFinancials.setDataProvider(dataProvider);
                });
    }

    private List<Financials> getDataProviderAllItems() {
        DataProvider<Financials, Void> existDataProvider = (DataProvider<Financials, Void>) gridFinancials.getDataProvider();
        List<Financials> listOfFinancials = existDataProvider.fetch(new Query<>()).collect(Collectors.toList());
        return listOfFinancials;
    }


    public static class Subscriber {

        public Subscriber() {
        }

        private Integer row;

        private Integer month;

        private String category;

        private String comment;

        private String paymentType;

        private String segment;

        public Integer getRow() {
            return row;
        }

        public void setRow(Integer row) {
            this.row = row;
        }

        public Integer getMonth() {
            return month;
        }

        public void setMonth(Integer month) {
            this.month = month;
        }

        public String getCategory() {
            return category;
        }

        public void setCategory(String category) {
            this.category = category;
        }

        public String getComment() {
            return comment;
        }

        public void setComment(String comment) {
            this.comment = comment;
        }

        public String getPaymentType() {
            return paymentType;
        }

        public void setPaymentType(String paymentType) {
            this.paymentType = paymentType;
        }

        public String getSegment() {
            return segment;
        }

        public void setSegment(String segment) {
            this.segment = segment;
        }
    }

    public static class UnitsDeepDive {

        public UnitsDeepDive() {
        }

        private Integer row;

        private Integer month;

        private String category;

        private String comment;

        private String segment;

        public Integer getRow() {
            return row;
        }

        public void setRow(Integer row) {
            this.row = row;
        }

        public Integer getMonth() {
            return month;
        }

        public void setMonth(Integer month) {
            this.month = month;
        }

        public String getCategory() {
            return category;
        }

        public void setCategory(String category) {
            this.category = category;
        }

        public String getComment() {
            return comment;
        }

        public void setComment(String comment) {
            this.comment = comment;
        }

        public String getSegment() {
            return segment;
        }

        public void setSegment(String segment) {
            this.segment = segment;
        }
    }

    public static class Financials {

        public Financials() {
        }

        private Integer row;

        private Integer month;

        private String category;

        private String comment;

        private String scenario;

        private String xtd;

        public Integer getRow() {
            return row;
        }

        public void setRow(Integer row) {
            this.row = row;
        }

        public Integer getMonth() {
            return month;
        }

        public void setMonth(Integer month) {
            this.month = month;
        }

        public String getCategory() {
            return category;
        }

        public void setCategory(String category) {
            this.category = category;
        }

        public String getComment() {
            return comment;
        }

        public void setComment(String comment) {
            this.comment = comment;
        }

        public String getScenario() {
            return scenario;
        }

        public void setScenario(String scenario) {
            this.scenario = scenario;
        }

        public String getXtd() {
            return xtd;
        }

        public void setXtd(String xtd) {
            this.xtd = xtd;
        }
    }

    public class GenericDataProvider<T> extends AbstractBackEndDataProvider<T, CrudFilter> {

        private final List<T> DATABASE;
        private Consumer<Long> sizeChangeListener;

        public GenericDataProvider(List<T> data) {
            this.DATABASE = data;
        }

        public void setSizeChangeListener(Consumer<Long> sizeChangeListener) {
            this.sizeChangeListener = sizeChangeListener;
        }

        @Override
        protected Stream<T> fetchFromBackEnd(Query<T, CrudFilter> query) {
            int offset = query.getOffset();
            int limit = query.getLimit();

            Stream<T> stream = DATABASE.stream();

            if (query.getFilter().isPresent()) {
                stream = stream.filter(predicate(query.getFilter().get()))
                        .sorted(comparator(query.getFilter().get()));
            }

            return stream.skip(offset).limit(limit);
        }

        private Predicate<T> predicate(CrudFilter filter) {
            return filter.getConstraints().entrySet().stream()
                    .map(constraint -> (Predicate<T>) entity -> {
                        try {
                            Object value = valueOf(constraint.getKey(), entity);
                            return value != null && value.toString().toLowerCase()
                                    .contains(constraint.getValue().toLowerCase());
                        } catch (Exception e) {
                            e.printStackTrace();
                            return false;
                        }
                    }).reduce(Predicate::and).orElse(entity -> true);
        }

        private Object valueOf(String fieldName, T entity) {
            try {
                Field field = entity.getClass().getDeclaredField(fieldName);
                field.setAccessible(true);
                return field.get(entity);
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        }

        private Comparator<T> comparator(CrudFilter filter) {
            return filter.getSortOrders().entrySet().stream().map(sortClause -> {
                try {
                    Comparator<T> comparator = Comparator.comparing(
                            entity -> (Comparable) valueOf(sortClause.getKey(),
                                    entity));

                    if (sortClause.getValue() == SortDirection.DESCENDING) {
                        comparator = comparator.reversed();
                    }

                    return comparator;

                } catch (Exception ex) {
                    return (Comparator<T>) (o1, o2) -> 0;
                }
            }).reduce(Comparator::thenComparing).orElse((o1, o2) -> 0);
        }

        @Override
        protected int sizeInBackEnd(Query<T, CrudFilter> query) {
            long count = fetchFromBackEnd(query).count();

            if (sizeChangeListener != null) {
                sizeChangeListener.accept(count);
            }

            return (int) count;
        }
        public void persist(T item) {
            try {
                Field field = item.getClass().getDeclaredField("row");
                field.setAccessible(true);
                Integer row = (Integer) field.get(item);

                if (row == null) {
                    row = DATABASE.stream().map(entity -> {
                        try {
                            Field entityField = entity.getClass().getDeclaredField("row");
                            entityField.setAccessible(true);
                            return (Integer) entityField.get(entity);
                        } catch (Exception e) {
                            e.printStackTrace();
                            return 0;
                        }
                    }).max(Comparator.naturalOrder()).orElse(0) + 1;
                    field.set(item, row);
                }

                Optional<T> existingItem = find(row);
                if (existingItem.isPresent()) {
                    int position = DATABASE.indexOf(existingItem.get());
                    DATABASE.remove(existingItem.get());
                    DATABASE.add(position, item);
                } else {
                    DATABASE.add(item);
                }
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        }

        public Optional<T> find(Integer id) {
            return DATABASE.stream().filter(entity -> {
                try {
                    Field field = entity.getClass().getDeclaredField("row");
                    field.setAccessible(true);
                    return field.get(entity).equals(id);
                } catch (Exception ex) {
                    throw new RuntimeException(ex);
                }
            }).findFirst();
        }

        public void delete(T item) {
            try {
                Field field = item.getClass().getDeclaredField("row");
                field.setAccessible(true);
                Integer row = (Integer) field.get(item);

                DATABASE.removeIf(entity -> {
                    try {
                        Field entityField = entity.getClass().getDeclaredField("row");
                        entityField.setAccessible(true);
                        return entityField.get(entity).equals(row);
                    } catch (Exception ex) {
                        throw new RuntimeException(ex);
                    }
                });
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        }

    }



    public class FinancialsDataProvider  extends AbstractBackEndDataProvider<Financials, CrudFilter> {
        final List<Financials> DATABASE;

        private Consumer<Long> sizeChangeListener;
        public FinancialsDataProvider(List<Financials> listOfFinancials) {
            this.DATABASE = listOfFinancials;
        }
        @Override
        protected Stream<Financials> fetchFromBackEnd(Query<Financials, CrudFilter> query) {
            int offset = query.getOffset();
            int limit = query.getLimit();

            Stream<Financials> stream = DATABASE.stream();

            if (query.getFilter().isPresent()) {
                stream = stream.filter(predicate(query.getFilter().get()))
                        .sorted(comparator(query.getFilter().get()));
            }

            return stream.skip(offset).limit(limit);
        }

        private static Predicate<Financials> predicate(CrudFilter filter) {
            // For RDBMS just generate a WHERE clause
            return filter.getConstraints().entrySet().stream()
                    .map(constraint -> (Predicate<Financials>) financials -> {
                        try {
                            Object value = valueOf(constraint.getKey(), financials);
                            return value != null && value.toString().toLowerCase()
                                    .contains(constraint.getValue().toLowerCase());
                        } catch (Exception e) {
                            e.printStackTrace();
                            return false;
                        }
                    }).reduce(Predicate::and).orElse(e -> true);
        }

        private static Object valueOf(String fieldName, Financials financials) {
            try {
                Field field = Financials.class.getDeclaredField(fieldName);
                field.setAccessible(true);
                return field.get(financials);
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        }

        private static Comparator<Financials> comparator(CrudFilter filter) {
            // For RDBMS just generate an ORDER BY clause
            return filter.getSortOrders().entrySet().stream().map(sortClause -> {
                try {
                    Comparator<Financials> comparator = Comparator.comparing(
                            person -> (Comparable) valueOf(sortClause.getKey(),
                                    person));

                    if (sortClause.getValue() == SortDirection.DESCENDING) {
                        comparator = comparator.reversed();
                    }

                    return comparator;

                } catch (Exception ex) {
                    return (Comparator<Financials>) (o1, o2) -> 0;
                }
            }).reduce(Comparator::thenComparing).orElse((o1, o2) -> 0);
        }


        @Override
        protected int sizeInBackEnd(Query<Financials, CrudFilter> query) {
            // For RDBMS just execute a SELECT COUNT(*) ... WHERE query
            long count = fetchFromBackEnd(query).count();

            if (sizeChangeListener != null) {
                sizeChangeListener.accept(count);
            }

            return (int) count;
        }

        public void persist(Financials item) {

            if (item.getRow() == null) {
                item.setRow(DATABASE.stream().map(Financials::getRow).max(naturalOrder())
                        .orElse(0) + 1);
            }

            final Optional<Financials> existingItem = find(item.getRow());
            if (existingItem.isPresent()) {
                int position = DATABASE.indexOf(existingItem.get());
                DATABASE.remove(existingItem.get());
                DATABASE.add(position, item);
            } else {
                DATABASE.add(item);
            }
        }

        Optional<Financials> find(Integer id) {
            return DATABASE.stream().filter(entity -> entity.getRow().equals(id))
                    .findFirst();
        }

        public void delete(Financials item) {
            DATABASE.removeIf(entity -> entity.getRow().equals(item.getRow()));
        }

    }




}
