package ec.edu.uce.view;

import ec.edu.uce.model.Api;
import ec.edu.uce.model.MarsPhoto;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Edgar Tipan
 */

public class PhotoViewer extends JFrame {

    private JTable table;
    private JButton loadButton;
    private JButton viewImageButton;
    private JComboBox<String> cameraFilter;
    private JComboBox<String> dateOrderFilter;
    private JComboBox<String> idOrderFilter;
    private JCheckBox parallelCheckBox;
    private Api api;
    private List<MarsPhoto> allMarsPhotos;
    private boolean useParallel = false;

    public PhotoViewer(Api api) {
        this.api = api;
        setTitle("API Nasa");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        setLayout(new BorderLayout());

        table = new JTable();
        add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel topPanel = new JPanel();
        parallelCheckBox = new JCheckBox("Usar Programacion Paralela");
        topPanel.add(parallelCheckBox);
        add(topPanel, BorderLayout.NORTH);

        JPanel controlPanel = new JPanel();
        controlPanel.setLayout(new BoxLayout(controlPanel, BoxLayout.Y_AXIS));

        JPanel buttonPanel = new JPanel();
        loadButton = new JButton("Cargar datos");
        viewImageButton = new JButton("Ver imagen");
        buttonPanel.add(loadButton);
        buttonPanel.add(viewImageButton);

        JPanel filterPanel = new JPanel();
        cameraFilter = new JComboBox<>();
        dateOrderFilter = new JComboBox<>(new String[]{"Ascendente", "Descendente"});
        idOrderFilter = new JComboBox<>(new String[]{"Ascendente", "Descendente"});

        filterPanel.add(new JLabel("Cámara:"));
        filterPanel.add(cameraFilter);
        filterPanel.add(new JLabel("Fecha:"));
        filterPanel.add(dateOrderFilter);
        filterPanel.add(new JLabel("ID:"));
        filterPanel.add(idOrderFilter);

        controlPanel.add(buttonPanel);
        controlPanel.add(filterPanel);

        add(controlPanel, BorderLayout.SOUTH);

        loadButton.addActionListener(e -> loadPhotos());
        viewImageButton.addActionListener(e -> viewImage());
        parallelCheckBox.addActionListener(e -> toggleParallel());
        cameraFilter.addActionListener(e -> filterPhotos());
        dateOrderFilter.addActionListener(e -> filterPhotos());
        idOrderFilter.addActionListener(e -> filterPhotos());

        setFiltersEnabled(false);
    }

    private void loadPhotos() {
        try {
            allMarsPhotos = api.fetchPhotos();
            updateFilters(allMarsPhotos);
            updateTable(allMarsPhotos);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void viewImage() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow >= 0) {
            String imageUrl = (String) table.getValueAt(selectedRow, 4);
            JFrame imageFrame = new JFrame("Imagen");
            imageFrame.setSize(600, 400);
            imageFrame.setLayout(new BorderLayout());
            imageFrame.setLocationRelativeTo(null);

            JLabel imageLabel = new JLabel("Cargando imagen...", SwingConstants.CENTER);
            imageFrame.add(new JScrollPane(imageLabel), BorderLayout.CENTER);
            imageFrame.setVisible(true);

            SwingUtilities.invokeLater(() -> {
                try {
                    ImageIcon imageIcon = new ImageIcon(new java.net.URL(imageUrl));
                    imageLabel.setIcon(imageIcon);
                    imageLabel.setText(""); // Clear the loading text
                } catch (Exception e) {
                    e.printStackTrace();
                    imageLabel.setText("Fallo al cargar imagen");
                }
            });
        }
    }

    private void filterPhotos() {
        if (allMarsPhotos == null) return;

        String selectedCamera = (String) cameraFilter.getSelectedItem();
        String dateOrder = (String) dateOrderFilter.getSelectedItem();
        String idOrder = (String) idOrderFilter.getSelectedItem();

        List<MarsPhoto> filteredMarsPhotos;
        if (useParallel) {
            filteredMarsPhotos = allMarsPhotos.parallelStream()
                    .filter(marsPhoto -> (selectedCamera == null || marsPhoto.getCamera().getName().equals(selectedCamera)))
                    .collect(Collectors.toList());
        } else {
            filteredMarsPhotos = allMarsPhotos.stream()
                    .filter(marsPhoto -> (selectedCamera == null || marsPhoto.getCamera().getName().equals(selectedCamera)))
                    .collect(Collectors.toList());
        }

        if ("Ascendente".equals(dateOrder)) {
            filteredMarsPhotos.sort(Comparator.comparing(MarsPhoto::getEarth_date));
        } else if ("Descendente".equals(dateOrder)) {
            filteredMarsPhotos.sort(Comparator.comparing(MarsPhoto::getEarth_date).reversed());
        }

        if ("Ascendente".equals(idOrder)) {
            filteredMarsPhotos.sort(Comparator.comparingInt(MarsPhoto::getId));
        } else if ("Descendente".equals(idOrder)) {
            filteredMarsPhotos.sort(Comparator.comparingInt(MarsPhoto::getId).reversed());
        }

        updateTable(filteredMarsPhotos);
    }

    private void updateFilters(List<MarsPhoto> marsPhotos) {
        Set<String> cameraNames = marsPhotos.stream().map(marsPhoto -> marsPhoto.getCamera().getName()).collect(Collectors.toSet());

        cameraFilter.setModel(new DefaultComboBoxModel<>(cameraNames.toArray(new String[0])));
        setFiltersEnabled(true);
    }

    private void setFiltersEnabled(boolean enabled) {
        cameraFilter.setEnabled(enabled);
        dateOrderFilter.setEnabled(enabled);
        idOrderFilter.setEnabled(enabled);
    }

    private void toggleParallel() {
        useParallel = parallelCheckBox.isSelected();
        filterPhotos();
    }

    public void updateTable(List<MarsPhoto> marsPhotos) {
        String[] columnNames = {"ID", "Sol", "Fecha", "Cámara", "URL de la imagen"};
        DefaultTableModel model = new DefaultTableModel(columnNames, 0);
        for (MarsPhoto marsPhoto : marsPhotos) {
            Object[] row = {marsPhoto.getId(), marsPhoto.getSol(), marsPhoto.getEarth_date(),
                    marsPhoto.getCamera().getName(), marsPhoto.getImg_src()};
            model.addRow(row);
        }
        table.setModel(model);
    }
}
