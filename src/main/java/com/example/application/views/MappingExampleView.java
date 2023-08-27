package com.example.application.views;

import com.example.application.data.entity.CLTV_HW_Measures;
import com.example.application.data.entity.CLTV_HW_MeasuresDataProvider;
import com.example.application.data.entity.TableInfo;
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
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Article;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.progressbar.ProgressBar;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MemoryBuffer;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.converter.StringToIntegerConverter;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.StreamResource;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import java.io.*;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CompletableFuture;

@Route(value="MappingExample", layout = MainLayout.class)
@PageTitle("Mapping-Example | TEF-Control")
public class MappingExampleView extends VerticalLayout {
    private String exportPath;
    private String exportFileName = "HW_Mapping.xls";
    private final CLTV_HW_MeasureService cltvHwMeasureService;
    private final JdbcTemplate jdbcTemplate;
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
    String fileName = "";
    long contentLength = 0;
    String mimeType = "";
    Button button = new Button("Hochladen");
    Div textArea = new Div();
    //TextArea detailsText = new TextArea();
    Icon icon;
    ProgressBar spinner = new ProgressBar();
    //  Details details = new Details();
    Button countRows = new Button("Count Rows");

    Article article = new Article();
    String ret = "ok";

    private Button exportButton = new Button("Export");
    private Anchor anchor = new Anchor(getStreamResource("CLTV_HW_Mapping.xls", "default content"), "click to download");

    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");

    public MappingExampleView(@Value("${csv_exportPath}") String p_exportPath, CLTV_HW_MeasureService cltvHwMeasureService, JdbcTemplate jdbcTemplate) {

        this.exportPath = p_exportPath;

        this.cltvHwMeasureService = cltvHwMeasureService;
        this.jdbcTemplate = jdbcTemplate;

        crud = new Crud<>(CLTV_HW_Measures.class, createEditor());


        setupGrid();
        setupDataProvider();
        setupUploader();
        setUpExportButton();

        crud.setHeight("600px");

        add(crud);

        HorizontalLayout horl = new HorizontalLayout();
        //horl.setWidthFull();
        horl.setWidth("600px");

        VerticalLayout verl = new VerticalLayout();
        verl.add(button, spinner);

        //horl.add(singleFileUpload,verl,countRows);
        horl.add(singleFileUpload, verl, exportButton, anchor);
        horl.setAlignItems(Alignment.CENTER);

        icon = VaadinIcon.EXCLAMATION_CIRCLE.create();
        icon.getStyle().set("width", "var(--lumo-icon-size-s)");
        icon.getStyle().set("height", "var(--lumo-icon-size-s)");

        //     detailsText.setWidthFull();
        //     detailsText.setHeight("300px");
        //     details = new Details("Details",detailsText);
        //     details.setOpened(false);
        //     details.setWidthFull();
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

        add(horl, textArea);

        //    article=new Article();
        //    article.setText("Warten auf Datei");
        //    textArea.add(article);


    }

    public StreamResource getStreamResource(String filename, String content) {
        return new StreamResource(filename,
                () -> new ByteArrayInputStream(content.getBytes()));
    }

    private void setUpExportButton() {

        exportButton.addThemeVariants(ButtonVariant.LUMO_SMALL);
        exportButton.addThemeVariants(ButtonVariant.LUMO_SUCCESS);
        exportButton.addClickListener(clickEvent -> {
            Notification.show("Exportiere Daten ");
            //System.out.println("aktuelle_SQL:" + aktuelle_SQL);
            try {
                generateExcel(exportPath + exportFileName, "SELECT [id],[monat_id],[device],[measure_name],[channel],[value] FROM [TEF].[dbo].[cltv_hw_measures]");

                File file = new File(exportPath + exportFileName);
                StreamResource streamResource = new StreamResource(file.getName(), () -> getStream(file));

                anchor.setHref(streamResource);


                anchor.setEnabled(true);
                exportButton.setEnabled(false);
                //      download("c:\\tmp\\" + aktuelle_Tabelle + ".xls");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

        anchor.getElement().setAttribute("download", true);
        anchor.setEnabled(false);
    }

    private static void generateExcel(String file, String query) throws IOException {


        try {

            Connection conn = DriverManager.getConnection("jdbc:sqlserver://192.168.58.130;databaseName=TEF;encrypt=true;trustServerCertificate=true", "dwhflex", "dwhflex");


            //   DriverManager.registerDriver(new oracle.jdbc.driver.OracleDriver());


            PreparedStatement stmt=null;
            //Workbook
            HSSFWorkbook workBook=new HSSFWorkbook();
            HSSFSheet sheet1=null;

            //Cell
            Cell c=null;

            CellStyle cs=workBook.createCellStyle();
            HSSFFont f =workBook.createFont();
            f.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
            f.setFontHeightInPoints((short) 12);
            cs.setFont(f);


            sheet1=workBook.createSheet("Sheet1");


            // String query="select  EMPNO, ENAME, JOB, MGR, HIREDATE, SAL, COMM, DEPTNO, WORK_CITY, WORK_COUNTRY from APEX_040000.WWV_DEMO_EMP";
            stmt=conn.prepareStatement(query);
            ResultSet rs=stmt.executeQuery();

            ResultSetMetaData metaData=rs.getMetaData();
            int colCount=metaData.getColumnCount();

            LinkedHashMap<Integer, TableInfo> hashMap=new LinkedHashMap<Integer, TableInfo>();


            for(int i=0;i<colCount;i++){
                TableInfo tableInfo=new TableInfo();
                tableInfo.setFieldName(metaData.getColumnName(i+1).trim());
                tableInfo.setFieldText(metaData.getColumnLabel(i+1));
                tableInfo.setFieldSize(metaData.getPrecision(i+1));
                tableInfo.setFieldDecimal(metaData.getScale(i+1));
                tableInfo.setFieldType(metaData.getColumnType(i+1));
                //     tableInfo.setCellStyle(getCellAttributes(workBook, c, tableInfo));

                hashMap.put(i, tableInfo);
            }

            //Row and Column Indexes
            int idx=0;
            int idy=0;

            HSSFRow row=sheet1.createRow(idx);
            TableInfo tableInfo=new TableInfo();

            Iterator<Integer> iterator=hashMap.keySet().iterator();

            while(iterator.hasNext()){
                Integer key=(Integer)iterator.next();

                tableInfo=hashMap.get(key);
                c=row.createCell(idy);
                c.setCellValue(tableInfo.getFieldText());
                c.setCellStyle(cs);
                if(tableInfo.getFieldSize() > tableInfo.getFieldText().trim().length()){
                    sheet1.setColumnWidth(idy, (tableInfo.getFieldSize()* 10));
                }
                else {
                    sheet1.setColumnWidth(idy, (tableInfo.getFieldText().trim().length() * 5));
                }
                idy++;
            }

            while (rs.next()) {

                idx++;
                row = sheet1.createRow(idx);
                //  System.out.println(idx);
                for (int i = 0; i < colCount; i++) {

                    c = row.createCell(i);
                    tableInfo = hashMap.get(i);

                    switch (tableInfo.getFieldType()) {
                        case 1:
                            c.setCellValue(rs.getString(i+1));
                            break;
                        case 2:
                            c.setCellValue(rs.getDouble(i+1));
                            break;
                        case 3:
                            c.setCellValue(rs.getDouble(i+1));
                            break;
                        default:
                            c.setCellValue(rs.getString(i+1));
                            break;
                    }
                    c.setCellStyle(tableInfo.getCellStyle());
                }

            }
            rs.close();
            stmt.close();
            conn.close();

            // String path="c:\\tmp\\test.xls";

            FileOutputStream fileOut = new FileOutputStream(file);

            workBook.write(fileOut);
            fileOut.close();


        } catch (SQLException | FileNotFoundException e) {
            System.out.println("Error in Method generateExcel(String file, String query) file: " + file + " query: "  + query);
            e.printStackTrace();
        }
    }




    private InputStream getStream(File file) {
        FileInputStream stream = null;
        try {
            stream = new FileInputStream(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        return stream;
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
                    elaFavoriten.setMonat_ID(checkCellNumeric(cell, RowNumber,"Monat_ID"));
                 //   elaFavoriten.setMonat_ID(checkCellDouble(cell, RowNumber,"Monat_ID"));
                }
                catch(Exception e)
                {
                    article=new Article();
                    article.setText(LocalDateTime.now().format(formatter) + ": Error: Zeile " + RowNumber.toString() + ", Spalte Monat_ID nicht vorhanden!");
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
                    article.setText(LocalDateTime.now().format(formatter) + ": Error: Zeile " + RowNumber.toString() + ", Spalte Device nicht vorhanden! " + e.getMessage());
                    textArea.add(article);
                    isError=true;
                    break;
                }

                try {
                    cell = cellIterator.next();
                    elaFavoriten.setMeasure_Name(checkCellString(cell, RowNumber,"Measure_Name"));
                }
                catch(Exception e)
                {
                    article=new Article();
                    article.setText(LocalDateTime.now().format(formatter) + ": Error: Zeile " + RowNumber.toString() + ", Spalte Measure_Name nicht vorhanden!");
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
                    elaFavoriten.setValue(checkCellNumeric(cell, RowNumber,"Value").toString());
                }
                catch(Exception e)
                {
                    article=new Article();
                    article.setText(LocalDateTime.now().format(formatter) + ": Error: Zeile " + RowNumber.toString() + ", Spalte value nicht vorhanden!");
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
        article.getStyle().set("white-space","pre-line");
        article.add(LocalDateTime.now().format(formatter) + ": Info: Anzahl Zeilen: " + elaFavoritenListe.size());
        article.add("\n");
        article.add(LocalDateTime.now().format(formatter) + ": Info: Start Upload to DB");
        textArea.add(article);

        System.out.println("Anzahl Zeilen im Excel: " + elaFavoritenListe.size());

        UI ui = UI.getCurrent();
        // Instruct client side to poll for changes and show spinner
        ui.setPollInterval(500);

        CompletableFuture.runAsync(() -> {

            // Do some long running task
            try {
                ret = write2DB(elaFavoritenListe);


                Thread.sleep(20); //2 Sekunden warten


            } catch (InterruptedException | SQLException | ClassNotFoundException e) {
                e.printStackTrace();
                spinner.setVisible(false);
            }

            // Need to use access() when running from background thread
            ui.access(() -> {
                // Stop polling and hide spinner
                ui.setPollInterval(-1);
//            spinner.setValue(1);
//            button.setEnabled(true);
                spinner.setVisible(false);
                if(!ret.equals("ok")){
                    System.out.println("Fehlgeschlagen! " );
                    article.add("\n");
                    article.add(LocalDateTime.now().format(formatter) + ": Error: Upload to DB fehlgeschlagen: " + ret);
                    textArea.add(article);

                }
                else {
                    article.add("\n");
                    article.add(LocalDateTime.now().format(formatter) + ": Info: Ende Upload to DB erfolgreich");
                    textArea.add(article);
                    setupDataProvider();
                }
                fileName="";
            });
        });

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

    private String write2DB(List<CLTV_HW_Measures> elaFavoritenListe) throws ClassNotFoundException, SQLException {

      //  Class.forName ("jdbc.sqlserver");
        //     Connection conn = DriverManager.getConnection("jdbc:oracle:thin:@//37.120.189.200:1521/xe", "SYSTEM", "Michael123");
        //     PreparedStatement sql_statement = null;
        String jdbc_insert_sql = "INSERT INTO EKP.ELA_FAVORITEN"  + "(ID,BENUTZER_KENNUNG,NUTZER_ID,NAME,VORNAME,ORT,PLZ,STRASSE,HAUSNUMMER,ORGANISATION,VERSION) VALUES"  + "(?,?,?,?,?,?,?,?,?,?,?)";
//        sql_statement = conn.prepareStatement(jdbc_insert_sql);

        //Connection connection = DriverManager.getConnection("jdbc:sqlserver://192.168.58.130;databaseName=TEF;encrypt=true;trustServerCertificate=true", "dwhflex", "dwhflex");


        try {
            DriverManagerDataSource ds = new DriverManagerDataSource();
    ds.setUrl("jdbc:sqlserver://192.168.58.130;databaseName=TEF;encrypt=true;trustServerCertificate=true");
    ds.setUsername("dwhflex");
    ds.setPassword("dwhflex");



            jdbcTemplate.setDataSource(ds);
            jdbcTemplate.batchUpdate("INSERT INTO [TEF].[dbo].[cltv_hw_measures] (id, monat_id, device, measure_name, channel, [value]) VALUES (?, ?, ?,?, ?, ?)",
                    elaFavoritenListe,
                    100,
                    (PreparedStatement ps, CLTV_HW_Measures elaFavoriten1) -> {
                        ps.setInt(1, elaFavoriten1.getId());
                     //   ps.setInt(2, elaFavoriten1.getMonat_ID());
                        ps.setDouble(2, elaFavoriten1.getMonat_ID());
                        ps.setString(3, elaFavoriten1.getDevice());
                        ps.setString(4, elaFavoriten1.getMeasure_Name());
                        ps.setString(5, elaFavoriten1.getChannel());
                        ps.setString(6, elaFavoriten1.getValue());
                    });
            //   textArea.setValue(textArea.getValue() + "\nIn DB gespeichert.");
        } catch (Exception e) {
            //   textArea.setValue(textArea.getValue() + "\nFehler beim Speichern in DB!");
            System.out.println("Exception: " + e.getMessage());
            return e.getMessage();
        }


        /* Close prepared statement */
        //     sql_statement.close();
        /* COMMIT transaction */

//            conn.commit();
        /* Close connection */
//        conn.close();

        return "ok";

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
            System.out.println("Exception" + e.getMessage());
            //detailsText.setValue(detailsText.getValue() + "\nZeile " + zeile.toString() + ", Spalte " + spalte + "  konnte nicht gelesen werden, da ExcelTyp Numeric!");
            article.add("\nZeile " + zeile.toString() + ", Spalte " + spalte + "  konnte nicht gelesen werden, da ExcelTyp Numeric!");
            textArea.add(article);
            return "";
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
         //   detailsText.setValue("Weitere Ladeinformationen bzgl. >>" + fileName + "<<");
            // Do something with the file data
            // processFile(fileData, fileName, contentLength, mimeType);
        });

    }


    private CrudEditor<CLTV_HW_Measures> createEditor() {

        //NumberField monat_ID = new NumberField ("Monat_ID");

        TextField monat_ID = new TextField ("Monat_ID");


        TextField device = new TextField("Device");
        TextField measure_name = new TextField("Measure");
        TextField channel = new TextField("Channel");
        TextField value = new TextField("Value");

        FormLayout form = new FormLayout(monat_ID, device, measure_name, channel, value);


        Binder<CLTV_HW_Measures> binder = new Binder<>(CLTV_HW_Measures.class);
        binder.forField(monat_ID).withNullRepresentation("202301").withConverter(new StringToIntegerConverter("Not a Number")).asRequired().bind(CLTV_HW_Measures::getMonat_ID, CLTV_HW_Measures::setMonat_ID);
      //  binder.forField(monat_ID).asRequired().bind(CLTV_HW_Measures::getMonat_ID, CLTV_HW_Measures::setMonat_ID);
        binder.forField(measure_name).asRequired().bind(CLTV_HW_Measures::getMeasure_Name,
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
