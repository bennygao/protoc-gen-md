package cc.devfun.protobuf.plugin;

import java.io.FileInputStream;

public class TestMain {
    public static void main(String[] args) throws Exception {

        CodeGeneratorFactory factory = new MarkdownGeneratorFactory();
        ProtocGenMd gen = new ProtocGenMd(new FileInputStream("test/proto.bin"), factory, "test");
        gen.generate();
        System.exit(0);
    }
}
