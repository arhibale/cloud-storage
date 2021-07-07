import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

public class FilesUtils {

    public static void main(String[] args) {
        File file = new File("img.png");
        System.out.println(file.exists());
        File copy = new File("copy.png");
        System.out.println(copy.exists());

        byte[] buffer = new byte[256];
        try (FileInputStream is = new FileInputStream(file);
             FileOutputStream os = new FileOutputStream(copy)) {
            int read;
            while ((read = is.read(buffer)) != -1) {
                os.write(buffer, 0 ,read);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
