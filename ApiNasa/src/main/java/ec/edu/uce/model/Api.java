package ec.edu.uce.model;

import java.io.IOException;
import java.util.List;

public interface Api {
    List<MarsPhoto> fetchPhotos() throws IOException;
}