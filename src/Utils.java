import java.util.ArrayList;

/**
 * Created by lukasz on 09.04.17.
 */
public class Utils {

    private static ArrayList<Bridge> warsawBridges;

    public static ArrayList<Bridge> getWarsawBridges() {
        if (warsawBridges != null) {
            return warsawBridges;
        }
        warsawBridges = new ArrayList<>();

        warsawBridges.add(new Bridge(1, "Siekierkowski", 52.214444, 21.0925));
        warsawBridges.add(new Bridge(2, "Łazienkowski", 52.225278, 21.049167));
        warsawBridges.add(new Bridge(3, "Poniatowskiego", 52.235556, 21.04));
        warsawBridges.add(new Bridge(4, "Świętokrzyski", 52.241667, 21.033889));
        warsawBridges.add(new Bridge(5, "Śląsko-Dąbrowski", 52.249444 , 21.022778));
        warsawBridges.add(new Bridge(6, "Gdański", 52.260833, 21.01));
        warsawBridges.add(new Bridge(7, "Grota-Rowieckiego", 52.287222, 20.995278));
        warsawBridges.add(new Bridge(8, "Północny", 52.307222, 20.950833));

        return warsawBridges;
    }

}
