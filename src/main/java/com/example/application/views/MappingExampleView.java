package com.example.application.views;

import com.example.application.data.entity.CLTV_HW_Measures;
import com.example.application.data.entity.CLTV_HW_MeasuresDataProvider;
import com.example.application.data.service.CLTV_HW_MeasureService;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.crud.BinderCrudEditor;
import com.vaadin.flow.component.crud.Crud;
import com.vaadin.flow.component.crud.CrudEditor;
import com.vaadin.flow.component.details.Details;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Article;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.progressbar.ProgressBar;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MemoryBuffer;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CompletableFuture;

@Route(value="MappingExample", layout = MainLayout.class)
@PageTitle("Mapping-Example | TEF-Control")
public class MappingExampleView extends VerticalLayout {

    private final CLTV_HW_MeasureService cltvHwMeasureService;
    private Crud<CLTV_HW_Measures> crud;

    private String MONAT_ID = "monat_ID";
    private String DEVICE = "device";
    private String MEASURE_NAME = "measure_Name";
    private String CHANNEL = "channel";
    private String VALUE = "value";
//    private String PROFESSION = "profession";
    private String EDIT_COLUMN = "vaadin-crud-edit-column";

    MemoryBuffer memoryBuffer = new MemoryBuffer();
    Upload singleFileUpload = new Upload(memoryBuffer);
    InputStream fileData;
    String fileName="";
    long contentLength=0;
    String mimeType="";
    Button button = new Button("Hochladen");
    Div textArea=new Div();
    TextArea detailsText = new TextArea();
    Icon icon;
    ProgressBar spinner = new ProgressBar();
    Details details = new Details();
    Button countRows = new Button("Count Rows");

    Article article=new Article();

    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");

    public MappingExampleView(CLTV_HW_MeasureService cltvHwMeasureService) {
        this.cltvHwMeasureService = cltvHwMeasureService;

        crud = new Crud<>(CLTV_HW_Measures.class, createEditor());


        setupGrid();
        setupDataProvider();
        setupUploader();

        add(crud);

        HorizontalLayout horl = new HorizontalLayout();
        //horl.setWidthFull();
        horl.setWidth("600px");

        VerticalLayout verl = new VerticalLayout();
        verl.add(button,spinner);

        //horl.add(singleFileUpload,verl,countRows);
        horl.add(singleFileUpload,verl);
        horl.setAlignItems(Alignment.CENTER);

        icon = VaadinIcon.EXCLAMATION_CIRCLE.create();
        icon.getStyle().set("width", "var(--lumo-icon-size-s)");
        icon.getStyle().set("height", "var(--lumo-icon-size-s)");

        detailsText.setWidthFull();
        detailsText.setHeight("300px");
        details = new Details("Details",detailsText);
        details.setOpened(false);
        details.setWidthFull();
        button.setEnabled(false);

        button.setWidth("180px");
        button.addThemeVariants(ButtonVariant.LUMO_SUCCESS);


    //    countRows.addClickListener(clickEvent -> countRows());

        button.addClickListener(clickEvent -> {
            try {
                upload();

                singleFileUpload.clearFileList();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            } catch (IOException e) {
                throw new RuntimeException(e);
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });

        spinner.setIndeterminate(true);
        spinner.setVisible(false);

        add(horl,textArea,details);

        article=new Article();
        article.setText("Warten auf Datei");
        textArea.add(article);


    }

    private void upload() throws SQLException, IOException, ClassNotFoundException, InterruptedException {

        if(fileName.isEmpty() || fileName.length()==0)
        {
            article=new Article();
            article.setText(LocalDateTime.now().format(formatter) + ": Error: Keine Datei angegeben!");
            textArea.add(article);
            return;
        }

        if(!mimeType.contains("application/vnd.ms-excel"))
        {
            article=new Article();
            article.setText(LocalDateTime.now().format(formatter) + ": Error: ungültiges Dateiformat!");
            textArea.add(article);
            return;
        }

        System.out.println("Excel import: "+  fileName + " => Mime-Type: " + mimeType  + " Größe " + contentLength + " Byte");
        textArea.setText(LocalDateTime.now().format(formatter) + ": Info: Verarbeite Datei: " + fileName + " (" + contentLength + " Byte)");


        //   FileInputStream input_document = new FileInputStream(new File("C:\\tmp\\ELA_FAVORITEN.XLS"));
        /* Load workbook */
        button.setEnabled(false);
        //spinner.setVisible(true);

        HSSFWorkbook my_xls_workbook = new HSSFWorkbook(fileData);
//    HSSFWorkbook my_xls_workbook = new HSSFWorkbook(input_document);
        /* Load worksheet */
        HSSFSheet my_worksheet = my_xls_workbook.getSheetAt(0);
        // we loop through and insert data
        Iterator<Row> rowIterator = my_worksheet.iterator();

        List<CLTV_HW_Measures> elaFavoritenListe = new ArrayList<CLTV_HW_Measures>();

        Integer RowNumber=0;
        Boolean isError=false;

        while(rowIterator.hasNext() && !isError)
        {
            CLTV_HW_Measures elaFavoriten = new CLTV_HW_Measures();
            Row row = rowIterator.next();
            RowNumber++;
            //   System.out.println("Zeile:" + RowNumber.toString());

            Iterator<Cell> cellIterator = row.cellIterator();
            while(cellIterator.hasNext()) {

                if(RowNumber==1) //Überschrift nicht betrachten
                {
                    break;
                }

                Cell cell = cellIterator.next();

                try {
                    elaFavoriten.setId(checkCellNumeric(cell, RowNumber,"ID"));
                }
                catch(Exception e)
                {
                    article=new Article();
                    article.setText(LocalDateTime.now().format(formatter) + ": Error: Zeile " + RowNumber.toString() + ", Spalte ID nicht vorhanden!");
                    textArea.add(article);
                    isError=true;
                    break;
                }


                try {
                    cell = cellIterator.next();
                    elaFavoriten.setChannel(checkCellString(cell, RowNumber,"Channel"));
                }
                catch(Exception e)
                {
                    article=new Article();
                    article.setText(LocalDateTime.now().format(formatter) + ": Error: Zeile " + RowNumber.toString() + ", Spalte Channel nicht vorhanden!");
                    textArea.add(article);
                    isError=true;
                    break;
                }



                try {
                    cell = cellIterator.next();
                    elaFavoriten.setDevice(checkCellString(cell, RowNumber,"Device"));
                }
                catch(Exception e)
                {
                    article=new Article();
                    article.setText(LocalDateTime.now().format(formatter) + ": Error: Zeile " + RowNumber.toString() + ", Spalte Device nicht vorhanden!");
                    textArea.add(article);
                    isError=true;
                    break;
                }



                elaFavoritenListe.add(elaFavoriten);

            }

        }

        if(isError)
        {
            //    button.setEnabled(true);
            spinner.setVisible(false);
            fileName="";
            return;
        }

        //textArea.setValue(textArea.getValue() + "\n" + Instant.now() + ": Start Upload to DB");
        article=new Article();
        article.add(LocalDateTime.now().format(formatter) + ": Info: Anzahl Zeilen: " + elaFavoritenListe.size());
        textArea.add(article);
        article=new Article();
        article.add(LocalDateTime.now().format(formatter) + ": Info: Start Upload to DB");
        textArea.add(article);

        System.out.println("Anzahl Zeilen im Excel: " + elaFavoritenListe.size());

        UI ui = UI.getCurrent();
        // Instruct client side to poll for changes and show spinner
        ui.setPollInterval(500);


    }

    private String checkCellString(Cell cell, Integer zeile, String spalte) {

        try{


            if (cell.getCellType()!=Cell.CELL_TYPE_STRING && !cell.getStringCellValue().isEmpty())
            {
                System.out.println("Zeile " + zeile.toString() + ", Spalte " + spalte + " konnte nicht gelesen werden, da ExcelTyp Numeric!");
                detailsText.setValue(detailsText.getValue() + "\nZeile " + zeile.toString() + ", Spalte " + spalte + "  konnte nicht gelesen werden, da ExcelTyp Numeric!");
                return "";
            }
            else
            {
                if (cell.getStringCellValue().isEmpty())
                {
                    //System.out.println("Info: Zeile " + zeile.toString() + ", Spalte " + spalte + " ist leer");
                    detailsText.setValue(detailsText.getValue() + "\nZeile " + zeile.toString() + ", Spalte " + spalte + " ist leer");
                }
                return  cell.getStringCellValue();

            }
        }
        catch(Exception e) {
            System.out.println("Exception" + e.getMessage());
            detailsText.setValue(detailsText.getValue() + "\nZeile " + zeile.toString() + ", Spalte " + spalte + "  konnte nicht gelesen werden, da ExcelTyp Numeric!");
            return "";
        }
    }

    private Integer checkCellNumeric(Cell cell, Integer zeile, String spalte) {

        if (cell.getCellType()!=Cell.CELL_TYPE_NUMERIC)
        {
            System.out.println("Zeile " + zeile.toString() + ", Spalte " + spalte + " konnte nicht gelesen werden, da ExcelTyp nicht numerisch!");
            //     textArea.setValue(textArea.getValue() + "\n" + LocalDateTime.now().format(formatter) + ": Error: Zeile " + zeile.toString() + ", Spalte " + spalte + " konnte nicht gelesen werden, da ExcelTyp nicht Numeric!");
            return 0;
        }
        else
        {
            return  (int) cell.getNumericCellValue();
        }

    }


  /*  private void countRows() {
        String jdbc_sql ="select count(*) from EKP.ELA_FAVORITEN_NEU";

        try {
            DriverManagerDataSource ds = new DriverManagerDataSource();
            Configuration conf;
            conf = comboBox.getValue();

            ds.setUrl(conf.getDb_Url());
            ds.setUsername(conf.getUserName());
            ds.setPassword(conf.getPassword());

            jdbcTemplate.setDataSource(ds);
            int result = jdbcTemplate.queryForObject(jdbc_sql, Integer.class);

            article=new Article();
            article.setText(LocalDateTime.now().format(formatter) + ": Info: Anzahl Zeilen in DB-Table " + result);
            textArea.add(article);


        } catch (Exception e) {
            //   textArea.setValue(textArea.getValue() + "\nFehler beim Speichern in DB!");
            System.out.println("Exception: " + e.getMessage());
            // return e.getMessage();
        }
    }*/

    private void setupUploader() {

        singleFileUpload.setWidth("600px");
        singleFileUpload.addSucceededListener(event -> {
            // Get information about the uploaded file
            fileData = memoryBuffer.getInputStream();
            fileName = event.getFileName();
            contentLength = event.getContentLength();
            mimeType = event.getMIMEType();
            button.setEnabled(true);
            textArea.setText("Warten auf Button \"Hochladen\"");
            detailsText.setValue("Weitere Ladeinformationen bzgl. >>" + fileName + "<<");
            // Do something with the file data
            // processFile(fileData, fileName, contentLength, mimeType);
        });

    }


    private CrudEditor<CLTV_HW_Measures> createEditor() {
        TextField monat_ID = new TextField("Monat");
        TextField device = new TextField("Device");
        TextField measure_Name = new TextField("Measure");
        TextField channel = new TextField("Channel");
        TextField value = new TextField("Value");

        FormLayout form = new FormLayout(monat_ID, device, measure_Name, channel, value);


        Binder<CLTV_HW_Measures> binder = new Binder<>(CLTV_HW_Measures.class);
        binder.forField(measure_Name).asRequired().bind(CLTV_HW_Measures::getMeasure_Name,
                CLTV_HW_Measures::setMeasure_Name);
        binder.forField(device).asRequired().bind(CLTV_HW_Measures::getDevice,
                CLTV_HW_Measures::setDevice);
        binder.forField(channel).asRequired().bind(CLTV_HW_Measures::getChannel,
                CLTV_HW_Measures::setChannel);
        binder.forField(value).asRequired().bind(CLTV_HW_Measures::getValue,
                CLTV_HW_Measures::setValue);

        return new BinderCrudEditor<>(binder, form);
    }

    private void setupGrid() {
        Grid<CLTV_HW_Measures> grid = crud.getGrid();

        // Only show these columns (all columns shown by default):
     //   List<String> visibleColumns = Arrays.asList(FIRST_NAME, LAST_NAME, EMAIL, PROFESSION, EDIT_COLUMN);
        List<String> visibleColumns = Arrays.asList(MONAT_ID, DEVICE, MEASURE_NAME, CHANNEL, VALUE, EDIT_COLUMN);
       // List<String> visibleColumns = Arrays.asList(MONAT_ID, DEVICE, MEASURE_NAME, CHANNEL, EDIT_COLUMN);
        grid.getColumns().forEach(column -> {
            String key = column.getKey();
            if (!visibleColumns.contains(key)) {
                grid.removeColumn(column);
            }
        });


        // Reorder the columns (alphabetical by default)
        grid.setColumnOrder(grid.getColumnByKey(MONAT_ID), grid.getColumnByKey(DEVICE), grid.getColumnByKey(MEASURE_NAME), grid.getColumnByKey(CHANNEL)
                , grid.getColumnByKey(VALUE)
                , grid.getColumnByKey(EDIT_COLUMN));





    }

    private void setupDataProvider() {
        CLTV_HW_MeasuresDataProvider dataProvider = new CLTV_HW_MeasuresDataProvider(cltvHwMeasureService);
        crud.setDataProvider(dataProvider);
        crud.addDeleteListener(
                deleteEvent -> dataProvider.delete(deleteEvent.getItem()));
        crud.addSaveListener(
                saveEvent -> dataProvider.persist(saveEvent.getItem()));
  //      crud.addEditListener(e -> System.out.println("Edit" + e.getItem().getId()));
    }

}
