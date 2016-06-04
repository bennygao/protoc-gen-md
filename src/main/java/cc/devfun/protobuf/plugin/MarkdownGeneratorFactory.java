package cc.devfun.protobuf.plugin;

import cc.devfun.protobuf.plugin.vm.MarkdownGenerator;

public class MarkdownGeneratorFactory implements CodeGeneratorFactory {
    @Override
    public CodeGenerator createCodeGenerator() throws Exception {
        return new MarkdownGenerator();
    }
}
