package pl.argo.argomobile;

import android.content.Context;
import android.os.StrictMode;
import androidx.room.Room;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import pl.argo.argomobile.data.*;
import pl.argo.argomobile.data.dto.RoverDto;
import pl.argo.argomobile.data.dto.RoverMapper;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

public class RoverService {

    private static RoverService instance;

    RoverDao roverDao;
    JointDao jointDao;

    private final RoverMapper roverMapper = new RoverMapper();

    private RoverService(Context applicationContext) {
        AppDatabase database = Room.databaseBuilder(applicationContext, AppDatabase.class, "database-name").allowMainThreadQueries().build();
        roverDao = database.roverDao();
        jointDao = database.jointDao();
    }

    public static RoverService getInstance(Context applicationContext) {
        if (instance == null) instance = new RoverService(applicationContext);
        return instance;
    }

    public RoverDto getRoverById(int id) {
        return roverMapper.toDto(roverDao.getById(id), jointDao.getAllByRoverId(id));
    }

    public List<RoverDto> getAllRovers() {
        List<Rover> rovers = roverDao.getAll();
        List<RoverDto> roverDtos = new ArrayList<>();

        for (Rover rover : rovers) {
            List<Joint> roverJoints = jointDao.getAllByRoverId(rover.id);

            roverDtos.add(roverMapper.toDto(rover, roverJoints));
        }

        return roverDtos;
    }

    /**
     * Method updating rovers list from "WebRoverConfigurator" application API.
     */
    //@SneakyThrows
    public boolean updateRoversFromApi() {

        // Allow internet connection on main thread not recommended, but easier and faster to implement
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        String sURL = "http://ec2-18-198-201-108.eu-central-1.compute.amazonaws.com:8080/api/rovers";//TODO: change when app maintainer will change

        JsonElement root;
        try {
            URL url = new URL(sURL);
            URLConnection request = url.openConnection();
            request.connect();

            root = JsonParser.parseReader(new InputStreamReader((InputStream) request.getContent())); //Convert the input stream to a json element
        } catch (IOException e) {
            return false;
        }

        // Convert to a JSON object to print data

        JsonArray allJsonRovers = root.getAsJsonArray();

        jointDao.deleteAll();
        roverDao.deleteAll();

        allJsonRovers.forEach(jsonRover -> {
            JsonObject jsonRoverObject = jsonRover.getAsJsonObject();
            Rover rover = new Rover.Builder()
                    .id(jsonRoverObject.get("id").getAsInt())
                    .name(jsonRoverObject.get("name").getAsString())
                    .topicPrefix(jsonRoverObject.get("topicPrefix").getAsString())
                    .build();

            JsonArray jsonJoints = jsonRoverObject.getAsJsonArray("joints");
            jsonJoints.forEach(jsonJoint -> {
                JsonObject jsonJointObject = jsonJoint.getAsJsonObject();
                Joint joint = new Joint.Builder()
                        .id(jsonJointObject.get("id").getAsInt())
                        .jointName(jsonJointObject.get("jointName").getAsString())
                        .roverId(rover.id)
                        .build();

                jointDao.insert(joint);
            });
            roverDao.insert(rover);
        });

        return true;
    }
}
