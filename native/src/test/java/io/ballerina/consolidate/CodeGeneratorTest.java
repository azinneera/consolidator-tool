package io.ballerina.consolidate;

import io.ballerina.projects.PackageManifest;
import io.ballerina.projects.buildtools.ToolContext;
import io.ballerina.projects.directory.BuildProject;
import org.testng.Assert;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.stream.Stream;

public class CodeGeneratorTest {

    private ByteArrayOutputStream console;
    private PrintStream printStream;

    @BeforeClass
    public void setup() throws IOException {
        System.setProperty("java.command", "java");
        System.setProperty("ballerina.home",
                Paths.get("../build/target/extracted-distributions/jballerina-tools-zip/jballerina-tools-2201.11.0").toAbsolutePath().toString());
        this.console = new ByteArrayOutputStream();
        this.printStream = new PrintStream(this.console);
        copyDirectory(Paths.get("src/test/resources/test-consolidate"), Paths.get("build/test-consolidate"));
    }

    @Test
    public void testValidToolUsage () {
        try {
            BuildProject project = BuildProject.load(Paths.get("build/test-consolidate"));
            PackageManifest.Tool tool = project.currentPackage().manifest().tools().getFirst();
            ToolContext toolContext = ToolContext.from(tool, project.currentPackage(), System.out);
            CodeGenerator codeGenerator = new CodeGenerator();
            Assert.assertFalse(Files.exists(project.sourceRoot().resolve("generated")));
            codeGenerator.execute(toolContext);

            // Verify the content in generated/consolidate_main.bal file
            Path generatedMainBalPath = project.sourceRoot().resolve("generated/consolidate_main.bal");
            Assert.assertTrue(Files.exists(generatedMainBalPath));
            String consolidated_mainBal = """
                import ballerina/log;
                
                public function main() {
                    log:printInfo("Started all the services");
                }
                """;
            Assert.assertEquals(Files.readString(generatedMainBalPath), consolidated_mainBal);

            // Verify the content in generated/consolidate.bal file
            Path generatedImportsBal = project.sourceRoot().resolve("generated/consolidate.bal");
            Assert.assertTrue(Files.exists(generatedImportsBal));
            String consolidatedBal = """
                import myOrg/svc1 as _;
                import myOrg/svc2 as _;
                import myOrg/svc3 as _;
                """;
            Assert.assertEquals(Files.readString(generatedImportsBal), consolidatedBal);
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    protected String readOutput(boolean silent) throws IOException {
        String output = "";
        output = console.toString();
        console.close();
        console = new ByteArrayOutputStream();
        printStream = new PrintStream(console);
        if (!silent) {
            PrintStream out = System.out;
            out.println(output);
        }
        return output;
    }

    public static void copyDirectory(Path source, Path target) throws IOException {
        Files.createDirectories(target);
        try (Stream<Path> paths = Files.walk(source)) {
            paths.forEach(src -> {
                try {
                    Path destination = target.resolve(source.relativize(src));
                    Files.copy(src, destination, StandardCopyOption.REPLACE_EXISTING);
                } catch (IOException e) {
                    throw new RuntimeException("Error copying file: " + src, e);
                }
            });
        }
    }
}