package cc.devfun.protobuf.plugin.vm;

import cc.devfun.protobuf.plugin.CodeGenerator;
import com.google.protobuf.DescriptorProtos;
import com.google.protobuf.compiler.PluginProtos;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;

import java.io.File;
import java.io.Writer;
import java.util.List;

public class MarkdownGenerator extends VelocityCodeGeneratorTemplate implements CodeGenerator {
    public MarkdownGenerator() throws Exception {
        super();
    }

    @Override
    public void generate(String srcDir, PluginProtos.CodeGeneratorRequest request) throws Exception {
        GeneratorContext gc = new GeneratorContext(request);
        VelocityContext vc = new VelocityContext();

        String javaPackage = gc.getJavaPackage();
        vc.put("package", javaPackage);
        vc.put("context", gc);
        vc.put("util", Utils.getInstance());

        File path = getSourcePath(srcDir, javaPackage);
        path.mkdirs();

        Template template = Velocity.getTemplate("method.md");
        List<CommentedDescriptor> allServices = gc.getAllServices();
        for (CommentedDescriptor service : allServices) {
            DescriptorProtos.ServiceDescriptorProto sdp = (DescriptorProtos.ServiceDescriptorProto) service.getDescriptor();
            String serviceName = sdp.getName();
            vc.put("serviceName", sdp.getName());
            File fullpath = new File(path, serviceName);
            fullpath.mkdirs();
            for (CommentedDescriptor cdMethod : service.getChildren()) {
                DescriptorProtos.MethodDescriptorProto method = (DescriptorProtos.MethodDescriptorProto) cdMethod.getDescriptor();
                String methodName = method.getName();
                File target = new File(fullpath, methodName + ".md");
                Writer writer = getSourceWriter(target, "utf-8");

                vc.put("method", cdMethod);
                vc.put("input", gc.getInputMessage(method));
                vc.put("output", gc.getOutputMessage(method));

                template.merge(vc, writer);
                writer.close();
            }
        }
    }
}
