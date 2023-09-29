package awesome.console.util;

/**
 * @author anyesu
 */
public class SystemUtils {

    public static String getOsName() {
        return System.getProperty("os.name");
    }


    public static boolean isWindows() {
        String osName = getOsName();
        return osName != null && osName.toLowerCase().startsWith("windows");
    }
}
