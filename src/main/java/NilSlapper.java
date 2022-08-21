import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

// Slaps nil loader
public class NilSlapper {
    public static void premain(String agentArgs) throws Exception {
        System.out.println("Running NilSlapper");
        Path prefixPath = Paths.get(Objects.requireNonNull(System.getProperty("nilslapper"), "nilslapper prop not set"));
        List<Path> cpPaths = Arrays.stream(System.getProperty("java.class.path").split(File.pathSeparator)).map(Paths::get).collect(Collectors.toList());
        ArrayList<Path> paths = new ArrayList<>();
        for (Path p : cpPaths) {
            if (p.startsWith(prefixPath)) {
                paths.add(p);
            }
        }
        System.out.println();
        System.out.println("Found " + paths.size() + " paths: ");
        paths.forEach(p -> System.out.println("    " + p));
        Path tmpDir = Paths.get(System.getProperty("java.io.tmpdir")).resolve("nilslapper" + System.currentTimeMillis());
        Files.createDirectories(tmpDir);
        File tmpFile = File.createTempFile("tmpnilslapper", ".jar", tmpDir.toFile());
        Files.delete(tmpFile.toPath());
        HashSet<String> written = new HashSet<>();
        try (ZipOutputStream os = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(tmpFile)))) {
            for (Path p : paths) {
                Files.walkFileTree(p, new SimpleFileVisitor<Path>() {
                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                        String fname = p.relativize(file).toString().replace('\\', '/');
                        if (written.add(fname)) {
                            os.putNextEntry(new ZipEntry(fname));
                            Files.copy(file, os);
                        }
                        return FileVisitResult.CONTINUE;
                    }
                });
            }
        }
        System.setProperty("nil.discoverPath", tmpDir.toString());
    }
}