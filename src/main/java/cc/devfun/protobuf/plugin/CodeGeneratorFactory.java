package cc.devfun.protobuf.plugin;

public interface CodeGeneratorFactory {
    public CodeGenerator createCodeGenerator() throws Exception;
}