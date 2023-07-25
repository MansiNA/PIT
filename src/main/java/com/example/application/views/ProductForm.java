package com.example.application.views;

import com.example.application.data.entity.ProductHierarchie;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.shared.Registration;

public class ProductForm extends FormLayout {


    ComboBox<String> msm_Type = new ComboBox("MSM Type");

    TextField product_name = new TextField("Produkt");
    TextField node = new TextField("Knoten");

    TextField exportTime_id = new TextField("Export Zeitpunkt");

    Button save = new Button("Save");
    Button delete = new Button("Delete");
    Button close = new Button("Cancel");
    private ProductHierarchie productHierarchie;

    Binder<ProductHierarchie> binder = new BeanValidationBinder<>(ProductHierarchie.class);
    public ProductForm() {
        addClassName("product-form");
        binder.bindInstanceFields(this);

        msm_Type.getItemLabelGenerator();
        msm_Type.setItems("MSM Post", "MSM PRE");

        // Optional: Setze einen Standardwert
        msm_Type.setValue("MSM Post");

        add(msm_Type,node,product_name, exportTime_id, createButtonsLayout());
    }

    public void setProduct(ProductHierarchie productHierarchie){ binder.setBean(productHierarchie);}

    private Component createButtonsLayout() {
        save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        delete.addThemeVariants(ButtonVariant.LUMO_ERROR);
        close.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        save.addClickShortcut(Key.ENTER);
        close.addClickShortcut(Key.ESCAPE);

        save.addClickListener(event -> validateAndSave());
        delete.addClickListener(event -> fireEvent(new DeleteEvent(this, binder.getBean())));
        close.addClickListener(event -> fireEvent(new CloseEvent(this)));

        binder.addStatusChangeListener(e -> save.setEnabled(binder.isValid())); // <4>
        return new HorizontalLayout(save, delete, close);
    }

    private void validateAndSave() {
        if(binder.isValid()) {
            System.out.println("Save-Button gedrückt!");

            fireEvent(new SaveEvent(this, binder.getBean()));



        }
    }

    // Events
    public static abstract class ProductFormEvent extends ComponentEvent<ProductForm> {
        private ProductHierarchie productHierarchie;

        protected ProductFormEvent(ProductForm source, ProductHierarchie product) {
            super(source, false);
            this.productHierarchie = product;
        }

        public ProductHierarchie getProduct() {
            return productHierarchie;
        }
    }

    public static class SaveEvent extends ProductFormEvent {
        SaveEvent(ProductForm source, ProductHierarchie productHierarchie) {super(source, productHierarchie);
        }
    }

    public static class DeleteEvent extends ProductFormEvent {
        DeleteEvent(ProductForm source, ProductHierarchie productHierarchie) {super(source, productHierarchie);
        }

    }

    public static class CloseEvent extends ProductFormEvent {
        CloseEvent(ProductForm source) {
            super(source, null);
        }
    }

/*    public Registration addDeleteListener(ComponentEventListener<DeleteEvent> listener) {

        return addListener(DeleteEvent.class, listener);
    }

    public void addSaveListener(ComponentEventListener<SaveEvent> listener) {
       // System.out.println("Save Event Listener wurde registriert");
        addListener(SaveEvent.class, listener);
    }
    public Registration addCloseListener(ComponentEventListener<CloseEvent> listener) {
        return addListener(CloseEvent.class, listener);
    }*/


    public <T extends ComponentEvent<?>> Registration addListener(Class<T> eventType, ComponentEventListener<T>listener){
        return getEventBus().addListener(eventType, listener);
    }


}
