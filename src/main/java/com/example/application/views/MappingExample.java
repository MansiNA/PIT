package com.example.application.views;

import com.example.application.data.entity.CLTV_HW_Measures;
import com.example.application.data.entity.CLTV_HW_MeasuresDataProvider;
import com.example.application.data.entity.Person;
import com.example.application.data.entity.PersonDataProvider;
import com.vaadin.flow.component.crud.BinderCrudEditor;
import com.vaadin.flow.component.crud.Crud;
import com.vaadin.flow.component.crud.CrudEditor;
import com.vaadin.flow.component.crud.CrudFilter;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.provider.AbstractBackEndDataProvider;
import com.vaadin.flow.data.provider.Query;
import com.vaadin.flow.data.provider.SortDirection;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import java.lang.reflect.Field;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static java.util.Comparator.naturalOrder;
import static org.apache.commons.lang3.StringUtils.valueOf;

@Route(value="MappingExample", layout = MainLayout.class)
@PageTitle("Mapping-Example | TEF-Control")
public class MappingExample extends VerticalLayout {

    private Crud<CLTV_HW_Measures> crud;

    private String MONAT_ID = "monat_ID";
    private String DEVICE = "device";
    private String MEASURE_NAME = "measure_Name";
    private String CHANNEL = "channel";
    private String VALUE = "value";
//    private String PROFESSION = "profession";
    private String EDIT_COLUMN = "vaadin-crud-edit-column";

    public MappingExample() {

        crud = new Crud<>(CLTV_HW_Measures.class, createEditor());

        setupGrid();
        setupDataProvider();

        add(crud);

    }


    private CrudEditor<CLTV_HW_Measures> createEditor() {
        TextField monat_ID = new TextField("Monat");
        TextField device = new TextField("Device");
        TextField measure_Name = new TextField("Measure");
        TextField channel = new TextField("Channel");
        TextField value = new TextField("Wert");

        FormLayout form = new FormLayout(monat_ID, device, measure_Name,
                channel, value);

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
        grid.getColumns().forEach(column -> {
            String key = column.getKey();
            if (!visibleColumns.contains(key)) {
                grid.removeColumn(column);
            }
        });


        // Reorder the columns (alphabetical by default)
        grid.setColumnOrder(grid.getColumnByKey(MONAT_ID), grid.getColumnByKey(DEVICE), grid.getColumnByKey(MEASURE_NAME), grid.getColumnByKey(CHANNEL), grid.getColumnByKey(VALUE), grid.getColumnByKey(EDIT_COLUMN));





    }

    private void setupDataProvider() {
        CLTV_HW_MeasuresDataProvider dataProvider = new CLTV_HW_MeasuresDataProvider();
        crud.setDataProvider(dataProvider);
        crud.addDeleteListener(
                deleteEvent -> dataProvider.delete(deleteEvent.getItem()));
        crud.addSaveListener(
                saveEvent -> dataProvider.persist(saveEvent.getItem()));
    }

}
