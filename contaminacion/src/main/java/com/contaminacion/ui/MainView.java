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
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.router.Route;
import org.springframework.beans.factory.annotation.Autowired;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
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
        Div form = new Div();
        form.getStyle().set("min-width", "360px");

        Button save = new Button("Guardar");
        save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        save.addClickListener(e -> onSave());

        Button predict = new Button("Predecir 24h");
        predict.addClickListener(e -> onPredict());

        Button export = new Button("Exportar CSV");
        export.addClickListener(e -> onExport());

        form.add(co2Field, so2Field, no2Field, pm25Field, o3Field, tempField, vientoField, humedadField, new HorizontalLayout(save, predict, export));
        return form;
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
        // create data URL for download
        String base64 = Base64.getEncoder().encodeToString(csv.getBytes(StandardCharsets.UTF_8));
        String href = "data:text/csv;base64," + base64;
        Anchor download = new Anchor(href, "Descargar reporte.csv");
        download.getElement().setAttribute("download", "reporte_zonas.csv");
        add(download);
        download.getElement().callJsFunction("click");
        remove(download);
    }

    private void refreshGrid() {
        grid.setItems(zonaRepository.findAll());
    }
}

