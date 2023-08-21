package com.example.application.views;

import com.example.application.data.entity.Attachments;
import com.example.application.data.entity.KnowledgeBase;
import com.example.application.data.entity.ProductHierarchie;
import com.example.application.data.service.AttachmentsService;
import com.example.application.data.service.KnowledgeBaseService;
import com.example.application.data.service.MSMService;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.TabSheet;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.wontlost.ckeditor.Config;
import com.wontlost.ckeditor.Constants;
import com.wontlost.ckeditor.VaadinCKEditor;
import com.wontlost.ckeditor.VaadinCKEditorBuilder;

import java.util.Optional;


@Route(value="PFG-Cube", layout = MainLayout.class)
@PageTitle("PIT | PFG-Cube")
public class PFGCubeView extends VerticalLayout {

    private final MSMService service;
    private final KnowledgeBaseService knowledgeBaseService;
    Grid<ProductHierarchie> grid = new Grid<>(ProductHierarchie.class);
    Grid<Attachments> gridAttachments = new Grid<>(Attachments.class);
    TextField filterText = new TextField();
    TabSheet tabSheet = new TabSheet();

    Button saveBtn = new Button("save");
    Button editBtn = new Button("edit");

    VaadinCKEditor editor;
    PFGProductForm form;
    public PFGCubeView(MSMService service, KnowledgeBaseService knowledgeBaseService, AttachmentsService attachmentsService) {
        this.service = service;
        this.knowledgeBaseService = knowledgeBaseService;

        addClassName("list-view");
        setSizeFull();
        configureGrid();
       // configureAttachmentsGrid();
        configureForm();

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
            editor.setReadOnly(true);

        }));

        editBtn.addClickListener(e->{
            editBtn.setVisible(false);
            saveBtn.setVisible(true);
            editor.setReadOnly(false);
        });


        //tabSheet.add("Description",  new Div(new Text("This is the Description tab content")));
        tabSheet.add("Description",  getPFGDescription());
        tabSheet.add("PFG-Mapping", getPFGMapping() );
        tabSheet.add("Attachments",  new Div(new Text("This is the Attachments tab content")));
        tabSheet.add("DB-Jobs",  new Div(new Text("This is the Job-Info/Execution tab")));
        tabSheet.add("QS",  new Div(new Text("This is the QS tab content")));


        tabSheet.setSizeFull();
        tabSheet.setHeightFull();

        return tabSheet;

    }

    private Span createBadge(int value) {
        Span badge = new Span(String.valueOf(value));
        badge.getElement().getThemeList().add("badge small contrast");
        badge.getStyle().set("margin-inline-start", "var(--lumo-space-xs)");
        return badge;
    }

    private VerticalLayout getPFGDescription() {

        VerticalLayout content = new VerticalLayout();

        Button changeReadonlyMode = new Button("change readonly mode");

        changeReadonlyMode.addClickListener((event -> {
            editor.setReadOnlyWithToolbarAction(!editor.isReadOnly());
        }));

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


        content.add(changeReadonlyMode,editor,editBtn,saveBtn);

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
        gridAttachments.setSizeFull();
        gridAttachments.setColumns("Filename", "Description");

        gridAttachments.getColumns().forEach(col -> col.setAutoWidth(true));

//        grid.asSingleSelect().addValueChangeListener(event ->
//                editProduct(event.getValue()));

        //gridAttachments.addItemDoubleClickListener(event -> editProduct(event.getItem()));

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

    private void startJob() {
        service.startJob("Test");

    }


}
