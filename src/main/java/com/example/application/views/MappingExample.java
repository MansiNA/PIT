package com.example.application.views;

import com.example.application.data.entity.CLTV_HW_Measures;
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

    private Crud<Person> crud;

    private String FIRST_NAME = "firstName";
    private String LAST_NAME = "lastName";
//    private String EMAIL = "email";
//    private String PROFESSION = "profession";
    private String EDIT_COLUMN = "vaadin-crud-edit-column";

    public MappingExample() {

        crud = new Crud<>(Person.class, createEditor());

        setupGrid();
        setupDataProvider();

        add(crud);

    }


    private CrudEditor<Person> createEditor() {
        TextField firstName = new TextField("First name");
        TextField lastName = new TextField("Last name");
        EmailField email = new EmailField("Email");
        TextField profession = new TextField("Profession");
        FormLayout form = new FormLayout(firstName, lastName, email,
                profession);

        Binder<Person> binder = new Binder<>(Person.class);
        binder.forField(firstName).asRequired().bind(Person::getFirstName,
                Person::setFirstName);
        binder.forField(lastName).asRequired().bind(Person::getLastName,
                Person::setLastName);


        return new BinderCrudEditor<>(binder, form);
    }

    private void setupGrid() {
        Grid<Person> grid = crud.getGrid();

        // Only show these columns (all columns shown by default):
     //   List<String> visibleColumns = Arrays.asList(FIRST_NAME, LAST_NAME, EMAIL, PROFESSION, EDIT_COLUMN);
        List<String> visibleColumns = Arrays.asList(FIRST_NAME, LAST_NAME, EDIT_COLUMN);
        grid.getColumns().forEach(column -> {
            String key = column.getKey();
            if (!visibleColumns.contains(key)) {
                grid.removeColumn(column);
            }
        });

        // Reorder the columns (alphabetical by default)
        grid.setColumnOrder(grid.getColumnByKey(FIRST_NAME),
                grid.getColumnByKey(LAST_NAME),
             //   grid.getColumnByKey(EMAIL),
           //     grid.getColumnByKey(PROFESSION),
                grid.getColumnByKey(EDIT_COLUMN));
    }

    private void setupDataProvider() {
        PersonDataProvider dataProvider = new PersonDataProvider();
        crud.setDataProvider(dataProvider);
        crud.addDeleteListener(
                deleteEvent -> dataProvider.delete(deleteEvent.getItem()));
        crud.addSaveListener(
                saveEvent -> dataProvider.persist(saveEvent.getItem()));
    }

}
