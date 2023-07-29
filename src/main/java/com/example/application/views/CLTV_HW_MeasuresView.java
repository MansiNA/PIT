package com.example.application.views;

import com.example.application.data.entity.CLTV_HW_Measures;
import com.example.application.data.entity.ProductHierarchie;
import com.example.application.data.service.CLTV_HW_MeasureService;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.gridpro.GridPro;
import com.vaadin.flow.component.gridpro.GridProVariant;
import com.vaadin.flow.component.gridpro.ItemUpdater;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

@Route(value="HW-Mapping", layout = MainLayout.class)
@PageTitle("HW-Mapping | TEF-Control")
public class CLTV_HW_MeasuresView extends VerticalLayout {

    private final CLTV_HW_MeasureService cltvHwMeasureService;
    GridPro<CLTV_HW_Measures> grid = new GridPro<>(CLTV_HW_Measures.class);
    TextField filterText = new TextField();
    TextField filterInt = new TextField();

    ComboBox<String> comboBox = new ComboBox<>("Monat");


    public CLTV_HW_MeasuresView(CLTV_HW_MeasureService cltvHwMeasureService) {
        this.cltvHwMeasureService = cltvHwMeasureService;

        addClassName("CLTVHW-list-view");
        setSizeFull();
        configureGrid();
//        configureForm();
//
        add(
                getToolbar(),
                getContent()
        );
//
        updateList();
//        closeEditor();

    }


    private void configureGrid() {
        grid.addClassNames("MSM-grid");
        grid.setSizeFull();
        grid.setColumns("monat_ID", "device", "measure_Name","channel");

        grid.addEditColumn(CLTV_HW_Measures::getValue)
            .text(new ItemUpdater<CLTV_HW_Measures, String>(){
                @Override
                public void accept(CLTV_HW_Measures currow, String s) {
                    //Update der Zeile
                    currow.setValue(s);
                    cltvHwMeasureService.update(currow,s);
                    //System.out.println("Update auf" + s);
                    updateList();
                }})
                .setHeader("Value");

        grid.addThemeVariants(GridProVariant.LUMO_HIGHLIGHT_EDITABLE_CELLS);

        grid.getColumns().forEach(col -> col.setAutoWidth(true));


//        grid.asSingleSelect().addValueChangeListener(event ->
//                editProduct(event.getValue()));

    }

    private HorizontalLayout getToolbar() {
        filterText.setPlaceholder("Filter by Device/Measure/Channel");
        filterText.setClearButtonVisible(true);
        filterText.setValueChangeMode(ValueChangeMode.LAZY);
        filterText.addValueChangeListener(e -> updateList());

  /*      filterInt.setPlaceholder("Filter by Monat");
        filterInt.setClearButtonVisible(true);
        filterInt.setValueChangeMode(ValueChangeMode.LAZY);
        filterInt.addValueChangeListener(e -> updateMonat());*/

        comboBox.setAutoOpen(true);
        comboBox.setItems(cltvHwMeasureService.getMonate());
        comboBox.setLabel("");
        //comboBox.setHelperText("Helper text");
        comboBox.setPlaceholder("Filter by Monat");
        comboBox.setTooltipText("Filter auf vorhandenen Monat");
        comboBox.addValueChangeListener(e -> updateMonat());
     //   comboBox.setClearButtonVisible(true);
        comboBox.setPrefixComponent(VaadinIcon.SEARCH.create());

        Button addProductButton = new Button("Add Monat");
   //     addProductButton.addClickListener(click -> addProduct());


        Button startJobButton = new Button("Start");
     //   startJobButton.addClickListener(click -> startJob());

        var toolbar = new HorizontalLayout(filterText, comboBox, addProductButton, startJobButton);
        toolbar.addClassName("toolbar");

        return toolbar;
    }

    private Component getContent() {
      //  HorizontalLayout content = new HorizontalLayout(grid, form);
        HorizontalLayout content = new HorizontalLayout(grid);
        content.setFlexGrow(2,grid);
    //    content.setFlexGrow(1,form);
        content.addClassName("content");
        content.setSizeFull();

        return content;

    }
    private void updateList() {

        System.out.println("Suche nach String: " + filterText.getValue());
        grid.setItems(cltvHwMeasureService.findAllProducts(filterText.getValue()));
    }

    private void updateMonat() {

       // System.out.println("Suche nach Monat: " + filterInt.getValue());
        grid.setItems(cltvHwMeasureService.findProductsbyMonat(comboBox.getValue().toString()));
    }
}
