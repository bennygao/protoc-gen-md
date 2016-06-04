package cc.devfun.protobuf.plugin;

import com.google.protobuf.compiler.PluginProtos;

public interface CodeGenerator {
    public void generate(String srcDir, PluginProtos.CodeGeneratorRequest request) throws Exception;
}

