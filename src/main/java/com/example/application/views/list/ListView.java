package com.example.application.views.list;

import com.example.application.data.entity.Contact;
import com.example.application.data.entity.ProductHierarchie;
import com.example.application.data.service.MSMService;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility.Margin;

@PageTitle("MQ-Mapping | TEF-Control")
@Route(value = "")
public class ListView extends VerticalLayout {

    private final MSMService service;
    Grid<ProductHierarchie> grid = new Grid<>(ProductHierarchie.class);
    TextField filterText = new TextField();

    ProductForm form;
    public ListView(MSMService service) {
        this.service = service;

        addClassName("list-view");
        setSizeFull();
        configureGrid();
        configureForm();

        add(
            getToolbar(),
            getContent()
        );

        updateList();
        closeEditor();
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
        //HorizontalLayout content = new HorizontalLayout(grid);
        content.setFlexGrow(2,grid);
        content.setFlexGrow(1,form);
        content.addClassName("content");
        content.setSizeFull();

        return content;

    }

    private void configureForm() {

        form = new ProductForm();
        form.setWidth("25em");
        //form.addSaveListener(this::saveProduct);

        form.addListener(ProductForm.SaveEvent.class,this::saveProduct);
        form.addListener(ProductForm.DeleteEvent.class, this::deleteProduct);
        form.addListener(ProductForm.CloseEvent.class, e -> closeEditor());


        //form.addDeleteListener(this::deleteProduct);
        //form.addCloseListener(e -> closeEditor());


    }

    private void saveProduct(ProductForm.SaveEvent event) {
        service.saveProduct(event.getProduct());
        updateList();
        closeEditor();
    }

    private void deleteProduct(ProductForm.DeleteEvent event) {
        service.deleteProduct(event.getProduct());
        updateList();
        closeEditor();
    }

    private void configureGrid() {
        grid.addClassNames("MSM-grid");
        grid.setSizeFull();
        grid.setColumns("msm_Type", "node", "product_name");

        grid.getColumns().forEach(col -> col.setAutoWidth(true));

        grid.asSingleSelect().addValueChangeListener(event ->
                editProduct(event.getValue()));

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

        Button addProductButton = new Button("Add product");
        addProductButton.addClickListener(click -> addProduct());
        var toolbar = new HorizontalLayout(filterText, addProductButton);
        toolbar.addClassName("toolbar");
        return toolbar;
    }


}
