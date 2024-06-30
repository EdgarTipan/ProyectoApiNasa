package ec.edu.uce;

import javax.swing.*;

import ec.edu.uce.controller.NasaApi;
import ec.edu.uce.model.Api;
import ec.edu.uce.view.PhotoViewer;

public class Main {

    public static void main(String[] args) {
        Api api = new NasaApi();
        SwingUtilities.invokeLater(() -> {
            PhotoViewer gui = new PhotoViewer(api);
            gui.setVisible(true);
        });
    }
}

