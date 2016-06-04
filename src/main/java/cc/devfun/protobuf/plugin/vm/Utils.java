package cc.devfun.protobuf.plugin.vm;

import com.google.protobuf.DescriptorProtos;
import com.google.protobuf.DescriptorProtos.MethodDescriptorProto;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;

public class Utils {
    static class SingletonHolder {
        static Utils singleton = new Utils();
    }

    public static Utils getInstance() {
        return SingletonHolder.singleton;
    }

    private Map<DescriptorProtos.FieldDescriptorProto.Type, String> typeNameMap;

    private Utils() {
        typeNameMap = new HashMap<>();
        typeNameMap.put(DescriptorProtos.FieldDescriptorProto.Type.TYPE_DOUBLE, "double");
        typeNameMap.put(DescriptorProtos.FieldDescriptorProto.Type.TYPE_FLOAT, "float");
        typeNameMap.put(DescriptorProtos.FieldDescriptorProto.Type.TYPE_INT64, "long");
        typeNameMap.put(DescriptorProtos.FieldDescriptorProto.Type.TYPE_UINT64, "long");
        typeNameMap.put(DescriptorProtos.FieldDescriptorProto.Type.TYPE_INT32, "int");
        typeNameMap.put(DescriptorProtos.FieldDescriptorProto.Type.TYPE_FIXED64, "long");
        typeNameMap.put(DescriptorProtos.FieldDescriptorProto.Type.TYPE_FIXED32, "int");
        typeNameMap.put(DescriptorProtos.FieldDescriptorProto.Type.TYPE_BOOL, "bool");
        typeNameMap.put(DescriptorProtos.FieldDescriptorProto.Type.TYPE_STRING, "string");
//        typeNameMap.put(DescriptorProtos.FieldDescriptorProto.Type.TYPE_GROUP, "");
//        typeNameMap.put(DescriptorProtos.FieldDescriptorProto.Type.TYPE_MESSAGE, "");
        typeNameMap.put(DescriptorProtos.FieldDescriptorProto.Type.TYPE_BYTES, "bytes");
        typeNameMap.put(DescriptorProtos.FieldDescriptorProto.Type.TYPE_UINT32, "int");
//        typeNameMap.put(DescriptorProtos.FieldDescriptorProto.Type.TYPE_ENUM, "");
        typeNameMap.put(DescriptorProtos.FieldDescriptorProto.Type.TYPE_SFIXED32, "int");
        typeNameMap.put(DescriptorProtos.FieldDescriptorProto.Type.TYPE_SFIXED64, "long");
        typeNameMap.put(DescriptorProtos.FieldDescriptorProto.Type.TYPE_SINT32, "int");
        typeNameMap.put(DescriptorProtos.FieldDescriptorProto.Type.TYPE_SINT64, "long");
    }

    public String getTypeName(DescriptorProtos.FieldDescriptorProto field) {
        String format = isRepeated(field) ? "list&lt;%s&gt;" : "%s";
        DescriptorProtos.FieldDescriptorProto.Type type = field.getType();
        String typename;
        if (type == DescriptorProtos.FieldDescriptorProto.Type.TYPE_MESSAGE) {
            typename = "__" + getBaseName(field.getTypeName()) + "__";
        } else {
            typename = typeNameMap.get(type);
        }

        return String.format(format, typename);
    }

    public boolean isMessageType(DescriptorProtos.FieldDescriptorProto field) {
        return field.getType() == DescriptorProtos.FieldDescriptorProto.Type.TYPE_MESSAGE;
    }

    public boolean isBasicType(DescriptorProtos.FieldDescriptorProto field) {
        return typeNameMap.containsKey(field.getType());
    }

    public boolean isStringType(DescriptorProtos.FieldDescriptorProto field) {
        return field.getType() == DescriptorProtos.FieldDescriptorProto.Type.TYPE_STRING;
    }

    public boolean isBasicType(String typeName) {
        String baseName = getBaseName(typeName);
        for (String name : typeNameMap.values()) {
            if (name.equalsIgnoreCase(typeName)) {
                return true;
            }
        }

        return false;
    }

    /**
     *
     * @param pathname
     * @return basename of pathname
     */
    public String getBaseName(String pathname) {
        int idx = pathname.lastIndexOf('.');
        return idx >= 0 ? pathname.substring(idx + 1) : pathname;
    }

    public String getFirstName(String name) {
        int idx = name.lastIndexOf('.');
        return idx >= 0 ? name.substring(0, idx) : name;
    }

    public String firstLetterUpperCaseFirstName(String name) {
        return firstLetterUpperCase(getFirstName(name));
    }

    public String firstLetterLowerCase(String name) {
        return name.substring(0, 1).toLowerCase() + name.substring(1);
    }

    public String firstLetterUpperCase(String name) {
        return name.substring(0, 1).toUpperCase() + name.substring(1);
    }

    public String firstLetterLowerCaseBaseName(String pathname) {
        String baseName = getBaseName(pathname);
        return firstLetterLowerCase(baseName);
    }

    public String firstLetterUpperCaseBaseName(String pathname) {
        String baseName = getBaseName(pathname);
        return firstLetterUpperCase(baseName);
    }

    public String getMethodPrototype(MethodDescriptorProto method) {
        return String.format("%s %s(%s)", method.getOutputType(), method.getName(), method.getInputType());
    }

    public String getSimpleMethodPrototype(MethodDescriptorProto method) {
        return String.format("%s %s(%s)", getBaseName(method.getOutputType()),
                getBaseName(method.getName()), getBaseName(method.getInputType()));
    }

    public String trimLeadingBlankspace(String text) {
        if (isEmpty(text)) {
            return text;
        }

        int position = -1;
        int size = text.length();
        for (int i = 0; i < size; ++i) {
            if (text.charAt(i) != ' ') {
                break;
            } else {
                position = i;
            }
        }

        if (position >= 0) {
            return text.substring(position + 1);
        } else {
            return text;
        }
    }

    public boolean isEmpty(String text) {
        if (text == null) {
            return true;
        } else if (text.length() == 0) {
            return true;
        } else {
            return false;
        }
    }

    public int hashCode(MethodDescriptorProto method) {
        return getMethodPrototype(method).hashCode();
    }

    public boolean isRepeated(DescriptorProtos.FieldDescriptorProto field) {
        DescriptorProtos.FieldDescriptorProto.Label label = field.getLabel();
        if (label != null) {
            if (label == DescriptorProtos.FieldDescriptorProto.Label.LABEL_REPEATED) {
                return true;
            }
        }

        return false;
    }

    public String H(int level) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < level; ++i) {
            sb.append('#');
        }

        return sb.toString();
    }

    public boolean isCode(String text) {
        return text.startsWith("\t");
    }
}
