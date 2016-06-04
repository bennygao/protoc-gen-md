package cc.devfun.protobuf.plugin.vm;

import com.google.protobuf.DescriptorProtos;
import com.google.protobuf.compiler.PluginProtos;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class GeneratorContext {
    private PluginProtos.CodeGeneratorRequest request;
    List<CommentedDescriptor> allServices = null;
    List<CommentedDescriptor> allMessages = null;
    private Utils utils = Utils.getInstance();

    public GeneratorContext(PluginProtos.CodeGeneratorRequest request) {
        this.request = request;
    }

    public List<CommentedDescriptor> getAllServices() {
        if (allServices != null) {
            return allServices;
        }

        allServices = new ArrayList<>();
        List<DescriptorProtos.FileDescriptorProto> protoList = request.getProtoFileList();
        for (DescriptorProtos.FileDescriptorProto proto : protoList) {
            List<CommentedDescriptor> services = getServicesComments(proto);
            allServices.addAll(services);
        }

        Collections.sort(allServices, new Comparator<CommentedDescriptor>() {
            @Override
            public int compare(CommentedDescriptor o1, CommentedDescriptor o2) {
                DescriptorProtos.ServiceDescriptorProto sd1 = (DescriptorProtos.ServiceDescriptorProto) o1.getDescriptor();
                DescriptorProtos.ServiceDescriptorProto sd2 = (DescriptorProtos.ServiceDescriptorProto) o2.getDescriptor();
                return sd1.getName().compareTo(sd2.getName());
            }
        });

        return allServices;
    }

    public  List<CommentedDescriptor> getAllMessages() {
        if (allMessages != null) {
            return allMessages;
        }

        allMessages = new ArrayList<>();
        List<DescriptorProtos.FileDescriptorProto> protoList = request.getProtoFileList();
        for (DescriptorProtos.FileDescriptorProto proto : protoList) {
            List<CommentedDescriptor> messages = getMessagesComments(proto);
            allMessages.addAll(messages);
        }

        Collections.sort(allMessages, new Comparator<CommentedDescriptor>() {
            @Override
            public int compare(CommentedDescriptor o1, CommentedDescriptor o2) {
                DescriptorProtos.DescriptorProto cd1 = (DescriptorProtos.DescriptorProto) o1.getDescriptor();
                DescriptorProtos.DescriptorProto cd2 = (DescriptorProtos.DescriptorProto) o2.getDescriptor();
                return cd1.getName().compareTo(cd2.getName());
            }
        });

        return allMessages;
    }

    public CommentedDescriptor getInputMessage(DescriptorProtos.MethodDescriptorProto method) {
        return getMessageByName(method.getInputType());
    }

    public CommentedDescriptor getOutputMessage(DescriptorProtos.MethodDescriptorProto method) {
        return getMessageByName(method.getOutputType());
    }

    public CommentedDescriptor getMessageByName(String name) {
        for (CommentedDescriptor cd : getAllMessages()) {
            DescriptorProtos.DescriptorProto msg = (DescriptorProtos.DescriptorProto) cd.getDescriptor();
            if (msg.getName().equalsIgnoreCase(utils.getBaseName(name))) {
                return cd;
            }
        }

        throw new RuntimeException("Cannot find message for name " + name);
    }

    public List<CommentedDescriptor> getNestedMessages(CommentedDescriptor cd) {
        List<CommentedDescriptor> nestedMessages = new ArrayList<>();
        getNestedMessages(cd, nestedMessages);
        return nestedMessages;
    }

    private void getNestedMessages(CommentedDescriptor cdMessage, List<CommentedDescriptor> nestedMessages) {
        DescriptorProtos.DescriptorProto message = (DescriptorProtos.DescriptorProto) cdMessage.getDescriptor();
        List<DescriptorProtos.FieldDescriptorProto> fields = message.getFieldList();
        for (DescriptorProtos.FieldDescriptorProto field : fields) {
            if (field.getType() == DescriptorProtos.FieldDescriptorProto.Type.TYPE_MESSAGE) {
                CommentedDescriptor subMessage = getMessageByName(field.getTypeName());
                nestedMessages.add(subMessage);
                getNestedMessages(subMessage, nestedMessages);
            }
        }
    }

    private List<CommentedDescriptor> getServicesComments(DescriptorProtos.FileDescriptorProto proto) {
        List<DescriptorProtos.ServiceDescriptorProto> services = proto.getServiceList();
        List<DescriptorProtos.SourceCodeInfo.Location> locations = proto.getSourceCodeInfo().getLocationList();
        String servicePathFormat = "" + DescriptorProtos.FileDescriptorProto.SERVICE_FIELD_NUMBER + ".%d";
        List<CommentedDescriptor> commentedServices = new ArrayList<>();

        String servicePath;
        DescriptorProtos.SourceCodeInfo.Location location;
        int idx = 0;
        for (DescriptorProtos.ServiceDescriptorProto service : services) {
            List<String> comments = new ArrayList<>();
            servicePath = String.format(servicePathFormat, idx);
            location = searchLocation(locations, servicePath);
            if (location != null) {
                trimComments(comments, location.getLeadingComments());
                trimComments(comments, location.getTrailingComments());
            }

            CommentedDescriptor cd = new CommentedDescriptor(service, comments);
            commentedServices.add(cd);

            int methodCount = service.getMethodCount();
            for (int i = 0; i < methodCount; ++i) {
                comments = new ArrayList<>();
                DescriptorProtos.MethodDescriptorProto method = service.getMethod(i);
                String fieldPath = servicePath + '.' + DescriptorProtos.FileDescriptorProto.PACKAGE_FIELD_NUMBER + '.' + i;
                location = searchLocation(locations, fieldPath);
                if (location != null) {
                    trimComments(comments, location.getLeadingComments());
                    trimComments(comments, location.getTrailingComments());
                }

                CommentedDescriptor cdMethod = new CommentedDescriptor(method, comments);
                cd.addChild(cdMethod);
            }

            ++idx;
        }

        return commentedServices;
    }

    private List<CommentedDescriptor> getMessagesComments(DescriptorProtos.FileDescriptorProto proto) {
        List<CommentedDescriptor> messagesComments = new ArrayList<>();
        List<DescriptorProtos.DescriptorProto> messages = proto.getMessageTypeList();
        List<DescriptorProtos.SourceCodeInfo.Location> locations = proto.getSourceCodeInfo().getLocationList();

        String messagePathFormat = "" + DescriptorProtos.FileDescriptorProto.MESSAGE_TYPE_FIELD_NUMBER + ".%d";
        String messagePath;
        DescriptorProtos.SourceCodeInfo.Location location;
        int idx = 0;
        for (DescriptorProtos.DescriptorProto message : messages) {
            List<String> comments = new ArrayList<>();
            messagePath = String.format(messagePathFormat, idx);
            location = searchLocation(locations, messagePath);
            if (location != null) {
                trimComments(comments, location.getLeadingComments());
                trimComments(comments, location.getTrailingComments());
            }

            CommentedDescriptor cd = new CommentedDescriptor(message, comments);
            messagesComments.add(cd);

            int fieldCount = message.getFieldCount();
            for (int i = 0; i < fieldCount; ++i) {
                List<String> fieldComments = new ArrayList<>();
                DescriptorProtos.FieldDescriptorProto field = message.getField(i);
                String fieldPath = messagePath + '.' + DescriptorProtos.FileDescriptorProto.PACKAGE_FIELD_NUMBER + '.' + i;
                location = searchLocation(locations, fieldPath);
                if (location != null) {
                    trimComments(fieldComments, location.getLeadingComments());
                    trimComments(fieldComments, location.getTrailingComments());
                }

                CommentedDescriptor cdField = new CommentedDescriptor(field, fieldComments);
                cd.addChild(cdField);
            }

            ++idx;
        }

        return messagesComments;
    }

    public String getJavaPackage() {
        for (DescriptorProtos.FileDescriptorProto proto : request.getProtoFileList()) {
            return proto.getOptions().getJavaPackage();
        }

        return "unknown";
    }

    public String getJavaPackagePath() {
        String javaPackage = getJavaPackage();
        StringBuilder sb = new StringBuilder();
        sb.append('/').append(javaPackage);
        return sb.toString().replaceAll("\\.", "/");
    }


    private String getPath(DescriptorProtos.SourceCodeInfo.Location location) {
        StringBuilder path = new StringBuilder();
        List<Integer> pathList = location.getPathList();
        int size = pathList.size();
        for (int i = 0; i < size; ++i) {
            if (i > 0) {
                path.append('.');
            }

            path.append(pathList.get(i));
        }

        return path.toString();
    }

    private DescriptorProtos.SourceCodeInfo.Location searchLocation(List<DescriptorProtos.SourceCodeInfo.Location> locations,
                                                                    String path) {
        for (DescriptorProtos.SourceCodeInfo.Location location : locations) {
            if (getPath(location).equalsIgnoreCase(path)) {
                return location;
            }
        }

        return null;
    }

    private void trimComments(List<String> commentsList, String comments) {
        if (comments == null) {
            return;
        }

        String[] sections = comments.split("\\n");
        for (String section : sections) {
            String text = utils.trimLeadingBlankspace(section);
            if (text.length() >= 2 && text.charAt(0) == '\\' && text.charAt(1) == 't') {
                text = "\t" + text.substring(2);
            } else {
                while (true) {
                    text = utils.trimLeadingBlankspace(text);
                    if (text.length() == 0) {
                        break;
                    } else if (text.charAt(0) == '*') {
                        if (text.length() == 1) {
                            text = "";
                        } else if (text.charAt(1) != '*') {
                            text = text.substring(1);
                        } else {
                            text = text.substring(2);
                        }
                    } else {
                        break;
                    }
                }
            }

            text = utils.trimLeadingBlankspace(text);
            commentsList.add(text);
        }
    }

    public String toJson(DescriptorProtos.DescriptorProto message) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream pstream = new PrintStream(baos);
        toJson(message, pstream, 0, false);
        pstream.close();
        return baos.toString();
    }

    private void toJson(DescriptorProtos.DescriptorProto message, PrintStream pstream, int level, boolean inList) {
        int currentLevel = level;
        int repeat = 1;
        if (level == 0 || inList) {
            printTab(pstream, currentLevel);
        }

        pstream.println("{");

        int fieldNo = 0;
        int fieldsCount = message.getFieldCount();
        for (DescriptorProtos.FieldDescriptorProto field : message.getFieldList()) {
            printTab(pstream, currentLevel + 1);
            pstream.print(String.format("\"%s\": ", field.getName()));

            if (utils.isRepeated(field)) {
                ++currentLevel;
                pstream.println("[");
                repeat = 2;
            } else {
                repeat = 1;
            }

            for (int i = 0; i < repeat; ++i) {
                if (utils.isBasicType(field)) {
                    String defaultValue = field.getDefaultValue();
                    if (defaultValue == null) {
                        pstream.print("null");
                    } else if (utils.isStringType(field)) {
                        pstream.print(String.format("\"%s\"", defaultValue));
                    } else {
                        pstream.print(defaultValue);
                    }
                } else {
                    DescriptorProtos.DescriptorProto subMessage = (DescriptorProtos.DescriptorProto)
                            getMessageByName(field.getTypeName()).getDescriptor();
                    toJson(subMessage, pstream, currentLevel + 1, repeat > 1);
                }

                if (repeat > 1) {
                    if (i < (repeat - 1)) {
                        pstream.println(',');
                    }
                }
            }

            if (utils.isRepeated(field)) {
                pstream.println();
                printTab(pstream, currentLevel);
                pstream.print(']');
                --currentLevel;
            }

            if (fieldNo < (fieldsCount - 1)) {
                pstream.println(',');
            } else {
                pstream.println();
            }

            ++fieldNo;
        }
        printTab(pstream, currentLevel);
        pstream.print("}");
    }

    private void printTab(PrintStream pstream, int level) {
        for (int i = 0; i < level + 1; ++i) {
            pstream.print('\t');
        }
    }
}
