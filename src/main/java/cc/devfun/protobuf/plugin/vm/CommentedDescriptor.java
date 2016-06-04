package cc.devfun.protobuf.plugin.vm;

import com.google.protobuf.DescriptorProtos;
import com.google.protobuf.GeneratedMessage;

import java.util.*;

public class CommentedDescriptor {
    public interface TagSetter {
        void setValue(String value);
    }

    Map<String, TagSetter> tagsSetter;
    Map<String, String> tagsValue;


    GeneratedMessage descriptor;
    List<String> comments;
    List<CommentedDescriptor> children;

    private String title; // @title
    private String httpMethod; // @method

    public CommentedDescriptor(GeneratedMessage descriptor, List<String> comments) {
        this.descriptor = descriptor;
        this.comments = comments == null ? new ArrayList<String>() : comments;
        this.children = new ArrayList<>();
        this.tagsValue = new HashMap<>();

        this.title = "";
        this.httpMethod = "";

        tagsSetter = new HashMap<>();
        tagsSetter.put("title", new TagSetter() {
            @Override
            public void setValue(String value) {
                title = value;
            }
        });
        tagsSetter.put("method", new TagSetter() {
            @Override
            public void setValue(String value) {
                httpMethod = value.toUpperCase();
            }
        });

        searchTags();
    }

    public String getTitle() {
        return title;
    }

    public String getHttpMethod() {
        return httpMethod;
    }

    private void searchTags() {
        List<String> trimmedComments = new ArrayList<>();
        int position;
        boolean found;
        for (String text : comments) {
            found = false;
            for (Map.Entry<String, TagSetter> entry : tagsSetter.entrySet()) {
                String tag = entry.getKey();
                TagSetter setter = entry.getValue();
                String tagIdent = "@" + tag;
                tagIdent = tagIdent.toLowerCase();
                if ((position = text.toLowerCase().indexOf(tagIdent)) >= 0) {
                    String value = text.substring(tagIdent.length());
                    value = Utils.getInstance().trimLeadingBlankspace(value);
                    setter.setValue(value);
                    found = true;
                    break;
                }
            }

            if (!found) {
                trimmedComments.add(Utils.getInstance().trimLeadingBlankspace(text));
            }
        }

        this.comments = trimmedComments;
    }

    public GeneratedMessage getDescriptor() {
        return this.descriptor;
    }

    public List<String> getComments() {
        return this.comments;
    }

    public List<CommentedDescriptor> getChildren() {
        return this.children;
    }

    public void addChild(CommentedDescriptor child) {
        this.children.add(child);
    }

    @Override
    public String toString() {
        if (descriptor instanceof DescriptorProtos.ServiceDescriptorProto) {
            DescriptorProtos.ServiceDescriptorProto sdp = (DescriptorProtos.ServiceDescriptorProto) descriptor;
            return "service:" + sdp.getName() + " // " + getCommentInOneLine();
        } else if (descriptor instanceof DescriptorProtos.DescriptorProto) {
            DescriptorProtos.DescriptorProto msg = (DescriptorProtos.DescriptorProto) descriptor;
            return "message:" + msg.getName() + " // " + getCommentInOneLine();
        } else if (descriptor instanceof DescriptorProtos.FieldDescriptorProto) {
            DescriptorProtos.FieldDescriptorProto field = (DescriptorProtos.FieldDescriptorProto) descriptor;
            DescriptorProtos.FieldDescriptorProto.Label label = field.getLabel();

            if (label == DescriptorProtos.FieldDescriptorProto.Label.LABEL_REPEATED) {
                return "field: list<" + field.getTypeName() + "> " + field.getName() + " = " + field.getNumber() +
                        " //" + getCommentInOneLine();
            } else {
                String typeName = Utils.getInstance().getTypeName(field);
                return "field:" + typeName + ' ' + field.getName() + " = " + field.getNumber() +
                        " //" + getCommentInOneLine();
            }
        } else if (descriptor instanceof DescriptorProtos.MethodDescriptorProto) {
            DescriptorProtos.MethodDescriptorProto mdp = (DescriptorProtos.MethodDescriptorProto) descriptor;
            return "method:" + Utils.getInstance().getSimpleMethodPrototype(mdp) + " //" + getCommentInOneLine();
        } else {
            return "unknown";
        }
    }

    public String getCommentInOneLine() {
        StringBuilder sb = new StringBuilder();
        for (String s : comments) {
            sb.append(s).append(' ');
        }

        return sb.toString();
    }
}
