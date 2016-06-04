package cc.devfun.protobuf.plugin;

import com.google.protobuf.DescriptorProtos;
import com.google.protobuf.GeneratedMessage;
import com.google.protobuf.TextFormat;
import com.google.protobuf.compiler.PluginProtos;

import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.List;

public class ProtocGenMd {
    private final static String PROTOC_GEN_MD_ARGS = "PROTOC_GEN_MD_ARGS";

    public static void main(String[] args) throws Exception {
        String protocGenMdArgs = System.getenv(PROTOC_GEN_MD_ARGS);
        if (protocGenMdArgs == null) {
            System.err.println("ERROR: environment PROTOC_GEN_MD_ARGS not set.");
            System.exit(1);
        }

        CodeGeneratorFactory factory = new MarkdownGeneratorFactory();
        ProtocGenMd gen = new ProtocGenMd(System.in, factory, protocGenMdArgs);
        gen.generate();
        System.exit(0);
    }

    private InputStream input;
    private CodeGeneratorFactory factory;
    private String outdir;

    public ProtocGenMd(InputStream input, CodeGeneratorFactory factory, String outdir) {
        this.input = input;
        this.factory = factory;
        this.outdir = outdir;
    }

    private void printProto(PluginProtos.CodeGeneratorRequest request) throws Exception {
        List<DescriptorProtos.FileDescriptorProto> protos = request.getProtoFileList();
        PrintStream ps = System.out;
        ps.println("=================");
        for (DescriptorProtos.FileDescriptorProto proto : protos) {
            ps.println(proto.getName());
        }
        ps.println("-----------------");
        ps.println(TextFormat.printToUnicodeString(request));
    }

    public void generate() throws Exception {
        com.google.protobuf.Parser<? extends GeneratedMessage> parser = PluginProtos.CodeGeneratorRequest.PARSER;
        PluginProtos.CodeGeneratorRequest request = (PluginProtos.CodeGeneratorRequest) parser.parseFrom(input);
//        printProto(request);
        CodeGenerator cg = factory.createCodeGenerator();
        cg.generate(outdir, request);
    }
}
