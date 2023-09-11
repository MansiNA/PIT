package com.example.application.views;

import com.example.application.data.entity.*;
import com.example.application.data.service.*;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Article;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.TabSheet;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.renderer.NativeButtonRenderer;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.Command;
import com.wontlost.ckeditor.Config;
import com.wontlost.ckeditor.Constants;
import com.wontlost.ckeditor.VaadinCKEditor;
import com.wontlost.ckeditor.VaadinCKEditorBuilder;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import javax.swing.text.Position;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;


@Route(value="PFG-Cube", layout = MainLayout.class)
@PageTitle("PIT | PFG-Cube")
public class PFGCubeView extends VerticalLayout {

    private final MSMService service;
    private final KnowledgeBaseService knowledgeBaseService;
    private final AttachmentsService attachmentsService;
    private final AgentJobsService agentJobsService;
    private final ProjectService projectService;
    Grid<ProductHierarchie> grid = new Grid<>(ProductHierarchie.class);
    Grid<Attachments> gridAttachments = new Grid<>(Attachments.class);

    Grid<AgentJobs> gridAgentJobs = new Grid<>(AgentJobs.class);
    TextField filterText = new TextField();
    TabSheet tabSheet = new TabSheet();

    Button saveBtn = new Button("save");
    Button editBtn = new Button("edit");
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");
    Div textArea = new Div();
    VerticalLayout messageLayout = new VerticalLayout();

    VaadinCKEditor editor;
    PFGProductForm form;


    Checkbox autorefresh = new Checkbox();

    private Label lastRefreshLabel;
    private Label countdownLabel;
    private ScheduledExecutorService executor;
    private UI ui ;
    Instant startTime;


    public PFGCubeView(MSMService service, KnowledgeBaseService knowledgeBaseService, AttachmentsService attachmentsService, AgentJobsService agentJobsService, ProjectService projectService) {
        this.service = service;
        this.knowledgeBaseService = knowledgeBaseService;
        this.attachmentsService = attachmentsService;
        this.agentJobsService = agentJobsService;
        this.projectService = projectService;

        // System.out.println("UI-ID im Konstruktor: " + ui.toString());

        countdownLabel = new Label();
        lastRefreshLabel=new Label();
        countdownLabel.setVisible(false);

        ui= UI.getCurrent();

        addClassName("list-view");
        setSizeFull();
        configureGrid();
       // configureAttachmentsGrid();
        configureForm();
        configureLoggingArea();

        configureAgentJobGrid();

        saveBtn.setVisible(false);
        editBtn.setVisible(true);

        HorizontalLayout hl = new HorizontalLayout();
     //   hl.add(getTabsheet(),saveBtn,editBtn);
        hl.add(getTabsheet());

        hl.setHeightFull();
        hl.setSizeFull();

        add(hl);


        updateList();
        closeEditor();
    }

    private void configureLoggingArea() {

        messageLayout = new VerticalLayout();
        messageLayout.setWidthFull();
        messageLayout.getStyle().set("background-color", "black");
        messageLayout.getStyle().set("color", "white");
        messageLayout.getStyle().set("position", "fixed");
        messageLayout.getStyle().set("bottom", "0");

        // Create and add messages

        messageLayout.add(textArea);

        // Add the layout to the main view
        add(messageLayout);

    }


    private Component getTabsheet() {

        //TabSheet tabSheet = new TabSheet();

        //Edit-Button nur im Tab Description anzeigen:
        tabSheet.addSelectedChangeListener(e->{

            if (e.getSelectedTab().getLabel().contains("Description"))
            {
                editBtn.setVisible(true);
            }
            else {
                editBtn.setVisible(false);
            }

        });

        saveBtn.addClickListener((event -> {
            //  System.out.println("Speicher den Inhalt: "+ editor.getValue());

            KnowledgeBase myKB = new KnowledgeBase();
            myKB.setId(1L);
            myKB.setRichText(editor.getValue());

            knowledgeBaseService.update(myKB);
            editBtn.setVisible(true);
            saveBtn.setVisible(false);
            //editor.setReadOnly(true);
            editor.setReadOnlyWithToolbarAction(!editor.isReadOnly());

        }));

        editBtn.addClickListener(e->{
            editBtn.setVisible(false);
            saveBtn.setVisible(true);
            //editor.setReadOnly(false);
            editor.setReadOnlyWithToolbarAction(!editor.isReadOnly());
        });


        //tabSheet.add("Description",  new Div(new Text("This is the Description tab content")));
        tabSheet.add("Description",  getPFGDescription());
        tabSheet.add("PFG-Mapping", getPFGMapping() );
       // tabSheet.add("Attachments",  new Div(new Text("This is the Attachments tab content")));
        tabSheet.add("Attachments",  getAttachmentsTab() );
        tabSheet.add("DB-Jobs",  getAgentJobTab() );
        tabSheet.add("QS",  new Div(new Text("This is the QS tab content")));


        tabSheet.setSizeFull();
        tabSheet.setHeightFull();

        return tabSheet;

    }

    private Component getAgentJobTab() {

   //     configureAgentJobGrid();

        VerticalLayout content = new VerticalLayout(gridAgentJobs);

        content.setSizeFull();
        content.setHeight("250px");

        content.add(getAgentJobToolbar());


        return content;

    }

    private Component getAgentJobToolbar() {

        Button refreshBtn = new Button("refresh");
        refreshBtn.addClickListener(e->{
            //System.out.println("Refresh Button gedrückt!");

            var agentJobs=getAgentJobs();

            //gridAgentJobs.setItems(getAgentJobs());

            gridAgentJobs.setItems(agentJobs);
            updateLastRefreshLabel();

        });

        countdownLabel.setText("initialisiert");

        autorefresh.setLabel("Auto-Refresh");

        autorefresh.addClickListener(e->{

            if (autorefresh.getValue()){
                System.out.println("Autorefresh wird eingeschaltet.");
                startCountdown(Duration.ofSeconds(60));
              //  countdownLabel.setText("timer on");
                countdownLabel.setVisible(true);

             /*   Integer count = 0;
                while (count < 10) {
                    // Sleep to emulate background work
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException ex) {
                        throw new RuntimeException(ex);
                    }
                    String message = "This is update " + count++;

                    ui.access(() -> add(new Span(message)));
                }*/

                // Inform that we are done
                ui.access(() -> {
                    add(new Span("Done updating"));
                });




            }
            else{
                System.out.println("Autorefresh wird ausgeschaltet.");
                stopCountdown();
                countdownLabel.setVisible(false);
            }

        });

        updateLastRefreshLabel();
        HorizontalLayout layout = new HorizontalLayout(refreshBtn,lastRefreshLabel, autorefresh, countdownLabel);
        layout.setPadding(false);
        layout.setAlignItems(FlexComponent.Alignment.BASELINE);


        return layout;

    }


    private void startCountdown(Duration duration) {
        executor = Executors.newSingleThreadScheduledExecutor();

        startTime = Instant.now();

        updateLastRefreshLabel();

        executor.scheduleAtFixedRate(() -> {

           /* Command com = new Command() {
                @Override
                public void execute() {
                    countdownLabel.setText("ongoing");
                }
            };

            ui.access(com);*/



            ui.access(() -> {

                Duration remainingTime = calculateRemainingTime(duration, startTime);
         //       updateCountdownLabel(remainingTime);

                countdownLabel.setText("ongoing");

                //System.out.println("UI-ID: " + ui.toString());

            });
            ui.notify();

        }, 0, 1, java.util.concurrent.TimeUnit.SECONDS);
    }

    private void updateCountdownLabel(Duration remainingTime) {
        long seconds = remainingTime.getSeconds();
        String formattedTime = String.format("%02d", (seconds % 60));

        System.out.println("Timeremaining:" + remainingTime);

        if (remainingTime.isNegative()){
            startTime = Instant.now();

            //gridAgentJobs.setItems(getAgentJobs());

            updateLastRefreshLabel();
            return;
        }

        countdownLabel.setText("in " + formattedTime + " Sekunden");
    }

    private void updateLastRefreshLabel() {
        LocalTime currentTime = LocalTime.now();
        String formattedTime = currentTime.format(DateTimeFormatter.ofPattern("HH:mm:ss"));

        lastRefreshLabel.setText("letzte Aktualisierung: " + formattedTime);
    }
    private void stopCountdown() {
        if (executor != null && !executor.isShutdown()) {
            executor.shutdown();
        }
    }

    private void configureAgentJobGrid() {
        gridAgentJobs.addClassNames("PFG-AgentJobs");
        //gridAttachments.setSizeFull();
        gridAgentJobs.setColumns("name", "job_Activity", "duration_Min", "jobStartDate", "jobStopDate", "jobNextRunDate", "result" );

        //select JobName, JobEnabled,JobDescription, JobActivity, DurationMin, JobStartDate, JobLastExecutedStep, JobExecutedStepDate, JobStopDate, JobNextRunDate, Result from job_status

        gridAgentJobs.addColumn(
                new NativeButtonRenderer<>("Run",
                        clickedItem -> {

                            System.out.println("clicked:" + clickedItem.getName());
                            Notification notification = Notification.show("Job " + clickedItem.getName() + " wurde gestartet...",6000, Notification.Position.TOP_END);
                            notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                            try {
                                startJob(clickedItem.getName());
                            } catch (InterruptedException e) {
                                throw new RuntimeException(e);
                            }


                        })
        );


        gridAgentJobs.getColumns().forEach(col -> col.setAutoWidth(true));

        gridAgentJobs.setItems(getAgentJobs());

    }

    private Duration calculateRemainingTime(Duration duration, Instant startTime) {
        Instant now = Instant.now();
        Instant endTime = startTime.plus(duration);
        return Duration.between(now, endTime);
    }


    private List<AgentJobs> getAgentJobs() {

        Project project = projectService.search("PFG_Cube");


        return agentJobsService.findbyJobName(project.getAgentjobs());
      //  return agentJobsService.findAll();

    }

    private Component getAttachmentsTab() {
        configureAttachmentsGrid();

        HorizontalLayout content = new HorizontalLayout(gridAttachments);

        content.setSizeFull();
        content.setHeightFull();

        return content;

    }

    private Span createBadge(int value) {
        Span badge = new Span(String.valueOf(value));
        badge.getElement().getThemeList().add("badge small contrast");
        badge.getStyle().set("margin-inline-start", "var(--lumo-space-xs)");
        return badge;
    }

    private VerticalLayout getPFGDescription() {

        VerticalLayout content = new VerticalLayout();

   //     Button changeReadonlyMode = new Button("change readonly mode");

//        changeReadonlyMode.addClickListener((event -> {
//            editor.setReadOnlyWithToolbarAction(!editor.isReadOnly());
//        }));

        Config config = new Config();
        config.setBalloonToolBar(Constants.Toolbar.values());
        config.setImage(new String[][]{},
                "", new String[]{"full", "alignLeft", "alignCenter", "alignRight"},
                new String[]{"imageTextAlternative", "|",
                        "imageStyle:alignLeft",
                        "imageStyle:full",
                        "imageStyle:alignCenter",
                        "imageStyle:alignRight"}, new String[]{});

        editor = new VaadinCKEditorBuilder().with(builder -> {

            builder.editorType = Constants.EditorType.CLASSIC;
            builder.width = "95%";
            builder.readOnly = true;
            builder.hideToolbar=true;
            builder.config = config;
        }).createVaadinCKEditor();

        editor.setReadOnly(true);


        content.add(editor,editBtn,saveBtn);

        Long id = 1L;
        Optional<KnowledgeBase> kb = knowledgeBaseService.findById(id);

        editor.setValue(kb.get().getRichText());


        //content.add(saveBtn);

        //content.setSizeFull();
        //content.setHeightFull();

        return content;

    }

    private void closeEditor() {
        form.setProduct(null);
        form.setVisible(false);
        removeClassName("editing");
    }
    private void updateList() {

        grid.setItems(service.findAllProducts(filterText.getValue()));
    }

    private Component getContent() {

        HorizontalLayout content = new HorizontalLayout(grid, form);

      //  HorizontalLayout content = new HorizontalLayout(grid, form);
     //   HorizontalLayout content = new HorizontalLayout(tabSheet);
        //HorizontalLayout content = new HorizontalLayout(grid);
      //  content.setFlexGrow(2,grid);
      //  content.setFlexGrow(1,form);
      //  content.addClassName("content");
      //  content.setSizeFull();

        return content;

    }

    private Component getPFGMapping() {

        VerticalLayout vl = new VerticalLayout();

        HorizontalLayout content = new HorizontalLayout(grid, form);
        content.setFlexGrow(2,grid);
        content.setFlexGrow(1,form);
        content.addClassName("content");
        content.setSizeFull();
        content.setHeightFull();

        vl.add(getToolbar(),content);

        vl.setSizeFull();
        vl.setHeightFull();

        return vl;

    }

    private void configureForm() {

        form = new PFGProductForm();
        form.setWidth("25em");
        //form.addSaveListener(this::saveProduct);

        form.addListener(PFGProductForm.SaveEvent.class,this::saveProduct);
        form.addListener(PFGProductForm.DeleteEvent.class, this::deleteProduct);
        form.addListener(PFGProductForm.CloseEvent.class, e -> closeEditor());


        //form.addDeleteListener(this::deleteProduct);
        //form.addCloseListener(e -> closeEditor());


    }

    private void saveProduct(PFGProductForm.SaveEvent event) {
        service.saveProduct(event.getProduct());
        updateList();
        closeEditor();
    }

    private void deleteProduct(PFGProductForm.DeleteEvent event) {
        service.deleteProduct(event.getProduct());
        updateList();
        closeEditor();
    }

    private void configureGrid() {
        grid.addClassNames("PFG-grid");
        grid.setSizeFull();
        grid.setHeightFull();
        grid.setColumns("pfg_Type", "node", "product_name");

        grid.getColumns().forEach(col -> col.setAutoWidth(true));

//        grid.asSingleSelect().addValueChangeListener(event ->
//                editProduct(event.getValue()));

        grid.addItemDoubleClickListener(event ->
                editProduct(event.getItem()));

    }

    private void configureAttachmentsGrid() {

        gridAttachments.addClassNames("PFG-Attachmentsgrid");
        //gridAttachments.setSizeFull();
        gridAttachments.setColumns("filename", "description");

        gridAttachments.getColumns().forEach(col -> col.setAutoWidth(true));

        gridAttachments.setItems(getAttachments());

//        grid.asSingleSelect().addValueChangeListener(event ->
//                editProduct(event.getValue()));

        //gridAttachments.addItemDoubleClickListener(event -> editProduct(event.getItem()));

    }

    private List<Attachments> getAttachments() {

        return attachmentsService.findAll();
    }


    private void editProduct(ProductHierarchie product) {
        if (product == null) {
            closeEditor();
        } else {
            form.setProduct(product);
            form.setVisible(true);
            addClassName("editing");
        }
    }

    private void addProduct() {
        grid.asSingleSelect().clear();
        editProduct(new ProductHierarchie());
    }

    private HorizontalLayout getToolbar() {
        filterText.setPlaceholder("Filter by node/product...");
        filterText.setClearButtonVisible(true);
        filterText.setValueChangeMode(ValueChangeMode.LAZY);
        filterText.addValueChangeListener(e -> updateList());

        Button addProductButton = new Button("Add Mapping");
        addProductButton.addClickListener(click -> addProduct());


//        Button startJobButton = new Button("Start");
//        startJobButton.addClickListener(click -> startJob());

  //      var toolbar = new HorizontalLayout(filterText, addProductButton, startJobButton);
        HorizontalLayout toolbar = new HorizontalLayout(filterText, addProductButton);
        toolbar.addClassName("toolbar");

        return toolbar;
    }

    private void startJob(String jobName) throws InterruptedException {

        var erg= service.startJob(jobName);
        if (!erg.contains("OK"))
        {
            Notification.show(erg, 5000, Notification.Position.MIDDLE);
        }

        Article article=new Article();
        article.setText(LocalDateTime.now().format(formatter) + ": Job " + jobName + " gestartet..." );
        textArea.add (article);

        Thread.sleep(2000);
        gridAgentJobs.setItems(getAgentJobs());

    }

    @Override
    protected void onDetach(DetachEvent event) {
        super.onDetach(event);
        // Stoppe den Timer, wenn das UI geschlossen wird
        stopCountdown();
    }
}
