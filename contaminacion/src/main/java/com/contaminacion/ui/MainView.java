package com.contaminacion.ui;

import com.contaminacion.model.Clima;
import com.contaminacion.model.Contaminacion;
import com.contaminacion.model.ZonaUrbana;
import com.contaminacion.repository.ZonaRepository;
import com.contaminacion.service.PrediccionService;
import com.contaminacion.service.ReporteService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.grid.Grid;
// ...existing code...
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.router.Route;
import org.springframework.beans.factory.annotation.Autowired;

import com.vaadin.flow.component.UI;
import java.util.List;

@Route("")
public class MainView extends VerticalLayout {

    private final ZonaRepository zonaRepository;
    private final PrediccionService prediccionService;
    private final ReporteService reporteService;

    private Grid<ZonaUrbana> grid = new Grid<>(ZonaUrbana.class, false);

    private NumberField co2Field = new NumberField("CO2 (mg/m3)");
    private NumberField so2Field = new NumberField("SO2 (ug/m3)");
    private NumberField no2Field = new NumberField("NO2 (ug/m3)");
    private NumberField pm25Field = new NumberField("PM2.5 (ug/m3)");
    private NumberField o3Field = new NumberField("O3 (ug/m3)");
    private NumberField tempField = new NumberField("Temperatura (C)");
    private NumberField vientoField = new NumberField("Vel. Viento (m/s)");
    private NumberField humedadField = new NumberField("Humedad (%)");

    private ZonaUrbana selected;
    private Div alertsBox;

    @Autowired
    public MainView(ZonaRepository zonaRepository, PrediccionService prediccionService, ReporteService reporteService) {
        this.zonaRepository = zonaRepository;
        this.prediccionService = prediccionService;
        this.reporteService = reporteService;

        setSizeFull();
        H2 title = new H2("Dashboard de Calidad del Aire - Quito (5 zonas)");
        add(title);

        configureGrid();

        HorizontalLayout layout = new HorizontalLayout();
        layout.setSizeFull();
        layout.add(grid, buildForm());

        add(layout);
        refreshGrid();
    }

    private void configureGrid() {
        grid.addColumn(ZonaUrbana::getId).setHeader("ID").setAutoWidth(true);
        grid.addColumn(ZonaUrbana::getNombre).setHeader("Zona");
        grid.addColumn(z -> {
            Contaminacion c = z.getContaminacionActual();
            return c == null ? "-" : String.format("PM2.5: %.1f", c.getPm25());
        }).setHeader("PM2.5");
        grid.addColumn(z -> z.getClimaActual() == null ? "-" : String.format("T:%.1fC", z.getClimaActual().getTemperatura())).setHeader("Clima");
        grid.addSelectionListener(evt -> evt.getFirstSelectedItem().ifPresent(this::onSelectZona));
        grid.setSelectionMode(Grid.SelectionMode.SINGLE);
    }

    private Div buildForm() {
        Div container = new Div();
        container.getStyle().set("min-width", "380px");

        // --- Edit selected zone section
        H2 editTitle = new H2("Editar zona seleccionada");
        Button save = new Button("Guardar");
        save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        save.addClickListener(e -> onSave());

        Button predict = new Button("Predecir 24h");
        predict.addClickListener(e -> onPredict());

        Button export = new Button("Exportar CSV");
        export.addClickListener(e -> onExport());

        FormLayout editForm = new FormLayout();
        editForm.add(co2Field, so2Field, no2Field, pm25Field, o3Field, tempField, vientoField, humedadField);
        HorizontalLayout editActions = new HorizontalLayout(save, predict, export);

        // --- Alerts section
        H2 alertsTitle = new H2("Alertas");
        if (this.alertsBox == null) {
            this.alertsBox = new Div();
            this.alertsBox.setId("alerts-box");
            this.alertsBox.getStyle().set("padding", "8px");
            this.alertsBox.getStyle().set("border", "1px solid var(--lumo-contrast-10pct)");
            this.alertsBox.getStyle().set("border-radius", "6px");
            this.alertsBox.getStyle().set("min-height", "80px");
        }

        // populate alerts now
        refreshAlerts(this.alertsBox);

        // --- Add zone section
        H2 addTitle = new H2("Añadir nueva zona");
        FormLayout addForm = new FormLayout();
        TextField nombreField = new TextField("Nombre");
        ComboBox<String> tipoBox = new ComboBox<>("Tipo");
        tipoBox.setItems("Residencial", "Industrial");
        NumberField fabricasField = new NumberField("N.º fábricas");
        fabricasField.setMin(0);
        Checkbox restriccionBox = new Checkbox("Restricción vehicular activa");

        NumberField addCo2 = new NumberField("CO2 (mg/m3)");
        NumberField addSo2 = new NumberField("SO2 (ug/m3)");
        NumberField addNo2 = new NumberField("NO2 (ug/m3)");
        NumberField addPm25 = new NumberField("PM2.5 (ug/m3)");
        NumberField addO3 = new NumberField("O3 (ug/m3)");
        NumberField addTemp = new NumberField("Temperatura (C)");
        NumberField addViento = new NumberField("Vel. Viento (m/s)");
        NumberField addHumedad = new NumberField("Humedad (%)");

        Button addZonaBtn = new Button("Añadir zona");
        addZonaBtn.addThemeVariants(ButtonVariant.LUMO_SUCCESS);
        addZonaBtn.addClickListener(e -> onAddZona(nombreField, tipoBox, fabricasField, restriccionBox, addCo2, addSo2, addNo2, addPm25, addO3, addTemp, addViento, addHumedad));

        addForm.add(nombreField, tipoBox, fabricasField, restriccionBox, addCo2, addSo2, addNo2, addPm25, addO3, addTemp, addViento, addHumedad);

        container.add(editTitle, editForm, editActions, alertsTitle, this.alertsBox, addTitle, addForm, addZonaBtn);
        return container;
    }

    private void refreshAlerts(Div box) {
        box.removeAll();
        List<ZonaUrbana> zonas = zonaRepository.findAll();
        boolean any = false;
        for (ZonaUrbana z : zonas) {
            Contaminacion c = z.getContaminacionActual();
            String msg = c == null ? "Sin datos" : checkOms(c);
            if (msg != null && !msg.isEmpty()) {
                any = true;
                HorizontalLayout row = new HorizontalLayout();
                Span lbl = new Span(z.getNombre() + ": " + msg);
                Button aplicar = new Button("Aplicar medidas");
                aplicar.addClickListener(ev -> {
                    try {
                        z.aplicarMedidasMitigacion();
                        zonaRepository.save(z);
                        Notification.show("Medidas aplicadas a " + z.getNombre(), 3000, Notification.Position.TOP_CENTER);
                        refreshGrid();
                        refreshAlerts(this.alertsBox);
                    } catch (Exception ex) {
                        Notification.show("Error aplicando medidas: " + ex.getMessage(), 4000, Notification.Position.MIDDLE);
                    }
                });
                row.add(lbl, aplicar);
                box.add(row);
            }
        }
        if (!any) {
            box.add(new Span("No hay alertas"));
        }
    }

    private void onAddZona(TextField nombreField, ComboBox<String> tipoBox, NumberField fabricasField, Checkbox restriccionBox,
                           NumberField addCo2, NumberField addSo2, NumberField addNo2, NumberField addPm25, NumberField addO3,
                           NumberField addTemp, NumberField addViento, NumberField addHumedad) {
        String nombre = nombreField.getValue();
        if (nombre == null || nombre.trim().isEmpty()) {
            Notification.show("Ingrese un nombre para la zona", 3000, Notification.Position.MIDDLE);
            return;
        }
        String tipo = tipoBox.getValue();
        ZonaUrbana nueva;
        try {
            if ("Industrial".equals(tipo)) {
                int nFab = fabricasField.getValue() == null ? 0 : fabricasField.getValue().intValue();
                nueva = new com.contaminacion.model.ZonaIndustrial(null, nombre, nFab);
            } else {
                boolean r = restriccionBox.getValue() == null ? false : restriccionBox.getValue();
                nueva = new com.contaminacion.model.ZonaResidencial(null, nombre, r);
            }

            // set contaminacion/clima if provided
            double co2 = addCo2.getValue() == null ? 0.0 : addCo2.getValue();
            double so2 = addSo2.getValue() == null ? 0.0 : addSo2.getValue();
            double no2 = addNo2.getValue() == null ? 0.0 : addNo2.getValue();
            double pm25 = addPm25.getValue() == null ? 0.0 : addPm25.getValue();
            double o3 = addO3.getValue() == null ? 0.0 : addO3.getValue();
            com.contaminacion.model.Contaminacion c = new com.contaminacion.model.Contaminacion(co2, so2, no2, pm25, o3, null);
            nueva.setContaminacionActual(c);

            double t = addTemp.getValue() == null ? 20.0 : addTemp.getValue();
            double v = addViento.getValue() == null ? 0.0 : addViento.getValue();
            double h = addHumedad.getValue() == null ? 50.0 : addHumedad.getValue();
            nueva.setClimaActual(new com.contaminacion.model.Clima(t, v, h));

            zonaRepository.save(nueva);
            Notification.show("Zona añadida: " + nombre, 3000, Notification.Position.TOP_CENTER);
            refreshGrid();
            refreshAlerts(this.alertsBox);

            // clear fields
            nombreField.clear(); tipoBox.clear(); fabricasField.clear(); restriccionBox.clear();
            addCo2.clear(); addSo2.clear(); addNo2.clear(); addPm25.clear(); addO3.clear();
            addTemp.clear(); addViento.clear(); addHumedad.clear();
        } catch (Exception ex) {
            Notification.show("Error al crear zona: " + ex.getMessage(), 4000, Notification.Position.MIDDLE);
        }
    }

    private void onSelectZona(ZonaUrbana zona) {
        this.selected = zonaRepository.findById(zona.getId());
        if (selected == null) return;
        Contaminacion c = selected.getContaminacionActual();
        if (c != null) {
            co2Field.setValue(c.getCo2());
            so2Field.setValue(c.getSo2());
            no2Field.setValue(c.getNo2());
            pm25Field.setValue(c.getPm25());
            o3Field.setValue(c.getO3());
        }
        if (selected.getClimaActual() != null) {
            tempField.setValue(selected.getClimaActual().getTemperatura());
            vientoField.setValue(selected.getClimaActual().getVelocidadViento());
            humedadField.setValue(selected.getClimaActual().getHumedad());
        }
    }

    private void onSave() {
        if (selected == null) {
            Notification.show("Seleccione una zona del Grid primero.", 3000, Notification.Position.MIDDLE);
            return;
        }
        try {
            Contaminacion nuevo = new Contaminacion(co2Field.getValue(), so2Field.getValue(), no2Field.getValue(), pm25Field.getValue(), o3Field.getValue(), null);
            selected.setContaminacionActual(nuevo);
            selected.agregarRegistroHistorico(nuevo);
            selected.setClimaActual(new Clima(tempField.getValue(), vientoField.getValue(), humedadField.getValue()));
            zonaRepository.save(selected);
            Notification.show("Datos guardados.", 2000, Notification.Position.TOP_CENTER);
            refreshGrid();
            refreshAlerts(this.alertsBox);
        } catch (Exception ex) {
            Notification.show("Error al guardar: " + ex.getMessage(), 4000, Notification.Position.MIDDLE);
        }
    }

    private void onPredict() {
        if (selected == null) {
            Notification.show("Seleccione una zona para predecir.", 2000, Notification.Position.MIDDLE);
            return;
        }
        Contaminacion pred = prediccionService.predecir24h(selected);
        if (pred == null) {
            Notification.show("No se pudo generar la predicción.", 2000, Notification.Position.MIDDLE);
            return;
        }
        Dialog d = new Dialog();
        String content = String.format("Predicción 24h - CO2: %.2f, PM2.5: %.2f, NO2: %.2f, SO2: %.2f, O3: %.2f", pred.getCo2(), pred.getPm25(), pred.getNo2(), pred.getSo2(), pred.getO3());
        d.add(content);
        d.open();

        // alert if exceeds OMS thresholds
        String alertMsg = checkOms(pred);
        if (!alertMsg.isEmpty()) {
            Notification.show("ALERTA: " + alertMsg, 5000, Notification.Position.TOP_CENTER);
        }
    }

    private String checkOms(Contaminacion c) {
        StringBuilder sb = new StringBuilder();
        if (c.getPm25() > 15) sb.append("PM2.5 ");
        if (c.getNo2() > 25) sb.append("NO2 ");
        if (c.getSo2() > 40) sb.append("SO2 ");
        if (c.getO3() > 100) sb.append("O3 ");
        if (c.getCo2() > 4.0) sb.append("CO2 ");
        return sb.toString();
    }

    private void onExport() {
        List<ZonaUrbana> zonas = zonaRepository.findAll();
        String csv = reporteService.generarCsvResumen(zonas);

        // Use client-side Blob creation to trigger download reliably without deprecated server APIs
        // Pass CSV string as argument to executeJs (Vaadin will serialize it safely)
        UI.getCurrent().getPage().executeJs(
            "(function(csv, filename){ const blob = new Blob([csv], {type: 'text/csv;charset=utf-8'}); const url = URL.createObjectURL(blob); const a = document.createElement('a'); a.href = url; a.download = filename; document.body.appendChild(a); a.click(); setTimeout(()=>{ URL.revokeObjectURL(url); a.remove(); }, 100); })( $0, $1 );",
            csv, "reporte_zonas.csv");
    }

    private void refreshGrid() {
        grid.setItems(zonaRepository.findAll());
    }
}

